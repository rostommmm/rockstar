package fun.rockstarity.api.render.color;

import java.awt.Color;

import fun.rockstarity.api.helpers.math.MathUtility;
//import fun.rockstarity.api.helpers.scripts.base.ColorBase;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.scripts.wrappers.base.ColorBase;
import lombok.Getter;

/**
 * @author ConeTin
 * @since 4 дек. 2023 г.
 */

public class FixColor extends Color {
	
	public static final FixColor BLACK = new FixColor(0,0,0);
	public static final FixColor WHITE = new FixColor(255,255,255);
	public static final FixColor CYAN = new FixColor(0, 255, 255);
	public static final FixColor RED = new FixColor(255, 0, 0);
	public static final FixColor BLUE = new FixColor(0, 0, 255);
	public static final FixColor YELLOW = new FixColor(255, 255, 0);
	public static final FixColor GREEN = new FixColor(0, 255, 0);
	public static final FixColor ORANGE = new FixColor(255, 165, 0);
	public static final FixColor GRAY = new FixColor(128, 128, 128);
	public static final FixColor TRANSPARENT = new FixColor(0,0,0,0);
	
	@Getter
	private final Animation selectAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);

	public FixColor(double r, double g, double b, double a) {
		super(fix(r), fix(g), fix(b), fix(a));
	}
	
	public FixColor(double r, double g, double b) {
		super(fix(r), fix(g), fix(b));
	}
	
	public FixColor(int hex) {
		super(hex);
	}
	
	public FixColor(ColorBase base) {
		super(base.getRed(), base.getGreen(), base.getBlue(), base.getAlpha());
	}
	
	public FixColor(Color color) {
		super(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	}
	
	private static int fix(double val) {
		return (int) Math.max(0, Math.min(255, val));
	}
	
	public FixColor alpha(double alpha) {
		return new FixColor(this.getRed(), this.getGreen(), this.getBlue(), this.getAlpha() * alpha);
	}
	
	public FixColor darker(float FACTOR) {
        return new FixColor(fix((int) (this.getRed() - FACTOR * 255f)),
        		fix((int) (this.getGreen() - FACTOR * 255f)),
        		fix((int) (this.getBlue() - FACTOR * 255f)),
                this.getAlpha());
    }
	
	public FixColor move(Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));
        return new FixColor(MathUtility.interpolate(this.getRed(), color2.getRed(), amount),
                MathUtility.interpolate(this.getGreen(), color2.getGreen(), amount),
                MathUtility.interpolate(this.getBlue(), color2.getBlue(), amount),
                MathUtility.interpolate(this.getAlpha(), color2.getAlpha(), amount));
    }
	
	public Color getColor() {
		return new Color(getRed(), getGreen(), getBlue(), getAlpha());
	}
	
	public FixColor brighter(float factor) {
		int r = getRed();
        int g = getGreen();
        int b = getBlue();
        int alpha = getAlpha();

        /* From 2D group:
         * 1. black.brighter() should return grey
         * 2. applying brighter to blue will always return blue, brighter
         * 3. non pure color (non zero rgb) will eventually return white
         */
        int i = (int)(1.0/(1.0-factor));
        if ( r == 0 && g == 0 && b == 0) {
            return new FixColor(i, i, i, alpha);
        }
        if ( r > 0 && r < i ) r = i;
        if ( g > 0 && g < i ) g = i;
        if ( b > 0 && b < i ) b = i;

        return new FixColor(
        		Math.min((int)(r/factor), 255),
                Math.min((int)(g/factor), 255),
                Math.min((int)(b/factor), 255),
                alpha
        );
	}
	
	public float[] getRGBAf() {
		int r = getRed();
        int g = getGreen();
        int b = getBlue();
        int alpha = getAlpha();
        return new float[]{r/255F, g/255F, b/255F, alpha/255F};
    }

}
