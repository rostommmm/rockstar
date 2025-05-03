package fun.rockstarity.api.helpers.player;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.via.ViaLoadingBase;
import lombok.experimental.UtilityClass;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;

/**
 * @author ConeTin
 * @since 26 нояб. 2024 г.
 */

@UtilityClass
public class Bypass implements IAccess {
	
	public boolean via() {
		return ViaLoadingBase.getInstance().getTargetVersion().isNewerThanOrEqualTo(ProtocolVersion.v1_17);
	}
	
	public void send(float yaw, float pitch) {
		send(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), yaw, pitch, mc.player.isOnGround());
	}
	
	public void send(double x, double y, double z, float yaw, float pitch, boolean ground) {
		mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(x,y,z,yaw,pitch,ground));
		mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
	}

}
