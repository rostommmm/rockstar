package fun.rockstarity.api.render.ui.mainmenu.banner.screens;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.helpers.math.Hover;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.helpers.secure.Web;
import fun.rockstarity.api.helpers.system.TextUtility;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.render.shaders.list.FrameFreeze;
import fun.rockstarity.api.render.shaders.list.Outline;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.mainmenu.Page;
import fun.rockstarity.api.render.ui.mainmenu.banner.Banner;
import fun.rockstarity.api.render.ui.rect.Rect;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.TranslationTextComponent;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ZarobotokScreen extends Screen {
	
	Animation nickHover = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	Animation supHover = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	
	public ZarobotokScreen() {
		super(new TranslationTextComponent("Заработок"));
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
		
		rock.getMenuManager().drawBackground(matrixStack, mouseX, mouseY, partialTicks);
		
		
		float offX = sr.getScaledWidth() / 2 - bold.get(20).getWidth("Промокод " + rock.getUser().getName() + " - скидка 5%") / 2F;
		float offY = sr.getScaledHeight() / 2 - 30;
		
		offX += bold.get(20).draw(matrixStack, "Промокод ", offX, offY, textSecond).getWidth();
		Rect rect = bold.get(20).draw(matrixStack, rock.getUser().getName(), offX, offY, Style.getMain().alpha(1-nickHover.get()*0.6f));
		nickHover.setForward(Hover.isHovered(rect, mouseX, mouseY));
		offX += rect.getWidth();
		offX += bold.get(20).draw(matrixStack, " - скидка 5%", offX, offY, textSecond).getWidth();
		offY += rect.getHeight();
		
		offX = sr.getScaledWidth() / 2 - bold.get(20).getWidth("Вы получите 10% с продажи") / 2F;
		offX += bold.get(20).draw(matrixStack, "Вы получите 10% с продажи", offX, offY, textSecond).getWidth();
		offY += rect.getHeight();

		offX = sr.getScaledWidth() / 2 - bold.get(20).getWidth("Насчет выплаты писать Поддержке") / 2F;
		offX += bold.get(20).draw(matrixStack, "Насчёт выплаты писать ", offX, offY, textSecond).getWidth();
		rect = bold.get(20).draw(matrixStack, "Поддержке", offX, offY, Style.getMain().alpha(1-supHover.get()*0.6f));
		supHover.setForward(Hover.isHovered(rect, mouseX, mouseY));
		offX += rect.getWidth();
		
		bold.get(20).draw(matrixStack, "Назад", 5, 5, textSecond);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (nickHover.finished()) {
			TextUtility.copyText(rock.getUser().getName());
		}
		
		if (supHover.finished()) {
			Web.openWebpage("https://t.me/rockclientsupport");
		}
		
		if (Hover.isHovered(new Rect(5, 5, bold.get(20).getWidth("Назад"), bold.get(20).getHeight()), mouseX, mouseY)) {
			mc.displayGuiScreen(Page.MAIN.getScreen());
		}
		
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
