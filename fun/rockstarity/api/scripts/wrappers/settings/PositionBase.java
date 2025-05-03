package fun.rockstarity.api.scripts.wrappers.settings;

import org.luaj.vm2.LuaFunction;

import fun.rockstarity.api.modules.settings.list.ColorPicker;
import fun.rockstarity.api.modules.settings.list.Input;
import fun.rockstarity.api.modules.settings.list.Position;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.scripts.wrappers.base.ColorBase;
import fun.rockstarity.api.scripts.wrappers.base.ElementBase;

public class PositionBase extends SettingBase {
	
	public PositionBase(ElementBase parent, String name) {
		super(new Position(parent.getInstance(), name), true);
	}
	
    public PositionBase(Position set) {
        super(set, false);
    }
    
    public float x() {
        return ((Position)child).getX();
    }
    
    public float y() {
        return ((Position)child).getY();
    }
    
    public PositionBase minX(float x) {
        ((Position)child).minX(x);
        return this;
    }
    
    public PositionBase minY(float y) {
        ((Position)child).minY(y);
        return this;
    }
    
    public PositionBase maxX(float x) {
        ((Position)child).maxX(x);
        return this;
    }
    
    public PositionBase maxY(float y) {
        ((Position)child).maxY(y);
        return this;
    }
    
    
    public PositionBase info(String text) {
    	((Position)child).desc(text);
		return this;
	}
    
    public PositionBase hide(LuaFunction func) {
    	((Position)child).hide(() -> func.call().toboolean()); 
        return this;
    }
}
