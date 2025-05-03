package fun.rockstarity.client.modules.other;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventWorldChange;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Input;
import fun.rockstarity.api.modules.settings.list.Mode;
import net.minecraft.client.gui.overlay.PlayerTabOverlayGui;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.network.play.server.SJoinGamePacket;
import net.minecraft.network.play.server.SOpenWindowPacket;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TextFormatting;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author Malecharik
 * @since 21 Mar 2024 22:45:01
 */

@NativeInclude
@Info(name = "AutoJoin", desc = "Автоматически заходит на гриф", type = Category.OTHER)
public class AutoJoin extends Module {

    protected final TimerUtility timerSystem = new TimerUtility();

    Mode mode = new Mode(this, "Режим");

    Mode.Element rw = new Mode.Element(mode, "ReallyWorld/FunTime");
    Mode.Element st = new Mode.Element(mode, "Spooky Дуэли");

    private final Input grief = new Input(this, "Гриф").set(true).hide(() -> !mode.is(rw));

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            joinerGrief();
            checkGriefContainer();
        }
        if (event instanceof EventReceivePacket e) joinSuccessful(e);
        if (event instanceof EventWorldChange && !st.get()) {
            this.toggle();
        }
    }

    @Override
    public void onEnable() {
        if (rw.get() && Server.isRW()) {
            joinerItem();
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
        }
    }

    private void joinSuccessful(EventReceivePacket event) {
        if (st.get() && Server.is("spooky")) {
            if (event.getPacket() instanceof SChatPacket) {
                joinerItem();
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            } else if (event.getPacket() instanceof SOpenWindowPacket packet) {
                if (packet.getTitle().getString().contains("Выберите режим")) {
                    mc.player.connection.sendPacket(new CClickWindowPacket(packet.getWindowId(),
                            14, 0, ClickType.PICKUP,
                            mc.player.openContainer.getSlot(14).getStack(),
                            mc.player.openContainer.getNextTransactionID(mc.player.inventory)));
                    event.cancel();
                }
            }
        }
        if (rw.get() && Server.isRW()) {
            if (event.getPacket() instanceof SJoinGamePacket) {
                if (mc.ingameGUI.getTabList().getHeader() == null) return;
                Chat.msg("Вход на " + this.grief.get() + " гриф успешен");
                this.toggle();
            } else if (event.getPacket() instanceof SChatPacket) {
                joinerItem();
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            } else if (event.getPacket() instanceof SOpenWindowPacket packet) {
                if (packet.getTitle().getString().contains("Выбор сервера")) {
                    mc.player.connection.sendPacket(new CClickWindowPacket(packet.getWindowId(),
                            21, 0, ClickType.PICKUP,
                            mc.player.openContainer.getSlot(21).getStack(),
                            mc.player.openContainer.getNextTransactionID(mc.player.inventory)));
                    event.cancel();
                }
            }
        } else if (rw.get() && Server.isFT()) {
            if (event.getPacket() instanceof SJoinGamePacket) {
                if (mc.ingameGUI.getTabList().getHeader() == null) return;
                Chat.msg("Вход на " + this.grief.get() + " анархию успешен");
                this.toggle();
            } else if (event.getPacket() instanceof SChatPacket p) {
                String m = TextFormatting.getTextWithoutFormattingCodes(p.getChatComponent().getString());
                if (m.contains("Вы уже подключены")) {
                    this.toggle();
                }
            }
        }
    }

    private void joinerGrief() {
        if (rw.get() && Server.isFT()) {
            mc.player.sendChatMessage("/an" + grief);
        }

        if (st.get() && Server.is("spooky")) {
            joinerItem();
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));

            if (Player.findItem(Items.DIAMOND_SWORD) != -1) {
                Chat.msg("Вход успешен");
                this.toggle();
            }
        }
    }

    private void joinerItem() {
        int slot = Player.findItem(Items.COMPASS);
        if (slot == -1) return;
        mc.player.inventory.currentItem = slot;
        mc.player.connection.sendPacket(new CHeldItemChangePacket(slot));
    }
    
    private void checkGriefContainer() {
        if (mc.player.openContainer == null) return;

        for (int slot = 0; slot < mc.player.openContainer.inventorySlots.size(); slot++) {
            Slot containerSlot = mc.player.openContainer.getSlot(slot);
            if (containerSlot != null && containerSlot.getHasStack()) {
                ItemStack stack = containerSlot.getStack();
                String itemName = stack.getDisplayName().getString();

                if (itemName.contains("ГРИФ #" + grief.get() + " (1.16.5+)")) {
                    mc.player.connection.sendPacket(new CClickWindowPacket(mc.player.openContainer.windowId,
                            slot, 0, ClickType.PICKUP,
                            stack,
                            mc.player.openContainer.getNextTransactionID(mc.player.inventory)));

                    mc.player.connection.sendPacket(new CCloseWindowPacket(mc.player.openContainer.windowId));
                }
            }
        }
    }

    @Override
    public void onDisable() {
    }
}