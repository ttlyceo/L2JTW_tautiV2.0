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

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.instancemanager.AwakingManager;

/**
 * Format: (ch) d
 * @author  ALF
 */
public class RequestChangeToAwakenedClass extends L2GameClientPacket
{
	private static final String _C__D0_A5_REQUESTCHANGETOAWAKENEDCLASS = "[C] D0:A5 RequestChangeToAwakenedClass";
	
	private int _newId;
	
	@Override
	protected void readImpl()
	{
		_newId = readD(); // 1 - , 0 - 
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		if (_newId > 0)
			AwakingManager.getInstance().SetAwakingId(activeChar);
	}
	
	@Override
	public String getType()
	{
		return _C__D0_A5_REQUESTCHANGETOAWAKENEDCLASS;
	}
}
