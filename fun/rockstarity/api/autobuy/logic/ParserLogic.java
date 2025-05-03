package fun.rockstarity.api.autobuy.logic;

import java.util.Map.Entry;

import fun.rockstarity.api.autobuy.AutoBuy;
import fun.rockstarity.api.autobuy.logic.interfaces.ILogicHandler;
import fun.rockstarity.api.autobuy.logic.items.AutoBuyItem;
import fun.rockstarity.api.autobuy.logic.items.MinecraftItem;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.game.ItemUtility;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.render.ui.alerts.AlertType;
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
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.Difficulty;

/**
 * @author ConeTin
 * @since 20 апр. 2024 г.
 */


@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParserLogic implements ILogicHandler {

	final AutoBuy autoBuy;
	final TimerUtility openAucTimer = new TimerUtility();
	boolean needUpdate;
	boolean canCheck;

	public ParserLogic(AutoBuy autoBuy) {
		this.autoBuy = autoBuy;
	}
	
	@Override
	public void onEvent(Event event) {
		fun.rockstarity.client.modules.other.AutoBuy module = rock.getModules().get(fun.rockstarity.client.modules.other.AutoBuy.class);
		
		if (event instanceof EventUpdate) {
			if (!module.getParser().get()) return;
			
			if (mc.world.getDifficulty() == Difficulty.EASY) {
				if (openAucTimer.passed(2500)) {
					mc.player.sendChatMessage("/an" + Server.FT_ANARCHY);
					openAucTimer.reset();
				}
			}
			
			{ // Выключалка
				boolean disable = true;
				
				for (AutoBuyItem item : rock.getAutoBuy().getItems()) {
					if (!item.isParsed()) {
						disable = false;
					}
				}
				
				if (disable) {
					module.getParser().set(false);
					rock.getAlertHandler().alert("Анализ цен завершён", AlertType.SUCCESS);
				}
			}
			
			if (mc.player.openContainer instanceof ChestContainer) {
				ChestScreen chest = (ChestScreen) mc.currentScreen;
				String chestName = chest.getTitle().getString();

				if (chestName.contains("Поиск")) {
					if (needUpdate) {
						mc.player.connection.sendPacket(new CClickWindowPacket(mc.player.openContainer.windowId, 49, 0, ClickType.SWAP, mc.player.openContainer.getSlot(49).getStack(), mc.player.openContainer.getNextTransactionID(mc.player.inventory)));
						needUpdate = false;
					}
					
					for (Slot s : mc.player.openContainer.inventorySlots) {
						ItemStack stack = s.getStack();
						String displayName = stack.getDisplayName().getString().toLowerCase();
						
						AutoBuyItem item = getItem(stack);

						if (item != null && canCheck) {
							String name = stack.getDisplayName().getString();
							int price = ItemUtility.getPrice(stack);
							String seller = ItemUtility.getSeller(stack);
							
            				Chat.debug(TextFormatting.GRAY + "Отправляю предмет " + name + " за " + price + " на проверку");
            				canCheck = false;

							autoBuy.getTaskManager().createTask(seller, name, price, () -> {
								parse(seller, name, price, s);
							}, () -> {
								stack.fake = true;
								item.setUpdates(item.getUpdates() + 1);
								if (item.getUpdates() > 5) {
									item.setUpdates(0);
									needUpdate = true;
								}
								//needUpdate = true;
								canCheck = true;
							});
						}
					}
				} else {
					mc.player.closeScreen();
				}
			} else {
				if (openAucTimer.passed(1000))
				for (AutoBuyItem item : rock.getAutoBuy().getItems()) {
					if (item.isParsed()) continue;
					
					String name = item.getFirst().getName();
					
					if (name.toLowerCase().contains("шалкер"))
						name = "Шалкер";
					
					mc.player.sendChatMessage("/ah search " + name);
					openAucTimer.reset();
					canCheck = true; 
					
					break;
				}
			}
		}
	}
	
	private void parse(String seller, String name, int price, Slot s) {
		ItemStack stack = mc.player.openContainer.getSlot(s.slotNumber).getStack();
		AutoBuyItem item = getItem(stack);
		
		canCheck = true;
		
		if (item == null || price != ItemUtility.getPrice(stack)) {
			Chat.debug(TextFormatting.RED + "Обнаружена фейк информация у " + name + "." + TextFormatting.GREEN +  " Реал: " + ItemUtility.getPrice(mc.player.openContainer.getSlot(s.slotNumber).getStack()) + "." + TextFormatting.RED + " Фейк: " + price);
			needUpdate = true;
			return;
		}
		
		//if (item.getMaxPrice() > (int) (price/(float)stack.getCount()*(1-rock.getModules().get(fun.rockstarity.client.modules.other.AutoBuy.class).getParsePercent().get()/100F))
		//		|| item.getUpdates() == 0) {
			Chat.debug(TextFormatting.GREEN + "Подтвержден предмет " + name + " за " + price + ", паршу");
			item.setMaxPrice((int) (price/(float)stack.getCount()*(1-rock.getModules().get(fun.rockstarity.client.modules.other.AutoBuy.class).getParsePercent().get()/100F)));
			item.setSellPrice((int) (price/(float)stack.getCount()));
		//}
		//if (item.getUpdates() > 5) {
			item.setParsed(true);
			mc.player.closeScreen();
			item.setUpdates(0);
		//}
		needUpdate = true;
		
		//mc.player.closeScreen();
		
		rock.getAlertHandler().alert("Для '" + item.getFirst().getName() + "' установлена цена " + item.getMaxPrice() + " $", AlertType.SUCCESS);
	}

	private AutoBuyItem getItem(ItemStack stack) {
		for (AutoBuyItem item : rock.getAutoBuy().getItems()) {
			for (MinecraftItem item1 : item.getItems()) {
				if (stack.getItem() == item1.getItem() && ItemUtility.getPrice(stack) > 0 && !item.isParsed()
						//&& updateTimer.passed(100)
						) {
					if (item.getEnchants().entrySet().isEmpty()) {
						return item;
					}
					
					for (Entry<String, Integer> set : item.getEnchants().entrySet()) {
	    	    	    int enchantmentId = Integer.parseInt(set.getKey().replace(set.getValue() +  "", ""));
	    	    	    Enchantment enchantment = Enchantment.getEnchantmentByID(enchantmentId);
	    	    	    if (enchantment != null) {
	    	    	        if (EnchantmentHelper.getEnchantmentLevel(enchantment, stack) >= set.getValue()) {
	    	    	        	if (Enchantment.getEnchantmentByID(Integer.parseInt(set.getKey().replace(set.getValue() +  "", ""))) == enchantment) {
			    	    			return item;
			    	    		}
			    	        }
			    	    }
			    	}
				}
			}
		}
		
		return null;
	}
	
}
