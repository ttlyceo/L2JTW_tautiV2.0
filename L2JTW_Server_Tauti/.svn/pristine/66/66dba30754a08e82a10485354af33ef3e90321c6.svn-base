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
import com.l2jserver.gameserver.datatables.SkillTable;
import com.l2jserver.gameserver.model.skills.L2Skill;
import com.l2jserver.gameserver.model.L2SkillLearn; //rocknow-God-Awaking
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance; //rocknow-God-Awaking
import com.l2jserver.gameserver.model.base.ClassId; //rocknow-God-Awaking

/**
 * This class ...
 *
 * @version $Revision: 1.8.2.6.2.18 $ $Date: 2005/04/06 16:13:25 $
 */
public class SkillNameTable
{
	private static final Logger _log = Logger.getLogger(SkillNameTable.class.getName());
	private static SkillNameTable _instance;
	private FastList<Nametable> tables = new FastList<>();
	
	private class Nametable
	{
		public String name;
		public int id;
		public int level;
	}
	
	public static SkillNameTable getInstance()
	{
		if (_instance == null)
			_instance = new SkillNameTable();
		
		return _instance;
	}
	
	private SkillNameTable()
	{
		reload();
	}
	
	@SuppressWarnings("synthetic-access")
	public void reload()
	{
		Connection con = null;
		int count = 0;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[] {"skill_id","name","level"}) + " FROM skill");
			ResultSet skilldata = statement.executeQuery();
			Nametable data;
			while(skilldata.next())
			{
				data = new Nametable();
				data.id = skilldata.getInt("skill_id");
				data.level = skilldata.getInt("level");
				data.name = skilldata.getString("name");
				
				if (tables == null)
					new FastList<Nametable>();
				
				tables.add(data);
				count++;
			}
			skilldata.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("Skill Name Table: FAILED");
			_log.warning(""+e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		
		L2Skill result = null;
		for(Nametable n : tables)
		{
			result = SkillTable.getInstance()._skills.get(SkillTable.getSkillHashCode(n.id, n.level));
			if(result != null)
				SkillTable.getInstance()._skills.get(SkillTable.getSkillHashCode(n.id, n.level)).setName(n.name);
		}
		_log.warning("Skill Name Table: " + count + " Name.");
	}
}