package fun.rockstarity.api.events.list.render.ui;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ConeTin
 * @since 14 июн. 2024 г.
 */

@Getter
@AllArgsConstructor
public class EventBossBar extends Event {
	
	private final String text;
	
}
