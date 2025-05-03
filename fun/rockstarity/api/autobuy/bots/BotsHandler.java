package fun.rockstarity.api.autobuy.bots;

import java.util.ArrayList;
import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.autobuy.bots.player.BotPlayer;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.secure.Debugger;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.network.play.client.CClickWindowPacket;

/**
 * @author ConeTin
 * @since 19 апр. 2024 г.
 */


public class BotsHandler implements IAccess {

	@Getter
	private final ArrayList<Bot> bots = new ArrayList<>();
	
	@Getter @Setter private BotPlayer lastBot;
	@Getter @Setter private String lastName;
	
	public void onEvent(Event event) {
		if (event instanceof EventUpdate) {
			try {
				for (Bot bot : this.bots) {
					BotPlayer player = bot.getPlayer();
					
					if (player.getHealth() <= 0.0f) {
						player.respawnPlayer();
						continue;
		            }
					
				}
			} catch (Exception e) {
				Debugger.print(e);
			}
		}
	}
	
	public void tickEntities() {
		try {
			for (Bot bot : this.bots) {
				bot.getWorld().tickEntities();
                bot.getWorld().removeEntityFromWorld(mc.player.getEntityId());
			}
		} catch (Exception e) {
			Debugger.print(e);
		}
	}
	
	public void worldTick() {
		try {
			for (Bot bot : this.bots) {
				bot.getWorld().tick(() ->
                {
                    return true;
                });
			}
		} catch (Exception e) {
			Debugger.print(e);
		}
	}
	
	public void tick() {
		try {
			for (Bot bot : this.bots) {
			//	bot.getPlayer().tick();
			}
		} catch (Exception e) {
			Debugger.print(e);
		}
	}
	
	public void controllerTick() {
		try {
			for (Bot bot : this.bots) {
				bot.getController().tick();
			}
		} catch (Exception e) {
			Debugger.print(e);
		}
	}
	
}
