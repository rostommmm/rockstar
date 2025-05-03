package fun.rockstarity.api.events.list.render.world;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

@Getter @Setter
@AllArgsConstructor
public class EventCameraDistance extends Event {
	
    private double distance;
    
}
