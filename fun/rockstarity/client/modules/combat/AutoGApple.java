package fun.rockstarity.client.modules.combat;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.EventType;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.math.InventoryUtility;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Slider;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.AirItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Effects;

/**
 * @author ConeTin
 * @since 6 дек. 2023 г.
 */


@Info(name="AutoGApple", desc="Автоматически ест геплы", type=Category.COMBAT)
public class AutoGApple extends Module {
	
	private final Slider health = new Slider(this, "Здоровье").min(1).max(20).inc(0.5f).set(15).desc("Здоровье при котором будет кушаться гепл");
	private final CheckBox eatBegining = new CheckBox(this, "Есть в начале");
	private final TimerUtility waitTimer = new TimerUtility();
	private boolean isEating;
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventUpdate) {
			if (shouldToTakeGApple() && eatBegining.get()) {
				takeGappleInOffHand();
			}

			eatGapple();
		}
	}

	private void eatGapple() {
		if (conditionToEat()) {
			startEating();
		} else if (isEating) {
			stopEating();
		}
	}

	private boolean shouldToTakeGApple() {
		boolean isTicksExisted = mc.player.ticksExisted == 15;
		boolean appleNotEaten = mc.player.getAbsorptionAmount() == 0.0f || !mc.player.isPotionActive(Effects.REGENERATION);
		boolean appleIsNotOffHand = mc.player.getHeldItemOffhand().getItem() != Items.GOLDEN_APPLE;
		boolean timeHasPassed = waitTimer.passed(200);
		boolean settingIsEnalbed = eatBegining.get();

		return (isTicksExisted && appleNotEaten && appleIsNotOffHand & timeHasPassed) && settingIsEnalbed;
	}

	private void takeGappleInOffHand() {
		int gappleSlot = InventoryUtility.getInstance().getSlotInInventory(Items.GOLDEN_APPLE);

		if (gappleSlot >= 0) {
			moveGappleToOffhand(gappleSlot);
		}
	}

	private void moveGappleToOffhand(int gappleSlot) {
		if (gappleSlot < 9 && gappleSlot != -1) {
			gappleSlot += 36;
		}
		mc.playerController.windowClick(0, gappleSlot, 0, ClickType.PICKUP, mc.player);
		mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
		if (!(mc.player.getHeldItemOffhand().getItem() instanceof AirItem)) {
			mc.playerController.windowClick(0, gappleSlot, 0, ClickType.PICKUP, mc.player);
		}
		waitTimer.reset();
	}

	private void startEating() {
		if (mc.currentScreen != null) {
			mc.currentScreen.passEvents = true;
		}
		if (!mc.gameSettings.keyBindUseItem.isKeyDown()) {
			mc.gameSettings.keyBindUseItem.setPressed(true);
			isEating = true;
		}
	}

	private void stopEating() {
		mc.gameSettings.keyBindUseItem.setPressed(false);
		isEating = false;
	}

	private boolean conditionToEat() {
		float myHealth = mc.player.getHealth() + mc.player.getAbsorptionAmount();
		boolean appleNotEaten = mc.player.getAbsorptionAmount() == 0.0f
				|| !mc.player.isPotionActive(Effects.REGENERATION);

		return (isHealthLow(myHealth) || mc.player.ticksExisted < 100 && appleNotEaten)
				&& hasGappleInHand()
				&& !isGappleOnCooldown();
	}

	private boolean isGappleOnCooldown() {
		return mc.player.getCooldownTracker().hasCooldown(Items.GOLDEN_APPLE);
	}

	private boolean isHealthLow(float health) {
		return health <= this.health.get();
	}

	private boolean hasGappleInHand() {
		return mc.player.getHeldItemMainhand().getItem() == Items.GOLDEN_APPLE ||
				mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE;
	}

	private void reset() {
		waitTimer.reset();
	}

	@Override
	public void onDisable() {
		reset();
	}

	@Override
	public void onEnable() {
		
	}
    
}
