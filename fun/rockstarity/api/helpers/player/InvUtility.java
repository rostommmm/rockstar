package fun.rockstarity.api.helpers.player;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.TimerUtility;
import lombok.experimental.UtilityClass;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SHeldItemChangePacket;
import net.minecraft.util.Hand;

/**
 * @author ConeTin
 * @since 3 РёСЋРЅ. 2024 Рі.
 */

public class InvUtility implements IAccess {
	private static Task current;
	private static final TimerUtility timer = new TimerUtility();
	
	public static void use(Item item) {
		use(item, Server.isHW());
	}
	
	public static void use(Item item, boolean smooth) {
		int slot = Inventory.findItemNoChanges(44, item);
		
		if (slot == -1 || mc.currentScreen != null) return;
		
		if (mc.player.getHeldItemOffhand().getItem() == item) {
			mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
			return;
		}
		
		boolean inHotbar = slot <= 8;
		
		if (smooth) {
			current = new Task(slot);
 		} else {
 			if (inHotbar) {
 				mc.player.connection.sendPacket(new CHeldItemChangePacket(slot));
 				mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
 				mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
 			} else {
 				mc.playerController.pickItem(slot);
 				mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
 				mc.playerController.pickItem(slot);
 				timer.reset();
 			}
 		}
	}
	
	public static void onEvent(Event event) {
		if (event instanceof EventReceivePacket e) {
			if (e.getPacket() instanceof SHeldItemChangePacket packet && !timer.passed(500)) {
				//mc.playerController.pickItem(lastSlot);
				//mc.playerController.currentPlayerItem = packet.getHeldItemHotbarIndex();
				mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem % 8 + 1));
				mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
    			e.cancel();
			}
		}
		
		if (event instanceof EventUpdate && current != null) {
			current.stage++;
			
			if (current.slot < 9) {
				switch (current.stage) {
				case 1:
					mc.player.connection.sendPacket(new CHeldItemChangePacket(current.slot));
					break;
				case 2:
					mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
					break;
				case 3:
	 				mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
					break;
				}
			} else {
				switch (current.stage) {
				case 1:
				case 3:
	 				mc.playerController.pickItem(current.slot);
					break;
				case 2:
					mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
					break;
				}
			}
			
			if (current.stage > 3) current = null;
		}
	}
	
	public static class Task {
		final int slot;
		int stage;

        Task(int slot) {
            this.slot = slot;
        }
    }
}