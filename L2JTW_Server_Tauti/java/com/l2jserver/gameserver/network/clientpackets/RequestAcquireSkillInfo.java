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

import com.l2jserver.Config;//rocknow-God-Test Only
import com.l2jserver.gameserver.datatables.SkillTable;
import com.l2jserver.gameserver.datatables.SkillTreesData;
import com.l2jserver.gameserver.model.L2SkillLearn;
//l2jtw add import com.l2jserver.gameserver.model.L2SquadTrainer;
//l2jtw add import com.l2jserver.gameserver.model.actor.L2Npc;
//l2jtw add import com.l2jserver.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.base.AcquireSkillType;
import com.l2jserver.gameserver.model.skills.L2Skill;
import com.l2jserver.gameserver.network.serverpackets.AcquireSkillInfo;
import com.l2jserver.gameserver.network.serverpackets.ExAcquireSkillInfo;
import com.l2jserver.gameserver.datatables.MessageTable;//rocknow-God-Test Only

/**
 * @author Zoey76
 */
public final class RequestAcquireSkillInfo extends L2GameClientPacket
{
	private static final String _C__73_REQUESTACQUIRESKILLINFO = "[C] 73 RequestAcquireSkillInfo";
	
	private int _id;
	private int _level;
	private int _skillType;
	
	@Override
	protected void readImpl()
	{
		_id = readD();
		_level = readD();
		_skillType = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if ((_id <= 0) || (_level <= 0))
		{
			_log.warning(RequestAcquireSkillInfo.class.getSimpleName() + ": Invalid Id: " + _id + " or level: " + _level + "!");
			return;
		}
		
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		/* l2jtw start
		final L2Npc trainer = activeChar.getLastFolkNPC();
		if (!(trainer instanceof L2NpcInstance))
		{
			return;
		}
		
		if (!trainer.canInteract(activeChar) && !activeChar.isGM())
		{
			return;
		}
		 * l2jtw end
		 */
		
		final L2Skill skill = SkillTable.getInstance().getInfo(_id, _level);
		if (skill == null)
		{
			if (Config.DEBUG) //l2jtw add
				_log.warning(RequestAcquireSkillInfo.class.getSimpleName() + MessageTable.Messages[2001].getExtra(1) + _id + MessageTable.Messages[2001].getExtra(2) + _level + MessageTable.Messages[2001].getExtra(3) + RequestAcquireSkillInfo.class.getName() + MessageTable.Messages[2001].getExtra(4));
			return;
		}
		
		// Hack check. Doesn't apply to all Skill Types
		final int prevSkillLevel = activeChar.getSkillLevel(_id);
		final AcquireSkillType skillType = AcquireSkillType.values()[_skillType];
		if ((prevSkillLevel > 0) && !((skillType == AcquireSkillType.Transfer) || (skillType == AcquireSkillType.SubPledge)))
		{
			if (prevSkillLevel == _level)
			{
				_log.warning(RequestAcquireSkillInfo.class.getSimpleName() + ": Player " + activeChar.getName() + " is trequesting info for a skill that already knows, Id: " + _id + " level: " + _level + "!");
			}
			else if (prevSkillLevel != (_level - 1))
			{
				_log.warning(RequestAcquireSkillInfo.class.getSimpleName() + ": Player " + activeChar.getName() + " is requesting info for skill Id: " + _id + " level " + _level + " without knowing it's previous level!");
			}
		}
		
		final L2SkillLearn s = SkillTreesData.getInstance().getSkillLearn(skillType, _id, _level, activeChar);
		if (s == null)
		{
			return;
		}
		
		switch (skillType)
		{
			case Class:
			{
				/* l2jtw start
				if (trainer.getTemplate().canTeach(activeChar.getLearningClass()))
				{
					final int customSp = s.getCalculatedLevelUpSp(activeChar.getClassId(), activeChar.getLearningClass());
					sendPacket(new AcquireSkillInfo(skillType, s, customSp));
				}
				 */
				final int customSp = s.getCalculatedLevelUpSp(activeChar.getClassId(), activeChar.getLearningClass());
				sendPacket(new ExAcquireSkillInfo(skillType, s, customSp));
				//l2jtw add end
				break;
			}
			case Transform:
			{
				sendPacket(new AcquireSkillInfo(skillType, s));
				break;
			}
			case Fishing:
			{
				sendPacket(new AcquireSkillInfo(skillType, s));
				break;
			}
			case Pledge:
			{
				if (!activeChar.isClanLeader())
				{
					return;
				}
				sendPacket(new AcquireSkillInfo(skillType, s));
				break;
			}
			case SubPledge:
			{
				if (!activeChar.isClanLeader())
				{
					return;
				}
				
				/* l2jtw add start
				if (!(trainer instanceof L2SquadTrainer))
				{
					return;
				}
				 * l2jtw add end
				 */
				sendPacket(new AcquireSkillInfo(skillType, s));
				break;
			}
			case SubClass:
			{
				sendPacket(new AcquireSkillInfo(skillType, s));
				break;
			}
			case Collect:
			{
				sendPacket(new AcquireSkillInfo(skillType, s));
				break;
			}
			case Transfer:
			{
				sendPacket(new AcquireSkillInfo(skillType, s));
				break;
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__73_REQUESTACQUIRESKILLINFO;
	}
}
