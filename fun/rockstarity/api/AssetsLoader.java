package fun.rockstarity.api;


import fun.rockstarity.api.helpers.render.Converter;
import fun.rockstarity.api.helpers.render.Gifs;
import fun.rockstarity.api.helpers.render.gif.GifRender;
import fun.rockstarity.api.modules.settings.list.Select.Element;
import fun.rockstarity.api.render.globals.emotions.instance.EmotionType;
import fun.rockstarity.api.render.shaders.list.ClientBloom;
import fun.rockstarity.api.render.shaders.list.ClientOutline;
import fun.rockstarity.api.render.shaders.list.Glow;
import fun.rockstarity.api.render.shaders.list.Outline2;
import fun.rockstarity.api.render.ui.mainmenu.loading.LoadingScreen;
import fun.rockstarity.api.render.ui.mainmenu.loading.LoadingStage;
import fun.rockstarity.api.secure.nativeapi.NativeHelper;
import fun.rockstarity.client.modules.render.Particles;
import lombok.experimental.UtilityClass;
import net.minecraft.util.ResourceLocation;
import ru.kotopushka.antiautistleak.obfuscator.includes.annotations.compile.ReleaseCompileToNativeCalls;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;
import ru.kotopushka.j2c.sdk.annotations.VMProtect;
import ru.kotopushka.j2c.sdk.enums.VMProtectType;

/**
 * @author ConeTin
 * @since 4 июн. 2024 г.
 */

@ReleaseCompileToNativeCalls
@UtilityClass
public class AssetsLoader implements IAccess {
	@NativeInclude
	public void load() {
		//ThreadManager.run(() -> {
		LoadingStage.FONTS.activate();
		bold(17);
		bold(15);
		bold(14);
		bold(12);
		bold(11);
		bold(13);
		bold(10);
		bold(16);
		bold(20);
		bold(24);
		bold(18);
		semibold(16);
		semibold(14);
		semibold(15);
		semibold(13);
		semibold(12);
		semibold(10);
		semibold(17);

		LoadingStage.IMAGES.activate();
		image("icons/menu/setting.png");
		image("masks/models/rabbit.png");
		image("icons/info.png");
		image("icons/xmark.png");
		image("icons/copy.png");
		image("icons/yes.png");
		image("masks/arrow.png");
		image("masks/circle.png");
		image("icons/hud/bind.png");
		image("icons/hud/coords.png");
		image("icons/hud/logo.png");
		image("icons/hud/server.png");
		image("icons/hud/speed.png");
		image("icons/hud/mouse/center.png");
		image("icons/hud/mouse/left.png");
		image("icons/hud/mouse/right.png");
		image("icons/hud/mouse/tail.png");
		image("icons/hud/finger/line1.png");
		image("icons/hud/finger/line2.png");
		image("icons/hud/finger/line3.png");
		image("icons/hud/finger/line4.png");
		image("icons/hud/finger/line5.png");
		image("icons/hud/finger/line6.png");
		image("icons/hud/finger/line7.png");
		image("masks/wheel_element.png");
		image("icons/mainmenu/alt/search.png");
		image("icons/mainmenu/alt/filter.png");
		image("icons/mainmenu/alt/magic.png");
		image("icons/mainmenu/alt/star.png");
		image("icons/mainmenu/alt/ban.png");
		image("icons/mainmenu/alt/trash.png");
		image("icons/mainmenu/alt/small-star.png");
		image("icons/mainmenu/alt/stroke-star.png");
		image("masks/energy.png");
		image("masks/chain.png");
		image("masks/glow.png");
		image("icons/hud/potions.png");
		image("icons/copy.png");
		image("icons/yes.png");
		image("masks/particles/Glow.png");

		Particles particles = rock.getModules().get(Particles.class);
		for (Element type : particles.getElementSettings().get(particles.getTotem()).select.getElements()) {
			image("masks/particles/" + particles.getElementSettings().get(particles.getTotem()).select.getElements().indexOf(type) + ".png");
		}

		for (int i = 0; i < 8; i++) {
			if (i < EmotionType.values().length)
				image("masks/emotions/" + EmotionType.values()[i].getName() + ".png");
		}

		LoadingStage.IMAGES.getReadyAnim().setForward(true);
		LoadingStage.IMAGES.setStage("Готово!");
		LoadingScreen.FINISH = true;
		//});
	}

	@VMProtect(type = VMProtectType.ULTRA)
	@NativeInclude
	private void image(String path) {
		LoadingStage.stage("Картинка " + path.replace("/", " -> "));
		Converter.getResourceLocation("https://rockstar.moscow/api/v1/files/premium/" + path);
	}

	@VMProtect(type = VMProtectType.ULTRA)
	@NativeInclude
	private void bold(int size) {
		LoadingStage.stage("Шрифт " + size + " с начертанием Bold");
		bold.get(size);
	}

	@VMProtect(type = VMProtectType.ULTRA)
	@NativeInclude
	private void semibold(int size) {
		LoadingStage.stage("Шрифт " + size + " с начертанием Semibold");
		semibold.get(size);
	}

	@VMProtect(type = VMProtectType.ULTRA)
	@NativeInclude
	public void loadGifs() {
		if (Gifs.idontknow == null) {
			Gifs.idontknow = loadGif("xz");
			Gifs.sleep = loadGif("sleep");
			Gifs.loading = loadGif("wait");
			Gifs.clean = new GifRender(Converter.getInputStream("https://rockstar.moscow/api/v1/files/premium/" + "gifs/clean.gif"));
			if (rock.getUser().isGif()) {
				Gifs.avatar = new GifRender(Converter.getInputStream(rock.getUser().getAvatar()));
			}
		}
	}

	@VMProtect(type = VMProtectType.ULTRA)
	@NativeInclude
	private GifRender loadGif(String name) {
		String word = rock.isNewYear() ? "new" : "";
		return new GifRender(Converter.getInputStream("https://rockstar.moscow/api/v1/files/premium/" + "gifs/" + word + name + ".gif"));
	}
}