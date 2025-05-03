package fun.rockstarity.client.commands;

import java.util.List;

import fun.rockstarity.api.binds.Bind;
import fun.rockstarity.api.binds.BindType;
import fun.rockstarity.api.commands.CmdInfo;
import fun.rockstarity.api.commands.Command;
import fun.rockstarity.api.commands.CommandParameter;
import fun.rockstarity.api.helpers.game.Binds;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.Setting;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import net.minecraft.util.text.TextFormatting;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author Malecharik
 * @since 18 апр. 2024 г. 10:00:53
 */
@NativeInclude

@CmdInfo(names = "bind", desc = "Позволяет управлять биндами")
public class BindCommand extends Command {
	
	CommandParameter add = new CommandParameter(this, "add", "save");
	CommandParameter clear = new CommandParameter(this, "clear");
	CommandParameter list = new CommandParameter(this, "list");
	
	@Override
	public void execute(String[] args) {
		// Проверяем, предоставлены ли аргументы
		if (args.length == 0) {
			this.error();
			return;
		}
		// Проверяем, является ли команда для добавления или сохранения бинда
		if (this.contains(args[0], add)) {
			Module module = null;
			String bindTypeArg = "toggle";
			// Проверяем, достаточно ли аргументов предоставлено
			if (args.length < 3) {
				this.error();
				return;
			}
			
		    if (args.length >= 4) {
		        bindTypeArg = args[3];
		    }
		    
		    if (args[1].matches("[A-Z]+") && args[2].matches("[a-zA-Z]+")) {
		    	String temp = args[1];
		    	args[1] = args[2];
            	args[2] = temp;
		    }
		    
		    // Ищем модуль
			for (Module mod : rock.getModules().values()) {
				if (mod.getInfo().name().equalsIgnoreCase(args[1])) {
					module = mod;
					break;
				}
			}
			// Если модуль не найден, выводим сообщение об ошибке
			if (module == null) {
	            rock.getAlertHandler().alert(TextFormatting.RED + "Модуль не найден: " + args[1], AlertType.ERROR);
	            return;
	        }
			module.getBinds().add(new Bind(Binds.KEYS.get(args[2].toLowerCase()), BindType.get(bindTypeArg)));
			Chat.msg("Бинд " + TextFormatting.GRAY + "[" + args[2].toUpperCase() + "]" + TextFormatting.WHITE
					+ " Добавлен в " + module.getInfo().name());
		} else if (contains(args[0], clear)) {
			 boolean notEmpty = true;
			 
			 for (Module mod : rock.getModules().values()) {
			        // Пропускаем модуль ClickGui
			        if (mod.getInfo().name().equalsIgnoreCase("ClickGui")) continue;
			        
			        if (!mod.getBinds().isEmpty()) {
			            mod.getBinds().clear();
			            notEmpty = false;
			        }
			 }
			 // Выводим соответствующий нотиф
			 rock.getAlertHandler().alert(notEmpty ? "Бинды уже пусты!" : "Все бинды очищены!", AlertType.INFO);
		} else if (contains(args[0], list)) {
			Chat.msg("Список биндов:");

			for (Module module : rock.getModules().values()) {
			    String moduleName = module.getInfo().name();
			    if (moduleName.contains("ClickGui")) continue;
			    
			    if (!module.getBinds().isEmpty()) {
			        printBindInfo(moduleName, module.getBinds(), module.getBinds()::clear);
			    }

			    for (Setting setting : module.getSettings()) {
			        if (!setting.getBinds().isEmpty()) {
			            printBindInfo(moduleName + " -> " + setting.getName(), setting.getBinds(), setting.getBinds()::clear);
			        }
			    }
			}
		}
	}
	
	private void printBindInfo(String displayName, List<Bind> binds, Runnable onClear) {
	    StringBuilder bindInfo = new StringBuilder(displayName + " ");
	    int bindCount = binds.size(), count = 0;

	    for (Bind bind : binds) {
	        count++;
	        bindInfo.append(TextFormatting.GRAY)
	                .append("[").append(Binds.getName(bind.getKey(), bind.getScancode())).append("]");
	        if (count < bindCount) bindInfo.append(", ");
	    }

	    Chat.msg(TextFormatting.AQUA + "[Rockstar] " + TextFormatting.RESET + bindInfo.toString(), 
	        "Удалить бинды у " + displayName, () -> {
	            onClear.run();
	            Chat.msg("Бинды " + displayName + " удалены. Обновляю список");
	            rock.getCommands().execute("bind list");
	        });
	}
}
