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
package com.l2jserver.gameserver.model.zone.type;

import java.util.concurrent.Future;

import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.instancemanager.JumpManager.Track;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.zone.L2ZoneType;
import com.l2jserver.gameserver.network.serverpackets.ExNotifyFlyMoveStart;

import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * L2JumpZone zones
 * @author ALF (r2max)
 */
public class L2JumpZone extends L2ZoneType
{
	private final TIntObjectHashMap<Future<?>> _task = new TIntObjectHashMap<>();
	private final int _startTask;
	private final int _reuseTask;
	private int _trackId;
	
	public L2JumpZone(int id)
	{
		super(id);
		
		_startTask = 10;
		_reuseTask = 500;
		_trackId = -1;
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("trackId"))
		{
			_trackId = Integer.parseInt(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}
	
	public int getTrackId()
	{
		return _trackId;
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if(!isInsideZone(character))
			return;
		character.setInsideZone(L2Character.ZONE_JUMP, true);
		
		if (character instanceof L2PcInstance)
		{
			L2PcInstance plr = (L2PcInstance) character;
			if (!plr.isAwaken())
			{
				return;
			}
			stopTask(plr);
			_task.put(plr.getObjectId(), ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new JumpReq(plr), _startTask, _reuseTask));
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_JUMP, false);
		stopTask(character);
	}
	
	@Override
	public void onDieInside(L2Character character)
	{
		onExit(character);
	}
	
	@Override
	public void onReviveInside(L2Character character)
	{
		onEnter(character);
	}
	
	protected void stopTask(L2Character character)
	{
		int poid = character.getObjectId();
		Future<?> t = _task.get(poid);
		_task.remove(poid);
		if (t != null)
		{
			t.cancel(false);
		}
	}
	
	class JumpReq implements Runnable
	{
		private final L2PcInstance player;
		
		JumpReq(L2PcInstance pl)
		{
			player = pl;
		}
		
		@Override
		public void run()
		{
			player.sendPacket(new ExNotifyFlyMoveStart());
		}
	}
}
