package fun.rockstarity.api.schedules;

import java.util.ArrayList;
import java.util.List;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.connection.globals.SyncServer;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.render.color.FixColor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * @author ConeTin
 * @since 12 янв. 2025 г.
 */

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ScheduleManager implements IAccess {
	
	ScheduleRecorder recorder = new ScheduleRecorder();
	List<Schedule> schedules = new ArrayList<>();
	
	public void onEvent(Event event) {
		recorder.onEvent(event);
		
		if (event instanceof EventUpdate) {
			if (mc.player.ticksExisted % 20 == 0) {
				ScheduleServer.update();
			}
			
			schedules.removeIf(schedule -> schedule.getSeconds() <= 0 && schedule.getShowingAnim().finished(false));
		}
	}
	
	public Schedule add(int anarcy, int time) {
		for (Schedule schedule : schedules) {
			if (schedule.getAnarchy() == anarcy) {
				schedule.setSeconds(time);
				return schedule;
			}
		}
		
		Schedule schedule = new Schedule(anarcy, time);
		schedules.add(schedule);
		return schedule;
	}
	
}
