package fun.rockstarity.api.render.particles;

import java.util.ArrayList;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.events.list.render.EventRender3D;
import fun.rockstarity.api.helpers.render.PositionTracker;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.render.color.themes.Style;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector4f;

/**
 * @author ConeTin
 * @since 15 мар. 2024 г.
 */

public class Particle3D implements IAccess {

	private Vector3d prevPosition = Vector3d.ZERO;
	@Getter @Setter
	private Vector3d position, motion;
	private final ArrayList<Vector4f> tail = new ArrayList<>();
	@Getter @Setter
	private float alpha = 1;
	private final float size;
	
	public Particle3D(Vector3d position, Vector3d motion) {
		this(position, motion, 0.8f);
	}
	
	public Particle3D(Vector3d position, Vector3d motion, float size) {
		this.position = position;
		this.motion = motion;
		this.size = size;
	}
	
	public void render(EventRender3D e) {
		this.prevPosition = new Vector3d(this.position.x, this.position.y, this.position.z);
		this.position = this.position.add(motion);
		
		MatrixStack ms = e.getMatrixStack();
		float size = this.size;
		float length = 30;
		
		double x = this.position.x;
		double y = this.position.y;
		double z = this.position.z;
		
		double dx = x - this.prevPosition.x;
	    double dy = y - this.prevPosition.y;
	    double dz = z - this.prevPosition.z;
	    double entitySpeed = Math.sqrt(dx * dx + dy * dy + dz * dz);
		int countMax = MathHelper.clamp((int)(entitySpeed / 0.045), 1, 16);
		int count = 0;
       ///* for (int count = 0; count < countMax; ++count)*/ {
       // 	double posX = this.prevPosition.x - dx * (double)(float)count / (float)countMax + ((double)-0.0875f + (double)0.175f * Math.random());
       // 	double posY = this.prevPosition.y - dy * (double)(float)count / (float)countMax + ((double)2 / 3.0 + (double)2 / 4.0 * Math.random() * (double)0.7f);
       // 	double posZ = this.prevPosition.z - dz * (double)(float)count / (float)countMax + ((double)-0.0875f + (double)0.175f * Math.random());
        	tail.add(new Vector4f((float) x, (float) y + 0.7f, (float) z, length));
       // }
        
        ArrayList<Vector4f> vec4f = new ArrayList<>();
    	for (Vector4f vec : this.tail) {
    		if (vec.getW() > 0) {
    			float miniSize = size * vec.getW() / length;
    			
    			double posX = vec.getX() - mc.getRenderManager().info.getProjectedView().x;
                double posY = vec.getY() - mc.getRenderManager().info.getProjectedView().y;
                double posZ = vec.getZ() - mc.getRenderManager().info.getProjectedView().z;
                
                if (PositionTracker.isInView(new Vector3d(vec.getX(), vec.getY(), vec.getZ()))) {
                	ms.push();
                   	ms.translate(posX, posY, posZ);
                   	ms.rotate(mc.getRenderManager().info.getRotation());
                    
                    Render.drawCleanImage(ms, (float) -miniSize / 2, -miniSize / 2, (float) -miniSize / 2, miniSize, miniSize, Style.getPoint((int) (12 * vec.getW())).alpha(vec.getW() / length * alpha));
                    ms.pop();
                }
                
                vec.set(vec.getX(), vec.getY() + 0.004f / Math.max((float) Minecraft.debugFPS, 5) * 300, vec.getZ(), vec.getW() - 0.3f / Math.max((float) Minecraft.debugFPS, 5) * 300);
                if (vec.getW() <= 0)
                	vec4f.add(vec);
    		}
        }
    	
    	if (this.alpha < 0) {
    		//this.motion = this.motion.mul(-1,-1,-1);
    		//this.alpha = 1;
    	}
    	
    	for (Vector4f vec : vec4f) {
    		this.tail.remove(vec);
    	}
	}
	
	public void update() {
		this.alpha -= 0.001 / Math.max((float) Minecraft.debugFPS, 5) * 300;
    	this.motion = this.motion.mul(0.95 / Math.max((float) Minecraft.debugFPS, 5) * 300, 0.95 / Math.max((float) Minecraft.debugFPS, 5) * 300, 0.95 / Math.max((float) Minecraft.debugFPS, 5) * 300);
	}
	
}
