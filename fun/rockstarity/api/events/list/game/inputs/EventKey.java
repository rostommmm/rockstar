package fun.rockstarity.api.events.list.game.inputs;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ConeTin
 * @since 2 дек. 2023 г.
 * @in KeyboardListener & MouseHelper
 */

@Getter
@AllArgsConstructor
public class EventKey extends Event {

	private final int key, scancode;
	private final boolean released;
	
}
