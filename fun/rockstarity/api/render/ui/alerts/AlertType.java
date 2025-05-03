package fun.rockstarity.api.render.ui.alerts;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ConeTin
 * @since 9 дек. 2023 г.
 */

@Getter
@AllArgsConstructor
public enum AlertType {
	
	SUCCESS("success"),
	WAIT("wait"),
	ERROR("error"),
	INFO("info");
	
	private final String name;
	
	public static AlertType get(String name) {
		for (AlertType type : values()) {
			if (type.getName().equalsIgnoreCase(name))
				return type;
		}
		return AlertType.INFO;
	}

}
