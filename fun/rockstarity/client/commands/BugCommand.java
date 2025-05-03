package fun.rockstarity.client.commands;

import java.util.HashMap;
import java.util.Map;

import fun.rockstarity.api.commands.CmdInfo;
import fun.rockstarity.api.commands.Command;
import fun.rockstarity.api.connection.WebHookControl;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.system.ThreadManager;
import fun.rockstarity.api.render.color.FixColor;
import net.minecraft.util.text.TextFormatting;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author Malecharik
 * @since 26 мая 2024 г. 23:23:10
 */

@NativeInclude
@CmdInfo(names = { "bug", "error", "report", "баг", "фича", "репорт"}, desc = "Отправляет баг админам")
public class BugCommand extends Command {
	
	@Override
	public void execute(String[] args) {
		if (args.length == 0) {
			Chat.msg(TextFormatting.RED + "⚠ За \u00a7cпрописывание \u00a7cкоманды \u00a7cбез \u00a7cадекватного \u00a7cбага мы \u00a7cблокируем \u00a7cдоступ \u00a7cк \u00a7cчиту ⚠");
			this.error();
			return;
		}
		StringBuilder msg = new StringBuilder();
		
		for (int i = 0; i < args.length; ++i) {
			msg.append(args[i]).append(" ");
		}
		
		final Map<String, String> parameters = new HashMap<>();
		
		parameters.put("name", rock.getUser().getName());
		parameters.put("avatar", rock.getUser().getAvatar());
		parameters.put("uid", rock.getUser().getUid() + "");
		parameters.put("role", rock.getUser().getRole());
		
		ThreadManager.run(() -> WebHookControl.logs("Bugs", msg.toString(), FixColor.YELLOW, parameters));
		Chat.msg("Баг отправлен! Мы постараемся решить вашу проблему как можно быстрее");
	}
}
