package fun.rockstarity.api.events.list.lua;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.scripts.wrappers.base.LivingEntityBase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.LivingEntity;

@Getter
@AllArgsConstructor
public class EventLuaAttack extends Event {
	
	private final LivingEntityBase target;
	
	public LivingEntityBase target() {
		return target;
	}
	
}
