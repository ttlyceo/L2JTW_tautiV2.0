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
package com.l2jserver.gameserver.network.clientpackets;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javolution.util.FastList;

import com.l2jserver.Config;
import com.l2jserver.gameserver.SevenSignsFestival;
import com.l2jserver.gameserver.instancemanager.AntiFeedManager;
import com.l2jserver.gameserver.model.L2Party;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.L2GameClient;
import com.l2jserver.gameserver.network.L2GameClient.GameClientState;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.CharSelectionInfo;
import com.l2jserver.gameserver.network.serverpackets.RestartResponse;
import com.l2jserver.gameserver.scripting.scriptengine.listeners.player.PlayerDespawnListener;
import com.l2jserver.gameserver.taskmanager.AttackStanceTaskManager;
import com.l2jserver.gameserver.datatables.MessageTable;

/**
 * This class ...
 *
 * @version $Revision: 1.11.2.1.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestRestart extends L2GameClientPacket
{
	private static final String _C__57_REQUESTRESTART = "[C] 57 RequestRestart";
	protected static final Logger _logAccounting = Logger.getLogger("accounting");
	private static List<PlayerDespawnListener> despawnListeners = new FastList<>();
	
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		
		if (player == null)
			return;
		
		if(player.getActiveEnchantItem() != null || player.getActiveEnchantAttrItem() != null)
		{
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		if (player.isLocked())
		{
			_log.warning("Player " + player.getName() + " tried to restart during class change.");
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		if (player.getPrivateStoreType() != 0)
		{
			/* Move To MessageTable For L2JTW
			player.sendMessage("Cannot restart while trading");
			*/
			player.sendMessage(332);
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		if (AttackStanceTaskManager.getInstance().getAttackStanceTask(player) && !(player.isGM() && Config.GM_RESTART_FIGHTING))
		{
			if (Config.DEBUG)
				_log.fine("Player " + player.getName() + " tried to logout while fighting.");
			
			player.sendPacket(SystemMessageId.CANT_RESTART_WHILE_FIGHTING);
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		// Prevent player from restarting if they are a festival participant
		// and it is in progress, otherwise notify party members that the player
		// is not longer a participant.
		if (player.isFestivalParticipant())
		{
			if (SevenSignsFestival.getInstance().isFestivalInitialized())
			{
				/* Move To MessageTable For L2JTW
				player.sendMessage("You cannot restart while you are a participant in a festival.");
				*/
				player.sendMessage(333);
				sendPacket(RestartResponse.valueOf(false));
				return;
			}
			
			final L2Party playerParty = player.getParty();
			
			if (playerParty != null)
				/* Move To MessageTable For L2JTW
				player.getParty().broadcastString(player.getName() + " has been removed from the upcoming festival.");
				*/
				player.getParty().broadcastString(player.getName() + MessageTable.Messages[334].getMessage());
		}

		for (PlayerDespawnListener listener : despawnListeners)
		{
			listener.onDespawn(player);
		}

		// Remove player from Boss Zone
		player.removeFromBossZone();
		
		final L2GameClient client = getClient();
		
		LogRecord record = new LogRecord(Level.INFO, "Logged out");
		record.setParameters(new Object[]{client});
		_logAccounting.log(record);
		
		// detach the client from the char so that the connection isnt closed in the deleteMe
		player.setClient(null);
		
		player.deleteMe();
		
		client.setActiveChar(null);
		AntiFeedManager.getInstance().onDisconnect(client);
		
		// return the client to the authed status
		client.setState(GameClientState.AUTHED);
		
		sendPacket(RestartResponse.valueOf(true));
		
		// send char list
		final CharSelectionInfo cl = new CharSelectionInfo(client.getAccountName(), client.getSessionId().playOkID1);
		sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
	}
	
	@Override
	public String getType()
	{
		return _C__57_REQUESTRESTART;
	}
	
	// Listeners
	/**
	 * Adds a despawn listener which will get triggered when a player despawns
	 * @param listener
	 */
	public static void addDespawnListener(PlayerDespawnListener listener)
	{
		if (!despawnListeners.contains(listener))
		{
			despawnListeners.add(listener);
		}
	}
	
	/**
	 * Removes a despawn listener
	 * @param listener
	 */
	public static void removeDespawnListener(PlayerDespawnListener listener)
	{
		despawnListeners.remove(listener);
	}
}