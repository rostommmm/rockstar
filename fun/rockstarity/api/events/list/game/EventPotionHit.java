package fun.rockstarity.api.events.list.game;

import java.util.List;

import fun.rockstarity.api.events.Event;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;

@AllArgsConstructor @Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventPotionHit extends Event {
	
	PotionEntity potion;
	List<EffectInstance> effects;
	Entity target;
	double duration;
	
}
