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

import com.l2jserver.gameserver.instancemanager.TerritoryWarManager;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author JIV
 *
 * Structure: ddddd
 */
public class ExDominionWarStart extends L2GameServerPacket
{
	private static final String TYPE = "[S] FE:A3 ExDominionWarStart";
	
	private int _objId;
	private int _terId;
	private boolean _isDisguised;
	
	public ExDominionWarStart(L2PcInstance player)
	{
		_objId = player.getObjectId();
		_terId = TerritoryWarManager.getInstance().getRegisteredTerritoryId(player);
		_isDisguised = TerritoryWarManager.getInstance().isDisguised(_objId);
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xA4);//449 Test
		writeD(_objId);
		writeD(1); //??
		writeD(_terId);
		writeD(_isDisguised ? 1 : 0);
		writeD(_isDisguised ? _terId : 0);
	}
	
	@Override
	public String getType()
	{
		return TYPE;
	}
}
