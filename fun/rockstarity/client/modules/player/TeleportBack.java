package fun.rockstarity.client.modules.player;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventTick;
import fun.rockstarity.api.events.list.game.EventWorldChange;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.game.packet.EventSendPacket;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;

/**
 * @author ConeTin
 * @since 12 авг. 2024 г.
 */

@Info(name="TeleportBack", desc="Телепортирует вас на ту же точку", type=Category.PLAYER)
public class TeleportBack extends Module {

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
    		mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(mc.player.getPosX(), mc.player.getPosY()-25, mc.player.getPosZ(), mc.player.rotationYaw, mc.player.rotationPitch, mc.player.isOnGround()));
        }
        
        if (event instanceof EventSendPacket e) {
        	/*
        	if (e.getPacket() instanceof CPlayerPacket || e.getPacket() instanceof CPlayerPacket.PositionPacket || e.getPacket() instanceof CPlayerPacket.RotationPacket) {
        		mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(mc.player.getPosX(), mc.player.getPosY()-25, mc.player.getPosZ(), mc.player.rotationYaw, mc.player.rotationPitch, mc.player.isOnGround()));
        		e.cancel();
        	}
        	
        	if (e.getPacket() instanceof CPlayerPacket.PositionRotationPacket packet) {
        	}
        	*/
        }
    }
    
    @Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}
	
}
