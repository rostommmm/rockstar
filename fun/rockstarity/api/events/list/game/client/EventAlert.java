package fun.rockstarity.api.events.list.game.client;

import fun.rockstarity.api.events.Event;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * @author ConeTin
 * @since 12 янв. 2025 г.
 */

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventAlert extends Event {

	String text, type;
	
}
