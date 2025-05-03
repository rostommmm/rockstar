package fun.rockstarity.api.scripts.wrappers.settings;

import org.luaj.vm2.LuaFunction;

import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.scripts.wrappers.base.ElementBase;
import fun.rockstarity.api.scripts.wrappers.base.ModuleBase;

public class SliderBase extends SettingBase {

	public SliderBase(ElementBase parent, String name) {
		super(new Slider(parent.getInstance(), name), true);
	}
	
    public SliderBase(Slider set) {
        super(set, false); 
    }
    
    public SliderBase max(float max) {
        ((Slider)child).max(max);
        return this;
    }
    
    public SliderBase min(float min) {
        ((Slider)child).min(min);
        return this;
    }
    
    public SliderBase set(float val) {
        ((Slider)child).set(val);
        return this;
    }
    
    public SliderBase inc(float inc) {
        ((Slider)child).inc(inc);
        return this;
    }
    
    public SliderBase text(float value, String text) {
        ((Slider)child).text(value, text);
        return this;
    }
    
    public float get() {
        return ((Slider)child).get(); 
    }
    
    public float inc() {
        return ((Slider)child).inc();
    }
    
    public float min() {
        return ((Slider)child).min();
    }
    
    public float max() {
        return ((Slider)child).max();
    }
    
    public SliderBase info(String text) {
    	((Slider)child).desc(text);
		return this;
	}

    
    public SliderBase hide(LuaFunction func) {
    	((Slider)child).hide(() -> func.call().toboolean());
        return this;
    }
}