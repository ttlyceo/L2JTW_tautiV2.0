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

import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.datatables.ClassListData;
import com.l2jserver.gameserver.datatables.SkillTable;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.base.ClassLevel;
import com.l2jserver.gameserver.model.base.SubClass;
import com.l2jserver.gameserver.model.skills.L2Skill;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;

public class L2SkillClassChange extends L2Skill
{
	private int _classIndex;
	public L2SkillClassChange(StatsSet set)
	{
		super(set);
		 _classIndex = set.getInteger("classIndex", 0);
	}
	@Override
	public void useSkill(L2Character activeChar, L2Object[] targets)
	{
		if(!activeChar.isPlayer())
			return;
		//l2jtw temp fix start
		if(!((L2PcInstance)activeChar).isSubClassActive() && ((L2PcInstance)activeChar).getAwakenSubClassCount() > 1)
		{
			((L2PcInstance)activeChar).sendMessage(763);
			return;
		}
		//l2jtw temp fix end
		ThreadPoolManager.getInstance().scheduleGeneral(new classChange((L2PcInstance)activeChar, _classIndex), 100);
	}
	
	class classChange implements Runnable
	{
		L2PcInstance player;
		int classIndex;
		public classChange(L2PcInstance player, int classIndex)
		{
			this.player = player;
			this.classIndex = classIndex;
		}
		@Override
		public void run()
		{
			int oldClassId = player.getClassIndex() == 0 ? player.getBaseClass() : player.getSubClasses().get(player.getClassIndex()).getClassId();
			int newClassId = classIndex == 0 ? player.getBaseClass() : player.getSubClasses().get(classIndex).getClassId();
			if(player.setActiveClass(classIndex))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SUBCLASS_TRANSFER_COMPLETED)
					.addSystemString(ClassListData.getInstance().getClass(oldClassId).getClassClientId())
					.addSystemString(ClassListData.getInstance().getClass(newClassId).getClassClientId())
				); // Transfer completed.
				L2Skill skill = SkillTable.getInstance().getInfo(1570, 1);
				if(skill != null)
				{
					skill.getEffectsSelf(player);
				}
			}
		}
	}
}
