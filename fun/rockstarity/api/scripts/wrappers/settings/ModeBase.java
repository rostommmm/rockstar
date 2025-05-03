package fun.rockstarity.api.scripts.wrappers.settings;

import java.util.ArrayList;

import org.luaj.vm2.LuaFunction;

import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Mode.Element;
import fun.rockstarity.api.scripts.Script;
import fun.rockstarity.api.scripts.wrappers.base.ElementBase;
import fun.rockstarity.api.scripts.wrappers.base.ModuleBase;
import lombok.Getter;
import lombok.Setter;

public class ModeBase extends SettingBase {
	@Getter @Setter
    private ArrayList<Element> prev = new ArrayList<>();

	public ModeBase(ElementBase parent, String name) {
		super(new Mode(parent.getInstance(), name), true);
	}

    public ModeBase(Mode set) {
        super(set, false);
    }

    public String get() {
        return ((Mode)child).getMode().getName();
    }

    public ModeBase add(String mode) {
		Script.getCurrent().getScriptModes().add(new Element((Mode)child, mode));
        return this;
    }

    public ModeBase del(String mode) {
        ((Mode)child).remove(mode);
        return this;
    }

    public ModeBase set(String mode) {
        ((Mode)child).set(mode);
        return this;
    }

    public boolean is(String mode) {
        return ((Mode)child).getMode().getName().equalsIgnoreCase(mode);
    }

    public ModeBase info(String text) {
    	((Mode)child).desc(text);
		return this;
	}
    
    public ModeBase hide(LuaFunction func) {
    	((Mode)child).hide(() -> func.call().toboolean());
        return this;
    }
}