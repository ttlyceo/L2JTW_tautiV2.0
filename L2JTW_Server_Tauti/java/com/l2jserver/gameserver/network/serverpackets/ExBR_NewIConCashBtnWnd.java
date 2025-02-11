package com.l2jserver.gameserver.network.serverpackets;

public class ExBR_NewIConCashBtnWnd extends L2GameServerPacket
{
	public ExBR_NewIConCashBtnWnd()
	{
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0x13E);
		writeH(0x01);
	}
	
	@Override
	public final String getType()
	{
		return "[S] FE:13E ExBR_NewIConCashBtnWnd";
	}
}
