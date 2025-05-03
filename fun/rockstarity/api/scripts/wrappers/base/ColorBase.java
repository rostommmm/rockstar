package fun.rockstarity.api.scripts.wrappers.base;

import java.awt.Color;

import fun.rockstarity.api.render.color.FixColor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ColorBase {
	
	public final static ColorBase WHITE = new ColorBase(0xFFFFFFFF);
	public final static ColorBase LIGHT_GRAY = new ColorBase(0xFFC0C0C0);
	public final static ColorBase GRAY = new ColorBase(0xFF808080);
	public final static ColorBase DARK_GRAY = new ColorBase(0xFF404040);
	public final static ColorBase BLACK = new ColorBase(0xFF000000);
	public final static ColorBase RED = new ColorBase(0xFFFF0000);
	public final static ColorBase PINK = new ColorBase(0xFFFFAFAF);
	public final static ColorBase ORANGE = new ColorBase(0xFFFFC800);
	public final static ColorBase YELLOW = new ColorBase(0xFFFFFF00);
	public final static ColorBase GREEN = new ColorBase(0xFF00FF00);
	public final static ColorBase MAGENTA = new ColorBase(0xFFFF00FF);
	public final static ColorBase CYAN = new ColorBase(0xFF00FFFF);
	public final static ColorBase BLUE = new ColorBase(0xFF0000FF);
	
	private int red, green, blue, alpha;
	
	public ColorBase(double red, double green, double blue, double alpha) {
		this.red = (int) (red * 255D);
		this.green = (int) (green * 255D);
		this.blue = (int) (blue * 255F);
		this.alpha = (int) (alpha * 255D);
	}
	
	public ColorBase(double red, double green, double blue) {
		this(red, green, blue, 1);
	}
	
	public ColorBase(int hex) {
		Color color = new Color(hex);
		this.red = color.getRed();
		this.green = color.getGreen();
		this.blue = color.getBlue();
		this.alpha = color.getAlpha();
	}
	
	public ColorBase(FixColor color) {
		red = color.getRed();
		green = color.getGreen();
		blue = color.getBlue();
		alpha = color.getAlpha();
	}

	public int getRGB() {
		return new Color(red,green,blue,alpha).getRGB();
	}
	
	public float red() {
		return red / 255F;
	}
	
	public float green() {
		return green / 255F;
	}
	
	public float blue() {
		return blue / 255F;
	}
	
}