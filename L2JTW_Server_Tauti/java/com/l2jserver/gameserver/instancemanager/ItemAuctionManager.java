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
package com.l2jserver.gameserver.instancemanager;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jserver.Config;
import com.l2jserver.L2DatabaseFactory;
import com.l2jserver.gameserver.model.itemauction.ItemAuctionInstance;

/**
 * @author Forsaiken
 */
public final class ItemAuctionManager
{
	private static final Logger _log = Logger.getLogger(ItemAuctionManager.class.getName());
	
	public static final ItemAuctionManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private final TIntObjectHashMap<ItemAuctionInstance> _managerInstances;
	private final AtomicInteger _auctionIds;
	
	protected ItemAuctionManager()
	{
		_managerInstances = new TIntObjectHashMap<>();
		_auctionIds = new AtomicInteger(1);
		
		if (!Config.ALT_ITEM_AUCTION_ENABLED)
		{
			_log.log(Level.INFO, "ItemAuctionManager: Disabled by config.");
			return;
		}
		
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT auctionId FROM item_auction ORDER BY auctionId DESC LIMIT 0, 1");
			ResultSet rset = statement.executeQuery();
			
			if (rset.next())
				_auctionIds.set(rset.getInt(1) + 1);
			
			rset.close();
			statement.close();
		}
		catch (final SQLException e)
		{
			_log.log(Level.SEVERE, "ItemAuctionManager: Failed loading auctions.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		
		final File file = new File(Config.DATAPACK_ROOT + "/data/ItemAuctions.xml");
		if (!file.exists())
		{
			_log.log(Level.WARNING, "ItemAuctionManager: Missing ItemAuctions.xml!");
			return;
		}
		
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		
		try
		{
			final Document doc = factory.newDocumentBuilder().parse(file);
			for (Node na = doc.getFirstChild(); na != null; na = na.getNextSibling())
			{
				if ("list".equalsIgnoreCase(na.getNodeName()))
				{
					for (Node nb = na.getFirstChild(); nb != null; nb = nb.getNextSibling())
					{
						if ("instance".equalsIgnoreCase(nb.getNodeName()))
						{
							final NamedNodeMap nab = nb.getAttributes();
							final int instanceId = Integer.parseInt(nab.getNamedItem("id").getNodeValue());
							
							if (_managerInstances.containsKey(instanceId))
								throw new Exception("Dublicated instanceId " + instanceId);
							
							final ItemAuctionInstance instance = new ItemAuctionInstance(instanceId, _auctionIds, nb);
							_managerInstances.put(instanceId, instance);
						}
					}
				}
			}
			_log.log(Level.INFO, "ItemAuctionManager: Loaded " + _managerInstances.size() + " instance(s).");
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "ItemAuctionManager: Failed loading auctions from xml.", e);
		}
	}
	
	public final void shutdown()
	{
		final ItemAuctionInstance[] instances = _managerInstances.values(new ItemAuctionInstance[0]);
		for (final ItemAuctionInstance instance : instances)
		{
			instance.shutdown();
		}
	}
	
	public final ItemAuctionInstance getManagerInstance(final int instanceId)
	{
		return _managerInstances.get(instanceId);
	}
	
	public final int getNextAuctionId()
	{
		return _auctionIds.getAndIncrement();
	}
	
	public static final void deleteAuction(final int auctionId)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM item_auction WHERE auctionId=?");
			statement.setInt(1, auctionId);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM item_auction_bid WHERE auctionId=?");
			statement.setInt(1, auctionId);
			statement.execute();
			statement.close();
		}
		catch (final SQLException e)
		{
			_log.log(Level.SEVERE, "L2ItemAuctionManagerInstance: Failed deleting auction: " + auctionId, e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	private static class SingletonHolder
	{
		protected static final ItemAuctionManager _instance = new ItemAuctionManager();
	}
}