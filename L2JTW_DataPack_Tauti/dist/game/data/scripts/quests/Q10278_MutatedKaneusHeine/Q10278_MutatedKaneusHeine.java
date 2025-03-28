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
package quests.Q10278_MutatedKaneusHeine;

import java.util.ArrayList;
import java.util.List;

import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.model.quest.State;

/**
 * Mutated Kaneus - Heine (10278).<br>
 * Original Jython script by Gnacik on 2010-06-29
 * @author nonom
 */
public class Q10278_MutatedKaneusHeine extends Quest
{
	private static final String qn = "10278_MutatedKaneusHeine";
	
	// NPCs
	private static final int GOSTA = 30916;
	private static final int MINEVIA = 30907;
	private static final int BLADE_OTIS = 18562;
	private static final int WEIRD_BUNEI = 18564;
	
	// Items
	private static final int TISSUE_BO = 13834;
	private static final int TISSUE_WB = 13835;
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "<html><body>目前沒有執行任務，或條件不符。</body></html>";
		final QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		switch (npc.getNpcId())
		{
			case GOSTA:
				if (st.isCompleted())
				{
					htmltext = "30916-06.htm";
				}
				else if (st.isCreated())
				{
					htmltext = (player.getLevel() >= 38) ? "30916-01.htm" : "30916-00.htm";
				}
				else if (st.hasQuestItems(TISSUE_BO) && st.hasQuestItems(TISSUE_WB))
				{
					htmltext = "30916-05.htm";
				}
				else if (st.getInt("cond") == 1)
				{
					htmltext = "30916-04.htm";
				}
				break;
			case MINEVIA:
				if (st.isCompleted())
				{
					htmltext = "<html><body>這是已經完成的任務。</body></html>";
				}
				else if (st.hasQuestItems(TISSUE_BO) && !st.hasQuestItems(TISSUE_WB))
				{
					htmltext = "30907-02.htm";
				}
				else
				{
					htmltext = "30907-01.htm";
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "30916-03.htm":
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
				break;
			case "30907-03.htm":
				st.rewardItems(57, 50000);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		QuestState st = killer.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		
		final int npcId = npc.getNpcId();
		if (killer.getParty() != null)
		{
			final List<QuestState> PartyMembers = new ArrayList<>();
			for (L2PcInstance member : killer.getParty().getMembers())
			{
				st = member.getQuestState(qn);
				if ((st != null) && st.isStarted() && (st.getInt("cond") == 1) && (((npcId == BLADE_OTIS) && !st.hasQuestItems(TISSUE_BO)) || ((npcId == WEIRD_BUNEI) && !st.hasQuestItems(TISSUE_WB))))
				{
					PartyMembers.add(st);
				}
			}
			
			if (!PartyMembers.isEmpty())
			{
				rewardItem(npcId, PartyMembers.get(getRandom(PartyMembers.size())));
			}
		}
		else
		{
			rewardItem(npcId, st);
		}
		return null;
	}
	
	/**
	 * @param npcId the killed monster Id.
	 * @param st the quest state of the killer or party member.
	 */
	private final void rewardItem(int npcId, QuestState st)
	{
		if ((npcId == BLADE_OTIS) && !st.hasQuestItems(TISSUE_BO))
		{
			st.giveItems(TISSUE_BO, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		else if ((npcId == WEIRD_BUNEI) && !st.hasQuestItems(TISSUE_WB))
		{
			st.giveItems(TISSUE_WB, 1);
			st.playSound("ItemSound.quest_itemget");
		}
	}
	
	public Q10278_MutatedKaneusHeine(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(GOSTA);
		addTalkId(MINEVIA);
		addTalkId(MINEVIA);
		
		addKillId(BLADE_OTIS);
		addKillId(WEIRD_BUNEI);
		
		questItemIds = new int[]
		{
			TISSUE_BO,
			TISSUE_WB
		};
	}
	
	public static void main(String[] args)
	{
		new Q10278_MutatedKaneusHeine(10278, qn, "Mutated Kaneus - Heine");
	}
}
