package com.l2jserver.gameserver.network.serverpackets;

import com.l2jserver.gameserver.instancemanager.JumpManager.JumpNode;
import com.l2jserver.gameserver.instancemanager.JumpManager.JumpWay;

/**
 * Created by IntelliJ IDEA. User: Keiichi Date: 27.05.2011 Time: 12:06:19 To change this template use File | Settings | File Templates.
 */
public class ExFlyMove extends L2GameServerPacket
{
	private static final String _S__FE_E7_EXFLYMOVE = "[S] FE:E7 ExFlyMove";
	
	int _objId;
	int _trackId;
	JumpWay _jw;
	
	public ExFlyMove(int objid, int trackId, JumpWay jw)
	{
		_objId = objid;
		_trackId = trackId;
		_jw = jw;
	}
	
	@Override
	protected void writeImpl()
	{
		if (_jw == null)
		{
			return;
		}
		writeC(0xfe);
		writeH(0xe8);//449 Test
		writeD(_objId);
		
		if (_jw.size() == 1)
		{
			writeD(2);
			
		}
		else
		{
			writeD(0);
		}
		writeD(0);
		writeD(_trackId);
		
		writeD(_jw.size());
		for (JumpNode n : _jw)
		{
			writeD(n.getNext());
			writeD(0);
			
			writeD(n.getX());
			writeD(n.getY());
			writeD(n.getZ());
			
		}
	}
	
	@Override
	public String getType()
	{
		return _S__FE_E7_EXFLYMOVE;
	}
}
