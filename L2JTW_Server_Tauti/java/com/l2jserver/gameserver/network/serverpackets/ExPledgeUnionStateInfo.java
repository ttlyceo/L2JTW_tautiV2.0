package com.l2jserver.gameserver.network.serverpackets;

public class ExPledgeUnionStateInfo extends L2GameServerPacket
{
	public ExPledgeUnionStateInfo()
	{
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0x138);
		writeD(0);
		writeD(1300);
		writeD(0);
	}
	
	@Override
	public final String getType()
	{
		return "[S] FE:138 ExPledgeUnionStateInfo";
	}
}
