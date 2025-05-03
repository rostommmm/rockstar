package fun.rockstarity.api.scripts.wrappers.factory;

import fun.rockstarity.api.scripts.wrappers.base.ElementBase;
import fun.rockstarity.api.scripts.wrappers.base.ModuleBase;
import fun.rockstarity.api.scripts.wrappers.settings.ModeBase;
import fun.rockstarity.api.scripts.wrappers.settings.PickerBase;

/**
 * @author ConeTin
 * @since 11 СЏРЅРІ. 2025вЂЇРі.
 */

public class ModeFactory {

	public static ModeBase create(ElementBase parent, String name) {
		return new ModeBase(parent, name);
	}
	
}
