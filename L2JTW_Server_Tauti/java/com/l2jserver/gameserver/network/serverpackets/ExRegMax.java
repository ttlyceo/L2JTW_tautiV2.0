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

public class ExRegMax extends L2GameServerPacket
{
	
	private static final String _S__FE_01_EXREGMAX = "[S] FE:01 ExRegenMax";
	private double _max;
	private int _count;
	private int _time;
	
	public ExRegMax(double max, int count, int time)
	{
		_max = max;
		_count = count;
		_time = time;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x01);
		writeD(1);
		writeD(_count);
		writeD(_time);
		writeF(_max);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_01_EXREGMAX;
	}
	
}
