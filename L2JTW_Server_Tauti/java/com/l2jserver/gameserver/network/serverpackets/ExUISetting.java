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

import java.util.List;

import com.l2jserver.gameserver.model.L2UIKeysSettings;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.entity.ActionKey;

/**
 *
 * @author  mrTJO
 */
public class ExUISetting extends L2GameServerPacket
{
	
	private final L2UIKeysSettings _uiSettings;
	private int buffsize, categories;
	
	
	public ExUISetting(L2PcInstance player)
	{
		_uiSettings = player.getUISettings();
		calcSize();
	}
	
	/**
	 * @see com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket#getType()
	 */
	@Override
	public String getType()
	{
		return null;
	}
	
	private void calcSize()
	{
		int size = 16; //initial header and footer
		int category = 0;
		int numKeyCt = _uiSettings.getKeys().size();
		for (int i = 0; i < numKeyCt; i++)
		{
			size++;
			if (_uiSettings.getCategories().containsKey(category))
			{
				List<Integer> catElList1 = _uiSettings.getCategories().get(category);
				size = size + catElList1.size();
			}
			category++;
			size++;
			if (_uiSettings.getCategories().containsKey(category))
			{
				List<Integer> catElList2 = _uiSettings.getCategories().get(category);
				size = size + catElList2.size();
			}
			category++;
			size = size+4;
			if (_uiSettings.getKeys().containsKey(i))
			{
				List<ActionKey> keyElList = _uiSettings.getKeys().get(i);
				size = size + (keyElList.size()*20);
			}
		}
		buffsize = size;
		categories = category;
	}
	
	/**
	 * @see com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x71);//449 Test
		
		writeD(buffsize);
		writeD(categories);
		
		int category = 0;
		
		int numKeyCt = _uiSettings.getKeys().size();
		writeD(numKeyCt);
		for (int i = 0; i < numKeyCt; i++)
		{
			if (_uiSettings.getCategories().containsKey(category))
			{
				List<Integer> catElList1 = _uiSettings.getCategories().get(category);
				writeC(catElList1.size());
				for (int cmd : catElList1)
				{
					writeC(cmd);
				}
			}
			else
			{
				writeC(0x00);
			}
			category++;
			
			if (_uiSettings.getCategories().containsKey(category))
			{
				List<Integer> catElList2 = _uiSettings.getCategories().get(category);
				writeC(catElList2.size());
				for (int cmd : catElList2)
				{
					writeC(cmd);
				}
			}
			else
			{
				writeC(0x00);
			}
			category++;
			
			if (_uiSettings.getKeys().containsKey(i))
			{
				List<ActionKey> keyElList = _uiSettings.getKeys().get(i);
				writeD(keyElList.size());
				for (ActionKey akey : keyElList)
				{
					writeD(akey.getCommandId());
					writeD(akey.getKeyId());
					writeD(akey.getToogleKey1());
					writeD(akey.getToogleKey2());
					writeD(akey.getShowStatus());
				}
			}
			else
			{
				writeD(0x00);
			}
		}
		writeD(0x11);
		writeD(0x10);
	}
}
