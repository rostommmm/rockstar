package fun.rockstarity.api.render.ui.fonts;

import java.awt.Font;
import java.io.File;
import java.util.HashMap;
import java.util.Locale;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.render.Converter;
import fun.rockstarity.api.helpers.secure.Web;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.ui.rect.Rect;
import lombok.Getter;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

/**
 * @author ConeTin
 * @since 4 дек. 2023 г.
 */

public class FontSize implements IAccess {
	
	private static boolean FLAT_RENDERER;
	private final static HashMap<String, Float> widthMap = new HashMap<>();

	private final CharSet charSet;
	@Getter private final int size;
	
	public FontSize(String name, int size) {
		int[] codes = new int[] {31, 127, 1024, 1106};
		char[] chars = new char[(codes[1] - codes[0] + codes[3] - codes[2])];
		
		int c = 0;
		for (int d = 0; d <= 2; d += 2) {
			for(int i = codes[d]; i <= codes[d + 1] - 1; i++) {
				chars[c] = (char) i;
				c++;
			}
		}
		this.size = size;
		this.charSet = new CharSet(Web.getFont(name, Font.PLAIN, size), chars);
	}
	
	public FontSize(Font font, int size) {
		int[] codes = new int[] {31, 127, 1024, 1106};
		char[] chars = new char[(codes[1] - codes[0] + codes[3] - codes[2])];
		
		int c = 0;
		for (int d = 0; d <= 2; d += 2) {
			for(int i = codes[d]; i <= codes[d + 1] - 1; i++) {
				chars[c] = (char) i;
				c++;
			}
		}
		this.size = size;
		this.charSet = new CharSet(font, chars);
	}
	
	public FontSize(File file, int size) {
		int[] codes = new int[] {31, 127, 1024, 1106};
		char[] chars = new char[(codes[1] - codes[0] + codes[3] - codes[2])];
		
		int c = 0;
		for (int d = 0; d <= 2; d += 2) {
			for(int i = codes[d]; i <= codes[d + 1] - 1; i++) {
				chars[c] = (char) i;
				c++;
			}
		}
		this.size = size;
		this.charSet = new CharSet(Converter.getFont(file, Font.PLAIN, size), chars);
	}
	
	public static final String STYLE_CODES = "0123456789abcdefklmnor";
	public static final int[] COLOR_CODES = new int[32];
	
	static {
		for (int i = 0; i < 32; ++i) {
			int j = (i >> 3 & 1) * 85;
			int k = (i >> 2 & 1) * 170 + j;
			int l = (i >> 1 & 1) * 170 + j;
			int i1 = (i & 1) * 170 + j;

			if (i == 6) {
				k += 85;
			}

			if (i >= 16) {
				k /= 4;
				l /= 4;
				i1 /= 4;
			}

			COLOR_CODES[i] = (k & 255) << 16 | (l & 255) << 8 | i1 & 255;
		}
	}
	
	public Rect draw(MatrixStack matrices, String text, float x, float y, FixColor color) {
		return renderString(matrices, text, x, y, color);
	}
	
	/**
	 * Объясняю:
	 * В начале большого блока с текстами пишешь font.start()
	 * В конце блока пишешь font.end()
	 * 
	 * Это позволяет рендерить большой блок текста в 1 запрос рендера, 
	 * что немножко оптимизует рендер текста
	 */
	public void start() {
		GlStateManager.enableBlend();
		BUILDER.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
		FLAT_RENDERER = true;
	}
	
	public void end() {
		TESSELLATOR.draw();
		GlStateManager.disableBlend();
		FLAT_RENDERER = false;
	}
	
	
	public float draw(MatrixStack matrices, ITextComponent text, float x, float y, FixColor color) {
		return drawStringMajestic(matrices, text, x, y, color, false);
	}
	
    public float drawStringMajestic(MatrixStack stack, ITextComponent text, double x, double y, FixColor color, boolean shadow) {
        float offset = 0;
        int i = 0;
        int removals = Server.isRW() ? 3 : 0;
        for (ITextComponent it : text.getSiblings()) {
            for (ITextComponent it1 : it.getSiblings()) {
            	String draw = it1.getString();
            	
                if (it1.getStyle().getColor() != null) this.renderString(stack, draw, (float) (x + offset), (float) y, new FixColor(it1.getStyle().getColor().getColor()));
                else this.renderString(stack, draw, (float) (x + offset), (float) y, color);
                
                offset += getWidth(draw) + 1f;
                i++;
            }

            if (it.getSiblings().size() <= 1) {
            	String draw = it.getString();
                
                this.renderString(stack, draw, (float) (x + offset), (float) y, it.getStyle().getColor() == null ? color : new FixColor(it.getStyle().getColor().getColor()));
                offset += getWidth(draw) + 1f;
                i++;
            }
        }
        
        if (text.getSiblings().isEmpty()) {
        	String draw = text.getString();
            
            this.renderString(stack, draw, (float) (x + offset), (float) y, text.getStyle().getColor() == null ? color : new FixColor(text.getStyle().getColor().getColor()));
            if (draw != null) offset += getWidth(draw) + 1f;
        }

        return 0;
    }
    
	private Rect renderString(MatrixStack matrices, String text, float x, float y, FixColor color) {
		if (text == null) return new Rect(0,0,0,0);
		
		float startPos = (float) x * 2.0f;
		float posX = startPos;
		float posY = (float) y * 2.0f;
		float[] colors = new float[] {color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f};
		//posY += size/3F;

		matrices.push();
		matrices.scale(0.5f, 0.5f, 1f);
		
		if (!FLAT_RENDERER) {
			GlStateManager.enableBlend();
			
			BUILDER.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
		}
		
		Matrix4f matrix = matrices.getLast().getMatrix();
		
		for(int i = 0; i < text.length(); i++) {
			char c0 = text.charAt(i);
			
			if (c0 == 167 && i + 1 < text.length() &&
					STYLE_CODES.indexOf(text.toLowerCase(Locale.ENGLISH).charAt(i + 1)) != -1) {
				int i1 = STYLE_CODES.indexOf(text.toLowerCase(Locale.ENGLISH).charAt(i + 1));

				if (i1 < 16) {
					int j1 = COLOR_CODES[i1];

					colors = new float[] {(float) (j1 >> 16 & 255) / 255.0F, (float) (j1 >> 8 & 255) / 255.0F, (float) (j1 & 255) / 255.0F, colors[3]};
				}
				
				i++;
			} else {
				float f = charSet.renderGlyph(matrix, c0, posX, posY, colors[0], colors[1], colors[2], colors[3]);
				posX += f * 0.82f;
			}
		}
		
		if (!FLAT_RENDERER) {
			TESSELLATOR.draw();
			
			GlStateManager.disableBlend();
		}
		
		matrices.pop();
		
		return new Rect(x,y,(posX - startPos) / 2.0f, size);
	}
	
	public float getWidth(ITextComponent text) {
        float offset = 0;
        int i = 0;
        int removals = Server.isRW() ? 3 : 0;
        for (ITextComponent it : text.getSiblings()) {
            for (ITextComponent it1 : it.getSiblings()) {
            	String draw = it1.getString();
                offset += getWidth(draw) + 1f;
                i++;
            }

            if (it.getSiblings().size() <= 1) {
            	String draw = it.getString();
                offset += getWidth(draw) + 1f;
                i++;
            }
        }
        
        if (text.getSiblings().isEmpty()) {
        	String draw = text.getString();
            if (draw != null) offset += getWidth(draw) + 1f;
        }
        
        return offset;
    }

	public float getWidth(String text) {
		if (text == null) return 0;
		
		//if (widthMap.containsKey(size+text)) {
		//	return widthMap.get(size+text);
		//} else {
			boolean bold = false;
			boolean italic = false;
			float width = 0.0f;
			
			for(int i = 0; i < text.length(); i++) {
				char c0 = text.charAt(i);

				if (c0 == 167 && i + 1 < text.length() &&
						 STYLE_CODES.indexOf(text.toLowerCase(Locale.ENGLISH).charAt(i + 1)) != -1) {
					int i1 = STYLE_CODES.indexOf(text.toLowerCase(Locale.ENGLISH).charAt(i + 1));

					if(i1 < 16) {
						bold = false;
						italic = false;
					} else if(i1 == 17) {
						bold = true;
					} else if(i1 == 20) {
						italic = true;
					} else if(i1 == 21) {
						bold = false;
						italic = false;
					}

					i ++;
				} else {
					width += charSet.rects.get(c0) == null ? 0 : (charSet.rects.get(c0).getWidth() + charSet.spacing) * 0.82f;
				}
			}
			
			widthMap.put(size+text, (width - charSet.spacing) / 2.0f);
			
			return (width - charSet.spacing) / 2.0f;
		//}
	}
	
	public float getHeight() {
		return size / 2F;
	}
	
}
