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
package quests.Q10284_AcquisitionOfDivineSword;

import com.l2jserver.gameserver.instancemanager.InstanceManager;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.entity.Instance;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.model.quest.State;

/**
 ** @author GKR
 **
 ** 2011-03-24
 */
public class Q10284_AcquisitionOfDivineSword extends Quest
{
	private static final String qn = "10284_AcquisitionOfDivineSword";
	// NPC's
	private static final int _rafforty = 32020;
	private static final int _jinia = 32760;
	private static final int _kroon = 32653;
	private static final int _taroon = 32654;
	
	public Q10284_AcquisitionOfDivineSword(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(_rafforty);
		addTalkId(_rafforty);
		addTalkId(_jinia);
		addTalkId(_kroon);
		addTalkId(_taroon);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return htmltext;
		
		if (npc.getNpcId() == _rafforty)
		{
			if (event.equalsIgnoreCase("32020-04.htm"))
			{
				st.setState(State.STARTED);
				st.set("progress", "1");
				st.set("cond", "1");
				st.set("jinia_themes", "102030"); //theme ID - state - something like 1-0, 2-0, 3-0
				st.playSound("ItemSound.quest_accept");
			}
		}
		
		else if (npc.getNpcId() == _jinia)
		{
			if (event.equalsIgnoreCase("32760-05.htm"))
			{
				switch(st.getInt("jinia_themes"))
				{
				case 112030: //1st theme have been readed
					htmltext = "32760-05a.htm";
					break;
				
				case 102130: //2nd theme have been readed
					htmltext = "32760-05b.htm";
					break;
				
				case 102031: //3rd theme have been readed
					htmltext = "32760-05c.htm";
					break;
				
				case 102131: //2nd and 3rd theme have been readed
					htmltext = "32760-05d.htm";
					break;
				
				case 112031: //1st and 3rd theme have been readed
					htmltext = "32760-05e.htm";
					break;
				
				case 112130: //1st and 2nd theme have been readed
					htmltext = "32760-05f.htm";
					break;
				
				case 112131: //all three themes have been readed
					htmltext = "32760-05g.htm";
				}
			}
			
			else if (event.equalsIgnoreCase("32760-02c.htm"))
			{
				int jinia_themes = st.getInt("jinia_themes");
				jinia_themes += 10000; //mark 1st theme as readed
				st.set("jinia_themes", Integer.toString(jinia_themes));
			}
			
			else if (event.equalsIgnoreCase("32760-03c.htm"))
			{
				int jinia_themes = st.getInt("jinia_themes");
				jinia_themes += 100; //mark 2nd theme as readed
				st.set("jinia_themes", Integer.toString(jinia_themes));
			}
			
			else if (event.equalsIgnoreCase("32760-04c.htm"))
			{
				int jinia_themes = st.getInt("jinia_themes");
				jinia_themes += 1; //mark 3rd theme as readed
				st.set("jinia_themes", Integer.toString(jinia_themes));
			}
			
			else if (event.equalsIgnoreCase("32760-07.htm"))
			{
				st.set("jinia_themes","102030");
				st.set("progress", "2");
				st.set("cond", "3");
				st.playSound("ItemSound.quest_middle");
			
			// destroy instance after 1 min
			Instance inst = InstanceManager.getInstance().getInstance(player.getInstanceId());
			inst.setDuration(60000);
			inst.setEmptyDestroyTime(0);
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "<html><body>目前沒有執行任務，或條件不符。</body></html>";
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (npc.getNpcId() == _rafforty)
		{
			switch (st.getState())
			{
				case State.CREATED:
					QuestState _prev = player.getQuestState("10283_RequestOfIceMerchant");
					if ((_prev != null) && (_prev.getState() == State.COMPLETED) && (player.getLevel() >= 82))
						htmltext = "32020-01.htm";
					else
						htmltext = "32020-03.htm";
					break;
				case State.STARTED:
					if (st.getInt("progress") == 1)
						htmltext = "32020-05.htm";
					else if (st.getInt("progress") == 2)
						htmltext = "32020-09.htm";
					break;
				case State.COMPLETED:
					htmltext = "32020-02.htm";
					break;
			}
		}
		else if (npc.getNpcId() == _jinia)
		{
			if (st.getState() != State.STARTED)
				return htmltext;
			
			if (st.getInt("progress") == 1)
			{
				int jinia_themes = st.getInt("jinia_themes");
				//look above for explanation 
				switch(jinia_themes)
				{
					case 102030:
						htmltext = "32760-01.htm"; 
						break;
					case 112030:
						htmltext = "32760-01a.htm"; 
						break;
					case 102130:
						htmltext = "32760-01b.htm"; 
						break;
					case 102031:
						htmltext = "32760-01c.htm"; 
						break;
					case 102131:
						htmltext = "32760-01d.htm"; 
						break;
					case 112031:
						htmltext = "32760-01e.htm"; 
						break;
					case 112130:
						htmltext = "32760-01f.htm"; 
						break;
					case 112131:
						htmltext = "32760-01g.htm"; 
						break;
				}
			}
		}
		
		else if (npc.getNpcId() == _kroon || npc.getNpcId() == _taroon)
		{
			if (st.getState() != State.STARTED)
				return "<html><body>目前沒有執行任務，或條件不符。</body></html>";
			
			if (st.getInt("progress") == 2)
				htmltext = npc.getNpcId() == _kroon ? "32653-01.htm" : "32654-01.htm";
			
			else if (st.getInt("progress") == 3)
			{
				st.set("jinia_themes","102030");
				st.giveItems(57, 296425);
				st.addExpAndSp(921805, 82230);
				st.playSound("ItemSound.quest_finish");
				htmltext = npc.getNpcId() == _kroon ? "32653-05.htm" : "32654-05.htm";
				st.exitQuest(false);
			}
		}
		
		return htmltext;
	}

	public static void main(String[] args)
	{
		new Q10284_AcquisitionOfDivineSword(10284, qn, "獲得神劍");
	}
}