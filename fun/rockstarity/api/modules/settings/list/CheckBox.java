package fun.rockstarity.api.modules.settings.list;

import java.util.Optional;
import java.util.function.Supplier;

import fun.rockstarity.api.binds.Bind;
import fun.rockstarity.api.binds.BindInfo.Type;
import fun.rockstarity.api.binds.BindType;
import fun.rockstarity.api.binds.Bindable;
import fun.rockstarity.api.helpers.game.Binds;
import fun.rockstarity.api.helpers.system.TextUtility;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.Setting;
import fun.rockstarity.api.modules.settings.list.Select.Element;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import fun.rockstarity.api.render.ui.clickgui.esp.ESPElement;
import fun.rockstarity.client.modules.render.Interface;
import fun.rockstarity.client.modules.render.Interface.UIElement;

/**
 * @author ConeTin
 * @since 5 дек. 2023 г.
 */

public class CheckBox extends Setting {
	
	private boolean enabled, bindEnabled, prevEnabled, ifEnabled = true;
	private Runnable onEnable, onDisable;
	
	private Animation enableAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300), bindAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);

	public CheckBox(Bindable parent, String name) {
		super(parent, name);
	}
	
	public Animation getEnableAnim(boolean bind) {
		return bind ? enableAnim : bindAnim;
	}
	
	public CheckBox bind(boolean enabled) {
		this.bindEnabled = enabled;
		
		return this;
	}
	
	public boolean bind() {
		return this.bindEnabled && !this.isHide();
	}

	public CheckBox ifEnabled(boolean ifEnabled) {
		this.ifEnabled = ifEnabled;
		return this;
	}
	
	@Override
	public void reset() {
		set(false);
		bind(false);
	    super.reset();
	}
	
	public boolean ifEnabled() {
		return ifEnabled;
	}
	
	public CheckBox set(boolean enabled) {
		this.enabled = enabled;
		if (enabled)
			this.call(onEnable);
		else
			this.call(onDisable);
		return this;
	}
	
	private void call(Runnable run) {
		if (run != null) run.run();
	}
	
	public boolean get() {
		return this.enabled && !this.isHide();
	}
	
	public CheckBox onEnable(Runnable val) {
		this.onEnable = val;
		return this;
	}

	public CheckBox onDisable(Runnable val) {
		this.onDisable = val;
		return this;
	}
	
	@Override
	public void toggleWithBind(Optional<Bind> opt) {
		Bind bind = opt.get();
		
		if (isHide()) return;
		
		this.toggled = !this.toggled;
		
		String gender = TextUtility.makeGender(getName());
		
		String parentName = "?";
		
		if (parent instanceof Module mod) {
			parentName = mod.getInfo().name();
		}
		
		if (parent instanceof UIElement ui) {
			parentName = ui.getName();
		}

		if (parent instanceof ESPElement ui) {
			parentName = ui.getName();
		}

		if (parent instanceof Setting ui) {
			parentName = ui.getName();
		}

		if (parent instanceof Select.Element ui) {
			parentName = ui.getName();
		}

		if (parent instanceof Mode.Element ui) {
			parentName = ui.getName();
		}
		
		if (bind.getType() != BindType.HOLD || bind.isHolding()) {
			if (toggled) {
				this.prevEnabled = this.get();
				this.set(!this.get());
				bind.setAlert(rock.getAlertHandler().alert(parentName + " -> " + getName() + " переключен" + gender, bind.getType() == BindType.HOLD ? AlertType.WAIT : get() ? AlertType.SUCCESS : AlertType.ERROR));
			} else {
				this.set(this.prevEnabled);
				bind.setAlert(rock.getAlertHandler().alert(parentName + " -> " + getName() + " переключен" + gender, bind.getType() == BindType.HOLD ? AlertType.WAIT : get() ? AlertType.SUCCESS : AlertType.ERROR));
			}
			bind.getAlert().setBindable(this);
			bind.getAlert().setBind(bind);
		} else {
			this.set(this.prevEnabled);
			if (bind.getAlert() != null)
				bind.getAlert().hide();
		}
		
		rock.getModules().get(Interface.class).getKeyBinds().updateBind(bind, get(), "");
	}
	
	public CheckBox hide(Supplier<Boolean> hide) {
		this.hide = hide;
		return this;
	}
	
	public CheckBox desc(String desc) {
		this.desc = desc;
		return this;
	}
	
}
