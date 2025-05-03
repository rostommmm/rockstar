package fun.rockstarity.client.modules.player;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Select.Element;
import lombok.Getter;

/**
 * @author Malecharik
 * @since 17 Mar 2024 22:09:39
 */


@Info(name="NoPush", desc="Не дает игроку оттолкнуться", type=Category.PLAYER)
@Getter
public class NoPush extends Module {
	private final Select utils = new Select(this, "Убирать...").desc("Убрать столкновение с объектами из списка");
	private final Element players = new Element(utils, "Игроки").set(true);
	private final Element water = new Element(utils, "Вода").set(true);
	private final Element blocs = new Element(utils, "Блоки").set(true);
	
	@Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}

	@Override
	public void onEvent(Event event) {
		
	}
	
}
