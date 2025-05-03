package fun.rockstarity.api.connection.globals;

import java.util.HashMap;

import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.render.color.FixColor;
import lombok.experimental.UtilityClass;

/**
 * @author ConeTin
 * @since 15 РёСЋРЅ. 2024 Рі.
 */

@UtilityClass
public class GlobalsColors {
	
	private final HashMap<String, FixColor> colors = new HashMap<>();
	
	static {
		colors.put("rockstar", FixColor.CYAN);
		colors.put("delight", FixColor.CYAN);
		colors.put("minced", FixColor.BLACK);
		colors.put("carbonara", FixColor.YELLOW);
		colors.put("excellent", FixColor.CYAN);
	}
	
	public FixColor getColor(String client) {
		return colors.containsKey(client) ? colors.get(client) : FixColor.WHITE;
	}
	
}
