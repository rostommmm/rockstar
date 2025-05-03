package fun.rockstarity.api.render.ui.clickgui.esp.elmts.cooldowns;

import fun.rockstarity.api.helpers.math.TimerUtility;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.item.Item;

/**
 * @author ConeTin
 * @since 19 Р°РїСЂ. 2025вЂЇРі.
 */

@Getter
@AllArgsConstructor
public class ItemData {

	private final TimerUtility creation = new TimerUtility();
	private final long cooldown;
	private final Item item;
	
}
