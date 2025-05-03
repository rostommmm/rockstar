package fun.rockstarity.api.render.particles;

import java.util.ArrayList;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.events.list.render.EventRender3D;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.render.animation.infinity.InfinityAnimation;
import fun.rockstarity.api.render.color.themes.Style;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

/**
 * @author ConeTin
 * @since 9 мая 2024 г.
 */


public class WorldParticles extends CheckBox {

	private final TimerUtility addingTimer = new TimerUtility();
	private final ArrayList<Particle3D> particles = new ArrayList<>();
	private final InfinityAnimation rotating = new InfinityAnimation();
	
	private int mode = 1;
	
	private final ArrayList<UppingParticle3D> uppingParticles = new ArrayList<>();
	
	public WorldParticles(Module parent) {
		super(parent, "Частицы");
	}

	public void onEvent(Event event) {
		/*
		mode = 1;
		if (mode == 0) {
			if (event instanceof EventUpdate) {
				this.rotating.animate(this.rotating.get() + 20, 100);
				
				if (this.addingTimer.passed(200)) {
					int offset = 50;
					Vector3d pos = mc.player.getPositionVec().add(MathUtility.random(-offset, offset),MathUtility.random(-offset, offset),MathUtility.random(-offset, offset));
					if (mc.world.getBlockState(new BlockPos(pos)).getBlock() == Blocks.AIR)
						this.particles.add(new Particle3D(pos, new Vector3d(0, 0, 0), Math.max(1, 1 / Math.max((float) Minecraft.debugFPS, 5) * 140)));
					// TODO reset
				}
			}
			
			if (event instanceof EventRender3D e) {
				ArrayList<Particle3D> removing = new ArrayList<>();
				
				Render.startImageRendering("masks/glow.png");
				
				for (Particle3D particle : this.particles) {
					float dist = (float) mc.player.getPositionVec().distanceTo(particle.getPosition());
					float angle = this.rotating.get() + this.particles.indexOf(particle) * 360/this.particles.size();
					float x = (float) Math.sin(Math.toRadians(angle)) * dist;
					float z = (float) Math.cos(Math.toRadians(angle)) * dist;
					float mul = 0.005f / Math.max((float) Minecraft.debugFPS, 5) * 50;
					
					particle.setMotion(new Vector3d(x, 0, z).mul(mul, mul, mul));

					particle.render(e);
					//particle.update();
					particle.setAlpha((float) (particle.getAlpha() - 0.001 / Math.max((float) Minecraft.debugFPS, 5) * 100));
					
					if (particle.getAlpha() < 0) removing.add(particle);
				}
				
				Render.finishImageRendering();
				
				this.particles.removeAll(removing);
			}
		} else if (mode == 1) {
			if (event instanceof EventUpdate) {
				for (int i = 0; i < 15; i++) {
					int offset = 20;
					Vector3d pos = mc.player.getPositionVec().add(MathUtility.random(-offset, offset),MathUtility.random(-7, 2),MathUtility.random(-offset, offset));
					if (mc.world.getBlockState(new BlockPos(pos)).getBlock() != Blocks.AIR && mc.world.getBlockState(new BlockPos(pos).add(0,1,0)).getBlock() == Blocks.AIR) {
						this.uppingParticles.add(new UppingParticle3D(pos, new Vector3d(0, 0, 0), Style.getPoint((int)MathUtility.random(0, 20)), 0.2f));
					}
				}
			}
			
			if (event instanceof EventRender3D e) {
				ArrayList<UppingParticle3D> removing = new ArrayList<>();
				
				Render.startImageRendering("masks/glow.png");
				
				for (UppingParticle3D particle : this.uppingParticles) {
					particle.render(e);
					particle.update();
					
					if (particle.getAlpha() < 0) removing.add(particle);
				}
				
				Render.finishImageRendering();
				
				this.uppingParticles.removeAll(removing);
			}
		}
		*/
	}
	
}
