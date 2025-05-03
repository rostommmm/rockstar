package fun.rockstarity.api.scripts.wrappers.base;

import fun.rockstarity.api.helpers.math.TimerUtility;

/**
 * @author ConeTin
 * @since 13 СЏРЅРІ. 2025вЂЇРі.
 */

public class TimerBase {
	
	private final TimerUtility timer = new TimerUtility();

	public void reset() {
		timer.reset();
	}

	public boolean passed(long time) {
		return timer.passed(time);
	}

	public long getElapsed() {
		return timer.getElapsed();
	}

}
