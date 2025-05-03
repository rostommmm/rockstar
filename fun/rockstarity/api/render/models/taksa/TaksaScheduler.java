package fun.rockstarity.api.render.models.taksa;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventMessage;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.math.TimerUtility;
import lombok.Getter;
import lombok.experimental.UtilityClass;

/**
 * @author ConeTin
 * @since 23 Р°РїСЂ. 2025вЂЇРі.
 */

@UtilityClass
public class TaksaScheduler {
	
	@Getter
	private final TimerUtility updateTimer = new TimerUtility();
	
	public void onEvent(Event event) {
		if (event instanceof EventUpdate) {
			if (updateTimer.passed(60_000)) {
				TaksaAI.update();
			}	
		}
		
		if (event instanceof EventMessage e) {
			TaksaAI.setLastMsg(e.getMessage());
			TaksaAI.update();
		}
	}

}
