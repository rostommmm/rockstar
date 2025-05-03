package fun.rockstarity.client.modules.player;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.player.InvUtility;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Slider;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.item.Items;
import net.minecraft.potion.Effects;

/**
 * @author Malecharik
 * @since 12 мая 2024 г. 12:20:53
 */

@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@Info(name="AutoUse", desc="Автоматическое использование предметов", type=Category.PLAYER)
public class AutoUse extends Module {
	
	Select items = new Select(this, "Предметы").min(1).desc("Предметы, которые будут использоваться автоматически");
	Select.Element godaura = new Select.Element(items, "Божья аура").set(true);
	Select.Element dezz = new Select.Element(items, "Дезориентация").set(true);
	Select.Element yav = new Select.Element(items, "Явная пыль").set(true);
	
	Select godauraConditions = new Select(this, "Условия для божки").desc("Условия, при которых будет использоваться божья аура").hide(() -> !godaura.get());
	Select.Element godauraCD = new Select.Element(godauraConditions, "Чарка без кд").set(true);
	
	Select dezzConditions = new Select(this, "Условия для дезорки").desc("Условия, при которых будет использоваться дезориентация").hide(() -> !dezz.get());
	Select.Element revenge = new Select.Element(dezzConditions, "В ответ").set(true);
	Select.Element minhealth = new Select.Element(dezzConditions, "Если мало хп").set(true);
	Slider health = new Slider(minhealth, "Здоровье").min(1).max(19).inc(0.5f).set(6).hide(() -> !this.minhealth.get());
	
	TimerUtility dezzTimer = new TimerUtility();
	TimerUtility godTimer = new TimerUtility();
	TimerUtility sugarTimer = new TimerUtility();
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventUpdate) {
			if (dezzTimer.passed(1000) && dezz.get()
					&& !mc.player.isHandActive()
					&& !mc.player.getCooldownTracker().hasCooldown(Items.ENDER_EYE)
					&& Player.findItem(44, Items.ENDER_EYE) != -1) {
				if ((revenge.get() && mc.player.isPotionActive(Effects.MINING_FATIGUE) && mc.player.get(Effects.MINING_FATIGUE).getAmplifier() > 2)
						|| (minhealth.get() && (mc.player.getHealth() + mc.player.getAbsorptionAmount()) < health.get())) {
					InvUtility.use(Items.ENDER_EYE);
					dezzTimer.reset();
				}
			}
			
			if (godTimer.passed(1000) && godaura.get()
					&& !mc.player.isHandActive()
					&& (!godauraCD.get() || !mc.player.getCooldownTracker().hasCooldown(Items.ENCHANTED_GOLDEN_APPLE))
					&& !mc.player.getCooldownTracker().hasCooldown(Items.PHANTOM_MEMBRANE)
					&& Player.findItem(44, Items.PHANTOM_MEMBRANE) != -1) {
				if (mc.player.isPotionActive(Effects.JUMP_BOOST)
						|| (mc.player.isPotionActive(Effects.WEAKNESS) && mc.player.get(Effects.WEAKNESS).getAmplifier() > 1)) {
					InvUtility.use(Items.PHANTOM_MEMBRANE);
					godTimer.reset();
				}
			}

			if (sugarTimer.passed(1000) && yav.get()
					&& !mc.player.isHandActive()
					&& Player.findItem(44, Items.SUGAR) != -1
					&& mc.player.hurtTime > 0
					&& Server.hasCT()) {
				if (!mc.player.getCooldownTracker().hasCooldown(Items.SUGAR)) {
					InvUtility.use(Items.SUGAR);
					sugarTimer.reset();
				}
			}
		}
	}
	
	@Override
	public void onEnable() {
	}
	
	@Override
	public void onDisable() {
	}
}
