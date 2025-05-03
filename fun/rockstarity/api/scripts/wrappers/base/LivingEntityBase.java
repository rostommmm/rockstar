package fun.rockstarity.api.scripts.wrappers.base;

import fun.rockstarity.api.helpers.game.Server;
import net.minecraft.entity.LivingEntity;

public class LivingEntityBase extends EntityBase {
	private LivingEntity entity;
	
	public LivingEntityBase(LivingEntity entity) {
		super(entity);
		this.entity = entity;
	}
	
	public LivingEntity getEntity() {
		return entity;
	}
	
	public int hurt_time() {
		return entity.hurtTime;
	}
	
	public float health() {
		return Server.isFT() || Server.isRW() ? entity.getRealHealth() : entity.getHealth();
	}
	
	public float gold_health() {
		return Server.isFT() ? 0 : entity.getAbsorptionAmount();
	}
	
	public float max_health() {
		return entity.getMaxHealth();
	}
	
	public float body_yaw() {
		return entity.renderYawOffset;
	}
}