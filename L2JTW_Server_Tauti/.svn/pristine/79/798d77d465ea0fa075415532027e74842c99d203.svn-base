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
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.l2jserver.Config;
import com.l2jserver.gameserver.network.serverpackets.KeyPacket;
import com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jserver.gameserver.datatables.MessageTable; //Update by rocknow

/**
 * This class ...
 *
 * @version $Revision: 1.5.2.8.2.8 $ $Date: 2005/04/02 10:43:04 $
 */
public final class ProtocolVersion extends L2GameClientPacket
{
	private static final String _C__0E_PROTOCOLVERSION = "[C] 0E ProtocolVersion";
	private static final Logger _logAccounting = Logger.getLogger("accounting");
	
	private int _version;
	private int _support = 488; //Update by rocknow
	
	@Override
	protected void readImpl()
	{
		_version = readD();
		readB(new byte[256]); //488
		readD(); //488
	}
	
	@Override
	protected void runImpl()
	{
		// this packet is never encrypted
		if (_version == -2)
		{
			if (Config.DEBUG)
				_log.info("Ping received");
			// this is just a ping attempt from the new C2 client
			getClient().close((L2GameServerPacket)null);
		}
		//Update by rocknow-Start
		/*
		else if (_version < (_support - 50))
		{
			LogRecord record = new LogRecord(Level.WARNING, "Protocol too old");
			record.setParameters(new Object[]{_version, getClient()});
			_logAccounting.log(record);
			_log.info(getClient() + MessageTable.Messages[1].getExtra(1) +
									MessageTable.Messages[1].getExtra(3) +
									MessageTable.Messages[1].getExtra(2));
			KeyPacket pk = new KeyPacket(getClient().enableCrypt(),0);
			getClient().setProtocolOk(false);
			getClient().close(pk);
		}
		else if (_version > (_support + 50))
		{
			LogRecord record = new LogRecord(Level.WARNING, "Protocol too new");
			record.setParameters(new Object[]{_version, getClient()});
			_logAccounting.log(record);
			_log.info(getClient() + MessageTable.Messages[1].getExtra(1) +
									MessageTable.Messages[1].getExtra(4) +
									MessageTable.Messages[1].getExtra(2));
			KeyPacket pk = new KeyPacket(getClient().enableCrypt(),0);
			getClient().setProtocolOk(false);
			getClient().close(pk);
		}
		*/
		else if (_version < _support)
		{
			LogRecord record = new LogRecord(Level.WARNING, "Older protocol");
			record.setParameters(new Object[]{_version, getClient()});
			_logAccounting.log(record);
			_log.fine(getClient() + MessageTable.Messages[2].getExtra(1) +
									MessageTable.Messages[2].getExtra(4) + 
									MessageTable.Messages[2].getExtra(2) + _version + 
									MessageTable.Messages[2].getExtra(3) +
									MessageTable.Messages[2].getExtra(6));
			KeyPacket pk = new KeyPacket(getClient().enableCrypt(),0);
			getClient().setProtocolOk(false);
			getClient().close(pk);
		}
		else if (_version > _support)
		{
			LogRecord record = new LogRecord(Level.WARNING, "Newer protocol");
			record.setParameters(new Object[]{_version, getClient()});
			_logAccounting.log(record);
			_log.fine(getClient() + MessageTable.Messages[2].getExtra(1) +
									MessageTable.Messages[2].getExtra(5) + 
									MessageTable.Messages[2].getExtra(2) + _version + 
									MessageTable.Messages[2].getExtra(3) +
									MessageTable.Messages[2].getExtra(6));
			KeyPacket pk = new KeyPacket(getClient().enableCrypt(),0);
			getClient().setProtocolOk(false);
			getClient().close(pk);
		}
		//Update by rocknow-End
		else if (!Config.PROTOCOL_LIST.contains(_version))
		{
			LogRecord record = new LogRecord(Level.WARNING, "Wrong protocol");
			record.setParameters(new Object[]{_version, getClient()});
			_logAccounting.log(record);
			KeyPacket pk = new KeyPacket(getClient().enableCrypt(),0);
			getClient().setProtocolOk(false);
			getClient().close(pk);
		}
		else
		{
			if (Config.DEBUG)
				_log.fine("Client Protocol Revision is ok: "+_version);
			
			KeyPacket pk = new KeyPacket(getClient().enableCrypt(),1);
			getClient().sendPacket(pk);
			getClient().setProtocolOk(true);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__0E_PROTOCOLVERSION;
	}
}
