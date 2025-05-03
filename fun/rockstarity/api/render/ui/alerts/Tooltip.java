package fun.rockstarity.api.render.ui.alerts;

import java.util.HashMap;
import java.util.HashSet;

import fun.rockstarity.api.helpers.game.Chat;
import lombok.experimental.UtilityClass;

/**
 * @author ConeTin
 * @since 14 июн. 2024 г.
 */

@UtilityClass
public class Tooltip {
	
	private final HashSet<String> usedTooltips = new HashSet<>();

	public String create(String text) {
		if (!usedTooltips.contains(text)) {
			usedTooltips.add(text);
			return text;
		}
		
		return "";
	}
	
}
