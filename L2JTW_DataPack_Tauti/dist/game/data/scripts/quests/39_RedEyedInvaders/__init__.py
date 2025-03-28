# Made by disKret, as a part of the
# Official L2J Datapack Project, please visit
# http://www.l2jdp.com/forum/ to meet the community behind it, or
# http://l2jdp.com/trac if you need to report a bug.
# Update by pmq 09-07-2010

import sys
from com.l2jserver import Config 
from com.l2jserver.gameserver.model.quest import State
from com.l2jserver.gameserver.model.quest import QuestState
from com.l2jserver.gameserver.model.quest.jython import QuestJython as JQuest

qn = "39_RedEyedInvaders"

# NPC
BABENCO = 30334
BATHIS  = 30332

# MOBS
M_LIZARDMAN       = 20919
M_LIZARDMAN_SCOUT = 20920
M_LIZARDMAN_GUARD = 20921
ARANEID           = 20925

# QUEST DROPS
BLACK_BONE_NECKLACE,RED_BONE_NECKLACE,INCENSE_POUCH,GEM_OF_MAILLE = range(7178,7182)

NECKLACE = {M_LIZARDMAN_GUARD:[RED_BONE_NECKLACE,100,BLACK_BONE_NECKLACE,"3"],
			M_LIZARDMAN:[BLACK_BONE_NECKLACE,100,RED_BONE_NECKLACE,"3"],
			M_LIZARDMAN_SCOUT:[BLACK_BONE_NECKLACE,100,RED_BONE_NECKLACE,"3"]
}
DROPLIST = {ARANEID:[GEM_OF_MAILLE,30,INCENSE_POUCH,"5"],
			M_LIZARDMAN_GUARD:[INCENSE_POUCH,30,GEM_OF_MAILLE,"5"],
			M_LIZARDMAN_SCOUT:[INCENSE_POUCH,30,GEM_OF_MAILLE,"5"]
}
# REWARDS
GREEN_COLORED_LURE_HG = 6521
BABY_DUCK_RODE        = 6529
FISHING_SHOT_NG       = 6535

def drop(partyMember,array) :
	item,max,item2,condition = array
	st = partyMember.getQuestState(qn)
	count = st.getQuestItemsCount(item)
	numItems,chance = divmod(100*Config.RATE_QUEST_DROP,100)
	if st.getQuest().getRandom(100) < chance :
		numItems = numItems + 1
	if count+numItems > max :
		numItems = max - count
	st.giveItems(item,int(numItems))
	if st.getQuestItemsCount(item) == max and st.getQuestItemsCount(item2) == max:
		st.playSound("ItemSound.quest_middle")
		st.set("cond",condition)
	else:
		st.playSound("ItemSound.quest_itemget")
	return

class Quest (JQuest) :

	def __init__(self,id,name,descr):
		JQuest.__init__(self,id,name,descr)
		self.questItemIds = range(7178,7182)

	def onAdvEvent (self,event,npc, player) :
		htmltext = event
		st = player.getQuestState(qn)
		if not st : return
		if event == "30334-1.htm" :
			st.set("cond","1")
			st.playSound("ItemSound.quest_accept")
			st.setState(State.STARTED)
		elif event == "30332-1.htm" :
			st.set("cond","2")
			st.playSound("ItemSound.quest_accept")
		elif event == "30332-3a.htm" :
			if st.getQuestItemsCount(BLACK_BONE_NECKLACE) == 100 and st.getQuestItemsCount(RED_BONE_NECKLACE) == 100 :
				st.takeItems(BLACK_BONE_NECKLACE,100)
				st.takeItems(RED_BONE_NECKLACE,100)
				st.set("cond","4")
				st.playSound("ItemSound.quest_accept")
			else :
				htmltext = "道具不符。"
		elif event == "30332-5.htm" :
			if st.getQuestItemsCount(INCENSE_POUCH) == 30 and st.getQuestItemsCount(GEM_OF_MAILLE) == 30 :
				st.takeItems(INCENSE_POUCH,30)
				st.takeItems(GEM_OF_MAILLE,30)
				st.giveItems(GREEN_COLORED_LURE_HG,60)
				st.giveItems(BABY_DUCK_RODE,1)
				st.giveItems(FISHING_SHOT_NG,500)
				st.addExpAndSp(62366,2783)
				st.unset("cond")
				st.exitQuest(False)
				st.playSound("ItemSound.quest_finish")
			else :
				htmltext = "道具不符。"
		return htmltext

	def onTalk (self,npc,player):
		htmltext = "<html><body>目前沒有執行任務，或條件不符。</body></html>"
		st = player.getQuestState(qn)
		if not st : return htmltext

		npcId = npc.getNpcId()
		id = st.getState()
		cond = st.getInt("cond")

		if id == State.COMPLETED :
			htmltext = "<html><body>這是已經完成的任務。</body></html>"
		elif id == State.CREATED :
			if npcId == BABENCO and cond == 0 :
				if player.getLevel() >= 20 :
					htmltext = "30334-0.htm"
				else :
					htmltext = "30334-2.htm"
					st.exitQuest(1)
		elif id == State.STARTED:
			if npcId == BABENCO :
				if cond == 1 :
					htmltext = "30334-3.htm"
			elif npcId == BATHIS :
				if cond == 1 :
					htmltext = "30332-0.htm"
				elif cond == 2 :
					htmltext = "30332-2.htm"
				elif cond == 3 :
					htmltext = "30332-3.htm"
				elif cond == 4 :
					htmltext = "30332-3b.htm"
				elif cond == 5 :
					htmltext = "30332-4.htm"
		return htmltext

	def onKill(self,npc,player,isPet):
		npcId = npc.getNpcId()
		partyMember = self.getRandomPartyMember(player,"2")
		if (partyMember and npcId != ARANEID) :
			drop(partyMember,NECKLACE[npcId])
		else:
			partyMember = self.getRandomPartyMember(player,"4")
			if (partyMember and npcId != M_LIZARDMAN) :     
				drop(partyMember,DROPLIST[npcId])
		return

QUEST		= Quest(39,qn,"紅眼的侵略者")

QUEST.addStartNpc(BABENCO)

QUEST.addTalkId(BABENCO)
QUEST.addTalkId(BATHIS)

QUEST.addKillId(M_LIZARDMAN)
QUEST.addKillId(M_LIZARDMAN_SCOUT)
QUEST.addKillId(M_LIZARDMAN_GUARD)
QUEST.addKillId(ARANEID)