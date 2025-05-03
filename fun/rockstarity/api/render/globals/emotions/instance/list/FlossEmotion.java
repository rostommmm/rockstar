package fun.rockstarity.api.render.globals.emotions.instance.list;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.render.player.EventModels;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.globals.emotions.instance.ActionEmotion;
import net.minecraft.entity.LivingEntity;

/**
 * @author ConeTin
 * @since 6 Р°РІРі. 2024вЂЇРі.
 */

public class FlossEmotion extends ActionEmotion {

	// РђРЅРёРјР°С†РёРё СЃРјРµРЅС‹ СЃС‚РѕСЂРѕРЅС‹ РґР»СЏ С„Р»РѕСЃСЃР° (С‚РёРїРѕ С‡С‚РѕР±С‹ СЃРЅР°С‡Р°Р»Р° Р»РµРІР°СЏ СЂСѓРєР° СЃР·Р°РґРё, Р° РїРѕС‚РѕРј СЃРїРµСЂРµРґРё)
	private final Animation rightChangeSideAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(250);
	private final Animation leftChangeSideAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(250);
	private int leftSwings, rightSwings; // Р’СЃРїРѕРјРѕРіР°С‚РµР»СЊРЅС‹Рµ С…СѓР№РЅРё
	
	public FlossEmotion(LivingEntity player) {
		super(player, 4500L);
		this.leftSwings = 1;
		this.rightSwings = 1;
		this.rightChangeSideAnim.setForward(true);
		this.leftChangeSideAnim.setForward(true);
	}
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventModels model) {
			// РЇ РµР±Р°Р» СЌС‚Рѕ РІСЃС‘...
			this.actionAnim.setSpeed(250); // РћРїС‚РёРјР°Р»СЊРЅР°СЏ СЃРєРѕСЂРѕСЃС‚СЊ РґР»СЏ С„Р»РѕСЃСЃР°
			// РђРЅРёРјРєР° С„Р»РѕСЃСЃР°, С‡С‚РѕР±С‹ С‡РµР»РёРє РґРІРёРіР°Р» СЂСѓРєР°РјРё С‚СѓРґР° СЃСЋРґР°
			if (this.actionAnim.finished()) {
				if (this.leftSwings == 2) {
					this.leftChangeSideAnim.setForward(false);
				}
				if (this.leftSwings == 3) {
					this.leftChangeSideAnim.setForward(true);
				}
				//Chat.debug(leftSwings);
				if (this.leftSwings > 2) {
					this.leftSwings = 0;
				}
				
				this.leftSwings++;
				this.actionAnim.setForward(false);
			} else if (this.actionAnim.finished(false)) {
				if (this.rightSwings == 1) {
					this.rightChangeSideAnim.setForward(false);
				}
				if (this.rightSwings == 2) {
					this.rightChangeSideAnim.setForward(true);
				}
				if (this.rightSwings > 2) {
					this.rightSwings = 0;
				}
				
				this.rightSwings++;
                this.actionAnim.setForward(true);
			}
			
			if (this.canAnimateHands()) {
				// РџСЂР°РІР°СЏ СЂСѓРєР°
				model.bipedRightArm.rotateAngleX = moveProp(model.bipedRightArm.rotateAngleX,  0.5f - 1f * this.rightChangeSideAnim.get());
				model.bipedRightArm.rotateAngleZ = moveProp(model.bipedRightArm.rotateAngleZ, 1-2f * this.actionAnim.get());
				
				// Р›РµРІР°СЏ СЂСѓРєР°
				model.bipedLeftArm.rotateAngleX = moveProp(model.bipedRightArm.rotateAngleX,  0.5f - 1f * this.leftChangeSideAnim.get());
				model.bipedLeftArm.rotateAngleZ = moveProp(model.bipedRightArm.rotateAngleZ, 1.5f-2f * this.actionAnim.get());
			}
			
			// РўРµР»Рѕ
			model.bipedBody.rotateAngleZ = moveProp(0, 0.3f * this.actionAnim.get() - 0.15f);
			
			// РџСЂР°РІР° РЅРѕРіР°
			model.bipedRightLeg.rotationPointX = moveProp(-2, -0.5f - 3f * this.actionAnim.get());

			// Р›РµРІР°СЏ РЅРѕРіР°
			model.bipedLeftLeg.rotationPointX = moveProp(2, 3 - 2.5f * this.actionAnim.get());
		}
		
		super.onEvent(event);
	}
	
}
