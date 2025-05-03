package fun.rockstarity.api.scripts.wrappers.factory;

import fun.rockstarity.api.scripts.wrappers.base.AnimBase;
import fun.rockstarity.api.scripts.wrappers.base.SoundBase;

/**
 * @author ConeTin
 * @since 11 СЏРЅРІ. 2025вЂЇРі.
 */

public class SoundFactory {

	public static SoundBase create(String url) {
		return new SoundBase(url);
	}
	
}
