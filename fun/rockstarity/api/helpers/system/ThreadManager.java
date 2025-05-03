package fun.rockstarity.api.helpers.system;

import lombok.experimental.UtilityClass;

/**
 * @author ConeTin
 * @since 25 мар. 2024 г.
 */

@UtilityClass
public class ThreadManager {

	public Thread run(Runnable runnable) {
		Thread thread = new Thread(() -> runnable.run());
		thread.start();
		return thread;
	}
	
}
