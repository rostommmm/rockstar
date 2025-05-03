package fun.rockstarity.client.modules.other;

import org.lwjgl.glfw.GLFW;

import fun.rockstarity.api.binds.Bind;
import fun.rockstarity.api.binds.BindType;
import fun.rockstarity.api.connection.globals.ClientAPI;
import fun.rockstarity.api.connection.globals.GlobalsThread;
import fun.rockstarity.api.connection.globals.SyncServer;
import fun.rockstarity.api.connection.globals.ServerAPI;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.system.ThreadManager;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Binding;
import fun.rockstarity.api.render.globals.marks.MarkServer;
import fun.rockstarity.api.schedules.ScheduleServer;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author ConeTin
 * @since 13 июн. 2024 г.
 */

@Info(name = "Globals", desc = "Показывает пользователей в табе", type = Category.OTHER, module="Emotions")
public class Globals extends Module {

	private final TimerUtility timer = new TimerUtility();
	private boolean init;
	
	@Getter
	final Binding wheelBind = new Binding(this, "Колесо эмоций").addBind(new Bind(GLFW.GLFW_KEY_J, BindType.HOLD)).desc("Кнопка, при нажатии которой будет открываться колесо эмоций");
	
	@Getter
	final Binding markBind = new Binding(this, "Клавиша метки").addBind(new Bind(GLFW.GLFW_KEY_J, BindType.HOLD)).desc("Кнопка, при нажатии которой будет отправляться метка");

	public Globals() {
		
	}
	
	@NativeInclude
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventUpdate) {
			if (init) {
				if (mc.player.ticksExisted % 15 == 0) {
					ThreadManager.run(() -> ClientAPI.update(ServerAPI.getClients()));
				}
				
				if (mc.player.ticksExisted % 15 == 4) {
					MarkServer.update();
				}
				
				if (mc.player.ticksExisted % 15 == 9) {
					SyncServer.update();
				}
			}

			if (!this.init) {
				SyncServer.init();
				ServerAPI.init();
				ServerAPI.updateName();
				ClientAPI.update(ServerAPI.getClients());
				this.init = true;
			}
		}
	}
	@NativeInclude
	@Override
	public void onDisable() {
		ServerAPI.finish();
		SyncServer.finish();
		ClientAPI.USERS.clear();
		GlobalsThread.setInit(false);
		init = false;
	}

	@Override
	public void onEnable() {
		SyncServer.init();
		MarkServer.init();
		ServerAPI.init();
		ServerAPI.updateName();
	}
	
}
