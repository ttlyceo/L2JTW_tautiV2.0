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
package com.l2jserver.gameserver.model.skills.funcs.formulas;

import com.l2jserver.gameserver.model.actor.templates.L2PcTemplate;
import com.l2jserver.gameserver.model.skills.funcs.Func;
import com.l2jserver.gameserver.model.stats.Env;
import com.l2jserver.gameserver.model.stats.Stats;

/**
 * @author UnAfraid
 *
 */
public class FuncMaxMpAdd extends Func
{
	private static final FuncMaxMpAdd _fmma_instance = new FuncMaxMpAdd();
	
	public static Func getInstance()
	{
		return _fmma_instance;
	}
	
	private FuncMaxMpAdd()
	{
		super(Stats.MAX_MP, 0x10, null);
	}
	
	@Override
	public void calc(Env env)
	{
		L2PcTemplate t = (L2PcTemplate) env.getCharacter().getTemplate();
		int lvl = env.getCharacter().getLevel() - t.getClassBaseLevel();
		//rocknow-Test-Start
		if (env.getCharacter().getLevel() < t.getClassBaseLevel())
		{
			double _lv = t.getClassId().level() + 1;
			if (_lv == 2) _lv = 3;
			else if (_lv == 5) _lv = 2;
			double mpfix = (t.getLvlMpAdd() / _lv) * lvl;
			env.addValue(mpfix);
			
		}
		else
		{
			double mpmod = t.getLvlMpMod() * lvl;
			double mpmax = (t.getLvlMpAdd() + mpmod) * lvl;
			double mpmin = (t.getLvlMpAdd() * lvl) + mpmod;
			env.addValue((mpmax + mpmin) / 2);
		}
		//rocknow-Test-End
	}
}