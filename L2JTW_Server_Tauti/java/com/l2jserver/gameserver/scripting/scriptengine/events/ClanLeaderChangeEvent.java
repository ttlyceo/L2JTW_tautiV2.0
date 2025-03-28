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
package com.l2jserver.gameserver.scripting.scriptengine.events;

import com.l2jserver.gameserver.model.L2Clan;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.scripting.scriptengine.events.impl.L2Event;

/**
 * @author TheOne
 */
public class ClanLeaderChangeEvent implements L2Event
{
	private L2Clan clan;
	private L2PcInstance newLeader;
	private L2PcInstance oldLeader;
	
	/**
	 * @return the clan
	 */
	public L2Clan getClan()
	{
		return clan;
	}
	
	/**
	 * @param clan the clan to set
	 */
	public void setClan(L2Clan clan)
	{
		this.clan = clan;
	}
	
	/**
	 * @return the newLeader
	 */
	public L2PcInstance getNewLeader()
	{
		return newLeader;
	}
	
	/**
	 * @param newLeader the newLeader to set
	 */
	public void setNewLeader(L2PcInstance newLeader)
	{
		this.newLeader = newLeader;
	}
	
	/**
	 * @return the oldLeader
	 */
	public L2PcInstance getOldLeader()
	{
		return oldLeader;
	}
	
	/**
	 * @param oldLeader the oldLeader to set
	 */
	public void setOldLeader(L2PcInstance oldLeader)
	{
		this.oldLeader = oldLeader;
	}
}
