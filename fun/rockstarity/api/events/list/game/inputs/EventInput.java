package fun.rockstarity.api.events.list.game.inputs;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.helpers.player.Move;
import fun.rockstarity.client.modules.render.FreeLook;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.MathHelper;

/**
 * @author ConeTin
 * @since 6 дек. 2023 г.
 * @in MovementInputFromOptions
 */

@Getter @Setter
public class EventInput extends Event {
	private float forward, strafe;
	private boolean jump, sneak;
	private double sneakSlowDownMultiplier;
    
    public EventInput(float moveForward, float moveStrafe, boolean jump, boolean sneak) {
    	forward = moveForward;
    	strafe = moveStrafe;
    	this.jump = jump;
    	this.sneak = sneak;
    	this.sneakSlowDownMultiplier = 0.3D;
    }
    
    public EventInput hook() {
		return (EventInput) super.hook();
	}
    
    public void setYaw(float yaw, float direction) {
    	FreeLook freelook = rock.getModules().get(FreeLook.class);
		final float forward = this.getForward();
        final float strafe = this.getStrafe();

        final double angle = MathHelper.wrapDegrees(Math.toDegrees(Move.direction(freelook.get() ? freelook.getRotation().x : direction, forward, strafe)));

        if (forward == 0 && strafe == 0) {
            return;
        }

        float closestForward = 0, closestStrafe = 0, closestDifference = Float.MAX_VALUE;

        for (float predictedForward = -1F; predictedForward <= 1F; predictedForward += 1F) {
            for (float predictedStrafe = -1F; predictedStrafe <= 1F; predictedStrafe += 1F) {
                if (predictedStrafe == 0 && predictedForward == 0) continue;

                final double predictedAngle = MathHelper.wrapDegrees(Math.toDegrees(Move.direction(yaw, predictedForward, predictedStrafe)));
                final double difference = Math.abs(angle - predictedAngle);

                if (difference < closestDifference) {
                    closestDifference = (float) difference;
                    closestForward = predictedForward;
                    closestStrafe = predictedStrafe;
                }
            }
        }

        this.setForward(closestForward);
        this.setStrafe(closestStrafe);
    }
	
    public void setYaw(final float yaw) {
    	setYaw(yaw, mc.player.rotationYaw);
	}
    
}