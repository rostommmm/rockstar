package fun.rockstarity.api.helpers.math;

import lombok.Setter;
import lombok.experimental.UtilityClass;

/**
 * @author ConeTin
 * @since 4 дек. 2023 г.
 */

public class TimerUtility {
	
	@Setter
    private long startTime = System.currentTimeMillis();

    public void reset() {
    	startTime = System.currentTimeMillis();
    }

    public boolean passed(float time) {
    	return passed((long) time);
    }

    public boolean passed(long time) {
    	return System.currentTimeMillis() - startTime > time;
    }

    public long getElapsed() {
    	return System.currentTimeMillis() - startTime;
    }
    
}
