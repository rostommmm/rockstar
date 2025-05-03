package fun.rockstarity.api.render.ui.rect;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author ConeTin
 * @since 3 дек. 2023 г.
 */

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Rect {

	public static final Rect EMPTY = new Rect(0,0,0,0);
	protected float x, y, width, height;
	
	public void set(float x, float y, float width, float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public Rect x(float x) {
		return new Rect(x,y,width,height);
	}
	
	public Rect y(float y) {
		return new Rect(x,y,width,height);
	}
	
	public Rect width(float width) {
		return new Rect(x,y,width,height);
	}
	
	public Rect height(float height) {
		return new Rect(x,y,width,height);
	}
	
	public Rect size(float off) {
		return new Rect(x+off, y+off, width-off*2, height-off*2);
	}
	
	public static Rect interpolate(Rect oldValue, Rect newValue, double interpolationValue) {
        float interpolatedX = (float) (oldValue.x + (newValue.x - oldValue.x) * interpolationValue);
        float interpolatedY = (float) (oldValue.y + (newValue.y - oldValue.y) * interpolationValue);
        float interpolatedWidth = (float) (oldValue.width + (newValue.width - oldValue.width) * interpolationValue);
        float interpolatedHeight = (float) (oldValue.height + (newValue.height - oldValue.height) * interpolationValue);
        return new Rect(interpolatedX, interpolatedY, interpolatedWidth, interpolatedHeight);
    }
	
	public boolean contains(Rect parent) {
		return contains(parent.getX(), parent.getY(), parent.getWidth(), parent.getHeight());
	}
	
	public boolean contains(float x, float y, float width, float height) {
        return this.x + this.width 	> x
            && this.x 				< x + width
            && this.y + this.height > y
            && this.y 				< y + height;
	}
	
	public boolean inside(Rect parent) {
		return inside(parent.getX(), parent.getY(), parent.getWidth(), parent.getHeight());
	}
	
	public boolean inside(float x, float y, float width, float height) {
        return this.x 				> x
            && this.x + this.width 	< x + width
            && this.y 				> y
            && this.y + this.height	< y + height;
	}
	
}
