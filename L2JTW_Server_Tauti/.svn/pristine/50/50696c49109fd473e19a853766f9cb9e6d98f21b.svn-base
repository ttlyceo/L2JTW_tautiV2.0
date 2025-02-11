package com.l2jserver.gameserver.network.serverpackets;

public class ExVitalityEffectInfo extends L2GameServerPacket
{
	public ExVitalityEffectInfo()
	{
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0x11E);
		writeD(14000);
		writeD(200);
		writeD(5);
		writeD(5);
	}
	
	@Override
	public final String getType()
	{
		return "[S] FE:11E ExVitalityEffectInfo";
	}
}
