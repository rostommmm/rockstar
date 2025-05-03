package fun.rockstarity.api.render.ui.mainmenu.banner;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.helpers.math.Hover;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.ui.rect.Rect;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

/**
 * @author ConeTin
 * @since 28 мар. 2025 г.
 */

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Banner {

	String png;
	Runnable onClick;
	float height;
	@NonFinal Rect rect;
	@NonFinal Animation hover = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	
	public void render(MatrixStack matrixStack, Rect rect, int mouseX, int mouseY, float alpha) {
		hover.setForward(Hover.isHovered(rect, mouseX, mouseY) && onClick != null);
		
		Render.scale(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2, 1 - 0.05f * hover.get());
		Render.image(png, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), FixColor.WHITE.alpha(alpha));
		Render.end();
		
		this.rect = rect;
	}
	
	public void mouseClicked(double mouseX, double mouseY, int button) {
		if (Hover.isHovered(rect, mouseX, mouseY) && button == 0 && onClick != null) {
			onClick.run();
		}
	}
	
}
