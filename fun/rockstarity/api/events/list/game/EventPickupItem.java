package fun.rockstarity.api.events.list.game;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;

@AllArgsConstructor @Getter
public class EventPickupItem extends Event {
	
	private final ItemStack itemStack;
	private final LivingEntity livingEntity;
	private final ItemEntity itemEntity;
}
