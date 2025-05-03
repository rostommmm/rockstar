package fun.rockstarity.api.scripts.wrappers.factory;

import fun.rockstarity.api.scripts.wrappers.base.ElementBase;
import fun.rockstarity.api.scripts.wrappers.base.ModuleBase;
import fun.rockstarity.api.scripts.wrappers.settings.InputBase;
import fun.rockstarity.api.scripts.wrappers.settings.SelectBase;

/**
 * @author ConeTin
 * @since 11 СЏРЅРІ. 2025вЂЇРі.
 */

public class InputFactory {

	public static InputBase create(ElementBase parent, String name) {
		return new InputBase(parent, name);
	}
	
}
