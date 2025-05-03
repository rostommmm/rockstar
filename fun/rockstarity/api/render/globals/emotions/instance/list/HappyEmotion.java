package fun.rockstarity.api.render.globals.emotions.instance.list;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.render.player.EventModels;
import fun.rockstarity.api.render.globals.emotions.instance.ActionEmotion;
import fun.rockstarity.api.render.globals.emotions.instance.Emotion;
import net.minecraft.entity.LivingEntity;

public class HappyEmotion extends Emotion {

    public HappyEmotion(LivingEntity player) {
        super(player, 2500L);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventModels model) {
            if (this.canAnimateHands()) {
                // Анимка на правую руку
                model.bipedRightArm.rotateAngleX = moveProp(model.bipedRightArm.rotateAngleX, 0f);
                model.bipedRightArm.rotateAngleZ = moveProp(model.bipedRightArm.rotateAngleZ, 2.75f);
                // Анимка на левую руку
                model.bipedLeftArm.rotateAngleX = moveProp(model.bipedLeftArm.rotateAngleX, 0f);
                model.bipedLeftArm.rotateAngleZ = moveProp(model.bipedLeftArm.rotateAngleZ, -2.75f);
            }
        }

        super.onEvent(event);
    }
}
