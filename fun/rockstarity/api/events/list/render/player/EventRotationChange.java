package fun.rockstarity.api.events.list.render.player;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.client.modules.render.FreeLook;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;

/**
 * @author ConeTin
 * @since 8 недрочаб. 2024 г.
 */

@Getter @Setter
@AllArgsConstructor
public class EventRotationChange extends Event {
	// Это не яв питч, а разница старого нового если чо (delta)
	private double yaw, pitch;
    
    public EventRotationChange hook() {
		return (EventRotationChange) super.hook();
	}
    
}