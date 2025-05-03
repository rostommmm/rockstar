package fun.rockstarity.client.modules.other;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.game.packet.EventSendPacket;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.player.Inventory;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import net.minecraft.block.Blocks;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.Items;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SEntityPropertiesPacket;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

@Info(name="Test", desc="Test.", type=Category.OTHER)
public class Test extends Module {
	
	// На будущее чтоб не ребутать
	private int value;
	private float value1;
	private long value2;
	private double value3;
	private boolean value4;
	private final TimerUtility timer = new TimerUtility();
	
	private double motion;
	private boolean velocity;

    @Override
    public void onEvent(Event event) {
    	if (event instanceof EventReceivePacket e) {
			IPacket<?> packet = e.getPacket();
			if (packet instanceof SEntityVelocityPacket velocityPacket) {
				if (velocityPacket.getEntityID() == mc.player.getEntityId()){
					//if (velocityPacket.getMotionY() / 8000.0D > 0.2) {
    	                motion = velocityPacket.getMotionY() / 8000.0D;
    	                velocity = true;
    	                e.cancel();
    	           // }
				}
			}
		}
    	/*
    	if (event instanceof EventUpdate && velocity) {
    		mc.player.getMotion().y = motion;
    	}
    	*/
    	
    	if (event instanceof EventSendPacket e) {
    		if (e.getPacket() instanceof CPlayerPacket || e.getPacket() instanceof CPlayerPacket.PositionPacket || e.getPacket() instanceof CPlayerPacket.PositionRotationPacket || e.getPacket() instanceof CPlayerPacket.RotationPacket) {
    		//	e.cancel();
    		}
    	}

    	if (event instanceof EventReceivePacket e && !(e.getPacket() instanceof SEntityPropertiesPacket)) {
    	//	Chat.debug(e.getPacket());
    	}
    	if (event instanceof EventUpdate && velocity) {
    	//	mc.player.setPosition(mc.player.getPositionVec().add(0, 0.1f, 0));
    	////	mc.player.connection.sendPacketSilent(new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), mc.player.isOnGround()));
    	//	mc.player.getMotion().y = 0.01f;
    	//	Move.setSpeed(0);ф
    	}
    	
    	if (event instanceof EventUpdate) {
            
    	    int r = (int) 10;
    	    BlockPos center = mc.player.getPosition();

    	    for (int x = -r; x <= r; x++) {
    	        for (int y = -2; y <= 2; y++) {
    	            for (int z = -r; z <= r; z++) {
    	                BlockPos pos = center.add(x, y, z);
    	                
    	               // if (mc.world.getBlock(pos) instanceof SweetBerryBushBlock berry && mc.world.getBlockState(pos).get(berry.AGE) >= 3) {
    	               // }
    	            }
    	        }
    	    }
        }
    	
    	
    	if (event instanceof EventUpdate && mc.gameSettings.keyBindJump.isKeyDown()) {
    	//	mc.player.getMotion().y = 0.12f;
        //    mc.player.getMotion().x = 0F;
		//	mc.player.getMotion().z = 0F;
		//	mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));

    	}
    	
    	/*
    	Player.look(event, mc.player.rotationYaw+MathUtility.random(-5, 5), 78.8f, true);

    	if (event instanceof EventMotion e) {
			BlockPos pos = mc.player.getPosition().add(0,-1,1);

			int i = Inventory.findItemNoChanges(44, Items.SLIME_BLOCK);
			boolean inHotbar = i <= 8;
			if(i != -1 && inHotbar) {
				
				if (inHotbar) {
					mc.player.connection.sendPacket(new CHeldItemChangePacket(i));
					mc.player.inventory.currentItem = i;
				} else {
					mc.playerController.pickItem(i);
				}
				//mc.player.connection.sendPacket(new CHeldItemChangePacket(i));
				pos = pos.down(8);
				
				//mc.player.connection.sendPacket(new CHeldItemChangePacket(i-36));
				for (int i1 = 10; i1 > 0; i1--) {
					pos = pos.up();
					if (mc.world.getBlock(pos) != Blocks.AIR || pos.y > mc.player.getPosY()) continue;
		            mc.player.connection.sendPacket(new CPlayerTryUseItemOnBlockPacket(Hand.MAIN_HAND, new BlockRayTraceResult(pos.down().getVec().add(0.5, 1, 0.5f), Direction.UP, pos.down(), false)));
		           // mc.world.setBlockState(pos, Blocks.SLIME_BLOCK.getDefaultState());
				}
				
        		mc.player.getMotion().y = 0.48f;
	            mc.player.getMotion().x = 0F;
				mc.player.getMotion().z = 0F;
				
	            if (!inHotbar) {
					mc.playerController.pickItem(i);
				} else {

				}


				//mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
			} else {
			 	rock.getAlertHandler().alert("Этот режим работает только с блоками льда в хотбаре", AlertType.INFO);
				set(false);
				onDisable();
			}
            
			//if (!mc.player.isSneaking()) return;
			//Move.setSpeed(pol = (float) Interpolator.LINEAR.interpolate(pol, this.speed.get(), 0.1f / Math.max((float) Minecraft.debugFPS, 5) * 175));
		}
    	*/
    }

    private boolean stalin(LivingEntity target) {
		Vector3d pos = target.getPositionVec();
		AxisAlignedBB hitbox = target.getBoundingBox();
		
		float off = 0.05f;
		
		return !isAir(hitbox.minX-off, pos.y, hitbox.minZ-off)
			|| !isAir(hitbox.maxX+off, pos.y, hitbox.minZ-off)
			|| !isAir(hitbox.minX-off, pos.y, hitbox.maxZ+off)
			|| !isAir(hitbox.maxX+off, pos.y, hitbox.maxZ+off);
	}
	
	private boolean isAir(double x, double y, double z) {
		return mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.SLIME_BLOCK;
	}
	
    @Override
    public void onEnable() {
		//mc.timer.timerSpeed = 0.01f;
		timer.reset();
    }

    @Override
    public void onDisable() {
    	velocity = false;
		//mc.timer.timerSpeed = 1f;
    }
}
