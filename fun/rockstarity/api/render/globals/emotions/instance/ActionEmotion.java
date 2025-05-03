package fun.rockstarity.api.render.globals.emotions.instance;

import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import net.minecraft.entity.LivingEntity;

/**
 * @author ConeTin
 * @since 6 авг. 2024 г.
 * Класс для тех эмоций, в которых есть постоянное действие
 */

public class ActionEmotion extends Emotion {

	protected final Animation actionAnim = new Animation().setEasing(Easing.BOTH_SINE); // Анимация действия
	
	public ActionEmotion(LivingEntity player, long time) {
		super(player, time);
	}
	
	public ActionEmotion(LivingEntity player) {
		super(player);
	}
	
}
