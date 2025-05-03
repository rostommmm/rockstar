package fun.rockstarity.api.render.globals.emotions.instance.list;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.render.player.EventModels;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.globals.emotions.instance.Emotion;
import net.minecraft.entity.LivingEntity;

/**
 * @author ConeTin
 * @since 6 авг. 2024 г.
 */

public class ConditionsEmotion extends Emotion {
	
	protected final Animation itemDropAnim = new Animation().setEasing(Easing.EASE_OUT_BOUNCE); // Анимация действия

	public ConditionsEmotion(LivingEntity player) {
		super(player, 3500L);
	}
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventModels model) {
			// Права нога
			model.bipedRightLeg.rotateAngleX = moveProp(model.bipedRightLeg.rotateAngleZ, -1.3f * itemDropAnim.get());
			itemDropAnim.setEasing(Easing.EASE_OUT_BOUNCE);
			itemDropAnim.setForward(true);
		}
		
		super.onEvent(event);
	}
	
}
