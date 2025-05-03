package fun.rockstarity.api.autobuy.logic.items;

import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.item.Item;

/**
 * @author ConeTin
 * @since 25 апр. 2024 г.
 */


@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MinecraftItem {

	final Item item;
	final Animation selectAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	
	public String getName() {
		return this.item.getName().getString();
	}
	
}
