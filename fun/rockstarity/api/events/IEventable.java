package fun.rockstarity.api.events;

/**
 * @author ConeTin
 * @since 2 дек. 2023 г.
 */

public interface IEventable {

	void onEvent(Event event);
	
	void onAllEvent(Event event);
}
