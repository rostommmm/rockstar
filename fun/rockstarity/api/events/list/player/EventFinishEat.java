package fun.rockstarity.api.events.list.player;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;

/**
 * @author ConeTin
 * @since 6 дек. 2023 г.
 * @in ItemStack
 */

@Getter
@AllArgsConstructor
public class EventFinishEat extends Event {

	private final LivingEntity entity;
	private final Item item;
	
}
