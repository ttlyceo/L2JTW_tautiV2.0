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
package quests.Q702_ATrapForRevenge;

import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.model.quest.State;

/**
 * A Trap for Revenge (702)
 * @author malyelfik
 */
public class Q702_ATrapForRevenge extends Quest
{
	private static final String qn = "702_ATrapForRevenge";
	// NPC
	private static final int Plenos = 32563;
	private static final int Lekon = 32557;
	private static final int Tenius = 32555;
	private static final int[] Monsters =
	{
		22612,
		22613,
		25632,
		22610,
		22611,
		25631,
		25626
	};
	// Items
	private static final int DrakeFlesh = 13877;
	private static final int RottenBlood = 13878;
	private static final int BaitForDrakes = 13879;
	private static final int VariantDrakeWingHorns = 13880;
	private static final int ExtractedRedStarStone = 14009;
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return "<html><body>目前沒有執行任務，或條件不符。</body></html>";
		}
		
		if (event.equalsIgnoreCase("32563-04.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("32563-07.html"))
		{
			htmltext = st.hasQuestItems(DrakeFlesh) ? "32563-08.html" : "32563-07.html";
		}
		else if (event.equalsIgnoreCase("32563-09.html"))
		{
			st.giveAdena(st.getQuestItemsCount(DrakeFlesh) * 100, false);
			st.takeItems(DrakeFlesh, -1);
		}
		else if (event.equalsIgnoreCase("32563-11.html"))
		{
			if (st.hasQuestItems(VariantDrakeWingHorns))
			{
				st.giveAdena(st.getQuestItemsCount(VariantDrakeWingHorns) * 200000, false);
				st.takeItems(VariantDrakeWingHorns, -1);
				htmltext = "32563-12.html";
			}
			else
			{
				htmltext = "32563-11.html";
			}
		}
		else if (event.equalsIgnoreCase("32563-14.html"))
		{
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
		}
		else if (event.equalsIgnoreCase("32557-03.html"))
		{
			if (!st.hasQuestItems(RottenBlood) && (st.getQuestItemsCount(ExtractedRedStarStone) < 100))
			{
				htmltext = "32557-03.html";
			}
			else if (st.hasQuestItems(RottenBlood) && (st.getQuestItemsCount(ExtractedRedStarStone) < 100))
			{
				htmltext = "32557-04.html";
			}
			else if (!st.hasQuestItems(RottenBlood) && (st.getQuestItemsCount(ExtractedRedStarStone) >= 100))
			{
				htmltext = "32557-05.html";
			}
			else if (st.hasQuestItems(RottenBlood) && (st.getQuestItemsCount(ExtractedRedStarStone) >= 100))
			{
				st.giveItems(BaitForDrakes, 1);
				st.takeItems(RottenBlood, 1);
				st.takeItems(ExtractedRedStarStone, 100);
				htmltext = "32557-06.html";
			}
		}
		else if (event.equalsIgnoreCase("32555-03.html"))
		{
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("32555-05.html"))
		{
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
		}
		else if (event.equalsIgnoreCase("32555-06.html"))
		{
			if (st.getQuestItemsCount(DrakeFlesh) < 100)
			{
				htmltext = "32555-06.html";
			}
			else
			{
				htmltext = "32555-07.html";
			}
		}
		else if (event.equalsIgnoreCase("32555-08.html"))
		{
			st.giveItems(RottenBlood, 1);
			st.takeItems(DrakeFlesh, 100);
		}
		else if (event.equalsIgnoreCase("32555-10.html"))
		{
			if (st.hasQuestItems(VariantDrakeWingHorns))
			{
				htmltext = "32555-11.html";
			}
			else
			{
				htmltext = "32555-10.html";
			}
		}
		else if (event.equalsIgnoreCase("32555-15.html"))
		{
			int i0 = getRandom(1000);
			int i1 = getRandom(1000);
			
			if ((i0 >= 500) && (i1 >= 600))
			{
				st.giveAdena(getRandom(49917) + 125000, false);
				if (i1 < 720)
				{
					st.giveItems(9628, getRandom(3) + 1);
					st.giveItems(9629, getRandom(3) + 1);
				}
				else if (i1 < 840)
				{
					st.giveItems(9629, getRandom(3) + 1);
					st.giveItems(9630, getRandom(3) + 1);
				}
				else if (i1 < 960)
				{
					st.giveItems(9628, getRandom(3) + 1);
					st.giveItems(9630, getRandom(3) + 1);
				}
				else if (i1 < 1000)
				{
					st.giveItems(9628, getRandom(3) + 1);
					st.giveItems(9629, getRandom(3) + 1);
					st.giveItems(9630, getRandom(3) + 1);
				}
				htmltext = "32555-15.html";
			}
			else if ((i0 >= 500) && (i1 < 600))
			{
				st.giveAdena(getRandom(49917) + 125000, false);
				if (i1 < 210)
				{
				}
				else if (i1 < 340)
				{
					st.giveItems(9628, getRandom(3) + 1);
				}
				else if (i1 < 470)
				{
					st.giveItems(9629, getRandom(3) + 1);
				}
				else if (i1 < 600)
				{
					st.giveItems(9630, getRandom(3) + 1);
				}
				
				htmltext = "32555-16.html";
			}
			else if ((i0 < 500) && (i1 >= 600))
			{
				st.giveAdena(getRandom(49917) + 25000, false);
				if (i1 < 720)
				{
					st.giveItems(9628, getRandom(3) + 1);
					st.giveItems(9629, getRandom(3) + 1);
				}
				else if (i1 < 840)
				{
					st.giveItems(9629, getRandom(3) + 1);
					st.giveItems(9630, getRandom(3) + 1);
				}
				else if (i1 < 960)
				{
					st.giveItems(9628, getRandom(3) + 1);
					st.giveItems(9630, getRandom(3) + 1);
				}
				else if (i1 < 1000)
				{
					st.giveItems(9628, getRandom(3) + 1);
					st.giveItems(9629, getRandom(3) + 1);
					st.giveItems(9630, getRandom(3) + 1);
				}
				htmltext = "32555-17.html";
			}
			else if ((i0 < 500) && (i1 < 600))
			{
				st.giveAdena(getRandom(49917) + 25000, false);
				if (i1 < 210)
				{
				}
				else if (i1 < 340)
				{
					st.giveItems(9628, getRandom(3) + 1);
				}
				else if (i1 < 470)
				{
					st.giveItems(9629, getRandom(3) + 1);
				}
				else if (i1 < 600)
				{
					st.giveItems(9630, getRandom(3) + 1);
				}
				
				htmltext = "32555-18.html";
			}
			st.takeItems(VariantDrakeWingHorns, 1);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "<html><body>目前沒有執行任務，或條件不符。</body></html>";
		final QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		if (npc.getNpcId() == Plenos)
		{
			switch (st.getState())
			{
				case State.CREATED:
					final QuestState prev = player.getQuestState("10273_GoodDayToFly");
					htmltext = ((prev != null) && prev.isCompleted() && (player.getLevel() >= 78)) ? "32563-01.htm" : "32563-02.htm";
					break;
				case State.STARTED:
					htmltext = (st.getInt("cond") == 1) ? "32563-05.html" : "32563-06.html";
					break;
			}
		}
		if (st.getState() == State.STARTED)
		{
			if (npc.getNpcId() == Lekon)
			{
				switch (st.getInt("cond"))
				{
					case 1:
						htmltext = "32557-01.html";
						break;
					case 2:
						htmltext = "32557-02.html";
						break;
				}
			}
			else if (npc.getNpcId() == Tenius)
			{
				switch (st.getInt("cond"))
				{
					case 1:
						htmltext = "32555-01.html";
						break;
					case 2:
						htmltext = "32555-04.html";
						break;
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		final L2PcInstance partyMember = getRandomPartyMember(player, "2");
		if (partyMember == null)
		{
			return null;
		}
		final QuestState st = partyMember.getQuestState(qn);
		final int chance = getRandom(1000);
		switch (npc.getNpcId())
		{
			case 22612:
				if (chance < 413)
				{
					st.giveItems(DrakeFlesh, 2);
				}
				else
				{
					st.giveItems(DrakeFlesh, 1);
				}
				break;
			case 22613:
				if (chance < 440)
				{
					st.giveItems(DrakeFlesh, 2);
				}
				else
				{
					st.giveItems(DrakeFlesh, 1);
				}
				break;
			case 25632:
				if (chance < 996)
				{
					st.giveItems(DrakeFlesh, 1);
				}
				break;
			case 22610:
				if (chance < 485)
				{
					st.giveItems(DrakeFlesh, 2);
				}
				else
				{
					st.giveItems(DrakeFlesh, 1);
				}
				break;
			case 22611:
				if (chance < 451)
				{
					st.giveItems(DrakeFlesh, 2);
				}
				else
				{
					st.giveItems(DrakeFlesh, 1);
				}
				break;
			case 25631:
				if (chance < 485)
				{
					st.giveItems(DrakeFlesh, 2);
				}
				else
				{
					st.giveItems(DrakeFlesh, 1);
				}
				break;
			case 25626:
				int count = 0;
				if (chance < 708)
				{
					count = getRandom(2) + 1;
				}
				else if (chance < 978)
				{
					count = getRandom(3) + 3;
				}
				else if (chance < 994)
				{
					count = getRandom(4) + 6;
				}
				else if (chance < 998)
				{
					count = getRandom(4) + 10;
				}
				else if (chance < 1000)
				{
					count = getRandom(5) + 14;
				}
				st.giveItems(VariantDrakeWingHorns, count);
				break;
		}
		st.playSound("ItemSound.quest_itemget");
		return null;
	}
	
	public Q702_ATrapForRevenge(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(Plenos);
		addTalkId(Plenos);
		addTalkId(Lekon);
		addTalkId(Tenius);
		addKillId(Monsters);
	}
	
	public static void main(String[] args)
	{
		new Q702_ATrapForRevenge(702, qn, "為了復仇的陷阱");
	}
}
