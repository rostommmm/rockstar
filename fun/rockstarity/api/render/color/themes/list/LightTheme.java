package fun.rockstarity.api.render.color.themes.list;

import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Theme;

/**
 * @author ConeTin
 * @since 12 РјР°СЂ. 2024 Рі.
 */

public class LightTheme extends Theme {
	
	public LightTheme() {
		super("РЎРІРµС‚Р»Р°СЏ",
				new FixColor(246, 249, 255), // background
				new FixColor(235, 241, 255), // settingsBg
				new FixColor(230, 236, 246), // separator
				new FixColor(201, 207, 217), // rect
				FixColor.BLACK, // black :D
				new FixColor(104, 104, 104)); // module
	}

}
