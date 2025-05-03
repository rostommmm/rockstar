package fun.rockstarity.api.connection.globals;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.helpers.system.ThreadManager;
import fun.rockstarity.api.render.globals.marks.MarkServer;
import fun.rockstarity.api.schedules.ScheduleServer;
import fun.rockstarity.client.modules.other.Globals;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;

/**
 * @author ConeTin
 * @since 8 февр. 2025 г.
 */

@UtilityClass
public class GlobalsThread implements IAccess {

	private final TimerUtility timer = new TimerUtility();
	
	@Getter @Setter
	private boolean init;
	
	public void start() {
		/*
		ThreadManager.run(() -> {
			while (true) {
				sleep();
				marksTick();
				scheduleTick();
				globalsTick();
			}
		});
		
		ThreadManager.run(() -> {
			while (true) {
				emoteTick();
			}
		});
		*/
	}
	
	private void marksTick() {
		try {
			if (!canNext()) return;
			if (!init) {
				SyncServer.init();
				sleep();
				MarkServer.init();
				sleep();
				ServerAPI.init();
				sleep();
				ServerAPI.updateName();
				ClientAPI.update(ServerAPI.getClients());
				init = true;
			}
			MarkServer.update();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void emoteTick() {
		try {
			if (!canNext()) return;
			SyncServer.update();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void scheduleTick() {
		try {
			if (!canNext()) return;
			ScheduleServer.update();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void globalsTick() {
		try {
			if (!canNext()) return;
			String data = ServerAPI.getClients();
			if (data != null)
				ClientAPI.update(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean canNext() {
		if (rock.getModules() == null || !rock.getModules().get(Globals.class).get())
			return false;
		
		if (!Player.isInGame()) return false;
		
		return true;
	}
	
	private void sleep() {
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
