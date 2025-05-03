package fun.rockstarity.api.events.list.player;

import fun.rockstarity.api.events.Event;
import lombok.*;

@Getter @Setter
@AllArgsConstructor
public class EventJump extends Event {
	private float motion, yaw;
}