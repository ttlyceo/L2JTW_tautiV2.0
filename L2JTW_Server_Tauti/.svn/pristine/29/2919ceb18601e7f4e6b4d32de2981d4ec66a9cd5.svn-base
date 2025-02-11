package com.l2jserver.gameserver.network.serverpackets;

import javolution.util.FastList;
import com.l2jserver.gameserver.model.base.CrystallizationItem;

/**
 * @author sgteam 
 * Goddess of Destruction 
 */
public class ExGetCrystalizingEstimation extends L2GameServerPacket
{
	private static final String _S__FE_E0_EXGETCRYSTALIZINGESTIMATION = "[S] FE:E0 ExGetCrystalizingEstimation";
	
	private FastList<CrystallizationItem> products;
	
	public ExGetCrystalizingEstimation(FastList<CrystallizationItem> products)
	{
		this.products = products;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xfe);
		writeH(0xe1);//449 Test
		
		writeD(products.size());
		for(CrystallizationItem item : products)
		{
			writeD(item.getItemId());
			writeQ(item.getCount());
			writeF(item.getProb());
		}
		FastList.recycle(products);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_E0_EXGETCRYSTALIZINGESTIMATION;
	}
}
