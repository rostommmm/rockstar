package fun.rockstarity.api.render.animation;

import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.math.TimerUtility;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Direction;

@Getter
public class Animation {
	private final TimerUtility timer = new TimerUtility();
    private int speed;
    private double size = 1;
    @Getter
    private boolean forward;
    private Easing easing;

    public boolean finished(boolean forward) {
        return timer.passed(speed) && (forward ? this.forward : !this.forward);
    }
    
    public boolean finished() {
        return timer.passed(speed) && this.forward;
    }
    
    public Animation setForward(boolean forward) {
    	if (this.forward != forward) {
    		this.forward = forward;
        	timer.setStartTime((long) (System.currentTimeMillis() - (size - Math.min(size, timer.getElapsed()))));
    	}
    	return this;
    }
    
    public Animation finish() {
    	timer.setStartTime((long) (System.currentTimeMillis()-speed));
    	return this;
    }
    
    public Animation setEasing(Easing easing) {
    	this.easing = easing;
    	return this;
    }
    
    public Animation setSpeed(int speed) {
    	this.speed = speed;
    	return this;
    }
    
    public Animation setSize(float size) {
    	this.size = size;
    	return this;
    }
    
    public float getLinear() {
    	if (forward) {
            if (timer.passed(speed)) {
                return (float) size;
            }

            return (float) (timer.getElapsed() / (double) speed * size);
        } else {
            if (timer.passed(speed)) {
                return 0.0f;
            }

            return (float) ((1 - timer.getElapsed() / (double) speed) * size);
        }
    }
    
    public float get() {
    	if (forward) {
            if (timer.passed(speed)) {
                return (float) size;
            }

            return (float) (easing.apply(timer.getElapsed() / (double) speed) * size);
        } else {
            if (timer.passed(speed)) {
                return 0.0f;
            }

            return (float) ((1 - easing.apply(timer.getElapsed() / (double) speed)) * size);
        }
    }
    
    public float reversed() {
    	return 1-get();
    }
    
    public void reset() {
    	timer.reset();
    }
    
    public void kuni() {
    	if (finished())
    		setForward(false);
    	else if (finished(false))
    		setForward(true);
    }
    
}
