package fun.rockstarity.api.render.ui.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.math.Hover;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.cursor.CursorType;
import fun.rockstarity.api.render.cursor.CursorUtility;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.fonts.FontSize;
import fun.rockstarity.api.render.ui.mainmenu.Page;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.client.modules.render.Interface;
import lombok.experimental.UtilityClass;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.gui.widget.button.LockIconButton;

/**
 * @author ConeTin
 * @since 4 июл. 2024 г.
 */

@UtilityClass
public class CustomButtonRenderer implements IAccess {

	public void renderButton(Button button, MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		float anim = mc.currentScreen == Page.ALT.getScreen() || mc.currentScreen == Page.SETTINGS.getScreen() || mc.currentScreen == Page.SINGE.getScreen()|| mc.currentScreen == Page.MULTI.getScreen()
				? partialTicks : mc.currentScreen instanceof ChatScreen e 
				? e.getAlphaAnim().get()
				: 1;
		float x = button.x;
		float y = button.y;
		float width = button.getWidth();
		float height = button.getHeight();
		
		Rect rect = new Rect(x,y,width,height);
		
		button.getHoverAnim().setForward(Hover.isHovered(rect, mouseX, mouseY));
		
		FontSize font = bold.get(16);
		
		if (Hover.isHovered(rect, mouseX, mouseY)) {
			CursorUtility.setType(CursorType.HAND);
		}
		
		FixColor[] circle = Interface.getCircle(anim * button.getHoverAnim().get() * 0.3f);
		
		Round.draw(matrixStack, rect, 3, rock.getThemes().getFirstColor().alpha(anim));

		Render.scale(x + width/2F, y + height/2F, 0.5f + button.getHoverAnim().get() * 0.5f);
		Round.draw(matrixStack, rect, 3, circle[0], circle[1], circle[2], circle[3]);
		Render.end();
		font.draw(matrixStack, button.getMessage().getString(), x + width / 2 - font.getWidth(button.getMessage().getString()) / 2, y + height / 2 - 6, rock.getThemes().getTextFirstColor().alpha(anim));
	}
	
	public void renderSliderBackground(AbstractSlider button, MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		float anim = mc.currentScreen == Page.ALT.getScreen() || mc.currentScreen == Page.SETTINGS.getScreen() || mc.currentScreen == Page.SINGE.getScreen() || mc.currentScreen == Page.MULTI.getScreen() ? partialTicks : 1;
		
		float x = button.x;
		float y = button.y;
		float width = button.getWidth();
		float height = button.getHeight();
		
		Rect rect = new Rect(x,y,width,height);
		
		button.getHoverAnim().setForward(Hover.isHovered(rect, mouseX, mouseY));
		
		FontSize font = bold.get(16);
		
		Round.draw(matrixStack, rect, 3, rock.getThemes().getFirstColor().alpha(anim - 0.3f * button.getHoverAnim().get()));
		
		Round.draw(matrixStack, new Rect(button.x + (int)(button.sliderValue * (double)(width - 8)),y,8,height), 3, rock.getThemes().getThirdColor().move(rock.getThemes().getTextFirstColor(), 0.1f).alpha(anim));
		
		if (Hover.isHovered(rect, mouseX, mouseY)) {
			CursorUtility.setType(CursorType.ARROW_HORIZONTAL);
		}
		
		font.draw(matrixStack, button.getMessage().getString(), x + width / 2 - font.getWidth(button.getMessage().getString()) / 2, y + height / 2 - 6, rock.getThemes().getTextFirstColor().alpha(anim));
	}
	
	public void renderLockBackground(LockIconButton button, MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		float anim = mc.currentScreen == Page.ALT.getScreen() || mc.currentScreen == Page.SETTINGS.getScreen() || mc.currentScreen == Page.SINGE.getScreen() || mc.currentScreen == Page.MULTI.getScreen() ? partialTicks : 1;
		
		float x = button.x;
		float y = button.y;
		float width = button.getWidth();
		float height = button.getHeight();
		
		Rect rect = new Rect(x,y,width,height);
		
		button.getHoverAnim().setForward(Hover.isHovered(rect, mouseX, mouseY));
		
		if (Hover.isHovered(rect, mouseX, mouseY)) {
			CursorUtility.setType(CursorType.HAND);
		}
		
		Round.draw(matrixStack, rect, 3, rock.getThemes().getFirstColor().alpha(anim - 0.3f * button.getHoverAnim().get()));
	}
	
	public void renderImageBackground(ImageButton button, MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		float anim = mc.currentScreen == Page.ALT.getScreen() || mc.currentScreen == Page.SETTINGS.getScreen() || mc.currentScreen == Page.SINGE.getScreen() || mc.currentScreen == Page.MULTI.getScreen() ? partialTicks : 1;
		
		float x = button.x;
		float y = button.y;
		float width = button.getWidth();
		float height = button.getHeight();
		
		Rect rect = new Rect(x,y,width,height);
		
		button.getHoverAnim().setForward(Hover.isHovered(rect, mouseX, mouseY));
		
		if (Hover.isHovered(rect, mouseX, mouseY)) {
			CursorUtility.setType(CursorType.HAND);
		}
		
		Round.draw(matrixStack, rect, 3, rock.getThemes().getFirstColor().alpha(anim - 0.3f * button.getHoverAnim().get()));
	}
	
}
