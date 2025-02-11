package com.l2jserver.gameserver.network.serverpackets;

import com.l2jserver.gameserver.model.actor.L2Character;

/**
 * RusDev
 * @author OSTIN
 */
public class ExFlyMoveBroadcast extends L2GameServerPacket
{
	private static final String _S__FE_10C_EXFLYMOVEBROADCAST = "[S] FE:10C ExFlyMoveBroadcast";
	int _objId, _x, _y, _z, _xDest, _yDest, _zDest;
	
	public ExFlyMoveBroadcast(L2Character cha)
	{
		_objId = cha.getObjectId();
		_x = cha.getXdestination();
		_y = cha.getYdestination();
		_z = cha.getZdestination();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0x10D);//449 Test
		writeD(_objId);
		
		writeD(0x02);
		writeD(0x00);
		
		writeD(_xDest);
		writeD(_yDest);
		writeD(_zDest);
		writeD(0x00);
		
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_10C_EXFLYMOVEBROADCAST;
	}
}
