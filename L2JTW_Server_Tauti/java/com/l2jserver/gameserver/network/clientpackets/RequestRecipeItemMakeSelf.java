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

import com.l2jserver.gameserver.RecipeController;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Administrator
 */
public final class RequestRecipeItemMakeSelf extends L2GameClientPacket
{
	private static final String _C__B8_REQUESTRECIPEITEMMAKESELF = "[C] B8 RequestRecipeItemMakeSelf";
	
	private int _id;
	
	@Override
	protected void readImpl()
	{
		_id = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		if (!getClient().getFloodProtectors().getManufacture().tryPerformAction("RecipeMakeSelf"))
			return;
		
		if (activeChar.getPrivateStoreType() != 0)
		{
			/* Move To MessageTable For L2JTW
			activeChar.sendMessage("You cannot create items while trading.");
			*/
			activeChar.sendMessage(321);
			return;
		}
		
		if (activeChar.isInCraftMode())
		{
			/* Move To MessageTable For L2JTW
			activeChar.sendMessage("You are currently in Craft Mode.");
			*/
			activeChar.sendMessage(322);
			return;
		}
		
		RecipeController.getInstance().requestMakeItem(activeChar, _id);
	}
	
	@Override
	public String getType()
	{
		return _C__B8_REQUESTRECIPEITEMMAKESELF;
	}
}
