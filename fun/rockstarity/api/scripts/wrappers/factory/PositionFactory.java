package fun.rockstarity.api.scripts.wrappers.factory;

import fun.rockstarity.api.scripts.wrappers.base.ElementBase;
import fun.rockstarity.api.scripts.wrappers.settings.PositionBase;

public class PositionFactory {
	
	public static PositionBase create(ElementBase parent, String name) {
		return new PositionBase(parent, name);
	}
}
