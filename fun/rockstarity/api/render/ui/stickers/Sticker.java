package fun.rockstarity.api.render.ui.stickers;

import java.awt.Color;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.helpers.render.Converter;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.rect.Rect;

/**
 * @author ConeTin
 * @since 5 РґРµРє. 2023 Рі.
 */

public class Sticker {

	protected final Animation gif;
	private final String name;
	private final int size;
	
	
	public Sticker(String name, int size, int speed) {
		this.name = name;
		this.size = size;
		this.gif = new Animation().setEasing(Easing.LINEAR).setSpeed(speed).setSize(size-1);
	}
	
	public void render(MatrixStack matrixStack, float x, float y, float width, float height, Color color, Rect rect, FixColor colorBlack) {
		gif.setForward(true);
		if (gif.finished()) gif.getTimer().reset();
		gif.setSpeed(3000);
		Round.draw(matrixStack, rect, 0, colorBlack);
		Render.image(Converter.getFixed("client/gifs/" + name + "/" + (1+(int)gif.get()) + ".png"), x, y, width, height, color);
	}
	
}
