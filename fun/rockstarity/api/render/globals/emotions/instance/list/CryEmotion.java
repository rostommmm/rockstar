package fun.rockstarity.api.render.globals.emotions.instance.list;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.render.player.EventModels;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.globals.emotions.instance.ActionEmotion;
import net.minecraft.entity.LivingEntity;

public class CryEmotion extends ActionEmotion {
	
	private final Animation handToFaceAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(400);
	private final Animation headBobAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(250);

	public CryEmotion(LivingEntity player) {
		super(player, 2500L);
		this.handToFaceAnim.setForward(true);
		this.headBobAnim.setForward(true);
	}
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventModels model) {
			if (this.canAnimateHands()) {
				float t = this.handToFaceAnim.get();

				float xBend = moveProp(0, -2.0f * t);
				float yInward = moveProp(0, 0.5f * t);
				float zAcross = moveProp(0, 1.2f * t);

				// РџСЂР°РІР°СЏ СЂСѓРєР°
				model.bipedRightArm.rotateAngleX = xBend;
				model.bipedRightArm.rotateAngleY = -yInward;
				model.bipedRightArm.rotateAngleZ = zAcross;

				// Р›РµРІР°СЏ СЂСѓРєР°
				model.bipedLeftArm.rotateAngleX = xBend;
				model.bipedLeftArm.rotateAngleY = yInward;
				model.bipedLeftArm.rotateAngleZ = -zAcross;
			}

			float headNod = 0.5f + 0.2f * this.headBobAnim.get();
			model.bipedHead.rotateAngleX = moveProp(model.bipedHead.rotateAngleX, headNod);
			model.bipedHead.rotateAngleZ = moveProp(model.bipedHead.rotateAngleZ, 0.15f * this.headBobAnim.get());

			model.bipedHeadwear.rotateAngleX = model.bipedHead.rotateAngleX;
			model.bipedHeadwear.rotateAngleZ = model.bipedHead.rotateAngleZ;
		}

		super.onEvent(event);
	}
}
