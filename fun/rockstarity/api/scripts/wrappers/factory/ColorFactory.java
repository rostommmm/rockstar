package fun.rockstarity.api.scripts.wrappers.factory;

import fun.rockstarity.api.scripts.wrappers.base.ColorBase;

/**
 * @author ConeTin
 * @since 11 СЏРЅРІ. 2025вЂЇРі.
 */

public class ColorFactory {

	public static ColorBase create(double red, double green, double blue, double alpha) {
		return new ColorBase(red, green, blue, alpha);
	}
	
	public static ColorBase create(double red, double green, double blue) {
		return new ColorBase(red, green, blue);
	}
	
	public static ColorBase create(int hex) {
		return new ColorBase(hex);
	}
	
}
