package fun.rockstarity.api.events.list.game;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.Item;

/**
 * @author ConeTin
 * @since 6 дек. 2023 г.
 * @in Minecraft
 */

@AllArgsConstructor @Getter @Setter
public class EventSetCooldown extends Event {
	
	private Item item;
	private int cooldown;
	
}
