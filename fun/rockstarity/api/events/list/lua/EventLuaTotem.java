package fun.rockstarity.api.events.list.lua;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.scripts.wrappers.base.LivingEntityBase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.item.ItemStack;

@AllArgsConstructor @Getter
public class EventLuaTotem extends Event {
	
	private final LivingEntityBase entity;
	private final ItemStack item;
	
	public LivingEntityBase target() {
		return entity;
	}	
	
	public LivingEntityBase entity() {
		return entity;
	}
	
	public String nick() {
		return entity.getEntity().getName().getString();
	}
	
    public String totemName() {
        return item.getDisplayName().getString();
    }
}