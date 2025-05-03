package fun.rockstarity.api.scripts.wrappers.settings;

import org.luaj.vm2.LuaFunction;

import fun.rockstarity.api.binds.Bindable;
import fun.rockstarity.api.modules.settings.list.Binding;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.scripts.wrappers.base.ElementBase;
import fun.rockstarity.api.scripts.wrappers.base.ModuleBase;

public class BindBase extends SettingBase {
	
	public BindBase(ElementBase parent, String name) {
		super(new Binding(parent.getInstance(), name), true);
	}
	
	public BindBase(Binding set) {
		super(set, false);
	}
	
	public BindBase info(String text) {
		((Binding)child).desc(text);
		return this;
	}
	
	public BindBase hide(LuaFunction func) {
		((Binding)child).hide(() -> func.call().toboolean());
		return this;
	}
	
	public int key() {
		return ((Binding)child).getBinds().isEmpty() ? -1 : ((Binding)child).getBinds().get(0).getKey();
	}
	
}