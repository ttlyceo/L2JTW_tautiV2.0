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
 * @author JIV
 */
public class ExEnchantSkillResult extends L2GameServerPacket
{
	private static final ExEnchantSkillResult STATIC_PACKET_TRUE = new ExEnchantSkillResult(true);
	private static final ExEnchantSkillResult STATIC_PACKET_FALSE = new ExEnchantSkillResult(false);
	
	public static final ExEnchantSkillResult valueOf(boolean result)
	{
		return result ? STATIC_PACKET_TRUE : STATIC_PACKET_FALSE;
	}
	
	private boolean _enchanted;
	
	public ExEnchantSkillResult(boolean enchanted)
	{
		_enchanted = enchanted;
	}
	
	
	/**
	 * @see com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket#getType()
	 */
	@Override
	public String getType()
	{
		return "[S] FE:A7 ExEnchantSkillResult";
	}
	
	/**
	 * @see com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0xa8);//449 Test
		writeD(_enchanted ? 1 : 0);
	}
	
}
