package fun.rockstarity.client.modules.other;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.helpers.math.aura.ai.RotationParser;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;

/**
 * @author ConeTin
 * @since 6 дек. 2023 г.
 */


@Info(name="Recorder", desc="Запись информации о ротации", type=Category.OTHER)
public class Recorder extends Module {

	@Override
	public void onEvent(Event event) {
		RotationParser.onEvent(event);
	}
	
	@Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}
	
}
