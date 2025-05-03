package fun.rockstarity.client.commands;

import fun.rockstarity.api.commands.CmdInfo;
import fun.rockstarity.api.commands.Command;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.game.GameUtility;
import fun.rockstarity.api.helpers.system.TextUtility;
import net.minecraft.client.gui.screen.ConnectingScreen;
import net.minecraft.client.gui.screen.DirtMessageScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.realms.RealmsBridgeScreen;
import net.minecraft.util.text.TranslationTextComponent;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;
@NativeInclude
@CmdInfo(names={"login", "alt", "reconnect"}, desc="Перезаходит на сервер с вашим ником")
public class ReconCommand extends Command {
	
	@Override
	public void execute(String[] args) {
		if (args.length == 0) {
			this.error();
			return;
		}
		
		if (args[0].equalsIgnoreCase("random") || args[0].equalsIgnoreCase("rand") || args[0].isEmpty()) {
			GameUtility.changeName(TextUtility.getRandomNick());
		} else {
			GameUtility.changeName(args[0]);
		}
		
		mc.world.sendQuittingDisconnectingPacket();
		
		mc.displayGuiScreen(new ConnectingScreen(new MainMenuScreen(), mc, ConnectingScreen.IP, ConnectingScreen.PORT));
	}
}
