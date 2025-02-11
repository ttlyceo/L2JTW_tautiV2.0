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

import java.util.logging.Level;

import com.l2jserver.gameserver.cache.CrestCache;
import com.l2jserver.gameserver.idfactory.IdFactory;
import com.l2jserver.gameserver.model.L2Clan;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.SystemMessageId;

/**
 * Format : chdb
 * c (id) 0xD0
 * h (subid) 0x11
 * d data size
 * b raw data (picture i think ;) )
 * @author -Wooden-
 */
public final class RequestExSetPledgeCrestLarge extends L2GameClientPacket
{
	private static final String _C__D0_11_REQUESTEXSETPLEDGECRESTLARGE = "[C] D0:11 RequestExSetPledgeCrestLarge";
	
	private int _index; //488 Test
	private int _total; //488 Test
	private int _length;
	private byte[] _data;
	
	@Override
	protected void readImpl()
	{
		_index = readD(); //_data index 0/1/2/3/4
		_total = readD(); //_length-Total 14336*4+8320=65664
		_length = readD(); //_length-Split 14336/14336/14336/14336/8320
		if (_length > 14336) //488 Test
			return;
		
		_data = new byte[_length];
		readB(_data);
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		L2Clan clan = activeChar.getClan();
		if (clan == null)
			return;
		
		if (_index > 0) // DeleteME: When support index 0/1/2/3/4
			return;
			
		if (_length < 0)
		{
			/* Move To MessageTable For L2JTW
			activeChar.sendMessage("File transfer error.");
			*/
			activeChar.sendMessage(280);
			return;
		}
		if (_length > 14336) //488 Test
		{
			/* Move To MessageTable For L2JTW
			activeChar.sendMessage("The insignia file size is greater than 2176 bytes.");
			*/
			activeChar.sendMessage(279);
			return;
		}
		
		boolean updated = false;
		int crestLargeId = -1;
		if ((activeChar.getClanPrivileges() & L2Clan.CP_CL_REGISTER_CREST) == L2Clan.CP_CL_REGISTER_CREST)
		{
			if (_length == 0 || _data == null)
			{
				if (clan.getCrestLargeId() == 0)
					return;
				
				crestLargeId = 0;
				/*
				activeChar.sendMessage("The insignia has been removed.");
				*/
				activeChar.sendPacket(SystemMessageId.CLAN_CREST_HAS_BEEN_DELETED);  //Update by pmq
				updated = true;
			}
			else
			{
				if (clan.getCastleId() == 0 && clan.getHideoutId() == 0)
				{
					/*
					activeChar.sendMessage("Only a clan that owns a clan hall or a castle can get their emblem displayed on clan related items"); //there is a system message for that but didnt found the id
					*/
					activeChar.sendPacket(SystemMessageId.CLAN_EMBLEM_WAS_SUCCESSFULLY_REGISTERED); //there is a system message for that but didnt found the id  //Update by pmq
					return;
				}
				
				crestLargeId = IdFactory.getInstance().getNextId();
				if (!CrestCache.getInstance().savePledgeCrestLarge(crestLargeId, _data))
				{
					_log.log(Level.INFO, "Error saving large crest for clan " + clan.getName() + " [" + clan.getClanId() + "]");
					return;
				}
				
				activeChar.sendPacket(SystemMessageId.CLAN_EMBLEM_WAS_SUCCESSFULLY_REGISTERED);
				updated = true;
			}
		}
		
		if (updated && crestLargeId != -1)
		{
			clan.changeLargeCrest(crestLargeId);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_11_REQUESTEXSETPLEDGECRESTLARGE;
	}
}
