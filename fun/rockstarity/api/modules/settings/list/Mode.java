package fun.rockstarity.api.modules.settings.list;

import java.util.ArrayList;
import java.util.Objects;
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
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.client.modules.render.Interface;
import lombok.Getter;
import lombok.Setter;

/**
 * @author ConeTin
 * @since 5 дек. 2
 * 023 г.
 */

@Getter @Setter
public class Mode extends Setting {
	
	private ArrayList<Element> elements = new ArrayList<>();
	private Element current, bindCurrent, prevCurrent;
	private Runnable onChange;

	public Mode(Bindable parent, String name) {
		super(parent, name);
	}
	

	public Mode onChange(Runnable val) {
		this.onChange = val;
		return this;
	}
	
	public Mode bind(Element elmt) {
		this.bindCurrent = elmt;
		return this;
	}
	
	public Mode set(Element elmt) {
		this.current = elmt;
		if (onChange != null) {
			onChange.run();
		}
		return this;
	}
	
	public Mode bind(String elmt) {
		for (Element elmta : elements) {
			if (elmta.getName().equals(elmt)) {
				this.bindCurrent = elmta;
			}
		}
		return this;
	}
	
	@Override
	public void reset() {
		set(elements.get(0));
		bind(elements.get(0).getName());
		super.reset();
	}
	
	public Mode set(String elmt) {
		for (Element elmta : elements) {
			if (elmta.getName().equals(elmt)) {
				this.current = elmta;
			}
		}
		return this;
	}
	
	public Mode.Element get(String elmt) {
		for (Element elmta : elements) {
			if (elmta.getName().equals(elmt)) {
				return elmta;
			}
		}
		return null;
	}
	
	public void remove(Element elmt) {
		this.elements.remove(elmt);
	}
	
	public void remove(String elmt) {
		elements.removeIf(elmt1 -> elmt1.getName().equals(elmt));
	}
	
	public Element getMode() {
		return current;
	}
	
	public Element bind() {
		return Objects.requireNonNullElse(bindCurrent, current);
	}
	
	public boolean is(Element elmt) {
		return current == elmt && !this.isHide();
	}
	
	public boolean isBind(Element elmt) {
		return bindCurrent == elmt && !this.isHide();
	}
	
	public Mode add(Element elmt) {
		elements.add(elmt);
		if (current == null) current = elmt;
		return this;
	}
	
	public Mode addCurrent(Element elmt) {
		elements.add(elmt);
		this.current = elmt;
		return this;
	}
	
	public Mode hide(Supplier<Boolean> hide) {
		this.hide = hide;
		return this;
	}
	
	@Override
	public void toggleWithBind(Optional<Bind> opt) {
		Bind bind = opt.get();
		
		this.toggled = !this.toggled;
		
		String gender = TextUtility.makeGender(getName());
		
		if (bind.getType() != BindType.HOLD || bind.isHolding()) {
			if (toggled) {
				this.prevCurrent = this.getMode();
				this.set(this.bindCurrent);
				bind.setAlert(rock.getAlertHandler().alert(getName() + " переключен" + gender, bind.getType() == BindType.HOLD ? AlertType.WAIT : AlertType.SUCCESS));
			} else {
				this.set(this.prevCurrent);
				bind.setAlert(rock.getAlertHandler().alert(getName() + " переключен" + gender, bind.getType() == BindType.HOLD ? AlertType.WAIT : AlertType.ERROR));
			}
			bind.getAlert().setBindable(this);
			bind.getAlert().setBind(bind);
		} else {
			this.set(this.prevCurrent);
			if (bind.getAlert() != null)
				bind.getAlert().hide();
		}
		
		rock.getModules().get(Interface.class).getKeyBinds().updateBind(bind, this.toggled, this.bindCurrent.getName());
	}
	
	@Getter
	public static class Element extends Bindable {
		
		private boolean ifEnabled = true;
		private final ArrayList<Setting> settings = new ArrayList<>();
		private Mode parent;
		private String name;
		private final Animation anim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300), hover = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(200).setSize(0.5f),
				bindAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300), bindHover = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(200).setSize(0.5f);
		private Animation settingsAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);

		@Setter
		private Rect settingRect = Rect.EMPTY;
		
		
		public boolean canSettings() {
			return !settingsAnim.finished(false);
		}
		
		public boolean hasSettings() {
			return !settings.isEmpty();
		}

		public Element ifEnabled(boolean ifEnabled) {
			this.ifEnabled = ifEnabled;
			return this;
		}
		
		public boolean ifEnabled() {
			return ifEnabled;
		}
		
		public Animation getAnim(boolean bind) {
			return bind ? anim : bindAnim;
		}
		
		public Animation getHover(boolean bind) {
			return bind ? hover : bindHover;
		}
		
		public Element(Mode parent, String name) {
			this.parent = parent;
			this.name = name;
			parent.add(this);
		}
		
		public Element set() {
			parent.set(this);
			return this;
		}
		
		public boolean get() {
			return parent.is(this);
		}
	}
	
	public Mode desc(String desc) {
		this.desc = desc;
		return this;
	}
	
}