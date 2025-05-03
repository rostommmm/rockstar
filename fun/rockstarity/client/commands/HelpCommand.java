package fun.rockstarity.client.commands;

import fun.rockstarity.api.commands.CmdInfo;
import fun.rockstarity.api.commands.Command;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.modules.Module;
import net.minecraft.util.text.TextFormatting;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;
@NativeInclude
@CmdInfo(names = {"help"}, desc = "Дает краткие описания командам")
public class HelpCommand extends Command {
	
	private final String popularQuestions = """
			1. Гуи клиента открывается на правый Shift
			2. 3D компоненты клиента вы можете настроить в ClickGui->Отображение->Cosmetics
			3. 2D компоненты клиента вы можете настроить в ClickGui->Отображение->Interface или же кликнуть по компоненту правой кнопкой мыши в чате
			4. Обнаружил баг? Пиши команду .bug и свою проблему
			5. Можно ли как то отключить анимации майнкрафта? Да, ClickGui->Отображение->Beautifully
			6. Если ты не нашел решение своей проблемы, задай ее в нашем дискорде
			""";
	
	@Override
	public void execute(String[] args) {
		if (args.length == 0) {
			for (Command cmd : rock.getCommands().values()) {
				msg(String.format(".%s - %s%s", cmd.getInfo().names()[0], TextFormatting.GRAY, cmd.getInfo().desc()));
			}
			msg("Если возникли другие вопросы введите .help popular");
		} else if (args[0].equalsIgnoreCase("popular")) {
			msg("Популярные вопросы: \n" + popularQuestions);
		}
	}
}
