/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.model.actor.instance;

import java.util.StringTokenizer;
import java.util.logging.Level;

import javolution.text.TextBuilder;

import com.l2jserver.Config;
import com.l2jserver.gameserver.SevenSigns;
import com.l2jserver.gameserver.cache.HtmCache;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.templates.L2NpcTemplate;
import com.l2jserver.gameserver.model.itemcontainer.PcInventory;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;
import com.l2jserver.gameserver.datatables.MessageTable;

/**
 * Dawn/Dusk Seven Signs Priest Instance
 *
 * @author Tempy
 */
public class L2SignsPriestInstance extends L2Npc
{
	public L2SignsPriestInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2SignsPriestInstance);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (player.getLastFolkNPC() == null || player.getLastFolkNPC().getObjectId() != getObjectId())
			return;
		
		if (command.startsWith("SevenSignsDesc"))
		{
			int val = Integer.parseInt(command.substring(15));
			showChatWindow(player, val, null, true);
		}
		else if (command.startsWith("SevenSigns"))
		{
			SystemMessage sm;
			
			String path;
			
			int cabal = SevenSigns.CABAL_NULL;
			int stoneType = 0;
			
			final long ancientAdenaAmount = player.getAncientAdena();
			
			int val = Integer.parseInt(command.substring(11, 12).trim());
			
			if (command.length() > 12) // SevenSigns x[x] x [x..x]
				val = Integer.parseInt(command.substring(11, 13).trim());
			
			if (command.length() > 13)
			{
				try
				{
					cabal = Integer.parseInt(command.substring(14, 15).trim());
				}
				catch (Exception e)
				{
					try
					{
						cabal = Integer.parseInt(command.substring(13, 14).trim());
					}
					catch (Exception e2)
					{
						try
						{
							StringTokenizer st = new StringTokenizer(command.trim());
							st.nextToken();
							cabal = Integer.parseInt(st.nextToken());
						}
						catch (Exception e3)
						{
							_log.warning("Failed to retrieve cabal from bypass command. NpcId: " + getNpcId() + "; Command: " + command);
						}
					}
				}
			}
			
			switch (val)
			{
				case 2: // Purchase Record of the Seven Signs
					if (!player.getInventory().validateCapacity(1))
					{
						player.sendPacket(SystemMessageId.SLOTS_FULL);
						break;
					}
					// Update by pmq-Start
					if (!player.reduceAdena("SevenSigns", SevenSigns.RECORD_SEVEN_SIGNS_COST, this, false))
					{
						if (this instanceof L2DawnPriestInstance)
							showChatWindow(player, val, "dawn_noadena", false);
						else
							showChatWindow(player, val, "dusk_noadena", false);
					//Update by pmq-End
						break;
					}
					player.getInventory().addItem("SevenSigns", SevenSigns.RECORD_SEVEN_SIGNS_ID, 1, player, this);
					
					sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
					sm.addItemName(SevenSigns.RECORD_SEVEN_SIGNS_ID);
					player.sendPacket(sm);
					
					if (this instanceof L2DawnPriestInstance)
						showChatWindow(player, val, "dawn", false);
					else
						showChatWindow(player, val, "dusk", false);
					break;
				case 33: // "I want to participate" request
					int oldCabal = SevenSigns.getInstance().getPlayerCabal(player.getObjectId());
					
					if (oldCabal != SevenSigns.CABAL_NULL)
					{
						if (this instanceof L2DawnPriestInstance)
							showChatWindow(player, val, "dawn_member", false);
						else
							showChatWindow(player, val, "dusk_member", false);
						return;
					}
					else if (player.getClassId().level() == 0)
					{
						if (this instanceof L2DawnPriestInstance)
							showChatWindow(player, val, "dawn_firstclass", false);
						else
							showChatWindow(player, val, "dusk_firstclass", false);
						return;
					}
					else if (cabal == SevenSigns.CABAL_DUSK && Config.ALT_GAME_CASTLE_DUSK) //dusk
					{
						// castle owners cannot participate with dusk side
						if (player.getClan() != null && player.getClan().getCastleId() > 0)
						{
							showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_33_dusk_no.htm");
							break;
						}
					}
					else if (cabal == SevenSigns.CABAL_DAWN && Config.ALT_GAME_CASTLE_DAWN) //dawn
					{
						// clans without castle need to pay participation fee
						if (player.getClan() == null || player.getClan().getCastleId() == 0)
						{
							showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_33_dawn_fee.htm");
							break;
						}
					}
					
					if (this instanceof L2DawnPriestInstance)
						showChatWindow(player, val, "dawn", false);
					else
						showChatWindow(player, val, "dusk", false);
					break;
				case 34: // Pay the participation fee request
					L2ItemInstance adena = player.getInventory().getItemByItemId(PcInventory.ADENA_ID); //adena
					L2ItemInstance certif = player.getInventory().getItemByItemId(6388); //Lord of the Manor's Certificate of Approval
					boolean fee = true;
					
					if (player.getClassId().level() < 2 || (adena != null && adena.getCount() >= SevenSigns.ADENA_JOIN_DAWN_COST) || (certif != null && certif.getCount() >= 1))
						fee = false;
					if (fee)
					{
						showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_33_dawn_no.htm");
					}
					else
					{
						showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_33_dawn.htm");
					}
					break;
				case 3: // Join Cabal Intro 1
				case 8: // Festival of Darkness Intro - SevenSigns x [0]1
					showChatWindow(player, val, SevenSigns.getCabalShortName(cabal), false);
					break;
				case 4: // Join a Cabal - SevenSigns 4 [0]1 x
					int newSeal = Integer.parseInt(command.substring(15));
					
					if (player.getClassId().level() >= 2)
					{
						if (cabal == SevenSigns.CABAL_DUSK && Config.ALT_GAME_CASTLE_DUSK)
						{
							if (player.getClan() != null && player.getClan().getCastleId() > 0) // even if in htmls is said that ally can have castle too, but its not
							{
								showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_33_dusk_no.htm");
								return;
							}
						}
						/*
						 * If the player is trying to join the Lords of Dawn, check if they are
						 * carrying a Lord's certificate.
						 *
						 * If not then try to take the required amount of adena instead.
						 */
						if (Config.ALT_GAME_CASTLE_DAWN && cabal == SevenSigns.CABAL_DAWN)
						{
							boolean allowJoinDawn = false;
							
							if (player.getClan() != null && player.getClan().getCastleId() > 0) // castle owner don't need to pay anything
								allowJoinDawn = true;
							else if (player.destroyItemByItemId("SevenSigns", SevenSigns.CERTIFICATE_OF_APPROVAL_ID, 1, this, true))
								allowJoinDawn = true;
							else if (player.reduceAdena("SevenSigns", SevenSigns.ADENA_JOIN_DAWN_COST, this, true))
								allowJoinDawn = true;
							
							if (!allowJoinDawn)
							{
								showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_33_dawn_fee.htm");
								return;
							}
						}
					}
					SevenSigns.getInstance().setPlayerInfo(player.getObjectId(), cabal, newSeal);
					
					if (cabal == SevenSigns.CABAL_DAWN)
						player.sendPacket(SystemMessageId.SEVENSIGNS_PARTECIPATION_DAWN); // Joined Dawn
					else
						player.sendPacket(SystemMessageId.SEVENSIGNS_PARTECIPATION_DUSK); // Joined Dusk
					
					// Show a confirmation message to the user, indicating which seal they chose.
					switch (newSeal)
					{
						case SevenSigns.SEAL_AVARICE:
							player.sendPacket(SystemMessageId.FIGHT_FOR_AVARICE);
							break;
						case SevenSigns.SEAL_GNOSIS:
							player.sendPacket(SystemMessageId.FIGHT_FOR_GNOSIS);
							break;
						case SevenSigns.SEAL_STRIFE:
							player.sendPacket(SystemMessageId.FIGHT_FOR_STRIFE);
							break;
					}
					
					showChatWindow(player, 4, SevenSigns.getCabalShortName(cabal), false);
					break;
				case 5:
					if (this instanceof L2DawnPriestInstance)
					{
						if (SevenSigns.getInstance().getPlayerCabal(player.getObjectId()) == SevenSigns.CABAL_NULL)
							showChatWindow(player, val, "dawn_no", false);
						else
							showChatWindow(player, val, "dawn", false);
					}
					else
					{
						if (SevenSigns.getInstance().getPlayerCabal(player.getObjectId()) == SevenSigns.CABAL_NULL)
							showChatWindow(player, val, "dusk_no", false);
						else
							showChatWindow(player, val, "dusk", false);
					}
					break;
				case 21:
					int contribStoneId = Integer.parseInt(command.substring(14, 18));
					
					L2ItemInstance contribBlueStones = player.getInventory().getItemByItemId(SevenSigns.SEAL_STONE_BLUE_ID);
					L2ItemInstance contribGreenStones = player.getInventory().getItemByItemId(SevenSigns.SEAL_STONE_GREEN_ID);
					L2ItemInstance contribRedStones = player.getInventory().getItemByItemId(SevenSigns.SEAL_STONE_RED_ID);
					
					long contribBlueStoneCount = contribBlueStones == null ? 0 : contribBlueStones.getCount();
					long contribGreenStoneCount = contribGreenStones == null ? 0 : contribGreenStones.getCount();
					long contribRedStoneCount = contribRedStones == null ? 0 : contribRedStones.getCount();
					
					long score = SevenSigns.getInstance().getPlayerContribScore(player.getObjectId());
					long contributionCount = 0;
					
					boolean contribStonesFound = false;
					
					long redContrib = 0;
					long greenContrib = 0;
					long blueContrib = 0;
					
					try
					{
						contributionCount = Long.parseLong(command.substring(19).trim());
					}
					catch (Exception NumberFormatException)
					{
						if (this instanceof L2DawnPriestInstance)
							showChatWindow(player, 6, "dawn_failure", false);
						else
							showChatWindow(player, 6, "dusk_failure", false);
						break;
					}
					
					switch (contribStoneId)
					{
						case SevenSigns.SEAL_STONE_BLUE_ID:
							blueContrib = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - score) / SevenSigns.BLUE_CONTRIB_POINTS;
							if (blueContrib > contribBlueStoneCount)
								blueContrib = contributionCount;
							break;
						case SevenSigns.SEAL_STONE_GREEN_ID:
							greenContrib = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - score) / SevenSigns.GREEN_CONTRIB_POINTS;
							if (greenContrib > contribGreenStoneCount)
								greenContrib = contributionCount;
							break;
						case SevenSigns.SEAL_STONE_RED_ID:
							redContrib = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - score) / SevenSigns.RED_CONTRIB_POINTS;
							if (redContrib > contribRedStoneCount)
								redContrib = contributionCount;
							break;
					}
					
					if (redContrib > 0)
					{
						if (player.destroyItemByItemId("SevenSigns", SevenSigns.SEAL_STONE_RED_ID, redContrib, this, false))
						{
							contribStonesFound = true;
							SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
							msg.addItemName(SevenSigns.SEAL_STONE_RED_ID);
							msg.addItemNumber(redContrib);
							player.sendPacket(msg);
						}
					}
					if (greenContrib > 0)
					{
						if (player.destroyItemByItemId("SevenSigns", SevenSigns.SEAL_STONE_GREEN_ID, greenContrib, this, false))
						{
							contribStonesFound = true;
							SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
							msg.addItemName(SevenSigns.SEAL_STONE_GREEN_ID);
							msg.addItemNumber(greenContrib);
							player.sendPacket(msg);
						}
					}
					if (blueContrib > 0)
					{
						if (player.destroyItemByItemId("SevenSigns", SevenSigns.SEAL_STONE_BLUE_ID, blueContrib, this, false))
						{
							contribStonesFound = true;
							SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
							msg.addItemName(SevenSigns.SEAL_STONE_BLUE_ID);
							msg.addItemNumber(blueContrib);
							player.sendPacket(msg);
						}
					}
					
					if (!contribStonesFound)
					{
						if (this instanceof L2DawnPriestInstance)
							showChatWindow(player, 6, "dawn_low_stones", false);
						else
							showChatWindow(player, 6, "dusk_low_stones", false);
					}
					else
					{
						score = SevenSigns.getInstance().addPlayerStoneContrib(player.getObjectId(), blueContrib, greenContrib, redContrib);
						sm = SystemMessage.getSystemMessage(SystemMessageId.CONTRIB_SCORE_INCREASED_S1);
						sm.addItemNumber(score);
						player.sendPacket(sm);
						
						if (this instanceof L2DawnPriestInstance)
							showChatWindow(player, 6, "dawn", false);
						else
							showChatWindow(player, 6, "dusk", false);
					}
					break;
				case 6: // Contribute Seal Stones - SevenSigns 6 x
					stoneType = Integer.parseInt(command.substring(13));
					
					L2ItemInstance blueStones = player.getInventory().getItemByItemId(SevenSigns.SEAL_STONE_BLUE_ID);
					L2ItemInstance greenStones = player.getInventory().getItemByItemId(SevenSigns.SEAL_STONE_GREEN_ID);
					L2ItemInstance redStones = player.getInventory().getItemByItemId(SevenSigns.SEAL_STONE_RED_ID);
					
					long blueStoneCount = blueStones == null ? 0 : blueStones.getCount();
					long greenStoneCount = greenStones == null ? 0 : greenStones.getCount();
					long redStoneCount = redStones == null ? 0 : redStones.getCount();
					
					long contribScore = SevenSigns.getInstance().getPlayerContribScore(player.getObjectId());
					boolean stonesFound = false;
					
					if (contribScore == Config.ALT_MAXIMUM_PLAYER_CONTRIB)
					{
						player.sendPacket(SystemMessageId.CONTRIB_SCORE_EXCEEDED);
					}
					else
					{
						long redContribCount = 0;
						long greenContribCount = 0;
						long blueContribCount = 0;
						
						String contribStoneColor = null;
						String stoneColorContr = null;
						
						long stoneCountContr = 0;
						int stoneIdContr = 0;
						
						switch (stoneType)
						{
							case 1:
								/* Move To MessageTable For L2JTW
								contribStoneColor = "Blue";
								stoneColorContr = "blue";
								*/
								contribStoneColor = MessageTable.Messages[740].getMessage();
								stoneColorContr = MessageTable.Messages[741].getMessage();
								stoneIdContr = SevenSigns.SEAL_STONE_BLUE_ID;
								stoneCountContr = blueStoneCount;
								break;
							case 2:
								/* Move To MessageTable For L2JTW
								contribStoneColor = "Green";
								stoneColorContr = "green";
								*/
								contribStoneColor = MessageTable.Messages[742].getMessage();
								stoneColorContr = MessageTable.Messages[743].getMessage();
								stoneIdContr = SevenSigns.SEAL_STONE_GREEN_ID;
								stoneCountContr = greenStoneCount;
								break;
							case 3:
								/* Move To MessageTable For L2JTW
								contribStoneColor = "Red";
								stoneColorContr = "red";
								*/
								contribStoneColor = MessageTable.Messages[744].getMessage();
								stoneColorContr = MessageTable.Messages[745].getMessage();
								stoneIdContr = SevenSigns.SEAL_STONE_RED_ID;
								stoneCountContr = redStoneCount;
								break;
							case 4:
								long tempContribScore = contribScore;
								redContribCount = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - tempContribScore) / SevenSigns.RED_CONTRIB_POINTS;
								if (redContribCount > redStoneCount)
									redContribCount = redStoneCount;
								
								tempContribScore += redContribCount * SevenSigns.RED_CONTRIB_POINTS;
								greenContribCount = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - tempContribScore) / SevenSigns.GREEN_CONTRIB_POINTS;
								if (greenContribCount > greenStoneCount)
									greenContribCount = greenStoneCount;
								
								tempContribScore += greenContribCount * SevenSigns.GREEN_CONTRIB_POINTS;
								blueContribCount = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - tempContribScore) / SevenSigns.BLUE_CONTRIB_POINTS;
								if (blueContribCount > blueStoneCount)
									blueContribCount = blueStoneCount;
								
								if (redContribCount > 0)
								{
									if (player.destroyItemByItemId("SevenSigns", SevenSigns.SEAL_STONE_RED_ID, redContribCount, this, false))
									{
										stonesFound = true;
										SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
										msg.addItemName(SevenSigns.SEAL_STONE_RED_ID);
										msg.addItemNumber(redContribCount);
										player.sendPacket(msg);
									}
								}
								if (greenContribCount > 0)
								{
									if (player.destroyItemByItemId("SevenSigns", SevenSigns.SEAL_STONE_GREEN_ID, greenContribCount, this, false))
									{
										stonesFound = true;
										SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
										msg.addItemName(SevenSigns.SEAL_STONE_GREEN_ID);
										msg.addItemNumber(greenContribCount);
										player.sendPacket(msg);
									}
								}
								if (blueContribCount > 0)
								{
									if (player.destroyItemByItemId("SevenSigns", SevenSigns.SEAL_STONE_BLUE_ID, blueContribCount, this, false))
									{
										stonesFound = true;
										SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
										msg.addItemName(SevenSigns.SEAL_STONE_BLUE_ID);
										msg.addItemNumber(blueContribCount);
										player.sendPacket(msg);
									}
								}
								
								if (!stonesFound)
								{
									if (this instanceof L2DawnPriestInstance)
										showChatWindow(player, val, "dawn_no_stones", false);
									else
										showChatWindow(player, val, "dusk_no_stones", false);
								}
								else
								{
									contribScore = SevenSigns.getInstance().addPlayerStoneContrib(player.getObjectId(), blueContribCount, greenContribCount, redContribCount);
									sm = SystemMessage.getSystemMessage(SystemMessageId.CONTRIB_SCORE_INCREASED_S1);
									sm.addItemNumber(contribScore);
									player.sendPacket(sm);
									
									if (this instanceof L2DawnPriestInstance)
										showChatWindow(player, 6, "dawn", false);
									else
										showChatWindow(player, 6, "dusk", false);
								}
								return;
						}
						
						if (this instanceof L2DawnPriestInstance)
							path = SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_6_dawn_contribute.htm";
						else
							path = SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_6_dusk_contribute.htm";
						
						String contentContr = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), path);
						
						if (contentContr != null)
						{
							contentContr = contentContr.replaceAll("%contribStoneColor%", contribStoneColor);
							contentContr = contentContr.replaceAll("%stoneColor%", stoneColorContr);
							contentContr = contentContr.replaceAll("%stoneCount%", String.valueOf(stoneCountContr));
							contentContr = contentContr.replaceAll("%stoneItemId%", String.valueOf(stoneIdContr));
							contentContr = contentContr.replaceAll("%objectId%", String.valueOf(getObjectId()));
							
							NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
							html.setHtml(contentContr);
							player.sendPacket(html);
						}
						else
							_log.warning("Problem with HTML text " + path);
					}
					break;
				case 7: // Exchange Ancient Adena for Adena - SevenSigns 7 xxxxxxx
					long ancientAdenaConvert = 0;
					
					try
					{
						ancientAdenaConvert = Long.parseLong(command.substring(13).trim());
					}
					catch (NumberFormatException e)
					{
						showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "blkmrkt_3.htm");
						break;
					}
					catch (StringIndexOutOfBoundsException e)
					{
						showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "blkmrkt_3.htm");
						break;
					}
					
					if (ancientAdenaConvert < 1)
					{
						showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "blkmrkt_3.htm");
						break;
					}
					if (ancientAdenaAmount < ancientAdenaConvert)
					{
						showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "blkmrkt_4.htm");
						break;
					}
					
					player.reduceAncientAdena("SevenSigns", ancientAdenaConvert, this, true);
					player.addAdena("SevenSigns", ancientAdenaConvert, this, true);
					
					showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "blkmrkt_5.htm");
					break;
				case 9: // Receive Contribution Rewards
					int playerCabal = SevenSigns.getInstance().getPlayerCabal(player.getObjectId());
					int winningCabal = SevenSigns.getInstance().getCabalHighestScore();
					
					if (SevenSigns.getInstance().isSealValidationPeriod() && playerCabal == winningCabal)
					{
						int ancientAdenaReward = SevenSigns.getInstance().getAncientAdenaReward(player.getObjectId(), true);
						
						if (ancientAdenaReward < 3)
						{
							if (this instanceof L2DawnPriestInstance)
								showChatWindow(player, 9, "dawn_b", false);
							else
								showChatWindow(player, 9, "dusk_b", false);
							break;
						}
						
						player.addAncientAdena("SevenSigns", ancientAdenaReward, this, true);
						
						if (this instanceof L2DawnPriestInstance)
							showChatWindow(player, 9, "dawn_a", false);
						else
							showChatWindow(player, 9, "dusk_a", false);
					}
					break;
				case 11: // Teleport to Hunting Grounds
					try
					{
						String portInfo = command.substring(14).trim();
						StringTokenizer st = new StringTokenizer(portInfo);
						
						int x = Integer.parseInt(st.nextToken());
						int y = Integer.parseInt(st.nextToken());
						int z = Integer.parseInt(st.nextToken());
						
						long ancientAdenaCost = Long.parseLong(st.nextToken());
						
						if (ancientAdenaCost > 0)
						{
							if (!player.reduceAncientAdena("SevenSigns", ancientAdenaCost, this, true))
								break;
						}
						
						player.teleToLocation(x, y, z);
					}
					catch (Exception e)
					{
						_log.log(Level.WARNING, "SevenSigns: Error occurred while teleporting player: " + e.getMessage(), e);
					}
					break;
				case 16:
					if (this instanceof L2DawnPriestInstance)
						showChatWindow(player, val, "dawn", false);
					else
						showChatWindow(player, val, "dusk", false);
					break;
				case 17: // Exchange Seal Stones for Ancient Adena (Type Choice) - SevenSigns 17 x
					stoneType = Integer.parseInt(command.substring(14));
					
					int stoneId = 0;
					long stoneCount = 0;
					int stoneValue = 0;
					
					String stoneColor = null;
					
					switch (stoneType)
					{
						case 1:
							/* Move To MessageTable For L2JTW
							stoneColor = "blue";
							*/
							stoneColor = MessageTable.Messages[740].getMessage();  //Update by pmq
							stoneId = SevenSigns.SEAL_STONE_BLUE_ID;
							stoneValue = SevenSigns.SEAL_STONE_BLUE_VALUE;
							break;
						case 2:
							/* Move To MessageTable For L2JTW
							stoneColor = "green";
							*/
							stoneColor = MessageTable.Messages[742].getMessage();  //Update by pmq
							stoneId = SevenSigns.SEAL_STONE_GREEN_ID;
							stoneValue = SevenSigns.SEAL_STONE_GREEN_VALUE;
							break;
						case 3:
							/* Move To MessageTable For L2JTW
							stoneColor = "red";
							*/
							stoneColor = MessageTable.Messages[744].getMessage();  //Update by pmq
							stoneId = SevenSigns.SEAL_STONE_RED_ID;
							stoneValue = SevenSigns.SEAL_STONE_RED_VALUE;
							break;
						case 4:
							L2ItemInstance blueStonesAll = player.getInventory().getItemByItemId(SevenSigns.SEAL_STONE_BLUE_ID);
							L2ItemInstance greenStonesAll = player.getInventory().getItemByItemId(SevenSigns.SEAL_STONE_GREEN_ID);
							L2ItemInstance redStonesAll = player.getInventory().getItemByItemId(SevenSigns.SEAL_STONE_RED_ID);
							
							long blueStoneCountAll = blueStonesAll == null ? 0 : blueStonesAll.getCount();
							long greenStoneCountAll = greenStonesAll == null ? 0 : greenStonesAll.getCount();
							long redStoneCountAll = redStonesAll == null ? 0 : redStonesAll.getCount();
							long ancientAdenaRewardAll = 0;
							
							ancientAdenaRewardAll = SevenSigns.calcAncientAdenaReward(blueStoneCountAll, greenStoneCountAll, redStoneCountAll);
							
							if (ancientAdenaRewardAll == 0)
							{
								if (this instanceof L2DawnPriestInstance)
									showChatWindow(player, 18, "dawn_no_stones", false);
								else
									showChatWindow(player, 18, "dusk_no_stones", false);
								return;
							}
							
							if (blueStoneCountAll > 0)
								player.destroyItemByItemId("SevenSigns", SevenSigns.SEAL_STONE_BLUE_ID, blueStoneCountAll, this, true);
							if (greenStoneCountAll > 0)
								player.destroyItemByItemId("SevenSigns", SevenSigns.SEAL_STONE_GREEN_ID, greenStoneCountAll, this, true);
							if (redStoneCountAll > 0)
								player.destroyItemByItemId("SevenSigns", SevenSigns.SEAL_STONE_RED_ID, redStoneCountAll, this, true);
							
							player.addAncientAdena("SevenSigns", ancientAdenaRewardAll, this, true);
							
							if (this instanceof L2DawnPriestInstance)
								showChatWindow(player, 18, "dawn", false);
							else
								showChatWindow(player, 18, "dusk", false);
							return;
					}
					
					L2ItemInstance stoneInstance = player.getInventory().getItemByItemId(stoneId);
					
					if (stoneInstance != null)
						stoneCount = stoneInstance.getCount();
					
					if (this instanceof L2DawnPriestInstance)
						path = SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_17_dawn.htm";
					else
						path = SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_17_dusk.htm";
					
					String content = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), path);
					
					if (content != null)
					{
						content = content.replaceAll("%stoneColor%", stoneColor);
						content = content.replaceAll("%stoneValue%", String.valueOf(stoneValue));
						content = content.replaceAll("%stoneCount%", String.valueOf(stoneCount));
						content = content.replaceAll("%stoneItemId%", String.valueOf(stoneId));
						content = content.replaceAll("%objectId%", String.valueOf(getObjectId()));
						
						NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
						html.setHtml(content);
						player.sendPacket(html);
					}
					else
						_log.warning("Problem with HTML text " + SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_17.htm: " + path);
					break;
				case 18: // Exchange Seal Stones for Ancient Adena - SevenSigns 18 xxxx xxxxxx
					int convertStoneId = Integer.parseInt(command.substring(14, 18));
					long convertCount = 0;
					
					try
					{
						convertCount = Long.parseLong(command.substring(19).trim());
					}
					catch (Exception NumberFormatException)
					{
						if (this instanceof L2DawnPriestInstance)
							showChatWindow(player, 18, "dawn_failed", false);
						else
							showChatWindow(player, 18, "dusk_failed", false);
						break;
					}
					
					L2ItemInstance convertItem = player.getInventory().getItemByItemId(convertStoneId);
					
					if (convertItem != null)
					{
						long ancientAdenaReward = 0;
						long totalCount = convertItem.getCount();
						
						if (convertCount <= totalCount && convertCount > 0)
						{
							switch (convertStoneId)
							{
								case SevenSigns.SEAL_STONE_BLUE_ID:
									ancientAdenaReward = SevenSigns.calcAncientAdenaReward(convertCount, 0, 0);
									break;
								case SevenSigns.SEAL_STONE_GREEN_ID:
									ancientAdenaReward = SevenSigns.calcAncientAdenaReward(0, convertCount, 0);
									break;
								case SevenSigns.SEAL_STONE_RED_ID:
									ancientAdenaReward = SevenSigns.calcAncientAdenaReward(0, 0, convertCount);
									break;
							}
							
							if (player.destroyItemByItemId("SevenSigns", convertStoneId, convertCount, this, true))
							{
								player.addAncientAdena("SevenSigns", ancientAdenaReward, this, true);
								
								if (this instanceof L2DawnPriestInstance)
									showChatWindow(player, 18, "dawn", false);
								else
									showChatWindow(player, 18, "dusk", false);
							}
						}
						else
						{
							if (this instanceof L2DawnPriestInstance)
								showChatWindow(player, 18, "dawn_low_stones", false);
							else
								showChatWindow(player, 18, "dusk_low_stones", false);
							break;
						}
					}
					else
					{
						if (this instanceof L2DawnPriestInstance)
							showChatWindow(player, 18, "dawn_no_stones", false);
						else
							showChatWindow(player, 18, "dusk_no_stones", false);
						break;
					}
					break;
				case 19: // Seal Information (for when joining a cabal)
					int chosenSeal = Integer.parseInt(command.substring(16));
					
					String fileSuffix = SevenSigns.getSealName(chosenSeal, true) + "_" + SevenSigns.getCabalShortName(cabal);
					
					showChatWindow(player, val, fileSuffix, false);
					break;
				case 20: // Seal Status (for when joining a cabal)
					TextBuilder contentBuffer = new TextBuilder();
					
					if (this instanceof L2DawnPriestInstance)
						/* Move To MessageTable For L2JTW
						contentBuffer.append("<html><body>Priest of Dawn:<br><font color=\"LEVEL\">[ Seal Status ]</font><br>");
						*/
						contentBuffer.append("<html><body>"+ MessageTable.Messages[746].getMessage() +"<br><font color=\"LEVEL\">"+ MessageTable.Messages[747].getMessage() +"</font><br>");
					else
						/* Move To MessageTable For L2JTW
						contentBuffer.append("<html><body>Dusk Priestess:<br><font color=\"LEVEL\">[ Status of the Seals ]</font><br>");
						*/
						contentBuffer.append("<html><body>"+ MessageTable.Messages[748].getMessage() +"<br><font color=\"LEVEL\">"+ MessageTable.Messages[749].getMessage() +"</font><br>");
					
					for (int i = 1; i < 4; i++)
					{
						int sealOwner = SevenSigns.getInstance().getSealOwner(i);
						
						if (sealOwner != SevenSigns.CABAL_NULL)
							/*
							contentBuffer.append("[" + SevenSigns.getSealName(i, false) + ": " + SevenSigns.getCabalName(sealOwner) + "]<br>");
							*/
							contentBuffer.append("[" + SevenSigns.getSealFileName(i, false) + ": " + SevenSigns.getCabalName(sealOwner) + "]<br>"); //Update by rocknow
						else
							/* Move To MessageTable For L2JTW
							contentBuffer.append("[" + SevenSigns.getSealName(i, false) + ": Nothingness]<br>");
							*/
							contentBuffer.append("[" + SevenSigns.getSealFileName(i, false) + MessageTable.Messages[750].getMessage() +"]<br>"); //Update by rocknow
					}
					
					/* Move To MessageTable For L2JTW
					contentBuffer.append("<a action=\"bypass -h npc_" + getObjectId() + "_Chat 0\">Go back.</a></body></html>");
					*/
					contentBuffer.append("<a action=\"bypass -h npc_" + getObjectId() + "_Chat 0\">"+ MessageTable.Messages[751].getMessage() +"</a></body></html>");
					
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setHtml(contentBuffer.toString());
					player.sendPacket(html);
					break;
				default:
					showChatWindow(player, val, null, false);
					break;
			}
		}
		else
			super.onBypassFeedback(player, command);
	}
	
	private void showChatWindow(L2PcInstance player, int val, String suffix, boolean isDescription)
	{
		String filename = SevenSigns.SEVEN_SIGNS_HTML_PATH;
		
		filename += (isDescription) ? "desc_" + val : "signs_" + val;
		filename += (suffix != null) ? "_" + suffix + ".htm" : ".htm";
		
		showChatWindow(player, filename);
	}
}