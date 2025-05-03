package fun.rockstarity.api.helpers.render;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.rect.Rect;
import lombok.experimental.UtilityClass;
import net.minecraft.util.math.vector.Vector2f;

/**
 * @author ConeTin
 * @since 28 РѕРєС‚. 2024вЂЇРі.
 */

@UtilityClass
public class Loading {
	
	private final Vector2f left = Vector2f.ZERO, center = Vector2f.ZERO, right = Vector2f.ZERO;
	private final Animation first = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300), second = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	private boolean swap;

	public void render(MatrixStack matrixStack, float x, float y, float size) {
		first.setSpeed(400);
		second.setSpeed(400);

		if (!swap) {
			if (first.finished(false))
				first.setForward(true);
			
			if (first.finished())
				second.setForward(true);
			
			if (first.finished() && second.finished())
				swap = true;
		} else {
			if (second.finished())
				second.setForward(false);
			
			if (second.finished(false))
				first.setForward(false);
			
			if (first.finished(false) && second.finished(false))
				swap = false;
		}
		
		/*
		if (first.finished())
			first.setForward(false);
		else if (first.finished(false))
			first.setForward(true);
		 */
		
		float secondMiddle = (second.get() <= 0.5) ? (1 - (second.get() * 2)) : ((second.get() - 0.5f) * 2);
		float firstMiddle = (first.get() <= 0.5) ? (1 - (first.get() * 2)) : ((first.get() - 0.5f) * 2);
		
		/*
		{ // Р›РµРІР°СЏ
			Round.draw(matrixStack, new Rect(x + size * 2f * second.get(), y, size, size), size/2F, FixColor.WHITE);
		}
		
		{ // Р¦РµРЅС‚СЂР°Р»СЊРЅР°СЏ
			Round.draw(matrixStack, new Rect(x + size * 2 + size * 2f * first.get(), y, size, size), size/2F, FixColor.WHITE);
		}
		*/
		
		{ // Р›РµРІР°СЏ
			Round.draw(matrixStack, new Rect(x + size * 2f * second.get(), y + size/4f - size/4f*(secondMiddle), size, size/2f + size/2f*(secondMiddle)), size/4F+size/4F*(secondMiddle), FixColor.WHITE);
		}
		
		{ // Р¦РµРЅС‚СЂР°Р»СЊРЅР°СЏ
			Round.draw(matrixStack, new Rect(x + size * 2 + size * 2f * first.get(), y + size/4f - size/4f*(firstMiddle), size, size/2f + size/2f*(firstMiddle)), size/4F+size/4F*(firstMiddle), FixColor.WHITE);
		}
		
		{ // РџСЂР°РІР°СЏ
			float pointX = (float) Math.cos(Math.toRadians(-first.get() * 180F)) * size - size;
			float pointY = (float) Math.sin(Math.toRadians(-first.get() * 180F)) * size;
			
			if (first.finished() || !second.isForward() && !second.finished(false)) {
				pointX = (float) Math.cos(Math.toRadians(-second.get() * 180F)) * size - size * 3;
				pointY = (float) Math.sin(Math.toRadians(-second.get() * 180F)) * size;
			}
			
			Round.draw(matrixStack, new Rect(x + size * 4 + pointX, y + pointY, size, size), size/2f, FixColor.WHITE);
		}
	}

	
}
