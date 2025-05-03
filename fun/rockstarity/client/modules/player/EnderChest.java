package fun.rockstarity.client.modules.player;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventCloseScreen;
import fun.rockstarity.api.events.list.game.EventWorldChange;
import fun.rockstarity.api.events.list.game.inputs.EventKey;
import fun.rockstarity.api.events.list.game.packet.EventSendPacket;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Binding;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.network.play.server.SCloseWindowPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@Info(name="EnderChest", desc="Утилиты для эндер-сундука", type=Category.PLAYER)
public class EnderChest extends Module {
	CheckBox save = new CheckBox(this, "Сохранять").set(true).desc("Сохраняет открытие эндер-сундука(можно открыть в любое время). Во время того как он открыт вы не можете открывать свой инвентарь");
    Binding close = new Binding(this, "Закрыть сундук").hide(() -> !save.get());
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    @NonFinal @Getter Container container;
    @NonFinal Screen screen;
    
    @Override
    public void onEvent(Event event) {
		if (save.get()) {
			if (event instanceof EventKey e) {
		        if (close.check(e) && check()) {
		            mc.player.connection.sendPacketSilent(new CCloseWindowPacket(container.windowId));
		            reset();
		        }
			}
			
			if (event instanceof EventSendPacket e) {
				IPacket<?> packet = e.getPacket();
		        if (packet instanceof SCloseWindowPacket && check()) {
		            reset();
		        }

		        if (packet instanceof CPlayerDiggingPacket digging && digging.getAction() == CPlayerDiggingPacket.Action.SWAP_ITEM_WITH_OFFHAND && check() && mc.currentScreen == null) {
		            int slot = container.getInventory().size() - 9 + mc.player.inventory.currentItem;
		            short nextTransactionID = container.getNextTransactionID(mc.player.inventory);
		            ItemStack itemstack = container.slotClick(slot, 40, ClickType.SWAP, mc.player);
		            mc.player.connection.sendPacket(new CClickWindowPacket(container.windowId, slot, 40, ClickType.SWAP, itemstack, nextTransactionID));
		            e.cancel();
		        }

		        if (packet instanceof CPlayerTryUseItemOnBlockPacket useItemOnBlock && check()) {
		            for (TileEntity entity : mc.world.loadedTileEntityList) {
		                if (entity.getPos().equals(new BlockPos(useItemOnBlock.func_218794_c().getPos().getVec()))) {
		                    mc.player.connection.sendPacketSilent(new CCloseWindowPacket(container.windowId));
		                    scheduler.schedule(() -> mc.player.connection.sendPacketSilent(useItemOnBlock), 50, TimeUnit.MILLISECONDS);
		                    reset();
		                    e.cancel();
		                }
		            }
		        }
			}
			
			if (event instanceof EventCloseScreen e) {
				if (e.getScreen() instanceof ChestScreen scr && (scr.getTitle().getString().contains("Эндер-сундук") || scr.getTitle().getString().contains("Ender"))) {
		            screen = mc.currentScreen;
		            container = mc.player.openContainer;
		            e.cancel();
		        } else if (check()) {
		            e.cancel();
		        }
			}
			
			if (event instanceof EventWorldChange) {
				reset();
			}
		}
    }

    public void enderChestLoad() {
        mc.displayGuiScreen(screen);
        mc.player.openContainer = container;
    }

    public void reset() {
        screen = null;
        container = null;
    }

    public boolean check() {
        return get() && save.get() && screen != null && container != null;
    }

	@Override
	public void onEnable() {
		
	}

	@Override
	public void onDisable() {
		mc.player.connection.sendPacketSilent(new CCloseWindowPacket(container.windowId));
		reset();
	}
}
