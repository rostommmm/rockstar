package fun.rockstarity.api.helpers.math;

import fun.rockstarity.api.render.ui.rect.Rect;

public class Hover {
	public static boolean isHovered(double x, double y, double width, double height, double mouseX, double mouseY) {
		return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
	}
	
	public static boolean isHovered(Rect rect, double mouseX, double mouseY) {
		if (rect == null) return false;
		return mouseX >= rect.getX() && mouseY >= rect.getY() && mouseX < rect.getX() + rect.getWidth() && mouseY < rect.getY() + rect.getHeight();
	}
	
	public static float getSliderValue(float min, float max, float start, float size, double mouseX) {
		return (float) (Math.min(1, Math.max(0, (mouseX - start) / size)) * (max - min)) + min;
	}
	
	public static float getPercent(float value, float min, float max) {
		return (value - min) / (max - min);
	}
}
