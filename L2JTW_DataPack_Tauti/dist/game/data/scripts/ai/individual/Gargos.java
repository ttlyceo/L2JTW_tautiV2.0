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
package ai.individual;

import ai.group_template.L2AttackableAIScript;

import com.l2jserver.gameserver.datatables.SkillTable;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.skills.L2Skill;

/**
 * 火的精靈 加爾高斯
 */
public class Gargos extends L2AttackableAIScript
{
	private static final int GARGOS = 18607;

	boolean _isStarted = false;

	public Gargos(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addAttackId(GARGOS);
		addKillId(GARGOS);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("TimeToFire"))
		{
			_isStarted = false;
			player.sendMessage("Oooo... Ooo...");
			npc.doCast(SkillTable.getInstance().getInfo(5705, 1));
		}
		return "";
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet, L2Skill skill)
	{
		int npcId = npc.getNpcId();

		if (npcId == GARGOS)
		{
			if (_isStarted == false)
			{
				startQuestTimer("TimeToFire", 60000, npc, player);
				_isStarted = true;
			}
		}
		return "";
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		int npcId = npc.getNpcId();

		if (npcId == GARGOS)
			cancelQuestTimer("TimeToFire", npc, player);

		return "";
	}

	public static void main(String[] args)
	{
		new Gargos(-1, "Gargos", "ai");
	}
}