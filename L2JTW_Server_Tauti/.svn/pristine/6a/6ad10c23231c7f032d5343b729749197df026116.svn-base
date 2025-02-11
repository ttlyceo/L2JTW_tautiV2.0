package com.l2jserver.gameserver.network.serverpackets;

public class ExCallToChangeClass extends L2GameServerPacket
{
	private static final String _S__FE_FD_EXCALLTOCHANGECLASS = "[S] FE:FD ExCallToChangeClass";
	private int _classId;
	public ExCallToChangeClass(int classId)
	{
		classId = _classId;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0xfe);//449 Test
		writeD(_classId);
		writeD(0); // 0 icon, window-the request to change the Saba 1, 2 and TD-0
	}
	
	@Override
	public String getType()
	{
		return _S__FE_FD_EXCALLTOCHANGECLASS;
	}
}
