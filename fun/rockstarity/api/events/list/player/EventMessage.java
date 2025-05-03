package fun.rockstarity.api.events.list.player;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ConeTin
 * @since 2 дек. 2023 г.
 * @in ClientPlayerEntity
 */

@Getter
@AllArgsConstructor
public class EventMessage extends Event {
	
	private final String message;
	
}
