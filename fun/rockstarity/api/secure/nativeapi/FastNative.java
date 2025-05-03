package fun.rockstarity.api.secure.nativeapi;

import java.util.HashMap;

import lombok.experimental.UtilityClass;
import net.minecraft.util.ResourceLocation;

/**
 * @author ConeTin
 * @since 7 июн. 2024 г.
 */

@UtilityClass
public class FastNative {

	private HashMap<String, ResourceLocation> imageData = new HashMap<>();

	public ResourceLocation getImageResource(String name) {
		if (imageData.containsKey(name)) {
			return imageData.get(name);
		} else {
			try {
				ResourceLocation resource = NativeHelper.getImageResource(name);
				imageData.put(name, resource);
				return resource;
			} catch (Exception e) {
				return new ResourceLocation("");
			}
		}
	}
		
}
