package fun.rockstarity.api.render.ui.mainmenu;

import java.awt.print.Pageable;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.game.GameUtility;
import fun.rockstarity.api.helpers.math.Hover;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.helpers.render.Stencil;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.animation.infinity.ColorAnimation;
import fun.rockstarity.api.render.animation.infinity.InfinityAnimation;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.render.shaders.list.Glow;
import fun.rockstarity.api.render.shaders.list.Outline;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.mainmenu.loading.LoadingScreen;
import fun.rockstarity.api.render.ui.mainmenu.screens.MainPageScreen;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.client.modules.render.Interface;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.screen.Screen;

/**
 * @author ConeTin
 * @since 20 окт. 2024 г.
 */

public class MenuManager implements IAccess {
	
	@Getter private final Animation alphaAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	@Getter @Setter private Screen target;
	@Getter @Setter private boolean finishing;
	
	private final InfinityAnimation windowHeight = new InfinityAnimation();
	private final InfinityAnimation windowWidth = new InfinityAnimation();
	private final InfinityAnimation windowY = new InfinityAnimation();
	private final ColorAnimation corner = new ColorAnimation();
	
	public void drawBackground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		FixColor first = rock.getThemes().getFirstColor();
		FixColor second = rock.getThemes().getSecondColor();

		float logoSize = sr.getScaledWidth() / 1.88f;
		
		Round.draw(matrixStack, new Rect(0,0,sr.getScaledWidth(), sr.getScaledHeight()), 0, first);
		
		Render.image("masks/ui/mainmenu/bglogo.png", -logoSize/9.14F, sr.getScaledHeight() + logoSize/7.31f - logoSize, logoSize, logoSize, second.move(first, rock.getThemes().getCurrent() == rock.getThemes().getDarkTheme() ? 0.5f : 0));
		
		Render.grid(matrixStack, rock.getThemes().getThirdColor().alpha(0.1f), sr.getScaledWidth()/15);
		Render.gridMask(matrixStack, FixColor.WHITE, new Rect(mouseX-5, mouseY-5, 10, 10), 100);
	}
	
	public Rect drawWindow(MatrixStack matrixStack, int mouseX, int mouseY, float anim) {
		FixColor first = rock.getThemes().getFirstColor();
		FixColor second = rock.getThemes().getSecondColor();
		FixColor text = rock.getThemes().getTextFirstColor();
		
		//mc.getGameSettings().guiScale = 2;
		//mc.getMainWindow().setGuiScale(2);

		if (rock.getMenuManager().finishing && mc.currentScreen == Page.MAIN.getScreen()) {
			windowWidth.animate(204, 50);
			windowHeight.animate(-1, 50);
			windowY.animate(0, 50);
		} else if (mc.currentScreen == Page.MAIN.getScreen()) {
			windowWidth.animate(236, 50);
			windowHeight.animate(146, 50);
			windowY.animate(0, 50);
		} else if (mc.currentScreen == Page.SINGE.getScreen()) {
			windowWidth.animate(298, 50);
			windowHeight.animate(sr.getScaledHeight()/1.33F, 50);
			windowY.animate(sr.getScaledHeight()/12.73F, 50);
		} else if (mc.currentScreen == Page.MULTI.getScreen()) {
			windowWidth.animate(328, 50);
			windowHeight.animate(sr.getScaledHeight()/1.33F, 50);
			windowY.animate(sr.getScaledHeight()/12.73F, 50);
		} else if (mc.currentScreen == Page.ALT.getScreen()) {
			windowWidth.animate(245, 50);
			windowHeight.animate(176, 50);
			windowY.animate(58, 50);
		} else if (mc.currentScreen == Page.SETTINGS.getScreen()) {
			windowWidth.animate(204, 50);
			windowHeight.animate(-1, 50);
			windowY.animate(0, 50);
		} else if (mc.currentScreen == Page.EXIT.getScreen()) {
			windowWidth.animate(204, 50);
			windowHeight.animate(75, 50);
			windowY.animate(0, 50);
		}
		
		FixColor cornerColor = corner.animate(mc.currentScreen == Page.MAIN.getScreen() || mc.currentScreen == Page.EXIT.getScreen() ? second : first, 150);
		
		float width = windowWidth.get();
		float height = windowHeight.get() * anim;
		Rect rect = new Rect(sr.getScaledWidth()/2F - width/2F, sr.getScaledHeight()/2F - height/2F - windowY.get(), width, height);

		Render.gridMask(matrixStack, rock.getThemes().getFoursColor(), rect);
		
		Round.draw(matrixStack, rect, 8, cornerColor.alpha(anim), first.alpha(anim), first.alpha(anim), first.alpha(anim));
		Outline.draw(matrixStack, rect, 8, 0.1f, rock.getThemes().getFoursColor().alpha(anim * 0.7f));
		
		if (rock.isNewYear()) {
			Stencil.init();
			Round.draw(matrixStack, rect.size(1), 8, cornerColor.alpha(anim), first.alpha(anim), first.alpha(anim), first.alpha(anim));
			Stencil.read(1);
			Render.image("events/snow.png", rect.getX() - 20, rect.getY() - 15, 47, 47, first.move(text, 0.05f));
			Render.image("events/snow.png", rect.getX() + 23, rect.getY() - 13, 32, 32, first.move(text, 0.05f));
			
			Render.image("events/snow.png", rect.getX() + rect.getWidth() + 20 - 47, rect.getY() - 15, 47, 47, first.move(text, 0.05f));
			Render.image("events/snow.png", rect.getX() + rect.getWidth() - 23 - 32, rect.getY() - 13, 32, 32, first.move(text, 0.05f));
			Stencil.finish();
		}
		
		return rect;
	}
	
	public void drawHotbar(MatrixStack matrixStack, int mouseX, int mouseY, float anim) {
		FixColor first = rock.getThemes().getFirstColor();
		FixColor second = rock.getThemes().getSecondColor();
		FixColor text = rock.getThemes().getTextFirstColor();
		
		float width = 5;
		float height = 29;
		
		int i = 0;
		for (Page page : Page.values()) {
			page.getShowing().setForward(i == 0 ? anim >= 0.3f : Page.values()[i-1].getShowing().get() > 0.1f);
			
			width += 25 * page.getShowing().get();
			i++;
		}
		
		Rect rect = new Rect(sr.getScaledWidth()/2F - width/2F, sr.getScaledHeight() - (10 + height) * anim, width, height);
		
		Render.gridMask(matrixStack, rock.getThemes().getFoursColor(), rect);
		
		Round.draw(matrixStack, rect, 6, second.alpha(anim), first.alpha(anim), second.alpha(anim), first.alpha(anim));
		Outline.draw(matrixStack, rect, 6, 0.1f, rock.getThemes().getFoursColor().alpha(anim * 0.7f));
		 
		Stencil.init();
		Round.draw(matrixStack, rect, 6, second.alpha(anim), first.alpha(anim), second.alpha(anim), first.alpha(anim));
		Stencil.read(1);
		float xOff = 0;
		for (Page page : Page.values()) {
			page.getSelection().setForward(page.getScreen() == mc.currentScreen);
			if (page.getSelectionGlow().finished() || page.getScreen() == mc.currentScreen)
				page.getSelectionGlow().setForward(false);
			
			FixColor color = Style.getCurrent().getColors()[1];
			float offset = 7;
			Rect pageRect = new Rect(rect.getX() + 7 + xOff, rect.getY() + 6 + 10 - 10 * page.getShowing().get(), 16, 16);
			Glow.draw(matrixStack, pageRect, offset+5, 0.4f * page.getSelectionGlow().get(), 10 + offset, color, color, color, color);
			Render.image("icons/mainmenu/" + page.getIcon() + ".png", pageRect.getX(), pageRect.getY(), pageRect.getWidth(), pageRect.getHeight(), text.move(color, page.getSelection().get()).alpha(anim * page.getShowing().get()));
			
			xOff += 25;
		}
		Stencil.finish();
		
		if (alphaAnim.finished(false) && target != null) {
			mc.displayGuiScreen(target);
			target = null;
		}
		
	}
	
	public boolean clicked(double mouseX, double mouseY, int button) {
		float width = 155;
		float height = 29;
		Rect rect = new Rect(sr.getScaledWidth()/2F - width/2F, sr.getScaledHeight() - 10 - height, width, height);

		float xOff = 0;
		for (Page page : Page.values()) {
			if (Hover.isHovered(rect.getX() + 7 + xOff, rect.getY() + 6, 16, 16, mouseX, mouseY) && mc.currentScreen != page.getScreen()) {
				target = page.getScreen();
				page.getSelectionGlow().setForward(true);
			}
			xOff += 25;
		}
		return false;
	}
	
	public void keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == 263) {
			target = Page.getPrevious(mc.currentScreen).getScreen();
		} else if (keyCode == 262) {
			target = Page.getNext(mc.currentScreen).getScreen();
		}
	}

}
