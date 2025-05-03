package fun.rockstarity.api.autobuy.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import fun.rockstarity.api.autobuy.AutoBuy;
import fun.rockstarity.api.autobuy.logic.interfaces.ILogicHandler;
import fun.rockstarity.api.autobuy.logic.items.AutoBuyItem;
import fun.rockstarity.api.autobuy.logic.items.MinecraftItem;
import fun.rockstarity.api.autobuy.logic.tasks.CheckTask;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.player.EventMessage;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.game.ItemUtility;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.system.TextUtility;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.network.play.server.SOpenWindowPacket;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.Difficulty;

/**
 * @author ConeTin
 * @since 20 апр. 2024 г.
 */


@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlayerLogic implements ILogicHandler {

	String ah;
	final AutoBuy autoBuy;

	final TimerUtility openAucTimer = new TimerUtility();
	final TimerUtility resetTimer = new TimerUtility();
	final TimerUtility antiAfkTimer = new TimerUtility();
	final TimerUtility updateTimer = new TimerUtility();
	
	final List<AutoBuyItem> toSell = new ArrayList<>();
	final TimerUtility buyTimer = new TimerUtility();
	boolean selling;
	
	boolean refresh;
	String lastAn;

	public PlayerLogic(AutoBuy autoBuy) {
		this.autoBuy = autoBuy;
	}
	
	@Override
	public void onEvent(Event event) {
		fun.rockstarity.client.modules.other.AutoBuy module = rock.getModules().get(fun.rockstarity.client.modules.other.AutoBuy.class);
		
		String an1 = module.getAn1().get();
		String an2 = module.getAn2().get();
		
		if (rock.getBotsHandler().getBots().isEmpty()
				|| module.getParser().get()) return;
		
		if (event instanceof EventUpdate) {
			if (mc.world.getDifficulty() == Difficulty.EASY) {
				if (openAucTimer.passed(2500)) {
					mc.player.sendChatMessage("/an" + Server.FT_ANARCHY);
					openAucTimer.reset();
				}
			}
			
			if (selling && buyTimer.passed(300)) {
				for (int i = 0; i < 44; i++) {
	                ItemStack stack = mc.player.inventory.getStackInSlot(i);
	                for (AutoBuyItem item : rock.getAutoBuy().getItems()) {
	        			for (MinecraftItem item1 : item.getItems()) {
	        				if (stack.getItem() == item1.getItem()
	        						//&& updateTimer.passed(100)
	        						) {
	        					if (item.getEnchants().entrySet().isEmpty()) {
	        						mc.player.closeScreen();
	        						if (i < 9) {
	        			 				mc.player.connection.sendPacket(new CHeldItemChangePacket(i));
	        			 				mc.player.inventory.currentItem = i;
	        						} else {
	        							mc.playerController.pickItem(i);
	        						}
	        						mc.player.sendChatMessage("/ah sell " + item.getSellPrice());
	        						resetTimer.reset();
	        						openAucTimer.reset();
	        						break;
	        					}
	        					
	        					for (Entry<String, Integer> set : item.getEnchants().entrySet()) {
	        	    	    	    int enchantmentId = Integer.parseInt(set.getKey().replace(set.getValue() +  "", ""));
	        	    	    	    Enchantment enchantment = Enchantment.getEnchantmentByID(enchantmentId);
	        	    	    	    if (enchantment != null) {
	        	    	    	        if (EnchantmentHelper.getEnchantmentLevel(enchantment, stack) >= set.getValue()) {
	        	    	    	        	if (Enchantment.getEnchantmentByID(Integer.parseInt(set.getKey().replace(set.getValue() +  "", ""))) == enchantment) {
	        	    	    	        		mc.player.closeScreen();
	        	    	    	        		if (i < 9) {
	        	        			 				mc.player.connection.sendPacket(new CHeldItemChangePacket(i));
	        	        			 				mc.player.inventory.currentItem = i;
	        	        						} else {
	        	        							mc.playerController.pickItem(i);
	        	        						}
	        	        						mc.player.sendChatMessage("/ah sell " + item.getSellPrice());
	        	        						resetTimer.reset();
	        	        						openAucTimer.reset();
	        	        						break;
	        			    	    		}
	        			    	        }
	        			    	    }
	        			    	}
	        				}
	        			}
	        		}
	            }
				
				selling = false;
			}
			
			if (mc.player.openContainer instanceof ChestContainer) {
				ChestScreen chest = (ChestScreen) mc.currentScreen;
				String chestName = chest.getTitle().getString();

				if (chestName.contains("Аукционы") || chestName.contains("Поиск")) {
					if (refresh) {
						mc.player.connection.sendPacket(new CClickWindowPacket(mc.player.openContainer.windowId, 46, 0, ClickType.SWAP, mc.player.openContainer.getSlot(46).getStack(), mc.player.openContainer.getNextTransactionID(mc.player.inventory)));
						refresh = false;
					}
					
					if (//(resetTimer.passed(5000) ? updateTimer.passed((long)MathUtility.random(3025, 3215)) : 
						updateTimer.passed(module.getSpeed().get()) && !autoBuy.getTaskManager().haveTasks() && !selling) {
						//Chat.debug(player.getNameClear() + ": Обновляю аукцион..");
						//player.connection.sendPacket(new CClickWindowPacket(player.openContainer.windowId, 49, 0, ClickType.PICKUP, player.openContainer.getSlot(49).getStack(), player.openContainer.getNextTransactionID(player.inventory)));
						//if (Math.random() < 0.3f) {
						//}
						//mc.player.connection.sendPacket(new CClickWindowPacket(mc.player.openContainer.windowId, 48, 0, ClickType.PICKUP, mc.player.openContainer.getSlot(48).getStack(), mc.player.openContainer.getNextTransactionID(mc.player.inventory)));

						updateTimer.reset();
//						if (resetTimer.passed(5000) && ah != null) {
//							Chat.debug("reload");
//							mc.player.closeScreen();
//							mc.player.sendChatMessage(ah);
//							resetTimer.reset();
//						} else {
							mc.player.connection.sendPacket(new CClickWindowPacket(mc.player.openContainer.windowId, 49, 0, ClickType.SWAP, mc.player.openContainer.getSlot(49).getStack(), mc.player.openContainer.getNextTransactionID(mc.player.inventory)));

//						}
						//player.swingArm(Hand.MAIN_HAND);
					}
					
					for (Slot s : mc.player.openContainer.inventorySlots) {
						ItemStack stack = s.getStack();
						String displayName = stack.getDisplayName().getString().toLowerCase();
						
						if (canBuy(stack)) {
							String name = stack.getDisplayName().getString();
							int price = ItemUtility.getPrice(stack);
							String seller = ItemUtility.getSeller(stack);
							
							for (CheckTask task : autoBuy.getTaskManager())
								if (task.getSeller().equals(seller)
										&& task.getName().equals(name)
										&& task.getPrice() == price) return;
							
							for (CheckTask task : autoBuy.getTaskManager().getBlackList())
								if (task.getSeller().equals(seller)
										&& task.getName().equals(name)
										&& task.getPrice() == price) return;
							
            				Chat.debug(TextFormatting.GRAY + "Отправляю предмет " + name + " за " + price + " на проверку");

            				resetTimer.reset();
            				
							autoBuy.getTaskManager().createTask(seller, name, price, () -> {
								buy(seller, name, price, s);
							});
						}
					}
				} else if (chestName.contains("Хранилище")) {
					mc.player.connection.sendPacket(new CClickWindowPacket(mc.player.openContainer.windowId, 52, 0, ClickType.SWAP, mc.player.openContainer.getSlot(52).getStack(), mc.player.openContainer.getNextTransactionID(mc.player.inventory)));
					mc.player.closeScreen();
				} else {
					updateTimer.reset();
				}
			} else {
				if (openAucTimer.passed(2500) && ah != null) {
					mc.player.sendChatMessage(ah);
					resetTimer.reset();
					openAucTimer.reset();
				}
			}
			
			if (this.antiAfkTimer.passed(2500) && !rock.getBotsHandler().getBots().isEmpty()) {
				///mc.player.sendChatMessage("/bal");
				antiAfkTimer.reset();
			}
		}
		
		if (event instanceof EventReceivePacket e && e.getPacket() instanceof SChatPacket packet) {
	        String message = packet.getChatComponent().getString().toLowerCase();
	        
	        if (message.contains("успешно купили")) {
	        	selling = true;
	        	buyTimer.reset();
	        }
	        
	        if (message.contains("освободите хранилище")) {
	        	mc.player.sendChatMessage(ah);
	        	refresh = true;
	        }
	    }
		
		if (event instanceof EventReceivePacket e) {
			if (e.getPacket() instanceof SOpenWindowPacket && !autoBuy.getTaskManager().haveTasks() && module.getSwapAn().get()) {
				if (resetTimer.passed(module.getAcSpeed().get())) {
					Chat.msg("Слишком медленное обновление аукциона, перезапускаюсь");
					if (lastAn == null || lastAn.equals(an1)) {
						mc.player.sendChatMessage(an2);
						lastAn = an2;
					} else {
						mc.player.sendChatMessage(an1);
						lastAn = an1;
					}
						
					openAucTimer.reset();
				}
				resetTimer.reset();
			}
		}
		
		if (event instanceof EventMessage e) {
			if (e.getMessage().toLowerCase().startsWith("/ah") && !e.getMessage().toLowerCase().contains("sell")) {
				ah = e.getMessage();
				resetTimer.reset();
				openAucTimer.reset();
			}
		}
		
		if (event instanceof EventReceivePacket e && ah != null) {
			if (e.getPacket() instanceof SOpenWindowPacket && !autoBuy.getTaskManager().haveTasks() && resetTimer.passed(5000)) {
				//mc.player.closeScreen();
				//mc.player.sendChatMessage(ah);
				//resetTimer.reset();
			}
		}
	}
	
	private void buy(String seller, String name, int price, Slot s) {
		if (!canBuy(mc.player.openContainer.getSlot(s.slotNumber).getStack())) {
			Chat.debug(TextFormatting.RED + "Обнаружена фейк информация у " + name + "." + TextFormatting.GREEN +  " Реал: " + ItemUtility.getPrice(mc.player.openContainer.getSlot(s.slotNumber).getStack()) + "." + TextFormatting.RED + " Фейк: " + price);
			return;
		}
		
		resetTimer.reset();
		
		Chat.debug(TextFormatting.GREEN + "Подтвержден предмет " + name + " за " + price + ", покупаю");
		mc.player.connection.sendPacket(new CClickWindowPacket(mc.player.openContainer.windowId, s.slotNumber, 0, ClickType.QUICK_MOVE, mc.player.openContainer.getSlot(s.slotNumber).getStack(), mc.player.openContainer.getNextTransactionID(mc.player.inventory)));
	}
	
	private boolean canBuy(ItemStack stack) {
		for (AutoBuyItem item : rock.getAutoBuy().getItems()) {
			for (MinecraftItem item1 : item.getItems()) {
				if (stack.getItem() == item1.getItem() && 
						(ItemUtility.getPrice(stack)/stack.getCount()) <= item.getMaxPrice()
						&& stack.getCount() >= item.getMinCount() && ItemUtility.getPrice(stack) > 0
						&& openAucTimer.passed(1000)
						//&& updateTimer.passed(100)
						) {
					if (item.getEnchants().entrySet().isEmpty()) {
						return true;
					}
					
					for (Entry<String, Integer> set : item.getEnchants().entrySet()) {
	    	    	    int enchantmentId = Integer.parseInt(set.getKey().replace(set.getValue() +  "", ""));
	    	    	    Enchantment enchantment = Enchantment.getEnchantmentByID(enchantmentId);
	    	    	    if (enchantment != null) {
	    	    	        if (EnchantmentHelper.getEnchantmentLevel(enchantment, stack) >= set.getValue()) {
	    	    	        	if (Enchantment.getEnchantmentByID(Integer.parseInt(set.getKey().replace(set.getValue() +  "", ""))) == enchantment) {
			    	    			return true;
			    	    		}
			    	        }
			    	    }
			    	}
				}
			}
		}
		
		return false;
	}
	
}
