package fun.rockstarity.api.events.list.player;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.secure.Debugger;
import fun.rockstarity.client.modules.combat.Aura;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.MathHelper;

/**
 * @author ConeTin
 * @since 2 дек. 2023 г.
 * @in ClientPlayerEntity
 */

@Getter
public class EventMotion extends Event {
	
	public static float LAST_YAW, LAST_PITCH;
	
	private float yaw, pitch;
	@Setter
	private boolean ground;
	
	public EventMotion(float yaw, float pitch, boolean ground) {
		this.yaw = yaw;
		this.pitch = pitch;
		this.ground = ground;
	}
	
	public void setYaw(float yaw) {
		this.yaw = yaw;
		
		if (Math.abs(EventMotion.LAST_YAW - yaw) > 90) {
		  //Chat.debug(EventMotion.LAST_YAW + " - " + yaw + " - " + rock.getModules().get(Aura.class).getTarget());
		}
	}
	
	public void setPitch(float pitch) {
		this.pitch = pitch;
		LAST_PITCH = pitch;
	}
	
	public EventMotion hook() {
		return (EventMotion) super.hook();
	}
	
}
