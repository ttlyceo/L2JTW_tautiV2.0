/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver;

import gnu.trove.procedure.TObjectProcedure;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jserver.Config;
import com.l2jserver.L2DatabaseFactory;
import com.l2jserver.gameserver.datatables.ClanTable;
import com.l2jserver.gameserver.datatables.OfflineTradersTable;
import com.l2jserver.gameserver.instancemanager.CHSiegeManager;
import com.l2jserver.gameserver.instancemanager.CastleManorManager;
import com.l2jserver.gameserver.instancemanager.CursedWeaponsManager;
import com.l2jserver.gameserver.instancemanager.GlobalVariablesManager;
import com.l2jserver.gameserver.instancemanager.GrandBossManager;
import com.l2jserver.gameserver.instancemanager.HellboundManager;
import com.l2jserver.gameserver.instancemanager.ItemAuctionManager;
import com.l2jserver.gameserver.instancemanager.ItemsOnGroundManager;
import com.l2jserver.gameserver.instancemanager.QuestManager;
import com.l2jserver.gameserver.instancemanager.RaidBossSpawnManager;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.entity.Hero;
import com.l2jserver.gameserver.model.olympiad.Olympiad;
import com.l2jserver.gameserver.network.L2GameClient;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.communityserver.CommunityServerThread;
import com.l2jserver.gameserver.network.gameserverpackets.ServerStatus;
import com.l2jserver.gameserver.network.serverpackets.ServerClose;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;
import com.l2jserver.gameserver.util.Broadcast;
import com.l2jserver.gameserver.datatables.MessageTable;
import com.l2jserver.gameserver.model.L2CoreMessage;


/**
 *
 * This class provides the functions for shutting down and restarting the server
 * It closes all open client connections and saves all data.
 *
 * @version $Revision: 1.2.4.5 $ $Date: 2005/03/27 15:29:09 $
 */
public class Shutdown extends Thread
{
	private static Logger _log = Logger.getLogger(Shutdown.class.getName());
	private static Shutdown _counterInstance = null;
	
	private int _secondsShut;
	private int _shutdownMode;
	public static final int SIGTERM = 0;
	public static final int GM_SHUTDOWN = 1;
	public static final int GM_RESTART = 2;
	public static final int ABORT = 3;
	private static final String[] MODE_TEXT = { "SIGTERM", "shutting down", "restarting", "aborting" };
	
	/**
	 * This function starts a shutdown count down from Telnet (Copied from Function startShutdown())
	 *
	 * @param seconds seconds until shutdown
	 */
	private void SendServerQuit(int seconds)
	{
		SystemMessage sysm = SystemMessage.getSystemMessage(SystemMessageId.THE_SERVER_WILL_BE_COMING_DOWN_IN_S1_SECONDS);
		sysm.addNumber(seconds);
		Broadcast.toAllOnlinePlayers(sysm);
	}
	
	public void startTelnetShutdown(String IP, int seconds, boolean restart)
	{
		_log.warning("IP: " + IP + " issued shutdown command. " + MODE_TEXT[_shutdownMode] + " in " + seconds + " seconds!");
		//_an.announceToAll("Server is " + _modeText[shutdownMode] + " in "+seconds+ " seconds!");
		
		if (restart)
		{
			_shutdownMode = GM_RESTART;
		}
		else
		{
			_shutdownMode = GM_SHUTDOWN;
		}
		
		if (_shutdownMode > 0)
		{
			switch (seconds)
			{
				case 540:
				case 480:
				case 420:
				case 360:
				case 300:
				case 240:
				case 180:
				case 120:
				case 60:
				case 30:
				case 10:
				case 5:
				case 4:
				case 3:
				case 2:
				case 1:
					break;
				default:
					SendServerQuit(seconds);
			}
		}
		
		if (_counterInstance != null)
		{
			_counterInstance._abort();
		}
		_counterInstance = new Shutdown(seconds, restart);
		_counterInstance.start();
	}
	
	/**
	 * This function aborts a running countdown
	 *
	 * @param IP IP Which Issued shutdown command
	 */
	public void telnetAbort(String IP)
	{
		_log.warning("IP: " + IP + " issued shutdown ABORT. " + MODE_TEXT[_shutdownMode] + " has been stopped!");
		
		if (_counterInstance != null)
		{
			_counterInstance._abort();
			Announcements _an = Announcements.getInstance();
			/* Move To MessageTable For L2JTW
			_an.announceToAll("Server aborts " + MODE_TEXT[_shutdownMode] + " and continues normal operation!");
			*/
			L2CoreMessage cm = new L2CoreMessage (MessageTable.Messages[44]);
			cm.addExtra(_shutdownMode);
			_an.announceToAll(cm.renderMsg());
		}
	}
	
	/**
	 * Default constructor is only used internal to create the shutdown-hook instance
	 *
	 */
	protected Shutdown()
	{
		_secondsShut = -1;
		_shutdownMode = SIGTERM;
	}
	
	/**
	 * This creates a countdown instance of Shutdown.
	 *
	 * @param seconds	how many seconds until shutdown
	 * @param restart	true is the server shall restart after shutdown
	 *
	 */
	public Shutdown(int seconds, boolean restart)
	{
		if (seconds < 0)
		{
			seconds = 0;
		}
		_secondsShut = seconds;
		if (restart)
		{
			_shutdownMode = GM_RESTART;
		}
		else
		{
			_shutdownMode = GM_SHUTDOWN;
		}
	}
	
	/**
	 * get the shutdown-hook instance
	 * the shutdown-hook instance is created by the first call of this function,
	 * but it has to be registrered externaly.
	 *
	 * @return	instance of Shutdown, to be used as shutdown hook
	 */
	public static Shutdown getInstance()
	{
		return SingletonHolder._instance;
	}
	
	/**
	 * this function is called, when a new thread starts
	 *
	 * if this thread is the thread of getInstance, then this is the shutdown hook
	 * and we save all data and disconnect all clients.
	 *
	 * after this thread ends, the server will completely exit
	 *
	 * if this is not the thread of getInstance, then this is a countdown thread.
	 * we start the countdown, and when we finished it, and it was not aborted,
	 * we tell the shutdown-hook why we call exit, and then call exit
	 *
	 * when the exit status of the server is 1, startServer.sh / startServer.bat
	 * will restart the server.
	 *
	 */
	@Override
	public void run()
	{
		if (this == SingletonHolder._instance)
		{
			TimeCounter tc = new TimeCounter();
			TimeCounter tc1 = new TimeCounter();
			try
			{
				if ((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && Config.RESTORE_OFFLINERS)
				{
					OfflineTradersTable.storeOffliners();
					_log.info("Offline Traders Table: Offline shops stored("+tc.getEstimatedTimeAndRestartCounter()+"ms).");
				}
			}
			catch (Throwable t)
			{
				_log.log(Level.WARNING, "Error saving offline shops.",t);
			}
			
			try
			{
				disconnectAllCharacters();
				_log.info("All players disconnected and saved("+tc.getEstimatedTimeAndRestartCounter()+"ms).");
			}
			catch (Throwable t)
			{
				// ignore
			}
			
			// ensure all services are stopped
			try
			{
				GameTimeController.getInstance().stopTimer();
				_log.info("Game Time Controller: Timer stopped("+tc.getEstimatedTimeAndRestartCounter()+"ms).");
			}
			catch (Throwable t)
			{
				// ignore
			}
			
			// stop all threadpolls
			try
			{
				ThreadPoolManager.getInstance().shutdown();
				_log.info("Thread Pool Manager: Manager has been shut down("+tc.getEstimatedTimeAndRestartCounter()+"ms).");
			}
			catch (Throwable t)
			{
				// ignore
			}
			
			try
			{
				CommunityServerThread.getInstance().interrupt();
				_log.info("Community Server Thread: Thread interruped("+tc.getEstimatedTimeAndRestartCounter()+"ms).");
			}
			catch (Throwable t)
			{
				// ignore
			}
			
			try
			{
				LoginServerThread.getInstance().interrupt();
				_log.info("Login Server Thread: Thread interruped("+tc.getEstimatedTimeAndRestartCounter()+"ms).");
			}
			catch (Throwable t)
			{
				// ignore
			}
			
			// last byebye, save all data and quit this server
			saveData();
			tc.restartCounter();
			
			// saveData sends messages to exit players, so shutdown selector after it
			try
			{
				GameServer.gameServer.getSelectorThread().shutdown();
				_log.info("Game Server: Selector thread has been shut down("+tc.getEstimatedTimeAndRestartCounter()+"ms).");
			}
			catch (Throwable t)
			{
				// ignore
			}
			
			// commit data, last chance
			try
			{
				L2DatabaseFactory.getInstance().shutdown();
				_log.info("L2Database Factory: Database connection has been shut down("+tc.getEstimatedTimeAndRestartCounter()+"ms).");
			}
			catch (Throwable t)
			{
				
			}
			
			// server will quit, when this function ends.
			if (SingletonHolder._instance._shutdownMode == GM_RESTART)
			{
				Runtime.getRuntime().halt(2);
			}
			else
			{
				Runtime.getRuntime().halt(0);
			}
			
			_log.info("The server has been successfully shut down in "+tc1.getEstimatedTime()/1000+"seconds.");
		}
		else
		{
			// gm shutdown: send warnings and then call exit to start shutdown sequence
			countdown();
			// last point where logging is operational :(
			_log.warning("GM shutdown countdown is over. " + MODE_TEXT[_shutdownMode] + " NOW!");
			switch (_shutdownMode)
			{
				case GM_SHUTDOWN:
					SingletonHolder._instance.setMode(GM_SHUTDOWN);
					System.exit(0);
					break;
				case GM_RESTART:
					SingletonHolder._instance.setMode(GM_RESTART);
					System.exit(2);
					break;
			}
		}
	}
	
	/**
	 * This functions starts a shutdown countdown
	 *
	 * @param activeChar	GM who issued the shutdown command
	 * @param seconds		seconds until shutdown
	 * @param restart		true if the server will restart after shutdown
	 */
	public void startShutdown(L2PcInstance activeChar, int seconds, boolean restart)
	{
		if (restart)
		{
			_shutdownMode = GM_RESTART;
		}
		else
		{
			_shutdownMode = GM_SHUTDOWN;
		}
		
		_log.warning("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") issued shutdown command. "
				+ MODE_TEXT[_shutdownMode] + " in " + seconds + " seconds!");
		
		if (_shutdownMode > 0)
		{
			switch (seconds)
			{
				case 540:
				case 480:
				case 420:
				case 360:
				case 300:
				case 240:
				case 180:
				case 120:
				case 60:
				case 30:
				case 10:
				case 5:
				case 4:
				case 3:
				case 2:
				case 1:
					break;
				default:
					SendServerQuit(seconds);
			}
		}
		
		if (_counterInstance != null)
		{
			_counterInstance._abort();
		}
		
		//		 the main instance should only run for shutdown hook, so we start a new instance
		_counterInstance = new Shutdown(seconds, restart);
		_counterInstance.start();
	}
	
	/**
	 * This function aborts a running countdown
	 *
	 * @param activeChar	GM who issued the abort command
	 */
	public void abort(L2PcInstance activeChar)
	{
		_log.warning("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") issued shutdown ABORT. "
				+ MODE_TEXT[_shutdownMode] + " has been stopped!");
		if (_counterInstance != null)
		{
			_counterInstance._abort();
			Announcements _an = Announcements.getInstance();
			/* Move To MessageTable For L2JTW
			_an.announceToAll("Server aborts " + MODE_TEXT[_shutdownMode] + " and continues normal operation!");
			*/
			L2CoreMessage cm = new L2CoreMessage (MessageTable.Messages[44]);
			cm.addExtra(_shutdownMode);
			_an.announceToAll(cm.renderMsg());
		}
	}
	
	/**
	 * set the shutdown mode
	 * @param mode	what mode shall be set
	 */
	private void setMode(int mode)
	{
		_shutdownMode = mode;
	}
	
	/**
	 * set shutdown mode to ABORT
	 *
	 */
	private void _abort()
	{
		_shutdownMode = ABORT;
	}
	
	/**
	 * this counts the countdown and reports it to all players
	 * countdown is aborted if mode changes to ABORT
	 */
	private void countdown()
	{
		
		try
		{
			while (_secondsShut > 0)
			{
				
				switch (_secondsShut)
				{
					case 540:
						SendServerQuit(540);
						break;
					case 480:
						SendServerQuit(480);
						break;
					case 420:
						SendServerQuit(420);
						break;
					case 360:
						SendServerQuit(360);
						break;
					case 300:
						SendServerQuit(300);
						break;
					case 240:
						SendServerQuit(240);
						break;
					case 180:
						SendServerQuit(180);
						break;
					case 120:
						SendServerQuit(120);
						break;
					case 60:
						LoginServerThread.getInstance().setServerStatus(ServerStatus.STATUS_DOWN); //avoids new players from logging in
						SendServerQuit(60);
						break;
					case 30:
						SendServerQuit(30);
						break;
					case 10:
						SendServerQuit(10);
						break;
					case 5:
						SendServerQuit(5);
						break;
					case 4:
						SendServerQuit(4);
						break;
					case 3:
						SendServerQuit(3);
						break;
					case 2:
						SendServerQuit(2);
						break;
					case 1:
						SendServerQuit(1);
						break;
				}
				
				_secondsShut--;
				
				int delay = 1000; //milliseconds
				Thread.sleep(delay);
				
				if (_shutdownMode == ABORT)
					break;
			}
		}
		catch (InterruptedException e)
		{
			//this will never happen
		}
	}
	
	/**
	 * this sends a last byebye, disconnects all players and saves data
	 *
	 */
	private void saveData()
	{
		switch (_shutdownMode)
		{
			case SIGTERM:
				_log.info("SIGTERM received. Shutting down NOW!");
				break;
			case GM_SHUTDOWN:
				_log.info("GM shutdown received. Shutting down NOW!");
				break;
			case GM_RESTART:
				_log.info("GM restart received. Restarting NOW!");
				break;
				
		}
		
		/*if (Config.ACTIVATE_POSITION_RECORDER)
			Universe.getInstance().implode(true);*/
		TimeCounter tc = new TimeCounter();
		// Seven Signs data is now saved along with Festival data.
		if (!SevenSigns.getInstance().isSealValidationPeriod())
		{
			SevenSignsFestival.getInstance().saveFestivalData(false);
			_log.info("SevenSignsFestival: Festival data saved("+tc.getEstimatedTimeAndRestartCounter()+"ms).");
		}
		
		// Save Seven Signs data before closing. :)
		SevenSigns.getInstance().saveSevenSignsData();
		_log.info("SevenSigns: Seven Signs data saved("+tc.getEstimatedTimeAndRestartCounter()+"ms).");
		SevenSigns.getInstance().saveSevenSignsStatus();
		_log.info("SevenSigns: Seven Signs status saved("+tc.getEstimatedTimeAndRestartCounter()+"ms).");
		
		// Save all raidboss and GrandBoss status ^_^
		RaidBossSpawnManager.getInstance().cleanUp();
		_log.info("RaidBossSpawnManager: All raidboss info saved("+tc.getEstimatedTimeAndRestartCounter()+"ms).");
		GrandBossManager.getInstance().cleanUp();
		_log.info("GrandBossManager: All Grand Boss info saved("+tc.getEstimatedTimeAndRestartCounter()+"ms).");
		HellboundManager.getInstance().cleanUp();
		_log.info("Hellbound Manager: Data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		_log.info("TradeController saving data.. This action may take some minutes! Please wait until completed!");
		TradeController.getInstance().dataCountStore();
		_log.info("TradeController: All count Item saved("+tc.getEstimatedTimeAndRestartCounter()+"ms).");
		ItemAuctionManager.getInstance().shutdown();
		_log.info("Item Auction Manager: All tasks stopped("+tc.getEstimatedTimeAndRestartCounter()+"ms).");
		Olympiad.getInstance().saveOlympiadStatus();
		_log.info("Olympiad System: Data saved("+tc.getEstimatedTimeAndRestartCounter()+"ms).");
		Hero.getInstance().shutdown();
		_log.info("Hero System: Data saved("+tc.getEstimatedTimeAndRestartCounter()+"ms).");
		ClanTable.getInstance().storeClanScore();
		_log.info("Clan System: Data saved("+tc.getEstimatedTimeAndRestartCounter()+"ms).");
		
		// Save Cursed Weapons data before closing.
		CursedWeaponsManager.getInstance().saveData();
		_log.info("Cursed Weapons Manager: Data saved("+tc.getEstimatedTimeAndRestartCounter()+"ms).");
		
		// Save all manor data
		CastleManorManager.getInstance().save();
		_log.info("Castle Manor Manager: Data saved("+tc.getEstimatedTimeAndRestartCounter()+"ms).");
		
		CHSiegeManager.getInstance().onServerShutDown();
		_log.info("CHSiegeManager: Siegable hall attacker lists saved!");
		
		// Save all global (non-player specific) Quest data that needs to persist after reboot
		QuestManager.getInstance().save();
		_log.info("Quest Manager: Data saved("+tc.getEstimatedTimeAndRestartCounter()+"ms).");
		
		// Save all global variables data
		GlobalVariablesManager.getInstance().saveVars();
		_log.info("Global Variables Manager: Variables saved("+tc.getEstimatedTimeAndRestartCounter()+"ms).");
		
		//Save items on ground before closing
		if (Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance().saveInDb();
			_log.info("Items On Ground Manager: Data saved("+tc.getEstimatedTimeAndRestartCounter()+"ms).");
			ItemsOnGroundManager.getInstance().cleanUp();
			_log.info("Items On Ground Manager: Cleaned up("+tc.getEstimatedTimeAndRestartCounter()+"ms).");
		}
		
		try
		{
			int delay = 5000;
			Thread.sleep(delay);
		}
		catch (InterruptedException e)
		{
			//never happens :p
		}
	}
	
	/**
	 * this disconnects all clients from the server
	 *
	 */
	private void disconnectAllCharacters()
	{
		L2World.getInstance().getAllPlayers().safeForEachValue(new DisconnectAllCharacters());
	}
	
	protected final class DisconnectAllCharacters implements TObjectProcedure<L2PcInstance>
	{
		private final Logger _log = Logger.getLogger(DisconnectAllCharacters.class.getName());
		
		@Override
		public final boolean execute(final L2PcInstance player)
		{
			if (player != null)
			{
				//Logout Character
				try
				{
					L2GameClient client = player.getClient();
					if (client != null && !client.isDetached())
					{
						client.close(ServerClose.STATIC_PACKET);
						client.setActiveChar(null);
						player.setClient(null);
					}
					player.deleteMe();
				}
				catch (Throwable t)
				{
					_log.log(Level.WARNING, "Failed logour char " + player, t);
				}
			}
			return true;
		}
	}
	
	/**
	 * A simple class used to track down the estimated time of method executions.
	 * Once this class is created, it saves the start time, and when you want to get
	 * the estimated time, use the getEstimatedTime() method.
	 */
	private static final class TimeCounter
	{
		private long _startTime;
		
		protected TimeCounter()
		{
			restartCounter();
		}
		
		protected void restartCounter()
		{
			_startTime = System.currentTimeMillis();
		}
		
		protected long getEstimatedTimeAndRestartCounter()
		{
			final long toReturn = System.currentTimeMillis() - _startTime;
			restartCounter();
			return toReturn;
		}
		
		protected long getEstimatedTime()
		{
			return System.currentTimeMillis() - _startTime;
		}
	}
	
	private static class SingletonHolder
	{
		protected static final Shutdown _instance = new Shutdown();
	}
}
