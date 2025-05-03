package fun.rockstarity.api.render.globals.emotions.instance.list;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.render.player.EventModels;
import fun.rockstarity.api.helpers.math.RussianNumberParser;
import fun.rockstarity.api.render.globals.emotions.instance.ActionEmotion;
import fun.rockstarity.api.secure.Debugger;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.LlamaSpitEntity;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;

/**
 * @author ConeTin
 * @since 6 Р°РІРі. 2024вЂЇРі.
 */

public class MasturbateEmotion extends ActionEmotion {

	private boolean spit;
	
	public MasturbateEmotion(LivingEntity player) {
		super(player, 3500L);
	}
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventModels model) {
			this.actionAnim.setSpeed(100); // Р”СЂРѕС‡РєР° РґРѕР»Р¶РЅР° Р±С‹С‚СЊ Р±С‹СЃС‚СЂРѕР№
			// РђРЅРёРјРєР° РґСЂРѕС‡РєРё С‡С‚РѕР±С‹ С‡РµР» РґРІРёРіР°Р» СЂСѓРєРѕР№ С‚СѓРґР° СЃСЋРґР°
			if (this.actionAnim.finished())
				this.actionAnim.setForward(false);
			else if (this.actionAnim.finished(false))
				this.actionAnim.setForward(true);

			if (!player.isSneaking()) {
				if (this.canAnimateHands()) {
					// РџСЂР°РІР°СЏ СЂСѓРєР°
					model.bipedRightArm.rotateAngleX = moveProp(model.bipedRightArm.rotateAngleX, -0.3f - 0.7f * this.actionAnim.get());
					model.bipedRightArm.rotateAngleZ = moveProp(model.bipedRightArm.rotateAngleZ, -0.55f);

					// Р›РµРІР°СЏ СЂСѓРєР°
					model.bipedLeftArm.rotateAngleX = moveProp(model.bipedLeftArm.rotateAngleX, 0.3f);
					model.bipedLeftArm.rotateAngleZ = moveProp(model.bipedLeftArm.rotateAngleZ, 1f);
				}

				// РўРµР»Рѕ
				model.bipedBody.rotateAngleX = moveProp(model.bipedBody.rotateAngleX, -0.1f);

				// РџСЂР°РІР° РЅРѕРіР°
				model.bipedRightLeg.rotationPointZ = moveProp(0, -1f);

				// Р›РµРІР°СЏ РЅРѕРіР°
				model.bipedLeftLeg.rotationPointZ = moveProp(0, -1f);
			} else {
				if (this.canAnimateHands()) {
					// РџСЂР°РІР°СЏ СЂСѓРєР°
					model.bipedRightArm.rotateAngleX = moveProp(model.bipedRightArm.rotateAngleX, 0.7f - 0.7f * this.actionAnim.get());
					model.bipedRightArm.rotateAngleZ = moveProp(model.bipedRightArm.rotateAngleZ, -0.55f);
				}
			}
			
			//if (!spit && emotionTimer.passed(time-500)) {
			//if (mc.player.ticksExisted % 20 == 0) {
 			//	spit();
			//}
		}
		
		super.onEvent(event);
	}
	
	private void spit() {
		LlamaSpitEntity llamaspitentity = new LlamaSpitEntity(mc.world, mc.player);
		
		double speed = 12;
		double x = -Math.sin(Math.toRadians(mc.player.rotationYaw)) * speed;
		double z = Math.cos(Math.toRadians(mc.player.rotationYaw)) * speed;
		
        double d0 = x - mc.player.getPosX();
        double d1 = 0;
        double d2 = z - mc.player.getPosZ();
        float f = MathHelper.sqrt(d0 * d0 + d2 * d2) * 0.2F;
        llamaspitentity.shoot(d0, d1 + (double)f, d2, 1.5F, 10.0F);

        mc.world.playSound(mc.player, mc.player.getPosX(), mc.player.getPosYEye(), mc.player.getPosZ(), SoundEvents.ENTITY_LLAMA_SPIT, mc.player.getSoundCategory(), 1.0F, 1.0F);
        
        mc.world.addEntity(llamaspitentity);
        
        spit = true;
	}
	
}
