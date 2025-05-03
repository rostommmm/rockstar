package fun.rockstarity.api.modules.settings.list;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import fun.rockstarity.api.binds.Bind;
import fun.rockstarity.api.binds.BindType;
import fun.rockstarity.api.binds.Bindable;
import fun.rockstarity.api.helpers.system.TextUtility;
import fun.rockstarity.api.modules.settings.Setting;
import fun.rockstarity.api.modules.settings.list.Mode.Element;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.client.modules.render.Interface;
import fun.rockstarity.client.modules.render.Interface.UIElement;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * @author ConeTin
 * @since 5 дек. 2
 * 023 г.
 */

@Getter @Setter
public class Select extends Setting {
	
	private final ArrayList<Element> elements = new ArrayList<>();
	private boolean draggable;
	private int min = 0, max = 20;
	
	public Select(Bindable parent, String name) {
		super(parent, name);
	}
	
	public Element getRandomEnabledElement() {
        List<Element> enabledElements = elements.stream()
                .filter(elmt -> elmt.get()) 
                .collect(Collectors.toList());

        if (!enabledElements.isEmpty()) {
            Random random = new Random();
            return enabledElements.get(random.nextInt(enabledElements.size()));
        }
        return null;
    }
	
	public Select min(int min) {
		this.min = min;
		return this;
	}
	
	public Select max(int max) {
		this.max = max;
		return this;
	}
	
	public Select add(Element elmt) {
		elements.add(elmt);
		return this;
	}
	
	public void remove(Element elmt) {
		this.elements.remove(elmt);
	}
	
	public Select draggable(boolean draggable) {
		this.draggable = draggable;
		return this;
	}
	
	public boolean isSelectedByIndex(int index) {
	    if (index >= 0 && index < elements.size()) {
	        Element element = elements.get(index);
	        return element.get() && !element.isHide();
	    }
	    return false;
	}
	
	public boolean is(Element elmt) {
		return elmt.get() && !this.isHide();
	}
	
	public ArrayList<Element> getToggled() {
		ArrayList<Element> toggled = new ArrayList<>();
		
		for (Element elmt : elements) {
			if (elmt.get()) {
				toggled.add(elmt);
			}
		}
			
		return toggled;
	}
	
	public Element get(String name) {
		Element elmt1 = null;
		
		for (Element elmt : elements) {
			if (elmt.getName().equalsIgnoreCase(name)) {
				elmt1 = elmt;
			}
		}
			
		return elmt1;
	}
	
	public Select hide(Supplier<Boolean> hide) {
		this.hide = hide;
		return this;
	}
	
	@Getter
	@FieldDefaults(level = AccessLevel.PRIVATE)
	public static class Element extends Bindable {
		
		private Animation settingsAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
		private boolean ifEnabled = true;
		private final ArrayList<Setting> settings = new ArrayList<>();
		protected Supplier<Boolean> hide = () -> false;
		Select parent;
		String name;
		boolean enabled, prevEnabled;
		final Animation anim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(200).setSize(1), hover = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(200).setSize(0.5f),
				hideAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(200);
		@Setter boolean dragging;
		@Setter float dragX, dragY;
		@Setter Rect rect;
		Runnable onEnable, onDisable;
		
		public Element(Select parent, String name) {
			this.parent = parent;
			this.name = name;
			parent.add(this);
		}
		
		@Setter
		private Rect settingRect = Rect.EMPTY;
		
		
		public boolean canSettings() {
			return !settingsAnim.finished(false);
		}
		
		public boolean hasSettings() {
			return !settings.isEmpty() && !(this instanceof UIElement);
		}

		public Element ifEnabled(boolean ifEnabled) {
			this.ifEnabled = ifEnabled;
			return this;
		}
		
		public boolean ifEnabled() {
			return ifEnabled;
		}
		
		public Element set(boolean val, boolean silent) {
		    int toggledCount = parent.getToggled().size();
		    if ((val && toggledCount < parent.max) || (!val && toggledCount > parent.min) || silent) {
		        this.enabled = val;
		    } else {
		    	if (!silent) {
		    		String word = "больше";
		    		
		    		if (toggledCount < parent.max) word = "меньше";
		    		
		    		rock.getAlertHandler().alert("Вы не можете выбрать " + word + " элементов", AlertType.ERROR);
		    	}
		    }
		    return this;
		}
		
		public Element set(boolean val) {
			if (val)
				this.call(onEnable);
			else
				this.call(onDisable);
			return this.set(val, false);
		}
		
		public boolean get() {
			return this.enabled && !this.isHide();
		}
		
		public Element hide(Supplier<Boolean> hide) {
			this.hide = hide;
			return this;
		}
		
		public Element onEnable(Runnable val) {
			this.onEnable = val;
			return this;
		}

		public Element onDisable(Runnable val) {
			this.onDisable = val;
			return this;
		}
		
		private void call(Runnable run) {
			if (run != null) run.run();
		}
		
		public boolean isHide() {
			return hide.get();
		}
		
		@Override
		public void toggleWithBind(Optional<Bind> opt) {
			Bind bind = opt.get();
			
			if (isHide()) return;
			
			this.toggled = !this.toggled;
			
			String gender = TextUtility.makeGender(getName());
			
			if (bind.getType() != BindType.HOLD || bind.isHolding()) {
				if (toggled) {
					this.prevEnabled = this.get();
					this.set(!this.get());
					bind.setAlert(rock.getAlertHandler().alert(getName() + " переключен" + gender, bind.getType() == BindType.HOLD ? AlertType.WAIT : get() ? AlertType.SUCCESS : AlertType.ERROR));
				} else {
					this.set(this.prevEnabled);
					bind.setAlert(rock.getAlertHandler().alert(getName() + " переключен" + gender, bind.getType() == BindType.HOLD ? AlertType.WAIT : get() ? AlertType.SUCCESS : AlertType.ERROR));
				}
				bind.getAlert().setBindable(this);
				bind.getAlert().setBind(bind);
			} else {
				this.set(this.prevEnabled);
				if (bind.getAlert() != null)
					bind.getAlert().hide();
			}
			
			rock.getModules().get(Interface.class).getKeyBinds().updateBind(bind, this.toggled, "");
		}
		
	}
	
	public Select desc(String desc) {
		this.desc = desc;
		return this;
	}
	
}