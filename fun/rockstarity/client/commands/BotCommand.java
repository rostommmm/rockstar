package fun.rockstarity.client.commands;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import fun.rockstarity.api.autobuy.bots.Bot;
import fun.rockstarity.api.autobuy.bots.connection.BotLoginNetHandler;
import fun.rockstarity.api.autobuy.bots.connection.BotNetworkManager;
import fun.rockstarity.api.commands.CmdInfo;
import fun.rockstarity.api.commands.Command;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.secure.Debugger;
import net.minecraft.client.gui.screen.ConnectingScreen;
import net.minecraft.network.ProtocolType;
import net.minecraft.network.handshake.client.CHandshakePacket;
import net.minecraft.network.login.client.CLoginStartPacket;
import net.minecraft.network.play.client.CChatMessagePacket;
import net.minecraft.util.text.TranslationTextComponent;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author Malecharik
 * @since 18 апр. 2024 г. 10:00:53
 */

@NativeInclude
@CmdInfo(names = "bot", desc = "Позволяет управлять ботами")
public class BotCommand extends Command {
	
	
	@Override
	public void execute(String[] args) {
		if (args.length == 0) {
			this.error();
			return;
		}
		
		ArrayList<Bot> bots = rock.getBotsHandler().getBots();
		
		if (this.contains(args[0], "add", "join")) {
			try {
				InetAddress inetaddress = InetAddress.getByName(ConnectingScreen.IP);
				BotNetworkManager networkManager = BotNetworkManager.createNetworkManagerAndConnect(inetaddress, ConnectingScreen.PORT, mc.getGameSettings().isUsingNativeTransport());
				networkManager.setNetHandler(new BotLoginNetHandler(networkManager, mc, null, (p_209549_1_) ->
                {
                }));
				networkManager.sendPacket(new CHandshakePacket(ConnectingScreen.IP, ConnectingScreen.PORT, ProtocolType.LOGIN));
				networkManager.sendPacket(new CLoginStartPacket(new GameProfile(UUID.randomUUID(), args[1])));
				
				rock.getBotsHandler().setLastName(args[1]);
			} catch (UnknownHostException e) {
				Debugger.print(e);
			}
		} else if (contains(args[0], "chat", "msg")) {
			StringBuilder msg = new StringBuilder();
			for (int i = 1; i < args.length; ++i) {
				msg.append(args[i]).append(" ");
			}
			
			try {
				for (Bot bot : bots) {
					bot.getNetworkManager().sendPacket(new CChatMessagePacket(msg.toString()));
				}
			} catch (Exception e) {
				Debugger.print(e);
			}
		} else if (contains(args[0], "clean")) {
			rock.getAutoBuy().getTaskManager().clear();;
		} else if (contains(args[0], "clear")) {
			try {
				for (Bot bot : bots) {
					bot.getNetworkManager().closeChannel(new TranslationTextComponent("multiplayer.status.quitting"));
				}
				bots.clear();
				
				Chat.msg("Все боты вышли");
			} catch (Exception e) {
				Debugger.print(e);
			}
		}
	}
}
