package fun.rockstarity.api.render.globals.emotions.instance.list;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.render.player.EventModels;
import fun.rockstarity.api.render.globals.emotions.instance.Emotion;
import net.minecraft.entity.LivingEntity;

/**
 * @author ConeTin
 * @since 6 авг. 2024 г.
 */

public class DebEmotion extends Emotion {

	public DebEmotion(LivingEntity player) {
		super(player);
	}
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventModels model) {
			if (this.canAnimateHands()) {
				// Правая рука
				model.bipedRightArm.rotateAngleX = moveProp(model.bipedRightArm.rotateAngleX, -0.7f);
				model.bipedRightArm.rotateAngleZ = moveProp(model.bipedRightArm.rotateAngleZ, -2);
		    	
				// Левая рука
				model.bipedLeftArm.rotateAngleX = moveProp(model.bipedLeftArm.rotateAngleX, -0.7f);
				model.bipedLeftArm.rotateAngleZ = moveProp(model.bipedLeftArm.rotateAngleZ, -2);
			}
    	
			// Голова
			model.bipedHead.rotateAngleX = moveProp(model.bipedHead.rotateAngleX, 0.7f);
			model.bipedHead.rotateAngleY = moveProp(model.bipedHead.rotateAngleY, 0.5f);
			
			model.bipedHeadwear.rotateAngleX = moveProp(model.bipedHeadwear.rotateAngleX, 0.7f);
			model.bipedHeadwear.rotateAngleY = moveProp(model.bipedHeadwear.rotateAngleY, 0.5f);
		}
		
		super.onEvent(event);
	}
	
}
