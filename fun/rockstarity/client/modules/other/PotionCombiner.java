package fun.rockstarity.client.modules.other;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.EventType;
import fun.rockstarity.api.events.list.game.inputs.EventInput;
import fun.rockstarity.api.events.list.player.*;
import fun.rockstarity.api.events.list.render.world.EventRenderWorld;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.math.InventoryUtility;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.player.Inventory;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Slider;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.screen.inventory.AnvilScreen;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.Hand;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

import org.lwjgl.glfw.GLFW;

import java.util.*;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Info(name="PotionCombiner", desc="Совмещает зелья в наковальне", type= Category.OTHER)
public class PotionCombiner extends Module {

    private final TimerUtility timer = new TimerUtility();
    private String currentName = "";

    private final Mode mode = new Mode(this, "Зелье");
    private final Mode.Element strength = new Mode.Element(mode, "Зелье силы");
    private final Mode.Element speed = new Mode.Element(mode, "Зелье скорости");

    private final CheckBox autoExp = new CheckBox(this, "Авто Бутылочки опыта");
    private final Slider dep = new Slider(this, "Пополнять опыт до").min(5).max(100).inc(1).set(15).hide(() -> !autoExp.get());

    private boolean autoRepair;
    private float pitch;

    // СПАСИБО ДИМЕ ЗА ТО ЧТО ДОБАВИЛ AUTOBREW
    @Override
    @NativeInclude
    @EventType({EventRenderWorld.class})
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            if (mc.currentScreen instanceof AnvilScreen && !autoRepair) {
                movePotionsToAnvil();
                if (areBothSlotsFilledWithStrengthPotions() && mc.player.experienceLevel >= 5) {
                    if (((AnvilScreen) mc.currentScreen).getCost() <= 5) takeResult();
                    appendExclamation();
                }
            }

            if (mc.player.experienceLevel < 5 && findExp() != -1) autoRepair = true;

            if (autoExp.get() && autoRepair) {

                if (mc.currentScreen instanceof AnvilScreen) {
                    mc.player.closeScreen();
                }

                if (mc.player.getHeldItemMainhand().getItem() != Items.EXPERIENCE_BOTTLE) {
                    int expSlot = findExp();

                    if (expSlot != -1 && expSlot != mc.player.inventory.currentItem + 36) {
                        Inventory.moveItem(expSlot, mc.player.inventory.currentItem + 36, true);
                    } else if (expSlot == -1) {
                        autoRepair = false;
                    }
                } else if (timer.passed(300)) {
                    mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                    timer.reset();

                    if (mc.player.experienceLevel >= dep.get()) {
                        autoRepair = false;
                    }
                }
            }
        }
        if (event instanceof EventMotion e && autoRepair) {
            e.setPitch(80.0f);
            mc.player.rotationPitchHead = 80.0f;
            this.pitch = 80.0f;
        }
    }
    @NativeInclude
    private boolean areBothSlotsFilledWithStrengthPotions() {
        return isStrengthPotion(getSlotStack(0)) && isStrengthPotion(getSlotStack(1));
    }
    @NativeInclude
    private void takeResult() {
        if (timer.passed(100)) {
            mc.playerController.windowClick(mc.player.openContainer.windowId, 2, 0, ClickType.QUICK_MOVE, mc.player);
            timer.reset();
            currentName = "";
            if (mc.currentScreen instanceof AnvilScreen) {
                ((AnvilScreen) mc.currentScreen).nameField.setText("");
            }
        }
    }

    private boolean isStrengthPotion(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (!(stack.getItem() instanceof PotionItem)) return false;
        List<EffectInstance> effects = PotionUtils.getEffectsFromStack(stack);
        for (EffectInstance effect : effects) {
            if ((strength.get() && effect.getPotion() == Effects.STRENGTH || speed.get() && effect.getPotion() == Effects.SPEED) && effect.getAmplifier() == 1) {
                return true;
            }
        }
        return false;
    }

    private void movePotionsToAnvil() {
        if (mc.player == null || mc.playerController == null || !(mc.currentScreen instanceof AnvilScreen)) {
            return;
        }
        for (int i = 0; i < 2; i++) {
            if (getItem(i) instanceof AirItem) {
                if(timer.passed(300)) {
                    swapOneItem(findStrengthPotionInInventory(), i);
                    timer.reset();
                }
            }
        }
    }

    private int findStrengthPotionInInventory() {
        if (mc.player == null || mc.player.openContainer == null || mc.player.openContainer.inventorySlots == null) {
            return -1; // Если контейнер недоступен
        }

        int slotCount = mc.player.openContainer.inventorySlots.size();
        for (int i = 5; i < slotCount; i++) { // Ограничиваем размером контейнера
            ItemStack stack = mc.player.openContainer.inventorySlots.get(i).getStack();
            if (stack != null && stack.getItem() instanceof PotionItem) { // Проверка на null
                List<EffectInstance> effects = PotionUtils.getEffectsFromStack(stack);
                if (effects != null) { // Проверка на null
                    for (EffectInstance effect : effects) {
                        if ((strength.get() && effect.getPotion() == Effects.STRENGTH || speed.get() && effect.getPotion() == Effects.SPEED) && effect.getAmplifier() == 1) {
                            return i;
                        }
                    }
                }
            }
        }
        return -1;
    }
    @NativeInclude
    public void swapOneItem(int from, int to) {
        mc.playerController.windowClick(mc.player.openContainer.windowId, from, 0, ClickType.PICKUP, mc.player);
        mc.playerController.windowClick(mc.player.openContainer.windowId, to, 1, ClickType.PICKUP, mc.player);
        mc.playerController.windowClick(mc.player.openContainer.windowId, from, 0, ClickType.PICKUP, mc.player);
    }

    private Item getItem(int slotId) {
        return mc.player.openContainer.inventorySlots.get(slotId).getStack().getItem();
    }
    private ItemStack getSlotStack(int slotId) {
        return mc.player.openContainer.inventorySlots.get(slotId).getStack();
    }
    @NativeInclude
    private void appendExclamation() {
        if (mc.currentScreen instanceof AnvilScreen) {
            AnvilScreen anvil = (AnvilScreen) mc.currentScreen;
            if (currentName.isEmpty()) {
                currentName = anvil.nameField.getText();
            }
            currentName += "!";
            anvil.nameField.setText(currentName);
        }
    }

    private int findExp() {
        if (mc.player.getHeldItemOffhand().getItem() == Items.EXPERIENCE_BOTTLE) {
            return 45;
        }

        for (int i = 0; i < mc.player.inventory.mainInventory.size(); ++i) {
            ItemStack stack = mc.player.inventory.mainInventory.get(i);
            if (stack.getItem() == Items.EXPERIENCE_BOTTLE) {
                return i < 9 ? 36 + i : i;
            }
        }
        return -1;
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}
}
