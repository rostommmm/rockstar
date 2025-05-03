package fun.rockstarity.api.render.ui.mainmenu.alt;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.ui.fonts.FontSize;
import fun.rockstarity.api.render.ui.rect.Rect;

/**
 * @author ConeTin
 * @since 31 Р°РІРі. 2024вЂЇРі.
 */

public class TextBlock extends Rect {
	
    public void render(MatrixStack matrices, String text, FontSize font, FixColor color) {
    	String[] words = text.split(" ");
    	
    	float yOff = 0;
    	float xOff = 0;
    	int i = 1;
    	
    	for (String word : words) {
    		float prev = xOff;
    		xOff += font.getWidth(word + " ");
    		
    		if (xOff < width) {
    			font.draw(matrices, word, x + prev, y + yOff, color);
    		} else {
    			yOff += font.getHeight();
    			xOff = 0;
    			
    			font.draw(matrices, word, x + xOff, y + yOff, color);
    			xOff += font.getWidth(word + " ");
    		}
    		
    		i++;
    	}
    	
    	height = yOff;
    }

    public float calcHeight(String text, FontSize font) {
    	String[] words = text.split(" ");
    	
    	float yOff = 0;
    	float xOff = 0;
    	int i = 1;
    	
    	for (String word : words) {
    		float prev = xOff;
    		xOff += font.getWidth(word + " ");
    		
    		if (xOff < width) {
    		} else {
    			yOff += font.getHeight();
    			xOff = 0;
    			
    			xOff += font.getWidth(word + " ");
    		}
    		
    		i++;
    	}

        return yOff;
    }

    
}
