package fun.rockstarity.api.modules.settings.list;

import java.util.ArrayList;
import java.util.function.Supplier;

import fun.rockstarity.api.binds.Bindable;
import fun.rockstarity.api.helpers.render.ColorUtility;
import fun.rockstarity.api.modules.settings.Setting;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Style;
import lombok.Getter;
import lombok.Setter;

/**
 * @author ConeTin
 * @since 5 дек. 2023 г.
 */

public class ColorPicker extends Setting {
	
	@Getter @Setter
	private boolean client = true;
	
	private final ArrayList<FixColor> colors = new ArrayList<>();
	
	public ColorPicker(Bindable parent, String name) {
		super(parent, name);
		this.colors.add(FixColor.RED);
	}
	
	public ColorPicker set(boolean client) {
		this.client = client;
		return this;
	}
	
	public ColorPicker add(FixColor... colors) {
		for (FixColor color : colors) {
			this.colors.add(color);
		}
		return this;
	}
	
	public FixColor get() {
		return get(1);
	}
	
	public FixColor get(int index) {
		if (this.colors.isEmpty() || client)
			return Style.getPoint(index);
		
		return colors.size() > 1 ? ColorUtility.gradient(5 * this.colors.size(), index, this.colors) : colors.get(0);
	}
	
	public ArrayList<FixColor> getColors() {
		return this.colors;
	}

	public ColorPicker hide(Supplier<Boolean> hide) {
		this.hide = hide;
		return this;
	}
	
	public ColorPicker desc(String desc) {
		this.desc = desc;
		return this;
	}
	
}
