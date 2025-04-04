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
package com.l2jserver.gameserver.model.skills.l2skills;

import java.util.logging.Level;

import com.l2jserver.Config;
import com.l2jserver.gameserver.datatables.NpcTable;
import com.l2jserver.gameserver.idfactory.IdFactory;
import com.l2jserver.gameserver.instancemanager.CHSiegeManager;
import com.l2jserver.gameserver.instancemanager.CastleManager;
import com.l2jserver.gameserver.instancemanager.FortManager;
import com.l2jserver.gameserver.instancemanager.FortSiegeManager;
import com.l2jserver.gameserver.instancemanager.SiegeManager;
import com.l2jserver.gameserver.instancemanager.TerritoryWarManager;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.instance.L2SiegeFlagInstance;
import com.l2jserver.gameserver.model.entity.Castle;
import com.l2jserver.gameserver.model.entity.Fort;
import com.l2jserver.gameserver.model.entity.clanhall.SiegableHall;
import com.l2jserver.gameserver.model.skills.L2Skill;
import com.l2jserver.gameserver.network.SystemMessageId;

public class L2SkillSiegeFlag extends L2Skill
{
	private final boolean _isAdvanced;
	private final boolean _isOutpost;
	
	public L2SkillSiegeFlag(StatsSet set)
	{
		super(set);
		_isAdvanced = set.getBool("isAdvanced", false);
		_isOutpost = set.getBool("isOutpost", false);
	}
	
	/**
	 * @see com.l2jserver.gameserver.model.skills.L2Skill#useSkill(com.l2jserver.gameserver.model.actor.L2Character, com.l2jserver.gameserver.model.L2Object[])
	 */
	@Override
	public void useSkill(L2Character activeChar, L2Object[] targets)
	{
		if (!(activeChar instanceof L2PcInstance))
			return;
		
		L2PcInstance player = (L2PcInstance) activeChar;
		
		if (player.getClan() == null || player.getClan().getLeaderId() != player.getObjectId())
			return;
		
		if (!checkIfOkToPlaceFlag(player, true, _isOutpost))
			return;
		
		// Territory War
		if (TerritoryWarManager.getInstance().isTWInProgress())
		{
			try
			{
				// Spawn a new flag
				L2SiegeFlagInstance flag = new L2SiegeFlagInstance(player, IdFactory.getInstance().getNextId(), NpcTable.getInstance().getTemplate((_isOutpost ? 36590 : 35062)), _isAdvanced, _isOutpost);
				flag.setTitle(player.getClan().getName());
				flag.setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());
				flag.setHeading(player.getHeading());
				flag.spawnMe(player.getX(), player.getY(), player.getZ() + 50);
				if (_isOutpost)
					TerritoryWarManager.getInstance().setHQForClan(player.getClan(), flag);
				else
					TerritoryWarManager.getInstance().addClanFlag(player.getClan(), flag);
			}
			catch (Exception e)
			{
				player.sendMessage("Error placing flag: " + e);
				_log.log(Level.WARNING, "Error placing flag: " + e.getMessage(), e);
			}
			return;
		}
		// Fortress/Castle siege
		try
		{
			// Spawn a new flag
			L2SiegeFlagInstance flag = new L2SiegeFlagInstance(player, IdFactory.getInstance().getNextId(), NpcTable.getInstance().getTemplate(35062), _isAdvanced, false);
			flag.setTitle(player.getClan().getName());
			flag.setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());
			flag.setHeading(player.getHeading());
			flag.spawnMe(player.getX(), player.getY(), player.getZ() + 50);
			Castle castle = CastleManager.getInstance().getCastle(activeChar);
			Fort fort = FortManager.getInstance().getFort(activeChar);
			SiegableHall hall = CHSiegeManager.getInstance().getNearbyClanHall(activeChar);
			if (castle != null)
				castle.getSiege().getFlag(player.getClan()).add(flag);
			else if(fort != null)
				fort.getSiege().getFlag(player.getClan()).add(flag);
			else 
				hall.getSiege().getFlag(player.getClan()).add(flag);
			
		}
		catch (Exception e)
		{
			player.sendMessage("Error placing flag:" + e);
			_log.log(Level.WARNING, "Error placing flag: " + e.getMessage(), e);
		}
	}
	
	/**
	 * @param activeChar The L2Character of the character placing the flag
	 * @param isCheckOnly if false, it will send a notification to the player telling him why it failed
	 * @param isOutPost 
	 * @return true if character clan place a flag
	 */
	public static boolean checkIfOkToPlaceFlag(L2Character activeChar, boolean isCheckOnly, boolean isOutPost)
	{
		if (TerritoryWarManager.getInstance().isTWInProgress())
			return checkIfOkToPlaceHQ(activeChar, isCheckOnly, isOutPost);
		else if (isOutPost)
			return false;
		Castle castle = CastleManager.getInstance().getCastle(activeChar);
		Fort fort = FortManager.getInstance().getFort(activeChar);
		SiegableHall hall = CHSiegeManager.getInstance().getNearbyClanHall(activeChar);
		
		if ((castle == null) && (fort == null) && (hall == null))
			return false;
		if (castle != null)
			return checkIfOkToPlaceFlag(activeChar, castle, isCheckOnly);
		else if(fort != null)
			return checkIfOkToPlaceFlag(activeChar, fort, isCheckOnly);
		return checkIfOkToPlaceFlag(activeChar, hall, isCheckOnly);
	}
	
	/**
	 * 
	 * @param activeChar
	 * @param castle
	 * @param isCheckOnly
	 * @return
	 * TODO: Replace strings with system messages!
	 */
	public static boolean checkIfOkToPlaceFlag(L2Character activeChar, Castle castle, boolean isCheckOnly)
	{
		if (!(activeChar instanceof L2PcInstance))
			return false;
		
		/* Move To MessageTable For L2JTW
		String text = "";
		*/
		int text = 0;
		L2PcInstance player = (L2PcInstance) activeChar;
		
		if (castle == null || castle.getCastleId() <= 0)
			/* Move To MessageTable For L2JTW
			text = "You must be on castle ground to place a flag.";
			*/
			text = 135;
		else if (!castle.getSiege().getIsInProgress())
			/* Move To MessageTable For L2JTW
			text = "You can only place a flag during a siege.";
			*/
			text = 136;
		else if (castle.getSiege().getAttackerClan(player.getClan()) == null)
			/* Move To MessageTable For L2JTW
			text = "You must be an attacker to place a flag.";
			*/
			text = 137;
		else if (!player.isClanLeader())
			/* Move To MessageTable For L2JTW
			text = "You must be a clan leader to place a flag.";
			*/
			text = 138;
		else if (castle.getSiege().getAttackerClan(player.getClan()).getNumFlags() >= SiegeManager.getInstance().getFlagMaxCount())
			/* Move To MessageTable For L2JTW
			text = "You have already placed the maximum number of flags possible.";
			*/
			text = 139;
		else if (!player.isInsideZone(L2Character.ZONE_HQ))
			player.sendPacket(SystemMessageId.NOT_SET_UP_BASE_HERE);
		else
			return true;
		
		if (!isCheckOnly)
			player.sendMessage(text);
		return false;
	}
	
	/**
	 * 
	 * @param activeChar
	 * @param fort
	 * @param isCheckOnly
	 * @return
	 * TODO: Replace strings with system messages!
	 */
	public static boolean checkIfOkToPlaceFlag(L2Character activeChar, Fort fort, boolean isCheckOnly)
	{
		if (!(activeChar instanceof L2PcInstance))
			return false;
		
		/* Move To MessageTable For L2JTW
		String text = "";
		*/
		int text = 0;
		L2PcInstance player = (L2PcInstance) activeChar;
		
		if (fort == null || fort.getFortId() <= 0)
			/* Move To MessageTable For L2JTW
			text = "You must be on fort ground to place a flag.";
			*/
			text = 141;
		else if (!fort.getSiege().getIsInProgress())
			/* Move To MessageTable For L2JTW
			text = "You can only place a flag during a siege.";
			*/
			text = 142;
		else if (fort.getSiege().getAttackerClan(player.getClan()) == null)
			/* Move To MessageTable For L2JTW
			text = "You must be an attacker to place a flag.";
			*/
			text = 137;
		else if (!player.isClanLeader())
			/* Move To MessageTable For L2JTW
			text = "You must be a clan leader to place a flag.";
			*/
			text = 138;
		else if (fort.getSiege().getAttackerClan(player.getClan()).getNumFlags() >= FortSiegeManager.getInstance().getFlagMaxCount())
			/* Move To MessageTable For L2JTW
			text = "You have already placed the maximum number of flags possible.";
			*/
			text = 139;
		else if (!player.isInsideZone(L2Character.ZONE_HQ))
			player.sendPacket(SystemMessageId.NOT_SET_UP_BASE_HERE);
		else
			return true;
		
		if (!isCheckOnly)
			player.sendMessage(text);
		return false;
	}
	
	/**
	 * 
	 * @param activeChar
	 * @param hall
	 * @param isCheckOnly
	 * @return
	 * TODO: Replace strings with system messages!
	 */
	public static boolean checkIfOkToPlaceFlag(L2Character activeChar, SiegableHall hall, boolean isCheckOnly)
	{
		if (!(activeChar instanceof L2PcInstance))
			return false;
		
		/* Move To MessageTable For L2JTW
		String text = "";
		*/
		int text = 0;
		L2PcInstance player = (L2PcInstance) activeChar;
		final int hallId = hall.getId();
		
		if (hallId <= 0)
			/* Move To MessageTable For L2JTW
			text = "You must be on Siegable clan hall ground to place a flag.";
			*/
			text = 146;
		else if (!hall.isInSiege())
			/* Move To MessageTable For L2JTW
			text = "You can only place a flag during a siege.";
			*/
			text = 142;
		else if (player.getClan() == null || !player.isClanLeader())
			/* Move To MessageTable For L2JTW
			text = "You must be a clan leader to place a flag.";
			*/
			text = 138;
		else if (!hall.isRegistered(player.getClan()))
			/* Move To MessageTable For L2JTW
			text = "You must be an attacker to place a flag.";
			*/
			text = 137;
		else if (hall.getSiege().getAttackerClan(player.getClan()).getNumFlags() > Config.CHS_MAX_FLAGS_PER_CLAN)
			/* Move To MessageTable For L2JTW
			text = "You have already placed the maximum number of flags possible.";
			*/
			text = 139;
		else if (!player.isInsideZone(L2Character.ZONE_HQ))
			player.sendPacket(SystemMessageId.NOT_SET_UP_BASE_HERE);
		else if(!hall.getSiege().canPlantFlag())
			/* Move To MessageTable For L2JTW
			text = "You cannot place a flag on this siege.";
			*/
			text = 147;
		else
			return true;
		
		if (!isCheckOnly)
			player.sendMessage(text);
		return false;
	}
	
	/**
	 * TODO: Replace strings with system messages!
	 * @param activeChar The L2Character of the character placing the flag
	 * @param isCheckOnly if false, it will send a notification to the player telling him why it failed
	 * @param isOutPost 
	 * @return true if character clan place a flag
	 */
	public static boolean checkIfOkToPlaceHQ(L2Character activeChar, boolean isCheckOnly, boolean isOutPost)
	{
		Castle castle = CastleManager.getInstance().getCastle(activeChar);
		Fort fort = FortManager.getInstance().getFort(activeChar);
		
		if ((castle == null) && (fort == null))
			return false;
		
		/* Move To MessageTable For L2JTW
		String text = "";
		*/
		int text = 0;
		L2PcInstance player = (L2PcInstance) activeChar;
		
		if ((fort != null && fort.getFortId() == 0) || (castle != null && castle.getCastleId() == 0))
			/* Move To MessageTable For L2JTW
			text = "You must be on fort or castle ground to construct an outpost or flag.";
			*/
			text = 143;
		else if ((fort != null && !fort.getZone().isActive()) || (castle != null && !castle.getZone().isActive()))
			/* Move To MessageTable For L2JTW
			text = "You can only construct an outpost or flag on siege field.";
			*/
			text = 144;
		else if (!player.isClanLeader())
			/* Move To MessageTable For L2JTW
			text = "You must be a clan leader to construct an outpost or flag.";
			*/
			text = 145;
		else if (TerritoryWarManager.getInstance().getHQForClan(player.getClan()) != null && isOutPost)
			player.sendPacket(SystemMessageId.NOT_ANOTHER_HEADQUARTERS);
		else if (TerritoryWarManager.getInstance().getFlagForClan(player.getClan()) != null && !isOutPost)
			player.sendPacket(SystemMessageId.A_FLAG_IS_ALREADY_BEING_DISPLAYED_ANOTHER_FLAG_CANNOT_BE_DISPLAYED);
		else if (!player.isInsideZone(L2Character.ZONE_HQ))
			player.sendPacket(SystemMessageId.NOT_SET_UP_BASE_HERE);
		else
			return true;
		
		if (!isCheckOnly)
			player.sendMessage(text);
		return false;
	}
}
