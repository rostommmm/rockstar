package fun.rockstarity.client.modules.player;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventBreakingBad;
import fun.rockstarity.api.events.list.game.inputs.EventKey;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.player.Inventory;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Binding;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

@Info(name="MineHelper", desc="Помощник на шахте", type= Category.PLAYER)
public class MineHelper extends Module {

    private final Binding fixBind = new Binding(this, "Кнопка починки");
    private final CheckBox save = new CheckBox(this, "Сохранять кирку");

    private boolean release = false;
    private final TimerUtility timer = new TimerUtility();
    private boolean cursorCheck;

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventKey e) this.handleKey(e);

        if (event instanceof EventUpdate) {
            Item mainHand = mc.player.getHeldItemMainhand().getItem();
            ItemStack stack = mc.player.getHeldItemMainhand();
            ItemStack cursorStack = mc.player.inventory.getItemStack();

            int max = stack.getMaxDamage(), cur = max - stack.getDamage();

            double perc = (double) cur / (double) max;

            if (release) {
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
                if ((mainHand == Items.NETHERITE_PICKAXE || mainHand == Items.DIAMOND_PICKAXE) && findPickaxe() != -1 & perc < 0.95) {

                    int exp = findExp();

                    if (mc.player.getHeldItemOffhand().getItem() != Items.EXPERIENCE_BOTTLE) {
                        if (exp != -1) {
                            Inventory.moveItem(exp, 45, true);
                        }
                    } else if (timer.passed(100)) {
                        mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
                        timer.reset();
                    }
                }
            }
        }
        if (event instanceof EventMotion e && release) {
            e.setPitch(80.0f);
            mc.player.rotationPitchHead = 80.0f;
        }

        if (event instanceof EventBreakingBad e && save.get()) {
            ItemStack stack = mc.player.getHeldItemMainhand();

            int max = stack.getMaxDamage(), cur = max - stack.getDamage();

            double perc = (double) cur / (double) max;

            if (perc < 0.1 && (stack.getItem() == Items.DIAMOND_PICKAXE || stack.getItem() == Items.NETHERITE_PICKAXE)) {
                e.cancel();
                if (timer.passed(800)) {
                    rock.getAlertHandler().alert("Кирка на грани поломки!", AlertType.ERROR);
                    timer.reset();
                }
            }
        }
    }

    private void handleKey(EventKey e) {
        if (mc.currentScreen == null && fixBind.getBindByKey(e).isPresent()) {
            release = !e.isReleased();
            if (e.isReleased()) mc.playerController.windowClick(0, 45, 0, ClickType.QUICK_MOVE, mc.player);
        }
    }

    @NativeInclude
    private int findPickaxe() {
        for (int i = 0; i < mc.player.inventory.mainInventory.size(); ++i) {
            ItemStack stack = mc.player.inventory.mainInventory.get(i);
            if (stack.getItem() == Items.NETHERITE_PICKAXE || stack.getItem() == Items.DIAMOND_PICKAXE) {
                return i < 9 ? 36 + i : i;
            }
        }
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
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }
}
