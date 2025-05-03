package fun.rockstarity.api.helpers.math.aura;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.MathUtility;
import javafx.animation.Interpolator;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

/**
 * @author ConeTin
 * @since 5 дек. 2023 г.
 */

@UtilityClass
public class Rotation implements IAccess {
	
	@Getter
	private float yaw = -1337, pitch = -1337, body = 0;
	
	public Vector3d getBestPoint(Vector3d pos, Entity entity) {
        if (Server.is("infinity"))
            return entity.getPositionVec().add(0, 1, 0);

        return new Vector3d(
                MathHelper.clamp(pos.x, entity.getBoundingBox().minX, entity.getBoundingBox().maxX),
                MathHelper.clamp(pos.y + mc.player.getEyeHeight(), entity.getBoundingBox().minY, entity.getBoundingBox().maxY),
                MathHelper.clamp(pos.z, entity.getBoundingBox().minZ, entity.getBoundingBox().maxZ)
        );
    }
	
	public Vector2f correctRotation(float yaw, float pitch) {
       // if ((yaw == -90 && pitch == 90) || yaw == -180) return new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);

        float gcd = getGCDValue();
        yaw -= yaw % gcd;
        pitch -= pitch % gcd;
        
        return new Vector2f(yaw, pitch);
    }
	
	public float getGCDValue() {
		double realGcd = mc.gameSettings.mouseSensitivity;
		double d4 = realGcd * (double) 0.6F + (double) 0.2F;
		return (float) (d4 * d4 * d4 * 8.0D * 0.15);
    }

	public Vector2f get(Vector3d from, Vector3d target) {
		Vector3d vec = target;
        double posX = vec.getX() - from.getX();
        double posY = vec.getY() - from.getY();
        double posZ = vec.getZ() - from.getZ();
        double sqrt = MathHelper.sqrt(posX * posX + posZ * posZ);
        float yaw = (float) (Math.atan2(posZ, posX) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) (-(Math.atan2(posY, sqrt) * 180.0 / Math.PI));
        float sens = (float) (Math.pow(mc.getGameSettings().mouseSensitivity, 1.5) * 0.05f + 0.1f);
        float pow = sens * sens * sens * 1.2F;
        yaw -= yaw % pow;
        pitch -= pitch % (pow * sens);
        return new Vector2f(yaw, pitch);
	}
	
	public Vector2f get(Vector3d target) {
		Vector3d vec = target;
        double posX = vec.getX() - mc.player.getPosX();
        double posY = vec.getY() - (mc.player.getPosY() + (double) mc.player.getEyeHeight());
        double posZ = vec.getZ() - mc.player.getPosZ();
        double sqrt = MathHelper.sqrt(posX * posX + posZ * posZ);
        float yaw = (float) (Math.atan2(posZ, posX) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) (-(Math.atan2(posY, sqrt) * 180.0 / Math.PI));
        float sens = (float) (Math.pow(mc.getGameSettings().mouseSensitivity, 1.5) * 0.05f + 0.1f);
        float pow = sens * sens * sens * 1.2F;
        yaw -= yaw % pow;
        pitch -= pitch % (pow * sens);
        return new Vector2f(yaw, pitch);
	}
	
	public float getDirection() {
		float rotationYaw = mc.player.rotationYaw;
		float factor = 0f;
		
		boolean forwardKeyDown = mc.getGameSettings().keyBindForward.isKeyDown();
		boolean backKeyDown = mc.getGameSettings().keyBindBack.isKeyDown();
		boolean leftKeyDown = mc.getGameSettings().keyBindLeft.isKeyDown();
		boolean rightKeyDown = mc.getGameSettings().keyBindRight.isKeyDown();
		float forward = forwardKeyDown == backKeyDown ? 0.0F : (forwardKeyDown ? 1.0F : -1.0F);
		float strafe = leftKeyDown == rightKeyDown ? 0.0F : (leftKeyDown ? 1.0F : -1.0F);
		
		if (forward > 0)
            factor = 1;
        if (forward < 0)
            factor = -1;

        if (factor == 0) {
            if (strafe > 0)
                rotationYaw -= 90;

            if (strafe < 0)
                rotationYaw += 90;
        } else {
            if (strafe > 0)
                rotationYaw -= 45 * factor;

            if (strafe < 0)
                rotationYaw += 45 * factor;
        }

        if (factor < 0)
            rotationYaw -= 180;
        
        if (body < rotationYaw - 50) body = rotationYaw - 50;
    	if (body > rotationYaw + 50) body = rotationYaw + 50;
        
		return rotationYaw;
	}
	
}
