package fun.rockstarity.client.modules.move;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.EventType;
import fun.rockstarity.api.events.list.player.EventKeepSprint;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.secure.Debugger;
import fun.rockstarity.client.modules.combat.Aura;
import fun.rockstarity.client.modules.player.FreeCam;
import lombok.Getter;
import lombok.Setter;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author ConeTin
 * @since 2 дек. 2023 г.
 */
@NativeInclude

@Info(name="AutoSprint", desc="Автоматически спринтится", type=Category.MOVE)
public class AutoSprint extends Module {
	@Getter @Setter
	private boolean canSprint = true;
	
	@Getter
	private final CheckBox keepSprint = new CheckBox(this, "Сохранять спринт").set(true);
	@Getter
	private final CheckBox hunger = new CheckBox(this, "Игнорировать голод");
	TimerUtility timerUtility = new TimerUtility();
	
	@Override
	@EventType({EventUpdate.class, EventKeepSprint.class})
	public void onEvent(Event event) {
		if (event instanceof EventUpdate && !rock.getModules().get(FreeCam.class).get()) {
			Aura auraNew = rock.getModules().get(Aura.class);
			if (mc.player.isSprinting()) timerUtility.reset();

			mc.getGameSettings().keyBindSprint.setPressed(canSprint);
		}
		
		// Сохранение спринта при ударе если включен чекбокс "Сохранять спринт"
		if (this.keepSprint.get() && event instanceof EventKeepSprint) {
			event.cancel();
		}
	}

	@Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}
}
