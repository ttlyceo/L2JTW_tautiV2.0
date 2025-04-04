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

import com.l2jserver.Config;
import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.zone.L2ZoneType;
import com.l2jserver.gameserver.network.SystemMessageId;

/**
 * A jail zone
 * @author durgus
 */
public class L2JailZone extends L2ZoneType
{
	public L2JailZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (character.isPlayer())
		{
			character.setInsideZone(L2Character.ZONE_JAIL, true);
			if (Config.JAIL_IS_PVP)
			{
				character.setInsideZone(L2Character.ZONE_PVP, true);
				character.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
			}
			if (Config.JAIL_DISABLE_TRANSACTION)
			{
				character.setInsideZone(L2Character.ZONE_NOSTORE, true);
			}
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (character.isPlayer())
		{
			character.setInsideZone(L2Character.ZONE_JAIL, false);
			if (Config.JAIL_IS_PVP)
			{
				character.setInsideZone(L2Character.ZONE_PVP, false);
				character.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
			}
			if (character.getActingPlayer().isInJail())
			{
				// when a player wants to exit jail even if he is still jailed, teleport him back to jail
				ThreadPoolManager.getInstance().scheduleGeneral(new BackToJail(character), 2000);
				/* Move To MessageTable For L2JTW
				character.sendMessage("You cannot cheat your way out of here. You must wait until your jail time is over.");
				*/
				character.sendMessage(508);
			}
			if (Config.JAIL_DISABLE_TRANSACTION)
			{
				character.setInsideZone(L2Character.ZONE_NOSTORE, false);
			}
		}
	}
	
	@Override
	public void onDieInside(L2Character character)
	{
	}
	
	@Override
	public void onReviveInside(L2Character character)
	{
	}
	
	private static class BackToJail implements Runnable
	{
		private final L2Character _activeChar;
		
		protected BackToJail(L2Character character)
		{
			_activeChar = character;
		}
		
		@Override
		public void run()
		{
			_activeChar.teleToLocation(-114356, -249645, -2984); // Jail
		}
	}
}
