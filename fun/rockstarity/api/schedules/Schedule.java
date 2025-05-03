package fun.rockstarity.api.schedules;

import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * @author ConeTin
 * @since 12 СЏРЅРІ. 2025вЂЇРі.
 */

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Schedule {
	
	@Getter final Animation showingAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300), secondAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	final TimerUtility sinceUpdate = new TimerUtility();
	@Getter final int anarchy;
	int seconds;
	
	public int getSeconds() {
		return (int) (seconds - sinceUpdate.getElapsed()/1000F);
	}
	
	public void setSeconds(int seconds) {
		this.seconds = seconds;
		sinceUpdate.reset();
	}
	
}
