/**
 * 
 */
package fun.rockstarity.api.events.list.player;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.util.math.BlockPos;

/**
 * @author Malecharik
 * @since 14 Mar 2024 21:17:41
 */

@AllArgsConstructor
public class EventCollision extends Event {
	@Getter
	private final BlockPos blockPos;
}
