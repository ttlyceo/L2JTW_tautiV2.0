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
package com.l2jserver.gameserver.network.serverpackets;

import com.l2jserver.gameserver.model.L2Clan; //488 Test
/**
 * Format: (ch) ddd b
 * d: ?
 * d: crest ID
 * d: crest size
 * b: raw data
 * @author -Wooden-
 *
 */
public class ExPledgeCrestLarge extends L2GameServerPacket
{
	private static final String _S__FE_28_EXPLEDGECRESTLARGE = "[S] FE:1b ExPledgeCrestLarge";
	private L2Clan _clan; //488 Test
	private int _crestId;
	private byte[] _data;
	
	public ExPledgeCrestLarge(L2Clan clan, int crestId, byte[] data) //488 Test
	{
		_clan = clan; //488 Test
		_crestId = crestId;
		_data = data;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x1b);
		
		//488 Test-Start
		writeD(_clan.getClanId());
		writeD(_crestId);
		writeD(0); //_data index 0/1/2/3/4
		if (_crestId > 0)
		{
			writeD(65664); //_data.length-Total 14336*4+8320=65664
			writeD(_data.length); //_data.length-Split 14336/14336/14336/14336/8320
			writeB(_data);
		}
		else
		{
			writeD(0); //guess
			writeD(0); //guess
		}
		//488 Test-End
		
		
	}
	
	@Override
	public String getType()
	{
		return _S__FE_28_EXPLEDGECRESTLARGE;
	}
	
}