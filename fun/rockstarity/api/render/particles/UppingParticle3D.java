package fun.rockstarity.api.render.particles;

import java.util.ArrayList;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.events.list.render.EventRender3D;
import fun.rockstarity.api.helpers.render.PositionTracker;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Style;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector4f;

/**
 * @author ConeTin
 * @since 9 мая 2024 г.
 */

public class UppingParticle3D implements IAccess {
	
	@Getter @Setter
	private Vector3d position, motion;
	@Getter @Setter
	private float alpha = 1;
	private final float size;
	private FixColor color;
	
	public UppingParticle3D(Vector3d position, Vector3d motion, FixColor color) {
		this(position, motion, color, 0.8f);
	}
	
	public UppingParticle3D(Vector3d position, Vector3d motion, FixColor color, float size) {
		this.position = position;
		this.motion = motion;
		this.size = size;
		this.color = color;
	}
	
	public void render(EventRender3D e) {
		this.position = this.position.add(motion);
		
		MatrixStack ms = e.getMatrixStack();
		float size = this.size;
		float miniSize = 0.05f;
		float length = 30;
		
		double x = this.position.x;
		double y = this.position.y;
		double z = this.position.z;
		
       if (PositionTracker.isInView(new Vector3d(x,y,z))) {
    	   double posX = x - mc.getRenderManager().info.getProjectedView().x;
           double posY = y - mc.getRenderManager().info.getProjectedView().y;
           double posZ = z - mc.getRenderManager().info.getProjectedView().z;
           
       		ms.push();
          	ms.translate(posX, posY, posZ);
          	ms.rotate(mc.getRenderManager().info.getRotation());
          	
        	Render.drawCleanImage(ms, (float) -size / 2, -size / 2, (float) -size / 2, size, size, this.color.alpha(alpha/2));
          	Render.drawCleanImage(ms, (float) -miniSize / 2, -miniSize / 2, (float) -size / 2, miniSize, miniSize, this.color.alpha(alpha));
        	Render.drawCleanImage(ms, (float) -size / 2, -size / 2, (float) -size / 2, size, size, this.color.alpha(alpha/2));
          	Render.drawCleanImage(ms, (float) -miniSize / 2, -miniSize / 2, (float) -size / 2, miniSize, miniSize, this.color.alpha(alpha));
        	Render.drawCleanImage(ms, (float) -size / 2, -size / 2, (float) -size / 2, size, size, this.color.alpha(alpha/2));
          	Render.drawCleanImage(ms, (float) -miniSize / 2, -miniSize / 2, (float) -size / 2, miniSize, miniSize, this.color.alpha(alpha));
          	ms.pop();
       }
	}
	
	public void update() {
		this.alpha -= 0.001 / Math.max((float) Minecraft.debugFPS, 5) * 100;
		this.motion.y = .001f / Math.max((float) Minecraft.debugFPS, 5) * 500;
	}
	
}
