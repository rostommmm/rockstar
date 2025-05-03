package fun.rockstarity.api.events.list.game.inputs;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.client.modules.render.FreeLook;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;

/**
 * @author ConeTin
 * @since 6 авг. 2024 г.
 */

@Getter @Setter
@AllArgsConstructor
public class EventMouseMove extends Event {
    
	private double xVelocity, yVelocity;

	public EventMouseMove hook() {
		return (EventMouseMove) super.hook();
	}
    
}