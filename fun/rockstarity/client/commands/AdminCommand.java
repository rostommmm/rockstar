package fun.rockstarity.client.commands;

import java.util.HashMap;

import fun.rockstarity.api.binds.Bind;
import fun.rockstarity.api.binds.BindType;
import fun.rockstarity.api.commands.CmdInfo;
import fun.rockstarity.api.commands.Command;
import fun.rockstarity.api.commands.CommandParameter;
import fun.rockstarity.api.connection.globals.ServerAPI;
import fun.rockstarity.api.helpers.game.Binds;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.secure.KeyGeneration;
import fun.rockstarity.api.helpers.secure.Web;
import fun.rockstarity.api.helpers.system.ThreadManager;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.util.text.TextFormatting;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author Malecharik
 * @since 18 апр. 2024 г. 10:00:53
 */

@NativeInclude
@CmdInfo(names = { "admin", "adm" }, desc = "Админские приколюхи :3")
public class AdminCommand extends Command {
	
	CommandParameter poka = new CommandParameter(this, "poka", "kick");
	CommandParameter globals = new CommandParameter(this, "globals");
	
	@Override
	public void execute(String[] args) {
		if (args.length == 0) {
			this.error();
			return;
		}

		//if (args.length == 1) {
			if (contains(args[0], poka)) {
				ThreadManager.run(() -> {
					HashMap<String, String> data = new HashMap<>();
					data.put("nick", args[1]);
					data.put("stage", "0");
					System.out.println(Web.protectedPostRequest(data, "https://rockstar.moscow/api/v1/premium/utility/auth/admin/cmd/kick.php"));;
					
					mc.player.sendChatMessage("пока");
					//mc.player.sendChatMessage("/msg " + args[1] + " извини, пока");

					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					data.put("stage", "1");
					Web.protectedPostRequest(data, "https://rockstar.moscow/api/v1/premium/utility/auth/admin/cmd/kick.php");
				});
			} else if (contains(args[0], globals)) {
				ServerAPI.updateName(args[1]);
				Chat.debug("Пробуем)");
			}

		//}
	}
}
