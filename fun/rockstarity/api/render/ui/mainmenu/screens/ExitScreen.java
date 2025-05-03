package fun.rockstarity.api.render.ui.mainmenu.screens;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.AssetsLoader;
import fun.rockstarity.api.helpers.math.Hover;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.helpers.render.gif.GifRender;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.render.shaders.list.FrameFreeze;
import fun.rockstarity.api.render.shaders.list.Outline;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.clickgui.ClickGuiRenderer;
import fun.rockstarity.api.render.ui.mainmenu.Page;
import fun.rockstarity.api.render.ui.rect.Rect;
import lombok.Getter;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * @author ConeTin
 * @since 20 окт. 2024 г.
 */

@Getter
public class ExitScreen extends Screen {
	
	private final Animation cancelAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	private final Animation successAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	
	private Rect leftRect, rightRect;
	
	public ExitScreen() {
		super(new TranslationTextComponent("Выход из игры"));
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
		bold.get(16).draw(matrixStack, "тише", -100, -100, textSecond);

		alphaAnim.setForward(rock.getMenuManager().getTarget() == null);
		
		float logoSize = sr.getScaledWidth() / 1.88f;
		
		rock.getMenuManager().drawBackground(matrixStack, mouseX, mouseY, partialTicks);
		rock.getMenuManager().drawHotbar(matrixStack, mouseX, mouseY, 1);
		
		Rect rect = rock.getMenuManager().drawWindow(matrixStack, mouseX, mouseY, 1);

		float titleWidth = bold.get(20).getWidth("Подтверждение выхода");
		bold.get(20).draw(matrixStack, "Подтверждение выхода", sr.getScaledWidth()/2F - titleWidth / 2F, rect.getY() - 18, text.alpha(alphaAnim.get()));
		
		
		float leaveWidth1 = bold.get(20).getWidth("Вы действительно желаете");
		bold.get(20).draw(matrixStack, "Вы действительно желаете", sr.getScaledWidth()/2F - leaveWidth1 / 2F, rect.getY() + 14, text.alpha(alphaAnim.get()));
		float leaveWidth2 = bold.get(20).getWidth("покинуть игру?");
		bold.get(20).draw(matrixStack, "покинуть игру?", sr.getScaledWidth()/2F - leaveWidth2 / 2F, rect.getY() + 22, text.alpha(alphaAnim.get()));
		
		// Левая
		leftRect = new Rect(rect.getX() + 10, rect.getY() + rect.getHeight() - 29, 87, 19);
		Round.draw(matrixStack, leftRect, 3, rock.getThemes().getSecondColor().alpha(alphaAnim.get()));
		Outline.draw(matrixStack, leftRect, 3, 0.1f, rock.getThemes().getFoursColor().alpha(alphaAnim.get() * 0.7f));
		bold.get(17).draw(matrixStack, "Отмена", rect.getX() + 53.5f - bold.get(17).getWidth("Отмена")/2F, rect.getY() + rect.getHeight() - 26f, text.alpha(alphaAnim.get()));
		
		// Правая
		rightRect = new Rect(rect.getX() + rect.getWidth() - 97, rect.getY() + rect.getHeight() - 29, 87, 19);
		Round.draw(matrixStack, rightRect.y(rightRect.getY() - 0.5f), 3, Style.getCurrent().getColors()[1].alpha(alphaAnim.get()));
		Outline.draw(matrixStack, rightRect, 3, 0.1f, rock.getThemes().getFoursColor().alpha(alphaAnim.get() * 0.7f));
		bold.get(17).draw(matrixStack, "Выйти", rect.getX() + rect.getWidth() - 53.5f - bold.get(17).getWidth("Выйти")/2F, rect.getY() + rect.getHeight() - 26f, text.alpha(alphaAnim.get()));
		
		//super.render(matrixStack, mouseX, mouseY, partialTicks);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		rock.getMenuManager().keyPressed(keyCode, scanCode, modifiers);

		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		rock.getMenuManager().clicked(mouseX, mouseY, button);
		
		if (Hover.isHovered(leftRect, mouseX, mouseY)) {
			rock.getMenuManager().setTarget(Page.MAIN.getScreen());
		} else if (Hover.isHovered(rightRect, mouseX, mouseY)) {
			rock.getMenuManager().setFinishing(true);
			rock.getMenuManager().setTarget(Page.MAIN.getScreen());
		}
		
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		return super.mouseReleased(mouseX, mouseY, button);
	}

}
