/**
 * 
 */
package fun.rockstarity.api.events.list.player;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.LivingEntity;

/**
 * @author Malecharik
 * @since 14 Mar 2024 21:26:23
 */

@Getter
@AllArgsConstructor
public class EventAttack extends Event {
	
	private final LivingEntity target;
	
}
