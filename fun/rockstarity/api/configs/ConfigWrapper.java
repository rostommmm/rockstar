package fun.rockstarity.api.configs;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Select;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * @author ConeTin
 * @since 10 февр. 2025 г.
 */

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConfigWrapper extends Module {

	CheckBox loadBinds = new CheckBox(this, "Загружать бинды").set(true);
	
	Select categories = new Select(this, "Загружать категории");
	
	public ConfigWrapper() {
		for (Category type : Category.values()) {
			if (type == Category.THEMES) continue;
			new Select.Element(categories, type.getDisplayName()).set(true);
		}
	}
	
	@Override
	public void onEvent(Event event) {
		
	}

	@Override
	public void onEnable() {
		
	}

	@Override
	public void onDisable() {
		
	}

}
