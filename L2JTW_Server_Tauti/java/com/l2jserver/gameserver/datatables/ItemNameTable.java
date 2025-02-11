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
package com.l2jserver.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

import javolution.util.FastList;

import com.l2jserver.L2DatabaseFactory;
import com.l2jserver.gameserver.datatables.ItemTable;
import com.l2jserver.gameserver.model.items.L2Item;

/**
 * 
 */
public class ItemNameTable
{
	private static final Logger _log = Logger.getLogger(ItemNameTable.class.getName());
	private static ItemNameTable _instance = new ItemNameTable();
	
	public static ItemNameTable getInstance()
	{
		return _instance;
	}
	
	private ItemNameTable()
	{
		reload();
	}
	
	@SuppressWarnings("synthetic-access")
	public void reload()
	{
		class Nametable
		{
			public int id;
			public String name;
		}
		FastList<Nametable> tables = new FastList<>();
		Connection con = null;
		int count = 0;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[] {"item_id","name"}) + " FROM Item");
			ResultSet itemdata = statement.executeQuery();
			Nametable data;
			while(itemdata.next())
			{
				data = new Nametable();
				data.id = itemdata.getInt("item_id");
				data.name = itemdata.getString("name");
				
				tables.add(data);
				count++;
			}
			itemdata.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("Item Name Table: FAILED");
			_log.warning("" + e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		
		L2Item result = null;
		for(Nametable n : tables)
		{
			result = ItemTable.getInstance().getTemplate(n.id);
			if(result != null)
				ItemTable.getInstance().getTemplate(n.id).setName(n.name);
		}
		
		_log.warning("Item Name Table: " + count + " Name.");
	}
}
