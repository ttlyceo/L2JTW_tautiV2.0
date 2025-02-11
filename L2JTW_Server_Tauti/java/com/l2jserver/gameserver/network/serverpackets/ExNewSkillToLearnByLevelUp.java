package com.l2jserver.gameserver.network.serverpackets;

/**
 * Created by IntelliJ IDEA.
 * User: Keiichi
 * Date: 07.06.2011
 * Time: 0:26:41
 * To change this template use File | Settings | File Templates.
 */
public class ExNewSkillToLearnByLevelUp extends L2GameServerPacket
{
	public ExNewSkillToLearnByLevelUp()
	{
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xfe);
		writeH(0xfd);//449 Test
	}
	
	@Override
	public final String getType()
	{
		return "[S] FE:FC ExNewSkillToLearnByLevelUp";
	}
}
