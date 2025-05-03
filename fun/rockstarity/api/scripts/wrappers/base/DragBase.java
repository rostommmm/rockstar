package fun.rockstarity.api.scripts.wrappers.base;

import fun.rockstarity.api.render.ui.draggables.Draggable;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.api.scripts.Script;

public class DragBase {
	private Draggable child;
	
	public DragBase(String name) {
		child = new Draggable(name, Rect.EMPTY);
		Script.getCurrent().getScriptDrags().add(child);
	}
	
	public DragBase(Draggable drag) {
		child = drag;
	}
	
	public DragBase set_x(float val) {
		child.setX(val);
		return this;
	}
	
	public DragBase set_y(float val) {
		child.setY(val);
		return this;
	}
	
	public DragBase set_width(float val) {
		child.setWidth(val);
		return this;
	}
	
	public DragBase set_height(float val) {
		child.setHeight(val);
		return this;
	}
	
	public boolean dragging() {
		return child.isDragging();
	}
	
	public float x() {
		return child.getX();
	}
	
	public float y() {
		return child.getY();
	}
	
	public float width() {
		return child.getWidth();
	}
	
	public float height() {
		return child.getHeight();
	}
	
	public String name() {
		return child.getName();
	}
}