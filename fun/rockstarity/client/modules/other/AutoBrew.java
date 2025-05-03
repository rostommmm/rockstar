package fun.rockstarity.client.modules.other;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.PremiumModule;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Slider;
import net.minecraft.inventory.container.BrewingStandContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.AirItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;

/**
 * @author Malecharik
 * @since 10 мая 2024 г. 10:03:16
 */

@Info(name = "AutoBrew", desc = "Автоматически варит зелья", type = Category.OTHER)
public class AutoBrew extends PremiumModule {
    private final Mode mode = new Mode(this, "Варить...");

    private final Mode.Element speed = new Mode.Element(mode, "Зелье скорости");
    private final Mode.Element strength = new Mode.Element(mode, "Зелье силы");
    private final Mode.Element fire = new Mode.Element(mode, "Зелье огнестойкости");

    private final Slider delay = new Slider(this, "Задержка").min(100).max(1000).inc(10).set(100);
    private final CheckBox loot = new CheckBox(this, "Забирать в инвентарь").set(true);
    private final CheckBox addGunpowder = new CheckBox(this, "Добавлять порох");

    private final TimerUtility timer = new TimerUtility();

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            if (mc.player.openContainer instanceof BrewingStandContainer) {
                if (isFuelEmpty()) {
                    if (findItemInBrewingStand(Items.BLAZE_POWDER) == -1) {
                        Chat.msg("Нет огненного порошка для заправки!");
                        toggle();;
                    }
                    swapOneItem(Items.BLAZE_POWDER, 4);
                }

                for (int i = 0; i < 3; i++) {
                    if (getItem(i) instanceof AirItem) {
                        int waterPotionSlot = findPotionInBrewingStand("water");
                        if (waterPotionSlot == -1) {
                        	toggle();
                            Chat.msg("Нет бутылок с водой!");
                        }
                        if (timer.passed((long) (delay.get() * 3))) {
                            swapOneItem(waterPotionSlot, i);
                            timer.reset();
                        }
                    }
                }

                if (getItem(3) instanceof AirItem) {
                    if (identicalPotionsCheck("water")) {
                        if (findItemInBrewingStand(Items.NETHER_WART) == -1) {
                        	toggle();
                            Chat.msg("Нет нароста!");
                        }
                        swapOneItem(Items.NETHER_WART, 3);
                    }

                    if (mode.is(speed)) {
                        if (identicalPotionsCheck("awkward")) {
                            if (findItemInBrewingStand(Items.SUGAR) == -1) {
                                Chat.msg("Нет сахара для зелья скорости!");
                                toggle();
                            }
                            swapOneItem(Items.SUGAR, 3);
                        }
                    } else if (mode.is(strength)) {
                        if (identicalPotionsCheck("awkward")) {
                            if (findItemInBrewingStand(Items.BLAZE_POWDER) == -1) {
                                Chat.msg("Нет огненного порошка для зелья силы!");
                                toggle();
                            }
                            swapOneItem(Items.BLAZE_POWDER, 3);
                        }
                    } else if (mode.is(fire)) {
                        if (identicalPotionsCheck("awkward")) {
                            if (findItemInBrewingStand(Items.MAGMA_CREAM) == -1) {
                                Chat.msg("Нет слизи магмы для зелья огнестойкости!");
                                toggle();
                            }
                            swapOneItem(Items.MAGMA_CREAM, 3);
                        }
                    }

                    if (identicalPotionsCheck("strength") || identicalPotionsCheck("swiftness")) {
                        if (findItemInBrewingStand(Items.GLOWSTONE_DUST) == -1) {
                            Chat.msg("Нет светокамня для усиления зелья!");
                            toggle();
                        }
                        swapOneItem(Items.GLOWSTONE_DUST, 3);
                    }

                    if (identicalPotionsCheck("fire_resistance")) {
                        if (findItemInBrewingStand(Items.REDSTONE) == -1) {
                            Chat.msg("Нет редстоуна для увеличения длительности зелья!");
                            toggle();
                        }
                        swapOneItem(Items.REDSTONE, 3);
                    }

                    if (identicalPotionsCheck("strong_strength") || identicalPotionsCheck("strong_swiftness") || identicalPotionsCheck("long_fire_resistance")) {
                        if (addGunpowder.get()) {
                            if (findItemInBrewingStand(Items.GUNPOWDER) == -1) {
                                Chat.msg("Нет пороха для создания взрывных зелий!");
                                toggle();
                            }
                            if (identicalSplashPotionsCheck()) {
                                if (loot.get()) {
                                    loot();
                                }
                            } else {
                                swapOneItem(Items.GUNPOWDER, 3);
                            }
                        } else {
                            if (loot.get()) {
                                loot();
                            }
                        }
                    }
                }
            }
        }
    }

    private void loot() {
        for (int i = 0; i < 3; i++) {
            if (timer.passed((long) (delay.get() * 2))) {
                mc.playerController.windowClick(mc.player.openContainer.windowId, i, 0, ClickType.QUICK_MOVE, mc.player);
            }
        }
    }

    private boolean identicalSplashPotionsCheck() {
        boolean needIng = true;
        for (int i = 0; i < 3; i++) {
            if (mc.player.openContainer.inventorySlots.get(i).getStack().getItem() != Items.SPLASH_POTION) {
                needIng = false;
            }
        }
        return needIng;
    }

    private boolean identicalPotionsCheck(String potionType) {
        boolean needIng = true;
        for (int i = 0; i < 3; i++) {
            if (PotionUtils.getPotionFromItem(mc.player.openContainer.inventorySlots.get(i).getStack()) != Potion.getPotionTypeForName(potionType)) {
                needIng = false;
            }
        }
        return needIng;
    }

    public void swapOneItem(Item item, int to) {
        if (timer.passed((long) (delay.get() * 2))) {
            int slot;
            if ((slot = findItemInBrewingStand(item)) != -1) {
                swapOneItem(slot, to);
                timer.reset();
            }
        }
    }

    public void swapOneItem(int from, int to) {
        mc.playerController.windowClick(mc.player.openContainer.windowId, from, 0, ClickType.PICKUP, mc.player);
        mc.playerController.windowClick(mc.player.openContainer.windowId, to, 1, ClickType.PICKUP, mc.player);
        mc.playerController.windowClick(mc.player.openContainer.windowId, from, 0, ClickType.PICKUP, mc.player);
    }

    private Item getItem(int slotId) {
        return mc.player.openContainer.inventorySlots.get(slotId).getStack().getItem();
    }

    private boolean isFuelEmpty() {
        return getItem(4) instanceof AirItem;
    }

    private int findPotionInBrewingStand(String potionType) {
        for (int i = 5; i < 41; i++) {
            if (PotionUtils.getPotionFromItem(mc.player.openContainer.inventorySlots.get(i).getStack()) == Potion.getPotionTypeForName(potionType)) return i;
        }
        return -1;
    }

    private int findItemInBrewingStand(Item item) {
        for (int i = 5; i < 41; i++) {
            if (getItem(i) == item) return i;
        }
        return -1;
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onEnable() {
    }
}