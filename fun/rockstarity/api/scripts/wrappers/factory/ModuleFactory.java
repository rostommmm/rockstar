package fun.rockstarity.api.scripts.wrappers.factory;

import fun.rockstarity.api.scripts.wrappers.base.ModuleBase;

/**
 * @author ConeTin
 * @since 11 СЏРЅРІ. 2025вЂЇРі.
 */

public class ModuleFactory {
	
	public static ModuleBase create(String name, String desc) {
		return new ModuleBase(name, desc);
	}
	
}
