package fun.rockstarity.api.autobuy.bots;

import fun.rockstarity.api.autobuy.bots.connection.BotNetworkManager;
import fun.rockstarity.api.autobuy.bots.player.BotController;
import fun.rockstarity.api.autobuy.bots.player.BotPlayer;
import fun.rockstarity.api.autobuy.bots.world.BotWorld;
import fun.rockstarity.api.helpers.math.TimerUtility;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * @author ConeTin
 * @since 19 апр. 2024 г.
 */

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Bot {
	
	String name;
	BotNetworkManager networkManager;
	BotController controller;
	BotPlayer player;
	BotWorld world;
	
}
