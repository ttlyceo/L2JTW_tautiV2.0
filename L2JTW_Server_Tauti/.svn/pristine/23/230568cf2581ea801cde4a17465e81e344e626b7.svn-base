package com.l2jserver.gameserver.network.serverpackets;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

public class MagicAndSkillList extends L2GameServerPacket
{
	private L2PcInstance _activeChar;
	
	public MagicAndSkillList(L2PcInstance player)
	{
		_activeChar = player;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x40);
		writeD(_activeChar.getObjectId());
		writeD(0); //Unknown Value
		writeC(0x86);
		writeC(0x25);
		writeC(0x0B);
		writeC(0x00);
	}
	
	@Override
	public final String getType()
	{
		return "[S] 40 MagicAndSkillList";
	}
}
