package fun.rockstarity.api.constuctor.window;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.ui.rect.Rect;
import lombok.Getter;

/**
 * @author ConeTin
 * @since 8 РґРµРє. 2023 Рі.
 */

public class Window extends Rect {
	
	@Getter
	protected final Animation opening = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300).setSize(1);
	protected FixColor black, bgColor, actionsColor;
	
	public Window(float x, float y, float width, float height) {
		super(x, y, width, height);
	}
	
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		
	}
	
	public boolean clicked(double mouseX, double mouseY, int button) {
		return true;
	}
	
}
