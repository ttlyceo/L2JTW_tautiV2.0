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
package handlers.admincommandhandlers;

import java.util.StringTokenizer;

import com.l2jserver.gameserver.handler.IAdminCommandHandler;
import com.l2jserver.gameserver.instancemanager.TransformationManager;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jserver.gameserver.network.serverpackets.SetupGauge;
import com.l2jserver.gameserver.util.Util;
import com.l2jserver.gameserver.datatables.MessageTable;

/**
 * This class handles following admin commands: polymorph
 * @version $Revision: 1.2.2.1.2.4 $ $Date: 2007/07/31 10:05:56 $
 */
public class AdminPolymorph implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_polymorph",
		"admin_unpolymorph",
		"admin_polymorph_menu",
		"admin_unpolymorph_menu",
		"admin_transform",
		"admin_untransform",
		"admin_transform_menu",
		"admin_untransform_menu",
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_untransform"))
		{
			L2Object obj = activeChar.getTarget();
			if (obj instanceof L2Character)
			{
				((L2Character) obj).stopTransformation(true);
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			}
		}
		else if (command.startsWith("admin_transform"))
		{
			L2Object obj = activeChar.getTarget();
			if (obj != null && obj.isPlayer())
			{
				L2PcInstance cha = obj.getActingPlayer();
				
				if (activeChar.isSitting())
				{
					activeChar.sendPacket(SystemMessageId.CANNOT_TRANSFORM_WHILE_SITTING);
					return false;
				}
				
				else if (cha.isTransformed() || cha.isInStance())
				{
					activeChar.sendPacket(SystemMessageId.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
					return false;
				}
				
				else if (cha.isInWater())
				{
					activeChar.sendPacket(SystemMessageId.YOU_CANNOT_POLYMORPH_INTO_THE_DESIRED_FORM_IN_WATER);
					return false;
				}
				
				else if (cha.isFlyingMounted() || cha.isMounted() || cha.isRidingStrider())
				{
					activeChar.sendPacket(SystemMessageId.YOU_CANNOT_POLYMORPH_WHILE_RIDING_A_PET);
					return false;
				}
				
				final String[] parts = command.split(" ");
				if (parts.length > 1)
				{
					if (Util.isDigit(parts[1]))
					{
						final int id = Integer.parseInt(parts[1]);
						if (!TransformationManager.getInstance().transformPlayer(id, cha))
						{
							cha.sendMessage(MessageTable.Messages[1800].getMessage() + id);
						}
					}
					else
					{
						activeChar.sendMessage("Usage: //transform <id>");
					}
				}
				else if (parts.length == 1)
				{
					cha.untransform();
				}
				else
				{
					activeChar.sendMessage("Usage: //transform <id>");
				}
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			}
		}
		if (command.startsWith("admin_polymorph"))
		{
			StringTokenizer st = new StringTokenizer(command);
			L2Object target = activeChar.getTarget();
			try
			{
				st.nextToken();
				String p1 = st.nextToken();
				if (st.hasMoreTokens())
				{
					String p2 = st.nextToken();
					doPolymorph(activeChar, target, p2, p1);
				}
				else
				{
					doPolymorph(activeChar, target, p1, "npc");
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //polymorph [type] <id>");
			}
		}
		else if (command.equals("admin_unpolymorph"))
		{
			doUnpoly(activeChar, activeChar.getTarget());
		}
		if (command.contains("_menu"))
		{
			showMainPage(activeChar, command);
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	/**
	 * @param activeChar
	 * @param obj
	 * @param id
	 * @param type
	 */
	private void doPolymorph(L2PcInstance activeChar, L2Object obj, String id, String type)
	{
		if (obj != null)
		{
			obj.getPoly().setPolyInfo(type, id);
			// animation
			if (obj instanceof L2Character)
			{
				L2Character Char = (L2Character) obj;
				MagicSkillUse msk = new MagicSkillUse(Char, 1008, 1, 4000, 0);
				Char.broadcastPacket(msk);
				SetupGauge sg = new SetupGauge(0, 4000);
				Char.sendPacket(sg);
			}
			// end of animation
			obj.decayMe();
			obj.spawnMe(obj.getX(), obj.getY(), obj.getZ());
			activeChar.sendMessage(1801);
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
		}
	}
	
	/**
	 * @param activeChar
	 * @param target
	 */
	private void doUnpoly(L2PcInstance activeChar, L2Object target)
	{
		if (target != null)
		{
			target.getPoly().setPolyInfo(null, "1");
			target.decayMe();
			target.spawnMe(target.getX(), target.getY(), target.getZ());
			activeChar.sendMessage(1802);
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
		}
	}
	
	/**
	 * @param activeChar
	 * @param command
	 */
	private void showMainPage(L2PcInstance activeChar, String command)
	{
		if (command.contains("transform"))
		{
			AdminHelpPage.showHelpPage(activeChar, "transform.htm");
		}
		else if (command.contains("abnormal"))
		{
			AdminHelpPage.showHelpPage(activeChar, "abnormal.htm");
		}
		else
		{
			AdminHelpPage.showHelpPage(activeChar, "effects_menu.htm");
		}
	}
}
