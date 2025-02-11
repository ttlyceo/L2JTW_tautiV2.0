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
package com.l2jserver.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.l2jserver.gameserver.datatables.SkillReplaceTable;
import com.l2jserver.gameserver.datatables.SkillTable;
import com.l2jserver.gameserver.datatables.SkillTreesData;
import com.l2jserver.gameserver.model.L2SkillLearn;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.base.Race;
import com.l2jserver.gameserver.model.holders.ItemHolder;
import com.l2jserver.gameserver.model.holders.SkillHolder;
import com.l2jserver.gameserver.model.skills.L2Skill;

/**
 * 
 * @author mrTJO
 */
public class ExAcquirableSkillListByClass extends L2GameServerPacket
{
	private static final String _S__FE_F9_EXACQUIRABLESKILLLISTBYCLASS = "[S] FE:F9 ExAcquirableSkillListByClass";
	L2PcInstance _activeChar;
	
	
	public ExAcquirableSkillListByClass(L2PcInstance activeChar)
	{
		_activeChar = activeChar;
	}
	
	@Override
	protected void writeImpl()
	{
		final Map<Integer, L2SkillLearn> skills = SkillTreesData.getInstance().getCompleteClassSkillTree(_activeChar.getClassId());
		List<L2SkillLearn> avaibleSkills = new ArrayList<>();
		final Race race = _activeChar.getRace();
		for (L2SkillLearn lskill : skills.values())
		{
			if (!lskill.getRaces().isEmpty() && !lskill.getRaces().contains(race))
			{
				continue;
			}
			L2Skill skill = SkillTable.getInstance().getInfo(lskill.getSkillId(), lskill.getSkillLevel());
			if(skill != null)
			{
				final L2Skill oldSkill = _activeChar.getSkills().get(lskill.getSkillId());
				if (oldSkill != null)
				{
					if (oldSkill.getLevel() == (lskill.getSkillLevel() - 1))
					{
						avaibleSkills.add(lskill);
					}
				}
				else if (lskill.getSkillLevel() == 1)
				{
					avaibleSkills.add(lskill);
				}
			}
		}
		
		writeC(0xFE);
		writeH(0xFA);//449 Test
		writeD(avaibleSkills.size());
		for (L2SkillLearn skill : avaibleSkills)
		{
			writeD(skill.getSkillId());
			writeD(skill.getSkillLevel());
			writeD(skill.getLevelUpSp());
			writeH(skill.getGetLevel());
			writeH(0x00);//488 Test
			writeD(skill.getRequiredItems().size());
			for(ItemHolder item : skill.getRequiredItems())
			{
				writeD(item.getId());
				writeQ(item.getCount());
			}
			List<Integer> rskill = SkillReplaceTable.getInstance().getReplaceSkills(skill.getSkillId());
			writeD(rskill.size());
			for(int id : rskill)
			{
				writeD(id);
				writeD(1);//skill level
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _S__FE_F9_EXACQUIRABLESKILLLISTBYCLASS;
	}
}
