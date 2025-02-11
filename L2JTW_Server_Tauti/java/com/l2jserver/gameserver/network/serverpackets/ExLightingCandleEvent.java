package com.l2jserver.gameserver.network.serverpackets;

public class ExLightingCandleEvent extends L2GameServerPacket
{
	public ExLightingCandleEvent()
	{
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0x11D);
		writeH(0x00);
	}
	
	@Override
	public final String getType()
	{
		return "[S] FE:11D ExLightingCandleEvent";
	}
}
