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

import com.l2jserver.gameserver.model.entity.Message;
import com.l2jserver.gameserver.model.itemcontainer.ItemContainer;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;

/**
 * @author Migi, DS
 */
public class ExReplyReceivedPost extends L2GameServerPacket
{
	private static final String _S__FE_AB_EXSHOWRECEIVEDPOST = "[S] FE:AB ExShowReceivedPost";
	
	private Message _msg;
	private L2ItemInstance[] _items = null;
	
	public ExReplyReceivedPost(Message msg)
	{
		_msg = msg;
		if (msg.hasAttachments())
		{
			final ItemContainer attachments = msg.getAttachments();
			if (attachments != null && attachments.getSize() > 0)
				_items = attachments.getItems();
			else
				_log.warning("Message "+msg.getId()+" has attachments but itemcontainer is empty.");
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0xac);//449 Test
		writeD(0x00); //rocknow-God
		writeD(_msg.getId());
		writeD(_msg.isLocked() ? 1 : 0);
		writeD(0x00); //Unknown
		writeS(_msg.getSenderName());
		writeS(_msg.getSubject());
		writeS(_msg.getContent());
		
		if (_items != null && _items.length > 0)
		{
			writeD(_items.length);
			for (L2ItemInstance item : _items)
			{
				writeD(0x00);
				writeD(item.getDisplayId());
				writeD(item.getLocationSlot());
				writeQ(item.getCount());
				writeH(item.getItem().getType2());
				writeH(item.getCustomType1());
				writeH(item.isEquipped() ? 0x01 : 0x00);
				writeD(item.getItem().getBodyPart());
				writeH(item.getEnchantLevel());
				writeH(item.getCustomType2());
				if (item.isAugmented())
					writeD(item.getAugmentation().getAugmentationId());
				else
					writeD(0x00);
				writeD(item.getMana());
				writeD(item.isTimeLimitedItem() ? (int) (item.getRemainingTime() / 1000) : -9999);
				writeH(0x01); //rocknow-God
				writeH(item.getAttackElementType());
				writeH(item.getAttackElementPower());
				for (byte i = 0; i < 6; i++)
				{
					writeH(item.getElementDefAttr(i));
				}
				// Enchant Effects
				writeH(0);
				writeH(0);
				writeH(0);
				writeD(0x00); //rocknow-God-Weapon Appearance
				writeD(item.getObjectId());
			}
			_items = null;
		}
		else
			writeD(0x00);
		
		writeQ(_msg.getReqAdena());
		writeD(_msg.hasAttachments() ? 1 : 0);
		writeD(_msg.getSendBySystem());
		
		_msg = null;
	}
	
	@Override
	public String getType()
	{
		return _S__FE_AB_EXSHOWRECEIVEDPOST;
	}
}
