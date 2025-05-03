package fun.rockstarity.api.events.list.render.entity;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.Entity;

@Getter
@AllArgsConstructor
public class EventRenderTag extends Event {
	
	private final String tag;
	private final Entity entity;
	
}