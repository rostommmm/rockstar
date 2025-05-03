package fun.rockstarity.api.scripts.wrappers.factory;

import fun.rockstarity.api.scripts.wrappers.base.ElementBase;
import fun.rockstarity.api.scripts.wrappers.base.ModuleBase;
import fun.rockstarity.api.scripts.wrappers.settings.PickerBase;

/**
 * @author ConeTin
 * @since 11 СЏРЅРІ. 2025вЂЇРі.
 */

public class PickerFactory {

	public static PickerBase create(ElementBase parent, String name) {
		return new PickerBase(parent, name);
	}
	
}
