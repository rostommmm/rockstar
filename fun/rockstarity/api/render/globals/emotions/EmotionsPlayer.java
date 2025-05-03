package fun.rockstarity.api.render.globals.emotions;

import java.util.Map.Entry;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.EventType;
import fun.rockstarity.api.events.list.game.EventTridentHitted;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.events.list.render.EventRender3D;
import fun.rockstarity.api.events.list.render.player.EventModels;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.render.globals.emotions.instance.Emotion;
import net.minecraft.entity.LivingEntity;

/**
 * @author ConeTin
 * @since 6 авг. 2024 г.
 */

public class EmotionsPlayer {
	
	private final Emotions emotions;
	
	public EmotionsPlayer(Emotions emotions) {
		this.emotions = emotions;
	}
	
	public void onEvent(Event event) {
		Emotion toRemove = null;
		
		try {
			for (Emotion emotion : this.emotions.getActiveEmotions()) {
				LivingEntity player = emotion.getPlayer();
				
				if (!(event instanceof EventModels model) || model.getOwner() == player) {
					emotion.onEvent(event);
				}
				
				if (event instanceof EventUpdate) {
					if (emotion.getEmotionAnim().finished(false) && !emotion.getEmotionAnim().isForward())
						toRemove = emotion;
				}
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}
		
		if (event instanceof EventUpdate && toRemove != null) {
			this.emotions.getActiveEmotions().remove(toRemove);
		}
	}

}
