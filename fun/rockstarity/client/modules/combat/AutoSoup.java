package fun.rockstarity.client.modules.combat;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Slider;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket.Action;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author ConeTin
 * @since 11 мая 2024 г.
 */


@Info(name="AutoSoup", desc="Автоматически ест супы", type=Category.COMBAT)
public class AutoSoup extends Module {
	
	private final TimerUtility eatTimer = new TimerUtility();
	
	private final Slider health = new Slider(this, "Здоровье").min(1).max(20).inc(0.5f).set(19).desc("Здоровье при котором будет использоваться суп");

	@Override
	public void onEvent(Event event) {
		if (event instanceof EventUpdate) {
			if (mc.player.getHealth() + mc.player.getAbsorptionAmount() <= this.health.get() && this.eatTimer.passed(1200L)) {
				int i = Player.findItem(46, Items.MUSHROOM_STEW);
				
				if(i != -1) {
					
					if (i == 40) {
						
						mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
					
					} else if (i >= 36) {
						mc.player.connection.sendPacket(new CHeldItemChangePacket(i-36));
						
						mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
						mc.player.connection.sendPacket(new CPlayerDiggingPacket(Action.DROP_ITEM, BlockPos.ZERO, Direction.DOWN));
						
						mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
						mc.player.connection.sendPacket(new CCloseWindowPacket());
						
						this.eatTimer.reset();
					} else {
						
						mc.playerController.windowClick(0, i, 0, ClickType.PICKUP, mc.player);
						mc.playerController.windowClick(0, 44, 0, ClickType.PICKUP, mc.player);
						
					}
				}
			}
		}
	}
	
	@NativeInclude
	private void privet() {}
	
	@Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}
	
}
