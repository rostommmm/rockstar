package fun.rockstarity.api.scripts.wrappers.factory;

import fun.rockstarity.api.scripts.wrappers.base.ElementBase;
import fun.rockstarity.api.scripts.wrappers.base.ModuleBase;
import fun.rockstarity.api.scripts.wrappers.settings.SliderBase;

/**
 * @author ConeTin
 * @since 11 СЏРЅРІ. 2025вЂЇРі.
 */

public class SliderFactory {

	public static SliderBase create(ElementBase parent, String name) {
		return new SliderBase(parent, name);
	}
	
}
