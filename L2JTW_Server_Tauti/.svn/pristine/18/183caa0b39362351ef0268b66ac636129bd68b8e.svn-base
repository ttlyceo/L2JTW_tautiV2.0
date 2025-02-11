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

import java.sql.Connection; //rocknow-God
import java.sql.PreparedStatement; //rocknow-God
import java.sql.ResultSet; //rocknow-God
import java.util.List;

import javolution.util.FastList;

import com.l2jserver.L2DatabaseFactory; //rocknow-God
import com.l2jserver.gameserver.datatables.CharNameTable;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * Support for "Chat with Friends" dialog.
 *
 * This packet is sent only at login.
 *
 * Format: cd (dSdd)
 * d: Total Friend Count
 *
 * d: Player Object ID
 * S: Friend Name
 * d: Online/Offline
 * d: Unknown (0 if offline)
 *
 * @author Tempy
 */
public class FriendList extends L2GameServerPacket
{
	private static final String _S__FA_FRIENDLIST = "[S] 75 FriendList";
	private final List<FriendInfo> _info; //rocknow-God
	
	private static class FriendInfo
	{
		int objId;
		String name;
		boolean online;
		int classid; //rocknow-God
		int level; //rocknow-God
		
		public FriendInfo(int objId, String name, boolean online, int classid, int level) //rocknow-God
		{
			this.objId = objId;
			this.name = name;
			this.online = online;
			this.classid = classid; //rocknow-God
			this.level = level; //rocknow-God
		}
	}
	
	public FriendList(L2PcInstance player)
	{
		_info = new FastList<>(player.getFriendList().size());
		for (int objId : player.getFriendList())
		{
			String name = CharNameTable.getInstance().getNameById(objId);
			L2PcInstance player1 = L2World.getInstance().getPlayer(objId);
			boolean online = false;
			int classid = 0; //rocknow-God
			int level = 0; //rocknow-God
 			//rocknow-God-Start
			if (player1 == null)
			{
				Connection con = null;
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement("SELECT char_name, online, classid, level FROM characters WHERE charId = ?");
					statement.setInt(1, objId);
					ResultSet rset = statement.executeQuery();
					if (rset.next())
					{
						_info.add(new FriendInfo(objId, rset.getString(1), rset.getInt(2) == 1, rset.getInt(3), rset.getInt(4)));
					}
					else
						continue;
				}
				catch (Exception e)
				{
					// Who cares?
				}
				finally
				{
					L2DatabaseFactory.close(con);
				}
				
				continue;
			}
			//rocknow-God-End
			if (player1.isOnline()) //rocknow-God
				online = true;
			classid = player1.getClassId().getId(); //rocknow-God
			level = player1.getLevel(); //rocknow-God
			_info.add(new FriendInfo(objId, name, online, classid, level)); //rocknow-God
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x75);
		writeD(_info.size());
		for (FriendInfo info : _info)
		{
			writeD(info.objId); // character id
			writeS(info.name);
			writeD(info.online ? 0x01 : 0x00); // online
			writeD(info.online ? info.objId : 0x00); // object id if online
			writeD(info.level); //rocknow-God
			writeD(info.classid); //rocknow-God
			writeH(0x00); //rocknow-God
		}
	}
	
	@Override
	public String getType()
	{
		return _S__FA_FRIENDLIST;
	}
}
