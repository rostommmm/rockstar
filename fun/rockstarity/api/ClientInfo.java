package fun.rockstarity.api;

import lombok.AllArgsConstructor;

/**
 * @author ConeTin
 * @since 2 апр. 2024 г.
 */

@AllArgsConstructor
public enum ClientInfo {

	NAME("Rockstar"),
	TYPE("Premium"),
	ACCESS("Alpha"),
	VERSION("1.0");
	
	private final String str;
	
	@Override
	public String toString() {
		return this.str;
	}
}
