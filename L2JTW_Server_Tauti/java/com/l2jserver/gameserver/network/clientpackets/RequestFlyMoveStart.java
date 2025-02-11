package com.l2jserver.gameserver.network.clientpackets;

import com.l2jserver.gameserver.instancemanager.JumpManager;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

public final class RequestFlyMoveStart extends L2GameClientPacket
{
	private static final String _C__D0_B4_REQUESTFLYMOVESTART = "[C] D0:B4 RequestFlyMoveStart";
	
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
			
		JumpManager.getInstance().StartJump(activeChar);
	}
	
	/* (non-Javadoc)
	 * @see com.l2jserver.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__D0_B4_REQUESTFLYMOVESTART;
	}
}
