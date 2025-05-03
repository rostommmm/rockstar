package fun.rockstarity.client.modules.render;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.constuctor.ConstructorScreen;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;

/**
 * @author ConeTin
 * @since 7 дек. 2023 г.
 */

@Info(name="Constructor", desc="Позволяет редактировать скрипты", type=Category.RENDER)
public class Constructor extends Module {
	
	@Override
	public void onAllEvent(Event event) {
		if (event instanceof EventUpdate && rock.getConstructor() == null) {
			rock.setConstructor(new ConstructorScreen());
		}
	}
	
	@Override
	public void onEvent(Event event) {
	}
	
	@Override
	public void onEnable() {
		mc.displayGuiScreen(rock.getScriptConstructor().getScreen());
		ConstructorScreen.opening.setForward(true);
	}

	@Override
	public void onDisable() {
	}
	
}
