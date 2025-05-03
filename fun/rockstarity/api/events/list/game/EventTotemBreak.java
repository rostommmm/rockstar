package fun.rockstarity.api.events.list.game;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

/**
 * @author ConeTin
 * @since 6 дек. 2023 г.
 * @in Minecraft
 */

@AllArgsConstructor @Getter
public class EventTotemBreak extends Event {
	
	private final LivingEntity entity;
	private final ItemStack totemItem;
	
}
