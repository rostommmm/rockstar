package fun.rockstarity.api.render.globals.marks;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.events.list.render.EventRender3D;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.render.PositionTracker;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Style;
import lombok.Getter;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

/**
 * @author ConeTin
 * @since 28 сент. 2024 г.
 */

@Getter
public class Mark implements IAccess {
	
	private Vector3d position;
	private Vector2f wts; // Позиция на экране в 2D проекции
	private TimerUtility timer = new TimerUtility();
	private String user;
	
	// Анимки
	@Getter
	private Animation showing = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	private Animation blinking = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);

	public Mark(String user, Vector3d position) {
		this.position = position;
		this.user = user;
	}
	
	public void onEvent(Event event) {
		if (event instanceof EventRender2D e) {
			wts = null;
			
			double[] pos = Render.worldToScreen(position.x, position.y, position.z);
            
            if (PositionTracker.isInView(position) && pos != null)
            	wts = new Vector2f((float) pos[0], (float) pos[1]);
            
            if (wts == null) return;
            
			float size = 20;
			Render.image("icons/gps/point.png", wts.x - size / 2, wts.y - size / 2, size, size, Style.getMain().alpha((0.3f + blinking.get() * 0.7f) * showing.get()));
			
			String who = "От " + (user.equals(mc.player.getName().getString()) ? "Вас" : user);
			bold.get(14).draw(e.getMatrixStack(), who, wts.x - bold.get(14).getWidth(who)/2F, wts.y + 12, FixColor.WHITE.alpha((0.3f + blinking.get() * 0.7f) * showing.get()));
			
			if (blinking.finished())
				blinking.setForward(false);
			else if (blinking.finished(false))
				blinking.setForward(true);
			
			blinking.setSpeed(MathHelper.clamp((int) (1000 - timer.getElapsed()/4), 100, 1000));
			showing.setForward(!timer.passed(4000));
		}
	}
	
}
