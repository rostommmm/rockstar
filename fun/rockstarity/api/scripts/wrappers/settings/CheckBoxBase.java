package fun.rockstarity.api.scripts.wrappers.settings;

import org.luaj.vm2.LuaFunction;

import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.scripts.wrappers.base.ElementBase;

public class CheckBoxBase extends SettingBase {
	
	public CheckBoxBase(ElementBase parent, String name) {
		super(new CheckBox(parent.getInstance(), name), true);
	}

	public CheckBoxBase(CheckBox set) {
		super(set, false);
	}
	
	public boolean get() {
		return ((CheckBox)child).get();
	}
	
	public CheckBoxBase set(boolean state) {
		((CheckBox)child).set(state);
		return this;
	}
	
	public CheckBoxBase info(String text) {
		((CheckBox)child).desc(text);
		return this;
	}
	
	public CheckBoxBase hide(LuaFunction func) {
		((CheckBox)child).hide(() -> func.call().toboolean());
		return this;
	}
	
	
}