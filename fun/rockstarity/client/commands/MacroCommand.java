/**
 * 
 */
package fun.rockstarity.client.commands;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import fun.rockstarity.api.commands.CmdInfo;
import fun.rockstarity.api.commands.Command;
import fun.rockstarity.api.commands.CommandParameter;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.inputs.EventKey;
import fun.rockstarity.api.helpers.game.Binds;
import fun.rockstarity.api.helpers.game.Chat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.util.text.TextFormatting;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author Malecharik
 * @since 18 апр. 2024 г. 09:25:33
 */
@NativeInclude
@CmdInfo(names = { "macro", "macros"}, desc = "Позволяет управлять макросами")
public class MacroCommand extends Command {
	
	@Getter
	private static final List<Macro> macroses = new ArrayList<>();
	
	CommandParameter clear = new CommandParameter(this, "clear");
	CommandParameter list = new CommandParameter(this, "list");
	CommandParameter add = new CommandParameter(this, "add", "save");
	CommandParameter del = new CommandParameter(this, "del", "delete", "remove");

	@Override
	public void onEvent(Event event) {
		if (event instanceof EventKey e) {
			int pressKey = e.getKey() < 8 ? GLFW.glfwGetMouseButton(mc.getMainWindow().getHandle(), e.getKey()) : GLFW.glfwGetKey(mc.getMainWindow().getHandle(), e.getKey());
			for (MacroCommand.Macro macro : macroses) {
				if (e.getKey() == macro.getKey() && pressKey == 1 && mc.currentScreen == null) {
					mc.player.sendChatMessage(macro.getMsg());
				}
			}
		}
	}
	
	@Override
	public void execute(String[] args) {
	    if (args.length == 0) {
	        this.error();
	        return;
	    }

	    String commands = args[0].toLowerCase();

	    if (contains(commands, clear)) {
	        if (macroses.isEmpty()) {
	            msg("Макросы не найдены");
	        } else {
	            macroses.clear();
	            msg("Список макросов очищен");
	        }
	        return;
	    }

	    if (contains(commands, list)) {
	        if (macroses.isEmpty()) {
	            msg("Список макросов пуст!");
	        } else {
	            msg("Список макросов:");
	            for (Macro macro : macroses) {
	                Chat.msg(TextFormatting.AQUA + "[Rockstar] " + TextFormatting.GRAY + "[" + macro.getName() + "] " + TextFormatting.RESET + macro.getMsg(),
	                        "Удалить макрос на" + TextFormatting.GRAY + " [" + macro.getName() + "]", () -> {
	                            macroses.remove(macro);
	                            msg("Макрос удален. Обновляю список");
	                            rock.getCommands().execute("macro list");
	                        });
	            }
	        }
	        return;
	    }

	    if (args.length < 3) {
	        this.error();
	        return;
	    }

	    String keyArg = null;
	    String commandArg = null;

	    for (int i = 0; i < 2; i++) {
	        String lowerArg = args[i].toLowerCase();
	        if (Binds.KEYS.containsKey(lowerArg)) {
	            keyArg = lowerArg;
	        } else {
	            commandArg = lowerArg;
	        }
	    }

	    if (contains(commandArg, add)) {
	        StringBuilder msg = new StringBuilder();

	        for (int i = 2; i < args.length; ++i) {
	            msg.append(args[i]).append(" ");
	        }
	        
	        macroses.add(new Macro(keyArg.toUpperCase(), Binds.KEYS.get(keyArg), msg.toString().trim()));
	        msg("Макрос добавлен на кнопку" + TextFormatting.GRAY + " [" + keyArg.toUpperCase() + "]");
	    } 
	    else if (contains(commandArg, del)) {
	        Macro removed = null;

	        for (Macro macro : macroses) {
	            if (macro.getName().equals(keyArg.toUpperCase())) {
	                removed = macro;
	            }
	        }

	        if (removed != null) {
	            macroses.remove(removed);
	            msg("Макрос на клавише " + TextFormatting.GRAY + "[" + removed.getName() + "]" + TextFormatting.RESET + " удален");
	        } else {
	            msg("Макрос на кнопке " + keyArg.toUpperCase() + " не найден.");
	        }
	    }
	}


	
	@Getter
	@AllArgsConstructor
	public static class Macro {
		private final String name;
		private final int key;
		private final String msg;
	}
}
