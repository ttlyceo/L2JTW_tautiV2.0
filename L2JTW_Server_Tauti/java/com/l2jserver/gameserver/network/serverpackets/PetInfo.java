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
package com.l2jserver.gameserver.network.serverpackets;

import com.l2jserver.gameserver.model.actor.L2Summon;
import com.l2jserver.gameserver.model.actor.instance.L2PetInstance;
import com.l2jserver.gameserver.model.actor.instance.L2ServitorInstance;

/**
 * This class ...
 *
 * @version $Revision: 1.6.2.5.2.12 $ $Date: 2005/03/31 09:19:16 $
 */
public class PetInfo extends L2GameServerPacket
{
	private static final String _S__B2_PETINFO = "[S] b2 PetInfo";
	private L2Summon _summon;
	private int _x, _y, _z, _heading;
	private boolean _isSummoned;
	private int _val;
	private int _mAtkSpd, _pAtkSpd;
	private int _runSpd, _walkSpd, _swimRunSpd, _swimWalkSpd, _flRunSpd, _flWalkSpd, _flyRunSpd, _flyWalkSpd;
	private int _maxHp, _maxMp;
	private int _maxFed, _curFed;
	private float _multiplier;
	
	/**
	 * rev 478  dddddddddddddddddddffffdddcccccSSdddddddddddddddddddddddddddhc
	 * @param summon
	 * @param val
	 */
	public PetInfo(L2Summon summon, int val)
	{
		_summon = summon;
		_isSummoned = _summon.isShowSummonAnimation();
		_x = _summon.getX();
		_y = _summon.getY();
		_z = _summon.getZ();
		_heading = _summon.getHeading();
		_mAtkSpd = _summon.getMAtkSpd();
		_pAtkSpd = _summon.getPAtkSpd();
		_multiplier = _summon.getMovementSpeedMultiplier();
		_runSpd = _summon.getTemplate().getBaseRunSpd();
		_walkSpd = _summon.getTemplate().getBaseWalkSpd();
		_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
		_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
		_maxHp = _summon.getMaxVisibleHp();
		_maxMp = _summon.getMaxMp();
		_val = val;
		if (_summon instanceof L2PetInstance)
		{
			L2PetInstance pet = (L2PetInstance)_summon;
			_curFed = pet.getCurrentFed(); // how fed it is
			_maxFed = pet.getMaxFed(); //max fed it can be
		}
		else if (_summon instanceof L2ServitorInstance)
		{
			L2ServitorInstance sum = (L2ServitorInstance)_summon;
			_curFed = sum.getTimeRemaining();
			_maxFed = sum.getTotalLifeTime();
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xb2);
		writeD(_summon.getSummonType());
		writeD(_summon.getObjectId());
		writeD(_summon.getTemplate().getIdTemplate()+1000000);
		writeD(0);    // 1=attackable
		
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);
		writeD(0);
		writeD(_mAtkSpd);
		writeD(_pAtkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_swimRunSpd);
		writeD(_swimWalkSpd);
		writeD(_flRunSpd);
		writeD(_flWalkSpd);
		writeD(_flyRunSpd);
		writeD(_flyWalkSpd);
		
		writeF(_multiplier); // movement multiplier
		writeF(1); // attack speed multiplier
		writeF(_summon.getTemplate().getfCollisionRadius());
		writeF(_summon.getTemplate().getfCollisionHeight());
		writeD(_summon.getWeapon()); // right hand weapon
		writeD(_summon.getArmor()); // body armor
		writeD(0); // left hand weapon
		writeC(_summon.getOwner() != null ? 1 : 0);	// when pet is dead and player exit game, pet doesn't show master name
		writeC(1);	// running=1 (it is always 1, walking mode is calculated from multiplier)
		writeC(_summon.isInCombat() ? 1 : 0);	// attacking 1=true
		writeC(_summon.isAlikeDead() ? 1 : 0);  // dead 1=true
		writeC(_isSummoned ? 2 : _val); //  0=teleported  1=default   2=summoned
		writeD(-1); // High Five NPCString ID
		if (_summon instanceof L2PetInstance)
		{
			writeS(_summon.getName()); // Pet name.
		}
		else
		{
			writeS(_summon.getTemplate().isServerSideName() ? _summon.getName() : ""); // Summon name.
		}
		writeD(-1); // High Five NPCString ID
		writeS(_summon.getTitle()); // owner name
		writeD(1);
		writeD(_summon.getOwner() != null ? _summon.getOwner().getPvpFlag() : 0);	//0 = white,2= purpleblink, if its greater then karma = purple
		int Karma = _summon.getOwner().getKarma(); //rocknow-God-Test
		if (Karma > 0) //rocknow-God-Test
			Karma = 0 - Karma; //rocknow-God-Test
		writeD(_summon.getOwner() != null ? Karma : 0);  // karma //rocknow-God-Test
		writeD(_curFed); // how fed it is
		writeD(_maxFed); //max fed it can be
		writeD((int)_summon.getCurrentHp());//current hp
		writeD(_maxHp);// max hp
		writeD((int)_summon.getCurrentMp());//current mp
		writeD(_maxMp);//max mp
		writeD(_summon.getStat().getSp()); //sp
		writeD(_summon.getLevel());// lvl
		writeQ(_summon.getStat().getExp());
		
		if (_summon.getExpForThisLevel()>_summon.getStat().getExp())
			writeQ(_summon.getStat().getExp());// 0%  absolute value
		else
			writeQ(_summon.getExpForThisLevel());// 0%  absolute value
		
		writeQ(_summon.getExpForNextLevel());// 100% absoulte value
		writeD(_summon instanceof L2PetInstance ? _summon.getInventory().getTotalWeight() : 0);//weight
		writeD(_summon.getMaxLoad());//max weight it can carry
		writeD(_summon.getPAtk(null));//patk
		writeD(_summon.getPDef(null));//pdef
		writeD(_summon.getAccuracy());//accuracy //rocknow-God
		writeD(_summon.getEvasionRate(null));//evasion //rocknow-God
		writeD(_summon.getCriticalHit(null,null));//critical //rocknow-God
		writeD(_summon.getMAtk(null,null));//matk //rocknow-God
		writeD(_summon.getMDef(null,null));//mdef //rocknow-God
		writeD(_summon.getAccuracy()); //rocknow-God //Magic
		writeD(_summon.getEvasionRate(null)); //rocknow-God //Magic
		writeD(_summon.getCriticalHit(null,null)); //rocknow-God //Magic
		writeD((int) _summon.getStat().getMoveSpeed());//speed
		writeD(_summon.getPAtkSpd());//atkspeed
		writeD(_summon.getMAtkSpd());//casting speed
		
		//rocknow-God writeD(_summon.getAbnormalEffect());//c2  abnormal visual effect... bleed=1; poison=2; poison & bleed=3; flame=4;
		writeH(_summon.isMountable() ? 1 : 0);//c2    ride button
		
		writeC(0); // c2
		
		// Following all added in C4.
		writeH(0); // ??
		writeC(_summon.getOwner() != null ? _summon.getOwner().getTeam() : 0); // team aura (1 = blue, 2 = red)
		writeD(_summon.getSoulShotsPerHit()); // How many soulshots this servitor uses per hit
		writeD(_summon.getSpiritShotsPerHit()); // How many spiritshots this servitor uses per hit
		
		int form = 0;
		final int npcId = _summon.getNpcId();
		if (npcId == 16041 || npcId == 16042)
		{
			if(_summon.getLevel() > 84)
				form = 3;
			else if(_summon.getLevel() > 79)
				form = 2;
			else if(_summon.getLevel() > 74)
				form = 1;
		}
		else if (npcId == 16025 ||npcId == 16037)
		{
			if(_summon.getLevel() > 69)
				form = 3;
			else if(_summon.getLevel() > 64)
				form = 2;
			else if(_summon.getLevel() > 59)
				form = 1;
		}
		writeD(form);//CT1.5 Pet form and skills
		writeD(0); //rocknow-God-displayEffect transform ID
		writeD(_summon.getOwner().getUsedSummonPoint()); //rocknow-God used summon point
		writeD(_summon.getOwner().getMaxSummonPoint()); //rocknow-God max summon point
		/* l2jtw start
		writeD(2); //rocknow-God AbnormalEffect-Number
		writeD(_summon.getAbnormalEffect()); //rocknow-God
		writeD(_summon.getSpecialEffect()); //rocknow-God
		 */
		java.util.List<Integer> el = _summon.getEffectIdList();
		writeD(el.size());
		for(int i : el)
		{
		   writeD(i);
		}
		// l2jtw end
		writeC(0); //rocknow-God
	}
	
	@Override
	public String getType()
	{
		return _S__B2_PETINFO;
	}
}
