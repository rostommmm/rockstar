package fun.rockstarity.api.scripts.wrappers.base;

import java.util.ArrayList;

import org.luaj.vm2.LuaFunction;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.ModuleInfo;
import fun.rockstarity.api.modules.settings.Setting;
import fun.rockstarity.api.modules.settings.list.Binding;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.ColorPicker;
import fun.rockstarity.api.modules.settings.list.Input;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Position;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.scripts.Script;
import fun.rockstarity.api.scripts.wrappers.settings.BindBase;
import fun.rockstarity.api.scripts.wrappers.settings.CheckBoxBase;
import fun.rockstarity.api.scripts.wrappers.settings.InputBase;
import fun.rockstarity.api.scripts.wrappers.settings.ModeBase;
import fun.rockstarity.api.scripts.wrappers.settings.PickerBase;
import fun.rockstarity.api.scripts.wrappers.settings.PositionBase;
import fun.rockstarity.api.scripts.wrappers.settings.SelectBase;
import fun.rockstarity.api.scripts.wrappers.settings.SettingBase;
import fun.rockstarity.api.scripts.wrappers.settings.SliderBase;
import lombok.Getter;

@Getter
public class ModuleBase extends ElementBase implements IAccess {
	protected Module instance;
	private final String name, desc;
	private LuaFunction onEnable, onDisable;
	
	public ModuleBase(String name, String desc) {
		this.name = name;
		this.desc = desc;
		instance = new Module() {
			@Override
			public void onEnable() {
				if (onEnable != null) onEnable.call();
			}
			
			@Override
			public void onDisable() {
				if (onDisable != null) onDisable.call();
			}

			@Override
			public void onEvent(Event event) {
				
			}
		};

		instance.setInfo(new ModuleInfo(name, desc, Category.SCRIPTS, new String[]{name}));
		
		Script.getCurrent().getScriptModules().add(instance);
	}
	
	public ModuleBase(Module mod) {
		this.name = mod.getInfo().name();
		this.desc = mod.getInfo().desc();
		instance = mod;
		Script.getCurrent().getBases().add(this);
	}
	
	public ModuleBase set(boolean state) {
		instance.set(state);
		return this;
	}
	
	public boolean get() {
		return instance.get();
	}
	
	public String name() {
		return instance.getInfo().name();
	}
	
	public String desc() {
		return instance.getInfo().desc();
	}
	
	public String type() {
		return instance.getInfo().type().getName();
	}
	
	public ModuleBase toggle(boolean silent) {
		instance.toggle(!instance.get(), silent);
		return this;
	}
	
	public ModuleBase toggle() {
		instance.toggle(!instance.get(), false);
		return this;
	}
	
	public SettingBase[] settings() {
		ArrayList<SettingBase> settings = new ArrayList<>();
		
		for (Setting set : instance.getSettings()) {
			if (set instanceof CheckBox) {
				settings.add(new CheckBoxBase((CheckBox)set));
			} else if (set instanceof Binding) {
				settings.add(new BindBase((Binding)set));
			} else if (set instanceof Input) {
				settings.add(new InputBase((Input)set));
			} else if (set instanceof Mode) {
				settings.add(new ModeBase((Mode)set));
			} else if (set instanceof Position) {
				settings.add(new PositionBase((Position)set));
			} else if (set instanceof ColorPicker) {
				settings.add(new PickerBase((ColorPicker)set));
			} else if (set instanceof Select) {
				settings.add(new SelectBase((Select)set));
			} else if (set instanceof Slider) {
				settings.add(new SliderBase((Slider)set));
			} else {
				settings.add(new SettingBase(set, false));
			}
		}
		
		SettingBase[] array = new SettingBase[settings.size()];
		array = settings.toArray(array);
		return array;
	}
	
	public int key() {
		return instance.getBinds().isEmpty() ? -1 : instance.getBinds().get(0).getKey();
	}
	
	public ModuleBase on_enable(LuaFunction func) {
		this.onEnable = func;
		return this;
	}
	
	public ModuleBase on_disable(LuaFunction func) {
		this.onDisable = func;
		return this;
	}
	
}