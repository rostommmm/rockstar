/**
 * 
 */
package fun.rockstarity.api.events.list.player;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;


/**
 * @author ConeTin
 * @since 25 апр. 2024 г.
 */

@Getter @Setter
@AllArgsConstructor
public class EventPostMotionMove extends Event {
	
	private double speed;
	
}
