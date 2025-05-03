package fun.rockstarity.api.helpers.player;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.events.list.game.inputs.EventInput;
import fun.rockstarity.api.events.list.player.EventMotionMove;
import fun.rockstarity.client.modules.render.FreeLook;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

/**
 * @author ConeTin
 * @since 8 дек. 2023 г.
 */

@UtilityClass
public class Move implements IAccess {
	
	@Getter
	private int airTicks;
	
	public void updateAirTicks() {
		if (!mc.player.isOnGround()) {
			airTicks++;
		} else {
			airTicks = 0;
		}
	}
	
	public void setMoveMotion(final EventMotionMove move, final double motion) {
        double forward = mc.player.movementInput.moveForward;
        double strafe = mc.player.movementInput.moveStrafe;
        FreeLook freelook = rock.getModules().get(FreeLook.class);
        float yaw = freelook.get() ? freelook.getRotation().x : mc.player.rotationYaw;
        if (forward == 0 && strafe == 0) {
            move.getMotion().x = 0;
            move.getMotion().z = 0;
        } else {
            if (forward != 0) {
                if (strafe > 0) {
                    yaw += (float) (forward > 0 ? -45 : 45);
                } else if (strafe < 0) {
                    yaw += (float) (forward > 0 ? 45 : -45);
                }
                strafe = 0;
                if (forward > 0) {
                    forward = 1;
                } else if (forward < 0) {
                    forward = -1;
                }
            }
            move.getMotion().x = forward * motion * MathHelper.cos((float) Math.toRadians(yaw + 90.0f))
                    + strafe * motion * MathHelper.sin((float) Math.toRadians(yaw + 90.0f));
            move.getMotion().z = forward * motion * MathHelper.sin((float) Math.toRadians(yaw + 90.0f))
                    - strafe * motion * MathHelper.cos((float) Math.toRadians(yaw + 90.0f));
        }
    }
	
	public boolean isMoving() {
		return (mc.player.movementInput.moveForward != 0F || mc.player.movementInput.moveStrafe != 0F);
	}
	
	public double getSpeed() {
		return Math.hypot(mc.player.getMotion().x,mc.player.getMotion().z);
	}
	
	public double getSpeed(Entity entity) {
		return Math.hypot(entity.getMotion().x, entity.getMotion().z);
	}
	
	public void setSpeed(double moveSpeed) {
		FreeLook freelook = rock.getModules().get(FreeLook.class);
		setSpeed(moveSpeed, freelook.get() ? freelook.getRotation().x : mc.player.rotationYaw, mc.player.movementInput.moveStrafe, mc.player.movementInput.moveForward);
	}

	public void setSpeed(double moveSpeed, float yaw, double strafe, double forward) {
	    if (forward != 0.0D) {
	        yaw += strafe > 0.0D ? (forward > 0.0D ? -45 : 45) : (strafe < 0.0D ? (forward > 0.0D ? 45 : -45) : 0);
	        strafe = 0.0D;
	        forward = forward > 0.0D ? 1.01D : (forward < 0.0D ? -1.01D : 0.0D);
	    }
	    strafe = strafe > 0.0D ? 1.0D : (strafe < 0.0D ? -1.0D : 0.0D);
	    double mx = Math.cos(Math.toRadians(yaw + 90.0F)), mz = Math.sin(Math.toRadians(yaw + 90.0F));
	    mc.player.getMotion().x = forward * moveSpeed * mx + strafe * moveSpeed * mz;
	    mc.player.getMotion().z = forward * moveSpeed * mz - strafe * moveSpeed * mx;
	}
	
    public double[] getSpeed(final double speed) {
        float moveForward = mc.player.movementInput.moveForward;
        float moveStrafe = mc.player.movementInput.moveStrafe;
        float rotationYaw = mc.player.prevRotationYaw + (mc.player.rotationYaw - mc.player.prevRotationYaw) * mc.getRenderPartialTicks();
        if (moveForward != 0.0f) {
        	rotationYaw += (moveStrafe > 0.0f) ? ((moveForward > 0.0f) ? -45 : 45) : (moveStrafe < 0.0f) ? ((moveForward > 0.0f) ? 45 : -45) : 0;
            moveStrafe = 0.0f;
            moveForward = moveForward > 0 ? moveForward = 1.0f : -1;
        }
        double rotationRadians = Math.toRadians(rotationYaw);
        double sinRotation = Math.sin(rotationRadians);
        double cosRotation = Math.cos(rotationRadians);
        
        final double posX = moveForward * speed * -sinRotation + moveStrafe * speed * cosRotation;
        final double posZ = moveForward * speed * cosRotation - moveStrafe * speed * -sinRotation;
        return new double[] { posX, posZ };
    }
    
    public double direction(float rotationYaw, final double moveForward, final double moveStrafing) {
        if (moveForward < 0F) rotationYaw += 180F;

        float forward = 1F;

        if (moveForward < 0F) forward = -0.5F;
        else if (moveForward > 0F) forward = 0.5F;

        if (moveStrafing > 0F) rotationYaw -= 90F * forward;
        if (moveStrafing < 0F) rotationYaw += 90F * forward;

        return Math.toRadians(rotationYaw);
    }
	
}
