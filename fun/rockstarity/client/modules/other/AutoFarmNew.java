package fun.rockstarity.client.modules.other;

import java.util.List;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventTrace;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.game.Server;
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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropsBlock;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Info(name="AutoFarm", desc="Автоматически выращивает и собирает культуру", type= Category.OTHER)
public class AutoFarmNew extends Module {

    private final Mode culture = new Mode(this, "Культура").desc("Культура которая будет собираться");
    private final Mode.Element carrot = new Mode.Element(culture, "Морковь");
    private final Mode.Element potato = new Mode.Element(culture, "Картошка");
    private final Mode.Element wheat = new Mode.Element(culture, "Пшеница");
    private final Mode.Element beetroot = new Mode.Element(culture, "Свекла");
    private final Slider delay = new Slider(this, "Задержка сбора").min(1f).max(1000f).inc(1f).set(200f);

    private final CheckBox autoExp = new CheckBox(this, "Авто-починка").set(true);
    private final CheckBox autoSell = new CheckBox(this, "Авто-продажа").set(true);
    private final Slider autoExpPerc = new Slider(this, "Начинать чинить при").min(1f).max(100).inc(1).set(15).hide(() -> !autoExp.get());

    private final TimerUtility timer = new TimerUtility();
    private boolean autoRepair, autoCos, close;
    private int attempts = 0;
    private boolean cursorCheck;

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            ItemStack mainStack = mc.player.inventory.getStackInSlot(mc.player.inventory.currentItem);
            ItemStack offhandStack = mc.player.getHeldItemOffhand();
            ItemStack cursorStack = mc.player.inventory.getItemStack();

            List<Item> hoes = List.of(Items.NETHERITE_HOE, Items.DIAMOND_HOE);
            List<Item> items = List.of(Items.CARROT, Items.POTATO, Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS);
            boolean empty = mainStack.isEmpty();
            RayTraceResult result = mc.player.pick(3, 1.0F, false);
            ItemStack stack = mc.player.getHeldItemMainhand();

            if (autoExp.get()) {
                int max = stack.getMaxDamage(), cur = max - stack.getDamage();
                double perc = (double) cur / (double) max;

                Chat.debug(perc + " " + findItem() + " " + findExp());

                if (!cursorStack.isEmpty()) {
                    cursorCheck = true;
                    if (timer.passed(200)) {
                        int emptySlot = findEmptySlot();
                        if (emptySlot != -1) {
                            mc.playerController.windowClick(0, emptySlot < 9 ? emptySlot + 36 : emptySlot, 0, ClickType.PICKUP, mc.player);
                        }
                    }
                    return;
                } else if (cursorCheck) {
                    cursorCheck = false;
                    timer.reset();
                }

                if (autoRepair) {
                    if (offhandStack.getItem() != Items.EXPERIENCE_BOTTLE) {
                        int expSlot = findExp();
                        if (expSlot != -1 && expSlot != 45) {
                            Inventory.moveItem(expSlot, 45, true);
                        } else if (expSlot == -1) {
                            autoRepair = false;
                            if (findItem() != -1) {
                                if (cursorStack.isEmpty()) {
                                    InventoryUtility.moveItem(findItem(), 45, true);
                                }
                            }
                        }
                    } else if (timer.passed(300)) {
                        mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
                        timer.reset();

                        if (perc >= 0.95 || findExp() == -1) {
                            autoRepair = false;
                            if (cursorStack.isEmpty() && findItem() != -1) {
                                InventoryUtility.moveItem(findItem(), 45, true);
                            }
                        }
                    }
                    return;
                }

                if (hoes.contains(stack.getItem()) && perc < (autoExpPerc.get() / 100) && !autoRepair) {
                    if (findExp() == -1) {
                        toggle();
                        return;
                    }
                    autoRepair = true;
                    attempts = 0;
                    return;
                }
            }

            if (mc.player.inventory.getFirstEmptyStack() == -1 && autoSell.get()) {
                if (mc.currentScreen instanceof ContainerScreen<?> screen) {
                    if (Server.isFT() ? screen.getTitle().getString().equals("● Выберите секцию") : screen.getTitle().getString().equals("● Выбери секцию")) {
                        Inventory.clickSlotId(21, 0, ClickType.PICKUP, true);
                    }
                    if (screen.getTitle().getString().equals("Скупщик еды")) {
                        Inventory.clickSlotId(ssItem(), 0, ClickType.PICKUP, true);
                        if (timer.passed(500)) {
                            mc.player.closeScreen();
                            timer.reset();
                        }
                    }
                } else if (timer.passed(300)) {
                    mc.player.sendChatMessage("/buyer");
                    timer.reset();
                }
            } else if (result.getType() == RayTraceResult.Type.BLOCK && result instanceof BlockRayTraceResult blockResult) {
                BlockPos pos = blockResult.getPos();
                BlockState state = mc.world.getBlockState(pos);
                BlockState stateX = mc.world.getBlockState(mc.player.getPosition());

                if (state.getBlock() instanceof CropsBlock crop && stateX.getBlock().equals(Blocks.FARMLAND)) {
                    autoCos = true;
                    if (hoes.contains(stack.getItem()) && timer.passed(delay.get())) {
                        mc.player.swing(Hand.MAIN_HAND, true);
                        mc.playerController.func_217292_a(mc.player, mc.world, Hand.MAIN_HAND, blockResult);
                        timer.reset();
                    }

                    if (state.get(CropsBlock.AGE) == crop.getMaxAge()) {
                        mc.player.swing(Hand.MAIN_HAND, false);
                        mc.playerController.onPlayerDamageBlock(mc.player.getPosition().up(), Direction.UP);
                    }
                } else if (state.getBlock().equals(Blocks.FARMLAND)) {
                    BlockPos cropPos = pos.up();
                    BlockState cropState = mc.world.getBlockState(cropPos);

                    if (cropState.isAir()) {
                        if ((offhandStack.isEmpty() || !items.contains(offhandStack.getItem())) && findItem() != -1) {
                            InventoryUtility.moveItem(findItem(), 45, true);
                        } else if (items.contains(offhandStack.getItem()) && timer.passed(delay.get())) {
                            mc.player.swing(Hand.OFF_HAND, true);
                            mc.playerController.func_217292_a(mc.player, mc.world, Hand.OFF_HAND, blockResult);
                            timer.reset();
                        }
                    }
                }
            }

            if (empty || !hoes.contains(mainStack.getItem())) {
                if (findHoe() != -1) Inventory.moveItem(findHoe(), mc.player.inventory.currentItem + 36, true);
            }
        }

        BlockState stateXX = mc.world.getBlockState(mc.player.getPosition());

        if (event instanceof EventMotion e && stateXX.getBlock().equals(Blocks.FARMLAND)) {
            e.setPitch(90.0f);
            mc.player.rotationPitchHead = 90.0f;
        }
        if (event instanceof EventTrace eventTrace && stateXX.getBlock().equals(Blocks.FARMLAND)) {
            eventTrace.setPitch(90);
            eventTrace.cancel();
        }
    }

    private int findHoe() {
        for (int i = 0; i < mc.player.inventory.mainInventory.size(); ++i) {
            ItemStack stack = mc.player.inventory.mainInventory.get(i);
            if (stack.getItem() == Items.DIAMOND_HOE) {
                return i < 9 ? 36 + i : i;
            }
        }
        return -1;
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

    public int findItem() {
        for (int i = 0; i < mc.player.inventory.mainInventory.size(); ++i) {
            ItemStack stack = mc.player.inventory.mainInventory.get(i);
            if ((carrot.get() && stack.getItem() == Items.CARROT) ||
                (potato.get() && stack.getItem() == Items.POTATO) ||
                (wheat.get() && stack.getItem() == Items.WHEAT_SEEDS) ||
                (beetroot.get() && stack.getItem() == Items.BEETROOT_SEEDS)) {
                return i < 9 ? 36 + i : i;
            }
        }
        return -1;
    }

    public int ssItem() {
        if (carrot.get() && mc.player.getHeldItemOffhand().getItem() == Items.CARROT) return 10;
        else if (potato.get() && mc.player.getHeldItemOffhand().getItem() == Items.POTATO) return 11;
        else if (wheat.get() && mc.player.getHeldItemOffhand().getItem() == Items.WHEAT_SEEDS) return 14;
        else if (beetroot.get() && mc.player.getHeldItemOffhand().getItem() == Items.BEETROOT_SEEDS) return 12;
        return -1;
    }
    
    private int findEmptySlot() {
        for (int i = 0; i < 36; i++) {
            if (mc.player.inventory.getStackInSlot(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onEnable() {
        autoRepair = false;
    }

    @Override
    public void onDisable() {
        autoRepair = false;
    }
}
