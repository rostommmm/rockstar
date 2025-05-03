package fun.rockstarity.api.render.shaders.fog;

import com.ibm.icu.impl.Pair;

import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.render.color.themes.Theme;
import lombok.Getter;

@Getter
public class ColorWheel {
	
    FixColor color1, color2, color3, color4;

    public ColorWheel() {
        color1 = color2 = color3 = color4 = FixColor.BLACK;
    }

    public void update() {
    	color1 = Style.getPoint(0);
		color2 = Style.getPoint(90);
		color3 = Style.getPoint(180);
		color4 = Style.getPoint(270);
		
		/*
		 * ColorUtility.gradient(10, 1, FixColor.RED, FixColor.BLUE, FixColor.YELLOW, FixColor.GREEN)
		 */
    }
    
}