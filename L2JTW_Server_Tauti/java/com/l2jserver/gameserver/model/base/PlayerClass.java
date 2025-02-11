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
package com.l2jserver.gameserver.model.base;

import static com.l2jserver.gameserver.model.base.ClassLevel.Awaken; //rocknow-God-Awaking
import static com.l2jserver.gameserver.model.base.ClassLevel.First;
import static com.l2jserver.gameserver.model.base.ClassLevel.Fourth;
import static com.l2jserver.gameserver.model.base.ClassLevel.Second;
import static com.l2jserver.gameserver.model.base.ClassLevel.Third;
import static com.l2jserver.gameserver.model.base.ClassType.Fighter;
import static com.l2jserver.gameserver.model.base.ClassType.Mystic;
import static com.l2jserver.gameserver.model.base.ClassType.Priest;
import static com.l2jserver.gameserver.model.base.Race.DarkElf;
import static com.l2jserver.gameserver.model.base.Race.Dwarf;
import static com.l2jserver.gameserver.model.base.Race.Elf;
import static com.l2jserver.gameserver.model.base.Race.Human;
import static com.l2jserver.gameserver.model.base.Race.Kamael;
import static com.l2jserver.gameserver.model.base.Race.Orc;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

import com.l2jserver.Config;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author luisantonioa
 */
public enum PlayerClass
{
	HumanFighter(Human, Fighter, First, 0),
	Warrior(Human, Fighter, Second, 0),
	Gladiator(Human, Fighter, Third, 2),
	Warlord(Human, Fighter, Third, 2),
	HumanKnight(Human, Fighter, Second, 0),
	Paladin(Human, Fighter, Third, 1),
	DarkAvenger(Human, Fighter, Third, 1),
	Rogue(Human, Fighter, Second, 0),
	TreasureHunter(Human, Fighter, Third, 3),
	Hawkeye(Human, Fighter, Third, 4),
	HumanMystic(Human, Mystic, First, 0),
	HumanWizard(Human, Mystic, Second, 0),
	Sorceror(Human, Mystic, Third, 5),
	Necromancer(Human, Mystic, Third, 5),
	Warlock(Human, Mystic, Third, 7),
	Cleric(Human, Priest, Second, 0),
	Bishop(Human, Priest, Third, 8),
	Prophet(Human, Priest, Third, 6),
	
	ElvenFighter(Elf, Fighter, First, 0),
	ElvenKnight(Elf, Fighter, Second, 0),
	TempleKnight(Elf, Fighter, Third, 1),
	Swordsinger(Elf, Fighter, Third, 6),
	ElvenScout(Elf, Fighter, Second, 0),
	Plainswalker(Elf, Fighter, Third, 3),
	SilverRanger(Elf, Fighter, Third, 4),
	ElvenMystic(Elf, Mystic, First, 0),
	ElvenWizard(Elf, Mystic, Second, 0),
	Spellsinger(Elf, Mystic, Third, 5),
	ElementalSummoner(Elf, Mystic, Third, 7),
	ElvenOracle(Elf, Priest, Second, 0),
	ElvenElder(Elf, Priest, Third, 8),
	
	DarkElvenFighter(DarkElf, Fighter, First, 0),
	PalusKnight(DarkElf, Fighter, Second, 0),
	ShillienKnight(DarkElf, Fighter, Third, 1),
	Bladedancer(DarkElf, Fighter, Third, 6),
	Assassin(DarkElf, Fighter, Second, 0),
	AbyssWalker(DarkElf, Fighter, Third, 3),
	PhantomRanger(DarkElf, Fighter, Third, 4),
	DarkElvenMystic(DarkElf, Mystic, First, 0),
	DarkElvenWizard(DarkElf, Mystic, Second, 0),
	Spellhowler(DarkElf, Mystic, Third, 5),
	PhantomSummoner(DarkElf, Mystic, Third, 7),
	ShillienOracle(DarkElf, Priest, Second, 0),
	ShillienElder(DarkElf, Priest, Third, 8),
	
	OrcFighter(Orc, Fighter, First, 0),
	OrcRaider(Orc, Fighter, Second, 0),
	Destroyer(Orc, Fighter, Third, 2),
	OrcMonk(Orc, Fighter, Second, 0),
	Tyrant(Orc, Fighter, Third, 2),
	OrcMystic(Orc, Mystic, First, 0),
	OrcShaman(Orc, Mystic, Second, 0),
	Overlord(Orc, Mystic, Third, 6),
	Warcryer(Orc, Mystic, Third, 6),
	
	DwarvenFighter(Dwarf, Fighter, First, 0),
	DwarvenScavenger(Dwarf, Fighter, Second, 0),
	BountyHunter(Dwarf, Fighter, Third, 3),
	DwarvenArtisan(Dwarf, Fighter, Second, 0),
	Warsmith(Dwarf, Fighter, Third, 2),
	
	dummyEntry1(null, null, null, 0),
	dummyEntry2(null, null, null, 0),
	dummyEntry3(null, null, null, 0),
	dummyEntry4(null, null, null, 0),
	dummyEntry5(null, null, null, 0),
	dummyEntry6(null, null, null, 0),
	dummyEntry7(null, null, null, 0),
	dummyEntry8(null, null, null, 0),
	dummyEntry9(null, null, null, 0),
	dummyEntry10(null, null, null, 0),
	dummyEntry11(null, null, null, 0),
	dummyEntry12(null, null, null, 0),
	dummyEntry13(null, null, null, 0),
	dummyEntry14(null, null, null, 0),
	dummyEntry15(null, null, null, 0),
	dummyEntry16(null, null, null, 0),
	dummyEntry17(null, null, null, 0),
	dummyEntry18(null, null, null, 0),
	dummyEntry19(null, null, null, 0),
	dummyEntry20(null, null, null, 0),
	dummyEntry21(null, null, null, 0),
	dummyEntry22(null, null, null, 0),
	dummyEntry23(null, null, null, 0),
	dummyEntry24(null, null, null, 0),
	dummyEntry25(null, null, null, 0),
	dummyEntry26(null, null, null, 0),
	dummyEntry27(null, null, null, 0),
	dummyEntry28(null, null, null, 0),
	dummyEntry29(null, null, null, 0),
	dummyEntry30(null, null, null, 0),
	/*
	 * (3rd classes)
	 */
	duelist(Human, Fighter, Fourth, 2),
	dreadnought(Human, Fighter, Fourth, 2),
	phoenixKnight(Human, Fighter, Fourth, 1),
	hellKnight(Human, Fighter, Fourth, 1),
	sagittarius(Human, Fighter, Fourth, 4),
	adventurer(Human, Fighter, Fourth, 3),
	archmage(Human, Mystic, Fourth, 5),
	soultaker(Human, Mystic, Fourth, 5),
	arcanaLord(Human, Mystic, Fourth, 7),
	cardinal(Human, Priest, Fourth, 8),
	hierophant(Human, Priest, Fourth, 6),
	
	evaTemplar(Elf, Fighter, Fourth, 1),
	swordMuse(Elf, Fighter, Fourth, 6),
	windRider(Elf, Fighter, Fourth, 3),
	moonlightSentinel(Elf, Fighter, Fourth, 4),
	mysticMuse(Elf, Mystic, Fourth, 5),
	elementalMaster(Elf, Mystic, Fourth, 7),
	evaSaint(Elf, Priest, Fourth, 8),
	
	shillienTemplar(DarkElf, Fighter, Fourth, 1),
	spectralDancer(DarkElf, Fighter, Fourth, 6),
	ghostHunter(DarkElf, Fighter, Fourth, 3),
	ghostSentinel(DarkElf, Fighter, Fourth, 4),
	stormScreamer(DarkElf, Mystic, Fourth, 5),
	spectralMaster(DarkElf, Mystic, Fourth, 7),
	shillienSaint(DarkElf, Priest, Fourth, 8),
	
	titan(Orc, Fighter, Fourth, 2),
	grandKhavatari(Orc, Fighter, Fourth, 2),
	dominator(Orc, Mystic, Fourth, 6),
	doomcryer(Orc, Mystic, Fourth, 6),
	
	fortuneSeeker(Dwarf, Fighter, Fourth, 3),
	maestro(Dwarf, Fighter, Fourth, 2),
	
	dummyEntry31(null, null, null, 0),
	dummyEntry32(null, null, null, 0),
	dummyEntry33(null, null, null, 0),
	dummyEntry34(null, null, null, 0),
	
	maleSoldier(Kamael, Fighter, First, 0),
	femaleSoldier(Kamael, Fighter, First, 0),
	trooper(Kamael, Fighter, Second, 0),
	warder(Kamael, Fighter, Second, 0),
	berserker(Kamael, Fighter, Third, 2),
	maleSoulbreaker(Kamael, Fighter, Third, 5),
	femaleSoulbreaker(Kamael, Fighter, Third, 5),
	arbalester(Kamael, Fighter, Third, 4),
	doombringer(Kamael, Fighter, Fourth, 2),
	maleSoulhound(Kamael, Fighter, Fourth, 5),
	femaleSoulhound(Kamael, Fighter, Fourth, 5),
	trickster(Kamael, Fighter, Fourth, 4),
	inspector(Kamael, Fighter, Third, 6),
	judicator(Kamael, Fighter, Fourth, 6),
	
	dummyEntry35(null, null, null, 0),
	dummyEntry36(null, null, null, 0),
	
	// Awakening
	sigelKnight(null, Fighter, Awaken, 1),
	tyrrWarrior(null, Fighter, Awaken, 2),
	othellRogue(null, Fighter, Awaken, 3),
	yulArcher(null, Fighter, Awaken, 4),
	feohWizard(null, Mystic, Awaken, 5),
	issEnchanter(null, Fighter, Awaken, 6),
	wynnSummoner(null, Mystic, Awaken, 7),
	aeoreHealer(null, Priest, Awaken, 8);
	
	private Race _race;
	private ClassLevel _level;
	private ClassType _type;
	private int _classtype2; //rocknow-God
	
	private static final Set<PlayerClass> mainSubclassSet;
	private static final Set<PlayerClass> neverSubclassed = EnumSet.of(Overlord, Warsmith);
	
	//rocknow-God-Start
	private static final Set<PlayerClass> subclasseSet1 = EnumSet.of(Paladin, DarkAvenger, TempleKnight, ShillienKnight, phoenixKnight, hellKnight, evaTemplar, shillienTemplar);
	private static final Set<PlayerClass> subclasseSet2 = EnumSet.of(Gladiator, Warlord, Destroyer, Tyrant, berserker, doombringer, duelist, dreadnought, titan, grandKhavatari, maestro);
	private static final Set<PlayerClass> subclasseSet3 = EnumSet.of(TreasureHunter, Plainswalker, AbyssWalker, BountyHunter, adventurer, windRider, ghostHunter, fortuneSeeker);
	private static final Set<PlayerClass> subclasseSet4 = EnumSet.of(Hawkeye, SilverRanger, PhantomRanger, arbalester, trickster, sagittarius, moonlightSentinel, ghostSentinel);
	private static final Set<PlayerClass> subclasseSet5 = EnumSet.of(Sorceror, Necromancer, Spellsinger, Spellhowler, maleSoulbreaker, femaleSoulbreaker, maleSoulhound, femaleSoulhound, archmage, soultaker, mysticMuse, stormScreamer);
	private static final Set<PlayerClass> subclasseSet6 = EnumSet.of(Prophet, Swordsinger, Bladedancer, Overlord, Warcryer, inspector, judicator, hierophant, swordMuse, spectralDancer, dominator, doomcryer);
	private static final Set<PlayerClass> subclasseSet7 = EnumSet.of(Warlock, ElementalSummoner, PhantomSummoner, arcanaLord, elementalMaster, spectralMaster);
	private static final Set<PlayerClass> subclasseSet8 = EnumSet.of(Bishop, ElvenElder, ShillienElder, cardinal, evaSaint, shillienSaint);
	//rocknow-God-End
	
	private static final EnumMap<PlayerClass, Set<PlayerClass>> subclassSetMap = new EnumMap<>(PlayerClass.class);
	
	static
	{
		Set<PlayerClass> subclasses = getSet(null, Third);
		subclasses.removeAll(neverSubclassed);
		
		mainSubclassSet = subclasses;
		
		//rocknow-God-Start
		subclassSetMap.put(Paladin, subclasseSet1);
		subclassSetMap.put(DarkAvenger, subclasseSet1);
		subclassSetMap.put(TempleKnight, subclasseSet1);
		subclassSetMap.put(ShillienKnight, subclasseSet1);
		subclassSetMap.put(phoenixKnight, subclasseSet1);
		subclassSetMap.put(hellKnight, subclasseSet1);
		subclassSetMap.put(evaTemplar, subclasseSet1);
		subclassSetMap.put(shillienTemplar, subclasseSet1);
		
		subclassSetMap.put(Gladiator, subclasseSet2);
		subclassSetMap.put(Warlord, subclasseSet2);
		subclassSetMap.put(Destroyer, subclasseSet2);
		subclassSetMap.put(Tyrant, subclasseSet2);
		subclassSetMap.put(berserker, subclasseSet2);
		subclassSetMap.put(duelist, subclasseSet2);
		subclassSetMap.put(dreadnought, subclasseSet2);
		subclassSetMap.put(titan, subclasseSet2);
		subclassSetMap.put(grandKhavatari, subclasseSet2);
		subclassSetMap.put(maestro, subclasseSet2);
		
		subclassSetMap.put(TreasureHunter, subclasseSet3);
		subclassSetMap.put(Plainswalker, subclasseSet3);
		subclassSetMap.put(AbyssWalker, subclasseSet3);
		subclassSetMap.put(BountyHunter, subclasseSet3);
		subclassSetMap.put(adventurer, subclasseSet3);
		subclassSetMap.put(windRider, subclasseSet3);
		subclassSetMap.put(ghostHunter, subclasseSet3);
		subclassSetMap.put(fortuneSeeker, subclasseSet3);
		
		subclassSetMap.put(Hawkeye, subclasseSet4);
		subclassSetMap.put(SilverRanger, subclasseSet4);
		subclassSetMap.put(PhantomRanger, subclasseSet4);
		subclassSetMap.put(arbalester, subclasseSet4);
		subclassSetMap.put(sagittarius, subclasseSet4);
		subclassSetMap.put(moonlightSentinel, subclasseSet4);
		subclassSetMap.put(ghostSentinel, subclasseSet4);
		
		subclassSetMap.put(Sorceror, subclasseSet5);
		subclassSetMap.put(Necromancer, subclasseSet5);
		subclassSetMap.put(Spellsinger, subclasseSet5);
		subclassSetMap.put(Spellhowler, subclasseSet5);
		subclassSetMap.put(maleSoulbreaker, subclasseSet5);
		subclassSetMap.put(femaleSoulbreaker, subclasseSet5);
		subclassSetMap.put(archmage, subclasseSet5);
		subclassSetMap.put(soultaker, subclasseSet5);
		subclassSetMap.put(mysticMuse, subclasseSet5);
		subclassSetMap.put(stormScreamer, subclasseSet5);
		
		subclassSetMap.put(Prophet, subclasseSet6);
		subclassSetMap.put(Swordsinger, subclasseSet6);
		subclassSetMap.put(Bladedancer, subclasseSet6);
		subclassSetMap.put(Overlord, subclasseSet6);
		subclassSetMap.put(Warcryer, subclasseSet6);
		subclassSetMap.put(inspector, subclasseSet6);
		subclassSetMap.put(hierophant, subclasseSet6);
		subclassSetMap.put(swordMuse, subclasseSet6);
		subclassSetMap.put(spectralDancer, subclasseSet6);
		subclassSetMap.put(dominator, subclasseSet6);
		subclassSetMap.put(doomcryer, subclasseSet6);
		
		subclassSetMap.put(Warlock, subclasseSet7);
		subclassSetMap.put(ElementalSummoner, subclasseSet7);
		subclassSetMap.put(PhantomSummoner, subclasseSet7);
		subclassSetMap.put(arcanaLord, subclasseSet7);
		subclassSetMap.put(elementalMaster, subclasseSet7);
		subclassSetMap.put(spectralMaster, subclasseSet7);
		
		subclassSetMap.put(Bishop, subclasseSet8);
		subclassSetMap.put(ElvenElder, subclasseSet8);
		subclassSetMap.put(ShillienElder, subclasseSet8);
		subclassSetMap.put(cardinal, subclasseSet8);
		subclassSetMap.put(evaSaint, subclasseSet8);
		subclassSetMap.put(shillienSaint, subclasseSet8);
		//rocknow-God-End
	}
	
	PlayerClass(Race pRace, ClassType pType, ClassLevel pLevel, int ClassType2) //rocknow-God
	{
		_race = pRace;
		_level = pLevel;
		_type = pType;
		_classtype2 = ClassType2; //rocknow-God
	}
	
	public final Set<PlayerClass> getAvailableSubclasses(L2PcInstance player)
	{
		Set<PlayerClass> subclasses = null;
		
		if (_level == Third || _level == Awaken) //l2jtw add
		{
			if (player.getRace() != Kamael)
			{
				subclasses = EnumSet.copyOf(mainSubclassSet);
				
				subclasses.remove(this);
				
				switch (player.getRace())
				{
					case Elf:
						subclasses.removeAll(getSet(DarkElf, Third));
						break;
					case DarkElf:
						subclasses.removeAll(getSet(Elf, Third));
						break;
				}
				
				subclasses.removeAll(getSet(Kamael, Third));
				
				Set<PlayerClass> unavailableClasses = subclassSetMap.get(this);
				
				if (unavailableClasses != null)
					subclasses.removeAll(unavailableClasses);
				
				//rocknow-God-Start
				if (player.getBaseClass() == 57 || player.getBaseClass() == 118)
				{
					if (subclasseSet2 != null)
						subclasses.removeAll(subclasseSet2);
				}
				
				if (player.getSubClasses().size() > 0)
				{
					for (SubClass subClass : player.getSubClasses().values())
					{
						int classtype2 = 0;
						classtype2 = subClass.getClassDefinition().ClassType2();
						switch (classtype2)
						{
							case 1:
								if (subclasseSet1 != null)
									subclasses.removeAll(subclasseSet1);
								break;
							case 2:
								if (subclasseSet2 != null)
									subclasses.removeAll(subclasseSet2);
								break;
							case 3:
								if (subclasseSet3 != null)
									subclasses.removeAll(subclasseSet3);
								break;
							case 4:
								if (subclasseSet4 != null)
									subclasses.removeAll(subclasseSet4);
								break;
							case 5:
								if (subclasseSet5 != null)
									subclasses.removeAll(subclasseSet5);
								break;
							case 6:
								if (subclasseSet6 != null)
									subclasses.removeAll(subclasseSet6);
								break;
							case 7:
								if (subclasseSet7 != null)
									subclasses.removeAll(subclasseSet7);
								break;
							case 8:
								if (subclasseSet8 != null)
									subclasses.removeAll(subclasseSet8);
								break;
							default:
								break;
						}
					}
				}
				if (player.getBaseClass() >= 139)
				{
					switch (player.getBaseClass())
					{
						case 139:
							if (subclasseSet1 != null)
								subclasses.removeAll(subclasseSet1);
							break;
						case 140:
							if (subclasseSet2 != null)
								subclasses.removeAll(subclasseSet2);
							break;
						case 141:
							if (subclasseSet3 != null)
								subclasses.removeAll(subclasseSet3);
							break;
						case 142:
							if (subclasseSet4 != null)
								subclasses.removeAll(subclasseSet4);
							break;
						case 143:
							if (subclasseSet5 != null)
								subclasses.removeAll(subclasseSet5);
							break;
						case 144:
							if (subclasseSet6 != null)
								subclasses.removeAll(subclasseSet6);
							break;
						case 145:
							if (subclasseSet7 != null)
								subclasses.removeAll(subclasseSet7);
							break;
						case 146:
							if (subclasseSet8 != null)
								subclasses.removeAll(subclasseSet8);
							break;
					}
				}
				//rocknow-God-End
			}
			else
			{
				subclasses = getSet(Kamael, Third);
				subclasses.remove(this);
				// Check sex, male subclasses female and vice versa
				// If server owner set MaxSubclass > 3 some kamael's cannot take 4 sub
				// So, in that situation we must skip sex check
				if (Config.MAX_SUBCLASS <= 3)
				{
					if (player.getAppearance().getSex())
						subclasses.removeAll(EnumSet.of(femaleSoulbreaker));
					else
						subclasses.removeAll(EnumSet.of(maleSoulbreaker));
				}
				if (!player.getSubClasses().containsKey(2) || player.getSubClasses().get(2).getLevel() < 75)
					subclasses.removeAll(EnumSet.of(inspector));
			}
		}
		return subclasses;
	}
	
	public static final EnumSet<PlayerClass> getSet(Race race, ClassLevel level)
	{
		EnumSet<PlayerClass> allOf = EnumSet.noneOf(PlayerClass.class);
		
		for (PlayerClass playerClass : EnumSet.allOf(PlayerClass.class))
		{
			if (race == null || playerClass.isOfRace(race))
			{
				if (level == null || playerClass.isOfLevel(level))
				{
					allOf.add(playerClass);
				}
			}
		}
		return allOf;
	}
	
	public final boolean isOfRace(Race pRace)
	{
		return _race == pRace;
	}
	
	public final boolean isOfType(ClassType pType)
	{
		return _type == pType;
	}
	
	public final boolean isOfLevel(ClassLevel pLevel)
	{
		return _level == pLevel;
	}
	//rocknow-God-Start
	public final int ClassType2()
	{
		return _classtype2;
	}
	//rocknow-God-End
	public final ClassLevel getLevel()
	{
		return _level;
	}
}
