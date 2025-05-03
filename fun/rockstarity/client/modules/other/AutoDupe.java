package fun.rockstarity.client.modules.other;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.PremiumModule;
import fun.rockstarity.api.modules.settings.list.Input;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * @author ConeTin
 * @since 17 авг. 2024 г.
 */

@Info(name = "AutoDupe", desc = "Автоматически фармит предметы на BravoHVH", type = Category.OTHER)
public class AutoDupe extends PremiumModule {
	
	private final TimerUtility kitTimer = new TimerUtility();
	private final TimerUtility cleanTimer = new TimerUtility();
	private final Input kit = new Input(this, "Кит").set("free");
	private boolean storeKit;

	@Override
	public void onEvent(Event event) {
		if (event instanceof EventUpdate) {
			if (invEmpty() && kitTimer.passed(61000)) {
				mc.player.sendChatMessage("/kit " + kit.get());
				kitTimer.reset();
				storeKit = true;
			}

			if (!invEmpty()) {
				if (haveGApple() && storeKit && cleanTimer.passed(1000)) {
					if (mc.currentScreen instanceof ContainerScreen) {
						int startIndex = mc.player.openContainer.getInventory().size() - 36;
			    		int endIndex = mc.player.openContainer.getInventory().size() - 1;
			    		
			    		for (int i = startIndex; i <= endIndex; i++) {
		    	            ItemStack itemstack = mc.player.openContainer.getSlot(i).getStack();
			    			if (itemstack.getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
			    				Chat.debug(itemstack.getItem());
			    				
			    				mc.playerController.windowClick(mc.player.openContainer.windowId, i, 0, ClickType.QUICK_MOVE, mc.player);
			    			}
			    		}
			    		
			    		storeKit = false;
			    		mc.player.closeScreen();
					} else {
						mc.player.sendChatMessage("/ec");
					}
					cleanTimer.reset();
				}
				
				if (!haveGApple() && cleanTimer.passed(1000)) {
					mc.player.sendChatMessage("/clear -confirmed");
					cleanTimer.reset();
				}
			}
		}
	}
	
	private boolean haveGApple() {
		for (int i = 0; i < mc.player.container.getInventory().size(); i++) {
			if (Player.find(i).isEmpty()) continue;
			if (Player.find(i).getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean invEmpty() {
		for (int i = 0; i < mc.player.container.getInventory().size(); i++) {
			if (!Player.find(i).isEmpty()) return false;
		}
		
		return true;
	}
	
	@Override
	public void onEnable() {
	}
	
	@Override
	public void onDisable() {
	}

}
