package fun.rockstarity.api.scripts.wrappers.factory;

import fun.rockstarity.api.scripts.wrappers.base.PotionBase;
import net.minecraft.potion.Effect;

/**
 * @author ConeTin
 * @since 11 СЏРЅРІ. 2025вЂЇРі.
 */

public class PotionFactory {

	public static PotionBase create(Effect effect, String name, String time) {
		return new PotionBase(effect, name, time);
	}
	
}
