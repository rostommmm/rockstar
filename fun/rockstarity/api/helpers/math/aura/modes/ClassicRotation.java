package fun.rockstarity.api.helpers.math.aura.modes;

import org.checkerframework.checker.units.qual.s;

import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.math.aura.Rotation;
import fun.rockstarity.api.helpers.math.aura.RotationMode;
import fun.rockstarity.api.helpers.player.Bypass;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.animation.infinity.RotationAnimation;
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
public class ClassicRotation extends RotationMode {
	
	Mode mode  = new Mode(this, "Наводка");
	Mode.Element classic = new Mode.Element(mode, "Обычная");
	Mode.Element snap = new Mode.Element(mode, "Снап");
	
	CheckBox fullpacket = new CheckBox(this, "Абуз 1.17+").hide(() -> !Bypass.via() || !get() || classic.get());
	
	Mode speedMode  = new Mode(this, "Режим скорости");
	Mode.Element fast = new Mode.Element(speedMode, "Моментальная");
	Mode.Element staticSpeed = new Mode.Element(speedMode, "Статичная");
	Mode.Element formula = new Mode.Element(speedMode, "Формула");

	Slider speedX = new Slider(this, "Скорость по X").min(1).max(200).inc(1).set(40).desc("Скорость по yaw/x/горизонтали").hide(() -> fast.get() || formula.get());
	Slider speedY = new Slider(this, "Скорость по Y").min(1).max(200).inc(1).set(40).desc("Скорость по pitch/y/вертикали").hide(() -> fast.get() );

	@NonFinal Vector2f prev = Vector2f.ZERO;
	RotationAnimation interp = new RotationAnimation();

	public ClassicRotation(Mode parent) {
		super(parent, "Классическая");
	}

	@Override
	public void update(LivingEntity target) {
		prev = rotation.copy();
		
		Aura aura = rock.getModules().get(Aura.class);
		
		if (target == null || snap.get() && (!aura.canCritical() || !aura.getAttackTimer().passed(300))) {
			int speed = 220;
			interp.animate(new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch), speed, speed);
			rotation = new Vector2f(interp.getYaw(), interp.getPitch());
		} else {
			Vector3d pos = mc.player.getEyePosition(0);
			
			Vector3d fastPoint = new Vector3d(
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
			 
			Vector2f rot = Rotation.get(aura.getMultipoint().get() || !aura.getAdditional().get() ? fastPoint : target.getEyePosition(0));
			
			if (fast.get()) {
				rotation = rot;
			} else {
				float shortestYawPath = (float) (((((rot.x - interp.getYaw()) % 360) + 540) % 360) - 180);
				float targetYaw = interp.getYaw() + shortestYawPath;
				float targetPitch = rot.y;

				rotation = interp.animate(new Vector2f(targetYaw, targetPitch), (int) yawSpeed(Math.abs(interp.getYaw() - targetYaw)), (int) (100-speedY.get()));
			}
		}
	}

	private float yawSpeed(float diff) {
		if (staticSpeed.get()) {
			return 100-speedX.get();
		}
		
		return MathHelper.clamp(
				diff, 						    	// Разница между интерполированной ротацией и обычной
				MathUtility.randomInt(1, 50),   	// Максимальная возможная скорость
				MathUtility.randomInt(150, 200)		// Минимальная возможная скорость - втф??? ты по-моему перепутал - не перепутал, у нас скорость в миллисекундах, чем больше - тем медленнее
			);
	}
	
	@Override
	public void reset(int reason) {
		float shortestYawPath = (float) (((((mc.player.rotationYaw - rotation.x) % 360) + 540) % 360) - 180);

		if (reason > 0)
			mc.player.rotationYaw = rotation.x + shortestYawPath;
		prev = new Vector2f(mc.player.prevRotationYaw, mc.player.prevRotationPitch);
		
		super.reset(reason);
		
		int speed = reason > 0 ? 240 : 1;
		interp.animate(rotation, speed, speed);
	}
	
}