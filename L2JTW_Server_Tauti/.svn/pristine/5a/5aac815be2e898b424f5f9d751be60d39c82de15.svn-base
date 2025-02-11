package com.l2jserver.gameserver.network.serverpackets;

/**
 * Created by IntelliJ IDEA.
 * User: Keiichi
 * Date: 07.06.2011
 * Time: 0:28:22
 * To change this template use File | Settings | File Templates.
 */
public class ExChangeToAwakenedClass extends L2GameServerPacket
{
	private static final String _S__FE_FE_EXCHANGETOAWAKENEDCLASS = "[S] FE:FE ExChangeToAwakenedClass";
	
	private int _newId;
	
	public ExChangeToAwakenedClass(int newId)
	{
		_newId = newId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xfe);
		writeH(0xff);//449 Test
		writeD(_newId);
		writeD(0x00);
	}
	
	@Override
	public final String getType()
	{
		return _S__FE_FE_EXCHANGETOAWAKENEDCLASS;
	}
}
