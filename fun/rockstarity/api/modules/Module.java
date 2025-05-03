package fun.rockstarity.api.modules;

import java.util.ArrayList;
import java.util.Optional;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.binds.Bind;
import fun.rockstarity.api.binds.BindType;
import fun.rockstarity.api.binds.Bindable;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.EventType;
import fun.rockstarity.api.events.IEventable;
import fun.rockstarity.api.events.list.game.client.EventToggle;
import fun.rockstarity.api.helpers.system.TextUtility;
import fun.rockstarity.api.modules.settings.Setting;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import fun.rockstarity.client.modules.render.ClickGui;
import fun.rockstarity.client.modules.render.Constructor;
import fun.rockstarity.client.modules.render.Interface;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author ConeTin
 * @since 2 дек. 2023 г.
 */

@NoArgsConstructor
public abstract class Module extends Bindable implements IEventable, IAccess {
	@Getter @Setter
	private ModuleInfo info;
	private boolean enabled;
	@Getter
	private int priority = 1;
	@Getter @Setter
	private ArrayList<Setting> settings = new ArrayList<>();
	@Getter
	private final Animation hover = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300),
			opened = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300),
			togglingAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	
	public Module(int priority) {
		this.priority = priority;
	}
	
	@Override
	public abstract void onEvent(Event event);
	@Override
	public void onAllEvent(Event event) {}
	
	public abstract void onEnable();
	
	public abstract void onDisable();
	
	public Module set(boolean enabled) {
		this.enabled = enabled;
		
		return this;
	}
	
	public boolean get() {
		return rock.isPanic() ? false : this.enabled;
	}
	
	public Module addBind(Bind bind) {
		getBinds().add(bind);
		
		return this;
	}
	
	public void toggle(boolean state, boolean silent) {
		this.enabled = state;
		
		String gender = TextUtility.makeGender(this.info.name());
		
		if (enabled) {
			onEnable();
			new EventToggle(this, true).hook();
			if (!(this instanceof ClickGui) && !(this instanceof Constructor)) 
				rock.getAlertHandler().alert(info.name() + " включен" + gender, AlertType.SUCCESS);
		} else {
			onDisable();
			new EventToggle(this, false).hook();
			if (!(this instanceof ClickGui) && !(this instanceof Constructor)) 
				rock.getAlertHandler().alert(info.name() + " выключен" + gender, AlertType.ERROR);
		}
		
		for (Bind bind : getBinds()) {
			if (bind.getType() == BindType.HOLD && bind.isHolding()) {
				bind.getAlert().hide();
			}
		}
	}
	
	@Override
	public void toggleWithBind(Optional<Bind> opt) {
		Bind bind = opt.get();
		
		this.enabled = !this.enabled;
		
		String gender = TextUtility.makeGender(this.info.name());
		
		if (bind.getType() != BindType.HOLD || bind.isHolding()) {
			if (enabled) {
				onEnable();
				new EventToggle(this, true).hook();
				if (!(this instanceof ClickGui) && !(this instanceof Constructor)) 
					bind.setAlert(rock.getAlertHandler().alert(info.name() + " включен" + gender, bind.getType() == BindType.HOLD ? AlertType.WAIT : AlertType.SUCCESS));
			} else {
				onDisable();
				new EventToggle(this, false).hook();
				bind.setAlert(rock.getAlertHandler().alert(info.name() + " выключен" + gender, bind.getType() == BindType.HOLD ? AlertType.WAIT : AlertType.ERROR));
			}
			if (bind.getAlert() != null) {
				bind.getAlert().setBindable(this);
				bind.getAlert().setBind(bind);
			}
		} else {
			if (bind.getAlert() != null)
				bind.getAlert().hide();
			onDisable();
			new EventToggle(this, false).hook();
		}
		
		if (!(this instanceof ClickGui))
			rock.getModules().get(Interface.class).getKeyBinds().updateBind(bind, true, "");
	}
	
	public void toggle() {
		this.toggle(!this.enabled, false);
	}
	
	public EventType getEventType() {
		return this.getClass().getAnnotation(EventType.class);
	}
}
