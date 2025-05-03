package fun.rockstarity.api.events.list.game.packet;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.IPacket;

/**
 * @author ConeTin
 * @since 6 дек. 2023 г.
 * @in NetworkManager
 */

@Getter
@AllArgsConstructor
public class EventSendPacket extends Event {

	private final IPacket packet;
	
}
