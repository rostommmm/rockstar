package fun.rockstarity.api.helpers.math.aura;

import fun.rockstarity.api.binds.Bindable;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.player.FallingPlayer;
import fun.rockstarity.api.helpers.player.Move;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.client.modules.combat.Aura;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

/**
 * @author ConeTin
 * @since 23 мар. 2025 г.
 */

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Boost extends Mode {
	
	Mode.Element notBoost = new Mode.Element(this, "Нет");
	Mode.Element direct = new Mode.Element(this, "Прямо");
	Mode.Element toTarget = new Mode.Element(this, "На цель");
	
	Mode boostMode = new Mode(toTarget, "Режим");
	@Getter Mode.Element spookyBoost = new Mode.Element(boostMode, "Spooky");
	Mode.Element custom = new Mode.Element(boostMode, "Свой");
	
	Slider fallingSpeed = new Slider(toTarget, "Скорость падения").min(0).max(0.4f).inc(0.05f).set(0.3f).desc("Скорость, c которой игрок будет ускоряться падая").hide(() -> spookyBoost.get());
	Slider jumpSpeed = new Slider(toTarget, "Скорость прыжка").min(0).max(0.4f).inc(0.05f).set(0.3f).desc("Скорость, c которой игрок будет ускоряться прыгая").hide(() -> spookyBoost.get());
	Slider groundSpeed = new Slider(toTarget, "Скорость на земле").min(0).max(0.4f).inc(0.05f).set(0).desc("Скорость, c которой игрок будет ускоряться на земле").hide(() -> spookyBoost.get());
	Slider centrifugalForce = new Slider(toTarget, "Центробежная сила").min(0).max(0.3f).inc(0.05f).set(0).desc("Отклонение от центра для вращения").hide(() -> spookyBoost.get());
	Slider multiplier = new Slider(toTarget, "Множитель").min(-0.2f).max(3).inc(0.05f).set(-0.1f).desc("Множитель ускорения. Для FunTime желательно ставить значения до 0, для SpookyTime можно ставить значения вполть до 3. Если не знаете, что ставить под конкретно ваш сервер, оствьте 0 или -0.1").hide(() -> false);
	
	TimerUtility collisionTimer = new TimerUtility();
	TimerUtility waitTimer = new TimerUtility();
	@NonFinal boolean disabled;
	
	public Boost(Bindable parent) {
		super(parent, "Ускорение");
	}

	public void onEvent(Event event, LivingEntity target) {
		if (event instanceof EventUpdate && target != null) {
			Aura aura = rock.getModules().get(Aura.class);

			if (direct.get()) {
				double speed = Math.hypot(Math.abs(target.prevPosX - target.getPosX()), Math.abs(target.prevPosZ - target.getPosZ()));
				//double speed = Math.hypot(this.target.getMotion().x, this.target.getMotion().z);
				
				//if (mc.player.getDistance(this.target) < (Server.is("infinity") ? 2 : 1.5f) && (speed < 0.1f || Server.is("infinity"))) {
				if (Player.collideWith(target)) {
					float p = mc.world.getBlockState(mc.player.getPosition().add(mc.player.getMotion().x, mc.player.getMotion().y, mc.player.getMotion().z)).getBlock().getSlipperiness();
					float f = mc.player.isOnGround() ? p * 1 : (Server.is("infinity") ? 0.91f : 0.81f);
					float f2 = mc.player.isOnGround() ? p : 0.99f;
		            
					//if (mc.player.fallDistance > 0)
					//	mc.player.getMotion().y *= 1.1f;
					mc.player.setVelocity(mc.player.getMotion().getX() / f * f2, mc.player.getMotion().getY(), mc.player.getMotion().getZ() / f * f2);
				} else {
					if (mc.player.fallDistance > 0.5f && Move.getSpeed() == 0) {
						//mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.START_DESTROY_BLOCK, mc.player.getPosition(), Direction.UP));
			            //mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.STOP_DESTROY_BLOCK, mc.player.getPosition(), Direction.UP));
					//	mc.player.getMotion().y *= 2f;
					}
				}
			} else if (toTarget.get()) {
				float mult = multiplier.get();
			    
				if (Player.getBlock(0, 2, 0) != Blocks.AIR || Player.getBlock(0, 3, 0) != Blocks.AIR || mc.world.getBlock(target.getPosition().add(0, 2, 0)) != Blocks.AIR || mc.world.getBlock(target.getPosition().add(0, 3, 0)) != Blocks.AIR || target.collidedHorizontally) {
			    //	return;
			    }
			    
				if (Player.collideWith(target, mult) /*mc.player.fallDistance > 0*/ && !disabled) {
				    Vector3d playerPos = mc.player.getPositionVec();
				    Vector3d targetPos = target.getPositionVec();//.add(Move.getSpeed(target) > 0.1f ? target.getMotion() : new Vector3d(1,0,0));
				    
				    Vector3d direction = targetPos.subtract(playerPos).normalize();
				    
				    float p = mc.world.getBlockState(mc.player.getPosition().add(mc.player.getMotion().x, mc.player.getMotion().y, mc.player.getMotion().z))
				            .getBlock().getSlipperiness();
				    float f = mc.player.isOnGround() ? p * 1 : (Server.is("infinity") ? 0.91f : 0.81f);
				    float f2 = mc.player.isOnGround() ? p : 0.99f;

				    double motionY = mc.player.getMotion().y;
				    
				    float ground = spookyBoost.get() ? (Player.collideWith(target) ? 0.1f : 0.05f) : groundSpeed.get();
				    float falling = spookyBoost.get() ? 0.05f : fallingSpeed.get();
				    float jump = spookyBoost.get() ? 0.05f : jumpSpeed.get();
				    
				    // Центробежная сила дада (космонавтики)
				    float gradus = System.currentTimeMillis() / 100;
				    float centrifugal = spookyBoost.get() ? 0f : centrifugalForce.get();
				    float deviationX = (float) Math.cos(Math.toDegrees(gradus)) * centrifugal;
				    float deviationZ = (float) Math.sin(Math.toDegrees(gradus)) * centrifugal;
				    direction = direction.add(deviationX, 0, deviationZ);
				    
				    double speed = mc.player.isOnGround() ? ground : mc.player.fallDistance > 0 ? falling : jump; 
				    
				    
				    boolean predictHit = !mc.player.isOnGround() && IdealHitUtility.canAIFall() && (FallingPlayer.fromPlayer(mc.player).findFall(IdealHitUtility.getNewFallDistance(target)) || aura.canCritical());
				    
					
				    if (predictHit) {
				    	//speed = -speed * 0.3f;
				    	//mc.player.movementInput.moveForward = 0;
				    	//mc.player.movementInput.moveStrafe = 0;
				    }
				    
				    double newX = direction.x * speed * f2 / f;
				    double newZ = direction.z * speed * f2 / f;
				    
				    mc.player.setVelocity(
				    		mc.player.getMotion().x
				    		+ newX, 
				    		motionY, 
				    		mc.player.getMotion().z
				    		+ newZ);
				    
				    if (collisionTimer.passed(2000)) {
				    	//disabled = true;
				    	//waitTimer.reset();
				    }
				} else {
					collisionTimer.reset();
					
					if (disabled && waitTimer.passed(1000)) {
						disabled = false;
					}
				}
			}
		}
	}

}
