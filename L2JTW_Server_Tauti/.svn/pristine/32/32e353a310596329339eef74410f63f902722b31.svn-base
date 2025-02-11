package com.l2jserver.gameserver.network.serverpackets;

/**
 * Created by IntelliJ IDEA.
 * User: Keiichi
 * Date: 07.06.2011
 * Time: 0:42:00
 * To change this template use File | Settings | File Templates.
 */
public class ExGetCrystalizingFail extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0xe2);//449 Test
	}
	
	/* (non-Javadoc)
	 * @see com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket#getType()
	 */
	@Override
	public String getType()
	{
		return "[S] FE:E1 ExGetCrystalizingFail";
	}
}
