/**
 * 
 */
package fun.rockstarity.api.events.list.player;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

/**
 * @author Malecharik
 * @since 22 Mar 2024 20:32:05
 */
@AllArgsConstructor
@Getter
@Setter
public class EventInteractBlock extends Event {
    private BlockPos pos;
    private Direction face;
}
