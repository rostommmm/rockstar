package fun.rockstarity.api.helpers.game;

import fun.rockstarity.api.IAccess;
import lombok.experimental.UtilityClass;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.LingeringPotionItem;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.item.TridentItem;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

/**
 * @author ConeTin
 * @since 26 авг. 2024 г.
 * 
 * Этот класс не пригодился для того, ради чего я его писал... Ну ладно, может потом что-то придумаем
 * 
 * UPD. а нет, пригодился
 */

@UtilityClass
public class PredictUtility implements IAccess {
	
	public Vector3d predictElytraPos(LivingEntity player, int ticks) {
		return predictElytraPos(player, player.getPositionVec(), ticks);
	}
	
	public Vector3d predictElytraPos(LivingEntity player, Vector3d pos, int ticks) {
	    Vector3d motion = player.getMotion();
	    
	    for (int i = 0; i < ticks; i++) {
	    	Vector3d lookVec = player.getLookVec();
	        float pitchRad = (float) Math.toRadians(player.rotationPitch);
	        double horizontalSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
	        double motionMag = motion.length();
	        float f1 = MathHelper.cos(pitchRad);
	        f1 = (float) (f1 * f1 * Math.min(1.0D, lookVec.length() / 0.4D));

	        motion = motion.add(0.0D, -0.08D * (-1.0D + (double) f1 * 0.75D), 0.0D);

	        if (motion.y < 0.0D && horizontalSpeed > 0.0D) {
	            double d5 = motion.y * -0.1D * f1;
	            motion = motion.add(lookVec.x * d5 / horizontalSpeed, d5, lookVec.z * d5 / horizontalSpeed);
	        }

	        if (pitchRad < 0.0F && horizontalSpeed > 0.0D) {
	            double lift = motionMag * (-MathHelper.sin(pitchRad)) * 0.04D;
	            motion = motion.add(-lookVec.x * lift / horizontalSpeed, lift * 3.2D, -lookVec.z * lift / horizontalSpeed);
	        }

	        if (horizontalSpeed > 0.0D) {
	            motion = motion.add(
	                (lookVec.x / horizontalSpeed * motionMag - motion.x) * 0.1D, 
	                0.0D, 
	                (lookVec.z / horizontalSpeed * motionMag - motion.z) * 0.1D
	            );
	        }

	        motion = motion.mul(0.99D, 0.98D, 0.99D);
	        pos = pos.add(motion);
	    }
	    
	    return pos;
	}
	
	@UtilityClass
	public static class Projectile {
		private Vector3d simulateShoot(double x, double y, double z, float velocity, float inaccuracy)
	    {
			float rand = 0.5f;
	        Vector3d vector3d = (new Vector3d(x, y, z)).normalize().add(0.5f * (double)0.0075F * (double)inaccuracy, 0.5f * (double)0.0075F * (double)inaccuracy, 0.5f * (double)0.0075F * (double)inaccuracy).scale((double)velocity);
	        return vector3d;
	    }

	    private Vector3d simulateThrow(Entity thrower, float pitch, float yaw, float p_234612_4_, float velocity, float inaccuracy)
	    {
	        float f = -MathHelper.sin(yaw * ((float)Math.PI / 180F)) * MathHelper.cos(pitch * ((float)Math.PI / 180F));
	        float f1 = -MathHelper.sin((pitch + p_234612_4_) * ((float)Math.PI / 180F));
	        float f2 = MathHelper.cos(yaw * ((float)Math.PI / 180F)) * MathHelper.cos(pitch * ((float)Math.PI / 180F));
	        Vector3d shootMotion = simulateShoot((double)f, (double)f1, (double)f2, velocity, inaccuracy);
	        Vector3d vector3d = thrower.getMotion();
	        return shootMotion.add(vector3d.x, thrower.isOnGround() ? 0.0D : vector3d.y, vector3d.z);
	    }
	    
	    public Vector3d predictThrowMotion(LivingEntity thrower, Item item)
        {
	        if (item instanceof TridentItem) {
	            return simulateThrow(thrower, thrower.rotationPitch, thrower.rotationYaw, 0.0F, 2.5F, 1.0F);
	        } else if (item instanceof SplashPotionItem || item instanceof LingeringPotionItem) {
	        	return simulateThrow(thrower, thrower.rotationPitch, thrower.rotationYaw, 0.0F, 0.5F, 1.0F);
	        }
	        return simulateThrow(thrower, thrower.rotationPitch, thrower.rotationYaw, 0.0F, 1.5F, 1.0F);
        }
	    
	    public Vector3d predictThrowPos(LivingEntity thrower) {
	    	return new Vector3d(thrower.getPosX(), thrower.getPosYEye() - (double)0.1F, thrower.getPosZ());
	    }
	}
}
