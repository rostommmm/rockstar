package fun.rockstarity.client.modules.player;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.packet.EventSendPacket;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import net.minecraft.network.play.client.CConfirmTeleportPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;

/**
 * @author Malecharik
 * @since 21 мая 2024 г. 20:16:02
 */


@Info(name="Reach", desc="Увеличивает дальность использования блоков", type=Category.PLAYER)
public class Reach extends Module {
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventUpdate e && rock.isDebugging()) {
			//mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), mc.player.rotationYaw, mc.player.rotationPitch, mc.player.isOnGround()));
			//mc.player.connection.sendPacket(new CPlayerPacket(mc.player.isOnGround()));
			//mc.player.jump();
			//mc.player.getMotion().y = 0.1f;
			//mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
			
           // this.mc.player.connection.sendPacket(new CClientSettingsPacket(mc.getGameSettings().language, 0, mc.getGameSettings().chatVisibility, mc.getGameSettings().chatColor, 0, mc.getGameSettings().mainHand));

            
//			LivingEntity target = rock.getModules().get(Aura.class).getPrevTarget();
//			
//			int slot = Inventory.findItemNoChanges(44, Items.OBSIDIAN);
//
//			if (target == null || mc.player.getDistance(target) > 6 || slot == -1) return;
//			
//			if (slot < 9) {
//				mc.player.connection.sendPacket(new CHeldItemChangePacket(slot));
//			} else {
//				mc.playerController.pickItem(slot);
//			}
//			
//			{
//				BlockPos pos = target.getPosition().add(0,-1,0);
//            	if (mc.world.getBlock(pos) == Blocks.AIR);
//            	mc.player.connection.sendPacket(new CPlayerTryUseItemOnBlockPacket(Hand.MAIN_HAND, new BlockRayTraceResult(new Vector3d(0.5, 0.5f, 0.5), Direction.UP, pos, true)));
//			}
//			
//			{
//				BlockPos pos = target.getPosition().add(0,2,0);
//            	if (mc.world.getBlock(pos) == Blocks.AIR);
//            	mc.player.connection.sendPacket(new CPlayerTryUseItemOnBlockPacket(Hand.MAIN_HAND, new BlockRayTraceResult(new Vector3d(0.5, 0.5f, 0.5), Direction.UP, pos, true)));
//			}
//			
//            for (int i = 0; i < 2; i++) {
//            	BlockPos pos = target.getPosition().add(0,i,1);
//            	if (mc.world.getBlock(pos) != Blocks.AIR) continue;
//            	mc.player.connection.sendPacket(new CPlayerTryUseItemOnBlockPacket(Hand.MAIN_HAND, new BlockRayTraceResult(new Vector3d(0.5, 0.5f, 0.5), Direction.UP, pos, true)));
//            }
//            
//            for (int i = 0; i < 2; i++) {
//            	BlockPos pos = target.getPosition().add(0,i,-1);
//            	if (mc.world.getBlock(pos) != Blocks.AIR) continue;
//            	mc.player.connection.sendPacket(new CPlayerTryUseItemOnBlockPacket(Hand.MAIN_HAND, new BlockRayTraceResult(new Vector3d(0.5, 0.5f, 0.5), Direction.UP, pos, true)));
//            }
//            
//            for (int i = 0; i < 2; i++) {
//            	BlockPos pos = target.getPosition().add(1,i,0);
//            	if (mc.world.getBlock(pos) != Blocks.AIR) continue;
//            	mc.player.connection.sendPacket(new CPlayerTryUseItemOnBlockPacket(Hand.MAIN_HAND, new BlockRayTraceResult(new Vector3d(0.5, 0.5f, 0.5), Direction.UP, pos, true)));
//            }
//            
//            for (int i = 0; i < 2; i++) {
//            	BlockPos pos = target.getPosition().add(-1,i,0);
//            	if (mc.world.getBlock(pos) != Blocks.AIR) continue;
//            	mc.player.connection.sendPacket(new CPlayerTryUseItemOnBlockPacket(Hand.MAIN_HAND, new BlockRayTraceResult(new Vector3d(0.5, 0.5f, 0.5), Direction.UP, pos, true)));
//            }
//
//            if (slot < 9) {
//				mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
//			} else {
//				mc.playerController.pickItem(slot);
//			}
		}
		
		if (event instanceof EventSendPacket e) {
		//	System.out.println(e.getPacket().getClass());
		}
	}
	
	@Override
	public void onEnable() {
		//Server.warningFT();
		if (rock.isDebugging()) {
		//	mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(14, 75, 76, true));
		//	mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(16, 75, 73, true));
		///	mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(17, 75, 70, true));
			
		}
		//NarratorChatListener.INSTANCE.say("Рокстар жоска пенит");
	}
	
	@Override
	public void onDisable() {

	}

}
