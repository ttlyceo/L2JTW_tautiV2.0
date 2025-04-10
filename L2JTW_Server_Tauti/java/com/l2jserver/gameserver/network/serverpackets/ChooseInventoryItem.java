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

public final class ChooseInventoryItem extends L2GameServerPacket
{
	private static final String _S__6F_CHOOSEINVENTORYITEM = "[S] 7c ChooseInventoryItem";
	
	private int _itemId;
	
	public ChooseInventoryItem(int itemId)
	{
		_itemId=itemId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x7c);
		writeD(_itemId);
	}
	
	@Override
	public String getType()
	{
		return _S__6F_CHOOSEINVENTORYITEM;
	}
}
