package fun.rockstarity.api.autobuy;

import java.util.ArrayList;
import java.util.Arrays;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.autobuy.logic.BotLogic;
import fun.rockstarity.api.autobuy.logic.ParserLogic;
import fun.rockstarity.api.autobuy.logic.PlayerLogic;
import fun.rockstarity.api.autobuy.logic.interfaces.ILogicHandler;
import fun.rockstarity.api.autobuy.logic.items.AutoBuyItem;
import fun.rockstarity.api.autobuy.logic.tasks.TaskManager;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.render.AutoBuyWindow;
import lombok.Getter;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;

/**
 * @author ConeTin
 * @since 20 апр. 2024 г.
 */


public class AutoBuy implements IAccess {
	
	private final ArrayList<ILogicHandler> handlers = new ArrayList<>();
	@Getter private final ArrayList<AutoBuyItem> items = new ArrayList<>();
	
	private final BotLogic botLogicHandler;
	private final PlayerLogic playerLogicHandler;
	private final ParserLogic parserLogicHandler;
	@Getter private AutoBuyWindow window;
	
	@Getter
	private final TaskManager taskManager;
	
	public AutoBuy() {
		Arrays.stream(new ILogicHandler[] {
				botLogicHandler = new BotLogic(this),
				playerLogicHandler = new PlayerLogic(this),
				parserLogicHandler = new ParserLogic(this)
		}).forEach(handlers::add);
		
		this.taskManager = new TaskManager();
		if (rock.isDebugging()) {
			window = new AutoBuyWindow(rock.getModules().get(fun.rockstarity.client.modules.other.AutoBuy.class));
		}
	}
	
	public void onEvent(Event event) {
		if (rock.getModules().get(fun.rockstarity.client.modules.other.AutoBuy.class) == null || !rock.getModules().get(fun.rockstarity.client.modules.other.AutoBuy.class).get()) return;
		
		if (event instanceof EventRender2D) {
			window.getOpening().setForward(mc.currentScreen instanceof ContainerScreen screen && avaibleServer() && (screen.getTitle().getString().contains("Аукцион") || screen.getTitle().getString().contains("Поиск")));
		}
		
		handlers.forEach(handler -> handler.onEvent(event));
	}

	public boolean avaibleServer() {
		return Server.isFT();
	}
	
}
