package fun.rockstarity.api.events.list.player;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.util.math.BlockPos;

@AllArgsConstructor
public class EventThrowPearl extends Event {
	@Getter
	private int x, y, z;
	@Getter
	private float yaw, pitch;
}
