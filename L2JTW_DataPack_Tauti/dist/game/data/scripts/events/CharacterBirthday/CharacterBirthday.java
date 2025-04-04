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
package events.CharacterBirthday;

import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.model.quest.State;
import com.l2jserver.gameserver.network.serverpackets.PlaySound;
import com.l2jserver.gameserver.util.Util;

/**
 * @author Gnacik. Updated to H5 by Nyaran
 */
public class CharacterBirthday extends Quest
{
	private static final int _npc = 32600;
	private static int _spawns = 0;
	
	private final static int[] _gk =
	{
		30006, 30059, 30080, 30134, 30146, 30177, 30233, 30256, 30320, 30540, 30576, 30836, 30848, 30878, 30899, 31275, 31320, 31964, 32163
	};
	
	public CharacterBirthday(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(_npc);
		addTalkId(_npc);
		for (int id : _gk)
		{
			addStartNpc(id);
			addTalkId(id);
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(getName());
		
		if (event.equalsIgnoreCase("despawn_npc"))
		{
			npc.doDie(player);
			_spawns--;
			
			htmltext = null;
		}
		else if (event.equalsIgnoreCase("change"))
		{
			// Change Hat
			if (st.hasQuestItems(10250))
			{
				st.takeItems(10250, 1); // Adventurer Hat (Event)
				st.giveItems(21594, 1); // Birthday Hat
				htmltext = null; // FIXME: Probably has html
				// Despawn npc
				npc.doDie(player);
				_spawns--;
			}
			else
			{
				htmltext = "32600-nohat.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		if (_spawns >= 3)
		{
			return "busy.htm";
		}
		
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		if (!Util.checkIfInRange(10, npc, player, true))
		{
			L2Npc spawned = st.addSpawn(32600, player.getX() + 10, player.getY() + 10, player.getZ() + 10, 0, false, 0, true);
			player.sendPacket(new PlaySound(1, "HB01", 0, 0, 0, 0, 0));
			st.setState(State.STARTED);
			st.startQuestTimer("despawn_npc", 180000, spawned);
			_spawns++;
		}
		else
		{
			return "tooclose.htm";
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new CharacterBirthday(-1, "CharacterBirthday", "events");
	}
}
