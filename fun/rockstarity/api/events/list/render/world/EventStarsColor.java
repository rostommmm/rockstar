package fun.rockstarity.api.events.list.render.world;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.render.color.FixColor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class EventStarsColor extends Event {

	private FixColor color;
	
}