package fun.rockstarity.api.binds;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ConeTin
 * @since 2 дек. 2023 г.
 */

@Getter
@AllArgsConstructor
public enum BindType {
	HOLD("Hold"),
	TOGGLE("Toggle");
	
	private final String name;
	
	public static BindType get(String name) {
		BindType mode = TOGGLE;
		for (BindType mod : values()) {
			if (mod.getName().toLowerCase().equals(name.toLowerCase())) mode = mod;
		}
		return mode;
	}
}
