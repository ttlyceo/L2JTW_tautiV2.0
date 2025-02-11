package com.l2jserver.gameserver.model.base;

/**
 * Created by IntelliJ IDEA.
 * User: Keiichi
 * Date: 28.05.2011
 * Time: 12:52:19
 * To change this template use File | Settings | File Templates.
 */
public final class CrystallizationItem
{
	private final int _itemId;
	private final int _count;
	private final int _prob;
	
	public CrystallizationItem(int itemId, int count, int prob)
	{
		_itemId = itemId;
		_count = count;
		_prob = prob;
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public int getCount()
	{
		return _count;
	}
	
	public int getProb()
	{
		return _prob;
	}
}
