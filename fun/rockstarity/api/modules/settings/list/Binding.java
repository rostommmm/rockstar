package fun.rockstarity.api.modules.settings.list;

import java.util.function.Supplier;

import fun.rockstarity.api.binds.Bind;
import fun.rockstarity.api.binds.Bindable;
import fun.rockstarity.api.events.list.game.inputs.EventKey;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.Setting;

/**
 * @author Malecharik
 * @since 23 Mar 2024 23:52:44
 */

public class Binding extends Setting {
	
	public Binding(Bindable parent, String name) {
		super(parent, name);
	}
	
	public boolean check(EventKey e) {
		if (mc.currentScreen != null || e.isReleased()) return false;
		
		return getBindByKey(e).isPresent();
	}
	
	public Binding addBind(Bind bind) {
		this.getBinds().add(bind);
		return this;
	}
	
	public Binding hide(Supplier<Boolean> hide) {
		this.hide = hide;
		return this;
	}
	
	public Binding desc(String desc) {
		this.desc = desc;
		return this;
	}
}
