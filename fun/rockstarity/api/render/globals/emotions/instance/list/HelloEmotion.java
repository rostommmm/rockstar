package fun.rockstarity.api.render.globals.emotions.instance.list;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.render.player.EventModels;
import fun.rockstarity.api.render.globals.emotions.instance.ActionEmotion;
import net.minecraft.entity.LivingEntity;

/**
 * @author ConeTin
 * @since 6 Р°РІРі. 2024вЂЇРі.
 */

public class HelloEmotion extends ActionEmotion {

	public HelloEmotion(LivingEntity player) {
		super(player, 2500L);
	}
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventModels model) {
			this.actionAnim.setSpeed(250); // РЎРєРѕСЂРѕСЃС‚СЊ РІР·РјР°С…Р° СЂСѓРєРё
			// РђРЅРёРјРєР° РїСЂРёРІРµС‚СЃС‚РІРёСЏ С‡С‚РѕР±С‹ С‡РµР» РґРІРёРіР°Р» СЂСѓРєРѕР№ С‚СѓРґР° СЃСЋРґР°
			if (this.actionAnim.finished())
				this.actionAnim.setForward(false);
			else if (this.actionAnim.finished(false))
                this.actionAnim.setForward(true);
			
			if (this.canAnimateHands()) {
				// РђРЅРёРјРєР° РЅР° СЂСѓРєСѓ
				model.bipedRightArm.rotateAngleX = moveProp(model.bipedRightArm.rotateAngleX, - 0.2f);
				model.bipedRightArm.rotateAngleZ = moveProp(model.bipedRightArm.rotateAngleZ, 2.75f - 0.25f * this.actionAnim.get());
			}
		}
		
		super.onEvent(event);
	}
	
}
