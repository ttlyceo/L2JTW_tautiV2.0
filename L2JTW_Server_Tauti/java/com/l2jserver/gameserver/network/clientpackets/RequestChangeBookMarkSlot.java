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
package com.l2jserver.gameserver.network.clientpackets;

/**
 *  @author ShanSoft
 *  Packets Structure: chddd
 */
public final class RequestChangeBookMarkSlot extends L2GameClientPacket
{
	private static final String _C__D0_51_05_REQUESCHANGEBOOKMARKSLOT = "[C] D0:51:05 RequestChangeBookMarkSlot";
	
	private int _unk1; //488
	private int _unk2; //488
	
	@Override
	protected void readImpl()
	{
		//There is nothing to read.
		_unk1 = readD(); //488
		_unk2 = readD(); //488
	}
	
	@Override
	protected void runImpl()
	{
		
	}
	
	
	@Override
	public String getType()
	{
		return _C__D0_51_05_REQUESCHANGEBOOKMARKSLOT;
	}
}
