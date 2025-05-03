package fun.rockstarity.api.render.globals.emotions.instance.list;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.render.player.EventModels;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.globals.emotions.instance.ActionEmotion;
import net.minecraft.entity.LivingEntity;

public class ClapEmotion extends ActionEmotion {
	
	private final Animation clapAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	
    public ClapEmotion(LivingEntity player) {
        super(player, 2500L);
        this.clapAnim.setForward(true);
    }
    
    @Override
    public void onEvent(Event event) {
        if (event instanceof EventModels model) {

            // РђРЅРёРјР°С†РёСЏ РїРѕРІС‚РѕСЂСЏРµС‚СЃСЏ С‚СѓРґР°-СЃСЋРґР°
            if (this.clapAnim.finished()) {
                this.clapAnim.setForward(false);
            } else if (this.clapAnim.finished(false)) {
                this.clapAnim.setForward(true);
            }

            if (this.canAnimateHands()) {
                float t = this.clapAnim.get();

                float xBend = moveProp(0, -0.8f * t); // РЅРµРјРЅРѕРіРѕ СЃРѕРіРЅСѓС‚СЊ
                float yInward = moveProp(0, 0.8f * t); // СЂСѓРєРё РІРЅСѓС‚СЂСЊ
                float zFlat = moveProp(0, 0.4f * t); // С‡СѓС‚СЊ РїСЂРёР¶Р°С‚СЊ Р»Р°РґРѕРЅСЊ

                // РџСЂР°РІР°СЏ СЂСѓРєР°
                model.bipedRightArm.rotateAngleX = xBend;
                model.bipedRightArm.rotateAngleY = -yInward;
                model.bipedRightArm.rotateAngleZ = zFlat;

                // Р›РµРІР°СЏ СЂСѓРєР°
                model.bipedLeftArm.rotateAngleX = xBend;
                model.bipedLeftArm.rotateAngleY = yInward;
                model.bipedLeftArm.rotateAngleZ = -zFlat;
            }
        }
        super.onEvent(event);
    }
}
