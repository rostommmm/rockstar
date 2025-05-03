package fun.rockstarity.api.scripts.wrappers.factory;

import fun.rockstarity.api.scripts.wrappers.base.ElementBase;
import fun.rockstarity.api.scripts.wrappers.base.ModuleBase;
import fun.rockstarity.api.scripts.wrappers.settings.ModeBase;
import fun.rockstarity.api.scripts.wrappers.settings.SelectBase;

/**
 * @author ConeTin
 * @since 11 СЏРЅРІ. 2025вЂЇРі.
 */

public class SelectFactory {

	public static SelectBase create(ElementBase parent, String name) {
		return new SelectBase(parent, name);
	}
	
}
