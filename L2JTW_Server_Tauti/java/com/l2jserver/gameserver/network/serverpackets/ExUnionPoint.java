package com.l2jserver.gameserver.network.serverpackets;

public class ExUnionPoint extends L2GameServerPacket
{
	private final int _UnionPoint;
	
	public ExUnionPoint(int UnionPoint)
	{
		_UnionPoint = UnionPoint;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0x139);
		writeD(_UnionPoint);
	}
	
	@Override
	public final String getType()
	{
		return "[S] FE:139 ExUnionPoint";
	}
}
