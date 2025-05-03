/**
 * 
 */
package fun.rockstarity.api.events.list.player;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

/**
 * @author Malecharik
 * @since 6 мая 2024 г. 19:30:23
 */
@Getter
@AllArgsConstructor
public class EventPlace extends Event {
	private final Block block;
	private final BlockPos pos;
}
