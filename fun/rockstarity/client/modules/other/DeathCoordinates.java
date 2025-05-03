package fun.rockstarity.client.modules.other;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.packet.EventSendPacket;
import fun.rockstarity.api.events.list.player.EventDeath;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TextFormatting;

/**
 * @author Malecharik
 * @since 14 Mar 2024 18:49:19
 */


@Info(name="DeathCoordinates", desc="Отпраяляет в чат место смерти", type=Category.OTHER)
public class DeathCoordinates extends Module {
	
	/*
	 * Обрабатываем событие смерти
	 */
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventDeath) {
			// Если игрок умирает отправляем в чат координаты 
			int xCord = mc.player.getPosition().getX(), yCord = mc.player.getPosition().getY(), zCord = mc.player.getPosition().getZ();
			
			Chat.msg(TextFormatting.AQUA + "[Rockstar] " + TextFormatting.RESET + "Координаты смерти: " + xCord + ", " + yCord + ", "+ zCord, "Создать метку", () -> {
				rock.getCommands().execute("way add dead " + xCord + " " + yCord + " " + zCord);
			});
		}
	}
	
	@Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}
	
}
