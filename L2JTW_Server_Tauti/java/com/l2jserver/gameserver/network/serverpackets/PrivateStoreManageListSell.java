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
 * 3 section to this packet
 * 1)playerinfo which is always sent
 * dd
 *
 * 2)list of items which can be added to sell
 * d(hhddddhhhd)
 *
 * 3)list of items which have already been setup
 * for sell in previous sell private store sell manageent
 * d(hhddddhhhdd) *
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class PrivateStoreManageListSell extends L2GameServerPacket
{
	private static final String _S__B3_PRIVATESELLLISTSELL = "[S] a0 PrivateSellListSell";
	private final int _objId;
	private final long _playerAdena;
	private final boolean _packageSale;
	private final TradeItem[] _itemList;
	private final TradeItem[] _sellList;
	
	public PrivateStoreManageListSell(L2PcInstance player, boolean isPackageSale)
	{
		_objId = player.getObjectId();
		_playerAdena = player.getAdena();
		player.getSellList().updateItems();
		_packageSale = isPackageSale;
		_itemList = player.getInventory().getAvailableItems(player.getSellList());
		_sellList = player.getSellList().getItems();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xa0);
		//section 1
		writeD(_objId);
		writeD(_packageSale ? 1 : 0); // Package sell
		writeQ(_playerAdena);
		
		//section2
		writeD(_itemList.length); //for potential sells
		for (TradeItem item : _itemList)
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
			// Player cannot sell/buy augmented, shadow or time-limited items.
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
			writeQ(item.getItem().getReferencePrice() * 2);
		}
		//section 3
		writeD(_sellList.length); //count for any items already added for sell
		for (TradeItem item : _sellList)
		{
			writeD(item.getObjectId());
			writeD(item.getItem().getItemId());
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
		return _S__B3_PRIVATESELLLISTSELL;
	}
}
