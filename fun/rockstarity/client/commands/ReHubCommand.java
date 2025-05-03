package fun.rockstarity.client.commands;

import fun.rockstarity.api.commands.CmdInfo;
import fun.rockstarity.api.commands.Command;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventWorldChange;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.client.modules.other.KTLeave;
import lombok.Setter;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SOpenWindowPacket;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.util.Hand;
import net.minecraft.world.Difficulty;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author Malecharik
 * @since 21 Mar 2024 23:42:04
 */
@NativeInclude
@Setter
@CmdInfo(names = {"rct", "rehub", "ркт"}, desc = "Перезаходит на сервер")
public class ReHubCommand extends Command {
	
	private boolean processing, clicked;
	protected int numberPvp, numberAn;
	protected boolean hvh, lite;
	protected String pref;
	protected final TimerUtility timer = new TimerUtility();
	
	@Override
	public void execute(String[] args) {
		if (Server.hasCT() && !rock.getModules().get(KTLeave.class).get()) {
			Chat.msg("Вы не можете перезаходить на сервер во время PVP"); 
			return;
		}

		if (Server.isFT() || Server.isRW() || Server.isFS() || Server.isHW() || Server.is("spooky")) {
			String tabHeader = mc.ingameGUI.getTabList().getHeader().getString();
			
			if (Server.isHW()) {
				lite = tabHeader.contains("Лайт");
				String lightInfo = tabHeader.split("▶")[1].replace("Анархия", "").trim();
				pref = lightInfo.split("Лайт")[0].trim();
				numberAn = Integer.parseInt(lightInfo.split("#")[1].trim());
				//pref = tabHeader.split()[1];
			}
			
			if (Server.isFS()) {
				try {
					numberPvp = Integer.parseInt(tabHeader.split("Анархия PvP #")[1].trim());
				} catch (Exception e) {
					
				}
				hvh = tabHeader.contains("Анархия HvH");
			}
			timer.reset();
			
			if (!rock.getModules().get(KTLeave.class).get() && (mc.world.getDifficulty() != Difficulty.EASY || !Server.isFT()))
				mc.player.sendChatMessage("/hub");
			
			this.processing = true;
		}
	}
	
	@Override
	public void onEvent(Event event) {
		if (!this.processing) return;
		
		if (event instanceof EventUpdate && (Server.isFT() || Server.is("spooky")) && mc.world.getDifficulty() == Difficulty.EASY) {
			mc.player.sendChatMessage("/an" + Server.FT_ANARCHY);
			this.processing = false;
		}
		
		
		if (event instanceof EventWorldChange && Server.isHW() && lite && processing) {
			if (mc.world.getDifficulty() == Difficulty.EASY) {
				processing = false;
			}
		}
		
		if (event instanceof EventReceivePacket e) {
			if (Server.isHW() && lite && processing) {
				if (e.getPacket() instanceof SOpenWindowPacket packet) {
					if (packet.getTitle().getString().contains("Выберите режим")) {
						mc.player.connection.sendPacket(new CClickWindowPacket(packet.getWindowId(),
								12, 0, ClickType.PICKUP,
								mc.player.openContainer.getSlot(12).getStack(),
								mc.player.openContainer.getNextTransactionID(mc.player.inventory)));
					}
					
					if (packet.getTitle().getString().contains("Выбор Лайт анархии")) {
						int slotToClick = -1;
						System.out.println(pref);
						switch (pref) {
		                    case "Соло":
		                        slotToClick = 0;
		                        break;
		                    case "Дуо":
		                        slotToClick = 1;
		                        break;
		                    case "Трио":
		                        slotToClick = 2;
		                        break;
		                    case "Клан":
		                        slotToClick = 3;
		                        break;
						}
						
						if (slotToClick != -1) {
							mc.player.connection.sendPacket(new CClickWindowPacket(packet.getWindowId(),
		                            slotToClick, 0, ClickType.PICKUP,
		                            mc.player.openContainer.getSlot(slotToClick).getStack(),
		                            mc.player.openContainer.getNextTransactionID(mc.player.inventory)));
							
						}
						
						int slotToClicks = -1;
						
						 switch (pref) {
		                    case "Соло":
		                    	slotToClicks = numberAn + 17;
		                        break;
		                    case "Дуо":
		                    	slotToClicks = (numberAn - 14) + 17;
		                        break;
		                    case "Трио":
		                    	slotToClicks = (numberAn - 31) + 17;
		                        break;
		                    case "Клан":
		                    	slotToClicks = (numberAn - 45) + 17;
		                        break;
		                }
						 
						 System.out.println(slotToClick);
		                    mc.player.connection.sendPacket(new CClickWindowPacket(packet.getWindowId(),
		                    		slotToClicks, 0, ClickType.PICKUP,
		                        mc.player.openContainer.getSlot(slotToClicks).getStack(),
		                        mc.player.openContainer.getNextTransactionID(mc.player.inventory)));
					}
				}
			}
			
			if (Server.isRW()) {
				if (mc.world.getDifficulty() == Difficulty.EASY) {
					if (e.getPacket() instanceof SOpenWindowPacket packet) {
						if (packet.getTitle().getString().contains("Выбор сервера")) {
							mc.player.connection.sendPacket(new CClickWindowPacket(packet.getWindowId(),
									21, 0, ClickType.PICKUP,
									mc.player.openContainer.getSlot(21).getStack(),
									mc.player.openContainer.getNextTransactionID(mc.player.inventory)));
							e.cancel();
						}
						
						if (packet.getTitle().getString().contains("Выбор мира грифа")) {
			            	int slot = Server.RW_GRIEF + (int) (Server.RW_GRIEF / 7 * 2);
			            	
							mc.player.connection.sendPacket(new CClickWindowPacket(packet.getWindowId(),
									slot, 0, ClickType.PICKUP,
									mc.player.openContainer.getSlot(slot).getStack(),
									mc.player.openContainer.getNextTransactionID(mc.player.inventory)));
							e.cancel();
							this.processing = false;
						}
					}
				}
			}
			
			if (Server.isFS()) {

				if (mc.world.getDifficulty() == Difficulty.NORMAL) {
					if (e.getPacket() instanceof SSetSlotPacket packet) {
						ItemStack stack = packet.getStack();
						
						if (!hvh) {
							// Если предмет тот, по которому надо кликнуть
							if (stack.getDisplayName().getString().contains("Анархия PvP")) {
								mc.player.connection.sendPacket(new CClickWindowPacket(packet.getWindowId(),
											14, 0, ClickType.PICKUP,
											mc.player.openContainer.getSlot(14).getStack(),
											mc.player.openContainer.getNextTransactionID(mc.player.inventory)));
							}
							
							if (stack.getDisplayName().getString().contains("Анархия PvP #" + numberPvp)) {
								mc.player.connection.sendPacket(new CClickWindowPacket(packet.getWindowId(),
											19 + numberPvp, 0, ClickType.PICKUP,
											mc.player.openContainer.getSlot(19 + numberPvp).getStack(),
											mc.player.openContainer.getNextTransactionID(mc.player.inventory)));
								processing = false;
							}
						} else {
							if (stack.getDisplayName().getString().contains("Анархия HvH")) {
								mc.player.connection.sendPacket(new CClickWindowPacket(packet.getWindowId(),
											13, 0, ClickType.PICKUP,
											mc.player.openContainer.getSlot(13).getStack(),
											mc.player.openContainer.getNextTransactionID(mc.player.inventory)));
								
								this.processing = false;
							}
						}
					}
				}
			
			}
 		}
		
		if (event instanceof EventUpdate) {
			if (Server.isFS()) {
				if (mc.world.getDifficulty() == Difficulty.NORMAL) {
					if (Player.findItem(9, Items.COMPASS) != -1 && mc.player.ticksExisted % 50 == 2) {
						int slot = Player.findItem(Items.COMPASS);
						mc.player.connection.sendPacket(new CHeldItemChangePacket(slot));
						mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
					}
				}
			}
		}
		
		if (event instanceof EventWorldChange) {
			if (Server.isHW() && mc.world.getDifficulty() == Difficulty.PEACEFUL) {
				mc.player.sendChatMessage("/menu");
			}
			
			if (Server.isHW() && mc.world.getDifficulty() == Difficulty.HARD) {
				processing = false;
			}
			
			if (Server.isRW()) {
				if (mc.world.getDifficulty() == Difficulty.EASY) {
					if (Player.findItem(9, Items.COMPASS) != -1) {
						int slot = Player.findItem(Items.COMPASS);
						mc.player.connection.sendPacket(new CHeldItemChangePacket(slot));
						mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
					}
				}
			}
		}
	}
}
