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
package transformations;

import com.l2jserver.gameserver.datatables.SkillTable;
import com.l2jserver.gameserver.instancemanager.TransformationManager;
import com.l2jserver.gameserver.model.L2Transformation;

public class SnowKung extends L2Transformation
{
	private static final int[] SKILLS =
	{
		940, 943, 5437, 619
	};
	
	public SnowKung()
	{
		// id, colRadius, colHeight
		super(114, 28, 30);
	}
	
	@Override
	public void onTransform()
	{
		if ((getPlayer().getTransformationId() != 114) || getPlayer().isCursedWeaponEquipped())
		{
			return;
		}
		
		transformedSkills();
	}
	
	public void transformedSkills()
	{
		// Fake Attack
		getPlayer().addSkill(SkillTable.getInstance().getInfo(940, 1), false);
		// Special Motion
		getPlayer().addSkill(SkillTable.getInstance().getInfo(943, 1), false);
		// Dissonance
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5437, 2), false);
		// Transform Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);
		
		getPlayer().setTransformAllowedSkills(SKILLS);
	}
	
	@Override
	public void onUntransform()
	{
		removeSkills();
	}
	
	public void removeSkills()
	{
		// Fake Attack
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(940, 1), false);
		// Special Motion
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(943, 1), false);
		// Dissonance
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5437, 2), false);
		// Transform Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
	
	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new SnowKung());
	}
}
