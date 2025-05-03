package fun.rockstarity.api.events.list.render.entity;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.Entity;

/**
 * @author ConeTin
 * @since 11 дек. 2023 г.
 * @in EntityRendererManager
 */

@Getter
@AllArgsConstructor
public class EventRenderEntity extends Event {
	private final Entity entity;
	private final boolean pre;
}