package fun.rockstarity.api.events.list.game;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.TridentEntity;

/**
 * @author ConeTin
 * @since 31 июл. 2024 г.
 */

@Getter
@AllArgsConstructor
public class EventTridentHitted extends Event {

	private final TridentEntity trident;
	private final LivingEntity hitted;
	
}
