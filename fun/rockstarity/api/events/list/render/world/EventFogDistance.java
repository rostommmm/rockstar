package fun.rockstarity.api.events.list.render.world;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class EventFogDistance extends Event {

	private float inner, outer;
	
}