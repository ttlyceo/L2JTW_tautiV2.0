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

import java.text.SimpleDateFormat;
import java.util.StringTokenizer;

import com.l2jserver.Config;
import com.l2jserver.gameserver.cache.HtmCache;
import com.l2jserver.gameserver.datatables.SkillTable;
import com.l2jserver.gameserver.datatables.TeleportLocationTable;
import com.l2jserver.gameserver.instancemanager.CHSiegeManager;
import com.l2jserver.gameserver.instancemanager.ClanHallManager;
import com.l2jserver.gameserver.model.L2Clan;
import com.l2jserver.gameserver.model.L2TeleportLocation;
import com.l2jserver.gameserver.model.actor.templates.L2NpcTemplate;
import com.l2jserver.gameserver.model.entity.ClanHall;
import com.l2jserver.gameserver.model.entity.clanhall.AuctionableHall;
import com.l2jserver.gameserver.model.entity.clanhall.SiegableHall;
import com.l2jserver.gameserver.model.skills.L2Skill;
import com.l2jserver.gameserver.model.skills.L2SkillType;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ActionFailed;
import com.l2jserver.gameserver.network.serverpackets.AgitDecoInfo;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jserver.gameserver.datatables.MessageTable;

public class L2ClanHallManagerInstance extends L2MerchantInstance
{
	protected static final int COND_OWNER_FALSE = 0;
	protected static final int COND_ALL_FALSE = 1;
	protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 2;
	protected static final int COND_OWNER = 3;
	private int _clanHallId = -1;
	
	/**
	 * @param objectId
	 * @param template
	 */
	public L2ClanHallManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2ClanHallManagerInstance);
	}
	
	@Override
	public boolean isWarehouse()
	{
		return true;
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if(getClanHall().isSiegableHall() && ((SiegableHall)getClanHall()).isInSiege())
			return;
		
		int condition = validateCondition(player);
		if (condition <= COND_ALL_FALSE)
			return;
		
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		if (condition == COND_OWNER)
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			String actualCommand = st.nextToken(); // Get actual command
			String val = "";
			if (st.countTokens() >= 1) {val = st.nextToken();}
			
			if (actualCommand.equalsIgnoreCase("banish_foreigner"))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				if ((player.getClanPrivileges() & L2Clan.CP_CH_DISMISS) == L2Clan.CP_CH_DISMISS)
				{
					if (val.equalsIgnoreCase("list"))
					{
						html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/banish-list.htm");
					}
					else if (val.equalsIgnoreCase("banish"))
					{
						getClanHall().banishForeigners();
						html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/banish.htm");
					}
				}
				else
				{
					html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/not_authorized.htm");
				}
				sendHtmlMessage(player, html);
				return;
			}
			else if(actualCommand.equalsIgnoreCase("manage_vault"))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				if ((player.getClanPrivileges() & L2Clan.CP_CL_VIEW_WAREHOUSE) == L2Clan.CP_CL_VIEW_WAREHOUSE)
				{
					if(getClanHall().getLease() <= 0)
						html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/vault-chs.htm");
					else
					{				
						html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/vault.htm");
						html.replace("%rent%", String.valueOf(getClanHall().getLease()));
						html.replace("%date%", format.format(getClanHall().getPaidUntil()));
					}
					sendHtmlMessage(player, html);
				}
				else
				{
					html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/not_authorized.htm");
					sendHtmlMessage(player, html);
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("door"))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				if ((player.getClanPrivileges() & L2Clan.CP_CH_OPEN_DOOR) == L2Clan.CP_CH_OPEN_DOOR)
				{
					if (val.equalsIgnoreCase("open"))
					{
						getClanHall().openCloseDoors(true);
						html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/door-open.htm");
					}
					else if (val.equalsIgnoreCase("close"))
					{
						getClanHall().openCloseDoors(false);
						html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/door-close.htm");
					}
					else
					{
						html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/door.htm");
					}
					sendHtmlMessage(player, html);
				}
				else
				{
					html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/not_authorized.htm");
					sendHtmlMessage(player, html);
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("functions"))
			{
				if (val.equalsIgnoreCase("tele"))
				{
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					if (getClanHall().getFunction(ClanHall.FUNC_TELEPORT) == null)
						html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/chamberlain-nac.htm");
					else
					//Update by rocknow-Start
					{
						int hallid = getClanHall().getId();
						switch (hallid)
						{
							case 21:
							case 22:
							case 23:
							case 24:
							case 25:
								html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/tele"+"Gludio"+getClanHall().getFunction(ClanHall.FUNC_TELEPORT).getLvl()+".htm");
								break;
							case 26:
							case 27:
							case 28:
							case 29:
							case 30:
								html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/tele"+"Gludin"+getClanHall().getFunction(ClanHall.FUNC_TELEPORT).getLvl()+".htm");
								break;
							case 31:
							case 32:
							case 33:
								html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/tele"+"Dion"+getClanHall().getFunction(ClanHall.FUNC_TELEPORT).getLvl()+".htm");
								break;
							case 34:
							case 36:
							case 37:
							case 38:
							case 39:
							case 40:
							case 41:
								html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/tele"+"Aden"+getClanHall().getFunction(ClanHall.FUNC_TELEPORT).getLvl()+".htm");
								break;
							case 42:
							case 43:
							case 44:
							case 45:
							case 46:
								html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/tele"+"Giran"+getClanHall().getFunction(ClanHall.FUNC_TELEPORT).getLvl()+".htm");
								break;
							case 47:
							case 48:
							case 49:
							case 50:
								html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/tele"+"Goddard"+getClanHall().getFunction(ClanHall.FUNC_TELEPORT).getLvl()+".htm");
								break;
							case 51:
							case 52:
							case 53:
							case 54:
							case 55:
							case 56:
							case 57:
								html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/tele"+"Rune"+getClanHall().getFunction(ClanHall.FUNC_TELEPORT).getLvl()+".htm");
								break;
							case 58:
							case 59:
							case 60:
							case 61:
								html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/tele"+"Schuttgart"+getClanHall().getFunction(ClanHall.FUNC_TELEPORT).getLvl()+".htm");
								break;
							case 62:
								html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/tele"+"Rainbow"+getClanHall().getFunction(ClanHall.FUNC_TELEPORT).getLvl()+".htm");
								break;
							case 64:
								html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/tele"+"Dead"+getClanHall().getFunction(ClanHall.FUNC_TELEPORT).getLvl()+".htm");
								break;
						}
					}
					//Update by rocknow-End
					sendHtmlMessage(player, html);
				}
				else if (val.equalsIgnoreCase("item_creation"))
				{
					if (getClanHall().getFunction(ClanHall.FUNC_ITEM_CREATE) == null){
						NpcHtmlMessage html = new NpcHtmlMessage(1);
						html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/chamberlain-nac.htm");
						sendHtmlMessage(player, html);
						return;
					}
					if (st.countTokens() < 1) return;
					int valbuy = Integer.parseInt(st.nextToken())+(getClanHall().getFunction(ClanHall.FUNC_ITEM_CREATE).getLvl()*100000);
					showBuyWindow(player, valbuy);
				}
				else if (val.equalsIgnoreCase("support"))
				{
					
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					if (getClanHall().getFunction(ClanHall.FUNC_SUPPORT)== null)
						html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/chamberlain-nac.htm");
					else{
						html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/support"+getClanHall().getFunction(ClanHall.FUNC_SUPPORT).getLvl()+".htm");
						html.replace("%mp%", String.valueOf((int)getCurrentMp()));
					}
					sendHtmlMessage(player, html);
				}
				else if (val.equalsIgnoreCase("back"))
					showChatWindow(player);
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions.htm");
					if (getClanHall().getFunction(ClanHall.FUNC_RESTORE_EXP) != null)
						html.replace("%xp_regen%", String.valueOf(getClanHall().getFunction(ClanHall.FUNC_RESTORE_EXP).getLvl()));
					else
						html.replace("%xp_regen%", "0");
					if (getClanHall().getFunction(ClanHall.FUNC_RESTORE_HP) != null)
						html.replace("%hp_regen%", String.valueOf(getClanHall().getFunction(ClanHall.FUNC_RESTORE_HP).getLvl()));
					else
						html.replace("%hp_regen%", "0");
					if (getClanHall().getFunction(ClanHall.FUNC_RESTORE_MP) != null)
						html.replace("%mp_regen%", String.valueOf(getClanHall().getFunction(ClanHall.FUNC_RESTORE_MP).getLvl()));
					else
						html.replace("%mp_regen%", "0");
					sendHtmlMessage(player, html);
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("manage"))
			{
				if ((player.getClanPrivileges() & L2Clan.CP_CH_SET_FUNCTIONS) == L2Clan.CP_CH_SET_FUNCTIONS)
				{
					if (val.equalsIgnoreCase("recovery"))
					{
						if (st.countTokens() >= 1)
						{
							if(getClanHall().getOwnerId() == 0)
							{
								/* Move To MessageTable For L2JTW
								player.sendMessage("This clan Hall have no owner, you cannot change configuration");
								*/
								player.sendMessage(585);
								return;
							}
							val = st.nextToken();
							if (val.equalsIgnoreCase("hp_cancel"))
							{
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-cancel.htm");
								html.replace("%apply%", "recovery hp 0");
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("mp_cancel"))
							{
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-cancel.htm");
								html.replace("%apply%", "recovery mp 0");
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("exp_cancel"))
							{
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-cancel.htm");
								html.replace("%apply%", "recovery exp 0");
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("edit_hp"))
							{
								val = st.nextToken();
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-apply.htm");
								/* Move To MessageTable For L2JTW
								html.replace("%name%", "Fireplace (HP Recovery Device)");
								*/
								html.replace("%name%", MessageTable.Messages[588].getMessage());
								int percent = Integer.parseInt(val);
								int cost;
								switch (percent)
								{
									case 20:
										cost = Config.CH_HPREG1_FEE;
										break;
									case 40:
										cost = Config.CH_HPREG2_FEE;
										break;
									case 80:
										cost = Config.CH_HPREG3_FEE;
										break;
									case 100:
										cost = Config.CH_HPREG4_FEE;
										break;
									case 120:
										cost = Config.CH_HPREG5_FEE;
										break;
									case 140:
										cost = Config.CH_HPREG6_FEE;
										break;
									case 160:
										cost = Config.CH_HPREG7_FEE;
										break;
									case 180:
										cost = Config.CH_HPREG8_FEE;
										break;
									case 200:
										cost = Config.CH_HPREG9_FEE;
										break;
									case 220:
										cost = Config.CH_HPREG10_FEE;
										break;
									case 240:
										cost = Config.CH_HPREG11_FEE;
										break;
									case 260:
										cost = Config.CH_HPREG12_FEE;
										break;
									default:
										cost = Config.CH_HPREG13_FEE;
										break;
								}
								
								/* Move To MessageTable For L2JTW
								html.replace("%cost%", String.valueOf(cost)+"</font>Adena /"+String.valueOf(Config.CH_HPREG_FEE_RATIO/1000/60/60/24)+" Day</font>)");
								html.replace("%use%","Provides additional HP recovery for clan members in the clan hall.<font color=\"00FFFF\">"+String.valueOf(percent)+"%</font>");
								*/
								html.replace("%cost%", String.valueOf(cost)+"</font>"+MessageTable.Messages[586].getMessage()+" /"+String.valueOf(Config.CH_HPREG_FEE_RATIO/1000/60/60/24)+MessageTable.Messages[587].getMessage()+"</font>)");
								html.replace("%use%",MessageTable.Messages[589].getMessage()+"<font color=\"00FFFF\">"+String.valueOf(percent)+"%</font>");
								html.replace("%apply%", "recovery hp " + String.valueOf(percent));
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("edit_mp"))
							{
								val = st.nextToken();
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-apply.htm");
								/* Move To MessageTable For L2JTW
								html.replace("%name%", "Carpet (MP Recovery)");
								*/
								html.replace("%name%", MessageTable.Messages[590].getMessage());
								int percent = Integer.parseInt(val);
								int cost;
								switch (percent)
								{
									case 5:
										cost = Config.CH_MPREG1_FEE;
										break;
									case 10:
										cost = Config.CH_MPREG2_FEE;
										break;
									case 15:
										cost = Config.CH_MPREG3_FEE;
										break;
									case 30:
										cost = Config.CH_MPREG4_FEE;
										break;
									default:
										cost = Config.CH_MPREG5_FEE;
										break;
								}
								/* Move To MessageTable For L2JTW
								html.replace("%cost%", String.valueOf(cost)+"</font>Adena /"+String.valueOf(Config.CH_MPREG_FEE_RATIO/1000/60/60/24)+" Day</font>)");
								html.replace("%use%","Provides additional MP recovery for clan members in the clan hall.<font color=\"00FFFF\">"+String.valueOf(percent)+"%</font>");
								*/
								html.replace("%cost%", String.valueOf(cost)+"</font>"+MessageTable.Messages[586].getMessage()+" /"+String.valueOf(Config.CH_MPREG_FEE_RATIO/1000/60/60/24)+MessageTable.Messages[587].getMessage()+"</font>)");
								html.replace("%use%",MessageTable.Messages[591].getMessage()+"<font color=\"00FFFF\">"+String.valueOf(percent)+"%</font>");
								html.replace("%apply%", "recovery mp " + String.valueOf(percent));
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("edit_exp"))
							{
								val = st.nextToken();
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-apply.htm");
								/* Move To MessageTable For L2JTW
								html.replace("%name%", "Chandelier (EXP Recovery Device)");
								*/
								html.replace("%name%", MessageTable.Messages[592].getMessage());
								int percent = Integer.parseInt(val);
								int cost;
								switch (percent)
								{
									case 5:
										cost = Config.CH_EXPREG1_FEE;
										break;
									case 10:
										cost = Config.CH_EXPREG2_FEE;
										break;
									case 15:
										cost = Config.CH_EXPREG3_FEE;
										break;
									case 25:
										cost = Config.CH_EXPREG4_FEE;
										break;
									case 35:
										cost = Config.CH_EXPREG5_FEE;
										break;
									case 40:
										cost = Config.CH_EXPREG6_FEE;
										break;
									default:
										cost = Config.CH_EXPREG7_FEE;
										break;
								}
								/* Move To MessageTable For L2JTW
								html.replace("%cost%", String.valueOf(cost)+"</font>Adena /"+String.valueOf(Config.CH_EXPREG_FEE_RATIO/1000/60/60/24)+" Day</font>)");
								html.replace("%use%","Restores the Exp of any clan member who is resurrected in the clan hall.<font color=\"00FFFF\">"+String.valueOf(percent)+"%</font>");
								*/
								html.replace("%cost%", String.valueOf(cost)+"</font>"+MessageTable.Messages[586].getMessage()+" /"+String.valueOf(Config.CH_EXPREG_FEE_RATIO/1000/60/60/24)+MessageTable.Messages[587].getMessage()+"</font>)");
								html.replace("%use%",MessageTable.Messages[593].getMessage()+"<font color=\"00FFFF\">"+String.valueOf(percent)+"%</font>");
								html.replace("%apply%", "recovery exp " + String.valueOf(percent));
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("hp"))
							{
								if (st.countTokens() >= 1)
								{
									int fee;
									if (Config.DEBUG) _log.warning("Mp editing invoked");
									val = st.nextToken();
									NpcHtmlMessage html = new NpcHtmlMessage(1);
									html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-apply_confirmed.htm");
									if (getClanHall().getFunction(ClanHall.FUNC_RESTORE_HP) != null)
									{
										if (getClanHall().getFunction(ClanHall.FUNC_RESTORE_HP).getLvl() == Integer.parseInt(val))
										{
											html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-used.htm");
											html.replace("%val%", String.valueOf(val)+"%");
											sendHtmlMessage(player, html);
											return;
										}
									}
									int percent = Integer.parseInt(val);
									switch (percent)
									{
										case 0:
											fee = 0;
											html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-cancel_confirmed.htm");
											break;
										case 20:
											fee = Config.CH_HPREG1_FEE;
											break;
										case 40:
											fee = Config.CH_HPREG2_FEE;
											break;
										case 80:
											fee = Config.CH_HPREG3_FEE;
											break;
										case 100:
											fee = Config.CH_HPREG4_FEE;
											break;
										case 120:
											fee = Config.CH_HPREG5_FEE;
											break;
										case 140:
											fee = Config.CH_HPREG6_FEE;
											break;
										case 160:
											fee = Config.CH_HPREG7_FEE;
											break;
										case 180:
											fee = Config.CH_HPREG8_FEE;
											break;
										case 200:
											fee = Config.CH_HPREG9_FEE;
											break;
										case 220:
											fee = Config.CH_HPREG10_FEE;
											break;
										case 240:
											fee = Config.CH_HPREG11_FEE;
											break;
										case 260:
											fee = Config.CH_HPREG12_FEE;
											break;
										default:
											fee = Config.CH_HPREG13_FEE;
											break;
									}
									if (!getClanHall().updateFunctions(player,ClanHall.FUNC_RESTORE_HP, percent, fee, Config.CH_HPREG_FEE_RATIO,(getClanHall().getFunction(ClanHall.FUNC_RESTORE_HP) == null)))
									{
										html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/low_adena.htm");
										sendHtmlMessage(player, html);
									}
									else
										revalidateDeco(player);
									sendHtmlMessage(player, html);
								}
								return;
							}
							else if (val.equalsIgnoreCase("mp"))
							{
								if (st.countTokens() >= 1)
								{
									int fee;
									if (Config.DEBUG) _log.warning("Mp editing invoked");
									val = st.nextToken();
									NpcHtmlMessage html = new NpcHtmlMessage(1);
									html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-apply_confirmed.htm");
									if (getClanHall().getFunction(ClanHall.FUNC_RESTORE_MP) != null)
									{
										if (getClanHall().getFunction(ClanHall.FUNC_RESTORE_MP).getLvl() == Integer.parseInt(val))
										{
											html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-used.htm");
											html.replace("%val%", String.valueOf(val)+"%");
											sendHtmlMessage(player, html);
											return;
										}
									}
									int percent = Integer.parseInt(val);
									switch (percent)
									{
										case 0:
											fee = 0;
											html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-cancel_confirmed.htm");
											break;
										case 5:
											fee = Config.CH_MPREG1_FEE;
											break;
										case 10:
											fee = Config.CH_MPREG2_FEE;
											break;
										case 15:
											fee = Config.CH_MPREG3_FEE;
											break;
										case 30:
											fee = Config.CH_MPREG4_FEE;
											break;
										default:
											fee = Config.CH_MPREG5_FEE;
											break;
									}
									if(!getClanHall().updateFunctions(player,ClanHall.FUNC_RESTORE_MP, percent, fee, Config.CH_MPREG_FEE_RATIO,(getClanHall().getFunction(ClanHall.FUNC_RESTORE_MP) == null)))
									{
										html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/low_adena.htm");
										sendHtmlMessage(player, html);
									}
									else
										revalidateDeco(player);
									sendHtmlMessage(player, html);
								}
								return;
							}
							else if (val.equalsIgnoreCase("exp"))
							{
								if (st.countTokens() >= 1)
								{
									int fee;
									if (Config.DEBUG) _log.warning("Exp editing invoked");
									val = st.nextToken();
									NpcHtmlMessage html = new NpcHtmlMessage(1);
									html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-apply_confirmed.htm");
									if (getClanHall().getFunction(ClanHall.FUNC_RESTORE_EXP) != null)
									{
										if (getClanHall().getFunction(ClanHall.FUNC_RESTORE_EXP).getLvl() == Integer.parseInt(val))
										{
											html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-used.htm");
											html.replace("%val%", String.valueOf(val)+"%");
											sendHtmlMessage(player, html);
											return;
										}
									}
									int percent = Integer.parseInt(val);
									switch (percent)
									{
										case 0:
											fee = 0;
											html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-cancel_confirmed.htm");
											break;
										case 5:
											fee = Config.CH_EXPREG1_FEE;
											break;
										case 10:
											fee = Config.CH_EXPREG2_FEE;
											break;
										case 15:
											fee = Config.CH_EXPREG3_FEE;
											break;
										case 25:
											fee = Config.CH_EXPREG4_FEE;
											break;
										case 35:
											fee = Config.CH_EXPREG5_FEE;
											break;
										case 40:
											fee = Config.CH_EXPREG6_FEE;
											break;
										default:
											fee = Config.CH_EXPREG7_FEE;
											break;
									}
									if (!getClanHall().updateFunctions(player, ClanHall.FUNC_RESTORE_EXP, percent, fee, Config.CH_EXPREG_FEE_RATIO,(getClanHall().getFunction(ClanHall.FUNC_RESTORE_EXP) == null)))
									{
										html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/low_adena.htm");
										sendHtmlMessage(player, html);
									}
									else
										revalidateDeco(player);
									sendHtmlMessage(player, html);
								}
								return;
							}
						}
						NpcHtmlMessage html = new NpcHtmlMessage(1);
						html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/edit_recovery.htm");
						String hp_grade0 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 20\">20%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 40\">40%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 220\">220%</a>]";
						String hp_grade1 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 40\">40%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 100\">100%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 160\">160%</a>]";
						String hp_grade2 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 80\">80%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 140\">140%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 200\">200%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 260\">260%</a>]";
						String hp_grade3 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 80\">80%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 120\">120%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 180\">180%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 240\">240%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 300\">300%</a>]";
						String exp_grade0 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 10\">10%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>]";
						String exp_grade1 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 30\">30%</a>]";
						String exp_grade2 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 40\">40%</a>]";
						String exp_grade3 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 35\">35%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 50\">50%</a>]";
						String mp_grade0 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 10\">10%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 25\">25%</a>]";
						String mp_grade1 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 25\">25%</a>]";
						String mp_grade2 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 30\">30%</a>]";
						String mp_grade3 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 30\">30%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 40\">40%</a>]";
						if (getClanHall().getFunction(ClanHall.FUNC_RESTORE_HP) != null)
						{
							/* Move To MessageTable For L2JTW
							html.replace("%hp_recovery%", String.valueOf(getClanHall().getFunction(ClanHall.FUNC_RESTORE_HP).getLvl()) + "%</font> (<font color=\"FFAABB\">"+String.valueOf(getClanHall().getFunction(ClanHall.FUNC_RESTORE_HP).getLease())+"</font>Adena /"+String.valueOf(Config.CH_HPREG_FEE_RATIO/1000/60/60/24)+" Day)");
							html.replace("%hp_period%","Withdraw the fee for the next time at " + format.format(getClanHall().getFunction(ClanHall.FUNC_RESTORE_HP).getEndTime()));
							*/
							html.replace("%hp_recovery%", String.valueOf(getClanHall().getFunction(ClanHall.FUNC_RESTORE_HP).getLvl()) + "%</font> (<font color=\"FFAABB\">"+String.valueOf(getClanHall().getFunction(ClanHall.FUNC_RESTORE_HP).getLease())+"</font>"+MessageTable.Messages[586].getMessage()+" /"+String.valueOf(Config.CH_HPREG_FEE_RATIO/1000/60/60/24)+MessageTable.Messages[587].getMessage()+")");
							html.replace("%hp_period%",MessageTable.Messages[594].getMessage() + format.format(getClanHall().getFunction(ClanHall.FUNC_RESTORE_HP).getEndTime()));
							int grade = getClanHall().getGrade();
							switch (grade)
							{
								case 0:
									html.replace("%change_hp%","[<a action=\"bypass -h npc_%objectId%_manage recovery hp_cancel\">Deactivate</a>]"+hp_grade0);
									break;
								case 1:
									html.replace("%change_hp%","[<a action=\"bypass -h npc_%objectId%_manage recovery hp_cancel\">Deactivate</a>]"+hp_grade1);
									break;
								case 2:
									html.replace("%change_hp%","[<a action=\"bypass -h npc_%objectId%_manage recovery hp_cancel\">Deactivate</a>]"+hp_grade2);
									break;
								case 3:
									html.replace("%change_hp%","[<a action=\"bypass -h npc_%objectId%_manage recovery hp_cancel\">Deactivate</a>]"+hp_grade3);
									break;
							}
						}
						else
						{
							/* Move To MessageTable For L2JTW
							html.replace("%hp_recovery%", "none");
							html.replace("%hp_period%", "none");
							*/
							html.replace("%hp_recovery%", MessageTable.Messages[596].getMessage());
							html.replace("%hp_period%", MessageTable.Messages[596].getMessage());
							int grade = getClanHall().getGrade();
							switch (grade)
							{
								case 0:
									html.replace("%change_hp%",hp_grade0);
									break;
								case 1:
									html.replace("%change_hp%",hp_grade1);
									break;
								case 2:
									html.replace("%change_hp%",hp_grade2);
									break;
								case 3:
									html.replace("%change_hp%",hp_grade3);
									break;
							}
						}
						if (getClanHall().getFunction(ClanHall.FUNC_RESTORE_EXP) != null)
						{
							/* Move To MessageTable For L2JTW
							html.replace("%exp_recovery%",  String.valueOf(getClanHall().getFunction(ClanHall.FUNC_RESTORE_EXP).getLvl()) + "%</font> (<font color=\"FFAABB\">"+String.valueOf(getClanHall().getFunction(ClanHall.FUNC_RESTORE_EXP).getLease())+"</font>Adena /"+String.valueOf(Config.CH_EXPREG_FEE_RATIO/1000/60/60/24)+" Day)");
							html.replace("%exp_period%", "Withdraw the fee for the next time at " + format.format(getClanHall().getFunction(ClanHall.FUNC_RESTORE_EXP).getEndTime()));
							*/
							html.replace("%exp_recovery%",  String.valueOf(getClanHall().getFunction(ClanHall.FUNC_RESTORE_EXP).getLvl()) + "%</font> (<font color=\"FFAABB\">"+String.valueOf(getClanHall().getFunction(ClanHall.FUNC_RESTORE_EXP).getLease())+"</font>"+MessageTable.Messages[586].getMessage()+" /"+String.valueOf(Config.CH_EXPREG_FEE_RATIO/1000/60/60/24)+MessageTable.Messages[587].getMessage()+")");
							html.replace("%exp_period%", MessageTable.Messages[594].getMessage() + format.format(getClanHall().getFunction(ClanHall.FUNC_RESTORE_EXP).getEndTime()));
							int grade = getClanHall().getGrade();
							switch (grade)
							{
								case 0:
									/* Move To MessageTable For L2JTW
									html.replace("%change_exp%","[<a action=\"bypass -h npc_%objectId%_manage recovery exp_cancel\">Deactivate</a>]"+exp_grade0);
									*/
									html.replace("%change_exp%","[<a action=\"bypass -h npc_%objectId%_manage recovery exp_cancel\">"+MessageTable.Messages[595].getMessage()+"</a>]"+exp_grade0);
									break;
								case 1:
									/* Move To MessageTable For L2JTW
									html.replace("%change_exp%","[<a action=\"bypass -h npc_%objectId%_manage recovery exp_cancel\">Deactivate</a>]"+exp_grade1);
									*/
									html.replace("%change_exp%","[<a action=\"bypass -h npc_%objectId%_manage recovery exp_cancel\">"+MessageTable.Messages[595].getMessage()+"</a>]"+exp_grade1);
									break;
								case 2:
									/* Move To MessageTable For L2JTW
									html.replace("%change_exp%","[<a action=\"bypass -h npc_%objectId%_manage recovery exp_cancel\">Deactivate</a>]"+exp_grade2);
									*/
									html.replace("%change_exp%","[<a action=\"bypass -h npc_%objectId%_manage recovery exp_cancel\">"+MessageTable.Messages[595].getMessage()+"</a>]"+exp_grade2);
									break;
								case 3:
									/* Move To MessageTable For L2JTW
									html.replace("%change_exp%","[<a action=\"bypass -h npc_%objectId%_manage recovery exp_cancel\">Deactivate</a>]"+exp_grade3);
									*/
									html.replace("%change_exp%","[<a action=\"bypass -h npc_%objectId%_manage recovery exp_cancel\">"+MessageTable.Messages[595].getMessage()+"</a>]"+exp_grade3);
									break;
							}
						}
						else
						{
							/* Move To MessageTable For L2JTW
							html.replace("%exp_recovery%", "none");
							html.replace("%exp_period%", "none");
							*/
							html.replace("%exp_recovery%", MessageTable.Messages[596].getMessage());
							html.replace("%exp_period%", MessageTable.Messages[596].getMessage());
							int grade = getClanHall().getGrade();
							switch (grade)
							{
								case 0:
									html.replace("%change_exp%",exp_grade0);
									break;
								case 1:
									html.replace("%change_exp%",exp_grade1);
									break;
								case 2:
									html.replace("%change_exp%",exp_grade2);
									break;
								case 3:
									html.replace("%change_exp%",exp_grade3);
									break;
							}
						}
						if (getClanHall().getFunction(ClanHall.FUNC_RESTORE_MP) != null )
						{
							/* Move To MessageTable For L2JTW
							html.replace("%mp_recovery%",  String.valueOf(getClanHall().getFunction(ClanHall.FUNC_RESTORE_MP).getLvl()) + "%</font> (<font color=\"FFAABB\">"+String.valueOf(getClanHall().getFunction(ClanHall.FUNC_RESTORE_MP).getLease())+"</font>Adena /"+String.valueOf(Config.CH_MPREG_FEE_RATIO/1000/60/60/24)+" Day)");
							html.replace("%mp_period%", "Withdraw the fee for the next time at " + format.format(getClanHall().getFunction(ClanHall.FUNC_RESTORE_MP).getEndTime()));
							*/
							html.replace("%mp_recovery%",  String.valueOf(getClanHall().getFunction(ClanHall.FUNC_RESTORE_MP).getLvl()) + "%</font> (<font color=\"FFAABB\">"+String.valueOf(getClanHall().getFunction(ClanHall.FUNC_RESTORE_MP).getLease())+"</font>"+MessageTable.Messages[586].getMessage()+" /"+String.valueOf(Config.CH_MPREG_FEE_RATIO/1000/60/60/24)+MessageTable.Messages[587].getMessage()+")");
							html.replace("%mp_period%", MessageTable.Messages[594].getMessage() + format.format(getClanHall().getFunction(ClanHall.FUNC_RESTORE_MP).getEndTime()));
							int grade = getClanHall().getGrade();
							switch (grade)
							{
								case 0:
									/* Move To MessageTable For L2JTW
									html.replace("%change_mp%","[<a action=\"bypass -h npc_%objectId%_manage recovery mp_cancel\">Deactivate</a>]"+mp_grade0);
									*/
									html.replace("%change_mp%","[<a action=\"bypass -h npc_%objectId%_manage recovery mp_cancel\">"+MessageTable.Messages[595].getMessage()+"</a>]"+mp_grade0);
									break;
								case 1:
									/* Move To MessageTable For L2JTW
									html.replace("%change_mp%","[<a action=\"bypass -h npc_%objectId%_manage recovery mp_cancel\">Deactivate</a>]"+mp_grade1);
									*/
									html.replace("%change_mp%","[<a action=\"bypass -h npc_%objectId%_manage recovery mp_cancel\">"+MessageTable.Messages[595].getMessage()+"</a>]"+mp_grade1);
									break;
								case 2:
									/* Move To MessageTable For L2JTW
									html.replace("%change_mp%","[<a action=\"bypass -h npc_%objectId%_manage recovery mp_cancel\">Deactivate</a>]"+mp_grade2);
									*/
									html.replace("%change_mp%","[<a action=\"bypass -h npc_%objectId%_manage recovery mp_cancel\">"+MessageTable.Messages[595].getMessage()+"</a>]"+mp_grade2);
									break;
								case 3:
									/* Move To MessageTable For L2JTW
									html.replace("%change_mp%","[<a action=\"bypass -h npc_%objectId%_manage recovery mp_cancel\">Deactivate</a>]"+mp_grade3);
									*/
									html.replace("%change_mp%","[<a action=\"bypass -h npc_%objectId%_manage recovery mp_cancel\">"+MessageTable.Messages[595].getMessage()+"</a>]"+mp_grade3);
									break;
							}
						}
						else
						{
							/* Move To MessageTable For L2JTW
							html.replace("%mp_recovery%", "none");
							html.replace("%mp_period%", "none");
							*/
							html.replace("%mp_recovery%", MessageTable.Messages[596].getMessage());
							html.replace("%mp_period%", MessageTable.Messages[596].getMessage());
							int grade = getClanHall().getGrade();
							switch (grade)
							{
								case 0:
									html.replace("%change_mp%",mp_grade0);
									break;
								case 1:
									html.replace("%change_mp%",mp_grade1);
									break;
								case 2:
									html.replace("%change_mp%",mp_grade2);
									break;
								case 3:
									html.replace("%change_mp%",mp_grade3);
									break;
							}
						}
						sendHtmlMessage(player, html);
					}
					else if (val.equalsIgnoreCase("other"))
					{
						if (st.countTokens() >= 1)
						{
							if(getClanHall().getOwnerId() == 0)
							{
								/* Move To MessageTable For L2JTW
								player.sendMessage("This clan Hall have no owner, you cannot change configuration");
								*/
								player.sendMessage(585);
								return;
							}
							val = st.nextToken();
							if (val.equalsIgnoreCase("item_cancel"))
							{
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-cancel.htm");
								html.replace("%apply%", "other item 0");
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("tele_cancel"))
							{
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-cancel.htm");
								html.replace("%apply%", "other tele 0");
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("support_cancel"))
							{
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-cancel.htm");
								html.replace("%apply%", "other support 0");
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("edit_item"))
							{
								val = st.nextToken();
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-apply.htm");
								/* Move To MessageTable For L2JTW
								html.replace("%name%", "Magic Equipment (Item Production Facilities)");
								*/
								html.replace("%name%", MessageTable.Messages[597].getMessage());
								int stage = Integer.parseInt(val);
								int cost;
								switch (stage)
								{
									case 1:
										cost = Config.CH_ITEM1_FEE;
										break;
									case 2:
										cost = Config.CH_ITEM2_FEE;
										break;
									default:
										cost = Config.CH_ITEM3_FEE;
										break;
								}
								/* Move To MessageTable For L2JTW
								html.replace("%cost%", String.valueOf(cost)+"</font>Adena /"+String.valueOf(Config.CH_ITEM_FEE_RATIO/1000/60/60/24)+" Day</font>)");
								html.replace("%use%","Allow the purchase of special items at fixed intervals.");
								*/
								html.replace("%cost%", String.valueOf(cost)+"</font>"+MessageTable.Messages[586].getMessage()+" /"+String.valueOf(Config.CH_ITEM_FEE_RATIO/1000/60/60/24)+MessageTable.Messages[587].getMessage()+"</font>)");
								html.replace("%use%",MessageTable.Messages[598].getMessage());
								html.replace("%apply%", "other item " + String.valueOf(stage));
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("edit_support"))
							{
								val = st.nextToken();
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-apply.htm");
								/* Move To MessageTable For L2JTW
								html.replace("%name%", "Insignia (Supplementary Magic)");
								*/
								html.replace("%name%", MessageTable.Messages[599].getMessage());
								int stage = Integer.parseInt(val);
								int cost;
								switch (stage)
								{
									case 1:
										cost = Config.CH_SUPPORT1_FEE;
										break;
									case 2:
										cost = Config.CH_SUPPORT2_FEE;
										break;
									case 3:
										cost = Config.CH_SUPPORT3_FEE;
										break;
									case 4:
										cost = Config.CH_SUPPORT4_FEE;
										break;
									case 5:
										cost = Config.CH_SUPPORT5_FEE;
										break;
									case 6:
										cost = Config.CH_SUPPORT6_FEE;
										break;
									case 7:
										cost = Config.CH_SUPPORT7_FEE;
										break;
									default:
										cost = Config.CH_SUPPORT8_FEE;
										break;
								}
								/* Move To MessageTable For L2JTW
								html.replace("%cost%", String.valueOf(cost)+"</font>Adena /"+String.valueOf(Config.CH_SUPPORT_FEE_RATIO/1000/60/60/24)+" Day</font>)");
								html.replace("%use%","Enables the use of supplementary magic.");
								*/
								html.replace("%cost%", String.valueOf(cost)+"</font>"+MessageTable.Messages[586].getMessage()+" /"+String.valueOf(Config.CH_SUPPORT_FEE_RATIO/1000/60/60/24)+MessageTable.Messages[587].getMessage()+"</font>)");
								html.replace("%use%",MessageTable.Messages[600].getMessage());
								html.replace("%apply%", "other support " + String.valueOf(stage));
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("edit_tele"))
							{
								val = st.nextToken();
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-apply.htm");
								/* Move To MessageTable For L2JTW
								html.replace("%name%", "Mirror (Teleportation Device)");
								*/
								html.replace("%name%", MessageTable.Messages[601].getMessage());
								int stage = Integer.parseInt(val);
								int cost;
								switch (stage)
								{
									case 1:
										cost = Config.CH_TELE1_FEE;
										break;
									default:
										cost = Config.CH_TELE2_FEE;
										break;
								}
								/* Move To MessageTable For L2JTW
								html.replace("%cost%", String.valueOf(cost)+"</font>Adena /"+String.valueOf(Config.CH_TELE_FEE_RATIO/1000/60/60/24)+" Day</font>)");
								html.replace("%use%","Teleports clan members in a clan hall to the target <font color=\"00FFFF\">Stage "+String.valueOf(stage)+"</font> staging area");
								*/
								html.replace("%cost%", String.valueOf(cost)+"</font>"+MessageTable.Messages[586].getMessage()+" /"+String.valueOf(Config.CH_TELE_FEE_RATIO/1000/60/60/24)+MessageTable.Messages[587].getMessage()+"</font>)");
								html.replace("%use%",MessageTable.Messages[602].getMessage()+"<font color=\"00FFFF\">"+MessageTable.Messages[603].getMessage()+String.valueOf(stage)+"</font>"+MessageTable.Messages[604].getMessage());
								html.replace("%apply%", "other tele " + String.valueOf(stage));
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("item"))
							{
								if (st.countTokens() >= 1)
								{
									if(getClanHall().getOwnerId() == 0){
										/* Move To MessageTable For L2JTW
										player.sendMessage("This clan Hall have no owner, you cannot change configuration");
										*/
										player.sendMessage(585);
										return;
									}
									if (Config.DEBUG) _log.warning("Item editing invoked");
									val = st.nextToken();
									NpcHtmlMessage html = new NpcHtmlMessage(1);
									html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-apply_confirmed.htm");
									if (getClanHall().getFunction(ClanHall.FUNC_ITEM_CREATE) != null)
									{
										if (getClanHall().getFunction(ClanHall.FUNC_ITEM_CREATE).getLvl() == Integer.parseInt(val))
										{
											html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-used.htm");
											/* Move To MessageTable For L2JTW
											html.replace("%val%","Stage " +String.valueOf(val));
											*/
											html.replace("%val%",MessageTable.Messages[603].getMessage() +String.valueOf(val));
											sendHtmlMessage(player, html);
											return;
										}
									}
									int fee;
									int lvl = Integer.parseInt(val);
									switch (lvl)
									{
										case 0:
											fee = 0;
											html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-cancel_confirmed.htm");
											break;
										case 1:
											fee = Config.CH_ITEM1_FEE;
											break;
										case 2:
											fee = Config.CH_ITEM2_FEE;
											break;
										default:
											fee = Config.CH_ITEM3_FEE;
											break;
									}
									if (!getClanHall().updateFunctions(player, ClanHall.FUNC_ITEM_CREATE, lvl, fee, Config.CH_ITEM_FEE_RATIO,(getClanHall().getFunction(ClanHall.FUNC_ITEM_CREATE) == null)))
									{
										html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/low_adena.htm");
										sendHtmlMessage(player, html);
									}
									else
										revalidateDeco(player);
									sendHtmlMessage(player, html);
								}
								return;
							}
							else if (val.equalsIgnoreCase("tele"))
							{
								if (st.countTokens() >= 1)
								{
									int fee;
									if (Config.DEBUG) _log.warning("Tele editing invoked");
									val = st.nextToken();
									NpcHtmlMessage html = new NpcHtmlMessage(1);
									html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-apply_confirmed.htm");
									if (getClanHall().getFunction(ClanHall.FUNC_TELEPORT) != null)
									{
										if (getClanHall().getFunction(ClanHall.FUNC_TELEPORT).getLvl() == Integer.parseInt(val))
										{
											html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-used.htm");
											/* Move To MessageTable For L2JTW
											html.replace("%val%","Stage " +String.valueOf(val));
											*/
											html.replace("%val%",MessageTable.Messages[603].getMessage() +String.valueOf(val));
											sendHtmlMessage(player, html);
											return;
										}
									}
									int lvl = Integer.parseInt(val);
									switch (lvl)
									{
										case 0:
											fee = 0;
											html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-cancel_confirmed.htm");
											break;
										case 1:
											fee = Config.CH_TELE1_FEE;
											break;
										default:
											fee = Config.CH_TELE2_FEE;
											break;
									}
									if (!getClanHall().updateFunctions(player,ClanHall.FUNC_TELEPORT, lvl, fee, Config.CH_TELE_FEE_RATIO,(getClanHall().getFunction(ClanHall.FUNC_TELEPORT) == null)))
									{
										html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/low_adena.htm");
										sendHtmlMessage(player, html);
									}
									else
										revalidateDeco(player);
									sendHtmlMessage(player, html);
								}
								return;
							}
							else if (val.equalsIgnoreCase("support"))
							{
								if (st.countTokens() >= 1)
								{
									int fee;
									if (Config.DEBUG) _log.warning("Support editing invoked");
									val = st.nextToken();
									NpcHtmlMessage html = new NpcHtmlMessage(1);
									html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-apply_confirmed.htm");
									if (getClanHall().getFunction(ClanHall.FUNC_SUPPORT) != null)
									{
										if (getClanHall().getFunction(ClanHall.FUNC_SUPPORT).getLvl() == Integer.parseInt(val))
										{
											html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-used.htm");
											/* Move To MessageTable For L2JTW
											html.replace("%val%","Stage " +String.valueOf(val));
											*/
											html.replace("%val%",MessageTable.Messages[603].getMessage() +String.valueOf(val));
											sendHtmlMessage(player, html);
											return;
										}
									}
									int lvl = Integer.parseInt(val);
									switch (lvl)
									{
										case 0:
											fee = 0;
											html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-cancel_confirmed.htm");
											break;
										case 1:
											fee = Config.CH_SUPPORT1_FEE;
											break;
										case 2:
											fee = Config.CH_SUPPORT2_FEE;
											break;
										case 3:
											fee = Config.CH_SUPPORT3_FEE;
											break;
										case 4:
											fee = Config.CH_SUPPORT4_FEE;
											break;
										case 5:
											fee = Config.CH_SUPPORT5_FEE;
											break;
										case 6:
											fee = Config.CH_SUPPORT6_FEE;
											break;
										case 7:
											fee = Config.CH_SUPPORT7_FEE;
											break;
										default:
											fee = Config.CH_SUPPORT8_FEE;
											break;
									}
									if (!getClanHall().updateFunctions(player,ClanHall.FUNC_SUPPORT, lvl, fee, Config.CH_SUPPORT_FEE_RATIO,(getClanHall().getFunction(ClanHall.FUNC_SUPPORT) == null)))
									{
										html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/low_adena.htm");
										sendHtmlMessage(player, html);
									}
									else
										revalidateDeco(player);
									sendHtmlMessage(player, html);
								}
								return;
							}
						}
						NpcHtmlMessage html = new NpcHtmlMessage(1);
						html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/edit_other.htm");
						/* Move To MessageTable For L2JTW
						String tele = "[<a action=\"bypass -h npc_%objectId%_manage other edit_tele 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_tele 2\">Level 2</a>]";
						String support_grade0 = "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 2\">Level 2</a>]";
						String support_grade1 = "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 2\">Level 2</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 4\">Level 4</a>]";
						String support_grade2 = "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 3\">Level 3</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 4\">Level 4</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 5\">Level 5</a>]";
						String support_grade3 = "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 3\">Level 3</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 5\">Level 5</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 7\">Level 7</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 8\">Level 8</a>]";
						String item = "[<a action=\"bypass -h npc_%objectId%_manage other edit_item 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_item 2\">Level 2</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_item 3\">Level 3</a>]";
						*/
						String tele = "[<a action=\"bypass -h npc_%objectId%_manage other edit_tele 1\">"+MessageTable.Messages[605].getMessage()+"1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_tele 2\">"+MessageTable.Messages[605].getMessage()+"2</a>]";
						String support_grade0 = "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 1\">"+MessageTable.Messages[605].getMessage()+"1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 2\">"+MessageTable.Messages[605].getMessage()+"2</a>]";
						String support_grade1 = "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 1\">"+MessageTable.Messages[605].getMessage()+"1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 2\">"+MessageTable.Messages[605].getMessage()+"2</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 4\">"+MessageTable.Messages[605].getMessage()+"4</a>]";
						String support_grade2 = "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 3\">"+MessageTable.Messages[605].getMessage()+"2</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 4\">"+MessageTable.Messages[605].getMessage()+"4</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 5\">"+MessageTable.Messages[605].getMessage()+"5</a>]";
						String support_grade3 = "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 3\">"+MessageTable.Messages[605].getMessage()+"2</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 5\">"+MessageTable.Messages[605].getMessage()+"5</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 7\">"+MessageTable.Messages[605].getMessage()+"7</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 8\">"+MessageTable.Messages[605].getMessage()+"8</a>]";
						String item = "[<a action=\"bypass -h npc_%objectId%_manage other edit_item 1\">"+MessageTable.Messages[605].getMessage()+"2</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_item 2\">"+MessageTable.Messages[605].getMessage()+"2</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_item 3\">"+MessageTable.Messages[605].getMessage()+"3</a>]";
						if (getClanHall().getFunction(ClanHall.FUNC_TELEPORT) != null)
						{
							/* Move To MessageTable For L2JTW
							html.replace("%tele%","Stage "+ String.valueOf(getClanHall().getFunction(ClanHall.FUNC_TELEPORT).getLvl()) + "</font> (<font color=\"FFAABB\">"+String.valueOf(getClanHall().getFunction(ClanHall.FUNC_TELEPORT).getLease())+"</font>Adena /"+String.valueOf(Config.CH_TELE_FEE_RATIO/1000/60/60/24)+" Day)");
							html.replace("%tele_period%","Withdraw the fee for the next time at " + format.format(getClanHall().getFunction(ClanHall.FUNC_TELEPORT).getEndTime()));
							html.replace("%change_tele%","[<a action=\"bypass -h npc_%objectId%_manage other tele_cancel\">Deactivate</a>]"+tele);
							*/
							html.replace("%tele%",MessageTable.Messages[603].getMessage()+ String.valueOf(getClanHall().getFunction(ClanHall.FUNC_TELEPORT).getLvl()) + "</font> (<font color=\"FFAABB\">"+String.valueOf(getClanHall().getFunction(ClanHall.FUNC_TELEPORT).getLease())+"</font>"+MessageTable.Messages[586].getMessage()+" /"+String.valueOf(Config.CH_TELE_FEE_RATIO/1000/60/60/24)+MessageTable.Messages[587].getMessage()+")");
							html.replace("%tele_period%",MessageTable.Messages[594].getMessage() + format.format(getClanHall().getFunction(ClanHall.FUNC_TELEPORT).getEndTime()));
							html.replace("%change_tele%","[<a action=\"bypass -h npc_%objectId%_manage other tele_cancel\">"+MessageTable.Messages[595].getMessage()+"</a>]"+tele);
						}
						else
						{
							/* Move To MessageTable For L2JTW
							html.replace("%tele%", "none");
							html.replace("%tele_period%", "none");
							*/
							html.replace("%tele%", MessageTable.Messages[596].getMessage());
							html.replace("%tele_period%", MessageTable.Messages[596].getMessage());
							html.replace("%change_tele%",tele);
						}
						if (getClanHall().getFunction(ClanHall.FUNC_SUPPORT) != null)
						{
							/* Move To MessageTable For L2JTW
							html.replace("%support%","Stage "+ String.valueOf(getClanHall().getFunction(ClanHall.FUNC_SUPPORT).getLvl()) + "</font> (<font color=\"FFAABB\">"+String.valueOf(getClanHall().getFunction(ClanHall.FUNC_SUPPORT).getLease())+"</font>Adena /"+String.valueOf(Config.CH_SUPPORT_FEE_RATIO/1000/60/60/24)+" Day)");
							html.replace("%support_period%","Withdraw the fee for the next time at " + format.format(getClanHall().getFunction(ClanHall.FUNC_SUPPORT).getEndTime()));
							*/
							html.replace("%support%",MessageTable.Messages[603].getMessage()+ String.valueOf(getClanHall().getFunction(ClanHall.FUNC_SUPPORT).getLvl()) + "</font> (<font color=\"FFAABB\">"+String.valueOf(getClanHall().getFunction(ClanHall.FUNC_SUPPORT).getLease())+"</font>"+MessageTable.Messages[586].getMessage()+" /"+String.valueOf(Config.CH_SUPPORT_FEE_RATIO/1000/60/60/24)+MessageTable.Messages[587].getMessage()+")");
							html.replace("%support_period%",MessageTable.Messages[594].getMessage() + format.format(getClanHall().getFunction(ClanHall.FUNC_SUPPORT).getEndTime()));
							int grade = getClanHall().getGrade();
							switch (grade)
							{
								case 0:
									/* Move To MessageTable For L2JTW
									html.replace("%change_support%","[<a action=\"bypass -h npc_%objectId%_manage other support_cancel\">Deactivate</a>]"+support_grade0);
									*/
									html.replace("%change_support%","[<a action=\"bypass -h npc_%objectId%_manage other support_cancel\">"+MessageTable.Messages[595].getMessage()+"</a>]"+support_grade0);
									break;
								case 1:
									/* Move To MessageTable For L2JTW
									html.replace("%change_support%","[<a action=\"bypass -h npc_%objectId%_manage other support_cancel\">Deactivate</a>]"+support_grade1);
									*/
									html.replace("%change_support%","[<a action=\"bypass -h npc_%objectId%_manage other support_cancel\">"+MessageTable.Messages[595].getMessage()+"</a>]"+support_grade1);
									break;
								case 2:
									/* Move To MessageTable For L2JTW
									html.replace("%change_support%","[<a action=\"bypass -h npc_%objectId%_manage other support_cancel\">Deactivate</a>]"+support_grade2);
									*/
									html.replace("%change_support%","[<a action=\"bypass -h npc_%objectId%_manage other support_cancel\">"+MessageTable.Messages[595].getMessage()+"</a>]"+support_grade2);
									break;
								case 3:
									/* Move To MessageTable For L2JTW
									html.replace("%change_support%","[<a action=\"bypass -h npc_%objectId%_manage other support_cancel\">Deactivate</a>]"+support_grade3);
									*/
									html.replace("%change_support%","[<a action=\"bypass -h npc_%objectId%_manage other support_cancel\">"+MessageTable.Messages[595].getMessage()+"</a>]"+support_grade3);
									break;
							}
						}
						else
						{
							/* Move To MessageTable For L2JTW
							html.replace("%support%", "none");
							html.replace("%support_period%", "none");
							*/
							html.replace("%support%", MessageTable.Messages[596].getMessage());
							html.replace("%support_period%", MessageTable.Messages[596].getMessage());
							int grade = getClanHall().getGrade();
							switch (grade)
							{
								case 0:
									html.replace("%change_support%",support_grade0);
									break;
								case 1:
									html.replace("%change_support%",support_grade1);
									break;
								case 2:
									html.replace("%change_support%",support_grade2);
									break;
								case 3:
									html.replace("%change_support%",support_grade3);
									break;
							}
						}
						if (getClanHall().getFunction(ClanHall.FUNC_ITEM_CREATE) != null)
						{
							/* Move To MessageTable For L2JTW
							html.replace("%item%","Stage "+ String.valueOf(getClanHall().getFunction(ClanHall.FUNC_ITEM_CREATE).getLvl()) + "</font> (<font color=\"FFAABB\">"+String.valueOf(getClanHall().getFunction(ClanHall.FUNC_ITEM_CREATE).getLease())+"</font>Adena /"+String.valueOf(Config.CH_ITEM_FEE_RATIO/1000/60/60/24)+" Day)");
							html.replace("%item_period%","Withdraw the fee for the next time at " + format.format(getClanHall().getFunction(ClanHall.FUNC_ITEM_CREATE).getEndTime()));
							html.replace("%change_item%","[<a action=\"bypass -h npc_%objectId%_manage other item_cancel\">Deactivate</a>]"+item);
							*/
							html.replace("%item%",MessageTable.Messages[603].getMessage()+ String.valueOf(getClanHall().getFunction(ClanHall.FUNC_ITEM_CREATE).getLvl()) + "</font> (<font color=\"FFAABB\">"+String.valueOf(getClanHall().getFunction(ClanHall.FUNC_ITEM_CREATE).getLease())+"</font>"+MessageTable.Messages[586].getMessage()+" /"+String.valueOf(Config.CH_ITEM_FEE_RATIO/1000/60/60/24)+MessageTable.Messages[587].getMessage()+")");
							html.replace("%item_period%",MessageTable.Messages[594].getMessage() + format.format(getClanHall().getFunction(ClanHall.FUNC_ITEM_CREATE).getEndTime()));
							html.replace("%change_item%","[<a action=\"bypass -h npc_%objectId%_manage other item_cancel\">"+MessageTable.Messages[595].getMessage()+"</a>]"+item);
						}
						else
						{
							/* Move To MessageTable For L2JTW
							html.replace("%item%", "none");
							html.replace("%item_period%", "none");
							*/
							html.replace("%item%", MessageTable.Messages[596].getMessage());
							html.replace("%item_period%", MessageTable.Messages[596].getMessage());
							html.replace("%change_item%",item);
						}
						sendHtmlMessage(player, html);
					}
					else if (val.equalsIgnoreCase("deco") && !getClanHall().isSiegableHall())
					{
						if (st.countTokens() >= 1)
						{
							if(getClanHall().getOwnerId() == 0)
							{
								/* Move To MessageTable For L2JTW
								player.sendMessage("This clan Hall have no owner, you cannot change configuration");
								*/
								player.sendMessage(585);
								return;
							}
							val = st.nextToken();
							if (val.equalsIgnoreCase("curtains_cancel"))
							{
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-cancel.htm");
								html.replace("%apply%", "deco curtains 0");
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("fixtures_cancel"))
							{
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-cancel.htm");
								html.replace("%apply%", "deco fixtures 0");
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("edit_curtains"))
							{
								val = st.nextToken();
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-apply.htm");
								/* Move To MessageTable For L2JTW
								html.replace("%name%", "Curtains (Decoration)");
								*/
								html.replace("%name%", MessageTable.Messages[606].getMessage());
								int stage = Integer.parseInt(val);
								int cost;
								switch (stage)
								{
									case 1:
										cost = Config.CH_CURTAIN1_FEE;
										break;
									default:
										cost = Config.CH_CURTAIN2_FEE;
										break;
								}
								/* Move To MessageTable For L2JTW
								html.replace("%cost%", String.valueOf(cost)+"</font>Adena /"+String.valueOf(Config.CH_CURTAIN_FEE_RATIO/1000/60/60/24)+" Day</font>)");
								html.replace("%use%","These curtains can be used to decorate the clan hall.");
								*/
								html.replace("%cost%", String.valueOf(cost)+"</font>"+MessageTable.Messages[586].getMessage()+" /"+String.valueOf(Config.CH_CURTAIN_FEE_RATIO/1000/60/60/24)+MessageTable.Messages[587].getMessage()+"</font>)");
								html.replace("%use%",MessageTable.Messages[607].getMessage());
								html.replace("%apply%", "deco curtains " + String.valueOf(stage));
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("edit_fixtures"))
							{
								val = st.nextToken();
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-apply.htm");
								/* Move To MessageTable For L2JTW
								html.replace("%name%", "Front Platform (Decoration)");
								*/
								html.replace("%name%", MessageTable.Messages[608].getMessage());
								int stage = Integer.parseInt(val);
								int cost;
								switch (stage)
								{
									case 1:
										cost = Config.CH_FRONT1_FEE;
										break;
									default:
										cost = Config.CH_FRONT2_FEE;
										break;
								}
								/* Move To MessageTable For L2JTW
								html.replace("%cost%", String.valueOf(cost)+"</font>Adena /"+String.valueOf(Config.CH_FRONT_FEE_RATIO/1000/60/60/24)+" Day</font>)");
								html.replace("%use%","Used to decorate the clan hall.");
								*/
								html.replace("%cost%", String.valueOf(cost)+"</font>"+MessageTable.Messages[586].getMessage()+" /"+String.valueOf(Config.CH_FRONT_FEE_RATIO/1000/60/60/24)+MessageTable.Messages[587].getMessage()+"</font>)");
								html.replace("%use%",MessageTable.Messages[609].getMessage());
								html.replace("%apply%", "deco fixtures " + String.valueOf(stage));
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("curtains"))
							{
								if (st.countTokens() >= 1)
								{
									int fee;
									if (Config.DEBUG) _log.warning("Deco curtains editing invoked");
									val = st.nextToken();
									NpcHtmlMessage html = new NpcHtmlMessage(1);
									html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-apply_confirmed.htm");
									if (getClanHall().getFunction(ClanHall.FUNC_DECO_CURTAINS) != null)
									{
										if (getClanHall().getFunction(ClanHall.FUNC_DECO_CURTAINS).getLvl() == Integer.parseInt(val))
										{
											html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-used.htm");
											/* Move To MessageTable For L2JTW
											html.replace("%val%","Stage " +String.valueOf(val));
											*/
											html.replace("%val%",MessageTable.Messages[603].getMessage() +String.valueOf(val));
											sendHtmlMessage(player, html);
											return;
										}
									}
									int lvl = Integer.parseInt(val);
									switch (lvl)
									{
										case 0:
											fee = 0;
											html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-cancel_confirmed.htm");
											break;
										case 1:
											fee = Config.CH_CURTAIN1_FEE;
											break;
										default:
											fee = Config.CH_CURTAIN2_FEE;
											break;
									}
									if (!getClanHall().updateFunctions(player,ClanHall.FUNC_DECO_CURTAINS, lvl, fee, Config.CH_CURTAIN_FEE_RATIO,(getClanHall().getFunction(ClanHall.FUNC_DECO_CURTAINS) == null)))
									{
										html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/low_adena.htm");
										sendHtmlMessage(player, html);
									}
									else
										revalidateDeco(player);
									sendHtmlMessage(player, html);
								}
								return;
							}
							else if (val.equalsIgnoreCase("fixtures"))
							{
								if (st.countTokens() >= 1)
								{
									int fee;
									if (Config.DEBUG) _log.warning("Deco fixtures editing invoked");
									val = st.nextToken();
									NpcHtmlMessage html = new NpcHtmlMessage(1);
									html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-apply_confirmed.htm");
									if (getClanHall().getFunction(ClanHall.FUNC_DECO_FRONTPLATEFORM) != null)
									{
										if (getClanHall().getFunction(ClanHall.FUNC_DECO_FRONTPLATEFORM).getLvl() == Integer.parseInt(val))
										{
											html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-used.htm");
											/* Move To MessageTable For L2JTW
											html.replace("%val%","Stage " +String.valueOf(val));
											*/
											html.replace("%val%",MessageTable.Messages[603].getMessage() +String.valueOf(val));
											sendHtmlMessage(player, html);
											return;
										}
									}
									int lvl = Integer.parseInt(val);
									switch (lvl)
									{
										case 0:
											fee = 0;
											html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/functions-cancel_confirmed.htm");
											break;
										case 1:
											fee = Config.CH_FRONT1_FEE;
											break;
										default:
											fee = Config.CH_FRONT2_FEE;
											break;
									}
									if (!getClanHall().updateFunctions(player,ClanHall.FUNC_DECO_FRONTPLATEFORM, lvl, fee, Config.CH_FRONT_FEE_RATIO,(getClanHall().getFunction(ClanHall.FUNC_DECO_FRONTPLATEFORM) == null)))
									{
										html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/low_adena.htm");
										sendHtmlMessage(player, html);
									}
									else
										revalidateDeco(player);
									sendHtmlMessage(player, html);
								}
								return;
							}
						}
						NpcHtmlMessage html = new NpcHtmlMessage(1);
						html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/deco.htm");
						/* Move To MessageTable For L2JTW
						String curtains = "[<a action=\"bypass -h npc_%objectId%_manage deco edit_curtains 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage deco edit_curtains 2\">Level 2</a>]";
						String fixtures = "[<a action=\"bypass -h npc_%objectId%_manage deco edit_fixtures 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage deco edit_fixtures 2\">Level 2</a>]";
						*/
						String curtains = "[<a action=\"bypass -h npc_%objectId%_manage deco edit_curtains 1\">"+MessageTable.Messages[605].getMessage()+"1</a>][<a action=\"bypass -h npc_%objectId%_manage deco edit_curtains 2\">"+MessageTable.Messages[605].getMessage()+"2</a>]";
						String fixtures = "[<a action=\"bypass -h npc_%objectId%_manage deco edit_fixtures 1\">"+MessageTable.Messages[605].getMessage()+"1</a>][<a action=\"bypass -h npc_%objectId%_manage deco edit_fixtures 2\">"+MessageTable.Messages[605].getMessage()+"2</a>]";
						if (getClanHall().getFunction(ClanHall.FUNC_DECO_CURTAINS) != null)
						{
							/* Move To MessageTable For L2JTW
							html.replace("%curtain%","Stage "+ String.valueOf(getClanHall().getFunction(ClanHall.FUNC_DECO_CURTAINS).getLvl()) + "</font> (<font color=\"FFAABB\">"+String.valueOf(getClanHall().getFunction(ClanHall.FUNC_DECO_CURTAINS).getLease())+"</font>Adena /"+String.valueOf(Config.CH_CURTAIN_FEE_RATIO/1000/60/60/24)+" Day)");
							html.replace("%curtain_period%","Withdraw the fee for the next time at " + format.format(getClanHall().getFunction(ClanHall.FUNC_DECO_CURTAINS).getEndTime()));
							html.replace("%change_curtain%","[<a action=\"bypass -h npc_%objectId%_manage deco curtains_cancel\">Deactivate</a>]"+curtains);
							*/
							html.replace("%curtain%",MessageTable.Messages[603].getMessage()+ String.valueOf(getClanHall().getFunction(ClanHall.FUNC_DECO_CURTAINS).getLvl()) + "</font> (<font color=\"FFAABB\">"+String.valueOf(getClanHall().getFunction(ClanHall.FUNC_DECO_CURTAINS).getLease())+"</font>"+MessageTable.Messages[586].getMessage()+" /"+String.valueOf(Config.CH_CURTAIN_FEE_RATIO/1000/60/60/24)+MessageTable.Messages[587].getMessage()+")");
							html.replace("%curtain_period%",MessageTable.Messages[594].getMessage() + format.format(getClanHall().getFunction(ClanHall.FUNC_DECO_CURTAINS).getEndTime()));
							html.replace("%change_curtain%","[<a action=\"bypass -h npc_%objectId%_manage deco curtains_cancel\">"+MessageTable.Messages[595].getMessage()+"</a>]"+curtains);
						}
						else
						{
							/* Move To MessageTable For L2JTW
							html.replace("%curtain%", "none");
							html.replace("%curtain_period%", "none");
							*/
							html.replace("%curtain%", MessageTable.Messages[596].getMessage());
							html.replace("%curtain_period%", MessageTable.Messages[596].getMessage());
							html.replace("%change_curtain%",curtains);
						}
						if (getClanHall().getFunction(ClanHall.FUNC_DECO_FRONTPLATEFORM) != null)
						{
							/* Move To MessageTable For L2JTW
							html.replace("%fixture%","Stage "+ String.valueOf(getClanHall().getFunction(ClanHall.FUNC_DECO_FRONTPLATEFORM).getLvl()) + "</font> (<font color=\"FFAABB\">"+String.valueOf(getClanHall().getFunction(ClanHall.FUNC_DECO_FRONTPLATEFORM).getLease())+"</font>Adena /"+String.valueOf(Config.CH_FRONT_FEE_RATIO/1000/60/60/24)+" Day)");
							html.replace("%fixture_period%", "Withdraw the fee for the next time at " + format.format(getClanHall().getFunction(ClanHall.FUNC_DECO_FRONTPLATEFORM).getEndTime()));
							html.replace("%change_fixture%","[<a action=\"bypass -h npc_%objectId%_manage deco fixtures_cancel\">Deactivate</a>]"+fixtures);
							*/
							html.replace("%fixture%",MessageTable.Messages[603].getMessage()+ String.valueOf(getClanHall().getFunction(ClanHall.FUNC_DECO_FRONTPLATEFORM).getLvl()) + "</font> (<font color=\"FFAABB\">"+String.valueOf(getClanHall().getFunction(ClanHall.FUNC_DECO_FRONTPLATEFORM).getLease())+"</font>"+MessageTable.Messages[586].getMessage()+" /"+String.valueOf(Config.CH_FRONT_FEE_RATIO/1000/60/60/24)+MessageTable.Messages[587].getMessage()+")");
							html.replace("%fixture_period%", MessageTable.Messages[594].getMessage() + format.format(getClanHall().getFunction(ClanHall.FUNC_DECO_FRONTPLATEFORM).getEndTime()));
							html.replace("%change_fixture%","[<a action=\"bypass -h npc_%objectId%_manage deco fixtures_cancel\">"+MessageTable.Messages[595].getMessage()+"</a>]"+fixtures);
						}
						else
						{
							/* Move To MessageTable For L2JTW
							html.replace("%fixture%", "none");
							html.replace("%fixture_period%", "none");
							*/
							html.replace("%fixture%", MessageTable.Messages[596].getMessage());
							html.replace("%fixture_period%", MessageTable.Messages[596].getMessage());
							html.replace("%change_fixture%",fixtures);
						}
						sendHtmlMessage(player, html);
					}
					else if (val.equalsIgnoreCase("back"))
						showChatWindow(player);
					else
					{
						NpcHtmlMessage html = new NpcHtmlMessage(1);
						html.setFile(player.getHtmlPrefix(), getClanHall().isSiegableHall()? "data/html/clanHallManager/manage_siegable.htm" : "data/html/clanHallManager/manage.htm");
						sendHtmlMessage(player, html);
					}
				}
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/not_authorized.htm");
					sendHtmlMessage(player, html);
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("support"))
			{
				if (player.isCursedWeaponEquipped())
				{
					// Custom system message
					/* Move To MessageTable For L2JTW
					player.sendMessage("The wielder of a cursed weapon cannot receive outside heals or buffs");
					*/
					player.sendMessage(610);
					return;
				}
				setTarget(player);
				L2Skill skill;
				if (val.isEmpty()) return;
				
				try
				{
					int skill_id = Integer.parseInt(val);
					try
					{
						int skill_lvl = 0;
						if (st.countTokens() >= 1) skill_lvl = Integer.parseInt(st.nextToken());
						skill = SkillTable.getInstance().getInfo(skill_id,skill_lvl);
						if (skill.getSkillType() == L2SkillType.SUMMON)
						{
							player.doSimultaneousCast(skill);
						}
						else
						{
							final int mpCost = skill.getMpConsume() + skill.getMpInitialConsume();
							//If Clan Hall Buff are free or current MP is greater than MP cost, the skill should be casted.
							if ((getCurrentMp() >= mpCost) || Config.CH_BUFF_FREE)
							{
								doCast(skill);
							}
							else
							{
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/support-no_mana.htm");
								html.replace("%mp%", String.valueOf((int) getCurrentMp()));
								sendHtmlMessage(player, html);
								return;
							}
						}
						if (getClanHall().getFunction(ClanHall.FUNC_SUPPORT) == null)
							return;
						NpcHtmlMessage html = new NpcHtmlMessage(1);
						if(getClanHall().getFunction(ClanHall.FUNC_SUPPORT).getLvl() == 0)
							return;
						html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/support-done.htm");
						html.replace("%mp%", String.valueOf((int) getCurrentMp()));
						sendHtmlMessage(player, html);
					}
					catch (Exception e)
					{
						/* Move To MessageTable For L2JTW
						player.sendMessage("Invalid skill level, contact your admin!");
						*/
						player.sendMessage(611);
					}
				}
				catch (Exception e)
				{
					/* Move To MessageTable For L2JTW
					player.sendMessage("Invalid skill level, contact your admin!");
					*/
					player.sendMessage(611);
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("list_back"))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				String file = "data/html/clanHallManager/chamberlain-"+getNpcId()+".htm";
				if(!HtmCache.getInstance().isLoadable(file))
					file = "data/html/clanHallManager/chamberlain.htm";
				html.setFile(player.getHtmlPrefix(), file);
				html.replace("%objectId%", String.valueOf(this.getObjectId()));
				html.replace("%npcname%", this.getName());
				sendHtmlMessage(player, html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("support_back"))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				if(getClanHall().getFunction(ClanHall.FUNC_SUPPORT).getLvl() == 0)
					return;
				html.setFile(player.getHtmlPrefix(), "data/html/clanHallManager/support" + getClanHall().getFunction(ClanHall.FUNC_SUPPORT).getLvl()+".htm");
				html.replace("%mp%", String.valueOf((int)getStatus().getCurrentMp()));
				sendHtmlMessage(player, html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("goto"))
			{
				int whereTo = Integer.parseInt(val);
				doTeleport(player, whereTo);
				return;
			}
		}
		super.onBypassFeedback(player, command);
	}
	
	private void sendHtmlMessage(L2PcInstance player, NpcHtmlMessage html)
	{
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcId%", String.valueOf(getNpcId()));
		player.sendPacket(html);
	}
	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/clanHallManager/chamberlain-no.htm";
		
		int condition = validateCondition(player);
		if (condition == COND_OWNER)
		{
			filename = "data/html/clanHallManager/chamberlain-"+getNpcId()+".htm";
			if(!HtmCache.getInstance().isLoadable(filename))
				filename = "data/html/clanHallManager/chamberlain.htm";// Owner message window
		}
		else if (condition == COND_OWNER_FALSE)
			filename = "data/html/clanHallManager/chamberlain-of.htm";
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(player.getHtmlPrefix(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcId%", String.valueOf(getNpcId()));
		player.sendPacket(html);
	}
	
	protected int validateCondition(L2PcInstance player)
	{
		if (getClanHall() == null) return COND_ALL_FALSE;
		if (player.isGM()) return COND_OWNER;
		if (player.getClan() != null)
		{
			if (getClanHall().getOwnerId() == player.getClanId())
				return COND_OWNER;
			return COND_OWNER_FALSE;
		}
		return COND_ALL_FALSE;
	}
	
	/**
	 * @return the L2ClanHall this L2NpcInstance belongs to.
	 */
	public final ClanHall getClanHall()
	{
		if (_clanHallId < 0)
		{
			ClanHall temp = ClanHallManager.getInstance().getNearbyClanHall(getX(), getY(), 500);
			if(temp == null)
				temp = CHSiegeManager.getInstance().getNearbyClanHall(this);
			
			if (temp != null)
				_clanHallId = temp.getId();
				
			if (_clanHallId < 0) return null;
		}
		return ClanHallManager.getInstance().getClanHallById(_clanHallId);
	}
	
	private void doTeleport(L2PcInstance player, int val)
	{
		if(Config.DEBUG)
			_log.warning("doTeleport(L2PcInstance player, int val) is called");
		L2TeleportLocation list = TeleportLocationTable.getInstance().getTemplate(val);
		if (list != null)
		{
			if (player.isCombatFlagEquipped())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD);
				return;
			}
			else if (player.destroyItemByItemId("Teleport", list.getItemId(), list.getPrice(), this, true))
			{
				if (Config.DEBUG)
					_log.warning("Teleporting player "+player.getName()+" for CH to new location: "+list.getLocX()+":"+list.getLocY()+":"+list.getLocZ());
				player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ());
			}
		}
		else
		{
			_log.warning("No teleport destination with id:" +val);
		}
		player.sendPacket( ActionFailed.STATIC_PACKET );
	}
	
	private void revalidateDeco(L2PcInstance player)
	{
		AuctionableHall ch = ClanHallManager.getInstance().getClanHallByOwner(player.getClan());
		if (ch == null) return;
		AgitDecoInfo bl = new AgitDecoInfo(ch);
		player.sendPacket(bl);
	}
}
