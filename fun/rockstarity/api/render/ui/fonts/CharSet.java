package fun.rockstarity.api.render.ui.fonts;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.render.ui.rect.Rect;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;

/**
 * @author ConeTin
 * @since 4 дек. 2023 г.
 */

public final class CharSet implements IAccess {
	private Map<Character, Rect> rectsCache = new HashMap<>();
	protected final Map<Character, Rect> rects = new HashMap<>();
	protected int texId, imgWidth, imgHeight;
	protected float fontHeight;
	float spacing = 2;

	public CharSet(Font font, char[] chars) {
		FontRenderContext fontRenderContext = new FontRenderContext(font.getTransform(), true, true);
		double maxWidth = 0;
		double maxHeight = 0;
		
		for(char c : chars) {
			Rectangle2D bound = font.getStringBounds(Character.toString(c), fontRenderContext);
			maxWidth = Math.max(maxWidth, bound.getWidth());
			maxHeight = Math.max(maxHeight, bound.getHeight());
		}
		
		int d = (int)Math.ceil(Math.sqrt((maxHeight + 2) * (maxWidth + 2) * chars.length));
		
		this.fontHeight = (float)(maxHeight / 2);
		this.imgHeight = d;
		this.imgWidth = d;
		
		BufferedImage image = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		
		graphics.setFont(font);
		graphics.setColor(new Color(255, 255, 255, 0));
		graphics.fillRect(0, 0, imgWidth, imgHeight);
		graphics.setColor(Color.WHITE);
		graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,	RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		
		FontMetrics fontMetrics = graphics.getFontMetrics();
		int posX = 1;
		int posY = 2;
		
		for(char c : chars) {
			Rect rect = new Rect();
			Rectangle2D bounds = fontMetrics.getStringBounds(Character.toString(c), graphics);
			rect.setWidth((float) (bounds.getWidth() + 1));
			rect.setHeight((float) (bounds.getHeight() + 2));

			if(posX + rect.getWidth() >= imgWidth) {
				posX = 1;
				posY += maxHeight + fontMetrics.getDescent() + 2;
			}

			rect.setX(posX);
			rect.setY(posY);

			graphics.drawString(Character.toString(c), posX, posY + fontMetrics.getAscent());

			posX += rect.getWidth() + 4;
			rects.put(c, rect);
		}

		RenderSystem.recordRenderCall(() -> texId = Render.loadTexture(image));
	}

	public float renderGlyph(Matrix4f matrix, char c, float x, float y, float red, float green, float blue, float alpha) {
		GlStateManager.bindTexture(texId);
		
		Rect rect = rectsCache.computeIfAbsent(c, this::getRect);

	    if (rect == null)
	        return 0;
		
		float pageX = rect.getX() / (float) imgWidth;
		float pageY = rect.getY() / (float) imgHeight;
		float width = rect.getWidth();
		float height = rect.getHeight();
		float pageWidth = width / (float) imgWidth;
		float pageHeight = height / (float) imgHeight;
		
		BUILDER.pos(matrix, x, y + height, 0).color(red, green, blue, alpha).tex(pageX, pageY + pageHeight).endVertex();
		BUILDER.pos(matrix, x + width, y + height, 0).color(red, green, blue, alpha).tex(pageX + pageWidth, pageY + pageHeight).endVertex();
		BUILDER.pos(matrix, x + width, y, 0).color(red, green, blue, alpha).tex(pageX + pageWidth, pageY).endVertex();
		BUILDER.pos(matrix, x, y, 0).color(red, green, blue, alpha).tex(pageX, pageY).endVertex();
		
		
		return width + spacing;
	}
	
	private Rect getRect(char c) {
	    Rect rect = rects.get(c);
	    return rect != null ? rect : null; // Use a default if the rect is not found
	}

}
