/**
 * 
 */
package fun.rockstarity.client.modules.move;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Slider;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author Malecharik
 * @since 15 Mar 2024 21:19:03
 */

@NativeInclude
@Info(name = "HighJump", desc = "Высокий прыжок", type = Category.MOVE)
public class HighJump extends Module {
	private final Mode mode = new Mode(this,"Мод");
	private final Mode.Element matrix = new Mode.Element(mode, "Matrix");
	private final Mode.Element infinity = new Mode.Element(mode, "Effect");
	
	private final Slider motion = new Slider(this, "Высота").min(1).max(80).inc(1).set(6);
	
	/*
	 * Обрабатываем событие Update
	 */
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventUpdate) {
			if (this.mode.is(this.matrix)) {
				//Если игрок нажал кнопку прыжка добавляем к его y + 6
				if (mc.getGameSettings().keyBindJump.isPressed()) mc.player.getMotion().y += this.motion.get();
			} else if (this.mode.is(this.infinity)) {
				//Еффект высокого прыжка
				EffectInstance effect = new EffectInstance(Effects.JUMP_BOOST, 1,(int) this.motion.get());
				effect.setPotionDurationMax(true); //Устанавливаем максимальное время
				mc.player.addPotionEffect(effect);
				if (mc.player.isInWater() && mc.getGameSettings().keyBindJump.isKeyDown()) mc.player.jump();
				
			}
		}
	}
	
	@Override
	public void onDisable() {
		mc.player.removePotionEffect(Effects.JUMP_BOOST);
	}
	
	@Override
	public void onEnable() {
		
	}
}
