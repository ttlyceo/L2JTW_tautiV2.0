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

import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jserver.Config;
import com.l2jserver.gameserver.datatables.ClassListData;
import com.l2jserver.gameserver.datatables.SkillTable;
import com.l2jserver.gameserver.datatables.SkillTreesData;
import com.l2jserver.gameserver.handler.IAdminCommandHandler;
import com.l2jserver.gameserver.instancemanager.AwakingManager; //rocknow-God-Awaking
import com.l2jserver.gameserver.model.L2Clan;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.L2SkillLearn;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.skills.L2Skill;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jserver.gameserver.network.serverpackets.PledgeSkillList;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;
import com.l2jserver.util.StringUtil;
import com.l2jserver.gameserver.datatables.MessageTable;

/**
 * This class handles following admin commands:
 * <ul>
 * 	<li>show_skills</li>
 * 	<li>remove_skills</li>
 * 	<li>skill_list</li>
 * 	<li>skill_index</li>
 * 	<li>add_skill</li>
 * 	<li>remove_skill</li>
 * 	<li>get_skills</li>
 * 	<li>reset_skills</li>
 * 	<li>give_all_skills</li>
 * 	<li>give_all_skills_fs</li>
 * 	<li>admin_give_all_clan_skills</li>
 * 	<li>remove_all_skills</li>
 * 	<li>add_clan_skills</li>
 * 	<li>admin_setskill</li>
 * </ul>
 * @version 2012/02/26
 * Small fixes by Zoey76 05/03/2011
 */
public class AdminSkill implements IAdminCommandHandler
{
	private static Logger _log = Logger.getLogger(AdminSkill.class.getName());
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_show_skills",
		"admin_remove_skills",
		"admin_skill_list",
		"admin_skill_index",
		"admin_add_skill",
		"admin_remove_skill",
		"admin_get_skills",
		"admin_reset_skills",
		"admin_give_all_skills",
		"admin_give_all_skills_fs",
		"admin_give_all_clan_skills",
		"admin_remove_all_skills",
		"admin_add_clan_skill",
		"admin_setskill"
	};
	
	private static L2Skill[] adminSkills;
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equals("admin_show_skills"))
			showMainPage(activeChar);
		else if (command.startsWith("admin_remove_skills"))
		{
			try
			{
				String val = command.substring(20);
				removeSkillsPage(activeChar, Integer.parseInt(val));
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.startsWith("admin_skill_list"))
		{
			AdminHelpPage.showHelpPage(activeChar, "skills.htm");
		}
		else if (command.startsWith("admin_skill_index"))
		{
			try
			{
				String val = command.substring(18);
				AdminHelpPage.showHelpPage(activeChar, "skills/" + val + ".htm");
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.startsWith("admin_add_skill"))
		{
			try
			{
				String val = command.substring(15);
				adminAddSkill(activeChar, val);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //add_skill <skill_id> <level>");
			}
		}
		else if (command.startsWith("admin_remove_skill"))
		{
			try
			{
				String id = command.substring(19);
				int idval = Integer.parseInt(id);
				adminRemoveSkill(activeChar, idval);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //remove_skill <skill_id>");
			}
		}
		else if (command.equals("admin_get_skills"))
		{
			adminGetSkills(activeChar);
		}
		else if (command.equals("admin_reset_skills"))
		{
			adminResetSkills(activeChar);
		}
		else if (command.equals("admin_give_all_skills"))
		{
			adminGiveAllSkills(activeChar, false);
		}
		else if (command.equals("admin_give_all_skills_fs"))
		{
			adminGiveAllSkills(activeChar, true);
		}
		else if (command.equals("admin_give_all_clan_skills"))
		{
			adminGiveAllClanSkills(activeChar);
		}
		else if (command.equals("admin_remove_all_skills"))
		{
			final L2Object target = activeChar.getTarget();
			if ((target == null) || !target.isPlayer())
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return false;
			}
			final L2PcInstance player = target.getActingPlayer();
			for (L2Skill skill : player.getAllSkills())
			{
				player.removeSkill(skill);
			}
			activeChar.sendMessage(MessageTable.Messages[1828].getExtra(1) + player.getName() + MessageTable.Messages[1828].getExtra(2));
			player.sendMessage(1829);
			player.sendSkillList();
			player.broadcastUserInfo();
		}
		else if (command.startsWith("admin_add_clan_skill"))
		{
			try
			{
				String[] val = command.split(" ");
				adminAddClanSkill(activeChar, Integer.parseInt(val[1]), Integer.parseInt(val[2]));
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //add_clan_skill <skill_id> <level>");
			}
		}
		else if (command.startsWith("admin_setskill"))
		{
			String[] split = command.split(" ");
			int id = Integer.parseInt(split[1]);
			int lvl = Integer.parseInt(split[2]);
			L2Skill skill = SkillTable.getInstance().getInfo(id, lvl);
			activeChar.addSkill(skill);
			activeChar.sendSkillList();
			activeChar.sendMessage("You added yourself skill "+skill.getName()+"("+id+") level "+lvl);
		}
		return true;
	}
	
	/**
	 * This function will give all the skills that the target can learn at his/her level
	 * @param activeChar the active char
	 * @param includedByFs if {@code true} Forgotten Scroll skills will be delivered.
	 */
	private void adminGiveAllSkills(L2PcInstance activeChar, boolean includedByFs)
	{
		final L2Object target = activeChar.getTarget();
		if ((target == null) || !target.isPlayer())
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		final L2PcInstance player = target.getActingPlayer();
		//Notify player and admin
		AwakingManager.getInstance().AwakingRemoveSkills(player); //rocknow-God-Awaking
		activeChar.sendMessage(MessageTable.Messages[1830].getExtra(1) + player.giveAvailableSkills(includedByFs, true) + MessageTable.Messages[1830].getExtra(2) + player.getName());
		player.sendSkillList();
	}
	
	/**
	 * This function will give all the skills that the target's clan can learn at it's level.<br>
	 * If the target is not the clan leader, a system message will be sent to the Game Master.
	 * @param activeChar the active char, probably a Game Master.
	 */
	private void adminGiveAllClanSkills(L2PcInstance activeChar)
	{
		final L2Object target = activeChar.getTarget();
		if ((target == null) || !target.isPlayer())
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		final L2PcInstance player = target.getActingPlayer();
		final L2Clan clan = player.getClan();
		if (clan == null)
		{
			activeChar.sendPacket(SystemMessageId.TARGET_MUST_BE_IN_CLAN);
			return;
		}
		
		if (!player.isClanLeader())
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.TARGET_MUST_BE_IN_CLAN);
			sm.addPcName(player);
			activeChar.sendPacket(sm);
		}
		
		final List<L2SkillLearn> skills = SkillTreesData.getInstance().getAvailablePledgeSkills(clan);
		SkillTable st = SkillTable.getInstance();
		for (L2SkillLearn s : skills)
		{
			clan.addNewSkill(st.getInfo(s.getSkillId(), s.getSkillLevel()));
		}
		
		// Notify target and active char
		clan.broadcastToOnlineMembers(new PledgeSkillList(clan));
		for (L2PcInstance member : clan.getOnlineMembers(0))
		{
			member.sendSkillList();
		}
		
		activeChar.sendMessage("You gave " + skills.size() + " skills to " + player.getName() + "'s clan " + clan.getName() + ".");
		player.sendMessage("Your clan received " + skills.size() + " skills.");
	}
	
	/**
	 * TODO: Externalize HTML
	 * @param activeChar the active Game Master.
	 * @param page
	 */
	private void removeSkillsPage(L2PcInstance activeChar, int page)
	{
		final L2Object target = activeChar.getTarget();
		if ((target == null) || !target.isPlayer())
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return;
		}
		
		final L2PcInstance player = target.getActingPlayer();
		final L2Skill[] skills = player.getAllSkills().toArray(new L2Skill[player.getAllSkills().size()]);
		
		int maxSkillsPerPage = 10;
		int maxPages = skills.length / maxSkillsPerPage;
		if (skills.length > maxSkillsPerPage * maxPages)
			maxPages++;
		
		if (page > maxPages)
			page = maxPages;
		
		int skillsStart = maxSkillsPerPage * page;
		int skillsEnd = skills.length;
		if (skillsEnd - skillsStart > maxSkillsPerPage)
			skillsEnd = skillsStart + maxSkillsPerPage;
		
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		final StringBuilder replyMSG = StringUtil.startAppend(
				500 + maxPages * 50 + (skillsEnd - skillsStart + 1) * 50,
				"<html><body>" +
				"<table width=260><tr>" +
				"<td width=40><button value=\""+MessageTable.Messages[1831].getMessage()+"\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>" +
				"<td width=180><center>"+MessageTable.Messages[1832].getMessage()+"</center></td>" +
				"<td width=40><button value=\""+MessageTable.Messages[1833].getMessage()+"\" action=\"bypass -h admin_show_skills\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>" +
				"</tr></table>" +
				"<br><br>" +
				"<center>"+MessageTable.Messages[1834].getMessage()+"<font color=\"LEVEL\">",
				player.getName(),
				"</font></center>" +
				"<br><table width=270><tr><td>"+MessageTable.Messages[1835].getMessage(),
				String.valueOf(player.getLevel()),
				" ",
				ClassListData.getInstance().getClass(player.getClassId()).getClientCode(),
				"</td></tr></table>" +
				"<br><table width=270><tr><td>"+MessageTable.Messages[1836].getMessage()+"</td></tr>" +
				"<tr><td>"+MessageTable.Messages[1837].getMessage()+"</td></tr></table>" +
				"<br><center>"+MessageTable.Messages[1838].getMessage()+"</center>" +
				"<br>" +
				"<center><table width=270><tr>"
		);
		
		for (int x = 0; x < maxPages; x++)
		{
			int pagenr = x + 1;
			StringUtil.append(replyMSG,
					"<td><a action=\"bypass -h admin_remove_skills ",
					String.valueOf(x),
					"\">"+MessageTable.Messages[1839].getMessage(),
					String.valueOf(pagenr),
					MessageTable.Messages[1839].getExtra(1)+"</a></td>"
			);
		}
		
		replyMSG.append(
				"</tr></table></center>" +
				"<br><table width=270>" +
				"<tr><td width=80>"+MessageTable.Messages[1840].getExtra(1)+"</td><td width=60>"+MessageTable.Messages[1840].getExtra(2)+"</td><td width=40>"+MessageTable.Messages[1840].getExtra(3)+"</td></tr>"
		);
		
		for (int i = skillsStart; i < skillsEnd; i++) {
			StringUtil.append(replyMSG,
					"<tr><td width=80><a action=\"bypass -h admin_remove_skill ",
					String.valueOf(skills[i].getId()),
					"\">",
					skills[i].getName(),
					"</a></td><td width=60>",
					String.valueOf(skills[i].getLevel()),
					"</td><td width=40>",
					String.valueOf(skills[i].getId()),
					"</td></tr>"
			);
		}
		
		replyMSG.append(
				"</table>" +
				"<br><center><table>" +
				MessageTable.Messages[1841].getMessage() +
				"<tr><td>"+MessageTable.Messages[1840].getExtra(3)+"</td>" +
				"<td><edit var=\"id_to_remove\" width=110></td></tr>" +
				"</table></center>" +
				"<center><button value=\""+MessageTable.Messages[1842].getMessage()+"\" action=\"bypass -h admin_remove_skill $id_to_remove\" width=110 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center>" +
				"<br><center><button value=\""+MessageTable.Messages[1833].getMessage()+"\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center>" +
		"</body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	/**
	 * @param activeChar the active Game Master.
	 */
	private void showMainPage(L2PcInstance activeChar)
	{
		final L2Object target = activeChar.getTarget();
		if ((target == null) || !target.isPlayer())
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		final L2PcInstance player = target.getActingPlayer();
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile(activeChar.getHtmlPrefix(), "data/html/admin/charskills.htm");
		adminReply.replace("%name%", player.getName());
		adminReply.replace("%level%", String.valueOf(player.getLevel()));
		adminReply.replace("%class%", ClassListData.getInstance().getClass(player.getClassId()).getClientCode());
		activeChar.sendPacket(adminReply);
	}
	
	/**
	 * @param activeChar the active Game Master.
	 */
	private void adminGetSkills(L2PcInstance activeChar)
	{
		final L2Object target = activeChar.getTarget();
		if ((target == null) || !target.isPlayer())
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		final L2PcInstance player = target.getActingPlayer();
		if (player.getName().equals(activeChar.getName()))
		{
			player.sendPacket(SystemMessageId.CANNOT_USE_ON_YOURSELF);
		}
		else
		{
			L2Skill[] skills = player.getAllSkills().toArray(new L2Skill[player.getAllSkills().size()]);
			adminSkills = activeChar.getAllSkills().toArray(new L2Skill[activeChar.getAllSkills().size()]);
			for (L2Skill skill: adminSkills)
			{
				activeChar.removeSkill(skill);
			}
			for (L2Skill skill: skills)
			{
				activeChar.addSkill(skill, true);
			}
			activeChar.sendMessage(MessageTable.Messages[1843].getExtra(1) + player.getName() + MessageTable.Messages[1843].getExtra(2));
			activeChar.sendSkillList();
		}
		showMainPage(activeChar);
	}
	
	/**
	 * @param activeChar the active Game Master.
	 */
	private void adminResetSkills(L2PcInstance activeChar)
	{
		final L2Object target = activeChar.getTarget();
		if ((target == null) || !target.isPlayer())
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		final L2PcInstance player = target.getActingPlayer();
		if (adminSkills == null)
		{
			activeChar.sendMessage("You must get the skills of someone in order to do this.");
		}
		else
		{
			L2Skill[] skills = player.getAllSkills().toArray(new L2Skill[player.getAllSkills().size()]);
			for (L2Skill skill: skills)
			{
				player.removeSkill(skill);
			}
			for (L2Skill skill: activeChar.getAllSkills())
			{
				player.addSkill(skill, true);
			}
			for (L2Skill skill: skills)
			{
				activeChar.removeSkill(skill);
			}
			for (L2Skill skill: adminSkills)
			{
				activeChar.addSkill(skill, true);
			}
			player.sendMessage(MessageTable.Messages[1844].getExtra(1) + activeChar.getName() + MessageTable.Messages[1844].getExtra(2));
			activeChar.sendMessage(1845);
			adminSkills = null;
			activeChar.sendSkillList();
			player.sendSkillList();
		}
		showMainPage(activeChar);
	}
	
	/**
	 * @param activeChar the active Game Master.
	 * @param val
	 */
	private void adminAddSkill(L2PcInstance activeChar, String val)
	{
		final L2Object target = activeChar.getTarget();
		if ((target == null) || !target.isPlayer())
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			showMainPage(activeChar);
			return;
		}
		final L2PcInstance player = target.getActingPlayer();
		final StringTokenizer st = new StringTokenizer(val);
		if (st.countTokens() != 2)
		{
			showMainPage(activeChar);
		}
		else
		{
			L2Skill skill = null;
			try
			{
				String id = st.nextToken();
				String level = st.nextToken();
				int idval = Integer.parseInt(id);
				int levelval = Integer.parseInt(level);
				skill = SkillTable.getInstance().getInfo(idval, levelval);
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "", e);
			}
			if (skill != null)
			{
				String name = skill.getName();
				// Player's info.
				player.sendMessage(MessageTable.Messages[1846].getExtra(1) + name + MessageTable.Messages[1846].getExtra(2));
				player.addSkill(skill, true);
				player.sendSkillList();
				// Admin info.
				activeChar.sendMessage(MessageTable.Messages[1847].getExtra(1) + name + MessageTable.Messages[1847].getExtra(2) + player.getName() + MessageTable.Messages[1847].getExtra(3));
				if (Config.DEBUG)
					_log.fine("[GM]" + activeChar.getName() + " gave skill " + name + " to " + player.getName() + ".");
				activeChar.sendSkillList();
			}
			else
				activeChar.sendMessage(1848);
			showMainPage(activeChar); //Back to start
		}
	}
	
	/**
	 * @param activeChar the active Game Master.
	 * @param idval
	 */
	private void adminRemoveSkill(L2PcInstance activeChar, int idval)
	{
		final L2Object target = activeChar.getTarget();
		if ((target == null) || !target.isPlayer())
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		final L2PcInstance player = target.getActingPlayer();
		L2Skill skill = SkillTable.getInstance().getInfo(idval, player.getSkillLevel(idval));
		if (skill != null)
		{
			String skillname = skill.getName();
			player.sendMessage(MessageTable.Messages[1849].getExtra(1) + skillname + MessageTable.Messages[1849].getExtra(2));
			player.removeSkill(skill);
			//Admin information
			activeChar.sendMessage(MessageTable.Messages[1850].getExtra(1) + skillname + MessageTable.Messages[1850].getExtra(2) + player.getName() + MessageTable.Messages[1850].getExtra(3));
			if (Config.DEBUG)
				_log.fine("[GM]" + activeChar.getName() + " removed skill " + skillname + " from " + player.getName() + ".");
			activeChar.sendSkillList();
		}
		else
			activeChar.sendMessage(1848);
		removeSkillsPage(activeChar, 0); //Back to previous page
	}
	
	/**
	 * @param activeChar the active Game Master.
	 * @param id
	 * @param level
	 */
	private void adminAddClanSkill(L2PcInstance activeChar, int id, int level)
	{
		final L2Object target = activeChar.getTarget();
		if ((target == null) || !target.isPlayer())
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			showMainPage(activeChar);
			return;
		}
		final L2PcInstance player = target.getActingPlayer();
		if (!player.isClanLeader())
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER);
			sm.addString(player.getName());
			activeChar.sendPacket(sm);
			showMainPage(activeChar);
			return;
		}
		if ((id < 370) || (id > 391) || (level < 1) || (level > 3))
		{
			activeChar.sendMessage("Usage: //add_clan_skill <skill_id> <level>");
			showMainPage(activeChar);
			return;
		}
		
		final L2Skill skill = SkillTable.getInstance().getInfo(id, level);
		if (skill == null)
		{
			activeChar.sendMessage(1848);
			return;
		}
		
		String skillname = skill.getName();
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_SKILL_S1_ADDED);
		sm.addSkillName(skill);
		player.sendPacket(sm);
		final L2Clan clan = player.getClan();
		clan.broadcastToOnlineMembers(sm);
		clan.addNewSkill(skill);
		activeChar.sendMessage(MessageTable.Messages[1851].getExtra(1) + skillname + MessageTable.Messages[1851].getExtra(2) + player.getClan().getName() + MessageTable.Messages[1851].getExtra(3));
		
		clan.broadcastToOnlineMembers(new PledgeSkillList(clan));
		for (L2PcInstance member : clan.getOnlineMembers(0))
		{
			member.sendSkillList();
		}
		
		showMainPage(activeChar);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
