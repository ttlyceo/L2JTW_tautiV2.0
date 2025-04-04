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

import com.l2jserver.gameserver.datatables.NpcTable;
import com.l2jserver.gameserver.datatables.SpawnTable;
import com.l2jserver.gameserver.model.L2Spawn;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.templates.L2NpcTemplate;

/**
 * 被污染的莫列克戰士
 * 被污染的巴吐戰士
 * 被污染的巴吐指揮官
 * 圖魯卡部下怨靈
 * 圖魯卡首領怨靈 墓地守護靈
 */
public class CryptsOfDisgrace extends L2AttackableAIScript
{
	public static final int[] MOBS =
	{
		22703, 22704, 22705, 22706, 22707
	};
	
	private static final int[][] MobSpawns =
	{
		{18464, -28681, 255110, -2160, 10},
		{18464, -26114, 254708, -2139, 10},
		{18463, -28457, 256584, -1926, 10},
		{18463, -26482, 257663, -1925, 10},
		{18464, -26453, 256745, -1930, 10},
		{18463, -27362, 256282, -1935, 10},
		{18464, -25441, 256441, -2147, 10}
	};
	
	public CryptsOfDisgrace(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		for (int i : MOBS)
		{
			addKillId(i);
		}
		
		for (int i = 0; i < MobSpawns.length; i++)
		{
			int[] loc = MobSpawns[i];
			addSpawn(loc[0], loc[1], loc[2], loc[3], loc[4]);
		}
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		return super.onKill(npc, player, isPet);
	}
	
	public void addSpawn(int mobId, int x, int y, int z, int respTime)
	{
		L2NpcTemplate template1;
		template1 = NpcTable.getInstance().getTemplate(mobId);
		L2Spawn spawn = null;
		try
		{
			spawn = new L2Spawn(template1);
			spawn.setLocx(x);
			spawn.setLocy(y);
			spawn.setLocz(z);
			spawn.setAmount(1);
			spawn.setHeading(-1);
			spawn.setRespawnDelay(respTime);
			spawn.setInstanceId(0);
			spawn.setOnKillDelay(0);
			SpawnTable.getInstance().addNewSpawn(spawn, false);
			spawn.init();
			spawn.startRespawn();
			if (respTime == 0)
				spawn.stopRespawn();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		new CryptsOfDisgrace(-1, "CryptsOfDisgrace", "ai");
	}
}