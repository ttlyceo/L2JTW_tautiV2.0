package com.l2jserver.gameserver.network.serverpackets;

import com.l2jserver.gameserver.model.base.ClassLevel;
import com.l2jserver.gameserver.model.base.SubClass;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

public class ExSubjobInfo extends L2GameServerPacket
{
	private final L2PcInstance _player;
	
	public ExSubjobInfo(L2PcInstance _cha)
	{
		_player = _cha;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0xEA);
		writeC(0x00);
		writeD(_player.getClassId().getId());
		writeD(_player.getRace().ordinal());
		writeD(_player.getSubClasses().size() + 1);
		
		writeD(_player.getClassIndex());
		writeD(_player.getBaseClass());
		writeD(_player.getStat().getBaseLevel());
		writeC(0x00); // 0 main, 1 dual, 2 sub
		
		for (SubClass sc : _player.getSubClasses().values())
		{
			writeD(sc.getClassIndex());
			writeD(sc.getClassId());
			writeD(sc.getLevel());
			writeC(sc.getClassDefinition().isOfLevel(ClassLevel.Awaken) ? 1 : 2); // 0 main, 1 dual, 2 sub
		}
	}
	
	@Override
	public String getType()
	{
		return "ExSubjobInfo";
	}
}