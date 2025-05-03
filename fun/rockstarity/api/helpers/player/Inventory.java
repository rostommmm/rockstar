package fun.rockstarity.api.helpers.player;

import java.util.concurrent.TimeUnit;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.client.modules.combat.Aura;
import lombok.experimental.UtilityClass;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.client.CEntityActionPacket.Action;
import net.minecraft.util.Hand;

/**
 * @author ConeTin
 * @since 6 дек. 2023 г.
 */

@UtilityClass
public class Inventory implements IAccess {
	
	private boolean sprint;
	
	public void moveItem(int one, int two) {
		moveItem(one, two, false);
	}

	public void moveItem(int one, int two, boolean swap) {
		mc.playerController.windowClick(0, one, 0, ClickType.PICKUP, mc.player);
		mc.playerController.windowClick(0, two, 0, ClickType.PICKUP, mc.player);
		if (swap)
			mc.playerController.windowClick(0, one, 0, ClickType.PICKUP, mc.player);
	}
	
	public int findItem(final int endSlot, final Item item) {
		for (int i = 0; i < endSlot; i++) {
			if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
				return i < 9 ? 36 + i : i;
			}
		}
		return -1;
	}
	
	public int findItemNoChanges(final int endSlot, final Item item) {
		for (int i = 0; i < endSlot; i++) {
			if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
				return i;
			}
		}
		return -1;
	}

	public void clickSlotId(int slotId, int buttonId, ClickType clickType, boolean packet) {
		clickSlotId(mc.player.openContainer.windowId, slotId, buttonId, clickType, packet);
	}

	public void clickSlotId(int windowId, int slotId, int buttonId, ClickType clickType, boolean packet) {
		if (packet) {
			mc.player.connection.sendPacket(new CClickWindowPacket(windowId, slotId, buttonId, clickType, ItemStack.EMPTY, mc.player.openContainer.getNextTransactionID(mc.player.inventory)));
		} else {
			mc.playerController.windowClick(windowId, slotId, buttonId, clickType, mc.player);
		}
	}
	
	public void swap(int slot) {
		mc.player.connection.sendPacket(new CHeldItemChangePacket(slot));
	}

	public int getFirework() {
		for (int i = 0; i < 9; i++) {
			if (mc.player.inventory.getStackInSlot(i).getItem() == Items.FIREWORK_ROCKET) {
				return i;
			}
		}
		return -1;
	}

	public ItemStack getItem(int item) {
		return mc.player.inventory.getStackInSlot(item);
	}
	
	public int getChestplate() {
		for (int i = 0; i < 45; ++i) {
			ItemStack itemStack = mc.player.inventory.getStackInSlot(i);
			if (itemStack.getItem() instanceof ArmorItem)
				if (((ArmorItem)itemStack.getItem()).getEquipmentSlot() == EquipmentSlotType.CHEST)
					return i == 40 ? 45 : i < 9 ? 36 + i : i;
		}
		return -1;
	}
}
