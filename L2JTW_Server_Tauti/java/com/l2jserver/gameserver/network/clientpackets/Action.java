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
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ActionFailed;
import com.l2jserver.gameserver.network.serverpackets.ExAbnormalStatusUpdateFromTargetPacket; //rocknow-God-Awaking

/**
 * This class ...
 * @version $Revision: 1.7.4.4 $ $Date: 2005/03/27 18:46:19 $
 */
@SuppressWarnings("unused")
public final class Action extends L2GameClientPacket
{
	private static final String __C__1F_ACTION = "[C] 1F Action";
	
	private int _objectId;
	private int _originX;
	private int _originY;
	private int _originZ;
	private int _actionId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD(); // Target object Identifier
		_originX = readD();
		_originY = readD();
		_originZ = readD();
		_actionId = readC(); // Action identifier : 0-Simple click, 1-Shift click
	}
	
	@Override
	protected void runImpl()
	{
		if (Config.DEBUG)
		{
			_log.fine(getType() + ": Action:" + _actionId + " ObjId: " + _objectId);
		}
		
		// Get the current L2PcInstance of the player
		final L2PcInstance activeChar = getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (activeChar.inObserverMode())
		{
			activeChar.sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2Object obj;
		if (activeChar.getTargetId() == _objectId)
		{
			obj = activeChar.getTarget();
		}
		else if (activeChar.isInAirShip() && (activeChar.getAirShip().getHelmObjectId() == _objectId))
		{
			obj = activeChar.getAirShip();
		}
		else
		{
			obj = L2World.getInstance().findObject(_objectId);
		}
		
		// If object requested does not exist, add warn msg into logs
		if (obj == null)
		{
			// pressing e.g. pickup many times quickly would get you here
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (!obj.isTargetable()) //rocknow-God
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Players can't interact with objects in the other instances, except from multiverse
		if ((obj.getInstanceId() != activeChar.getInstanceId()) && (activeChar.getInstanceId() != -1))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Only GMs can directly interact with invisible characters
		if (obj.isPlayer() && obj.getActingPlayer().getAppearance().getInvisible() && !activeChar.isGM())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if the target is valid, if the player haven't a shop or isn't the requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...)
		if (activeChar.getActiveRequester() != null)
		{
			// Actions prohibited when in trade
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		switch (_actionId)
		{
			case 0:
			{
				obj.onAction(activeChar);
				activeChar.sendPacket(new ExAbnormalStatusUpdateFromTargetPacket(activeChar.getTargetId())); //rocknow-God-Awaking
				break;
			}
			case 1:
			{
				if (!activeChar.isGM() && !(obj.isNpc() && Config.ALT_GAME_VIEWNPC))
				{
					obj.onAction(activeChar, false);
				}
				else
				{
					obj.onActionShift(activeChar);
				}
				break;
			}
			default:
			{
				// Invalid action detected (probably client cheating), log this
				_log.warning(getType() + ": Character: " + activeChar.getName() + " requested invalid action: " + _actionId);
				sendPacket(ActionFailed.STATIC_PACKET);
				break;
			}
		}
	}
	
	@Override
	public String getType()
	{
		return __C__1F_ACTION;
	}
}
