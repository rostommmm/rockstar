package fun.rockstarity.api.events.list.game;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author ConeTin
 * @since 6 янв. 2025 г.
 */

@Getter @Setter
@AllArgsConstructor
public class EventChat extends Event {
	private String message;
}