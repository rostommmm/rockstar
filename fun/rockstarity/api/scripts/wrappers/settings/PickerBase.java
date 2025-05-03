package fun.rockstarity.api.scripts.wrappers.settings;


import org.luaj.vm2.LuaFunction;

import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.ColorPicker;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.scripts.wrappers.base.ColorBase;
import fun.rockstarity.api.scripts.wrappers.base.ElementBase;
import fun.rockstarity.api.scripts.wrappers.base.ModuleBase;

public class PickerBase extends SettingBase {

	public PickerBase(ElementBase parent, String name) {
		super(new ColorPicker(parent.getInstance(), name), true);
	}

    public PickerBase(ColorPicker set) {
        super(set, false);
    }

    public ColorBase get() {
        return new ColorBase(((ColorPicker)child).get());
    }
    
    public boolean client(boolean client) {
    	((ColorPicker)child).setClient(client);
    	return ((ColorPicker)child).isClient();
    }

    public PickerBase set(ColorBase state) {
        ((ColorPicker)child).add(new FixColor(state));
        return this;
    }

    public PickerBase info(String text) {
    	((ColorPicker)child).desc(text);
		return this;
	}

    public PickerBase hide(LuaFunction func) {
    	((ColorPicker)child).hide(() -> func.call().toboolean());
        return this;
    }
}