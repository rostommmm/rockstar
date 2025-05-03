package fun.rockstarity.api.render.globals.marks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.inputs.EventKey;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.client.modules.other.Globals;
import lombok.Getter;
import lombok.experimental.UtilityClass;

/**
 * @author ConeTin
 * @since 28 сент. 2024 г.
 */

@UtilityClass
public class MarkManager implements IAccess {
	
	@Getter
	private final List<Mark> marks = new ArrayList<>();

 	public void onEvent(Event event) {
		if (event instanceof EventKey e && (mc.currentScreen == null || e.isReleased())) handleKeys(e);
		
		Iterator<Mark> iterator = marks.iterator();

		try {
			synchronized (marks) {
				while (iterator.hasNext()) {
			        Mark mark = iterator.next();
			        mark.onEvent(event);
			        
			        if (event instanceof EventRender2D) {
			            if (mark.getShowing().finished(false)) {
			                marks.remove(mark);
			            }
			        }
			    }
			}
		} catch (Exception e2) {
		}
    }
 	
 	private void handleKeys(EventKey e) {
 		if (rock.getModules().get(Globals.class).getMarkBind().getBindByKey(e).isPresent() && !e.isReleased()) {
 			Mark mark = new Mark(mc.player.getNameClear(), MathUtility.rayTrace(150, mc.player.rotationYaw, mc.player.rotationPitch, mc.player).getHitVec());
 			mark.getShowing().finish();
			marks.add(mark);
			MarkServer.send(mark);
 		}
 	}
	
}
