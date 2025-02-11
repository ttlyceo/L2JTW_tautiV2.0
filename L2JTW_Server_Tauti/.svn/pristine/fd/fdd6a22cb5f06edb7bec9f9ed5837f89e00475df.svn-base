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

import com.l2jserver.Config;
import com.l2jserver.gameserver.model.BlockList;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ActionFailed;
import com.l2jserver.gameserver.network.serverpackets.SendTradeRequest;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;
import com.l2jserver.gameserver.util.Util;

/**
 * This class ...
 *
 * @version $Revision: 1.2.2.1.2.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class TradeRequest extends L2GameClientPacket
{
	private static final String _C__1A_TRADEREQUEST = "[C] 15 TradeRequest";
	
	private int _objectId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getActiveChar(); //rocknow-Sync L2J
		if (player == null)
			return;
		
		if (!player.getAccessLevel().allowTransaction())
		{
			/* Move To MessageTable For L2JTW
			player.sendMessage("Transactions are disabled for your current Access Level.");
			*/
			player.sendMessage(381);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2Object target = L2World.getInstance().findObject(_objectId); //rocknow-Sync L2J
		if ((target == null) || !player.getKnownList().knowsObject(target) || ((target.getInstanceId() != player.getInstanceId()) && (player.getInstanceId() != -1))) //rocknow-Sync L2J
		{
			//rocknow-Sync L2J player.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		if (target.getObjectId() == player.getObjectId())
		{
			player.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return;
		}
		
		if (!target.isPlayer()) //rocknow-Sync L2J
		{
			player.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		final L2PcInstance partner = target.getActingPlayer(); //rocknow-Sync L2J
		
		// cant trade with players from other instance except from multiverse
		if (partner.getInstanceId() != player.getInstanceId() && player.getInstanceId() != -1)
			return;
		
		if (partner.isInOlympiadMode() || player.isInOlympiadMode())
		{
			/* Move To MessageTable For L2JTW
			player.sendMessage("A user currently participating in the Olympiad cannot accept or request a trade.");
			*/
			player.sendMessage(382);
			return;
		}
		
		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TRADE && (player.getKarma() > 0))
		{
			/* Move To MessageTable For L2JTW
			player.sendMessage("You cannot trade while you are in a chaotic state.");
			*/
			player.sendMessage(383);
			return;
		}
		
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TRADE && (partner.getKarma() > 0))
		{
			/* Move To MessageTable For L2JTW
			player.sendMessage("You cannot request a trade while your target is in a chaotic state.");
			*/
			player.sendMessage(386);
			return;
		}
		
		if (Config.JAIL_DISABLE_TRANSACTION && (player.isInJail() || partner.isInJail()))
		{
			/* Move To MessageTable For L2JTW
			player.sendMessage("You cannot trade while you are in in Jail.");
			*/
			player.sendMessage(384);
			return;
		}
		
		if ((player.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_NONE) || (partner.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_NONE)) //rocknow-Sync L2J
		{
			player.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
			return;
		}
		
		if (player.isProcessingTransaction())
		{
			if (Config.DEBUG)
				_log.fine("Already trading with someone else.");
			player.sendPacket(SystemMessageId.ALREADY_TRADING);
			return;
		}
		
		SystemMessage sm;
		if (partner.isProcessingRequest() || partner.isProcessingTransaction())
		{
			if (Config.DEBUG)
				_log.info("Transaction already in progress.");
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER);
			sm.addString(partner.getName());
			player.sendPacket(sm);
			return;
		}
		
		if (partner.getTradeRefusal())
		{
			/* Move To MessageTable For L2JTW
			player.sendMessage("That person is in trade refusal mode.");
			*/
			player.sendMessage(385);
			return;
		}
		
		if (BlockList.isBlocked(partner, player))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST);
			sm.addCharName(partner);
			player.sendPacket(sm);
			return;
		}
		
		if (Util.calculateDistance(player, partner, true) > 150)
		{
			player.sendPacket(SystemMessageId.TARGET_TOO_FAR);
			return;
		}
		
		player.onTransactionRequest(partner);
		partner.sendPacket(new SendTradeRequest(player.getObjectId()));
		sm = SystemMessage.getSystemMessage(SystemMessageId.REQUEST_C1_FOR_TRADE);
		sm.addString(partner.getName());
		player.sendPacket(sm);
	}
	
	@Override
	public String getType()
	{
		return _C__1A_TRADEREQUEST;
	}
}
