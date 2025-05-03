package fun.rockstarity.client.modules.other;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Select.Element;
import net.minecraft.potion.Effects;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

@Info(name="PotionRemover", desc="Удаляет действия плохих эффектов", type=Category.PLAYER)
public class PotionRemover extends Module {
	
	private final Select select = new Select(this, "Эффект..");
	
	private final Element jumpBoost = new Element(select, "Прыгучести");
	private final Element levitation = new Element(select, "Левитации");
	private final Element slowFalling = new Element(select, "Плавное падение");

	@Override
	public void onEvent(Event event) {
		if (event instanceof EventMotion) {
			if (jumpBoost.get()) {
				mc.player.removePotionEffect(Effects.JUMP_BOOST);
			}
			
			if (levitation.get()) {
				mc.player.removePotionEffect(Effects.LEVITATION);
			}
			
			if (slowFalling.get()) {
				mc.player.removePotionEffect(Effects.SLOW_FALLING);
			}
		}
	}
	@NativeInclude
	@Override
	public void onEnable() {
	}

	@Override
	public void onDisable() {
	}
	
}
