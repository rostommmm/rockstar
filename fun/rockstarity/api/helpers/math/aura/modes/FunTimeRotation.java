package fun.rockstarity.api.helpers.math.aura.modes;

import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.math.aura.IdealHitUtility;
import fun.rockstarity.api.helpers.math.aura.Rotation;
import fun.rockstarity.api.helpers.math.aura.RotationMode;
import fun.rockstarity.api.helpers.player.FallingPlayer;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.animation.infinity.InfinityAnimation;
import fun.rockstarity.api.secure.Debugger;
import fun.rockstarity.client.modules.combat.Aura;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

/**
 * @author ConeTin
 * @since 20 мар. 2025 г.
 */

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FunTimeRotation extends RotationMode {

	Animation xAnim = new Animation().setSpeed(300).setEasing(Easing.BOTH_CIRC);
	Animation yAnim = new Animation().setSpeed(300).setEasing(Easing.BOTH_CIRC);
	
	InfinityAnimation pitchAnim = new InfinityAnimation();
	@NonFinal Vector2f additional = Vector2f.ZERO;

	public FunTimeRotation(Mode parent) {
		super(parent, "FunTime Snap");
	}

	@Override
	public void update(LivingEntity target) {
		Aura aura = rock.getModules().get(Aura.class);
		
		xAnim.setSpeed(700);
		yAnim.setSpeed(500);
		
		xAnim.kuni();
		yAnim.kuni();
		
		Vector3d pos = mc.player.getEyePosition(0);
		
		Vector3d fastPoint = target == null ? Vector3d.ZERO : new Vector3d(
                MathHelper.clamp(pos.x, 
                		target.getBoundingBox().minX, 
                		target.getBoundingBox().maxX),
                
                MathHelper.clamp(pos.y, 
                		target.getBoundingBox().minY, 
                		target.getBoundingBox().maxY),
                
                MathHelper.clamp(pos.z, 
                		target.getBoundingBox().minZ, 
                		target.getBoundingBox().maxZ)
        );
		
		
		Vector2f rot = target == null ? new Vector2f(0, mc.player.rotationPitch) : get(aura.getMultipoint().get() ? fastPoint : target.getPositionVec().add(0, target.getHeight() / 2F, 0));
		
		if (target != null) {
			if (!aura.isSnapTick() && (!(FallingPlayer.fromPlayer(mc.player).findFall(IdealHitUtility.getNewFallDistance(target)) && !mc.player.isOnGround() || aura.canCritical()) || !aura.getAttackTimer().passed(300))) {
				rot.x = xAnim.get() * 40 - 20;
				rot.y += yAnim.get() * 15 - 7;
			} else {
			    if (Server.isFT() && aura.isSnapTick()) {
			      rot.y += MathUtility.random(5, 30);
			      rot.x += MathUtility.random(5, 10);
			    }
			}
		}
		
		rotation.x = mc.player.rotationYaw + MathUtility.step(rotation.x - mc.player.rotationYaw, rot.x, MathUtility.random(80, 85));
		rotation.y = pitchAnim.animate(rot.y, MathUtility.randomInt(100, 150));
		rotation = Rotation.correctRotation(rotation.x, rotation.y);
		
		//mc.player.rotationYaw + xAnim.get() * 40 - 20, rot.y + yAnim.get() * 15 - 7
	}
	
	public Vector2f get(Vector3d target) {
		Vector3d vec = target;
        double posX = vec.getX() - mc.player.getPosX();
        double posY = vec.getY() - (mc.player.getPosY() + (double) mc.player.getEyeHeight());
        double posZ = vec.getZ() - mc.player.getPosZ();
        double sqrt = MathHelper.sqrt(posX * posX + posZ * posZ);
        float yaw = (float) (Math.atan2(posZ, posX) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) (-(Math.atan2(posY, sqrt) * 180.0 / Math.PI));
        
        float shortestYawPath = (float) (((((yaw - mc.player.rotationYaw) % 360) + 540) % 360) - 180);
		yaw = shortestYawPath;
		
        return new Vector2f(yaw, pitch);
	}
	
	@Override
	public void reset(int reason) {
		//float shortestYawPath = (float) (((((mc.player.rotationYaw - rotation.x) % 360) + 540) % 360) - 180);

		//if (reason > 0)
		//	mc.player.rotationYaw = rotation.x + shortestYawPath;
		//prev = new Vector2f(mc.player.prevRotationYaw, mc.player.prevRotationPitch);
		
		//super.reset(reason);
		Aura aura = rock.getModules().get(Aura.class);

		if (aura.getTarget() == null && reason == 0) {
		//	rotation.x = mc.player.rotationYaw;
		}
	}
	
}
