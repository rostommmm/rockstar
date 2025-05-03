package fun.rockstarity.api.secure.nativeapi;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.render.Converter;
import fun.rockstarity.api.helpers.secure.KeyGeneration;
import fun.rockstarity.api.helpers.secure.Web;
import fun.rockstarity.api.secure.users.User;
import lombok.experimental.UtilityClass;
import net.minecraft.util.ResourceLocation;
import ru.kotopushka.antiautistleak.obfuscator.includes.annotations.auth.ReleaseNativeAuth;
import ru.kotopushka.antiautistleak.obfuscator.includes.annotations.compile.ReleaseCompileToNativeCalls;

/**
 * @author ConeTin
 * @since 7 июн. 2024 г.
 */

@ReleaseNativeAuth
@ReleaseCompileToNativeCalls
@UtilityClass
public class NativeHelper {

	public String readShader(String name) {
		/*
		HashMap<String, String> data = new HashMap<>();
		data.put("shader", KeyGeneration.encrypt(name));
		return KeyGeneration.decrypt(Web.protectedPostRequest(data, "https://rockstar.moscow/api/v1/files/premium/shaders/give_shader.php").trim());
		*/

//		StringBuilder stringBuilder = new StringBuilder();
//		String resourcePath = "/assets/minecraft/rockstar/api/v1/files/premium/shaders/list/" + name;
//		try (InputStream inputStream = NativeHelper.class.getResourceAsStream(resourcePath)) {
//			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
//			String line;
//			while ((line = bufferedReader.readLine()) != null)
//				stringBuilder.append(line).append('\n');
//		} catch (Exception e) {
//		}
//
//		return stringBuilder.toString();

		return Web.read("/assets/minecraft/rockstar/api/v1/files/premium/shaders/list/" + name);
	}

	public ResourceLocation getImageResource(String name) {
		return name.startsWith("http") ? Converter.getResourceLocationFromUrl(name) : Converter.getResourceLocation("/assets/minecraft/rockstar/api/v1/files/premium/" + name);
	}

	public String getFontResource(String name) {
		return "/assets/minecraft/rockstar/api/v1/files/premium/fonts/" + name + ".ttf";
	}

	public String getSoundResource(String name) {
		return "/assets/minecraft/rockstar/api/v1/files/premium/sounds/" + name + ".wav";
	}

	public String getThemes() {
		return Web.read("/assets/minecraft/rockstar/api/v1/premium/themes/style_list.json");
	}
}