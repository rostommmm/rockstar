package fun.rockstarity.api.helpers.render;

import java.util.ArrayList;

import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.render.color.FixColor;
import lombok.experimental.UtilityClass;

/**
 * @author ConeTin
 * @since 13 мар. 2024 г.
 */

@UtilityClass
public class ColorUtility {
	
	public int red(int c) {
        return (c >> 16) & 0xFF;
    }

    public int green(int c) {
        return (c >> 8) & 0xFF;
    }

    public int blue(int c) {
        return c & 0xFF;
    }

    public int alpha(int c) {
        return (c >> 24) & 0xFF;
    }

    public float redf(int c) {
        return red(c) / 255.0f;
    }

    public float greenf(int c) {
        return green(c) / 255.0f;
    }

    public float bluef(int c) {
        return blue(c) / 255.0f;
    }

    public float alphaf(int c) {
        return alpha(c) / 255.0f;
    }

    public int[] getRGBA(int c) {
        return new int[]{red(c), green(c), blue(c), alpha(c)};
    }

    public int[] getRGB(int c) {
        return new int[]{red(c), green(c), blue(c)};
    }

    public float[] getRGBAf(int c) {
        return new float[]{redf(c), greenf(c), bluef(c), alphaf(c)};
    }

    public float[] getRGBf(int c) {
        return new float[]{redf(c), greenf(c), bluef(c)};
    }

    public int getColor(float red, float green, float blue, float alpha) {
        return getColor(Math.round(red * 255), Math.round(green * 255), Math.round(blue * 255), Math.round(alpha * 255));
    }

    public int getColor(int red, int green, int blue, float alpha) {
        return getColor(red, green, blue, Math.round(alpha * 255));
    }

    public int getColor(float red, float green, float blue) {
        return getColor(red, green, blue, 1.0F);
    }

    public int getColor(int brightness, int alpha) {
        return getColor(brightness, brightness, brightness, alpha);
    }

    public int getColor(int brightness, float alpha) {
        return getColor(brightness, Math.round(alpha * 255));
    }

    public int getColor(int brightness) {
        return getColor(brightness, brightness, brightness);
    }

    public int replAlpha(int color, int alpha) {
        return getColor(red(color), green(color), blue(color), alpha);
    }
	
	public float[] getRGBAFloat(FixColor color) {
		return new float[] { color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F };
	}
	
	public FixColor gradient(int speed, int index, FixColor... colors) {
	    int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
	    angle = angle >= 180 ? 360 - angle : angle;

	    float position = angle / 180f;
	    int segment = Math.min((int) (position * (colors.length - 1)), colors.length - 2);

	    float fraction = position * (colors.length - 1) - segment;

	    return interpolateColor(colors[segment], colors[segment + 1], fraction);
	}
	
	public FixColor gradient(int speed, int index, ArrayList<FixColor> colors) {
	    int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
	    angle = angle >= 180 ? 360 - angle : angle;

	    float position = angle / 180f;
	    int segment = Math.min((int) (position * (colors.size() - 1)), colors.size() - 2);

	    float fraction = position * (colors.size() - 1) - segment;

	    return interpolateColor(colors.get(segment), colors.get(segment + 1), fraction);
	}

	public FixColor interpolateColor(FixColor color1, FixColor color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));
        return new FixColor(MathUtility.interpolate(color1.getRed(), color2.getRed(), amount),
        		MathUtility.interpolate(color1.getGreen(), color2.getGreen(), amount),
        		MathUtility.interpolate(color1.getBlue(), color2.getBlue(), amount),
        		MathUtility.interpolate(color1.getAlpha(), color2.getAlpha(), amount));
    }

}
