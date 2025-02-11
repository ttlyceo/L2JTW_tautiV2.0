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

import javolution.util.FastList;

/**
 * @author a
 */
public final class ExInzoneWaitingInfo extends L2GameServerPacket
{
	private final FastList<Info> info = new FastList<>();
	private final int currentId;
	
	/**
	 * @param currentId
	 */
	public ExInzoneWaitingInfo(int currentId)
	{
		super();
		// TODO Auto-generated constructor stub
		this.currentId = currentId;
	}
	
	private class Info
	{
		public int id, second;
		
		public Info(int id, int second)
		{
			this.id = id;
			this.second = second;
		}
	}
	
	public void add(int id, int second)
	{
		info.add(new Info(id, second));
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		// TODO Auto-generated method stub
		writeC(0xfe);
		writeH(0x124);//449 Test
		writeD(currentId);
		writeD(info.size());
		for (Info i : info)
		{
			writeD(i.id);
			writeD(i.second);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket#getType()
	 */
	@Override
	public String getType()
	{
		// TODO Auto-generated method stub
		return "[S] FE:0x123 EXINZONEWAITINGINFO";
	}
	
}
