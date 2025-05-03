package fun.rockstarity.api.scripts.wrappers.base;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.math.Hover;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.math.aura.Rotation;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class MathBase implements IAccess {
	
	public float[] rotation(EntityBase e) {
		Entity entity = e.getEntity();
		return MathUtility.getRotVec(mc.player.getPositionVec(), Rotation.getBestPoint(mc.player.getEyePosition(mc.timer.renderPartialTicks), entity));
	}
	
	public LivingEntityBase search_target(float range) {
		ArrayList<LivingEntity> targets = new ArrayList<>();
		for (Entity e : mc.world.getAllEntities()) {
			if (e instanceof LivingEntity && e != mc.player) {
				if (e instanceof AbstractClientPlayerEntity) {
			        AbstractClientPlayerEntity clientPlayer = (AbstractClientPlayerEntity) e;
			        if (e.getUniqueID().equals(UUID.nameUUIDFromBytes(("OfflinePlayer:" + e.getName()).getBytes(StandardCharsets.UTF_8)))) {
			            continue;
			        }
				}
		        
				Vector3d best = Rotation.getBestPoint(mc.player.getEyePosition(mc.timer.renderPartialTicks), e);

				if (best != null && MathHelper.sqrt(best.distanceTo(mc.player.getEyePosition(mc.getRenderPartialTicks()))) < range) {
					targets.add((LivingEntity)e);
				}
			}
		}
		targets.sort(Comparator.comparingDouble(mc.player::getDistance));
		return targets.size() == 0 ? null : new LivingEntityBase(targets.get(0));
	}
	
	public boolean hovered(double x, double y, double width, double height, double mouseX, double mouseY) {
		return Hover.isHovered(x, y, width, height, mouseX, mouseY);
	}
	
	public double min(double a, double b) {
		return Math.min(a, b);
	}
	
	public double round(double a) {
		return Math.round(a);
	}
	
	public double max(double a, double b) {
		return Math.max(a, b);
	}
	
	public double hypot(double a, double b) {
		return Math.hypot(a, b);
	}
	
	public double abs(double a) {
		return Math.abs(a);
	}
	
	public double ceil(double a) {
		return Math.ceil(a);
	}
	
	public double cos(double a) {
		return Math.cos(a);
	}

	public double sin(double a) {
		return Math.sin(a);
	}
	
	public double deg(double a) {
		return Math.toDegrees(a);
	}
	
	public double exp(double a) {
		return Math.exp(a);
	}
	
	public double floor(double a) {
		return Math.floor(a);
	}
	
	public double rad(double a) {
		return Math.toRadians(a);
	}
	
	public double tan(double a) {
		return Math.tan(a);
	}
	
	public double sqrt(double a) {
		return Math.sqrt(a);
	}
	
	public double pow(double a, double b) {
		return Math.pow(a, b);
	}
	
	public double random() {
		return Math.random();
	}
	
	public double random(double a, double b) {
		return MathUtility.random(a, b);
	}
	
	public double atan2(double a, double b) {
		return Math.atan2(a, b);
	}
	
}