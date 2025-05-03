package fun.rockstarity.client.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fun.rockstarity.api.commands.CmdInfo;
import fun.rockstarity.api.commands.Command;
import fun.rockstarity.api.commands.CommandParameter;
import fun.rockstarity.api.helpers.game.Chat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;
@NativeInclude
@CmdInfo(names={"scoreboard", "score", "sb"}, desc="Позволяет управлять изменением скорборда")
public class ScoreCommand extends Command {

	@Getter
	private static final List<Line> scores = new ArrayList<>();
	@Getter
	private static final Map<String, String> texts = new HashMap<>();
	
	CommandParameter clear = new CommandParameter(this, "clear");
	CommandParameter list = new CommandParameter(this, "list");
	CommandParameter add = new CommandParameter(this, "add", "save");
	CommandParameter del = new CommandParameter(this, "del", "remove", "delete");
	
	public static ITextComponent replaceText(ITextComponent original, String toReplace, String replacement) {
		if (rock.isPanic() || scores.isEmpty()) return original;
		
        for (ITextComponent child1 : original.getSiblings()) {
        	if (child1 instanceof StringTextComponent textComponent) {
        		texts.put(textComponent.text.replace(toReplace, replacement), textComponent.text);
                textComponent.text = textComponent.text.replace(toReplace, replacement);
            }
        	
        	for (ITextComponent child2 : child1.getSiblings()) {
        		if (child2 instanceof StringTextComponent textComponent) {
            		texts.put(textComponent.text.replace(toReplace, replacement), textComponent.text);
                    textComponent.text = textComponent.text.replace(toReplace, replacement);
                }
        		
        		for (ITextComponent child3 : child1.getSiblings()) {
                	if (child3 instanceof StringTextComponent textComponent) {
                		texts.put(textComponent.text.replace(toReplace, replacement), textComponent.text);
                        textComponent.text = textComponent.text.replace(toReplace, replacement);
                    }
                }
            }
        }
        
        return original;
    }
	
	public static void back(ITextComponent original) {
		for (ITextComponent child1 : original.getSiblings()) {
        	if (child1 instanceof StringTextComponent textComponent && texts.containsKey(textComponent.text)) {
        		String old = textComponent.text;
                textComponent.text = texts.get(textComponent.text);
                texts.remove(old);
            }
        	
        	for (ITextComponent child2 : child1.getSiblings()) {
        		if (child2 instanceof StringTextComponent textComponent && texts.containsKey(textComponent.text)) {
        			String old = textComponent.text;
                    textComponent.text = texts.get(textComponent.text);
                    texts.remove(old);
                }
        		
        		for (ITextComponent child3 : child1.getSiblings()) {
                	if (child3 instanceof StringTextComponent textComponent && texts.containsKey(textComponent.text)) {
                		String old = textComponent.text;
                        textComponent.text = texts.get(textComponent.text);
                        texts.remove(old);
                    }
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
	        if (scores.isEmpty()) {
	            msg("Строки не найдены");
	        } else {
	        	scores.clear();
	            msg("Список строк очищен");
	        }
	        return;
	    }

	    if (contains(commands, list)) {
	        if (scores.isEmpty()) {
	            msg("Список строк пуст!");
	        } else {
	            msg("Список строк:");
	            for (Line line : scores) {
	                Chat.msg(String.format("%s%s %s%s%s » %s%s", TextFormatting.AQUA, "[Rockstar]", TextFormatting.GRAY, line.original, TextFormatting.WHITE, TextFormatting.GRAY, line.newVal),
	                        "Удалить строку" + TextFormatting.GRAY + " [" + line.original + "]", () -> {
	                        	scores.remove(line);
	                            msg("Строка удалена. Обновляю список");
	                            rock.getCommands().execute("sb list");
	                        });
	            }
	        }
	        return;
	    }

	    if (args.length < 3) {
	        this.error();
	        return;
	    }
	    
	    String commandArg = args[0];
	    String original = args[1];

	    if (contains(commandArg, add)) {
	        StringBuilder msg = new StringBuilder();

	        for (int i = 2; i < args.length; ++i) {
	            msg.append(args[i]).append(" ");
	        }

	        scores.add(new Line(original, msg.toString().replace("&", "§").trim()));
	        msg("Добавлена строка " + String.format("%s%s%s » %s%s", TextFormatting.GRAY, original, TextFormatting.WHITE, TextFormatting.GRAY, msg.toString().replace("&", "§")));
	    } 
	    else if (contains(commandArg, del)) {
	        Line removed = null;

	        for (Line macro : scores) {
	            if (macro.getOriginal().equals(original)) {
	                removed = macro;
	            }
	        }

	        if (removed != null) {
	            scores.remove(removed);
	            msg("Строка " + TextFormatting.GRAY + original + TextFormatting.RESET + " удалена");
	        } else {
	            msg("Строка " + TextFormatting.GRAY + original + TextFormatting.RESET + " не найдена.");
	        }
	    }
	}


	
	@Getter
	@AllArgsConstructor
	public static class Line {
		private final String original;
		private final String newVal;
	}
}