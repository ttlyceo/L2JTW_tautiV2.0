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

import java.util.List;

import javolution.util.FastList;

/**
 * Sample:<br>
 * 0000: 6d 0c 00 00 00 00 00 00 00 03 00 00 00 f3 03 00 m...............<br>
 * 0010: 00 00 00 00 00 01 00 00 00 f4 03 00 00 00 00 00 ................<br>
 * 0020: 00 01 00 00 00 10 04 00 00 00 00 00 00 01 00 00 ................<br>
 * 0030: 00 2c 04 00 00 00 00 00 00 03 00 00 00 99 04 00 .,..............<br>
 * 0040: 00 00 00 00 00 02 00 00 00 a0 04 00 00 00 00 00 ................<br>
 * 0050: 00 01 00 00 00 c0 04 00 00 01 00 00 00 01 00 00 ................<br>
 * 0060: 00 76 00 00 00 01 00 00 00 01 00 00 00 a3 00 00 .v..............<br>
 * 0070: 00 01 00 00 00 01 00 00 00 c2 00 00 00 01 00 00 ................<br>
 * 0080: 00 01 00 00 00 d6 00 00 00 01 00 00 00 01 00 00 ................<br>
 * 0090: 00 f4 00 00 00<br>
 * <br>
 * format d (ddd)
 * @version $Revision: 1.3.2.1.2.5 $ $Date: 2005/03/27 15:29:39 $
 */
public final class SkillList extends L2GameServerPacket
{
	private static final String _S__5F_SKILLLIST = "[S] 5F SkillList";
	private final List<Skill> _skills;
	
	static class Skill
	{
		public int id;
		public int level;
		public boolean passive;
		public boolean disabled;
		public boolean enchanted;
		
		Skill(int pId, int pLevel, boolean pPassive, boolean pDisabled, boolean pEnchanted)
		{
			id = pId;
			level = pLevel;
			passive = pPassive;
			disabled = pDisabled;
			enchanted = pEnchanted;
		}
	}
	
	public SkillList()
	{
		_skills = new FastList<>();
	}
	
	public void addSkill(int id, int level, boolean passive, boolean disabled, boolean enchanted)
	{
		_skills.add(new Skill(id, level, passive, disabled, enchanted));
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x5F);
		writeD(_skills.size());
		
		for (Skill temp : _skills)
		{
			writeD(temp.passive ? 1 : 0);
			writeD(temp.level);
			writeD(temp.id);
			writeD(-1); //rocknow-God
			writeC(temp.disabled ? 1 : 0);
			writeC(temp.enchanted ? 1 : 0);
		}
		writeD(0x00); //rocknow-God Need Not?
	}
	
	@Override
	public String getType()
	{
		return _S__5F_SKILLLIST;
	}
}
