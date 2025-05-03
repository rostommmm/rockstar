package fun.rockstarity.api.render.particles;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.events.list.render.EventRender3D;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.animation.infinity.InfinityAnimation;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.render.shaders.fog.Vector2d;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

/**
 * @author ConeTin
 * @since 4 РёСЋРЅ. 2024 Рі.
 */


public class PetalParticle implements IAccess {

	private final Vector3d startPosition;
	@Getter private Vector3d position, motion;
	@Getter private float alpha;
	private float rotation;
	private final int type, point;
	private final float size;
	private int direction;
	private boolean falling;
	private final InfinityAnimation fallingAnim = new InfinityAnimation();
	private final Animation swingAnim = new Animation().setEasing(Easing.EASE_IN_OUT_QUART).setSpeed(1500).setSize(2);
	
	public PetalParticle(Vector3d pos) {
		this.position = this.startPosition = pos;
		this.motion = new Vector3d(MathUtility.random(-0.3f, 0.3f), MathUtility.random(1, 2), MathUtility.random(-0.3f, 0.3f));
		
		this.type = MathUtility.randomInt(0, 3);
		this.point = MathUtility.randomInt(0, 150);
		this.rotation = MathUtility.randomInt(0, 360);
		this.direction = MathUtility.randomInt(0, 360);
		this.size = 0.3f;
		this.alpha = 1;
	}
	
	public void render(EventRender3D e) {
		MatrixStack ms = e.getMatrixStack();
		
		this.position = this.position.add(motion.mul(Vector3d.copy(0.03f)));
		Vector3d renderPos = this.position.subtract(mc.getRenderManager().info.getProjectedView());
		if (this.position.distanceTo(startPosition) > 0 || this.falling) {
			if (this.motion.y > 0.1f) {
				this.motion.y *= 0.99f;
			} else if (this.motion.y > 0) {
				this.motion.y = this.fallingAnim.animate(0, 150);
			}else {
				this.motion.y = this.fallingAnim.animate(-0.05f, 150);
				
				if (this.motion.y < -0.045f) {
					if (Math.abs(this.motion.x) < 0.05f && Math.abs(this.motion.z) < 0.05f) {
						Vector2d directionMotions = MathUtility.circleCords(this.direction, 0.07f);
						this.motion.x = directionMotions.x * (this.swingAnim.get()-1);
						this.motion.z = directionMotions.y * (this.swingAnim.get()-1);
						this.alpha -= 0.01f / Math.max((float) Minecraft.debugFPS, 5) * 30;
						
						if (this.swingAnim.finished())
							this.swingAnim.setForward(false);
						else if (this.swingAnim.finished(false))
							this.swingAnim.setForward(true);
					} else {
						this.motion.x *= 0.99f;
						this.motion.z *= 0.99f;
					}
				}
			}
			this.falling = true;
		} else {
			this.motion = this.motion.mul(Vector3d.copy(0.9999f));
		}

		ms.push();
       	ms.translate(renderPos.x, renderPos.y, renderPos.z);
       	ms.rotate(mc.getRenderManager().info.getRotation());
       	ms.rotate(Vector3f.ZN.rotation(this.rotation));

       	Render.drawImage(ms, "masks/petals/petal" + this.type + ".png", (float) -size / 2, -size / 2, (float) -size / 2, size, size, Style.getPoint(point).alpha(alpha));
       
       	ms.pop();
	}
	
}
