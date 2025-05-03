package fun.rockstarity.api.scripts.wrappers.base;

import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.infinity.InfinityAnimation;
import net.minecraft.util.Direction;

public class AnimBase {
    public InfinityAnimation anim = new InfinityAnimation();
   
    public void animate(float destination, int ms) {
        anim.animate(destination, ms);
    }

    public boolean isDone() {
        return anim.finished();
    }
    
    public boolean done() {
    	return isDone();
    }
    
    public float get() {
    	return getOutput();
    }
   
    public float getOutput() {
        return anim.get();
    }
}