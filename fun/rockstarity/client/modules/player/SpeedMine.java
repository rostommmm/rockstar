package fun.rockstarity.client.modules.player;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Select.Element;
import lombok.Getter;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;

/**
 * @author Malecharik
 * @since 22 Mar 2024 20:09:04
 */


@Info(name = "SpeedMine", desc = "Ускоряет копание", type = Category.PLAYER)
public class SpeedMine extends Module {
	@Getter
	private final CheckBox ft = new CheckBox(this, "Работать на FunTime");
	private final CheckBox always = new CheckBox(this, "Работать всегда").hide(this.ft::get);
	
	private final Select conditions = new Select(this, "Работать").hide(() -> this.ft.get() || this.always.get());
	
	private final Element onlyAir = new Element(conditions, "Только в воздухе");
	private final Element onlyWater = new Element(conditions, "Только в воде");
	
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventUpdate) {
        	if (this.always.get() || (this.conditions.is(this.onlyWater) && (mc.player.isInWater() || !mc.player.isOnGround())) || (this.conditions.is(this.onlyAir) && !mc.player.isOnGround())) {
        		mc.player.addPotionEffect(new EffectInstance(Effects.HASTE)); //Если выполняются все проверки то накладываем эффект спешки
        	} else {
        		mc.player.removePotionEffect(Effects.HASTE); //Иначе ремуваем эффект
        	}
		}
	}
	
	@Override
	public void onDisable() {
		mc.player.removePotionEffect(Effects.HASTE);
	}

	@Override
	public void onEnable() {
		
	}
	
}
