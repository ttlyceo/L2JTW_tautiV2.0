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

import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jserver.Config;
import com.l2jserver.gameserver.engines.DocumentParser;
import com.l2jserver.gameserver.model.L2Clan;
import com.l2jserver.gameserver.model.L2SkillLearn;
import com.l2jserver.gameserver.model.L2SkillLearn.SubClassData;
import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.base.AcquireSkillType;
import com.l2jserver.gameserver.model.base.ClassId;
import com.l2jserver.gameserver.model.base.Race;
import com.l2jserver.gameserver.model.base.SocialClass;
import com.l2jserver.gameserver.model.base.SubClass;
import com.l2jserver.gameserver.model.holders.ItemHolder;
import com.l2jserver.gameserver.model.holders.SkillHolder;
import com.l2jserver.gameserver.model.skills.L2Skill;

/**
 * This class loads and manage the characters and pledges skills trees.<br>
 * Here can be found the following skill trees:<br>
 * <ul>
 * <li>Class skill trees: player skill trees for each class.</li>
 * <li>Transfer skill trees: player skill trees for each healer class.</lI>
 * <li>Collect skill tree: player skill tree for Gracia related skills.</li>
 * <li>Fishing skill tree: player skill tree for fishing related skills.</li>
 * <li>Transform skill tree: player skill tree for transformation related skills.</li>
 * <li>Sub-Class skill tree: player skill tree for sub-class related skills.</li>
 * <li>Noble skill tree: player skill tree for noblesse related skills.</li>
 * <li>Hero skill tree: player skill tree for heroes related skills.</li>
 * <li>GM skill tree: player skill tree for Game Master related skills.</li>
 * <li>Common skill tree: custom skill tree for players, skills in this skill tree will be available for all players.</li>
 * <li>Pledge skill tree: clan skill tree for main clan.</li>
 * <li>Sub-Pledge skill tree: clan skill tree for sub-clans.</li>
 * </ul>
 * For easy customization of player class skill trees, the parent Id of each class is taken from the XML data, this means you can use a different class parent Id than in the normal game play, for example all 3rd class dagger users will have Treasure Hunter skills as 1st and 2nd class skills.<br>
 * For XML schema please refer to skillTrees.xsd in datapack in xsd folder and for parameters documentation refer to documentation.txt in skillTrees folder.<br>
 * @author Zoey76
 */
public final class SkillTreesData extends DocumentParser
{
	// ClassId, FastMap of Skill Hash Code, L2SkillLearn
	private static final Map<ClassId, Map<Integer, L2SkillLearn>> _classSkillTrees = new HashMap<>();
	private static final Map<ClassId, Map<Integer, L2SkillLearn>> _transferSkillTrees = new HashMap<>();
	// Skill Hash Code, L2SkillLearn
	private static final Map<Integer, L2SkillLearn> _collectSkillTree = new HashMap<>();
	private static final Map<Integer, L2SkillLearn> _fishingSkillTree = new HashMap<>();
	private static final Map<Integer, L2SkillLearn> _pledgeSkillTree = new HashMap<>();
	private static final Map<Integer, L2SkillLearn> _subClassSkillTree = new HashMap<>();
	private static final Map<Integer, L2SkillLearn> _subPledgeSkillTree = new HashMap<>();
	private static final Map<Integer, L2SkillLearn> _transformSkillTree = new HashMap<>();
	private static final Map<Integer, L2SkillLearn> _commonSkillTree = new HashMap<>();
	// Other skill trees
	private static final Map<Integer, L2SkillLearn> _nobleSkillTree = new HashMap<>();
	private static final Map<Integer, L2SkillLearn> _heroSkillTree = new HashMap<>();
	private static final Map<Integer, L2SkillLearn> _gameMasterSkillTree = new HashMap<>();
	private static final Map<Integer, L2SkillLearn> _gameMasterAuraSkillTree = new HashMap<>();
	
	// Checker, sorted arrays of hash codes
	private TIntObjectHashMap<int[]> _skillsByClassIdHashCodes; // Occupation skills
	private TIntObjectHashMap<int[]> _skillsByRaceHashCodes; // Race-specific Transformations
	private int[] _allSkillsHashCodes; // Fishing, Collection, Transformations, Common Skills.
	
	private boolean _loading = true;
	
	/**
	 * Parent class IDs are read from XML and stored in this map, to allow easy customization.
	 */
	private static final Map<ClassId, ClassId> _parentClassMap = new HashMap<>();
	
	/**
	 * Instantiates a new skill trees data.
	 */
	protected SkillTreesData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_loading = true;
		_classSkillTrees.clear();
		_collectSkillTree.clear();
		_fishingSkillTree.clear();
		_pledgeSkillTree.clear();
		_subClassSkillTree.clear();
		_subPledgeSkillTree.clear();
		_transferSkillTrees.clear();
		_transformSkillTree.clear();
		_nobleSkillTree.clear();
		_heroSkillTree.clear();
		_gameMasterSkillTree.clear();
		_gameMasterAuraSkillTree.clear();
		
		// Load files.
		parseDirectory(new File(Config.DATAPACK_ROOT, "data/skillTrees/"));
		
		// Generate check arrays.
		generateCheckArrays();
		
		_loading = false;
		
		// Logs a report with skill trees info.
		report();
	}
	
	/**
	 * Parse a skill tree file and store it into the correct skill tree.
	 */
	@Override
	protected void parseDocument()
	{
		NamedNodeMap attrs;
		Node attr;
		String type = null;
		int cId = -1;
		int parentClassId = -1;
		ClassId classId = null;
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("skillTree".equalsIgnoreCase(d.getNodeName()))
					{
						final Map<Integer, L2SkillLearn> classSkillTree = new HashMap<>();
						final Map<Integer, L2SkillLearn> trasferSkillTree = new HashMap<>();
						
						type = d.getAttributes().getNamedItem("type").getNodeValue();
						attr = d.getAttributes().getNamedItem("classId");
						if (attr != null)
						{
							cId = Integer.parseInt(attr.getNodeValue());
							classId = ClassId.values()[cId];
						}
						else
						{
							cId = -1;
						}
						
						attr = d.getAttributes().getNamedItem("parentClassId");
						if (attr != null)
						{
							parentClassId = Integer.parseInt(attr.getNodeValue());
							if ((cId > -1) && (cId != parentClassId) && (parentClassId > -1) && !_parentClassMap.containsKey(classId))
							{
								_parentClassMap.put(classId, ClassId.values()[parentClassId]);
							}
						}
						
						for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
						{
							if ("skill".equalsIgnoreCase(c.getNodeName()))
							{
								final StatsSet learnSkillSet = new StatsSet();
								attrs = c.getAttributes();
								for (int i = 0; i < attrs.getLength(); i++)
								{
									attr = attrs.item(i);
									learnSkillSet.set(attr.getNodeName(), attr.getNodeValue());
								}
								
								final L2SkillLearn skillLearn = new L2SkillLearn(learnSkillSet);
								for (Node b = c.getFirstChild(); b != null; b = b.getNextSibling())
								{
									attrs = b.getAttributes();
									switch (b.getNodeName())
									{
										case "item":
											skillLearn.addRequiredItem(new ItemHolder(parseInt(attrs, "id"), parseInt(attrs, "count")));
											break;
										case "preRequisiteSkill":
											skillLearn.addPreReqSkill(new SkillHolder(parseInt(attrs, "id"), parseInt(attrs, "lvl")));
											break;
										case "race":
											skillLearn.addRace(Race.valueOf(b.getTextContent()));
											break;
										case "residenceId":
											skillLearn.addResidenceId(Integer.valueOf(b.getTextContent()));
											break;
										case "socialClass":
											skillLearn.setSocialClass(Enum.valueOf(SocialClass.class, b.getTextContent()));
											break;
										case "subClassConditions":
											skillLearn.addSubclassConditions(parseInt(attrs, "slot"), parseInt(attrs, "lvl"));
											break;
									}
								}
								
								final int skillHashCode = SkillTable.getSkillHashCode(skillLearn.getSkillId(), skillLearn.getSkillLevel());
								switch (type)
								{
									case "classSkillTree":
									{
										if (cId != -1)
										{
											classSkillTree.put(skillHashCode, skillLearn);
										}
										else
										{
											_commonSkillTree.put(skillHashCode, skillLearn);
										}
										break;
									}
									case "transferSkillTree":
									{
										trasferSkillTree.put(skillHashCode, skillLearn);
										break;
									}
									case "collectSkillTree":
									{
										_collectSkillTree.put(skillHashCode, skillLearn);
										break;
									}
									case "fishingSkillTree":
									{
										_fishingSkillTree.put(skillHashCode, skillLearn);
										break;
									}
									case "pledgeSkillTree":
									{
										_pledgeSkillTree.put(skillHashCode, skillLearn);
										break;
									}
									case "subClassSkillTree":
									{
										_subClassSkillTree.put(skillHashCode, skillLearn);
										break;
									}
									case "subPledgeSkillTree":
									{
										_subPledgeSkillTree.put(skillHashCode, skillLearn);
										break;
									}
									case "transformSkillTree":
									{
										_transformSkillTree.put(skillHashCode, skillLearn);
										break;
									}
									case "nobleSkillTree":
									{
										_nobleSkillTree.put(skillHashCode, skillLearn);
										break;
									}
									case "heroSkillTree":
									{
										_heroSkillTree.put(skillHashCode, skillLearn);
										break;
									}
									case "gameMasterSkillTree":
									{
										_gameMasterSkillTree.put(skillHashCode, skillLearn);
										break;
									}
									case "gameMasterAuraSkillTree":
									{
										_gameMasterAuraSkillTree.put(skillHashCode, skillLearn);
										break;
									}
									default:
									{
										_log.warning(getClass().getSimpleName() + ": Unknown Skill Tree type: " + type + "!");
									}
								}
							}
						}
						
						if (type.equals("transferSkillTree"))
						{
							_transferSkillTrees.put(classId, trasferSkillTree);
						}
						else if (type.equals("classSkillTree") && (cId > -1))
						{
							if (!_classSkillTrees.containsKey(classId))
							{
								_classSkillTrees.put(classId, classSkillTree);
							}
							else
							{
								_classSkillTrees.get(classId).putAll(classSkillTree);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Method to get the complete skill tree for a given class id.<br>
	 * Include all skills common to all classes.<br>
	 * Includes all parent skill trees.
	 * @param classId the class skill tree ID.
	 * @return the complete Class Skill Tree including skill trees from parent class for a given {@code classId}.
	 */
	public Map<Integer, L2SkillLearn> getCompleteClassSkillTree(ClassId classId)
	{
		final Map<Integer, L2SkillLearn> skillTree = new HashMap<>();
		// Add all skills that belong to all classes.
		skillTree.putAll(_commonSkillTree);
		while ((classId != null) && (_classSkillTrees.get(classId) != null))
		{
			skillTree.putAll(_classSkillTrees.get(classId));
			classId = _parentClassMap.get(classId);
		}
		return skillTree;
	}
	
	/**
	 * Gets the transfer skill tree.<br>
	 * If new classes are implemented over 3rd class, we use a recursive call.
	 * @param classId the transfer skill tree ID.
	 * @return the complete Transfer Skill Tree for a given {@code classId}.
	 */
	public Map<Integer, L2SkillLearn> getTransferSkillTree(ClassId classId)
	{
		if (classId.level() >= 3)
		{
			return getTransferSkillTree(classId.getParent());
		}
		return _transferSkillTrees.get(classId);
	}
	
	/**
	 * Gets the common skill tree.
	 * @return the complete Common Skill Tree.
	 */
	public Map<Integer, L2SkillLearn> getCommonSkillTree()
	{
		return _commonSkillTree;
	}
	
	/**
	 * Gets the collect skill tree.
	 * @return the complete Collect Skill Tree.
	 */
	public Map<Integer, L2SkillLearn> getCollectSkillTree()
	{
		return _collectSkillTree;
	}
	
	/**
	 * Gets the fishing skill tree.
	 * @return the complete Fishing Skill Tree.
	 */
	public Map<Integer, L2SkillLearn> getFishingSkillTree()
	{
		return _fishingSkillTree;
	}
	
	/**
	 * Gets the pledge skill tree.
	 * @return the complete Pledge Skill Tree.
	 */
	public Map<Integer, L2SkillLearn> getPledgeSkillTree()
	{
		return _pledgeSkillTree;
	}
	
	/**
	 * Gets the sub class skill tree.
	 * @return the complete Sub-Class Skill Tree.
	 */
	public Map<Integer, L2SkillLearn> getSubClassSkillTree()
	{
		return _subClassSkillTree;
	}
	
	/**
	 * Gets the sub pledge skill tree.
	 * @return the complete Sub-Pledge Skill Tree.
	 */
	public Map<Integer, L2SkillLearn> getSubPledgeSkillTree()
	{
		return _subPledgeSkillTree;
	}
	
	/**
	 * Gets the transform skill tree.
	 * @return the complete Transform Skill Tree.
	 */
	public Map<Integer, L2SkillLearn> getTransformSkillTree()
	{
		return _transformSkillTree;
	}
	
	/**
	 * Gets the noble skill tree.
	 * @return the complete Noble Skill Tree.
	 */
	public Map<Integer, L2Skill> getNobleSkillTree()
	{
		final Map<Integer, L2Skill> tree = new HashMap<>();
		final SkillTable st = SkillTable.getInstance();
		for (Entry<Integer, L2SkillLearn> e : _nobleSkillTree.entrySet())
		{
			tree.put(e.getKey(), st.getInfo(e.getValue().getSkillId(), e.getValue().getSkillLevel()));
		}
		return tree;
	}
	
	/**
	 * Gets the hero skill tree.
	 * @return the complete Hero Skill Tree.
	 */
	public Map<Integer, L2Skill> getHeroSkillTree()
	{
		final Map<Integer, L2Skill> tree = new HashMap<>();
		final SkillTable st = SkillTable.getInstance();
		for (Entry<Integer, L2SkillLearn> e : _heroSkillTree.entrySet())
		{
			tree.put(e.getKey(), st.getInfo(e.getValue().getSkillId(), e.getValue().getSkillLevel()));
		}
		return tree;
	}
	
	/**
	 * Gets the gM skill tree.
	 * @return the complete Game Master Skill Tree.
	 */
	public Map<Integer, L2Skill> getGMSkillTree()
	{
		final Map<Integer, L2Skill> tree = new HashMap<>();
		final SkillTable st = SkillTable.getInstance();
		for (Entry<Integer, L2SkillLearn> e : _gameMasterSkillTree.entrySet())
		{
			tree.put(e.getKey(), st.getInfo(e.getValue().getSkillId(), e.getValue().getSkillLevel()));
		}
		return tree;
	}
	
	/**
	 * Gets the gM aura skill tree.
	 * @return the complete Game Master Aura Skill Tree.
	 */
	public Map<Integer, L2Skill> getGMAuraSkillTree()
	{
		final Map<Integer, L2Skill> tree = new HashMap<>();
		final SkillTable st = SkillTable.getInstance();
		for (Entry<Integer, L2SkillLearn> e : _gameMasterAuraSkillTree.entrySet())
		{
			tree.put(e.getKey(), st.getInfo(e.getValue().getSkillId(), e.getValue().getSkillLevel()));
		}
		return tree;
	}
	
	/**
	 * Gets the available skills.
	 * @param player the learning skill player.
	 * @param classId the learning skill class ID.
	 * @param includeByFs if {@code true} skills from Forgotten Scroll will be included.
	 * @param includeAutoGet if {@code true} Auto-Get skills will be included.
	 * @return all available skills for a given {@code player}, {@code classId}, {@code includeByFs} and {@code includeAutoGet}.
	 */
	public List<L2SkillLearn> getAvailableSkills(L2PcInstance player, ClassId classId, boolean includeByFs, boolean includeAutoGet)
	{
		final List<L2SkillLearn> result = new ArrayList<>();
		final Map<Integer, L2SkillLearn> skills = getCompleteClassSkillTree(classId);
		if (skills.isEmpty())
		{
			// The Skill Tree for this class is undefined.
			_log.warning(getClass().getSimpleName() + ": Skilltree for class " + classId + " is not defined!");
			return result;
		}
		
		for (L2SkillLearn skill : skills.values())
		{
			if (((includeAutoGet && skill.isAutoGet()) || skill.isLearnedByNpc() || (includeByFs && skill.isLearnedByFS())) && (player.getLevel() >= skill.getGetLevel()))
			{
				final L2Skill oldSkill = player.getSkills().get(skill.getSkillId());
				if (oldSkill != null)
				{
					if (oldSkill.getLevel() == (skill.getSkillLevel() - 1))
					{
						result.add(skill);
					}
				}
				else if (skill.getSkillLevel() == 1)
				{
					result.add(skill);
				}
			}
		}
		return result;
	}
	
	/**
	 * Gets the available auto get skills.
	 * @param player the player requesting the Auto-Get skills.
	 * @return all the available Auto-Get skills for a given {@code player}.
	 */
	public List<L2SkillLearn> getAvailableAutoGetSkills(L2PcInstance player)
	{
		final List<L2SkillLearn> result = new ArrayList<>();
		final Map<Integer, L2SkillLearn> skills = getCompleteClassSkillTree(player.getClassId());
		if (skills.isEmpty())
		{
			// The Skill Tree for this class is undefined, so we return an empty list.
			_log.warning(getClass().getSimpleName() + ": Skill Tree for this class Id(" + player.getClassId() + ") is not defined!");
			return result;
		}
		
		final Race race = player.getRace();
		for (L2SkillLearn skill : skills.values())
		{
			if (!skill.getRaces().isEmpty() && !skill.getRaces().contains(race))
			{
				continue;
			}
			
			if (skill.isAutoGet() && (player.getLevel() >= skill.getGetLevel()))
			{
				final L2Skill oldSkill = player.getSkills().get(skill.getSkillId());
				if (oldSkill != null)
				{
					if (oldSkill.getLevel() < skill.getSkillLevel())
					{
						result.add(skill);
					}
				}
				else
				{
					result.add(skill);
				}
			}
		}
		return result;
	}
	
	/**
	 * Dwarvens will get additional dwarven only fishing skills.
	 * @param player the player
	 * @return all the available Fishing skills for a given {@code player}.
	 */
	public List<L2SkillLearn> getAvailableFishingSkills(L2PcInstance player)
	{
		final List<L2SkillLearn> result = new ArrayList<>();
		final Race playerRace = player.getRace();
		for (L2SkillLearn skill : _fishingSkillTree.values())
		{
			// If skill is Race specific and the player's race isn't allowed, skip it.
			if (!skill.getRaces().isEmpty() && !skill.getRaces().contains(playerRace))
			{
				continue;
			}
			
			if (skill.isLearnedByNpc() && (player.getLevel() >= skill.getGetLevel()))
			{
				final L2Skill oldSkill = player.getSkills().get(skill.getSkillId());
				if (oldSkill != null)
				{
					if (oldSkill.getLevel() == (skill.getSkillLevel() - 1))
					{
						result.add(skill);
					}
				}
				else if (skill.getSkillLevel() == 1)
				{
					result.add(skill);
				}
			}
		}
		return result;
	}
	
	/**
	 * Used in Gracia continent.
	 * @param player the collecting skill learning player.
	 * @return all the available Collecting skills for a given {@code player}.
	 */
	public List<L2SkillLearn> getAvailableCollectSkills(L2PcInstance player)
	{
		final List<L2SkillLearn> result = new ArrayList<>();
		for (L2SkillLearn skill : _collectSkillTree.values())
		{
			final L2Skill oldSkill = player.getSkills().get(skill.getSkillId());
			if (oldSkill != null)
			{
				if (oldSkill.getLevel() == (skill.getSkillLevel() - 1))
				{
					result.add(skill);
				}
			}
			else if (skill.getSkillLevel() == 1)
			{
				result.add(skill);
			}
		}
		return result;
	}
	
	/**
	 * Gets the available transfer skills.
	 * @param player the transfer skill learning player.
	 * @return all the available Transfer skills for a given {@code player}.
	 */
	public List<L2SkillLearn> getAvailableTransferSkills(L2PcInstance player)
	{
		final List<L2SkillLearn> result = new ArrayList<>();
		ClassId classId = player.getClassId();
		// If new classes are implemented over 3rd class, a different way should be implemented.
		if (classId.level() == 3)
		{
			classId = classId.getParent();
		}
		
		if (!_transferSkillTrees.containsKey(classId))
		{
			return result;
		}
		
		for (L2SkillLearn skill : _transferSkillTrees.get(classId).values())
		{
			// If player doesn't know this transfer skill:
			if (player.getKnownSkill(skill.getSkillId()) == null)
			{
				result.add(skill);
			}
		}
		return result;
	}
	
	/**
	 * Some transformations are not available for some races.
	 * @param player the transformation skill learning player.
	 * @return all the available Transformation skills for a given {@code player}.
	 */
	public List<L2SkillLearn> getAvailableTransformSkills(L2PcInstance player)
	{
		final List<L2SkillLearn> result = new ArrayList<>();
		final Race race = player.getRace();
		for (L2SkillLearn skill : _transformSkillTree.values())
		{
			if ((player.getLevel() >= skill.getGetLevel()) && (skill.getRaces().isEmpty() || skill.getRaces().contains(race)))
			{
				final L2Skill oldSkill = player.getSkills().get(skill.getSkillId());
				if (oldSkill != null)
				{
					if (oldSkill.getLevel() == (skill.getSkillLevel() - 1))
					{
						result.add(skill);
					}
				}
				else if (skill.getSkillLevel() == 1)
				{
					result.add(skill);
				}
			}
		}
		return result;
	}
	
	/**
	 * Gets the available pledge skills.
	 * @param clan the pledge skill learning clan.
	 * @return all the available Pledge skills for a given {@code clan}.
	 */
	public List<L2SkillLearn> getAvailablePledgeSkills(L2Clan clan)
	{
		final List<L2SkillLearn> result = new ArrayList<>();
		for (L2SkillLearn skill : _pledgeSkillTree.values())
		{
			if (!skill.isResidencialSkill() && (clan.getLevel() >= skill.getGetLevel()))
			{
				final L2Skill oldSkill = clan.getSkills().get(skill.getSkillId());
				if (oldSkill != null)
				{
					if (oldSkill.getLevel() == (skill.getSkillLevel() - 1))
					{
						result.add(skill);
					}
				}
				else if (skill.getSkillLevel() == 1)
				{
					result.add(skill);
				}
			}
		}
		return result;
	}
	
	/**
	 * Gets the available sub pledge skills.
	 * @param clan the sub-pledge skill learning clan.
	 * @return all the available Sub-Pledge skills for a given {@code clan}.
	 */
	public List<L2SkillLearn> getAvailableSubPledgeSkills(L2Clan clan)
	{
		final List<L2SkillLearn> result = new ArrayList<>();
		for (L2SkillLearn skill : _subPledgeSkillTree.values())
		{
			if ((clan.getLevel() >= skill.getGetLevel()) && clan.isLearnableSubSkill(skill.getSkillId(), skill.getSkillLevel()))
			{
				result.add(skill);
			}
		}
		return result;
	}
	
	/**
	 * Gets the available sub class skills.
	 * @param player the sub-class skill learning player.
	 * @return all the available Sub-Class skills for a given {@code player}.
	 */
	public List<L2SkillLearn> getAvailableSubClassSkills(L2PcInstance player)
	{
		final List<L2SkillLearn> result = new ArrayList<>();
		for (L2SkillLearn skill : _subClassSkillTree.values())
		{
			if (player.getLevel() >= skill.getGetLevel())
			{
				List<SubClassData> subClassConds = null;
				for (SubClass subClass : player.getSubClasses().values())
				{
					subClassConds = skill.getSubClassConditions();
					if (!subClassConds.isEmpty() && (subClass.getClassIndex() <= subClassConds.size()) && (subClass.getClassIndex() == subClassConds.get(subClass.getClassIndex() - 1).getSlot()) && (subClassConds.get(subClass.getClassIndex() - 1).getLvl() <= subClass.getLevel()))
					{
						final L2Skill oldSkill = player.getSkills().get(skill.getSkillId());
						if (oldSkill != null)
						{
							if (oldSkill.getLevel() == (skill.getSkillLevel() - 1))
							{
								result.add(skill);
							}
						}
						else if (skill.getSkillLevel() == 1)
						{
							result.add(skill);
						}
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * Gets the available residential skills.
	 * @param residenceId the id of the Castle, Fort, Territory.
	 * @return all the available Residential skills for a given {@code residenceId}.
	 */
	public List<L2SkillLearn> getAvailableResidentialSkills(int residenceId)
	{
		final List<L2SkillLearn> result = new ArrayList<>();
		for (L2SkillLearn skill : _pledgeSkillTree.values())
		{
			if (skill.isResidencialSkill() && skill.getResidenceIds().contains(residenceId))
			{
				result.add(skill);
			}
		}
		return result;
	}
	
	/**
	 * Just a wrapper for all skill trees.
	 * @param skillType the skill type.
	 * @param id the skill Id.
	 * @param lvl the skill level.
	 * @param player the player learning the skill.
	 * @return the skill learn for the specified parameters.
	 */
	public L2SkillLearn getSkillLearn(AcquireSkillType skillType, int id, int lvl, L2PcInstance player)
	{
		L2SkillLearn sl = null;
		switch (skillType)
		{
			case Class:
				sl = getClassSkill(id, lvl, player.getLearningClass());
				break;
			case Transform:
				sl = getTransformSkill(id, lvl);
				break;
			case Fishing:
				sl = getFishingSkill(id, lvl);
				break;
			case Pledge:
				sl = getPledgeSkill(id, lvl);
				break;
			case SubPledge:
				sl = getSubPledgeSkill(id, lvl);
				break;
			case Transfer:
				sl = getTransferSkill(id, lvl, player.getClassId());
				break;
			case SubClass:
				sl = getSubClassSkill(id, lvl);
				break;
			case Collect:
				sl = getCollectSkill(id, lvl);
				break;
		}
		return sl;
	}
	
	/**
	 * Gets the transform skill.
	 * @param id the transformation skill ID.
	 * @param lvl the transformation skill level.
	 * @return the transform skill from the Transform Skill Tree for a given {@code id} and {@code lvl}.
	 */
	public L2SkillLearn getTransformSkill(int id, int lvl)
	{
		return _transformSkillTree.get(SkillTable.getSkillHashCode(id, lvl));
	}
	
	/**
	 * Gets the class skill.
	 * @param id the class skill ID.
	 * @param lvl the class skill level.
	 * @param classId the class skill tree ID.
	 * @return the class skill from the Class Skill Trees for a given {@code classId}, {@code id} and {@code lvl}.
	 */
	public L2SkillLearn getClassSkill(int id, int lvl, ClassId classId)
	{
		return getCompleteClassSkillTree(classId).get(SkillTable.getSkillHashCode(id, lvl));
	}
	
	/**
	 * Gets the fishing skill.
	 * @param id the fishing skill ID.
	 * @param lvl the fishing skill level.
	 * @return Fishing skill from the Fishing Skill Tree for a given {@code id} and {@code lvl}.
	 */
	public L2SkillLearn getFishingSkill(int id, int lvl)
	{
		return _fishingSkillTree.get(SkillTable.getSkillHashCode(id, lvl));
	}
	
	/**
	 * Gets the pledge skill.
	 * @param id the pledge skill ID.
	 * @param lvl the pledge skill level.
	 * @return the pledge skill from the Pledge Skill Tree for a given {@code id} and {@code lvl}.
	 */
	public L2SkillLearn getPledgeSkill(int id, int lvl)
	{
		return _pledgeSkillTree.get(SkillTable.getSkillHashCode(id, lvl));
	}
	
	/**
	 * Gets the sub pledge skill.
	 * @param id the sub-pledge skill ID.
	 * @param lvl the sub-pledge skill level.
	 * @return the sub-pledge skill from the Sub-Pledge Skill Tree for a given {@code id} and {@code lvl}.
	 */
	public L2SkillLearn getSubPledgeSkill(int id, int lvl)
	{
		return _subPledgeSkillTree.get(SkillTable.getSkillHashCode(id, lvl));
	}
	
	/**
	 * Gets the transfer skill.
	 * @param id the transfer skill ID.
	 * @param lvl the transfer skill level.
	 * @param classId the transfer skill tree ID.
	 * @return the transfer skill from the Transfer Skill Trees for a given {@code classId}, {@code id} and {@code lvl}.
	 */
	public L2SkillLearn getTransferSkill(int id, int lvl, ClassId classId)
	{
		if (classId.getParent() != null)
		{
			final ClassId parentId = classId.getParent();
			if (_transferSkillTrees.get(parentId) != null)
			{
				return _transferSkillTrees.get(parentId).get(SkillTable.getSkillHashCode(id, lvl));
			}
		}
		return null;
	}
	
	/**
	 * Gets the sub class skill.
	 * @param id the sub-class skill ID.
	 * @param lvl the sub-class skill level.
	 * @return the sub-class skill from the Sub-Class Skill Tree for a given {@code id} and {@code lvl}.
	 */
	public L2SkillLearn getSubClassSkill(int id, int lvl)
	{
		return _subClassSkillTree.get(SkillTable.getSkillHashCode(id, lvl));
	}
	
	/**
	 * Gets the common skill.
	 * @param id the common skill Id.
	 * @param lvl the common skill level.
	 * @return the common skill from the Common Skill Tree for a given {@code id} and {@code lvl}.
	 */
	public L2SkillLearn getCommonSkill(int id, int lvl)
	{
		return _commonSkillTree.get(SkillTable.getSkillHashCode(id, lvl));
	}
	
	/**
	 * Gets the collect skill.
	 * @param id the collect skill ID.
	 * @param lvl the collect skill level.
	 * @return the collect skill from the Collect Skill Tree for a given {@code id} and {@code lvl}.
	 */
	public L2SkillLearn getCollectSkill(int id, int lvl)
	{
		return _collectSkillTree.get(SkillTable.getSkillHashCode(id, lvl));
	}
	
	/**
	 * Gets the min level for new skill.
	 * @param player the player that requires the minimum level.
	 * @param skillTree the skill tree to search the minimum get level.
	 * @return the minimum level for a new skill for a given {@code player} and {@code skillTree}.
	 */
	public int getMinLevelForNewSkill(L2PcInstance player, Map<Integer, L2SkillLearn> skillTree)
	{
		int minLevel = 0;
		if (skillTree.isEmpty())
		{
			_log.warning(getClass().getSimpleName() + ": SkillTree is not defined for getMinLevelForNewSkill!");
		}
		else
		{
			for (L2SkillLearn s : skillTree.values())
			{
				if (s.isLearnedByNpc() && (player.getLevel() < s.getGetLevel()))
				{
					if ((minLevel == 0) || (minLevel > s.getGetLevel()))
					{
						minLevel = s.getGetLevel();
					}
				}
			}
		}
		return minLevel;
	}
	
	/**
	 * Checks if is hero skill.
	 * @param skillId the Id of the skill to check.
	 * @param skillLevel the level of the skill to check, if it's -1 only Id will be checked.
	 * @return {@code true} if the skill is present in the Hero Skill Tree, {@code false} otherwise.
	 */
	public boolean isHeroSkill(int skillId, int skillLevel)
	{
		if (_heroSkillTree.containsKey(SkillTable.getSkillHashCode(skillId, skillLevel)))
		{
			return true;
		}
		
		for (L2SkillLearn skill : _heroSkillTree.values())
		{
			if ((skill.getSkillId() == skillId) && (skillLevel == -1))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if is GM skill.
	 * @param skillId skillId the Id of the skill to check.
	 * @param skillLevel skillLevel the level of the skill to check, if it's -1 only Id will be checked.
	 * @return {@code true} if the skill is present in the Game Master Skill Trees, {@code false} otherwise.
	 */
	public boolean isGMSkill(int skillId, int skillLevel)
	{
		final Map<Integer, L2SkillLearn> gmSkills = new HashMap<>();
		gmSkills.putAll(_gameMasterSkillTree);
		gmSkills.putAll(_gameMasterAuraSkillTree);
		if (gmSkills.containsKey(SkillTable.getSkillHashCode(skillId, skillLevel)))
		{
			return true;
		}
		
		for (L2SkillLearn skill : gmSkills.values())
		{
			if ((skill.getSkillId() == skillId) && (skillLevel == -1))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Adds the skills.
	 * @param gmchar the player to add the Game Master skills.
	 * @param auraSkills if {@code true} it will add "GM Aura" skills, else will add the "GM regular" skills.
	 */
	public void addSkills(L2PcInstance gmchar, boolean auraSkills)
	{
		final Collection<L2SkillLearn> skills = auraSkills ? _gameMasterAuraSkillTree.values() : _gameMasterSkillTree.values();
		final SkillTable st = SkillTable.getInstance();
		for (L2SkillLearn sl : skills)
		{
			gmchar.addSkill(st.getInfo(sl.getSkillId(), sl.getSkillLevel()), false); // Don't Save GM skills to database
		}
	}
	
	/**
	 * Create and store hash values for skills for easy and fast checks.
	 */
	private void generateCheckArrays()
	{
		int i;
		int[] array;
		
		// Class specific skills:
		Map<Integer, L2SkillLearn> tempMap;
		final Set<ClassId> keySet = _classSkillTrees.keySet();
		_skillsByClassIdHashCodes = new TIntObjectHashMap<>(keySet.size());
		for (ClassId cls : keySet)
		{
			i = 0;
			tempMap = getCompleteClassSkillTree(cls);
			array = new int[tempMap.size()];
			for (int h : tempMap.keySet())
			{
				array[i++] = h;
			}
			tempMap.clear();
			Arrays.sort(array);
			_skillsByClassIdHashCodes.put(cls.ordinal(), array);
		}
		
		// Race specific skills from Fishing and Transformation skill trees.
		final List<Integer> list = new ArrayList<>();
		_skillsByRaceHashCodes = new TIntObjectHashMap<>(Race.values().length);
		for (Race r : Race.values())
		{
			for (L2SkillLearn s : _fishingSkillTree.values())
			{
				if (s.getRaces().contains(r))
				{
					list.add(SkillTable.getSkillHashCode(s.getSkillId(), s.getSkillLevel()));
				}
			}
			
			for (L2SkillLearn s : _transformSkillTree.values())
			{
				if (s.getRaces().contains(r))
				{
					list.add(SkillTable.getSkillHashCode(s.getSkillId(), s.getSkillLevel()));
				}
			}
			
			i = 0;
			array = new int[list.size()];
			for (int s : list)
			{
				array[i++] = s;
			}
			Arrays.sort(array);
			_skillsByRaceHashCodes.put(r.ordinal(), array);
			list.clear();
		}
		
		// Skills available for all classes and races
		for (L2SkillLearn s : _commonSkillTree.values())
		{
			if (s.getRaces().isEmpty())
			{
				list.add(SkillTable.getSkillHashCode(s.getSkillId(), s.getSkillLevel()));
			}
		}
		
		for (L2SkillLearn s : _fishingSkillTree.values())
		{
			if (s.getRaces().isEmpty())
			{
				list.add(SkillTable.getSkillHashCode(s.getSkillId(), s.getSkillLevel()));
			}
		}
		
		for (L2SkillLearn s : _transformSkillTree.values())
		{
			if (s.getRaces().isEmpty())
			{
				list.add(SkillTable.getSkillHashCode(s.getSkillId(), s.getSkillLevel()));
			}
		}
		
		for (L2SkillLearn s : _collectSkillTree.values())
		{
			list.add(SkillTable.getSkillHashCode(s.getSkillId(), s.getSkillLevel()));
		}
		
		_allSkillsHashCodes = new int[list.size()];
		int j = 0;
		for (int hashcode : list)
		{
			_allSkillsHashCodes[j++] = hashcode;
		}
		Arrays.sort(_allSkillsHashCodes);
	}
	
	/**
	 * Verify if the give skill is valid for the given player.<br>
	 * GM's skills are excluded for GM players.
	 * @param player the player to verify the skill.
	 * @param skill the skill to be verified.
	 * @return {@code true} if the skill is allowed to the given player.
	 */
	public boolean isSkillAllowed(L2PcInstance player, L2Skill skill)
	{
		if (skill.isExcludedFromCheck())
		{
			return true;
		}
		
		if (player.isGM() && skill.isGMSkill())
		{
			return true;
		}
		
		// Prevent accidental skill remove during reload
		if (_loading)
		{
			return true;
		}
		
		final int maxLvl = SkillTable.getInstance().getMaxLevel(skill.getId());
		final int hashCode = SkillTable.getSkillHashCode(skill.getId(), Math.min(skill.getLevel(), maxLvl));
		
		if (Arrays.binarySearch(_skillsByClassIdHashCodes.get(player.getClassId().ordinal()), hashCode) >= 0)
		{
			return true;
		}
		
		if (Arrays.binarySearch(_skillsByRaceHashCodes.get(player.getRace().ordinal()), hashCode) >= 0)
		{
			return true;
		}
		
		if (Arrays.binarySearch(_allSkillsHashCodes, hashCode) >= 0)
		{
			return true;
		}
		
		// Exclude Transfer Skills from this check.
		if (getTransferSkill(skill.getId(), Math.min(skill.getLevel(), maxLvl), player.getClassId()) != null)
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Logs current Skill Trees skills count.
	 */
	private void report()
	{
		int classSkillTreeCount = 0;
		for (Map<Integer, L2SkillLearn> classSkillTree : _classSkillTrees.values())
		{
			classSkillTreeCount += classSkillTree.size();
		}
		
		int trasferSkillTreeCount = 0;
		for (Map<Integer, L2SkillLearn> trasferSkillTree : _transferSkillTrees.values())
		{
			trasferSkillTreeCount += trasferSkillTree.size();
		}
		
		int dwarvenOnlyFishingSkillCount = 0;
		for (L2SkillLearn fishSkill : _fishingSkillTree.values())
		{
			if (fishSkill.getRaces().contains(Race.Dwarf))
			{
				dwarvenOnlyFishingSkillCount++;
			}
		}
		
		int resSkillCount = 0;
		for (L2SkillLearn pledgeSkill : _pledgeSkillTree.values())
		{
			if (pledgeSkill.isResidencialSkill())
			{
				resSkillCount++;
			}
		}
		
		final String className = getClass().getSimpleName();
		_log.info(className + ": Loaded " + classSkillTreeCount + " Class Skills for " + _classSkillTrees.size() + " Class Skill Trees.");
		_log.info(className + ": Loaded " + _subClassSkillTree.size() + " Sub-Class Skills.");
		_log.info(className + ": Loaded " + trasferSkillTreeCount + " Transfer Skills for " + _transferSkillTrees.size() + " Transfer Skill Trees.");
		_log.info(className + ": Loaded " + _fishingSkillTree.size() + " Fishing Skills, " + dwarvenOnlyFishingSkillCount + " Dwarven only Fishing Skills.");
		_log.info(className + ": Loaded " + _collectSkillTree.size() + " Collect Skills.");
		_log.info(className + ": Loaded " + _pledgeSkillTree.size() + " Pledge Skills, " + (_pledgeSkillTree.size() - resSkillCount) + " for Pledge and " + resSkillCount + " Residential.");
		_log.info(className + ": Loaded " + _subPledgeSkillTree.size() + " Sub-Pledge Skills.");
		_log.info(className + ": Loaded " + _transformSkillTree.size() + " Transform Skills.");
		_log.info(className + ": Loaded " + _nobleSkillTree.size() + " Noble Skills.");
		_log.info(className + ": Loaded " + _heroSkillTree.size() + " Hero Skills.");
		_log.info(className + ": Loaded " + _gameMasterSkillTree.size() + " Game Master Skills.");
		_log.info(className + ": Loaded " + _gameMasterAuraSkillTree.size() + " Game Master Aura Skills.");
		final int commonSkills = _commonSkillTree.size();
		if (commonSkills > 0)
		{
			_log.info(className + ": Loaded " + commonSkills + " Common Skills to all classes.");
		}
	}
	
	/**
	 * Gets the single instance of SkillTreesData.
	 * @return the only instance of this class.
	 */
	public static SkillTreesData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	/**
	 * Singleton holder for the SkillTreesData class.
	 */
	private static class SingletonHolder
	{
		protected static final SkillTreesData _instance = new SkillTreesData();
	}
}
