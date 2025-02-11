package com.l2jserver.gameserver.network.serverpackets;

public class ExBlockRemoveResult extends L2GameServerPacket
{
	public ExBlockRemoveResult()
	{
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xfe);
		writeH(0xee);//449 Test
	}
	
	@Override
	public final String getType()
	{
		return "[S] FE:ED ExBlockRemoveResult";
	}
}
