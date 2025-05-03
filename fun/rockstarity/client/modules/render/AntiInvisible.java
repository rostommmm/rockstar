package fun.rockstarity.client.modules.render;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Slider;
import lombok.Getter;

@Info(name="AntiInvisible", desc="Показывает игроков с невидимостью", type=Category.RENDER)
public class AntiInvisible extends Module {
	@Getter
	private final Slider alpha = new Slider(this, "Прозрачность").min(0.1f).max(1).inc(0.1f).set(0.3f).desc("Выбрать прозрачность моделек игроков");
	
	@Override
	public void onEvent(Event event) {
	}
	
	@Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}
	
}
