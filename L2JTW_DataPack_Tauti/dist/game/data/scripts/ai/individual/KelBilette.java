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
 * 卡納斯 凱爾 比拉特 / 比拉特的手下
 */
public class KelBilette extends L2AttackableAIScript
{
	private static final int KEL   = 18573;
	private static final int GUARD = 18574;

	boolean _isAlreadyStarted = false;
	boolean _isAlreadySpawned = false;

	public KelBilette(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addAttackId(KEL);
		addKillId(GUARD);
		addKillId(KEL);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		int x = player.getX();
		int y = player.getY();

		if (event.equalsIgnoreCase("time_to_skill"))
		{
			npc.setTarget(player);
			npc.doCast(SkillTable.getInstance().getInfo(4748, 6));
			_isAlreadyStarted = false;
			startQuestTimer("time_to_skill1", 10000, npc, player);
		}
		else if (event.equalsIgnoreCase("time_to_skill1"))
		{
			npc.setTarget(player);
			npc.doCast(SkillTable.getInstance().getInfo(5203, 6));
		}
		else if (event.equalsIgnoreCase("time_to_spawn"))
			addSpawn(GUARD, x + 100, y + 50, npc.getZ(), 0, false, 0, false, npc.getInstanceId());

		return "";
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet, L2Skill skill)
	{
		int npcId = npc.getNpcId();

		if (npcId == KEL)
		{
			if (_isAlreadyStarted == false)
			{
				startQuestTimer("time_to_skill", 30000, npc, player);
				_isAlreadyStarted = true;
			}
			if (_isAlreadyStarted == true)
				return "";
			if (_isAlreadySpawned == false)
			{
				startQuestTimer("time_to_spawn", 10000, npc, player);
				_isAlreadySpawned = true;
			}
			if (_isAlreadySpawned == true)
				return "";
		}

		return "";
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		int npcId = npc.getNpcId();

		if (npcId == GUARD)
			_isAlreadySpawned = true;
		else if (npcId == KEL)
		{
			cancelQuestTimer("time_to_spawn", npc, player);
			cancelQuestTimer("time_to_skill", npc, player);
		}

		return "";
	}

	public static void main(String[] args)
	{
		new KelBilette(-1, "KelBilette", "ai");
	}
}