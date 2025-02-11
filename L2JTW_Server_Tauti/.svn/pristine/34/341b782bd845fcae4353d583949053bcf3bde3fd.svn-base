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
 * @author mrTJO
 */
public final class ExNotifyFlyMoveStart extends L2GameServerPacket
{
	private static final String _S__FE_E7_EXNOTIFYFLYMOVESTART = "[S] FE:E7 ExNotifyFlyMoveStart";
	
	//public static final ExNotifyFlyMoveStart STATIC_PACKET = new ExNotifyFlyMoveStart();
	
	public ExNotifyFlyMoveStart()
	{
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		/* l2jtw start
		writeH(0xE7); //writeH(0x114);
		 */
		writeH(0x115);//449 Test
		// l2jtw end
	}
	
	@Override
	public String getType()
	{
		return _S__FE_E7_EXNOTIFYFLYMOVESTART;
	}
}
