package fun.rockstarity.api.scripts.wrappers.factory;

import fun.rockstarity.api.scripts.wrappers.base.VectorBase;

/**
 * @author ConeTin
 * @since 11 СЏРЅРІ. 2025вЂЇРі.
 */

public class VectorFactory {
	
	public static VectorBase create(double x, double y, double z) {
		return new VectorBase(x, y, z);
	}
	
}
