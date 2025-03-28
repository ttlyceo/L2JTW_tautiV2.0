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
package com.l2jserver.gameserver.model.zone.type;

import java.util.concurrent.Future;

import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.model.L2WorldRegion;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.skills.L2Skill;
import com.l2jserver.gameserver.model.zone.L2ZoneType;

/**
 * A dynamic zone? Maybe use this for interlude skills like protection field :>
 * @author durgus
 */
public class L2DynamicZone extends L2ZoneType
{
	private L2WorldRegion _region;
	private L2Character _owner;
	private Future<?> _task;
	private L2Skill _skill;
	
	protected void setTask(Future<?> task)
	{
		_task = task;
	}
	
	public L2DynamicZone(L2WorldRegion region, L2Character owner, L2Skill skill)
	{
		super(-1);
		_region = region;
		_owner = owner;
		_skill = skill;
		
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				remove();
			}
		};
		setTask(ThreadPoolManager.getInstance().scheduleGeneral(r, skill.getBuffDuration()));
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (character.isPlayer())
		{
			/* Move To MessageTable For L2JTW
			character.sendMessage("You have entered a temporary zone!");
			*/
			character.sendMessage(504);
		}
		if (_owner != null)
		{
			_skill.getEffects(_owner, character);
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (character.isPlayer())
		{
			/* Move To MessageTable For L2JTW
			character.sendMessage("You have left a temporary zone!"); // XXX: Custom message?
			*/
			character.sendMessage(505);
		}
		
		if (character == _owner)
		{
			remove();
			return;
		}
		
		character.stopSkillEffects(_skill.getId());
	}
	
	protected void remove()
	{
		if (_task == null || _skill == null)
			return;
		
		_task.cancel(false);
		_task = null;
		
		_region.removeZone(this);
		for (L2Character member : getCharactersInside())
		{
			member.stopSkillEffects(_skill.getId());
		}
		_owner.stopSkillEffects(_skill.getId());
		
	}
	
	@Override
	public void onDieInside(L2Character character)
	{
		if (character == _owner)
		{
			remove();
		}
		else
		{
			character.stopSkillEffects(_skill.getId());
		}
	}
	
	@Override
	public void onReviveInside(L2Character character)
	{
		_skill.getEffects(_owner, character);
	}
}
