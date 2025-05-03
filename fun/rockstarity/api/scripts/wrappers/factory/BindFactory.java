package fun.rockstarity.api.scripts.wrappers.factory;

import fun.rockstarity.api.scripts.wrappers.base.ElementBase;
import fun.rockstarity.api.scripts.wrappers.base.ModuleBase;
import fun.rockstarity.api.scripts.wrappers.settings.BindBase;
import fun.rockstarity.api.scripts.wrappers.settings.InputBase;

/**
 * @author ConeTin
 * @since 11 СЏРЅРІ. 2025вЂЇРі.
 */

public class BindFactory {

	public static BindBase create(ElementBase parent, String name) {
		return new BindBase(parent, name);
	}
	
}
