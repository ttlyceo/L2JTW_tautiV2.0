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
 * This class ...
 *
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class ExAutoSoulShot extends L2GameServerPacket
{
	private static final String _S__FE_12_EXAUTOSOULSHOT = "[S] FE:0c ExAutoSoulShot";
	private int _itemId;
	private int _type;
	
	/**
	 * 0xfe:0x12 ExAutoSoulShot (ch)dd
	 * @param itemId
	 * @param type
	 */
	public ExAutoSoulShot(int itemId, int type)
	{
		_itemId = itemId;
		_type = type;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0x0c);     // sub id
		writeD(_itemId);
		writeD(_type);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_12_EXAUTOSOULSHOT;
	}
}
