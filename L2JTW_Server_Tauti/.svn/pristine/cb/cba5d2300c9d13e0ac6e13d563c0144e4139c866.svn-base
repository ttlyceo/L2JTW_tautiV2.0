package com.l2jserver.gameserver.network.serverpackets;

/**
 * 
 * @author mrTJO
 */
public class ExShowUsmVideo extends L2GameServerPacket
{
	
	private static final String _S__FE_10D_EXSHOWUSM = "[S] FE:10D ExShowUsm";
	int _usmVideo;
	
	public static int GD1_INTRO = 2;
	
	public static int Q001 = 0x01;
	public static int Q002 = 0x03;
	public static int Q003 = 0x04;
	public static int Q004 = 0x05;
	public static int Q005 = 0x06;
	public static int Q006 = 0x07;
	public static int Q007 = 0x08;
	public static int Q009 = 0x09;
	public static int Q010 = 0x0A;
	
	public static int AWAKE_1 = 0x8B;
	public static int AWAKE_2 = 0x8C;
	public static int AWAKE_3 = 0x8D;
	public static int AWAKE_4 = 0x8E;
	public static int AWAKE_5 = 0x8F;
	public static int AWAKE_6 = 0x90;
	public static int AWAKE_7 = 0x91;
	public static int AWAKE_8 = 0x92;
	
	public ExShowUsmVideo(int usmVideo)
	{
		_usmVideo = usmVideo;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x10E);//449 Test
		writeD(_usmVideo);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_10D_EXSHOWUSM;
	}
}
