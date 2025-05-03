package fun.rockstarity.client.modules.player;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.game.packet.EventSendPacket;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CPlayerAbilitiesPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;

/**
 * @author ConeTin
 * @since 25 мая 2024 г.
 */


@Info(name="Phase", desc="Помогает входить в блоки без перлов и т.д.", type=Category.PLAYER)
public class Phase extends Module {
	
	private GameType prev;
	private final TimerUtility timer = new TimerUtility();
	private float x,y,z;
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventSendPacket e) {
			IPacket packet = e.getPacket();
			
			if (packet instanceof CPlayerPacket || packet instanceof CPlayerAbilitiesPacket) event.cancel();
		}
		
		if (event instanceof EventReceivePacket e) {
			IPacket packet = e.getPacket();
			
			if (packet instanceof SPlayerPositionLookPacket) {
				event.cancel();
			}
		}
		
		if (event instanceof EventMotion) {
			mc.player.noClip = true;
			mc.player.getMotion().y = 0;
		}
		
		if (this.timer.passed(150L)) {
			this.toggle();
		}
	}
	
	@Override
	public void onEnable() {
		this.prev = mc.playerController.getCurrentGameType();
        mc.player.connection.getPlayerInfo(mc.player.getUniqueID()).setGameType(GameType.SPECTATOR);
        this.x = (float)mc.player.getPosX();
        this.y = (float)mc.player.getPosY();
        this.z = (float)mc.player.getPosZ();
        mc.player.setSneaking(true);
        this.timer.reset();
	}
	
	@Override
	public void onDisable() {
		 mc.player.connection.getPlayerInfo(mc.player.getUniqueID()).setGameType(this.prev);
         mc.player.setMotion(0.0, 0.0, 0.0);
         mc.player.setVelocity(0.0, 0.0, 0.0);
         mc.player.setPosition(this.x, this.y, this.z);
         mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY() + 5.941588215E-315, mc.player.getPosZ(), mc.player.isOnGround()));
         mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY() + 5.941588215E-315, mc.player.getPosZ(), mc.player.isOnGround()));
         mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY() + 5.941588215E-315, mc.player.getPosZ(), mc.player.isOnGround()));
         mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY() + 5.941588215E-315, mc.player.getPosZ(), mc.player.isOnGround()));
         mc.player.setSneaking(false);
	}
}
