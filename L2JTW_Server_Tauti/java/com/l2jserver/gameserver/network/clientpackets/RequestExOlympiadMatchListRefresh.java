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
package com.l2jserver.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.olympiad.OlympiadGameManager;
import com.l2jserver.gameserver.model.olympiad.OlympiadGameTask;
import com.l2jserver.gameserver.network.serverpackets.ExOlympiadMatchList;

/**
 * Format: (ch)d
 * d: unknown (always 0?)
 * 
 * @author  mrTJO
 */
public class RequestExOlympiadMatchListRefresh extends L2GameClientPacket
{
	private static final String _C__D0_88_REQUESTEXOLYMPIADMATCHLISTREFRESH = "[C] D0:88 RequestExOlympiadMatchListRefresh";
	
	private int _unk; //488
	
	@Override
	protected void readImpl()
	{
		// readD();
		_unk = readD(); //488
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null || !activeChar.inObserverMode())
			return;
		
		List<OlympiadGameTask> games = new ArrayList<>();
		OlympiadGameTask task;
		for (int i = 0; i < OlympiadGameManager.getInstance().getNumberOfStadiums(); i++)
		{
			task = OlympiadGameManager.getInstance().getOlympiadTask(i);
			if (task != null)
			{
				if (!task.isBattleFinished())
					games.add(task);
			}
		}
		activeChar.sendPacket(new ExOlympiadMatchList(games));
	}
	
	@Override
	public String getType()
	{
		return _C__D0_88_REQUESTEXOLYMPIADMATCHLISTREFRESH;
	}
}