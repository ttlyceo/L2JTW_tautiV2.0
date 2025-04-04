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
package quests.Q692_HowtoOpposeEvil;

import gnu.trove.map.hash.TIntObjectHashMap;

import com.l2jserver.Config;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.model.quest.State;

/**
 * How to Oppose Evil(692)
 * @author Gigiikun
 */
public final class Q692_HowtoOpposeEvil extends Quest
{
	private static final String qn = "692_HowtoOpposeEvil";
	private static final int DILIOS = 32549;
	private static final int LEKONS_CERTIFICATE = 13857;
	private static final int[] QUEST_ITEMS =
	{
		13863,
		13864,
		13865,
		13866,
		13867,
		15535,
		15536
	};
	
	private static final TIntObjectHashMap<int[]> _questMobs = new TIntObjectHashMap<>();
	
	//@formatter:off
	static
	{
		// Seed of Infinity
		_questMobs.put(22509, new int[] { 13863, 500 });
		_questMobs.put(22510, new int[] { 13863, 500 });
		_questMobs.put(22511, new int[] { 13863, 500 });
		_questMobs.put(22512, new int[] { 13863, 500 });
		_questMobs.put(22513, new int[] { 13863, 500 });
		_questMobs.put(22514, new int[] { 13863, 500 });
		_questMobs.put(22515, new int[] { 13863, 500 });
		// Seed of Destruction
		_questMobs.put(22537, new int[] { 13865, 250 });
		_questMobs.put(22538, new int[] { 13865, 250 });
		_questMobs.put(22539, new int[] { 13865, 250 });
		_questMobs.put(22540, new int[] { 13865, 250 });
		_questMobs.put(22541, new int[] { 13865, 250 });
		_questMobs.put(22542, new int[] { 13865, 250 });
		_questMobs.put(22543, new int[] { 13865, 250 });
		_questMobs.put(22544, new int[] { 13865, 250 });
		_questMobs.put(22546, new int[] { 13865, 250 });
		_questMobs.put(22547, new int[] { 13865, 250 });
		_questMobs.put(22548, new int[] { 13865, 250 });
		_questMobs.put(22549, new int[] { 13865, 250 });
		_questMobs.put(22550, new int[] { 13865, 250 });
		_questMobs.put(22551, new int[] { 13865, 250 });
		_questMobs.put(22552, new int[] { 13865, 250 });
		_questMobs.put(22593, new int[] { 13865, 250 });
		_questMobs.put(22596, new int[] { 13865, 250 });
		_questMobs.put(22597, new int[] { 13865, 250 });
		// Seed of Annihilation
		_questMobs.put(22746, new int[] { 15536, 125 });
		_questMobs.put(22747, new int[] { 15536, 125 });
		_questMobs.put(22748, new int[] { 15536, 125 });
		_questMobs.put(22749, new int[] { 15536, 125 });
		_questMobs.put(22750, new int[] { 15536, 125 });
		_questMobs.put(22751, new int[] { 15536, 125 });
		_questMobs.put(22752, new int[] { 15536, 125 });
		_questMobs.put(22753, new int[] { 15536, 125 });
		_questMobs.put(22754, new int[] { 15536, 125 });
		_questMobs.put(22755, new int[] { 15536, 125 });
		_questMobs.put(22756, new int[] { 15536, 125 });
		_questMobs.put(22757, new int[] { 15536, 125 });
		_questMobs.put(22758, new int[] { 15536, 125 });
		_questMobs.put(22759, new int[] { 15536, 125 });
		_questMobs.put(22760, new int[] { 15536, 125 });
		_questMobs.put(22761, new int[] { 15536, 125 });
		_questMobs.put(22762, new int[] { 15536, 125 });
		_questMobs.put(22763, new int[] { 15536, 125 });
		_questMobs.put(22764, new int[] { 15536, 125 });
		_questMobs.put(22765, new int[] { 15536, 125 });
	}
	//@formatter:on
	
	private static final boolean giveReward(QuestState st, int itemId, int minCount, int rewardItemId, long rewardCount)
	{
		long count = st.getQuestItemsCount(itemId);
		if (count < minCount)
		{
			return false;
		}
		
		count = count / minCount;
		st.takeItems(itemId, count * minCount);
		st.rewardItems(rewardItemId, rewardCount * count);
		return true;
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return "<html><body>目前沒有執行任務，或條件不符。</body></html>";
		}
		if (event.equalsIgnoreCase("32549-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("32550-04.htm"))
		{
			st.set("cond", "3");
		}
		else if (event.equalsIgnoreCase("32550-07.htm"))
		{
			if (!giveReward(st, 13863, 5, 13796, 1))
			{
				return "32550-08.htm";
			}
		}
		else if (event.equalsIgnoreCase("32550-09.htm"))
		{
			if (!giveReward(st, 13798, 1, 57, 5000))
			{
				return "32550-10.htm";
			}
		}
		else if (event.equalsIgnoreCase("32550-12.htm"))
		{
			if (!giveReward(st, 13865, 5, 13841, 1))
			{
				return "32550-13.htm";
			}
		}
		else if (event.equalsIgnoreCase("32550-14.htm"))
		{
			if (!giveReward(st, 13867, 1, 57, 5000))
			{
				return "32550-15.htm";
			}
		}
		else if (event.equalsIgnoreCase("32550-17.htm"))
		{
			if (!giveReward(st, 15536, 5, 15486, 1))
			{
				return "32550-18.htm";
			}
		}
		else if (event.equalsIgnoreCase("32550-19.htm"))
		{
			if (!giveReward(st, 15535, 1, 57, 5000))
			{
				return "32550-20.htm";
			}
		}
		return event;
	}
	
	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "<html><body>目前沒有執行任務，或條件不符。</body></html>";
		final QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		if (st.isCreated())
		{
			htmltext = (player.getLevel() >= 75) ? "32549-01.htm" : "32549-00.htm";
		}
		else
		{
			final int cond = st.getInt("cond");
			if (npc.getNpcId() == DILIOS)
			{
				if ((cond == 1) && st.hasQuestItems(LEKONS_CERTIFICATE))
				{
					htmltext = "32549-04.htm";
					st.takeItems(LEKONS_CERTIFICATE, -1);
					st.set("cond", "2");
				}
				else if (cond == 2)
				{
					htmltext = "32549-05.htm";
				}
			}
			else
			{
				if (cond == 2)
				{
					htmltext = "32550-01.htm";
				}
				else if (cond == 3)
				{
					for (int i : QUEST_ITEMS)
					{
						if (st.getQuestItemsCount(i) > 0)
						{
							return "32550-05.htm";
						}
					}
					htmltext = "32550-04.htm";
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		final L2PcInstance partyMember = getRandomPartyMember(player, "3");
		if (partyMember == null)
		{
			return null;
		}
		final QuestState st = partyMember.getQuestState(qn);
		final int npcId = npc.getNpcId();
		if ((st != null) && _questMobs.containsKey(npcId))
		{
			int chance = (int) (_questMobs.get(npcId)[1] * Config.RATE_QUEST_DROP);
			int numItems = chance / 1000;
			chance = chance % 1000;
			if (getRandom(1000) < chance)
			{
				numItems++;
			}
			if (numItems > 0)
			{
				st.giveItems(_questMobs.get(npcId)[0], numItems);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
	
	public Q692_HowtoOpposeEvil(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(DILIOS);
		addTalkId(DILIOS);
		addTalkId(32550);
		addKillId(_questMobs.keys());
	}
	
	public static void main(String[] args)
	{
		new Q692_HowtoOpposeEvil(692, qn, "對抗惡的方法");
	}
}