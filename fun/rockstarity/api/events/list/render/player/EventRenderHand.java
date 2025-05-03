package fun.rockstarity.api.events.list.render.player;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EventRenderHand extends Event {
	private final boolean pre;
}
