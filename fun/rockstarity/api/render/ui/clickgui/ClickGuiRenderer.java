package fun.rockstarity.api.render.ui.clickgui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.math.Hover;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.render.Converter;
import fun.rockstarity.api.helpers.render.Gifs;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.helpers.render.Stencil;
import fun.rockstarity.api.helpers.system.TextUtility;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.Setting;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.render.color.themes.Theme;
import fun.rockstarity.api.render.cursor.CursorType;
import fun.rockstarity.api.render.cursor.CursorUtility;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.fonts.FontSize;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.api.render.ui.widgets.InputWidget;
import fun.rockstarity.api.render.ui.widgets.Window;
import fun.rockstarity.api.scripts.Script;
import fun.rockstarity.api.secure.Debugger;
import fun.rockstarity.client.modules.render.ClickGui;
import fun.rockstarity.client.modules.render.ESP;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * @author ConeTin
 * @since 7 дек. 2023 г.
 */

public class ClickGuiRenderer implements IAccess {

	FixColor bgColor, settingsBg, separatorColor, moduleColor, text, black, white;
	public static Animation opening = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300),
			tooltip = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300),
			rotationESP = new Animation().setEasing(Easing.EASE_OUT_CIRC).setSpeed(1000),
			dragAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300),
			changeCurrentTail = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(500),
			changeCurrent = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300),
			swapAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300),
			loadedAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(600),
			upSideAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300),
			separatorAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300),
			moveAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300),
			changeCategoryAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(150),
			hoverCloseAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300),
			hoverSaveConfigAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300),
			hoverOpenFolderAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300),
			hoverSearchAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300),
			searchAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300),
			searchFocusedAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300),
			hoverBindAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300),
			themesAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300),
			shadowAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300),
			avatarHoverAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300),
			openedAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(400);
	@Getter
	@Setter
	private Vector3d closePos = Vector3d.ZERO;
	@Getter
	private float yaw, pitch;
	private ClickGuiWindow window;
	@Getter
	private float loadedAnimX = 0;
	@Getter
	private static final ArrayList<SettingRect> settings = new ArrayList<>();
	@Getter
	@Setter
	private static boolean loaded, apply;
	private String loadingAction;
	private final Comparator<Object> SORT_METHOD = Comparator.comparing(m -> {
		Module module = (Module) m;
		return module.getInfo().name();
	}).reversed();
	@Getter
	private static final GlyphType[] glyphes = new GlyphType[Category.values().length];
	private boolean stencil = true;

	@Setter
	private SettingRect hovered;

	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		opening.setSpeed(300);
		ClickGui clickGui = rock.getModules().get(ClickGui.class);
		clickGui.getDarkness().setForward(clickGui.getShadow().get());

		if (!clickGui.getDarkness().finished(false))
			Round.draw(matrixStack, new Rect(0, 0, sr.getScaledWidth(), sr.getScaledHeight()), 0,
					FixColor.BLACK.alpha(0.5f * clickGui.getDarkness().get() * opening.get()));

		this.window = rock.getClickGui().getWindow();

		// Цвета

		dragAnim.setForward(window.isDrag());

		// Перенос гуишки при закрытии
		float modif = 4;

		if (opening.isForward() && mc.player != null) {
			yaw = mc.player.rotationYaw * modif;
			pitch = mc.player.rotationPitch * modif;
		}

		GL11.glPushMatrix();
		GL11.glTranslatef(yaw - mc.player.rotationYaw * modif, pitch - mc.player.rotationPitch * modif, 0);
		// GL11.glRotated(Math.abs(yaw - mc.player.rotationYaw * modif), 1, 0, 0);
		// GL11.glRotated(Math.abs(pitch - mc.player.rotationPitch * modif), 0, 1, 0);

		// Анимация на размер гуишки (открытие/закрытие)
		Render.scale(rock.getClickGui().getWindow().getX() + window.getWidth() / 2,
				window.getY() + window.getHeight() / 2,
				0.5f + (float) opening.get() * 0.5f /*- 0.03f * dragAnim.get()*/);

		// Отображение самого окна
		this.renderWindow(matrixStack, mouseX, mouseY, partialTicks);

		Render.end();

		GL11.glPopMatrix();

		if (hovered != null && !this.hovered.getParent().isHide() && this.hovered.getParent().getDesc() != null
				&& !this.hovered.getParent().getDesc().isEmpty()
				&& (this.hovered.getY() < this.window.getY() + this.window.getHeight()
						|| !(hovered.getParent().getParent() instanceof Module))) {
			FontSize font = bold.get(14);
			String[] words = this.hovered.getParent().getDesc().split(" ");

			float xOff = 0;
			float yOff = 10;
			float maxWidth = 110;
			float height = 10;
			float width = 10;
			float indent = 10;

			for (String word : words) {
				xOff += font.getWidth(word + " ");
				width = Math.max(width, xOff);
				if (xOff > maxWidth && Arrays.asList(words).indexOf(word) != words.length - 1) {
					yOff += 8;
					xOff = 0;
				}
			}

			height = yOff;
			xOff = 0;
			yOff = 0;

			Render.drawTriangle(this.hovered.getX() + this.hovered.getWidth() + indent + xOff - 6.5f,
					this.hovered.getY() + this.hovered.getHeight() / 2 + yOff - height / 2 + height / 2, 3, 90,
					bgColor.move(moduleColor, 0.15f).alpha(this.hovered.getSlowHover().get() * 0.9f).getRGB());
			Round.draw(matrixStack, new Rect(this.hovered.getX() + this.hovered.getWidth() + indent + xOff - 4,
					this.hovered.getY() + this.hovered.getHeight() / 2 + yOff - height / 2 - 2, width + 5, height + 4),
					5, bgColor.move(moduleColor, 0.15f).alpha(this.hovered.getSlowHover().get() * 0.9f));

			for (String word : words) {
				font.draw(matrixStack, word, this.hovered.getX() + this.hovered.getWidth() + indent + xOff,
						this.hovered.getY() + this.hovered.getHeight() / 2 + yOff - height / 2,
						text.alpha(this.hovered.getSlowHover().get()));

				xOff += font.getWidth(word + " ");
				if (xOff > maxWidth) {
					yOff += 8;
					xOff = 0;
				}
			}
		}
	}

	public void renderWindow(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		if (window == null)
			return;

		bgColor = rock.getThemes().getFirstColor().alpha(opening.get());
		settingsBg = rock.getThemes().getSecondColor().alpha(opening.get());
		separatorColor = rock.getThemes().getThirdColor().alpha(opening.get() * separatorAnim.get());
		moduleColor = rock.getThemes().getTextSecondColor().alpha(opening.get() * separatorAnim.get());
		text = rock.getThemes().getTextFirstColor().alpha(opening.get());
		black = FixColor.BLACK.alpha(opening.get());
		white = FixColor.WHITE.alpha(opening.get());

		// Главный фон гуишки
		Round.draw(matrixStack, window, 8, bgColor);

		/*
		 * Rockstar Vision Pro Round.draw(matrixStack, new Rect(window.getX() + 165,
		 * window.getY() + window.getHeight() + 10, window.getWidth() - 300, 10), 5,
		 * FixColor.WHITE.alpha(0.7f * opening.get())); Round.draw(matrixStack, new
		 * Rect(window.getX() + 150, window.getY() + window.getHeight() + 10, 10, 10),
		 * 5, FixColor.WHITE.alpha(0.7f * opening.get()));
		 */

		if (rock.isNewYear()) {
			Stencil.init();
			Round.draw(matrixStack, window, 8, bgColor);
			Stencil.read(1);
			// Сверху слева
			Render.image("events/snow.png", window.getX() - 20, window.getY() - 15, 47, 47, bgColor.move(text, 0.05f));
			Render.image("events/snow.png", window.getX() + 23, window.getY() - 13, 32, 32, bgColor.move(text, 0.05f));

			// Справа сверху
			Render.image("events/snow.png", window.getX() + window.getWidth() + 20 - 47, window.getY() - 15, 47, 47,
					bgColor.move(text, 0.05f));
			Render.image("events/snow.png", window.getX() + window.getWidth() - 23 - 32, window.getY() - 13, 32, 32,
					bgColor.move(text, 0.05f));

			// Снизу слева
			Render.image("events/snow.png", window.getX() - 20, window.getY() + window.getHeight() + 15 - 47, 47, 47,
					bgColor.move(text, 0.05f));
			Render.image("events/snow.png", window.getX() + 23, window.getY() + window.getHeight() + 13 - 32, 32, 32,
					bgColor.move(text, 0.05f));
			Stencil.finish();
		}

		swapAnim.setForward(loaded);
		loadedAnim.setForward(swapAnim.finished());
		upSideAnim.setForward(loadedAnim.finished());
		separatorAnim.setForward(upSideAnim.finished());
		moveAnim.setForward(separatorAnim.finished());

		if (!swapAnim.finished()) {
			stencil = true;
		}

		int i1 = 0;
		boolean canStecil = stencil;
		for (Category cat : Category.values()) {
			if (cat.getIndex() == 0) {
				cat.getMoveAnim().setForward(moveAnim.finished());
			} else {
				cat.getMoveAnim().setForward(Category.values()[i1 - 1].getMoveAnim().finished());
			}

			if (canStecil && !cat.getMoveAnim().finished()) {
				canStecil = false;
			}

			cat.setIndex(i1);
			i1++;
		}

		if (canStecil)
			stencil = false;

		loadedAnimX = (float) (window.getX() + window.getWidth() - window.getWidth() * loadedAnim.get());

		if (!apply) {
			new Thread(() -> {
				loadingAction = "Загружаем функции";

				settings.clear();

				for (Category cat : Category.values()) {
					int i = cat.getIndex();
					glyphes[i] = new GlyphType();

					List<Module> modules = new ArrayList<>();

					rock.getModules().values().forEach(mod -> modules.add(mod));
					for (Script script : rock.getScriptHandler().getEnabledScripts()) {
						script.getScriptModules().forEach(mod -> modules.add(mod));
					}

					modules.sort(SORT_METHOD);

					for (Module module : modules) {
						if (module.getInfo().type() == cat) {
							if (!glyphes[i].containsKey(module.getInfo().name().charAt(0)))
								glyphes[i].put(module.getInfo().name().charAt(0), new ArrayList<>());

							glyphes[i].get(module.getInfo().name().charAt(0)).add(module);

							for (Setting setting : module.getSettings())
								settings.add(new SettingRect(setting));
						}
					}
				}

				loadingAction = "Загружаем картиночки";
				for (Category type : Category.values()) {
					Converter.getResourceLocation("https://rockstar.moscow/api/v1/files/premium/" + "category/"
							+ type.getName().toLowerCase() + ".png");
				}
				Converter
						.getResourceLocation("https://rockstar.moscow/api/v1/files/premium/" + "category/rockstar.png");
				Converter.getResourceLocation("https://rockstar.moscow/api/v1/files/premium/" + "icons/bind.png");
				Converter.getResourceLocation("https://rockstar.moscow/api/v1/files/premium/" + "icons/close.png");
				Converter.getResourceLocation("https://rockstar.moscow/api/v1/files/premium/" + "category/search.png");
				Converter.getResourceLocation("https://rockstar.moscow/api/v1/files/premium/" + "icons/checkmark.png");
				Converter.getResourceLocation(rock.getUser().getAvatar());
				loadingAction = "Читекс загружен :D";
				try {
					Thread.sleep(1300);
				} catch (InterruptedException e) {
					Debugger.print(e);
				}

				loaded = true;
			}).start();
			apply = true;
		}

		if (stencil) {
			Stencil.init();
			Round.draw(matrixStack, window, 8, bgColor);
			Stencil.read(1);
		} else {
			if (mc.currentScreen == rock.getClickGui()) {
				GL11.glEnable(GL11.GL_SCISSOR_TEST);
				Render.scissor(window.getX() + yaw - mc.player.rotationYaw * 4,
						window.getY() + pitch - mc.player.rotationPitch * 4, window.getWidth(), window.getHeight());
			}
		}

		if (!loaded || !swapAnim.finished()) {
			float anim = -window.getWidth() * swapAnim.get();

			if (Gifs.loading != null)
				Gifs.loading.draw(matrixStack, window.getX() + 194 + anim, window.getY() + 95, 100, 100, opening.get(),
						20);
			String wait = "Пожалуйста, подождите";

			if (bold.loaded(24))
				bold.get(24).draw(matrixStack, wait,
						window.getX() + window.getWidth() / 2 - bold.get(24).getWidth(wait) / 2 + anim,
						window.getY() + 192, text);

			if (bold.loaded(16))
				bold.get(16).draw(matrixStack, loadingAction,
						window.getX() + window.getWidth() / 2 - bold.get(16).getWidth(loadingAction) / 2 + anim,
						window.getY() + 207, new FixColor(104, 104, 104).alpha(opening.get()));

			if (stencil) {
				Stencil.finish();
			} else {
				if (mc.currentScreen == rock.getClickGui())
					GL11.glDisable(GL11.GL_SCISSOR_TEST);
			}
			return;
		}

		window.setCanDrag(true);

		// Отображение панели слева
		this.renderPanel(matrixStack, mouseX, mouseY, partialTicks);

		// Отображение списка модулей
		this.renderModules(matrixStack, mouseX, mouseY, partialTicks);

		this.renderOpened(matrixStack, mouseX, mouseY, partialTicks);

		// Разделение между модулями и настройками
		Round.draw(matrixStack,
				window.x(window.getX() + 155).width(1).height(window.getHeight() - 288 * this.themesAnim.get()), 0,
				separatorColor);

		this.renderTab(matrixStack, mouseX, mouseY, partialTicks);

		if (stencil) {
			Stencil.finish();
		} else {
			if (mc.currentScreen == rock.getClickGui())
				GL11.glDisable(GL11.GL_SCISSOR_TEST);
		}

		if (!tooltip.finished(false)) {
			List<String> lines = TextUtility.wrapText("Тут может быть то, что вам нужно", semibold.get(16), 100);
			float yOff1 = 0;

			Round.draw(matrixStack, new Rect(window.getX() + 98, window.getY() + 47.5f + yOff1, 20, 1), 5,
					moduleColor.alpha(tooltip.get() * opening.get()));

			for (String line : lines) {
				semibold.get(16).draw(matrixStack, line, window.getX() + 118, window.getY() + 41 + yOff1,
						moduleColor.alpha(tooltip.get() * opening.get()));
				yOff1 += 9;
			}
		}
		// Round.draw(matrixStack, new Rect(mouseX - 5, mouseY - 5, 10, 10), 5,
		// FixColor.WHITE);
	}

	private void renderPanel(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		float anim1 = 30 * Category.values()[0].getMoveAnim().get() - 30;

		// Рендер иконки рокстара
		Render.image("category/rockstar.png", window.getX() + 9 + 30 * moveAnim.get() - 30 + 3.5f - 1.5f,
				window.getY() + 9.5f + 3.5f - 1.5f, 16, 16, text);

		// Рендер категорий
		float yOff = 0;
		for (Category type : Category.values()) {
			float anim = 30 * type.getMoveAnim().get() - 30;

			if (Hover.isHovered(window.getX() + 8 + anim, window.getY() + 49 + yOff, 21, 21, mouseX, mouseY)) {
				window.setCanDrag(false);
			}

			type.getHover().setForward(
					Hover.isHovered(window.getX() + 8 + anim, window.getY() + 49 + yOff, 21, 21, mouseX, mouseY));
			type.getOpen().setForward(type == window.getCurrent());

			Round.draw(matrixStack, new Rect(window.getX() + 8 + anim, window.getY() + 49 + yOff, 21, 21), 4,
					separatorColor);
			Round.draw(matrixStack, new Rect(window.getX() + 8 + anim, window.getY() + 49 + yOff, 21, 21), 4,
					text.alpha(type == window.getCurrent() ? 0.1f + type.getOpen().get() * 0.05f
							: type.getHover().get() * 0.1f));
			Round.draw(matrixStack, new Rect(window.getX() + 9 + anim, window.getY() + 50 + yOff, 19, 19), 3, bgColor);

			Render.image("category/" + type.getName().toLowerCase() + ".png", window.getX() + 12 + anim,
					window.getY() + 53 + yOff, 13, 13, text);

			type.setIndex((int) (yOff / 24));
			yOff += 24;
		}

		// Аватарка
		float off = this.avatarHoverAnim.get();
		Rect rect = new Rect(window.getX() + 9 + 30 * moveAnim.get() - 30 - off,
				window.getY() + window.getHeight() - 30 - off, 20 + off * 2, 20 + off * 2);
		this.avatarHoverAnim.setForward(Hover.isHovered(rect, mouseX, mouseY));
		if (Hover.isHovered(rect, mouseX, mouseY)) {
			CursorUtility.setType(CursorType.HAND);
			window.setCanDrag(false);
		}

		rock.getUser().drawAvatar(matrixStack, rect.getX(), rect.getY(), rect.getWidth(), 5,
				opening.get() * moveAnim.get());

		// Полоска слева от категорий
		Round.draw(matrixStack, new Rect(window.getX() + anim1, (float) (window.getY() + 51 + 24 * window.getPrevIndex()
				- 24 * changeCurrent.get()
				- (changeCurrentTail.get() < 0 ? Math.abs(24 * changeCurrentTail.get() - 24 * changeCurrent.get())
						: 0)),
				2, (float) Math.abs(24 * changeCurrentTail.get() - 24 * changeCurrent.get()) + 17), 0, 2, 0, 2,
				Style.getMain().alpha(opening.get()));

		// Разделение между категориями и модулями
		Round.draw(matrixStack, window.x(window.getX() + 37).width(1), 0, separatorColor);
	}

	private void renderModules(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {

		if (this.changeCategoryAnim.finished(false))
			this.changeCategoryAnim.setForward(true);

		boolean forwardsValue = window.getPrevIndex() > window.getCurrent().getIndex()
				? this.changeCategoryAnim.isForward()
				: !this.changeCategoryAnim.isForward();
		float animValue = (forwardsValue ? window.getHeight() * this.changeCategoryAnim.get() - window.getHeight()
				: window.getHeight() * (1 - this.changeCategoryAnim.get()));
		Category current = !this.changeCategoryAnim.isForward() && !this.changeCategoryAnim.finished()
				? window.getPrevCurrent()
				: window.getCurrent();

		GL11.glPushMatrix();
		GL11.glTranslatef(0, -window.getHeight() - 40 + (window.getHeight() + animValue + 40) * moveAnim.get(), 0);

		// Имя текущей категории
		if (this.isFullScreenCategory(current)) {
			Stencil.init();
			Round.draw(matrixStack, new Rect(window.getX() + 156 - 118 * this.themesAnim.get(), window.getY(),
					window.getWidth() - 156 + 118 * this.themesAnim.get(), 29), 1, FixColor.RED);
			Stencil.read(1);
			bold.get(24).draw(matrixStack, current.getDisplayName(), window.getX() + 48, window.getY() + 7, text);
			Stencil.finish();
		} else {
			bold.get(24).start();
			bold.get(24).draw(matrixStack, current.getDisplayName(),
					window.getX() + 48 - 10 * this.searchFocusedAnim.get(), window.getY() + 9,
					text.alpha(1 - this.searchFocusedAnim.get()));
			bold.get(24).draw(matrixStack, "Поиск..", window.getX() + 48 + 10 - 10 * this.searchFocusedAnim.get(),
					window.getY() + 9, text.alpha(this.searchFocusedAnim.get()));
			bold.get(24).end();
		}

		Rect modulesArea = new Rect(window.getX() + 38, window.getY() + 29, 118, window.getHeight() - 29);
		if (stencil) {
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			Render.scissor(modulesArea.getX(), modulesArea.getY(), modulesArea.getWidth(), modulesArea.getHeight());
		} else {
			Stencil.init();
			Round.draw(matrixStack, modulesArea, 0, separatorColor);
			Stencil.read(1);
		}

		float yOff = this.window.getScrollValue().get();
		if (current == Category.SCRIPTS) {
			if (rock.getScriptHandler().getEnabledScripts().isEmpty()) {
				semibold.get(14).start();

				yOff += 7;
				semibold.get(14).draw(matrixStack, "Тут будут отображаться", window.getX() + 48,
						window.getY() + 30 + yOff, moduleColor);
				yOff += 8;
				semibold.get(14).draw(matrixStack, "модули, созданные", window.getX() + 48, window.getY() + 30 + yOff,
						moduleColor);
				yOff += 8;
				semibold.get(14).draw(matrixStack, "при помощи скриптов", window.getX() + 48, window.getY() + 30 + yOff,
						moduleColor);
				yOff += 16;

				semibold.get(14).draw(matrixStack, "Посмотреть список", window.getX() + 48, window.getY() + 30 + yOff,
						moduleColor);
				yOff += 8;
				semibold.get(14).draw(matrixStack, "скриптов - .lua list", window.getX() + 48,
						window.getY() + 30 + yOff, moduleColor);

				semibold.get(14).end();
			}

			float startOff = yOff;

			for (Script script : rock.getScriptHandler().getEnabledScripts()) {
				String name = script.getName();

				if (script.getScriptModules().isEmpty())
					continue;

				// Рендер разделения по буквам
				Round.draw(matrixStack, new Rect(window.getX() + 37, window.getY() + 35 + yOff, 13, 1), 0,
						separatorColor);
				bold.get(12).draw(matrixStack, name, window.getX() + 49 + 6.5f, window.getY() + 31 + yOff, moduleColor);
				Round.draw(
						matrixStack, new Rect(window.getX() + 37 + bold.get(12).getWidth(name) + 25,
								window.getY() + 35 + yOff, 118 - (bold.get(12).getWidth(name) + 25), 1),
						0, separatorColor);

				yOff += 12;

				for (Module module : script.getScriptModules()) {
					yOff += 13;
				}
			}

			yOff = startOff;

			semibold.get(16).start();

			for (Script script : rock.getScriptHandler().getEnabledScripts()) {
				String name = script.getName();

				if (script.getScriptModules().isEmpty())
					continue;

				yOff += 12;

				// Рендер модулей
				for (Module module : script.getScriptModules()) {
					if (Hover.isHovered(modulesArea, window.getX() + 2 * module.getHover().get() + 48.5f,
							window.getY() + 35.5f + yOff)) {
						if (Hover.isHovered(window.getX() + 37, window.getY() + 30 + yOff, 118, 13, mouseX, mouseY)) {
							window.setCanDrag(false);
						}
						
						module.getOpened().setForward(window.getOpened() == module);
						module.getHover().setForward(Hover.isHovered(window.getX() + 37, window.getY() + 30 + yOff, 118,
								13, mouseX, mouseY));
						module.getTogglingAnim().setForward(module.get());

						semibold.get(16).draw(matrixStack, module.getInfo().name(),
								window.getX() + 2 * module.getHover().get() + 4 * module.getOpened().get() + 48.5f, window.getY() + 30.5f + yOff,
								text.alpha(0.3f));

						semibold.get(16).draw(matrixStack, module.getInfo().name(),
								window.getX() + 2 * module.getHover().get() + 4 * module.getOpened().get() + 48, window.getY() + 30 + yOff,
								(moduleColor.move(Style.getPoint((int) yOff).alpha(opening.get()),
										module.getTogglingAnim().get())));
					}
					yOff += 13;
				}
			}
			semibold.get(16).end();
		} else {
			Set<Entry<Character, List<Module>>> mods = get(current).entrySet();

			float startOff = yOff;
			for (Entry<Character, List<Module>> entry : mods) {
				String name = entry.getKey().toString();

				// Рендер разделения по буквам
				Round.draw(matrixStack, new Rect(window.getX() + 37, window.getY() + 35 + yOff, 13, 1), 0,
						separatorColor);
				bold.get(12).draw(matrixStack, name, window.getX() + 49 + 6.5f, window.getY() + 31 + yOff, moduleColor);
				Round.draw(
						matrixStack, new Rect(window.getX() + 37 + bold.get(12).getWidth(name) + 25,
								window.getY() + 35 + yOff, 118 - (bold.get(12).getWidth(name) + 25), 1),
						0, separatorColor);

				yOff += 12;

				for (Module module : entry.getValue()) {
					yOff += 13;
				}

			}

			yOff = startOff;

			semibold.get(16).start();
			for (Entry<Character, List<Module>> entry : mods) {
				String name = entry.getKey().toString();

				yOff += 12;

				// Рендер модулей
				for (Module module : entry.getValue()) {
					if (Hover.isHovered(modulesArea, window.getX() + 2 * module.getHover().get() + 48.5f,
							window.getY() + 35.5f + yOff)) {
						if (Hover.isHovered(window.getX() + 37, window.getY() + 30 + yOff, 118, 13, mouseX, mouseY)) {
							window.setCanDrag(false);
						}
						
						module.getOpened().setForward(window.getOpened() == module);
						module.getHover().setForward(Hover.isHovered(window.getX() + 37, window.getY() + 30 + yOff, 118, 13, mouseX, mouseY));
						module.getTogglingAnim().setForward(module.get());

						semibold.get(16).draw(matrixStack, module.getInfo().name(),
								window.getX() + 2 * module.getHover().get() + 4 * module.getOpened().get() + 48, window.getY() + 30 + yOff,
								(moduleColor.move(Style.getPoint((int) yOff).alpha(opening.get()),
										module.getTogglingAnim().get())));
					}

					boolean displayTooltip = false;

					if (mods.size() == 1 && Arrays.asList(mods).get(0).size() == 1) {
						for (String sin : entry.getValue().get(0).getInfo().module()) {
							if (sin.toLowerCase().contains(window.getInput().getText().toLowerCase())) {
								displayTooltip = true;
							}
						}
					}

					tooltip.setForward(displayTooltip);

					yOff += 13;
				}

			}
			semibold.get(16).end();
		}

		if (opening.isForward()) {
			Round.draw(matrixStack, new Rect(window.getX() + 38, window.getY() + 27, 118, 15), 1, bgColor, bgColor,
					bgColor.alpha(0), bgColor.alpha(0));

			Round.draw(matrixStack,
					new Rect(window.getX() + 38, window.getY() + 29 + window.getHeight() - 29 - 15, 118, 15), 1,
					bgColor.alpha(0), bgColor.alpha(0), bgColor, bgColor);
		}

		if (stencil) {
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
		} else {
			Stencil.finish();
		}

		yOff -= this.window.getScrollValue().get();

		window.setScroll(yOff < window.getHeight() - 30 ? 0 : Math.max(-yOff + 280, window.getScroll()));

		if (get(current).entrySet().isEmpty() && this.window.isSearching() && current != Category.SCRIPTS) {
			semibold.get(16).draw(matrixStack, "Ничего не найдено..", window.getX() + 48, window.getY() + 30 + yOff,
					moduleColor);
		}

		Render.end();
	}

	private void renderOpened(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		if (window.getQueue() != null) {
			openedAnim.setForward(false);
			if (openedAnim.finished(false)) {
				window.setOpened(window.getQueue());
				window.setQueue(null);
			}
		} else {
			openedAnim.setForward(window.getOpened() != null);
		}
		if (window.getOpened() != null)
			window.setPrevOpened(window.getOpened());

		if (!openedAnim.finished()) {

			this.themesAnim.setForward(this.isFullScreenCategory(window.getCurrent()));

			if (!stencil) {
				Stencil.init();
				Round.draw(matrixStack, window, 8, settingsBg);
				Stencil.read(1);
			}
			// Фон настроек
			Round.draw(matrixStack,
					new Rect((float) (loadedAnimX + 156 - 118 * this.themesAnim.get()),
							(float) (window.getY() + 29 * upSideAnim.get()),
							window.getWidth() - 156 + 118 * this.themesAnim.get(),
							(float) (window.getHeight() - (29 * upSideAnim.get()))),
					0, (float) (12 - 12 * upSideAnim.get()), 0, 8, settingsBg);

			float anim = 10 * upSideAnim.get();

			GL11.glPushMatrix();
			GL11.glTranslatef(window.getWidth() * this.themesAnim.get(), 0, 0);

			if (!rock.isNewYear() || stencil)
				Gifs.idontknow.draw(matrixStack, loadedAnimX + 272, window.getY() + 79 + 5 + anim, 100, 100,
						this.opening.get(), 20);

			String nothing = "Ничего не открыто";
			String desc1 = "После того как вы откроете модуль здесь";
			String desc2 = "будут отображаться настройки";

			bold.get(24).draw(matrixStack, nothing,
					loadedAnimX + 155 + (window.getWidth() - 155) / 2 - bold.get(24).getWidth(nothing) / 2,
					window.getY() + 184 + anim, text);

			bold.get(16).start();
			bold.get(16).draw(matrixStack, desc1,
					loadedAnimX + 155 + (window.getWidth() - 155) / 2 - bold.get(16).getWidth(desc1) / 2,
					window.getY() + 201 + anim, moduleColor);
			bold.get(16).draw(matrixStack, desc2,
					loadedAnimX + 155 + (window.getWidth() - 155) / 2 - bold.get(16).getWidth(desc2) / 2,
					window.getY() + 210 + anim, moduleColor);
			bold.get(16).end();

			Render.end();

			if (rock.isNewYear()) {
				Render.image("events/snow.png", window.getX() + window.getWidth() + 20 - 47,
						window.getY() + window.getHeight() + 15 - 47, 47, 47, bgColor.move(text, 0.05f));
				Render.image("events/snow.png", window.getX() + window.getWidth() - 23 - 32,
						window.getY() + window.getHeight() + 13 - 32, 32, 32, bgColor.move(text, 0.05f));
			}

			if (!stencil)
				Stencil.finish();

			if (rock.isNewYear() && !isFullScreenCategory(window.getCurrent()) && !stencil) {
				Stencil.init();
				Round.draw(matrixStack, new Rect(loadedAnimX + 272, window.getY() + 79 + 5 + anim, 100, 100), 11,
						FixColor.WHITE);
				Stencil.read(1);
				Gifs.idontknow.draw(matrixStack, loadedAnimX + 272, window.getY() + 79 + 5 + anim, 100, 100,
						this.opening.get(), 20);
				Stencil.finish();
			}

			if (window.getCurrent() == Category.THEMES || window.getPrevCurrent() == Category.THEMES)
				this.renderThemes(matrixStack, mouseX, mouseY, partialTicks);
		}

		if (window.getPrevOpened() != null && !openedAnim.finished(false)) {
			float anim = window.getWidth() - window.getWidth() * openedAnim.get();

			if (!stencil) {
				Stencil.init();
				Round.draw(matrixStack,
						new Rect((float) (loadedAnimX + 156), (float) (window.getY() + 29 * upSideAnim.get()),
								window.getWidth() - 156, (float) (window.getHeight() - (29 * upSideAnim.get()))),
						0, (float) (12 - 12 * upSideAnim.get()), 0, 8, settingsBg);
				Stencil.read(1);
				Round.draw(matrixStack,
						new Rect((float) (loadedAnimX + 156 + anim), (float) (window.getY() + 29 * upSideAnim.get()),
								window.getWidth() - 156, (float) (window.getHeight() - (29 * upSideAnim.get()))),
						0, (float) (12 - 12 * upSideAnim.get()), 0, 8, settingsBg);
				Stencil.finish();
			} else {
				Round.draw(matrixStack,
						new Rect((float) (loadedAnimX + 156 + anim), (float) (window.getY() + 29 * upSideAnim.get()),
								window.getWidth() - 156, (float) (window.getHeight() - (29 * upSideAnim.get()))),
						0, (float) (12 - 12 * upSideAnim.get()), 0, 8, settingsBg);
			}

			String openedMod = "Настройки " + window.getPrevOpened().getInfo().name();
			bold.get(24).draw(matrixStack, openedMod, loadedAnimX + 166 + anim, window.getY() + 39, text);
			bold.get(16).draw(matrixStack, window.getPrevOpened().getInfo().desc(), loadedAnimX + 166 + anim,
					window.getY() + 53.5f, moduleColor);

			if (Hover.isHovered(loadedAnimX + anim + 171 + bold.get(24).getWidth(openedMod), window.getY() + 43.5f, 9,
					9, mouseX, mouseY)
					|| Hover.isHovered(loadedAnimX + window.getWidth() - 21 + anim, window.getY() + 43, 9, 9, mouseX,
							mouseY)) {
				window.setCanDrag(false);
			}

			this.hoverBindAnim.setForward(Hover.isHovered(loadedAnimX + anim + 171 + bold.get(24).getWidth(openedMod),
					window.getY() + 43.5f, 9, 9, mouseX, mouseY));
			Render.image("icons/bind.png", loadedAnimX + anim + 171 + bold.get(24).getWidth(openedMod),
					window.getY() + 43.5f, 9, 9, text.alpha(1 - this.hoverBindAnim.get() * 0.3f));

			this.hoverCloseAnim.setForward(Hover.isHovered(loadedAnimX + window.getWidth() - 21 + anim,
					window.getY() + 43, 9, 9, mouseX, mouseY));
			Render.image("icons/close.png", loadedAnimX + window.getWidth() - 21 + anim, window.getY() + 43, 9, 9,
					text.alpha(1 - this.hoverCloseAnim.get() * 0.3f));

			if (!stencil) {
				Stencil.init();
				Round.draw(matrixStack,
						new Rect((float) (loadedAnimX + 156), (float) (window.getY() + 29 * upSideAnim.get() + 40),
								window.getWidth() - 156, (float) (window.getHeight() - 40 - (29 * upSideAnim.get()))),
						0, (float) (12 - 12 * upSideAnim.get()), 0, 8, settingsBg);
				Stencil.read(1);
			}

			if (window.getPrevOpened() instanceof ESP && mc.currentScreen instanceof ClickGuiScreen) {
				this.window.getEspSettings().renderPage(matrixStack, mouseX, mouseY, partialTicks);
			} else {
				if (get(window.getPrevOpened()).isEmpty()) {
					float space = 20;

					Gifs.sleep.draw(matrixStack, loadedAnimX + 272 + anim, window.getY() + 79 + 5 + space, 100, 100,
							this.opening.get(), 20);

					String nothing = "Настроек нет";
					String desc1 = "У этого модуля ещё нет настроек";
					String desc2 = "Может быть когда-нибудь появятся...";

					bold.get(24).draw(matrixStack, nothing, loadedAnimX + 155 + anim + (window.getWidth() - 155) / 2
							- bold.get(24).getWidth(nothing) / 2, window.getY() + 184 + space, text);

					bold.get(16).start();
					bold.get(16).draw(matrixStack, desc1,
							loadedAnimX + 155 + anim + (window.getWidth() - 155) / 2 - bold.get(16).getWidth(desc1) / 2,
							window.getY() + 201 + space, moduleColor);
					bold.get(16).draw(matrixStack, desc2,
							loadedAnimX + 155 + anim + (window.getWidth() - 155) / 2 - bold.get(16).getWidth(desc2) / 2,
							window.getY() + 210 + space, moduleColor);
					bold.get(16).end();
				} else {
					int leftY = 0, rightY = 0, column = 0;

					for (SettingRect setting : get(window.getPrevOpened())) {
						setting.getHide().setForward(setting.getParent().isHide());

						// Эта строчка переносит функции по столбикам без учёта тех, которые под хайдом
						// TODO сделать чтобы переносилось с анимкой
						// if (setting.getHide().finished(true)) continue;

						if (!setting.getHide().finished()) {
							setting.set(loadedAnimX + 166 + 160 * column + anim,
									window.getY() + 74 + (column == 0 ? leftY : rightY) - 8 * setting.getHide().get()
											+ window.getSettingsScrollValue().get(),
									152, setting.getHeight());
							setting.render(matrixStack, mouseX, mouseY, partialTicks,
									(float) opening.get() * (1 - setting.getHide().get()));

							if (setting.getParent().getDesc() != null
									&& Hover.isHovered(window.getX(), window.getY() + 30, window.getWidth(),
											window.getHeight() - 30, mouseX, mouseY)
									&& Hover.isHovered(setting, mouseX, mouseY)) {
								boolean canHover = true;
								
								for (Window window : window.getWindows()) {
									if (!window.canClick(mouseX, mouseY)) canHover = false;
								}
								
								if (canHover)
								hovered = setting;
							}
						}

						if (column == 0) {
							leftY += (setting.getHeight() + 9) * (1 - setting.getHide().get());
						} else {
							rightY += (setting.getHeight() + 9) * (1 - setting.getHide().get());
						}
						column++;
						if (column > 1)
							column = 0;
					}

					float yOff = Math.max(leftY, rightY) + window.getSettingsScrollValue().get();

					float scroll = Math.max(window.getSettingsScroll(),
							window.getHeight() - Math.max(leftY, rightY) - 80);

					window.setSettingsScroll(Math.max(leftY, rightY) < window.getHeight() - 100 ? 0 : scroll);
				}
			}

			if (!stencil)
				Stencil.finish();

			this.shadowAnim.setForward(window.getSettingsScroll() != 0);

			Round.draw(matrixStack,
					new Rect((float) (loadedAnimX + 156), (float) (window.getY() + 29 * upSideAnim.get() + 40),
							(window.getWidth() - 156), (float) (window.getHeight() - 40 - (29 * upSideAnim.get())) / 7),
					1, settingsBg.alpha(this.shadowAnim.get()), settingsBg.alpha(this.shadowAnim.get()),
					settingsBg.alpha(0), settingsBg.alpha(0));
			Round.draw(matrixStack, new Rect((float) (loadedAnimX + 156),
					(float) (window.getY() + 29 * upSideAnim.get() + 40)
							+ (float) (window.getHeight() - 40 - (29 * upSideAnim.get()))
							- (window.getHeight() - 40 - (29 * upSideAnim.get())) / 15,
					(window.getWidth() - 156), (float) (window.getHeight() - 40 - (29 * upSideAnim.get())) / 15), 12,
					settingsBg.alpha(0), settingsBg.alpha(0), settingsBg.alpha(this.shadowAnim.get()),
					settingsBg.alpha(this.shadowAnim.get()));
		}
	}

	private void renderTab(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		float anim = this.searchAnim.get();
		// Разделение между верхней частью и настройками
		Round.draw(matrixStack,
				new Rect(window.getX() + 156 - 118 * this.themesAnim.get(),
						window.getY() + 29 - (29 - 29 * upSideAnim.get()),
						window.getWidth() - 156 + 118 * this.themesAnim.get(), 1),
				0, separatorColor);

		this.hoverOpenFolderAnim.setForward(Hover.isHovered(window.getX() + 449,
				window.getY() + 8 - (29 - 29 * upSideAnim.get()), 13, 13, mouseX, mouseY));
		// Render.image("category/folder.png", window.getX() + 449, window.getY() + 8 -
		// (29 - 29 * upSideAnim.get()), 13, 13, text.alpha(1 -
		// this.hoverOpenFolderAnim.get()*0.3f));

		this.hoverSearchAnim.setForward(Hover.isHovered(window.getX() + 468,
				window.getY() + 9 - (29 - 29 * upSideAnim.get()), 11, 11, mouseX, mouseY));
		if (Hover.isHovered(window.getX() + 468, window.getY() + 9 - (29 - 29 * upSideAnim.get()), 11, 11, mouseX,
				mouseY)) {
			window.setCanDrag(false);
		}

		// Если поиск активен рендерим иконку закрытия, если нет то иконку поиска
		Render.image("category/search.png", window.getX() + 468, window.getY() + 9 - (29 - 29 * upSideAnim.get()), 11,
				11, text.alpha((1 - this.hoverSearchAnim.get() * 0.3f) * (1 - anim)));
		Render.image("icons/close.png", window.getX() + 468, window.getY() + 9 - (29 - 29 * upSideAnim.get()), 11, 11,
				text.alpha((1 - this.hoverSearchAnim.get() * 0.3f) * anim));

		String buttonTitle = "Сохранить конфиг";
		Rect rect = new Rect(window.getX() + 166, window.getY() + 7 - (29 - 29 * upSideAnim.get()),
				MathUtility.interpolate(bold.get(16).getWidth(buttonTitle) + 9, 150, anim), 15);
		this.hoverSaveConfigAnim.setForward(Hover.isHovered(rect, mouseX, mouseY));

		if (Hover.isHovered(rect, mouseX, mouseY)) {
			window.setCanDrag(false);
		}

		// Рект, который мы используем как для кнопки "Сохранить конфиг", так и для
		// поиска
		Round.draw(matrixStack, rect, 3,
				separatorColor.move(separatorColor.darker(0.1f), this.hoverSaveConfigAnim.get()));

		this.searchAnim.setForward(window.isSearching()); // Если поиск активен анимируем :D

		if (!this.searchAnim.finished()) { // То, что отображается когда поиск неактивен
			bold.get(16).draw(matrixStack, buttonTitle, window.getX() + 170,
					window.getY() + 9 - (29 - 29 * upSideAnim.get()) - 5 * anim, text.alpha(1 - anim));

			if (!this.hoverSaveConfigAnim.finished(false)) {
				String tooltipTitle = "Кнопка сохранит конфиг \"" + rock.getConfigHandler().getCurrent() + "\"";

				Round.draw(matrixStack, new Rect(window.getX() + 181 + bold.get(16).getWidth(buttonTitle),
						window.getY() + 7 - (29 - 29 * upSideAnim.get()), bold.get(16).getWidth(tooltipTitle) + 9, 15),
						3, separatorColor.alpha(0.7f * this.hoverSaveConfigAnim.get() * (1 - anim)));
				bold.get(16).draw(matrixStack, tooltipTitle, window.getX() + 185 + bold.get(16).getWidth(buttonTitle),
						window.getY() + 9 - (29 - 29 * upSideAnim.get()),
						text.alpha(this.hoverSaveConfigAnim.get() * (1 - anim)));
			}
		}

		if (!this.searchAnim.finished(false)) { // То, что отображается когда поиск активен
			bold.get(16).draw(matrixStack, "Поиск..", window.getX() + 170,
					window.getY() + 9 - (29 - 29 * upSideAnim.get()) + 5 - 5 * anim - 5 * this.searchFocusedAnim.get(),
					text.alpha(
							anim * (1 - this.hoverSaveConfigAnim.get() * 0.5f) * (1 - this.searchFocusedAnim.get())));

			if (this.window.getInput() == null) {
				this.window.setInput(new InputWidget(bold.get(16), (int) (window.getX() + 166),
						(int) (window.getY() + 7 - (29 - 29 * upSideAnim.get())), (int) (150), (int) (15),
						new TranslationTextComponent(""), false));
			}

			this.searchFocusedAnim.setForward(this.window.isSearching()
					&& (this.window.getInput().isFocused() || !this.window.getInput().getText().isEmpty()));

			this.window.getInput().x = (int) (window.getX() + 165);
			this.window.getInput().y = (int) (window.getY() + 5 - 5 * this.searchFocusedAnim.get() + 8
					- (29 - 29 * upSideAnim.get()));

			this.window.getInput().renderButton(matrixStack, mouseX, mouseY, partialTicks,
					this.opening.get() * anim * this.searchFocusedAnim.get());
		}
	}

	private void renderThemes(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		Stencil.init();
		Round.draw(matrixStack, window.x(window.getX() + 38).width(window.getWidth() - 38), 12, separatorColor);
		Stencil.read(1);
		GL11.glPushMatrix();
		GL11.glTranslatef(window.getWidth() * this.themesAnim.get(), 0, 0);

		float xOff = 0;
		for (Theme theme : rock.getThemes()) {
			theme.getSelectAnimation().setForward(rock.getThemes().getCurrent() == theme);

			float width = bold.get(18).getWidth(theme.getName()) + 65;
			float off = theme.getSelectAnimation().get();
			Round.draw(matrixStack,
					new Rect(window.getX() - window.getWidth() + 45 + xOff, window.getY() + 37, width, 25), 3,
					separatorColor);
			Round.draw(matrixStack, new Rect(window.getX() - window.getWidth() + 45 + xOff + off,
					window.getY() + 37 + off, width - off * 2, 25 - off * 2), 3 - off, bgColor);
			bold.get(18).draw(matrixStack, theme.getName(), window.getX() - window.getWidth() + 53 + xOff,
					window.getY() + 43, text);

			Round.draw(matrixStack,
					new Rect(window.getX() - window.getWidth() + 47 + width + xOff - 19, window.getY() + 45, 9, 9),
					4.5f, separatorColor);
			Round.draw(matrixStack,
					new Rect(window.getX() - window.getWidth() + 47 + width + xOff - 19 + 1, window.getY() + 46, 7, 7),
					3.5f, theme.getFirstColor().alpha(this.opening.get()));

			Round.draw(matrixStack,
					new Rect(window.getX() - window.getWidth() + 47 + width + xOff - 30, window.getY() + 45, 9, 9),
					4.5f, separatorColor);
			Round.draw(matrixStack,
					new Rect(window.getX() - window.getWidth() + 47 + width + xOff - 30 + 1, window.getY() + 46, 7, 7),
					3.5f, theme.getSecondColor().alpha(this.opening.get()));

			xOff += width + 5;
		}

		xOff = 0;
		float yOff = 33;
		for (Style style : Style.values()) {
			style.getSelectAnimation().setForward(Style.getCurrent() == style);

			float width = 105;
			float off = style.getSelectAnimation().get();
			Round.draw(matrixStack,
					new Rect(window.getX() - window.getWidth() + 45 + xOff, window.getY() + 37 + yOff, width, 33), 3,
					separatorColor);
			Round.draw(matrixStack, new Rect(window.getX() - window.getWidth() + 45 + xOff + off,
					window.getY() + 37 + yOff + off, width - off * 2, 33 - off * 2), 3 - off, bgColor);
			bold.get(16).draw(matrixStack, style.getName(), window.getX() - window.getWidth() + 53 + xOff,
					window.getY() + 43 + yOff, text);

			float xOffColors = 0;
			for (FixColor color : style.getColors()) {
				Round.draw(matrixStack, new Rect(window.getX() - window.getWidth() + 52 + xOff + xOffColors,
						window.getY() + 55 + yOff, 8, 8), 4, separatorColor);
				Round.draw(matrixStack, new Rect(window.getX() - window.getWidth() + 53 + xOff + xOffColors,
						window.getY() + 56 + yOff, 6, 6), 3, color.alpha(this.opening.get()));

				xOffColors += 10;
			}

			xOff += width + 5;
			if (xOff + width + 5 > window.getWidth()) {
				yOff += 38;
				xOff = 0;
			}
		}

		Render.end();
		Stencil.finish();
	}

	private boolean isFullScreenCategory(Category cat) {
		return cat == Category.THEMES;
	}

	private GlyphType get(Category type) {
		return window.get(type);
	}

	private List<SettingRect> get(Module module) {
		return window.get(module);
	}

}
