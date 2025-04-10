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
package ai.group_template;

import gnu.trove.map.hash.TIntObjectHashMap;

import com.l2jserver.gameserver.ai.CtrlIntention;
import com.l2jserver.gameserver.model.actor.L2Attackable;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.NpcStringId;
import com.l2jserver.gameserver.network.clientpackets.Say2;
import com.l2jserver.gameserver.network.serverpackets.CreatureSay;

/**
 * @author Slyce
 */
public class PolymorphingOnAttack extends L2AttackableAIScript
{
	private static final TIntObjectHashMap<Integer[]> MOBSPAWNS = new TIntObjectHashMap<>();
	
	static
	{
		MOBSPAWNS.put(21258, new Integer[]
		{
			21259, 100, 100, -1
		}); // Fallen Orc Shaman -> Sharp Talon Tiger (always polymorphs)
		MOBSPAWNS.put(21261, new Integer[]
		{
			21262, 100, 20, 0
		}); // Ol Mahum Transcender 1st stage
		MOBSPAWNS.put(21262, new Integer[]
		{
			21263, 100, 10, 1
		}); // Ol Mahum Transcender 2nd stage
		MOBSPAWNS.put(21263, new Integer[]
		{
			21264, 100, 5, 2
		}); // Ol Mahum Transcender 3rd stage
		MOBSPAWNS.put(21265, new Integer[]
		{
			21271, 100, 33, 0
		}); // Cave Ant Larva -> Cave Ant
		MOBSPAWNS.put(21266, new Integer[]
		{
			21269, 100, 100, -1
		}); // Cave Ant Larva -> Cave Ant (always polymorphs)
		MOBSPAWNS.put(21267, new Integer[]
		{
			21270, 100, 100, -1
		}); // Cave Ant Larva -> Cave Ant Soldier (always polymorphs)
		MOBSPAWNS.put(21271, new Integer[]
		{
			21272, 66, 10, 1
		}); // Cave Ant -> Cave Ant Soldier
		MOBSPAWNS.put(21272, new Integer[]
		{
			21273, 33, 5, 2
		}); // Cave Ant Soldier -> Cave Noble Ant
		MOBSPAWNS.put(21521, new Integer[]
		{
			21522, 100, 30, -1
		}); // Claws of Splendor
		MOBSPAWNS.put(21527, new Integer[]
		{
			21528, 100, 30, -1
		}); // Anger of Splendor
		MOBSPAWNS.put(21533, new Integer[]
		{
			21534, 100, 30, -1
		}); // Alliance of Splendor
		MOBSPAWNS.put(21537, new Integer[]
		{
			21538, 100, 30, -1
		}); // Fang of Splendor
	}
	protected static final NpcStringId[][] MOBTEXTS =
	{
		new NpcStringId[]
		{
			NpcStringId.ENOUGH_FOOLING_AROUND_GET_READY_TO_DIE, 
			NpcStringId.YOU_IDIOT_IVE_JUST_BEEN_TOYING_WITH_YOU,
			NpcStringId.NOW_THE_FUN_STARTS
		}, 
		new NpcStringId[]
		{
			NpcStringId.I_MUST_ADMIT_NO_ONE_MAKES_MY_BLOOD_BOIL_QUITE_LIKE_YOU_DO,
			NpcStringId.NOW_THE_BATTLE_BEGINS, 
			NpcStringId.WITNESS_MY_TRUE_POWER
		}, 
		new NpcStringId[]
		{
			NpcStringId.PREPARE_TO_DIE, 
			NpcStringId.ILL_DOUBLE_MY_STRENGTH,
			NpcStringId.YOU_HAVE_MORE_SKILL_THAN_I_THOUGHT
		}
	};
	
	public PolymorphingOnAttack(int questId, String name, String descr)
	{
		super(questId, name, descr);
		registerMobs(MOBSPAWNS.keys(), QuestEventType.ON_ATTACK);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (npc.isVisible() && !npc.isDead())
		{
			final Integer[] tmp = MOBSPAWNS.get(npc.getNpcId());
			if (tmp != null)
			{
				if (npc.getCurrentHp() <= (npc.getMaxHp() * tmp[1] / 100.0) && getRandom(100) < tmp[2])
				{
					if (tmp[3] >= 0)
					{
						NpcStringId npcString = MOBTEXTS[tmp[3]][getRandom(MOBTEXTS[tmp[3]].length)];
						npc.broadcastPacket(new CreatureSay(npc.getObjectId(), Say2.ALL, npc.getName(), npcString));
						
					}
					npc.deleteMe();
					L2Attackable newNpc = (L2Attackable) addSpawn(tmp[0], npc.getX(), npc.getY(), npc.getZ() + 10, npc.getHeading(), false, 0, true);
					L2Character originalAttacker = isPet ? attacker.getPet() : attacker;
					newNpc.setRunning();
					newNpc.addDamageHate(originalAttacker, 0, 500);
					newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, originalAttacker);
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	public static void main(String[] args)
	{
		new PolymorphingOnAttack(-1, "polymorphing_on_attack", "ai");
	}
}
