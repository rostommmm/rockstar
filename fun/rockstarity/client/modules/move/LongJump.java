package fun.rockstarity.client.modules.move;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.player.Move;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Mode;
import lombok.Getter;
import net.minecraft.block.Blocks;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author Malecharik
 * @since 9 Mar 2024 00:21:15
 */


@Getter
@Info(name = "LongJump", desc = "Прыжки на дальние дистанции", type = Category.MOVE)
public class LongJump extends Module {

	private double pol;
	private boolean boost;

	private final Mode mode = new Mode(this, "Режим");

	private final Mode.Element ft = new Mode.Element(mode, "FunTime");
	
	@Override
	public void onEvent(Event event) {
		if (this.mode.is(this.ft)) {
			if (event instanceof EventUpdate && Move.isMoving()) {
				if (mc.player.isOnGround()) {
					boost = false;
				}
				
				if (mc.player.isOnGround() && Player.getBlock(0,-1,0) == Blocks.AIR) {
					if (mc.player.isOnGround() || mc.player.fallDistance < mc.player.stepHeight && !mc.world.hasNoCollisions(mc.player, mc.player.getBoundingBox().offset(0.0D, (double)(mc.player.fallDistance - mc.player.stepHeight), 0.0D))) {
						mc.player.jump();
						//Move.setSpeed(0.5f);
						boost = true;
					}
				}
				
				if (boost && mc.player.fallDistance < 0.5f) {
					//Move.setSpeed(0.4f);
					mc.player.setMotion(mc.player.getMotion().mul(1.05f, 1, 1.05f));
				}
			}
			
			/*
			if (event instanceof EventUpdate) {
				if (!mc.player.isOnGround()) {
					//mc.player.getMotion().x *= 1.5f;
					//mc.player.getMotion().z *= 1.5f;
					if (mc.player.fallDistance == 0) {
						Move.setSpeed(0.5f);
					}
					//mc.getGameSettings().keyBindSneak.setPressed(false);
				} else {
					Move.setSpeed(0);
					mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, Action.PRESS_SHIFT_KEY));
					mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, Action.RELEASE_SHIFT_KEY));
				}
				
	            int slot = -1; 
	            for (int i = 0; i < 9; i++) {
	                if (mc.player.inventory.getStackInSlot(i).getItem() == Items.STONE_SLAB) { 
	                    slot = i; 
	                    break;
	                }
	            }
	            
	           // mc.player.inventory.currentItem = slot;
	            
	            BlockPos pos = mc.player.getPosition();
	            if (slot != -1 && mc.player.fallDistance > 0.3f && Player.getBlock() != Blocks.STONE_SLAB) {
	            	for (int i = 0; i < 2; i++) {
						BlockPos target = pos.down(1).up(i);
						BlockRayTraceResult result = new BlockRayTraceResult(new Vector3d(target.getX(), target.getY(), target.getZ()), Direction.UP, target, false);
						mc.player.connection.sendPacket(new CPlayerTryUseItemOnBlockPacket(Hand.MAIN_HAND, result));
						if (mc.world.getBlockState(target).getBlock() == Blocks.AIR)
							mc.world.setBlockState(target, Blocks.STONE_SLAB.getDefaultState());
			        }
	            }
	           
			}
			
			if (event instanceof EventMotion e) {
				this.pol = (float) Interpolator.LINEAR.interpolate(this.pol, 90, 0.7f);
				mc.player.rotationPitchHead = (float) this.pol;
				e.setPitch((float) this.pol);
			}
			
			if (event instanceof EventTrace e) {
				e.setPitch((float) this.pol);
				e.cancel();
			}
			 */
		}
	}
	@NativeInclude
	@Override
	public void onEnable() {
		pol = mc.player.rotationPitch;
	}
	@NativeInclude
	@Override
	public void onDisable() {
		mc.getGameSettings().keyBindSneak.setPressed(false);
	}
}
