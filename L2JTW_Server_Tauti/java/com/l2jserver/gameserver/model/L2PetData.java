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
package com.l2jserver.gameserver.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.l2jserver.gameserver.datatables.SkillTable;
import com.l2jserver.gameserver.model.holders.SkillHolder;

/**
 * Class hold information about basic pet stats which are same on each level.
 * @author JIV
 */
public class L2PetData
{
	private final Map<Integer, L2PetLevelData> _levelStats = new HashMap<>();
	private final List<L2PetSkillLearn> _skills = new ArrayList<>();
	
	private int _load = 20000;
	private int _hungryLimit = 1;
	private int _minlvl = Byte.MAX_VALUE;
	private final List<Integer> _food = new ArrayList<>();
	
	/**
	 * @param level the pet's level.
	 * @param data the pet's data.
	 */
	public void addNewStat(int level, L2PetLevelData data)
	{
		if (_minlvl > level)
		{
			_minlvl = level;
		}
		_levelStats.put(level, data);
	}
	
	/**
	 * @param petLevel the pet's level.
	 * @return the pet data associated to that pet level.
	 */
	public L2PetLevelData getPetLevelData(int petLevel)
	{
		return _levelStats.get(petLevel);
	}
	
	/**
	 * @return the pet's weight load.
	 */
	public int getLoad()
	{
		return _load;
	}
	
	/**
	 * @return the pet's hunger limit.
	 */
	public int getHungryLimit()
	{
		return _hungryLimit;
	}
	
	/**
	 * @return the pet's minimum level.
	 */
	public int getMinLevel()
	{
		return _minlvl;
	}
	
	/**
	 * @return the pet's food list.
	 */
	public List<Integer> getFood()
	{
		return _food;
	}
	
	/**
	 * @param foodId the pet's food Id to add.
	 */
	public void addFood(Integer foodId)
	{
		_food.add(foodId);
	}
	
	/**
	 * @param load the weight load to set.
	 */
	public void setLoad(int load)
	{
		_load = load;
	}
	
	/**
	 * @param limit the hunger limit to set.
	 */
	public void setHungryLimit(int limit)
	{
		_hungryLimit = limit;
	}
	
	// SKILS
	
	/**
	 * @param skillId the skill Id to add.
	 * @param skillLvl the skill level.
	 * @param petLvl the pet's level when this skill is available.
	 */
	public void addNewSkill(int skillId, int skillLvl, int petLvl)
	{
		_skills.add(new L2PetSkillLearn(skillId, skillLvl, petLvl));
	}
	
	/**
	 * TODO: Simplify this.
	 * @param skillId the skill Id.
	 * @param petLvl the pet level.
	 * @return the level of the skill for the given skill Id and pet level.
	 */
	public int getAvailableLevel(int skillId, int petLvl)
	{
		int lvl = 0;
		for (L2PetSkillLearn temp : _skills)
		{
			if (temp.getSkillId() != skillId)
			{
				continue;
			}
			if (temp.getSkillLvl() == 0)
			{
				if (petLvl < 70)
				{
					lvl = (petLvl / 10);
					if (lvl <= 0)
					{
						lvl = 1;
					}
				}
				else
				{
					lvl = (7 + ((petLvl - 70) / 5));
				}
				
				// formula usable for skill that have 10 or more skill levels
				int maxLvl = SkillTable.getInstance().getMaxLevel(temp.getSkillId());
				if (lvl > maxLvl)
				{
					lvl = maxLvl;
				}
				break;
			}
			else if (temp.getMinLevel() <= petLvl)
			{
				if (temp.getSkillLvl() > lvl)
				{
					lvl = temp.getSkillLvl();
				}
			}
		}
		return lvl;
	}
	
	/**
	 * @return the list with the pet's skill data.
	 */
	public List<L2PetSkillLearn> getAvailableSkills()
	{
		return _skills;
	}
	
	public static final class L2PetSkillLearn extends SkillHolder
	{
		private final int _minLevel;
		
		/**
		 * @param id the skill Id.
		 * @param lvl the skill level.
		 * @param minLvl the minimum level when this skill is available.
		 */
		public L2PetSkillLearn(int id, int lvl, int minLvl)
		{
			super(id, lvl);
			_minLevel = minLvl;
		}
		
		/**
		 * @return the minimum level for the pet to get the skill.
		 */
		public int getMinLevel()
		{
			return _minLevel;
		}
	}
}
