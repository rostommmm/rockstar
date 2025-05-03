package fun.rockstarity.api.modules.settings.list;

import java.util.Optional;
import java.util.function.Supplier;
import fun.rockstarity.api.binds.Bind;
import fun.rockstarity.api.binds.BindType;
import fun.rockstarity.api.helpers.system.TextUtility;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.Setting;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import lombok.Getter;
import lombok.Setter;

/**
 * @author ConeTin
 * @since 5 дек. 2023 г.
 */

public class Clickable extends Setting {
	
	private Runnable func = () -> System.out.println("empty button!");;
	@Getter private String buttonText = "Нажми меня";
	
	@Getter
	private Animation hoverButtonAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300).setSize(1);
	
	public Clickable set(Runnable func) {
		this.func = func;
		
		return this;
	}
	
	public Runnable get() {
		return this.func;
	}
	
	public Clickable set(String buttonText) {
		this.buttonText = buttonText;
		
		return this;
	}
	
	public Clickable(Module parent, String name) {
		super(parent, name);
	}
	
	@Override
	public void toggleWithBind(Optional<Bind> opt) {
		Bind bind = opt.get();
		
		String gender = TextUtility.makeGender(getName());
		
		this.get().run();
		
		if (bind.getType() != BindType.HOLD || bind.isHolding()) {
			bind.setAlert(rock.getAlertHandler().alert(getName() + " нажат" + gender, bind.getType() == BindType.HOLD ? AlertType.WAIT : AlertType.ERROR));
		} else {
			if (bind.getAlert() != null)
				bind.getAlert().hide();
		}
	}
	
	public Clickable hide(Supplier<Boolean> hide) {
		this.hide = hide;
		return this;
	}
	
	public Clickable desc(String desc) {
		this.desc = desc;
		return this;
	}
	
}
