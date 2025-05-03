package fun.rockstarity.api.render.particles;

import java.util.ArrayList;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Theme;

/**
 * @author ConeTin
 * @since 23 С„РµРІСЂ. 2024 Рі. 22:51:14
 */

public class ParticleEngine implements IAccess {

	private final ArrayList<Particle2D> particles = new ArrayList<>();
	private final TimerUtility timer = new TimerUtility();
	
	public void render(MatrixStack matrixStack) {
		Particle2D removed = null;
		
		for (Particle2D particle : this.particles) {
			particle.render(matrixStack);
			if (particle.getAlpha() <= 0) {
				removed = particle;
			}
		}
		
		if (removed != null) {
			this.particles.remove(removed);
		}
	}
	
	public void spawn(float x, float y, float motionX, float motionY, FixColor color) {
		if (this.timer.passed(5)) {
			this.particles.add(new Particle2D(x, y, motionX, motionY, color));
			this.timer.reset();
		}
	}
	
}
