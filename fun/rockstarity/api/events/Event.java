package fun.rockstarity.api.events;

import fun.rockstarity.api.IAccess;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * @author ConeTin
 * @since 2 дек. 2023 г.
 */

@FieldDefaults(level = AccessLevel.PRIVATE)
public class Event implements IAccess {
	
	@Getter @Setter boolean cancel;
	
	public void cancel() {
		this.cancel = true;
	}
	
	public <T extends Event> Event hook() {
		EventHandler.handleEvent(this);
		
		return this;
	}
	
}
