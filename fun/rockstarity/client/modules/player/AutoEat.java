package fun.rockstarity.client.modules.player;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.client.modules.combat.Aura;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.AirItem;
import net.minecraft.item.UseAction;

/**
 * @author Malecharik
 * @since 15 Mar 2024 11:37:41
 */


@Info(name="AutoEat", desc="Автоматически ест еду из инвентаря", type=Category.PLAYER)
public class AutoEat extends Module {
	
    private final Slider feedLevel = new Slider(this, "Голод").min(1f).max(19f).set(15f).inc(0.5f);
    private final CheckBox swap = new CheckBox(this, "Брать в руку");
    private final CheckBox auraNo = new CheckBox(this, "Не использовать с Aura").desc("Не будет использоваться если есть таргет в Aura");
    private boolean isUse;

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate && shouldEat()) {
        	Aura aura = rock.getModules().get(Aura.class);
        	if (aura.get() && aura.getTarget() != null && auraNo.get()) return;
            mc.getGameSettings().keyBindUseItem.setPressed(isUse);

            if (mc.player.getFoodStats().getFoodLevel() < feedLevel.get()) {
                handleEating();
                isUse = true;
            } else {
                isUse = mc.player.getFoodStats().needFood();
            }
        }
    }

    private boolean shouldEat() {
        return mc.player.getHeldItemOffhand().getUseAction() == UseAction.EAT || mc.player.getHeldItemMainhand().getUseAction() == UseAction.EAT || swap.get();
    }

    private void handleEating() {
        int hotbarSlot = Player.findEatInHotbar();
        if (hotbarSlot != -1) {
            mc.player.inventory.currentItem = hotbarSlot;
        } else {
            int inventorySlot = Player.findEatInInventory();
            if (inventorySlot != -1) {
                swapItemToOffhand(inventorySlot);
            }
        }
    }

    private void swapItemToOffhand(int inventorySlot) {
        mc.playerController.windowClick(0, inventorySlot, 0, ClickType.PICKUP, mc.player);
        mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
        if (!(mc.player.getHeldItemOffhand().getItem() instanceof AirItem)) {
            mc.playerController.windowClick(0, inventorySlot, 0, ClickType.PICKUP, mc.player);
        }
    }
    
    @Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}
}
