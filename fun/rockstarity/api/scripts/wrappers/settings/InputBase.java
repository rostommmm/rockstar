package fun.rockstarity.api.scripts.wrappers.settings;

import org.luaj.vm2.LuaFunction;

import fun.rockstarity.api.binds.Bindable;
import fun.rockstarity.api.modules.settings.list.Binding;
import fun.rockstarity.api.modules.settings.list.Input;
import fun.rockstarity.api.scripts.wrappers.base.ElementBase;
import fun.rockstarity.api.scripts.wrappers.base.ModuleBase;

public class InputBase extends SettingBase {

	public InputBase(ElementBase parent, String name) {
		super(new Input(parent.getInstance(), name), true);
	}
	
    public InputBase(Input set) {
        super(set, false);
    }
    
    public String get() {
        return ((Input)child).get();
    }
    
    public InputBase size(int value) {
        ((Input)child).getInput().maxStringLength = value;
        return this;
    }
    
    public InputBase set(String state) {
        ((Input)child).set(state);
        return this;
    }
    
    public InputBase info(String text) {
    	((Input)child).desc(text);
		return this;
	}
    
    public InputBase hide(LuaFunction func) {
    	((Input)child).hide(() -> func.call().toboolean());
        return this;
    }
}