package fun.rockstarity.api.scripts.wrappers.factory;

import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.scripts.wrappers.base.ElementBase;
import fun.rockstarity.api.scripts.wrappers.base.ModuleBase;
import fun.rockstarity.api.scripts.wrappers.settings.CheckBoxBase;

/**
 * @author ConeTin
 * @since 11 СЏРЅРІ. 2025вЂЇРі.
 */

public class CheckBoxFactory {
	
	public static CheckBoxBase create(ElementBase parent, String name) {
		return new CheckBoxBase(parent, name);
	}

}
