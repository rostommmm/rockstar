package fun.rockstarity.api.events.list.lua;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.scripts.wrappers.base.LivingEntityBase;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EventLuaKill extends Event {
	
	private final LivingEntityBase target;
	
	public LivingEntityBase target() {
		return target;
	}
	
}