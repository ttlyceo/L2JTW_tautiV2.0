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
package handlers.voicedcommandhandlers;

import com.l2jserver.gameserver.handler.IVoicedCommandHandler;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.util.Util;
import com.l2jserver.gameserver.datatables.MessageTable;
import com.l2jserver.gameserver.model.L2CoreMessage;

/**
 * @author Zoey76
 */
public class SetVCmd implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"set name",
		"set home",
		"set group"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String params)
	{
		if (command.equals("set"))
		{
			final L2Object target = activeChar.getTarget();
			if ((target == null) || !target.isPlayer())
			{
				return false;
			}
			
			final L2PcInstance player = activeChar.getTarget().getActingPlayer();
			if ((activeChar.getClan() == null) || (player.getClan() == null) || (activeChar.getClan().getClanId() != player.getClan().getClanId()))
			{
				return false;
			}
			
			if (params.startsWith("privileges"))
			{
				final String val = params.substring(11);
				if (!Util.isDigit(val))
				{
					return false;
				}
				
				final int n = Integer.parseInt(val);
				if (!((activeChar.getClanPrivileges() > n) || activeChar.isClanLeader()))
				{
					return false;
				}
				
				player.setClanPrivileges(n);
				L2CoreMessage cm = new L2CoreMessage (MessageTable.Messages[1204]);
				cm.addNumber(n);
				cm.addString(activeChar.getName());
				activeChar.sendMessage(cm.renderMsg());
			}
			else if (params.startsWith("title"))
			{
				
			}
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
