package fun.rockstarity.api.events.list.player;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author ConeTin
 * @since 6 дек. 2023 г.
 * @in Entity
 */

@Getter @Setter
@AllArgsConstructor
public class EventMove extends Event {
	
	private float yaw, pitch;
	
	public EventMove hook() {
		return (EventMove) super.hook();
	}
	
}
