package fun.rockstarity.api.render.color.themes.list;

import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Theme;

/**
 * @author ConeTin
 * @since 12 РјР°СЂ. 2024 Рі.
 */

public class DarkTheme extends Theme {
	
	public DarkTheme() {
		super("РўРµРјРЅР°СЏ",
				new FixColor(14, 17, 21), // background
				new FixColor(19, 21, 27), // settingsBg
				new FixColor(25, 29, 36), // separator
				new FixColor(68, 65, 77), // rect
				new FixColor(255, 255, 255), // black :D
				new FixColor(184, 184, 184)); // module
	}

}
