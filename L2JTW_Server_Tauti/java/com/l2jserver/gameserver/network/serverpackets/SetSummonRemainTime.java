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

/**
 *
 * format  (c) dd
 *
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:40 $
 */
public final class SetSummonRemainTime extends L2GameServerPacket
{
	private static final String _S__D1_SET_SUMMON_REMAIN_TIME = "[S] d1 SetSummonRemainTime";
	private int _maxTime;
	private int _remainingTime;
	
	public SetSummonRemainTime(int maxTime, int remainingTime)
	{
		_remainingTime = remainingTime;
		_maxTime = maxTime;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xd1);
		writeD(_maxTime);
		writeD(_remainingTime);
	}
	
	@Override
	public String getType()
	{
		return _S__D1_SET_SUMMON_REMAIN_TIME;
	}
	
}
