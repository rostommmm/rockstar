package fun.rockstarity.api.render.menufilter;

import java.util.ArrayList;
import java.util.List;

import fun.rockstarity.client.modules.render.ClickGui;
import lombok.RequiredArgsConstructor;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.render.EventRender3D;
import fun.rockstarity.api.events.list.render.world.EventFogColor;
import fun.rockstarity.api.events.list.render.world.EventFogDistance;
import fun.rockstarity.api.events.list.render.world.EventFov;
import fun.rockstarity.api.events.list.render.world.EventStarsColor;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Style;
import lombok.experimental.UtilityClass;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;

/**
 * @author ConeTin
 * @since 26 янв. 2025 г.
 */

@UtilityClass
public class MenuFilter implements IAccess {
	
	private final Animation showing = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	private final List<Quad> quads = new ArrayList<>();
	
	public void onEvent(Event event) {
		if (showing.finished(false)) return;
		
	    if (event instanceof EventRender3D e) {
	        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
	        GL11.glPushMatrix();

	        MatrixStack ms = e.getMatrixStack();

	        RenderSystem.enableBlend();
	        RenderSystem.disableTexture();
	        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

	        GL11.glDepthMask(false);
	        GlStateManager.enableBlend();
	        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
	        GL11.glShadeModel(GL11.GL_SMOOTH);
	        GL11.glAlphaFunc(GL11.GL_GREATER, 0);

	        Tessellator tessellator = Tessellator.getInstance();
	        BufferBuilder builder = tessellator.getBuffer();
	        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

	        Matrix4f matrix = ms.getLast().getMatrix();

	        for (Quad quad : quads) {
	            quad.draw(e.getMatrixStack(), builder);
	        }

	        tessellator.draw();

	        GL11.glPopMatrix();
	        GL11.glPopAttrib();

	        RenderSystem.disableBlend();
	        RenderSystem.enableTexture();
	        Render.resetColor();
	    }
		

		if (event instanceof EventStarsColor e) {
			e.setColor(e.getColor().move(FixColor.WHITE.alpha(0.1f), showing.get()));
			e.cancel();
		}
		
		if (event instanceof EventFov e) {
			e.setFov(Math.max(1, e.getFov() - showing.get() * rock.getModules().get(ClickGui.class).getZoom().get()));
			e.setCancel(true);
		}
		
		if (event instanceof EventFogColor e) {
			e.setColor(e.getColor().move(FixColor.BLACK, showing.get()));
		}
		
		if (event instanceof EventFogDistance e) {
			e.setInner(MathUtility.interpolate(e.getInner(), 0, showing.get()));
			e.setOuter(MathUtility.interpolate(e.getOuter(), 5, showing.get()));
		}
	}
	
	public void show(boolean show) {
		showing.setForward(show);
		
		if (show) {
			showing.setSpeed(500);
			showing.setEasing(Easing.EASE_OUT_CIRC);
			
			quads.clear();
			
			float place = 30;
			float size = 1;
			float gap = 0.1f;
			
			float yOffset = (float) mc.player.getPositionVec().y;
			
			for (int i = 0; i < 5; i++) {
				 if (mc.world.getBlock(mc.player.getPosition().add(0,-i,0)) != Blocks.AIR/*
						 && !(mc.world.getBlockState(target.getPosition().add(0,-i,0)).getBlock() instanceof TallGrassBlock)
						 && !(mc.world.getBlockState(target.getPosition().add(0,-i,0)).getBlock() instanceof FlowerBlock)
						 && !(mc.world.getBlockState(target.getPosition().add(0,-i,0)).getBlock() instanceof DoublePlantBlock)
						 && !(mc.world.getBlockState(target.getPosition().add(0,-i,0)).getBlock() instanceof DeadBushBlock)
						 && !(mc.world.getBlockState(target.getPosition().add(0,-i,0)).getBlock() != Blocks.SNOW)*/) {
					 yOffset=i;
					 break;
				 }
			}
			
			for (float x = -place; x < place; x += size + gap) {
				for (float z = -place; z < place; z += size + gap) {
					Vector3d pos = mc.player.getPositionVec().add(x,-yOffset+1.001f,z);
					Quad quad = new Quad(pos);
			        quads.add(quad);
			        quad.showing.finish();
				}
			}
		}
	}
	
	public boolean active() {
		return showing.isForward();
	}
	
	static class Quad {
		
		Vector3d pos;
		final Animation showing = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);

		public Quad(Vector3d pos) {
			this.pos = pos;
		}

		void draw(MatrixStack ms, BufferBuilder builder) {
			float place = 20 * MenuFilter.showing.get();
			double dist = mc.player.getPositionVec().distanceTo(pos);
			showing.setForward(MenuFilter.showing.isForward() && !MenuFilter.showing.finished() && dist + 3 > place && dist + 2 < place);
			
			if (showing.finished(false)) return;
			
			FixColor color = Style.getPoint((int) (MenuFilter.quads.indexOf(this)*50)).alpha(showing.get() * (1-MenuFilter.showing.get()*MenuFilter.showing.get()));
			//FixColor color = FixColor.WHITE.alpha(showing.get() * (1-MenuFilter.showing.get()*MenuFilter.showing.get()));
			
			ms.push();
			Matrix4f matrix = ms.getLast().getMatrix();
			float size = 1;
		    
			ms.translate(pos.sub(Render.cameraPos()));

			builder.pos(matrix, 0, 0, 0).color(color).endVertex();
			builder.pos(matrix, 0, 0, size).color(color).endVertex();
			builder.pos(matrix, size, 0, size).color(color).endVertex();
			builder.pos(matrix, size, 0, 0).color(color).endVertex();

			ms.pop();
		}
		
	}
}
