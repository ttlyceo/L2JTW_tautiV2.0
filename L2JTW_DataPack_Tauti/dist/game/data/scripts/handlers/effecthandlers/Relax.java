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
package handlers.effecthandlers;

import com.l2jserver.gameserver.ai.CtrlIntention;
import com.l2jserver.gameserver.model.CharEffectList;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.effects.EffectTemplate;
import com.l2jserver.gameserver.model.effects.L2Effect;
import com.l2jserver.gameserver.model.effects.L2EffectType;
import com.l2jserver.gameserver.model.stats.Env;
import com.l2jserver.gameserver.network.SystemMessageId;

public class Relax extends L2Effect
{
	public Relax(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.RELAXING;
	}
	
	@Override
	public boolean onStart()
	{
		if (getEffected() instanceof L2PcInstance)
		{
			((L2PcInstance) getEffected()).sitDown(false);
		}
		else
			getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
		return super.onStart();
	}
	
	@Override
	public void onExit()
	{
		super.onExit();
	}
	
	@Override
	public boolean onActionTime()
	{
		if (getEffected().isDead())
			return false;
		
		if (getEffected() instanceof L2PcInstance)
		{
			if (!((L2PcInstance) getEffected()).isSitting())
				return false;
		}
		
		if (getEffected().getCurrentHp() + 1 > getEffected().getMaxRecoverableHp())
		{
			if (getSkill().isToggle())
			{
				getEffected().sendPacket(SystemMessageId.SKILL_DEACTIVATED_HP_FULL);
				return false;
			}
		}
		
		double manaDam = calc();
		
		if (manaDam > getEffected().getCurrentMp())
		{
			if (getSkill().isToggle())
			{
				getEffected().sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
				return false;
			}
		}
		
		getEffected().reduceCurrentMp(manaDam);
		return true;
	}
	
	@Override
	public int getEffectFlags()
	{
		return CharEffectList.EFFECT_FLAG_RELAXING;
	}
}
