package fun.rockstarity.client.commands;

import fun.rockstarity.api.commands.CmdInfo;
import fun.rockstarity.api.commands.Command;
import fun.rockstarity.api.commands.CommandParameter;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import net.minecraft.util.text.TextFormatting;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author ConeTin
 * @since 9 дек. 2023 г.
 */
@NativeInclude
@CmdInfo(names={"prefix", "префикс"}, desc="Позволяет изменять префикс клиента")
public class PrefixCommand extends Command {
	
	CommandParameter reset = new CommandParameter(this, "reset", "default", "clear");
	CommandParameter list = new CommandParameter(this, "list");
	
	@Override
	public void execute(String[] args) {
		if (contains(args[0], reset)) {
			rock.getCommands().setPrefix(".");
			rock.getAlertHandler().alert("Префикс успешно сброшен", AlertType.INFO);
		} else if (contains(args[0], list)) {
			Chat.msg(TextFormatting.AQUA + "[Rockstar] " + TextFormatting.RESET + "Ваш текущий префикс - \"" + rock.getCommands().getPrefix() + "\"", "Нажмите для сброса", () -> {
				rock.getCommands().setPrefix(".");
				rock.getAlertHandler().alert("Установлен стандартный префикс", AlertType.INFO);
			});
		} else {
			rock.getCommands().setPrefix(args[0]);
			rock.getAlertHandler().alert("Префикс успешно установлен", AlertType.INFO);
		}
	}
}
