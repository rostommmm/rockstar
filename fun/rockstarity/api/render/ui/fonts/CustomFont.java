package fun.rockstarity.api.render.ui.fonts;

import java.util.HashMap;

import lombok.AllArgsConstructor;

/**
 * @author ConeTin
 * @since 4 дек. 2023 г.
 */

@AllArgsConstructor
public class CustomFont extends HashMap<Integer, FontSize> {
	
	private final String name;
	
	public FontSize get(int size) {
		if (this.containsKey(size)) {
			return super.get(size);
		} else {
			FontSize font = new FontSize(name, size);
			this.put(size, font);
			return new FontSize(name, size);
		}
	}
	
	public boolean loaded(int size) {
		return this.containsKey(size);
	}
	
}
