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

/**
 * This class represents a Newbie Helper Buff
 *
 * Author: Ayor
 */

public class L2HelperBuff
{
	/** Min level that the player must achieve to obtain this buff from Newbie Helper */
	private int _lowerLevel;
	
	/** Max level that the player mustn't exceed if it want to obtain this buff from Newbie Helper */
	private int _upperLevel;
	
	/** Identifier of the skill (buff) that the Newbie Helper must cast */
	private int _skillID;
	
	/** Level of the skill (buff) that the Newbie Helper must cast */
	private int _skillLevel;
	
	/** If True only Magus class will obtain this Buff <BR>
	 *  If False only Fighter class will obtain this Buff */
	private boolean _isMagicClass;
	
	private boolean _forSummon = false;
	/**
	 * Constructor of L2HelperBuff.<BR><BR>
	 * @param set 
	 */
	public L2HelperBuff(StatsSet set)
	{
		
		_lowerLevel = set.getInteger("lowerLevel");
		_upperLevel = set.getInteger("upperLevel");
		_skillID = set.getInteger("skillID");
		_skillLevel = set.getInteger("skillLevel");
		if ("true".equals(set.getString("forSummon")))
			_forSummon = true;
		
		if ("false".equals(set.getString("isMagicClass")))
			_isMagicClass = false;
		else
			_isMagicClass = true;
		
	}
	
	/**
	 * Returns the lower level that the L2PcInstance must achieve in order to obtain this buff
	 * @return int
	 */
	public int getLowerLevel()
	{
		return _lowerLevel;
	}
	
	/**
	 * Returns the upper level that the L2PcInstance mustn't exceed in order to obtain this buff
	 * @return int
	 */
	public int getUpperLevel()
	{
		return _upperLevel;
	}
	
	/**
	 * Returns the ID of the buff that the L2PcInstance will receive
	 * @return int
	 */
	public int getSkillID()
	{
		return _skillID;
	}
	
	/**
	 * Returns the Level of the buff that the L2PcInstance will receive
	 * @return int
	 */
	public int getSkillLevel()
	{
		return _skillLevel;
	}
	
	/**
	 * Returns if this Buff can be cast on a fighter or a mystic
	 * @return boolean : False if it's a fighter class Buff
	 */
	public boolean isMagicClassBuff()
	{
		return _isMagicClass;
	}
	
	public boolean isForSummon()
	{
		return _forSummon;
	}
}