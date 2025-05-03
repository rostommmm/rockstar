package fun.rockstarity.client.modules.player;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;

/**
 * @author Malecharik
 * @since 2 апр. 2024 г. 19:44:04
 */


@Info(name="NoServerRotate", desc="Убирает установку ротации сервером", type=Category.PLAYER)
public class NoRotate extends Module {
	
    protected float targetYaw;
    protected float targetPitch;
    protected boolean packetSent;
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventReceivePacket e) {
			if(e.getPacket() instanceof SPlayerPositionLookPacket packet) {
				// Устанавливаем углы поворота пакета равными углам поворота игрока
				packet.setYaw(mc.player.rotationYaw);
				packet.setPitch(mc.player.rotationPitch);
			}
		}
	}
	
    public void sendRotationPacket(float yaw, float pitch) {
        targetYaw = yaw;
        targetPitch = pitch;
        packetSent = true;
    }
	
	@Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}
	
}
