package fun.rockstarity.api.scripts.wrappers.base;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.scripts.Script;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;

public class EntityBase implements IAccess {
	private Entity entity;
	
	public EntityBase(Entity entity) {
		this.entity = entity;
	}
	
	public Entity getEntity() {
		return entity;
	}
	
	public VectorBase pos() {
		if (Script.isActive3d()) {
			return new Vector3d(
					entity.lastTickPosX + (entity.getPosX() - entity.lastTickPosX) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getX(),
					entity.lastTickPosY + (entity.getPosY() - entity.lastTickPosY) * mc.getRenderPartialTicks()  - mc.getRenderManager().info.getProjectedView().getY(),
					entity.lastTickPosZ + (entity.getPosZ() - entity.lastTickPosZ) * mc.getRenderPartialTicks()  - mc.getRenderManager().info.getProjectedView().getZ()
					);
		} else {
			return entity.getPositionVec();
		}
	}
	
	public boolean watching(float range, float yaw, float pitch, LivingEntityBase base) {
		return MathUtility.rayTraceWithBlock(range, yaw, pitch, entity, base.getEntity(), false);
	}

	public VectorBase motion() {
		return entity.getMotion();
	}
	
	public String name() {
		return entity.getName().getString();
	}
	
	public float fall_distance() {
		return entity.fallDistance;
	}
	
	public boolean ground() {
		return entity.isOnGround();
	}
	
	public boolean in_water() {
		return entity.isInWater();
	}
	
	public boolean in_lava() {
		return entity.isInLava();
	}
	
	public float pitch() {
		return entity.rotationPitch;
	}
	
	public float yaw() {
		return entity.rotationYaw;
	}
	
	public boolean isPlayer() {
		return entity instanceof PlayerEntity;
	}

	public int id() {
		return entity.getEntityId();
	}
}