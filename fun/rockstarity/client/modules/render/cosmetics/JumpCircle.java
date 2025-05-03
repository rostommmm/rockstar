package fun.rockstarity.client.modules.render.cosmetics;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventJump;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.render.EventRender3D;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.render.shaders.list.Glass;
import fun.rockstarity.client.modules.render.Beautifully;
import fun.rockstarity.client.modules.render.Cosmetics;
import fun.rockstarity.client.modules.render.Cosmetics.Cosmetic;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.optifine.shaders.Shaders;

public class JumpCircle extends Cosmetic {
	
	private final Slider speed;
	private final Slider size;
	private final List<Circle> circles = new ArrayList<>();
	
	public JumpCircle(Cosmetics ui, Select select) {
		super(select, "РЎС‘СЂРєР»С‹");
		this.size = new Slider(ui, "Р Р°Р·РјРµСЂ").min(1.5f).max(3.5f).inc(0.1f).set(2f).hide(() -> !get());
		this.speed = new Slider(ui, "РЎРєРѕСЂРѕСЃС‚СЊ").min(25).max(100).set(75).inc(5).hide(() -> !get());
	}
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventRender3D e) {
			MatrixStack ms = e.getMatrixStack();
			
			for (Circle circle : circles) {
				Vector3d pos = circle.pos();
				
				float size = (float) (this.size.get() * circle.getAnimation());
				
				if (rock.getModules().get(Beautifully.class).get() && rock.getModules().get(Beautifully.class).getBloom().get() && Shaders.shaderPackLoaded) {
					Glass.draw(FixColor.WHITE, 0, 0, 0);
					Glass.end();
				}
				RenderSystem.enableDepthTest();
				RenderSystem.enableCull();
				
				ms.push();
	            GlStateManager.depthMask(false);
	           	ms.translate(pos.x, pos.y-0.49f*size, pos.z);
	           	ms.rotate(Vector3f.XP.rotationDegrees(90));
	           	if (mc.getGameSettings().getPointOfView() == PointOfView.FIRST_PERSON) GL11.glDisable(GL11.GL_CULL_FACE);
	           	for (int i = 0; i < 14; i++) {
		           	ms.translate(0, 0, -0.03f);
		           	float alpha = (float) (circle.getAlphaAnimation() * (1-(float) (i / 14F)) / 2f);
	           		
	           		Render.drawImage(ms, "masks/circle.png", (float) -size / 2, -size / 2, (float) -size / 2, size, size, 
		            		Style.getPoint(90).alpha(alpha),
		            		Style.getPoint(180).alpha(alpha),
		            		Style.getPoint(240).alpha(alpha),
		            		Style.getPoint(360).alpha(alpha));
	           	}
	            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, 9729);
	            GlStateManager.depthMask(true);
	            ms.pop();
			}
			
		}
		
		if (event instanceof EventMotion) {
			circles.removeIf(Circle::update);
		}
		if (event instanceof EventJump) {
			circles.add(new Circle(mc.player.getPositionVec().add(0,0.05f,0)));
		}
	}

	
	private double createAnimation(double value) {
		return Math.sqrt(0.95f - Math.pow(value - 1, 2.0));
	}
	
	public class Circle {
		private final Vector3d vector;
		private double tick, prevTick;
		private Animation anim;
		private Animation out;

		public Circle(Vector3d vector) {
			this.vector = vector;
			this.tick = speed.get();
			this.prevTick = tick;
			anim = new Animation().setEasing(Easing.EASE_OUT_BACK).setSpeed(700);
			out = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(1000);
		}

		public double getAlphaAnimation() {
			return 1-out.get();
		}
		
		public double getAnimation() {
			return anim.get();
		}

		public boolean update() {
			anim.setForward(true);
			if (anim.finished() && this.tick <= 30) {
				this.out.setForward(true);
			}
			prevTick = tick;
			tick = tick - 0.75;
			return tick <= 0;
		}

		public Vector3d pos() {
			return new Vector3d(vector.x - mc.getRenderManager().info.getProjectedView().getX(), vector.y - mc.getRenderManager().info.getProjectedView().getY(), vector.z - mc.getRenderManager().info.getProjectedView().getZ());
		}
	}
}
