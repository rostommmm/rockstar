package fun.rockstarity.api.modules.settings;

import java.util.ArrayList;
import java.util.function.Supplier;

import fun.rockstarity.api.binds.Bindable;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.ui.clickgui.esp.ESPElement;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.client.modules.render.Interface.UIElement;
import lombok.Getter;
import lombok.Setter;

/**
 * @author ConeTin
 * @since 5 дек. 2023 г.
 */

@Getter
public class Setting extends Bindable {

	private final ArrayList<Setting> settings = new ArrayList<>();
	protected Supplier<Boolean> hide = () -> false;
	protected final Bindable parent;
	private final String name;
	protected String desc;
	
	@Setter
	private Rect settingRect = Rect.EMPTY;
	private Animation settingsAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);

	private float height = 28, bindHeight = 28;
	
	public Setting(Bindable parent, String name) {
		this.parent = parent;
		this.name = name;
		
		if (parent instanceof Module mod) {
			mod.getSettings().add(this);
			return;
		}
		
		if (parent instanceof UIElement ui) {
			ui.getSettings().add(this);
			return;
		}

		if (parent instanceof ESPElement ui) {
			ui.getSettings().add(this);
			return;
		}

		if (parent instanceof Setting ui) {
			ui.getSettings().add(this);
			return;
		}

		if (parent instanceof Select.Element ui) {
			ui.getSettings().add(this);
			return;
		}

		if (parent instanceof Mode.Element ui) {
			ui.getSettings().add(this);
			return;
		}
	}
	
	public boolean canSettings() {
		return !settingsAnim.finished(false);
	}
	
	public boolean hasSettings() {
		return !settings.isEmpty();
	}
	
	public void setHeight(float val) {
		this.setHeight(val, false);
	}
	
	public void reset() {
	    for (Setting setting : settings) {
	        setting.reset();
	    }
	    getBinds().clear();
	}
	
	public void setHeight(float val, boolean bind) {
		if (bind)
			bindHeight = val;
		else
			height = val;
	}
	
	public Setting hide(Supplier<Boolean> hide) {
		this.hide = hide;
		return this;
	}

	public Setting desc(String desc) {
		this.desc = desc;
		return this;
	}
	
	public boolean isHide() {
		return hide.get();
	}
	
}
