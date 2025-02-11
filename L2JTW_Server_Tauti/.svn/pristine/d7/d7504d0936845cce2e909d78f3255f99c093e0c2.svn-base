package com.l2jserver.gameserver.network.clientpackets;

import javolution.util.FastList;

import com.l2jserver.Config;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.base.CrystallizationItem;
import com.l2jserver.gameserver.model.base.Race;
import com.l2jserver.gameserver.model.itemcontainer.PcInventory;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.model.skills.L2Skill;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ActionFailed;
import com.l2jserver.gameserver.network.serverpackets.ExGetCrystalizingEstimation;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;
import com.l2jserver.gameserver.util.Util;

/**
 * Created by IntelliJ IDEA.
 * User: Keiichi
 * Date: 28.05.2011
 * Time: 14:01:33
 * To change this template use File | Settings | File Templates.
 */
public class RequestCrystallizeEstimate extends L2GameClientPacket
{
	private static final String _C__D0_91_REQUESTCRYSTALIZEESTIMATE = "[C] D0:91 RequestCrystallizeEstimate";
	private FastList<CrystallizationItem> products;
	
	private int _objectId;
	private long _count;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_count = readQ();
	}
	
	@Override
	protected void runImpl()
	{
		if(products == null)
		{
			products = new FastList<CrystallizationItem>();
		}
		
		L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			_log.fine("RequestCrystallizeEstimate: activeChar was null");
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("crystallize"))
		{
			activeChar.sendMessage("You crystallizing too fast.");
			return;
		}
		
		if (_count <= 0)
		{
			Util.handleIllegalPlayerAction(activeChar, "[RequestCrystallizeItem] count <= 0! ban! oid: " + _objectId + " owner: " + activeChar.getName(), Config.DEFAULT_PUNISH);
			return;
		}
		
		if (activeChar.getPrivateStoreType() != 0 || activeChar.isInCrystallize())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
			return;
		}
		
		int skillLevel = activeChar.getSkillLevel(L2Skill.SKILL_CRYSTALLIZE);
		if (skillLevel <= 0)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			if (activeChar.getRace() != Race.Dwarf && activeChar.getClassId().ordinal() != 117 && activeChar.getClassId().ordinal() != 55)
				_log.info("Player "+activeChar.getClient()+" used crystalize with classid: "+activeChar.getClassId().ordinal());
			return;
		}
		
		PcInventory inventory = activeChar.getInventory();
		if (inventory != null)
		{
			L2ItemInstance item = inventory.getItemByObjectId(_objectId);
			if (item == null)
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (item.isHeroItem())
				return;
			
			if (_count > item.getCount())
				_count = activeChar.getInventory().getItemByObjectId(_objectId).getCount();
		}
		
		L2ItemInstance itemToRemove = activeChar.getInventory().getItemByObjectId(_objectId);
		if (itemToRemove == null
				|| itemToRemove.isShadowItem()
				|| itemToRemove.isTimeLimitedItem())
			return;
		
		if (/*!itemToRemove.getItem().isCrystallizable() || */(itemToRemove.getItem().getCrystalCount() < 0) || (itemToRemove.getItem().getCrystalType() == L2Item.CRYSTAL_NONE)) //rocknow-temp fix
		{
			_log.warning(activeChar.getName() + " (" + activeChar.getObjectId() + ") tried to crystallize " + itemToRemove.getItem().getItemId());
			return;
		}
		
		if (!activeChar.getInventory().canManipulateWithItemId(itemToRemove.getItemId()))
		{
			activeChar.sendMessage("Cannot use this item.");
			return;
		}
		
		// Check if the char can crystallize items and return if false;
		boolean canCrystallize = true;
		
		switch (itemToRemove.getItem().getItemGradeSPlus())
		{
			case L2Item.CRYSTAL_C:
			{
				if (skillLevel <= 1)
					canCrystallize = false;
				break;
			}
			case L2Item.CRYSTAL_B:
			{
				if (skillLevel <= 2)
					canCrystallize = false;
				break;
			}
			case L2Item.CRYSTAL_A:
			{
				if (skillLevel <= 3)
					canCrystallize = false;
				break;
			}
			case L2Item.CRYSTAL_S:
			{
				if (skillLevel <= 4)
					canCrystallize = false;
				break;
			}
			case L2Item.CRYSTAL_R:
			{
				if (skillLevel <= 5)
					canCrystallize = false;
				break;
			}
			case L2Item.CRYSTAL_R95:
			{
				if (skillLevel <= 6)
					canCrystallize = false;
				break;
			}
			case L2Item.CRYSTAL_R99:
			{
				if (skillLevel <= 7)
					canCrystallize = false;
				break;
			}
		}
		
		if (!canCrystallize)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// add crystals
		int crystalId = itemToRemove.getItem().getCrystalItemId();
		int crystalAmount = itemToRemove.getCrystalCount();
		
		
		CrystallizationItem item = new CrystallizationItem(crystalId, crystalAmount, 100);
		products.add(item);
		activeChar.sendPacket(new ExGetCrystalizingEstimation(products));
	}
	
	@Override
	public String getType()
	{
		return _C__D0_91_REQUESTCRYSTALIZEESTIMATE;
	}
}
