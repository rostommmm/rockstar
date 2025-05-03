/**
 * 
 */
package fun.rockstarity.api.events.list.player;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Malecharik
 * @since 12 мая 2024 г. 12:56:17
 */

@Getter @Setter
@AllArgsConstructor
public class EventPostMotion extends Event {
	
	private boolean packetSended;
	
	public EventPostMotion hook() {
		return (EventPostMotion) super.hook();
	}
	
}
