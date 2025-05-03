package fun.rockstarity.api.render.ui.mainmenu.screens;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.helpers.secure.Web;
import fun.rockstarity.api.helpers.system.FileUtility;
import fun.rockstarity.api.helpers.system.TextUtility;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.render.shaders.list.FrameFreeze;
import fun.rockstarity.api.render.shaders.list.Outline;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.mainmenu.banner.Banner;
import fun.rockstarity.api.render.ui.mainmenu.banner.screens.ZarobotokScreen;
import fun.rockstarity.api.render.ui.rect.Rect;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * @author ConeTin
 * @since 20 окт. 2024 г.
 */

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MainPageScreen extends Screen {
	
	Animation bgAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(500);
	Animation showBgAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(500);
	Animation showingAnim = new Animation().setEasing(Easing.EASE_OUT_CIRC).setSpeed(300);
	Animation showNameAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	
	Banner[] banners = {
			new Banner("banners/zarabotok.png", () -> {
				mc.displayGuiScreen(new ZarobotokScreen());
			}, 55),
			new Banner("banners/beseda.png", () -> {
				Web.openWebpage(rock.getUser().getRole().equals("Premium") || rock.getUser().getRole().equals("Basic") ? "https://t.me/+wn9wSCac5kRlNDcy" :  "https://t.me/+gLtcsAznTzUwMjBi");
			}, 55)
	};
	
	public MainPageScreen() {
		super(new TranslationTextComponent("Главное меню"));
	}
	
	@Override
	protected void init() {
		
		super.init();
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		
		FixColor first = rock.getThemes().getFirstColor();
		FixColor second = rock.getThemes().getSecondColor();
		FixColor text = rock.getThemes().getTextFirstColor();
		FixColor textSecond = rock.getThemes().getTextSecondColor();
		Animation alphaAnim = rock.getMenuManager().getAlphaAnim();
		
		bgAnim.setForward(true);
		showBgAnim.setForward(bgAnim.get() > 0.7f && !rock.getMenuManager().isFinishing());
		showingAnim.setForward(showBgAnim.isForward() && showBgAnim.get() > 0.8f);
		alphaAnim.setForward(showingAnim.isForward() && showingAnim.get() > 0.85f && rock.getMenuManager().getTarget() == null);
		
		float logoSize = sr.getScaledWidth() / 1.88f;
		
    	Render.initRotate(sr.getScaledWidth()/2F, sr.getScaledHeight() - 10, -45 * bgAnim.get());
		Render.scale(sr.getScaledWidth()/2F, sr.getScaledHeight() - 10, 1 - bgAnim.get());
		FrameFreeze.draw(matrixStack, bgAnim.get());
		Render.end();
		Render.endRotate();
		
		Round.draw(matrixStack, new Rect(0,0,sr.getScaledWidth(), sr.getScaledHeight()), 0, first.alpha(bgAnim.get()));

		if (bgAnim.get() > 0.7f) {
			Render.image("masks/ui/mainmenu/bglogo.png", -logoSize/9.14F - logoSize + logoSize * showBgAnim.get(), sr.getScaledHeight() + logoSize/7.31f - logoSize, logoSize, logoSize, second.move(first, rock.getThemes().getCurrent() == rock.getThemes().getDarkTheme() ? 0.5f : 0).alpha(showBgAnim.get()));
			Render.grid(matrixStack, rock.getThemes().getThirdColor().alpha(bgAnim.get()*0.1f), sr.getScaledWidth()/15);
			Render.gridMask(matrixStack, FixColor.WHITE, new Rect(mouseX-5, mouseY-5, 10, 10), 100);

			// Хотбар
			rock.getMenuManager().drawHotbar(matrixStack, mouseX, mouseY, showingAnim.get());
			
			Rect rect = rock.getMenuManager().drawWindow(matrixStack, mouseX, mouseY, showingAnim.get());
			
			drawString(matrixStack, sr.getScaledWidth()/2F - (bold.get(20).getWidth("Вы наиграли " + TextUtility.formatTime(rock.getUser().getPlaytime() + rock.getUser().getPlayed().getElapsed()))) / 2F, 5, "Вы наиграли ", TextUtility.formatTime(rock.getUser().getPlaytime() + rock.getUser().getPlayed().getElapsed()) + "", text.move(textSecond, 0.5f).alpha(alphaAnim.get()), Style.getCurrent().getColors()[1].alpha(alphaAnim.get()));
			drawString(matrixStack, sr.getScaledWidth()/2F - (bold.get(20).getWidth("За эту сессию " + TextUtility.formatTime(rock.getUser().getPlaytime() + rock.getUser().getPlayed().getElapsed()-rock.getUser().getStartTime()))) / 2F, 18, "За эту сессию ", TextUtility.formatTime(rock.getUser().getPlaytime() + rock.getUser().getPlayed().getElapsed() - rock.getUser().getStartTime()) + "", text.move(textSecond, 0.5f).alpha(alphaAnim.get()*0.7f), Style.getCurrent().getColors()[1].alpha(alphaAnim.get()*0.7f));

			// Приветствие
			float titleWidth = bold.get(20).getWidth("Привествуем в Rockstar!") + 1.5f;
			float titleOffset = drawString(matrixStack, sr.getScaledWidth()/2F - titleWidth / 2F, rect.getY() - 18, "Привествуем в ", "Rockstar", text.alpha(alphaAnim.get()), Style.getCurrent().getColors()[1].alpha(alphaAnim.get()))
					+ bold.get(20).getWidth("Rockstar") + 1.5f;
			bold.get(20).draw(matrixStack, "!", sr.getScaledWidth()/2F - titleWidth / 2F + titleOffset, rect.getY() - 18, text.alpha(alphaAnim.get()));
			
			// Аватар
			FixColor upLeft = Style.getPoint(0).alpha(alphaAnim.get());
			FixColor upRight = Style.getPoint(90).alpha(alphaAnim.get());
			FixColor downLeft = Style.getPoint(180).alpha(alphaAnim.get());
			FixColor downRight = Style.getPoint(270).alpha(alphaAnim.get());
			
			float avatarSize = 40;
			Rect avatarRect = new Rect(sr.getScaledWidth()/2F - avatarSize/2f, rect.getY() + 27, avatarSize, avatarSize);
			Outline.draw(matrixStack, avatarRect.size(-1.5f), (avatarSize+2)/2F, 0.5f, upLeft, upRight, downLeft, downRight);
			rock.getUser().drawAvatar(matrixStack, avatarRect.getX(), avatarRect.getY(), avatarRect.getWidth(), avatarRect.getWidth()/2F,alphaAnim.get());

			// Инфа о акке
			float yOff = 65;
			drawString(matrixStack, sr.getScaledWidth()/2F - (bold.get(20).getWidth("Вы авторизованы как: " + rock.getUser().getName())) / 2F, rect.getY() + rect.getHeight() - yOff, "Вы авторизованы как: ", rock.getUser().getName(), text.move(textSecond, 0.5f).alpha(alphaAnim.get()), Style.getCurrent().getColors()[1].alpha(alphaAnim.get()));
			yOff -= 14;
			drawString(matrixStack, sr.getScaledWidth()/2F - (bold.get(20).getWidth("Ваш порядковый номер: " + rock.getUser().getUid())) / 2F, rect.getY() + rect.getHeight() - yOff, "Ваш порядковый номер: ", rock.getUser().getUid() + "", text.move(textSecond, 0.5f).alpha(alphaAnim.get()), Style.getCurrent().getColors()[1].alpha(alphaAnim.get()));
			yOff -= 14;
			drawString(matrixStack, sr.getScaledWidth()/2F - (bold.get(20).getWidth("Кол-во запусков: " + rock.getUser().getStarts())) / 2F, rect.getY() + rect.getHeight() - yOff, "Кол-во запусков: ", rock.getUser().getStarts() + "", text.move(textSecond, 0.5f).alpha(alphaAnim.get()), Style.getCurrent().getColors()[1].alpha(alphaAnim.get()));
			 
			float bannerY = 0;
			for (Banner banner : banners) {
				banner.render(matrixStack, new Rect(4 - 152 + 152 * alphaAnim.get() * showNameAnim.get(), 5 + bannerY, 152, banner.getHeight()), mouseX, mouseY, alphaAnim.get() * showNameAnim.get());
				bannerY += banner.getHeight() + 5;
			}
			
			// Майнкрафт нейм
			showNameAnim.setForward(sr.getScaledHeight() > 300);
			drawString(matrixStack, sr.getScaledWidth()/2F - (bold.get(20).getWidth("В игре вы: " + mc.getSession().getUsername())) / 2F, rect.getY() + rect.getHeight() + 2 - 5 + 5 * showNameAnim.get(), "В игре вы: ", mc.getSession().getUsername(), text.alpha(alphaAnim.get() * showNameAnim.get()), Style.getCurrent().getColors()[1].alpha(alphaAnim.get() * showNameAnim.get()));
		}
		
		//super.render(matrixStack, mouseX, mouseY, partialTicks);
		
		if (rock.getMenuManager().isFinishing() && showingAnim.finished(false)) {
			mc.shutdown();
		}
	}
	
	private float drawString(MatrixStack matrixStack, float x, float y, String left, String right, FixColor color, FixColor color1) {
		float titleOffset = 0;
		
		bold.get(20).draw(matrixStack, left, x + titleOffset, y, color);
		titleOffset += bold.get(20).getWidth(left);
		bold.get(20).draw(matrixStack, right, x + titleOffset, y, color1);
		
		return titleOffset;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		rock.getMenuManager().keyPressed(keyCode, scanCode, modifiers);

		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		
		for (Banner banner : banners) {
			banner.mouseClicked(mouseX, mouseY, button);
		}
		
		rock.getMenuManager().clicked(mouseX, mouseY, button);
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		return super.mouseReleased(mouseX, mouseY, button);
	}
	
	@Override
	public void closeScreen() {
		super.closeScreen();
	}

}
