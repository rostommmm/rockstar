package fun.rockstarity.api.helpers.math.aura.modes;

import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.math.aura.RotationMode;
import fun.rockstarity.api.helpers.math.aura.ai.AIPredictor;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Position;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.client.modules.combat.Aura;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;

/**
 * @author ConeTin
 * @since 20 мар. 2025 г.
 */

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NeuroRotation extends RotationMode {
	
	Mode mode  = new Mode(this, "Наводка");
	Mode.Element classic = new Mode.Element(mode, "Обычная");
	Mode.Element snap = new Mode.Element(mode, "Снап");
	
	Mode model  = new Mode(this, "Модель").desc("Выберите модель нейросети");
	Mode.Element all = new Mode.Element(model, "Общее");
	Mode.Element artem = new Mode.Element(model, "ConeTin");
	Mode.Element powen = new Mode.Element(model, "Powen");
	Mode.Element zahar = new Mode.Element(model, "Захар");
	Mode.Element alex = new Mode.Element(model, "saintits");
	Mode.Element egor = new Mode.Element(model, "Егор");
	Mode.Element nikita = new Mode.Element(model, "Damage");
	Mode.Element akhmedx = new Mode.Element(model, "akhmedx");
	Mode.Element stas = new Mode.Element(model, "Стас");
	
	Slider speedX = new Slider(this, "Скорость по X").min(1).max(100).inc(1).set(40).desc("Скорость по yaw/x");
	Slider speedY = new Slider(this, "Скорость по Y").min(1).max(100).inc(1).set(40).desc("Скорость по pitch/y");

	@NonFinal Vector2f prev = Vector2f.ZERO;
	
	public NeuroRotation(Mode parent) {
		super(parent, "Нейро");
	}

	@Override
	public void update(LivingEntity target) {
		prev = rotation.copy();
		
		Aura aura = rock.getModules().get(Aura.class);

		if (target == null || snap.get() && (!aura.canCritical() || !aura.getAttackTimer().passed(300))) {
			int speed = 220;
			AIPredictor.interp.animate(new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch), speed, speed);
			rotation = new Vector2f(AIPredictor.interp.getYaw(), AIPredictor.interp.getPitch());
		} else {
			rotation = AIPredictor.predict(target, rotation, prev, new Vector2f(100-speedX.get(), 100-speedY.get()));
		}
        rotation.y = MathHelper.clamp(rotation.y, -90, 90);
	}

	@Override
	public void reset(int reason) {
		float shortestYawPath = (float) (((((mc.player.rotationYaw - rotation.x) % 360) + 540) % 360) - 180);

		if (reason > 0)
			mc.player.rotationYaw = rotation.x + shortestYawPath;
		prev = new Vector2f(mc.player.prevRotationYaw, mc.player.prevRotationPitch);
		
		super.reset(reason);
		
		int speed = reason > 0 ? 240 : 1;
		AIPredictor.interp.animate(rotation, speed, speed);
	}
	
}
