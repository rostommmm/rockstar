package fun.rockstarity.client.modules.player;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventPostMotion;
import fun.rockstarity.api.events.list.render.entity.EventRenderEntity;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import lombok.Getter;
import net.minecraft.entity.Pose;
import net.minecraft.network.play.client.CEntityActionPacket;

/**
 * @author Malecharik
 * @since 12 мая 2024 г. 12:20:53
 */


@Info(name="Sneak", desc="Бегать с зажатым шифтом", type=Category.PLAYER)
public class Sneak extends Module {
	
	@Getter
	private final CheckBox onlyAir = new CheckBox(this, "Работать только в воздухе");
	
	@Override
	public void onEvent(Event event) {
		if (mc.player.isSwimming()) return;
		
		if (event instanceof EventRenderEntity e && e.getEntity() == mc.player) {
			if (onlyAir.get() && mc.player.isOnGround()) return;
			mc.player.setPose(Pose.CROUCHING);
		}
		
		if (event instanceof EventMotion) {
			mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.RELEASE_SHIFT_KEY));
		}

		if (event instanceof EventPostMotion) {
			if (this.onlyAir.get() && mc.player.isOnGround()) return;
			mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.PRESS_SHIFT_KEY));
		}
	}
	
	@Override
	public void onEnable() {
		Server.warningFT();
	}
	
	@Override
	public void onDisable() {
		mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.RELEASE_SHIFT_KEY));
	}
}
