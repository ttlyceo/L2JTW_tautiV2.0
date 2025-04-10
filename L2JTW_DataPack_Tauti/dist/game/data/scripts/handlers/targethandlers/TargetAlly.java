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
package handlers.targethandlers;

import java.util.Collection;
import java.util.List;

import javolution.util.FastList;

import com.l2jserver.gameserver.handler.ITargetTypeHandler;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Playable;
import com.l2jserver.gameserver.model.actor.L2Summon;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.entity.TvTEvent;
import com.l2jserver.gameserver.model.skills.L2Skill;
import com.l2jserver.gameserver.model.skills.targets.L2TargetType;

/**
 * @author UnAfraid
 */
public class TargetAlly implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new FastList<>();
		if (activeChar instanceof L2Playable)
		{
			final L2PcInstance player = activeChar.getActingPlayer();
			
			if (player == null)
				return _emptyTargetList;
			
			if (player.isInOlympiadMode())
				return new L2Character[] { player };
			
			if (onlyFirst)
				return new L2Character[] { player };
			
			targetList.add(player);
			
			final int radius = skill.getSkillRadius();
			
			for(L2Summon s : player.getPets())
			{
				if(L2Skill.addCharacter(activeChar, s, radius, false))
				{
					targetList.add(s);
				}
			}
			
			if (player.getClan() != null)
			{
				// Get all visible objects in a spherical area near the L2Character
				final Collection<L2PcInstance> objs = activeChar.getKnownList().getKnownPlayersInRadius(radius);
				for (L2PcInstance obj : objs)
				{
					if (obj == null)
						continue;
					if ((obj.getAllyId() == 0 || obj.getAllyId() != player.getAllyId()) && (obj.getClan() == null || obj.getClanId() != player.getClanId()))
						continue;
					
					if (player.isInDuel())
					{
						if (player.getDuelId() != obj.getDuelId())
							continue;
						if (player.isInParty() && obj.isInParty() && player.getParty().getLeaderObjectId() != obj.getParty().getLeaderObjectId())
							continue;
					}
					
					// Don't add this target if this is a Pc->Pc pvp
					// casting and pvp condition not met
					if (!player.checkPvpSkill(obj, skill))
						continue;
					
					if (!TvTEvent.checkForTvTSkill(player, obj, skill))
						continue;

					for(L2Summon s : obj.getPets())
					{
						if(!onlyFirst && L2Skill.addCharacter(activeChar, s, radius, false))
						{
							targetList.add(s);
						}
					}
					
					if (!L2Skill.addCharacter(activeChar, obj, radius, false))
						continue;
					
					if (onlyFirst)
						return new L2Character[] { obj };
					
					if (skill.getMaxTargets() > -1 && targetList.size() >= skill.getMaxTargets())
						break;
					
					targetList.add(obj);
				}
			}
		}
		
		return targetList.toArray(new L2Character[targetList.size()]);
	}
	
	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.TARGET_ALLY;
	}
}
