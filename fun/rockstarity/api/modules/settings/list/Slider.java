package fun.rockstarity.api.modules.settings.list;

import java.util.HashMap;
import java.util.Optional;
import java.util.function.Supplier;

import fun.rockstarity.api.binds.Bind;
import fun.rockstarity.api.binds.BindType;
import fun.rockstarity.api.binds.Bindable;
import fun.rockstarity.api.helpers.system.TextUtility;
import fun.rockstarity.api.modules.settings.Setting;
import fun.rockstarity.api.render.animation.infinity.InfinityAnimation;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import fun.rockstarity.client.modules.render.Interface;
import lombok.Getter;
import net.minecraft.util.math.MathHelper;

/**
 * @author ConeTin
 * @since 5 дек. 2
 * 023 г.
 */

public class Slider extends Setting {
	
	private final InfinityAnimation anim = new InfinityAnimation(), bindAnim = new InfinityAnimation();
	@Getter private final InfinityAnimation maxAnim = new InfinityAnimation();
	private float value, min, max, inc, bindValue, prev;
	@Getter private boolean percentMode;
	@Getter
	private final HashMap<Float, String> textValues = new HashMap<>();
	
	public Slider(Bindable parent, String name) {
		super(parent, name);
	}
	
    public Slider percent() {
        percentMode = true;
        min = 0;
        max = 1;
        inc = 0.01f;
        value = 1;
        return this;
    }
    
    public Slider percent(float value) {
        percentMode = true;
        min = 0;
        max = 1;
        inc = 0.01f;
        set(value);
        return this;
    }
    
    
	
	public InfinityAnimation getAnim(boolean bind) {
		return bind ? bindAnim : anim;
	}
	
	public Slider text(float value, String text) {
		this.textValues.put(value, text);
		
		return this;
	}

    public Slider set(float value) {
        if (percentMode) {
            this.value = MathHelper.clamp((float) (Math.round(value * 100 * (1.0 / this.inc)) / (1.0 / this.inc)), this.min, this.max);
        } else {
            this.value = MathHelper.clamp((float) (Math.round(value * (1.0 / this.inc)) / (1.0 / this.inc)), this.min, this.max);
        }
        return this;
    }
	
    public Slider bind(float value) {
        if (percentMode) {
            this.bindValue = MathHelper.clamp((float) (Math.round(value * 100 * (1.0 / this.inc)) / (1.0 / this.inc)), this.min, this.max);
        } else {
            this.bindValue = MathHelper.clamp((float) (Math.round(value * (1.0 / this.inc)) / (1.0 / this.inc)), this.min, this.max);
        }
        return this;
    }
    
    @Override
    public void reset() {
    	set(value);
    	bind(min);
    	super.reset();
    }
	
	public Slider range(float min, float max) {
		this.min = min;
		this.max = max;
		
		return this;
	}
	
	public Slider min(float min) {
		this.min = min;
		
		return this;
	}
	
	public Slider max(float max) {
		this.max = max;
		
		return this;
	}
	
	public Slider inc(float inc) {
		this.inc = inc;
		
		return this;
	}
	
    public float bind() {
        return this.bindValue;
    }
	
    public float get() {
        return value;
    }
	
	public float min() {
		return min;
	}
	
	public float max() {
		return max;
	}
	
	public float inc() {
		return inc;
	}
	
	@Override
	public void toggleWithBind(Optional<Bind> opt) {
		Bind bind = opt.get();
		
		this.toggled = !this.toggled;
		
		String display = "0";
		
		String gender = TextUtility.makeGender(getName());
		
		if (bind.getType() != BindType.HOLD || bind.isHolding()) {
			if (toggled) {
				display = TextUtility.formatNumber(this.bindValue);
				this.prev = this.get();
				this.set(this.bindValue);
				bind.setAlert(rock.getAlertHandler().alert(getName() + " установлен" + gender + " на " + display, bind.getType() == BindType.HOLD ? AlertType.WAIT : AlertType.SUCCESS));
			} else {
				display = TextUtility.formatNumber(this.prev);
				this.set(this.prev);
				bind.setAlert(rock.getAlertHandler().alert(getName() + " установлен" + gender + " на " + display, bind.getType() == BindType.HOLD ? AlertType.WAIT : AlertType.ERROR));
			}
			bind.getAlert().setBindable(this);
			bind.getAlert().setBind(bind);
		} else {
			this.set(this.prev);
			if (bind.getAlert() != null)
				bind.getAlert().hide();
		}
		
		rock.getModules().get(Interface.class).getKeyBinds().updateBind(bind, this.toggled, display);
	}
	
	public Slider hide(Supplier<Boolean> hide) {
		this.hide = hide;
		return this;
	}
	
	public Slider desc(String desc) {
		this.desc = desc;
		return this;
	}
	
}
