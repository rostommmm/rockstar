package fun.rockstarity.client.modules.combat;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.packet.EventSendPacket;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Slider;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket.Action;
import net.minecraft.network.play.client.CPlayerPacket.PositionRotationPacket;

@Info(name="SuperBow", desc="Усиливает силу лука", type=Category.COMBAT)
public class SuperBow extends Module {
	
	private final Slider pow = new Slider(this, "Сила").min(10).max(50).inc(1).set(10);

	@Override
	public void onEvent(Event event) {
		if (event instanceof EventSendPacket e && e.getPacket() instanceof CPlayerDiggingPacket packet) {
			if (packet.getAction() == Action.RELEASE_USE_ITEM && mc.player.getActiveItemStack().getItem() == Items.BOW) {
				mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_SPRINTING));
				
                for (int i = 0; i < pow.get(); i++) {
                	posRot(mc.player.getPosY() + 1e-10, false);
                    posRot(mc.player.getPosY() - 1e-10, true);
                }
			}
		}
	}
	
    protected void posRot(double y, boolean ground) {
        mc.player.connection.sendPacket(new PositionRotationPacket(mc.player.getPosX(), y, mc.player.getPosZ(), mc.player.rotationYaw, mc.player.rotationPitch, ground));
    }

	@Override
	public void onEnable() {
		
	}

	@Override
	public void onDisable() {
	}

}
