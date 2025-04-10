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

import com.l2jserver.gameserver.model.TradeItem;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 *
 * @version $Revision: 1.2.2.3.2.6 $ $Date: 2005/03/27 15:29:57 $
 */
public class PrivateStoreListSell extends L2GameServerPacket
{
	private static final String _S__B4_PRIVATESTORELISTSELL = "[S] a1 PrivateStoreListSell";
	private final int _objId;
	private final long _playerAdena;
	private final boolean _packageSale;
	private final TradeItem[] _items;
	
	// player's private shop
	public PrivateStoreListSell(L2PcInstance player, L2PcInstance storePlayer)
	{
		_objId = storePlayer.getObjectId();
		_playerAdena = player.getAdena();
		_items = storePlayer.getSellList().getItems();
		_packageSale = storePlayer.getSellList().isPackaged();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xa1);
		writeD(_objId);
		writeD(_packageSale ? 1 : 0);
		writeQ(_playerAdena);
		writeD(0x00); //rocknow-God
		writeD(_items.length);
		for (TradeItem item : _items)
		{
			writeD(item.getObjectId());
			writeD(item.getItem().getDisplayId());
			writeD(item.getLocationSlot());
			writeQ(item.getCount());
			writeH(item.getItem().getType2());
			writeH(item.getCustomType1());
			writeH(0x00);
			writeD(item.getItem().getBodyPart());
			writeH(item.getEnchant());
			writeH(item.getCustomType2());
			// Player cannot sell/buy augmented, shadow or time-limited items
			// probably so hardcode values here
			writeD(0x00);	// Augment
			writeD(-1);		// Mana
			writeD(-9999);	// Time
			writeH(0x01); //rocknow-God
			writeH(item.getAttackElementType());
			writeH(item.getAttackElementPower());
			for (byte i = 0; i < 6; i++)
			{
				writeH(item.getElementDefAttr(i));
			}
			// Enchant Effects
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			writeD(0x00); //rocknow-God-Weapon Appearance
			writeQ(item.getPrice());
			writeQ(item.getItem().getReferencePrice() * 2);
		}
	}
	
	@Override
	public String getType()
	{
		return _S__B4_PRIVATESTORELISTSELL;
	}
}
