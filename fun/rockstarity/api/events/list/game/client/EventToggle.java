package fun.rockstarity.api.events.list.game.client;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.modules.Module;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * @author ConeTin
 * @since 17 фев. 2025 г.
 */

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventToggle extends Event {

	Module module;
	boolean value;
	
}
