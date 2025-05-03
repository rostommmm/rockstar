package fun.rockstarity.client.modules.move;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.packet.EventSendPacket;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.modules.settings.list.Select.Element;
import net.minecraft.block.Blocks;
import net.minecraft.network.play.client.CConfirmTeleportPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author Malecharik
 * @since 22 Mar 2024 22:24:37
 */


@Info(name = "WaterLeave", desc = "Высоко прыгает в воде", type = Category.MOVE)
public class WaterLeave extends Module {
	private final Select util = new Select(this, "Выбор");
	private final Element hightJump = new Element(util, "Высокий прыжок").set(true);
	private final Element lily = new Element(util, "Работать с кувшинкой");
	private final Slider motion = new Slider(this, "Высота").min(1).max(10).inc(0.5f).set(1)
			.hide(() -> !this.hightJump.get());

	@Override
	@NativeInclude
	public void onEvent(Event event) {
		if (event instanceof EventSendPacket e && !this.lily.get() && e.getPacket() instanceof SPlayerPositionLookPacket p) {
			mc.player.setPosition(p.getX(), p.getY(), p.getZ());
			mc.player.connection.sendPacket(new CConfirmTeleportPacket(p.getTeleportId()));
			e.cancel();
		}
			
		

		if (event instanceof EventUpdate) {
			if (this.lily.get() && Player.getBlock() == Blocks.LILY_PAD && mc.player.ticksExisted % 7 == 1 && mc.player.isCrouching()) {
				mc.player.getMotion().y = 0.2f;
			}
			
			if (this.hightJump.get() && mc.player.isInWater() && mc.player.fallDistance > 0) {
					mc.player.getMotion().y = this.motion.get();
			}
		}
	}
	
	@Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}
	
}
