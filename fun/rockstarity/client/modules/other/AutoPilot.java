package fun.rockstarity.client.modules.other;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventTrace;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.player.Inventory;
import fun.rockstarity.api.helpers.player.Move;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.client.commands.WayCommand;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.gen.Heightmap;

/**
 * @author Malecharik
 * @since 22 мая 2024 г. 20:12:40
 */

@Info(name = "AutoPilot", desc = "Автоматически летит на ивент", type = Category.OTHER)
public class AutoPilot extends Module {

	private Vector3d target = Vector3d.ZERO;
	private float yaw, pitch;
	private final TimerUtility timer = new TimerUtility();

	private final CheckBox swapChestplate = new CheckBox(this, "Брать нагрудник").desc("Брать нагрудник по прилёту на координаты");
	
	@Override
	public void onEvent(Event event) {
		
		
		if (event instanceof EventMotion e) {
			target = WayCommand.points.values().stream().findFirst().orElse(null);
			if (target != null && target != Vector3d.ZERO && mc.player.isElytraFlying()) {
				Player.look(event, yaw, pitch, true);
				e.setYaw(yaw);
				e.setPitch(pitch);
				
				
				Vector3d vec = target.subtract(mc.player.getEyePosition(mc.getRenderPartialTicks())).normalize();
				float rawYaw = (float) Math.toDegrees(Math.atan2(-vec.x, vec.z));
	            int highestY = (int) mc.player.getPosY();
	            int highestX = (int) target.x;
	            int highestZ = (int) target.z;
	            int iterations = 60;
	            
	            for (int x = -iterations; x < iterations; x++) {
	            	for (int z = -iterations; z < iterations; z++) {
	            		int height = mc.world.getHeight(Heightmap.Type.WORLD_SURFACE, (int) (mc.player.getPosX() + x), (int) (mc.player.getPosZ() + z)) + 5;
	            		
	            		if (height > highestY && height > mc.player.getPosY()) {
	                        highestY = height;
	                        highestX = (int) (mc.player.getPosX() + x);
	                        highestZ = (int) (mc.player.getPosZ() + z);
	                    }
	            	}
	            }
	            
	            Vector3d vecHeight = new Vector3d(highestX, highestY + 23, highestZ).subtract(mc.player.getEyePosition(mc.getRenderPartialTicks())).normalize();
	            float rawPitch = (float) MathHelper.clamp(Math.toDegrees(Math.asin(-vecHeight.y)), -89, 89);
	            
	            yaw = rawYaw;
	            pitch = rawPitch + 13f;
	            
	            mc.getGameSettings().keyBindSprint.setPressed(true);
	            mc.getGameSettings().keyBindForward.setPressed(true);
	            
	            if (Move.getSpeed() < 1.46 && timer.passed((long) (1000 / mc.timer.timerSpeed)) && Inventory.getFirework() != -1) {
					mc.player.connection.sendPacket(new CHeldItemChangePacket(Inventory.getFirework()));
					mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
					mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
	                timer.reset();
	            }
	            
	            for (int i = mc.world.getHeight(Heightmap.Type.WORLD_SURFACE, (int) (mc.player.getPosX()), (int) (mc.player.getPosZ())) - 10; i < mc.player.getPosY(); i++) {
	            	if (!mc.world.getBlockState(new BlockPos(mc.player.getPosX(), i, mc.player.getPosZ())).getFluidState().isEmpty() && mc.player.getPosY() - i < 5) {
	                    rawPitch -= 11;
	                    break;
	            	}
	            }
	            
	            if (mc.player.getDistance(target) < 30) {
	            	Chat.msg("Отличная поездка! Спасибо за использование сервиса \"Димамик\"");
					if (this.swapChestplate.get()) {
						int item = Inventory.getChestplate();
						Player.moveItemOld(item < 46 ? item : 6, 6, true);
					}
	            	toggle();
	            }
			}
		}
		
		if (event instanceof EventTrace e) {
			e.setYaw(this.yaw);
			e.setPitch(this.pitch);
			e.cancel();
		}
	}
	
	@Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}
}
