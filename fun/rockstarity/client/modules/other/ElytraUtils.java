package fun.rockstarity.client.modules.other;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.inputs.EventKey;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.InventoryUtility;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.player.InvUtility;
import fun.rockstarity.api.helpers.player.Inventory;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Binding;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CEntityActionPacket;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;


@Info(name = "ElytraUtils", desc = "Помощник с элитрами", type = Category.OTHER, module = {"ElytraBoost", "ElytraSwap"})
public class ElytraUtils extends Module {

    private final Binding swapBind = new Binding(this, "Кнопка свапа").desc("Кнопка, при нажатии которой, Элитры будут менятся местами с Нагрудником");

    private final Binding fireworkBind = new Binding(this, "Кнопка исп. фейерверка").desc("Кнопка, при нажатии которой, будет использоваться фейерверк");

    private final CheckBox automat = new CheckBox(this, "Авто взлёт").desc("Автоматически начинает парить, если надеты элитры");

    private final CheckBox useFirework = new CheckBox(this, "Фейерверк при взлете").desc("Фейерверк при взлете");

    private final CheckBox removeOnLand = new CheckBox(this, "Грудак при приземлении").desc("Автоматически снимает элитры при приземлении и надевает нагрудник, если он есть");

    private final CheckBox autoFly = new CheckBox(this, "Авто /fly").desc("Автоматически прописывает /fly при свапе элитр");
    
    @Getter
    private final CheckBox elytraBoost = new CheckBox(this, "Ускорение с элитрой").desc("Ускоряет полет с элитрами");

    @Getter
    private final Slider elytraSpeed = new Slider(this, "Скорость ускорения").min(1f).max(3f).inc(0.1f).set(1.5f).hide(() -> !elytraBoost.get()).text(1, "Авто");

    private Task current;
    private final TimerUtility timer = new TimerUtility();
    private boolean wasFlying;
    private boolean isLanding;
    @NativeInclude
    public void use(int one, int two) {
        current = new Task(one, two);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate && current != null) {
            current.stage++;

            switch (current.stage) {
                case 1:
                    mc.playerController.windowClick(0, current.one, 0, ClickType.PICKUP, mc.player);
                    break;
                case 3:
                    mc.playerController.windowClick(0, current.two, 0, ClickType.PICKUP, mc.player);
                    break;
                case 5:
                    mc.playerController.windowClick(0, current.one, 0, ClickType.PICKUP, mc.player);
                    break;
            }

            if (current.stage > 5) current = null;
        }

        if (!(mc.currentScreen instanceof ChatScreen) && mc.currentScreen != rock.getClickGui()) {
            if (event instanceof EventKey e) {
                handleKey(e);
            }
        }

        if (event instanceof EventUpdate) {
            if (mc.player.isElytraFlying()) {
                wasFlying = true;
                isLanding = false;

                if (useFirework.get() && mc.player.getTicksElytraFlying() <= 1) InvUtility.use(Items.FIREWORK_ROCKET);
            }

            if (automat.get() && ElytraItem.isUsable(mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST)) && mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() == Items.ELYTRA && !mc.player.isElytraFlying() && !mc.player.isOnGround()) {
                mc.player.startFallFlying();
                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
            } else if (mc.player.isOnGround() && automat.get() && mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() == Items.ELYTRA) {
                mc.player.jump();
            }

            if (removeOnLand.get() && mc.player.isOnGround() && mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() == Items.ELYTRA && wasFlying) {
                if (!isLanding) isLanding = true;
                if (isLanding && mc.player.getTicksElytraFlying() > 8) {
                    int chestplateSlot = Inventory.getChestplate();

                    if (chestplateSlot != -1) {
                        if (Server.isHW()) use(chestplateSlot, 6); else Player.moveItem(chestplateSlot, 6, true);
                    } else {
                        mc.playerController.windowClick(0, 6, 0, ClickType.PICKUP, mc.player);
                    }

                    wasFlying = false;
                    isLanding = false;
                }
            }
        }
    }
    @NativeInclude
    private void handleKey(EventKey e) {
        if (!e.isReleased() && mc.currentScreen == null) {
            if (this.swapBind.getBindByKey(e).isPresent() && Inventory.getChestplate() != -1 && Player.findItem(45, Items.ELYTRA) != -1) {
                changeChestPlate(mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST));
            }
            if (this.fireworkBind.getBindByKey(e).isPresent() && ElytraItem.isUsable(mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST)) && mc.player.isElytraFlying()) {
                InvUtility.use(Items.FIREWORK_ROCKET);
            }
        }
    }

    private void changeChestPlate(ItemStack stack) {
        if (mc.currentScreen != null) {
            return;
        }
        if (stack.getItem() != Items.ELYTRA) {
            int elytraSlot = getItemSlot(Items.ELYTRA);
            if (elytraSlot >= 0) {
                if (Server.isHW()) {
                	current = new Task(elytraSlot, 6);
                } else {
                	InventoryUtility.moveItem(elytraSlot, 6);
                }
                if (autoFly.get())
                	mc.player.sendChatMessage("/fly");
                return;
            }
        }
        int armorSlot = getChestPlateSlot();
        InventoryUtility.moveItem(armorSlot, 6);
    }


    private int getChestPlateSlot() {
        for (int i = 0; i < 36; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() instanceof ArmorItem armorItem) {
                if (armorItem.getEquipmentSlot() == EquipmentSlotType.CHEST) {
                    int slot = i;
                    if (i < 9) {
                        slot += 36;
                    }
                    return slot;
                }
            }
        }
        return -1;
    }

    @Override
    public void onDisable() {
        wasFlying = false;
        isLanding = false;
    }

    @Override
    public void onEnable() {
        wasFlying = false;
        isLanding = false;
    }

    @RequiredArgsConstructor
    class Task {
        final int one, two;
        int stage;
    }

    private int getItemSlot(Item input) {
        int slot = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.inventory.getStackInSlot(i);
            if (s.getItem() == input) {
                slot = i;
                break;
            }
        }
        if (slot < 9 && slot != -1) {
            slot = slot + 36;
        }
        return slot;
    }
}
