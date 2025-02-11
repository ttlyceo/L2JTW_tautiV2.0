package com.l2jserver.gameserver.network.serverpackets;

public class ExTutorialList extends L2GameServerPacket
{
	public ExTutorialList()
	{
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0x6C);
		writeB(new byte[128]);
	}
	
	@Override
	public final String getType()
	{
		return "[S] FE:6C ExTutorialList";
	}
}
