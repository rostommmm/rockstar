package fun.rockstarity.api.render.animation.infinity;

import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import lombok.Getter;

/**
 * @author ConeTin
 * @since 6 дек. 2023 г.
 */

public class InfinityAnimation {
    
	private float output, endpoint;
	private Easing easing = Easing.LINEAR;

    @Getter
    private Animation animation = new Animation().setSize(0).setSpeed(0).setForward(false).setEasing(easing);

    public float animate(float destination, int ms) {
    	ms = Math.max(1, ms);
        output = endpoint - animation.get();
        endpoint = destination;
        if (output != (endpoint - destination)) {
            animation = new Animation().setSize(endpoint - output).setSpeed(ms).setForward(false).setEasing(easing);
        }
        return output;
    }

    public boolean finished() {
        return output == endpoint || animation.finished() || animation.finished(false);
    }

    public float get() {
        output = endpoint - animation.get();
        return output;
    }
    
    public InfinityAnimation easing(Easing easing) {
		this.easing = easing;
		
		return this;
	}
    
}
