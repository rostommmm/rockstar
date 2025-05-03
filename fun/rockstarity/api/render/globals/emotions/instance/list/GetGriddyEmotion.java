package fun.rockstarity.api.render.globals.emotions.instance.list;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.render.player.EventModels;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.globals.emotions.instance.ActionEmotion;
import net.minecraft.entity.LivingEntity;

/**
 * @author ConeTin
 * @since 6 Р°РІРі. 2024вЂЇРі.
 */

public class GetGriddyEmotion extends ActionEmotion {
	
	final Animation legAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(200); // РђРЅРёРјРєР° РЅРѕРі РґР»СЏ РіРµС‚ РіСЂРёРґРґРё
	final Animation toEyeAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(500); // РђРЅРёРјРєР°, РєРѕРіРґР° СЂСѓРєРё С‚СЏРЅСѓС‚СЃСЏ Рє РіР»Р°Р·Р°Рј
	final TimerUtility eyeTimer = new TimerUtility(); // РўР°Р№РјРµСЂ - СЃРєРѕР»СЊРєРѕ СЂСѓРєРё Р±СѓРґСѓС‚ РґРµСЂР¶Р°С‚СЊСЃСЏ Сѓ РіР»Р°Р·
	int armSwings; // РљРѕР»-РІРѕ РІР·РјР°С…РѕРІ СЂСѓРєРѕР№

	public GetGriddyEmotion(LivingEntity player) {
		super(player, 5500L);
	}
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventModels model) {
			this.actionAnim.setSpeed(350); // РћРїС‚РёРјР°Р»СЊРЅР°СЏ РІР·РјР°С…Р° СЂСѓРє
			
			if (this.toEyeAnim.finished(false)) {
				if (this.actionAnim.finished()) {
					this.actionAnim.setForward(false);
					this.armSwings++;
				} else if (this.actionAnim.finished(false)) {
                    this.actionAnim.setForward(true);
				}
			}
			
			this.legAnim.setSpeed(300); // TODO СѓР±СЂР°С‚СЊ
			if (this.legAnim.finished())
				this.legAnim.setForward(false);
			else if (this.legAnim.finished(false))
                this.legAnim.setForward(true);
			
			if (this.armSwings > 2) {
				this.toEyeAnim.setForward(true);
				this.eyeTimer.reset();
				this.armSwings = 0;
			}
			
			if (this.eyeTimer.passed(1000)) {
				this.toEyeAnim.setForward(false);
			}
			
			if (this.canAnimateHands()) {
				// РџСЂР°РІР°СЏ СЂСѓРєР°
				model.bipedRightArm.rotateAngleX = moveProp(model.bipedRightArm.rotateAngleX,  1 - 1.5f * this.actionAnim.get() - 3.5f * this.toEyeAnim.get());
				model.bipedRightArm.rotateAngleZ = moveProp(model.bipedRightArm.rotateAngleZ, 0.5f-0.75f * this.actionAnim.get());
				
				// Р›РµРІР°СЏ СЂСѓРєР°
				model.bipedLeftArm.rotateAngleX = moveProp(model.bipedRightArm.rotateAngleX,  1 - 1.5f * this.actionAnim.get() - 3.5f * this.toEyeAnim.get());
				model.bipedLeftArm.rotateAngleZ = moveProp(model.bipedRightArm.rotateAngleZ, -0.5f+0.75f * this.actionAnim.get());
			}
			
			// РўРµР»Рѕ
			model.bipedBody.rotateAngleX = moveProp(model.bipedBody.rotateAngleX, 0.2f - 0.05f * this.actionAnim.get());
			
			// РџСЂР°РІР° РЅРѕРіР°
			model.bipedRightLeg.rotationPointZ = moveProp(0, 2f - 0.5f * this.actionAnim.get());
			model.bipedRightLeg.rotateAngleX = moveProp(model.bipedRightLeg.rotateAngleZ, 0.3f * this.legAnim.get() - 0.3f);
			
			// Р›РµРІР°СЏ РЅРѕРіР°
			model.bipedLeftLeg.rotationPointZ = moveProp(0, 2f - 0.5f * this.actionAnim.get());
			model.bipedLeftLeg.rotateAngleX = moveProp(model.bipedLeftLeg.rotateAngleZ, -0.3f * this.legAnim.get() - 0.1f);
			
			// Р“РѕР»РѕРІР°
			model.bipedHead.rotateAngleY = moveProp(model.bipedHead.rotateAngleZ, -0.4f+0.4f * this.toEyeAnim.get());
			model.bipedHead.rotateAngleX = moveProp(model.bipedHead.rotateAngleX, -0.3f * this.toEyeAnim.get());
			model.bipedHead.rotateAngleZ = moveProp(model.bipedHead.rotateAngleZ, 0);
			
			model.bipedHeadwear.rotateAngleY = moveProp(model.bipedHead.rotateAngleZ, -0.4f+0.4f * this.toEyeAnim.get());
			model.bipedHeadwear.rotateAngleX = moveProp(model.bipedHead.rotateAngleX, -0.3f * this.toEyeAnim.get());
			model.bipedHeadwear.rotateAngleZ = moveProp(model.bipedHead.rotateAngleZ, 0);
		}
		
		super.onEvent(event);
	}
	
}
