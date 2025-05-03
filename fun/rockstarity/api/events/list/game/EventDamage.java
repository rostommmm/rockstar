package fun.rockstarity.api.events.list.game;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.LivingEntity;

/**
 * @author ConeTin
 * @since 15 мар. 2024 г.
 */

@Getter
@AllArgsConstructor
public class EventDamage extends Event {
	
	private final LivingEntity target;
	
}