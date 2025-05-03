package fun.rockstarity.client.modules.render;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventAttack;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.events.list.render.EventRender3D;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.math.aura.Rotation;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.menufilter.MenuFilter;
import fun.rockstarity.api.render.shaders.list.Glass;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Info(name="Torus", desc="Красивые водянистые бублики", type=Category.RENDER)
public class Torus extends Module {
	
	Slider noise = new Slider(this, "Искажение").min(1).max(10).inc(1).set(5).desc("Сила искажения бублика");
	Slider reflect = new Slider(this, "Водянистость").min(50).max(100).inc(5).set(100).desc("Насколько сильным будет эффект \"Воды\"");
	Slider blur = new Slider(this, "Размытие").min(0).max(10).inc(1).set(0).desc("Насколько сильным будет эффект размытия");
	Slider sizeSetting = new Slider(this, "Размер").min(0.5f).max(1.5f).inc(0.1f).set(0.5f);
	
	ArrayList<TorusObj> toruses = new ArrayList<>();
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventAttack e) {
			Vector3d particlePos = e.getTarget().getPositionVec().add(0, e.getTarget().getHeight()/2F, 0);
			TorusObj particle = new TorusObj(particlePos);

	        toruses.add(particle);
		}
		
		if (event instanceof EventRender2D e) {
			//Glass.draw();
			//Shader.drawQuads(e.getMatrixStack());
			//Glass.end();
		}
		
		if (event instanceof EventRender3D e) {
			toruses.removeIf(torus -> torus.shouldRemove());
			
			if (toruses.isEmpty()) return;
			
	        RenderSystem.enableBlend();
	        RenderSystem.disableTexture();
	        RenderSystem.defaultBlendFunc();
	        
	        Glass.draw(FixColor.WHITE, noise.get(), reflect.get(), blur.get()*5F);
	        Tessellator tessellator = Tessellator.getInstance();
	        BufferBuilder builder = tessellator.getBuffer();
	        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
	    	MatrixStack ms = e.getMatrixStack();

	        for (TorusObj particle : toruses) {
	            particle.render(e.getMatrixStack(), builder);
	        }
	        
	        tessellator.draw();
	        Glass.end();
	        RenderSystem.disableBlend();
	        RenderSystem.enableTexture();
	        Render.resetColor();
		}
	}
	
	@Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}
	
	class TorusObj {

		final Vector2f rotation;
		Animation sizing = new Animation().setEasing(Easing.BOTH_CIRC).setSpeed(300);
		TimerUtility timer = new TimerUtility();
		Vector3d pos;
		float size = 1.0F;
		long lifeTime;
		
		public TorusObj(Vector3d pos) {
			Vector3d sub = mc.player.getEyePosition(1).sub(pos).mul(0.5f);
			this.pos = pos.add(MathHelper.clamp(sub.x, -0.5f, 0.5f), MathHelper.clamp(sub.y, -1, 1), MathHelper.clamp(sub.z, -0.5f, 0.5f));
			rotation = Rotation.get(pos);
			lifeTime = 1000;
		}
		
		public void render(MatrixStack matrices, BufferBuilder builder) {
			sizing.setEasing(Easing.EASE_OUT_CIRC);
			sizing.setSpeed((int) lifeTime);
		    sizing.setForward(true);

		    matrices.push();

		    FixColor render = FixColor.WHITE.alpha(1);

		    float size = sizeSetting.get();
		    float radius = sizing.get()*size;
		    float innerRadius = (size-sizing.get()*size) * radius;
		    float outerRadius = (sizing.get()) * radius;

		    Matrix4f matrix = matrices.getLast().getMatrix();
		       
		    Vector3d camera = Render.cameraPos();
		    double x = pos.getX() - camera.x;
		    double y = pos.getY() - camera.y;
		    double z = pos.getZ() - camera.z;

		    matrices.translate(x, y, z);

		    matrices.rotate(Vector3f.YN.rotation((float) Math.toRadians(rotation.x)));
		    matrices.rotate(Vector3f.XN.rotation((float) Math.toRadians(-rotation.y + 90)));

		    int segments = 30;

		    for (int i = 0; i < segments; i++) {
		        double theta1 = 2 * Math.PI * i / segments;
		        double theta2 = 2 * Math.PI * (i + 1) / segments;

		        for (int j = 0; j < segments; j++) {
		            double phi1 = 2 * Math.PI * j / segments;
		            double phi2 = 2 * Math.PI * (j + 1) / segments;

		            float x1 = (float) ((outerRadius + innerRadius * Math.cos(phi1)) * Math.cos(theta1));
		            float y1 = (float) (innerRadius * Math.sin(phi1));
		            float z1 = (float) ((outerRadius + innerRadius * Math.cos(phi1)) * Math.sin(theta1));

		            float x2 = (float) ((outerRadius + innerRadius * Math.cos(phi2)) * Math.cos(theta1));
		            float y2 = (float) (innerRadius * Math.sin(phi2));
		            float z2 = (float) ((outerRadius + innerRadius * Math.cos(phi2)) * Math.sin(theta1));

		            float x3 = (float) ((outerRadius + innerRadius * Math.cos(phi2)) * Math.cos(theta2));
		            float y3 = (float) (innerRadius * Math.sin(phi2));
		            float z3 = (float) ((outerRadius + innerRadius * Math.cos(phi2)) * Math.sin(theta2));

		            float x4 = (float) ((outerRadius + innerRadius * Math.cos(phi1)) * Math.cos(theta2));
		            float y4 = (float) (innerRadius * Math.sin(phi1));
		            float z4 = (float) ((outerRadius + innerRadius * Math.cos(phi1)) * Math.sin(theta2));

		            builder.pos(matrix, x1, y1, z1).color(render.getRGB()).endVertex();
		            builder.pos(matrix, x2, y2, z2).color(render.getRGB()).endVertex();
		            builder.pos(matrix, x3, y3, z3).color(render.getRGB()).endVertex();
		            builder.pos(matrix, x4, y4, z4).color(render.getRGB()).endVertex();
		        }
		    }

		    matrices.pop();
		}


		public boolean shouldRemove() {
			return timer.passed(lifeTime);
		}
		
	}
}
