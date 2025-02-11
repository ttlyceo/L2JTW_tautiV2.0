package com.l2jserver.gameserver.network.serverpackets;

public class ExCastleState extends L2GameServerPacket
{
	private final int _CastleState;
	
	public ExCastleState(int CastleState)
	{
		_CastleState = CastleState;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0x133);
		writeD(_CastleState);
		writeD(1);
	}
	
	@Override
	public final String getType()
	{
		return "[S] FE:133 ExCastleState";
	}
}
