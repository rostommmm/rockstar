package fun.rockstarity.api.render.animation.infinity;

import fun.rockstarity.api.render.color.FixColor;
import net.minecraft.util.math.vector.Vector3f;

/**
 * @author ConeTin
 * @since 6 дек. 2023 г.
 */

public class ColorAnimation {
	   
	private final InfinityAnimation r = new InfinityAnimation();
	private final InfinityAnimation g = new InfinityAnimation();
	private final InfinityAnimation b = new InfinityAnimation();
	
    public FixColor animate(FixColor destination, int ms) {
        return new FixColor(
        		r.animate(destination.getRed(), ms),
        		g.animate(destination.getGreen(), ms),
        		b.animate(destination.getBlue(), ms)
        );
    }

}
