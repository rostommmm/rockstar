package fun.rockstarity.api.events.list.game;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.Entity;

/**
 * @author Malecharik
 * @since 8 мая 2024 г. 21:41:29
 */
@AllArgsConstructor
public class EventSpawn extends Event {
	@Getter
	private final Entity entity;
}
