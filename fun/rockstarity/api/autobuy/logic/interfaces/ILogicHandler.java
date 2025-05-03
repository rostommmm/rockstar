package fun.rockstarity.api.autobuy.logic.interfaces;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.events.Event;

/**
 * @author ConeTin
 * @since 20 апр. 2024 г.
 */

public interface ILogicHandler extends IAccess {

	void onEvent(Event event);
	
}
