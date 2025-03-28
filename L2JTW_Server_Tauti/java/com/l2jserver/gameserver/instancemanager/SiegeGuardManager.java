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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;

import com.l2jserver.L2DatabaseFactory;
import com.l2jserver.gameserver.datatables.NpcTable;
import com.l2jserver.gameserver.model.L2Spawn;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.templates.L2NpcTemplate;
import com.l2jserver.gameserver.model.entity.Castle;

public class SiegeGuardManager
{
	private static Logger _log = Logger.getLogger(SiegeGuardManager.class.getName());
	
	private Castle _castle;
	private List<L2Spawn> _siegeGuardSpawn = new FastList<>();
	
	public SiegeGuardManager(Castle castle)
	{
		_castle = castle;
	}
	
	/**
	 * Add guard.
	 * @param activeChar 
	 * @param npcId 
	 */
	public void addSiegeGuard(L2PcInstance activeChar, int npcId)
	{
		if (activeChar == null)
			return;
		addSiegeGuard(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getHeading(), npcId);
	}
	
	/**
	 * Add guard.
	 * @param x 
	 * @param y 
	 * @param z 
	 * @param heading 
	 * @param npcId 
	 */
	public void addSiegeGuard(int x, int y, int z, int heading, int npcId)
	{
		saveSiegeGuard(x, y, z, heading, npcId, 0);
	}
	
	/**
	 * Hire merc.
	 * @param activeChar 
	 * @param npcId 
	 */
	public void hireMerc(L2PcInstance activeChar, int npcId)
	{
		if (activeChar == null)
			return;
		hireMerc(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getHeading(), npcId);
	}
	
	/**
	 * Hire merc.
	 * @param x 
	 * @param y 
	 * @param z 
	 * @param heading 
	 * @param npcId 
	 */
	public void hireMerc(int x, int y, int z, int heading, int npcId)
	{
		saveSiegeGuard(x, y, z, heading, npcId, 1);
	}
	
	/**
	 * Remove a single mercenary, identified by the npcId and location.
	 * Presumably, this is used when a castle lord picks up a previously dropped ticket
	 * @param npcId 
	 * @param x 
	 * @param y 
	 * @param z 
	 */
	public void removeMerc(int npcId, int x, int y, int z)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("Delete From castle_siege_guards Where npcId = ? And x = ? AND y = ? AND z = ? AND isHired = 1");
			statement.setInt(1, npcId);
			statement.setInt(2, x);
			statement.setInt(3, y);
			statement.setInt(4, z);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Error deleting hired siege guard at " + x + ',' + y + ',' + z + ": " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	/**
	 * Remove mercs.<BR><BR>
	 */
	public void removeMercs()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("Delete From castle_siege_guards Where castleId = ? And isHired = 1");
			statement.setInt(1, getCastle().getCastleId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Error deleting hired siege guard for castle " + getCastle().getName() + ": " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	/**
	 * Spawn guards.<BR><BR>
	 */
	public void spawnSiegeGuard()
	{
		try
		{
			int hiredCount = 0, hiredMax = MercTicketManager.getInstance().getMaxAllowedMerc(_castle.getCastleId());
			boolean isHired = (getCastle().getOwnerId() > 0) ? true : false;
			loadSiegeGuard();
			for (L2Spawn spawn : getSiegeGuardSpawn())
			{
				if (spawn != null)
				{
					spawn.init();
					if (isHired)
					{
						spawn.stopRespawn();
						if (++hiredCount > hiredMax)
							return;
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Error spawning siege guards for castle " + getCastle().getName(), e);
		}
	}
	
	/**
	 * Unspawn guards.<BR><BR>
	 */
	public void unspawnSiegeGuard()
	{
		for (L2Spawn spawn : getSiegeGuardSpawn())
		{
			if (spawn == null)
				continue;
			
			spawn.stopRespawn();
			spawn.getLastSpawn().doDie(spawn.getLastSpawn());
		}
		
		getSiegeGuardSpawn().clear();
	}
	
	/**
	 * Load guards.<BR><BR>
	 */
	private void loadSiegeGuard()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM castle_siege_guards Where castleId = ? And isHired = ?");
			statement.setInt(1, getCastle().getCastleId());
			if (getCastle().getOwnerId() > 0) // If castle is owned by a clan, then don't spawn default guards
				statement.setInt(2, 1);
			else
				statement.setInt(2, 0);
			ResultSet rs = statement.executeQuery();
			
			L2Spawn spawn1;
			L2NpcTemplate template1;
			
			while (rs.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rs.getInt("npcId"));
				if (template1 != null)
				{
					spawn1 = new L2Spawn(template1);
					spawn1.setAmount(1);
					spawn1.setLocx(rs.getInt("x"));
					spawn1.setLocy(rs.getInt("y"));
					spawn1.setLocz(rs.getInt("z"));
					spawn1.setHeading(rs.getInt("heading"));
					spawn1.setRespawnDelay(rs.getInt("respawnDelay"));
					spawn1.setLocation(0);
					
					_siegeGuardSpawn.add(spawn1);
				}
				else
				{
					_log.warning("Missing npc data in npc table for id: " + rs.getInt("npcId"));
				}
			}
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Error loading siege guard for castle " + getCastle().getName() + ": " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	/**
	 * Save guards.<BR><BR>
	 * @param x 
	 * @param y 
	 * @param z 
	 * @param heading 
	 * @param npcId 
	 * @param isHire 
	 */
	private void saveSiegeGuard(int x, int y, int z, int heading, int npcId, int isHire)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("Insert Into castle_siege_guards (castleId, npcId, x, y, z, heading, respawnDelay, isHired) Values (?, ?, ?, ?, ?, ?, ?, ?)");
			statement.setInt(1, getCastle().getCastleId());
			statement.setInt(2, npcId);
			statement.setInt(3, x);
			statement.setInt(4, y);
			statement.setInt(5, z);
			statement.setInt(6, heading);
			statement.setInt(7, (isHire == 1 ? 0 : 600));
			statement.setInt(8, isHire);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Error adding siege guard for castle " + getCastle().getName() + ": " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	public final Castle getCastle()
	{
		return _castle;
	}
	
	public final List<L2Spawn> getSiegeGuardSpawn()
	{
		return _siegeGuardSpawn;
	}
}
