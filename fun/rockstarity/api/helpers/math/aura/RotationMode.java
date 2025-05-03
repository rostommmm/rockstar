package fun.rockstarity.api.helpers.math.aura;

import fun.rockstarity.api.modules.settings.list.Mode;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector2f;

/**
 * @author ConeTin
 * @since 20 мар. 2025 г.
 */

public abstract class RotationMode extends Mode.Element {
	
	protected Vector2f rotation = Vector2f.ZERO;

	public RotationMode(Mode parent, String name) {
		super(parent, name);
	}
	
	public abstract void update(LivingEntity target);
	
	public void reset(int reason) {
		rotation = new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);
	}
	
	public float getYaw() {
		return rotation.x;
	}
	
	public float getPitch() {
		return rotation.y;
	}
	
}
