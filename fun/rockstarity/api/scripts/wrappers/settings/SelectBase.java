package fun.rockstarity.api.scripts.wrappers.settings;

import org.luaj.vm2.LuaFunction;

import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.ColorPicker;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Select.Element;
import fun.rockstarity.api.scripts.Script;
import fun.rockstarity.api.scripts.wrappers.base.ElementBase;
import fun.rockstarity.api.scripts.wrappers.base.ModuleBase;

public class SelectBase extends SettingBase {

	public SelectBase(ElementBase parent, String name) {
		super(new Select(parent.getInstance(), name), true);
	}
	
    public SelectBase(Select set) {
        super(set, false);
    }
    
    public SelectBase add(String mode) {
		Script.getCurrent().getScriptSelects().add(new Element((Select) child, mode));
        return this;
    }
    
    public SelectBase set(String elmt, boolean value) {
        ((Select)child).get(elmt).set(value);
        return this;
    }
    
    public boolean is(String mode) {
        return ((Select)child).get(mode).isEnabled();
    }
    
    public SelectBase info(String text) {
    	((Select)child).desc(text);
		return this;
	}

    public SelectBase hide(LuaFunction func) {
    	((Select)child).hide(() -> func.call().toboolean()); 
        return this;
    }
}