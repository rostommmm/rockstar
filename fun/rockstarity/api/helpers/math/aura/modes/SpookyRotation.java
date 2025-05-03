package fun.rockstarity.api.helpers.math.aura.modes;

import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.math.VectorUtility;
import fun.rockstarity.api.helpers.math.aura.AuraUtility;
import fun.rockstarity.api.helpers.math.aura.Rotation;
import fun.rockstarity.api.helpers.math.aura.RotationMode;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.animation.infinity.InfinityAnimation;
import fun.rockstarity.api.render.animation.infinity.RotationAnimation;
import fun.rockstarity.client.modules.combat.Aura;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import org.checkerframework.checker.units.qual.A;

import static net.minecraft.util.math.MathHelper.wrapDegrees;

/**
 * @author ConeTin
 * @since 20 мар. 2025 г.
 */

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SpookyRotation extends RotationMode {
	Vector2f rotateVector = new Vector2f(0,0);

	public SpookyRotation(Mode parent) {
		super(parent, "Альтернативная");
	}

	@Override
	public void update(LivingEntity target) {
		Aura aura = rock.getModules().get(Aura.class);
		if (target != null) {
			Vector3d vec = VectorUtility.getBestVector(target, 0);
			rotateVector.x = mc.player.rotationYawHead; rotateVector.y = mc.player.rotationPitchHead;

			float shortestYawPath = (float) ((((((Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90) - this.rotation.x) % 360) + 540) % 360) - 180);
			float yawToTarget = rotateVector.x + shortestYawPath;
			float pitchToTarget = (float) -Math.toDegrees(Math.atan2(vec.y, Math.hypot(vec.z, vec.x)));

			float yawDelta = (wrapDegrees(yawToTarget - rotateVector.x));
			float pitchDelta = (wrapDegrees(pitchToTarget - rotateVector.y));

			float yaw, pitch, speed = AuraUtility.getSens(MathUtility.randomNew(0.6, 0.8)), clampYaw, clampPitch,
					testCountSpeed = AuraUtility.getSens(MathUtility.randomNew(130, 155));
			float rayCaster = MathUtility.rayTraceWithBlock(aura.getRange().get(), rotateVector.x, rotateVector.y, mc.player, target, false) ? 0.4f : 1;

			clampYaw = Math.min(Math.abs(yawDelta), testCountSpeed) * speed;
			yawDelta = yawDelta > 0 ? clampYaw : -clampYaw;

			boolean check = Player.collideWith(target) && (stalin(target)
					|| Player.getBlock(0, 2, 0) != Blocks.AIR && Player.getBlock(0, -1, 0)
					!= Blocks.AIR && Player.getBlock(0, 2, 0) != Blocks.WATER && Player.getBlock(0, -1, 0) != Blocks.WATER);

			if (check) yawDelta /= 30;

			clampPitch = Math.min(Math.abs(pitchDelta), MathUtility.random(23.133, 26.477)) * speed / testCountSpeed * 90 * rayCaster;
			pitchDelta = pitchDelta > 0 ? clampPitch : -clampPitch;

			if (pitchDelta > 0) pitchDelta += MathUtility.random(-0.826, 0.459);

			yawDelta = AuraUtility.fixDeltaNonVanillaMouse(yawDelta, pitchDelta).x;
			pitchDelta = AuraUtility.fixDeltaNonVanillaMouse(yawDelta, pitchDelta).y;

			yaw = rotateVector.x + yawDelta;
			pitch = MathHelper.clamp(rotateVector.y + pitchDelta, -90, 90);

			yaw = AuraUtility.correctRotation(yaw); pitch = AuraUtility.correctRotation(pitch);

			rotation.x = yaw; rotation.y = pitch;
			rotation = Rotation.correctRotation(rotation.x, rotation.y);
		} else {
			rotation = new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);
		}
	}
	

    private boolean stalin(LivingEntity target) {
		Vector3d pos = target.getPositionVec();
		AxisAlignedBB hitbox = target.getBoundingBox();
		
		float off = 0.05f;
		
		return !isAir(hitbox.minX-off, pos.y, hitbox.minZ-off)
			|| !isAir(hitbox.maxX+off, pos.y, hitbox.minZ-off)
			|| !isAir(hitbox.minX-off, pos.y, hitbox.maxZ+off)
			|| !isAir(hitbox.maxX+off, pos.y, hitbox.maxZ+off);
	}
	
	private boolean isAir(double x, double y, double z) {
		return mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.AIR;
	}
}
