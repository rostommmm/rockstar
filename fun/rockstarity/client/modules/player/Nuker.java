package fun.rockstarity.client.modules.player;

import java.util.ArrayList;
import java.util.List;

import fun.rockstarity.api.modules.settings.list.Slider;
import org.lwjgl.opengl.GL11;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.inputs.EventInput;
import fun.rockstarity.api.events.list.player.EventJump;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventMove;
import fun.rockstarity.api.events.list.player.EventTrace;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.events.list.render.world.EventRenderWorld;
import fun.rockstarity.api.helpers.math.aura.Rotation;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.render.color.themes.Style;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

/**
 * @author Malecharik
 * @since 12 мая 2024 г. 12:20:53
 */


@Info(name="Nuker", desc="Копает территорию вокруг себя", type=Category.PLAYER)
public class Nuker extends Module {
	
	private BlockPos target;
	private float yaw, pitch;
	private List<BlockPos> breaked = new ArrayList<>();
	Slider ranged = new Slider(this, "Дистанция").min(2).max(6).inc(0.1f).set(3);
	Slider count = new Slider(this, "Задержка").min(4).max(20).inc(1).set(11);
	private long lastBlockBreakTime;
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventUpdate) {
	        int range = (int) ranged.get();
	        long blockBreakInterval = 1;
	        int blocksToBreakCount = (int) count.get();
	        
	        if (System.currentTimeMillis() - this.lastBlockBreakTime >= blockBreakInterval) {
	        	if (breaked.isEmpty()) {
	        		 Vector3d playerPos = mc.player.getPositionVec();
	        		 
	        		 for (int dx = -range; dx <= range; ++dx) {
	        			for (int dy = -range; dy <= range; ++dy) {
	        				for (int dz = -range; dz <= range; ++dz) {
	        					BlockPos targetPos = new BlockPos(playerPos.x + dx, playerPos.y + dy, playerPos.z + dz);
	        					
	        					if (mc.world.getBlockState(targetPos).getBlock() != Blocks.AIR) {
	        						breaked.add(targetPos);
	        					}
	        				}
	        			}
	        		 }
	        	}
	        	
	        	for (int i = 0; i < blocksToBreakCount && !breaked.isEmpty(); i++) {
	        		BlockPos targetPos = breaked.remove(0);
	                mc.player.swing(Hand.MAIN_HAND, false);
	                mc.playerController.onPlayerDamageBlock(targetPos, Direction.UP);
	                this.lastBlockBreakTime = System.currentTimeMillis();
	        	}
	        }
		}
		
		 if (event instanceof EventMotion e) {
			// mc.getGameSettings().keyBindSneak.setPressed(true);
			// mc.getGameSettings().keyBindForward.setPressed(true);
			 
			 if (this.target != null) {
				 Vector2f rots = Rotation.get(this.target.getVec().add(0.5f, 0.5f, 0.5f));
				 e.setYaw(rots.x);
				 e.setPitch(rots.y);
				 mc.player.rotationYawHead = rots.x;
				 mc.player.renderYawOffset = rots.x;
				 mc.player.rotationPitchHead = rots.y;
				 
				 this.yaw = rots.x;
				 this.pitch = rots.y;
				 
				 this.target = null;
			 }
		 }
		 
		 if (this.target == null) return;
		 
		 if (event instanceof EventTrace e) {
			 e.setYaw(yaw);
			 e.setPitch(pitch);
			 e.cancel();
		 }
		 
		 if (event instanceof EventMove e) {
			 e.setYaw(yaw);
			 e.setPitch(pitch);
		 }
		 
		 if (event instanceof EventInput e) {
			 e.setYaw(yaw);
		 }
		 
		 if (event instanceof EventJump e) {
			 e.setYaw(yaw);
		 }
	}

	@Override
	public void onEnable() {
		this.breaked.clear();
	}

	@Override
	public void onDisable() {

	}
	
}
