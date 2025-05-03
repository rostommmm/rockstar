package fun.rockstarity.api.scripts.wrappers.settings;

import org.luaj.vm2.LuaFunction;

import fun.rockstarity.Rockstar;
import fun.rockstarity.api.binds.Bindable;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.Setting;
import fun.rockstarity.api.scripts.Script;
import fun.rockstarity.api.scripts.wrappers.base.ElementBase;
import lombok.Getter;

public class SettingBase extends ElementBase {
	@Getter
	protected Setting child;
	private final Bindable parent;
	private final String name;
	
	public SettingBase(Setting set, boolean fromChild) {
		this.name = set.getName();
		this.parent = set.getParent();
		child = set;
		if (fromChild) {
			Script.getCurrent().getScriptSets().add(child);
		}
		instance = set;
	}
	
	public String name() {
		return child.getName();
	}
	
	public SettingBase info(String text) {
		child.desc(text);
		
		return this;
	}
	
	public SettingBase hide(LuaFunction func) {
		child.hide(() -> func.call().toboolean());
		return this;
	}
}