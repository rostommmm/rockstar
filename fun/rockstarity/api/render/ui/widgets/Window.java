package fun.rockstarity.api.render.ui.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.math.Hover;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.cursor.CursorType;
import fun.rockstarity.api.render.cursor.CursorUtility;
import fun.rockstarity.api.render.ui.rect.Rect;
import lombok.Getter;

/**
 * @author ConeTin
 * @since 12 дек. 2023 г.
 */

public class Window extends Rect implements IAccess {
	
	protected FixColor black, bgColor, actionsColor, text;
	@Getter
	protected final Animation opening = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300).setSize(1).setForward(false).finish();

	public Window(float x, float y, float width, float height) {
		super(x, y, width, height);
		opening.setForward(true);
	}
	
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		black = FixColor.BLACK.alpha(opening.get());
		text = rock.getThemes().getTextFirstColor().alpha(opening.get());
		bgColor = rock.getThemes().getFirstColor().alpha(opening.get());
		actionsColor = rock.getThemes().getThirdColor().alpha(opening.get());
		
		if (Hover.isHovered(this, mouseX, mouseY)) {
			CursorUtility.setType(CursorType.HAND);
		}
	}
	
	public void tick() {}
	
	public void charTyped(char codePoint, int modifiers) {}
	
	public boolean clicked(double mouseX, double mouseY, int button) {
		if (!Hover.isHovered(x, y, width, height, mouseX, mouseY)) {
			if (this.opening.finished())
			opening.setForward(false);
			return false;
		} else {
			return true;
		}
	}
	
	public void update() {}
	
	public boolean pressed(int keyCode, int scanCode, int modifiers) {
		return false;
	}
	
	public boolean dragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		return false;
	}
	
	public boolean released(double mouseX, double mouseY, int button) {
		return false;
	}
	
	public void scrolled(double mouseX, double mouseY, double delta) {}
	
	public boolean canClick(double mouseX, double mouseY) {
		return !Hover.isHovered(this, mouseX, mouseY);
	}
	
}
