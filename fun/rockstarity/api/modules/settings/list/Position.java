package fun.rockstarity.api.modules.settings.list;

import java.util.Optional;
import java.util.function.Supplier;

import fun.rockstarity.api.binds.Bind;
import fun.rockstarity.api.binds.BindType;
import fun.rockstarity.api.binds.Bindable;
import fun.rockstarity.api.helpers.system.TextUtility;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.Setting;
import fun.rockstarity.api.render.animation.infinity.InfinityAnimation;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import lombok.Getter;

/**
 * @author ConeTin
 * @since 7 дек. 2023 г.
 */

public class Position extends Setting {
	
	private final InfinityAnimation xAnim = new InfinityAnimation(), yAnim = new InfinityAnimation(), bindXAnim = new InfinityAnimation(), bindYAnim = new InfinityAnimation();

	@Getter
	private float x, y, minX = -1, maxX = 1, minY = -1, maxY = 1, bindX, bindY, prevX, prevY;
	
    
	public Position(Bindable parent, String name) {
		super(parent, name);
	}
	
	public InfinityAnimation getXAnim(boolean bind) {
		return bind ? bindXAnim : xAnim;
	}
	
	public InfinityAnimation getYAnim(boolean bind) {
		return bind ? bindYAnim : yAnim;
	}

	public Position bindX(float x) {
		this.bindX = x;
		return this;
	}
	
	public Position bindY(float y) {
		this.bindY = y;
		return this;
	}
	
	public Position x(float x) {
		this.x = x;
		return this;
	}
	
	public Position y(float y) {
		this.y = y;
		return this;
	}

	public Position minX(float x) {
		this.minX = x;
		return this;
	}
	
	public Position minY(float y) {
		this.minY = y;
		return this;
	}
	
	public Position maxX(float x) {
		this.maxX = x;
		return this;
	}
	
	public Position maxY(float y) {
		this.maxY = y;
		return this;
	}
	
	public Position hide(Supplier<Boolean> hide) {
		this.hide = hide;
		return this;
	}
	
	public Position desc(String desc) {
		this.desc = desc;
		return this;
	}
	
	@Override
	public void toggleWithBind(Optional<Bind> opt) {
		Bind bind = opt.get();
		
		this.toggled = !this.toggled;
		
		String gender = TextUtility.makeGender(getName());
		
		if (bind.getType() != BindType.HOLD || bind.isHolding()) {
			if (toggled) {
				this.prevX = this.x;
				this.prevY = this.y;
				this.x(this.bindX);
				this.y(this.bindY);
				bind.setAlert(rock.getAlertHandler().alert(getName() + " переключен" + gender, bind.getType() == BindType.HOLD ? AlertType.WAIT : AlertType.SUCCESS));
			} else {
				this.x(this.prevX);
				this.y(this.prevY);
				bind.setAlert(rock.getAlertHandler().alert(getName() + " переключен" + gender, bind.getType() == BindType.HOLD ? AlertType.WAIT : AlertType.ERROR));
			}
			bind.getAlert().setBindable(this);
			bind.getAlert().setBind(bind);
		} else {
			this.x(this.prevX);
			this.y(this.prevY);
			
			if (bind.getAlert() != null)
				bind.getAlert().hide();
		}
	}
	
}
