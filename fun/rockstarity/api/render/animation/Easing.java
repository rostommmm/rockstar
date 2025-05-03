package fun.rockstarity.api.render.animation;

import net.minecraft.client.Minecraft;

/**
 * @author ConeTin
 * @since 4 дек. 2023 г.
 */

public enum Easing {
	
	LINEAR(x -> x),
	BOTH_SINE(x -> -(Math.cos(Math.PI * x) - 1) / 2),
	BOTH_CIRC(x -> x < 0.5 ? (1 - Math.sqrt(1 - Math.pow(2 * x, 2))) / 2 : (Math.sqrt(1 - Math.pow(-2 * x + 2, 2)) + 1) / 2),
	BOTH_CUBIC(x -> x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2),
	EASE_IN_OUT_QUART(x -> x < 0.5 ? 8 * x * x * x * x : 1 - Math.pow(-2 * x + 2, 4) / 2),
	EASE_OUT_BACK(x -> 1 + 2.70158 * Math.pow(x - 1, 3) + 1.70158 * Math.pow(x - 1, 2)),
	TARGETESP_EASE_OUT_BACK(x -> 1 + 3.70158 * Math.pow(x - 1, 3) + 2.70158 * Math.pow(x - 1, 2)),
	EASE_OUT_CIRC(x -> Math.sqrt(1 - Math.pow(x - 1, 2))),
	EASE_OUT_BOUNCE(x -> easeOutBounce(x)),
	SMOOTH_STEP(x -> -2 * Math.pow(x, 3) + (3 * Math.pow(x, 2)));

	private static double easeOutBounce(double x) {
		return 0;
	}
	
    private final Function function;

    Easing(Function function) {
        this.function = function;
    }

    public double apply(double arg) {
        return function.apply(arg);
    }
    
}
