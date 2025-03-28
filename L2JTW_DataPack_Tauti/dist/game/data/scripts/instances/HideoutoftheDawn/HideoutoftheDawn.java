/*
By knoxville OpenTeamFree 10.09.2010, Based on PTS Freya.
*/

package instances.HideoutoftheDawn;

import com.l2jserver.gameserver.instancemanager.InstanceManager;
import com.l2jserver.gameserver.instancemanager.InstanceManager.InstanceWorld;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.ai.CtrlIntention;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;


public class HideoutoftheDawn extends Quest
{

	private class HoDWorld extends InstanceWorld
	{
		public long[] storeTime = {0,0}; 
		public HoDWorld()
		{
			//InstanceManager.getInstance().super();
		}
	}

	private static final String qn = "HideoutoftheDawn";
	private static final int INSTANCEID = 113; 

	private static final int WOOD = 32593;
	private static final int JAINA = 32582;  

	private class teleCoord {int instanceId; int x; int y; int z;}

	
	private void teleportplayer(L2PcInstance player, teleCoord teleto)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(teleto.instanceId);
		player.teleToLocation(teleto.x, teleto.y, teleto.z);
		return;
	}
	protected void exitInstance(L2PcInstance player, teleCoord tele)
	{
		player.setInstanceId(0);
		player.teleToLocation(tele.x, tele.y, tele.z);
	}
	protected int enterInstance(L2PcInstance player, String template, teleCoord teleto)
	{
		int instanceId = 0;
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null)
		{
			if (!(world instanceof HoDWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return 0;
			}
			teleto.instanceId = world.instanceId;
			teleportplayer(player,teleto);
			return instanceId;
		}
		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		world = new HoDWorld();
		world.instanceId = instanceId;
		world.templateId = INSTANCEID;
		world.status = 0;
		((HoDWorld)world).storeTime[0] = System.currentTimeMillis();
		InstanceManager.getInstance().addWorld(world);
		_log.info("HideoutoftheDawn started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
		teleto.instanceId = instanceId;
		teleportplayer(player,teleto);
		world.allowed.add(player.getObjectId());
		return instanceId;
	}

	@Override
	public String onTalk ( L2Npc npc, L2PcInstance player)
	{
		int npcId = npc.getNpcId();
		QuestState st = player.getQuestState(qn);
		if (st == null)
			st = newQuestState(player);
		if (npcId == WOOD)
		{
			teleCoord tele = new teleCoord();
			tele.x = -23759;      
			tele.y = -8976;
			tele.z = -5385;
			enterInstance(player, "HideoutoftheDawn.xml", tele);
		}
		else if (npcId == JAINA)
		{
			InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
			world.allowed.remove(world.allowed.indexOf(player.getObjectId()));
			teleCoord tele = new teleCoord();
			tele.instanceId = 0;
			tele.x = 147081;    
			tele.y = 23785;
			tele.z = -1990;
			exitInstance(player,tele);
		}
		return "";
	
	}

	public HideoutoftheDawn(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(WOOD);
		addTalkId(WOOD);
		addTalkId(JAINA);
	}

	public static void main(String[] args)
	{
		new HideoutoftheDawn(-1,qn,"instances");
	}
}