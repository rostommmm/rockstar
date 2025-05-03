package fun.rockstarity.api.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.system.TextUtility;
import fun.rockstarity.api.secure.Debugger;
import fun.rockstarity.client.commands.BindCommand;
import fun.rockstarity.client.commands.BotCommand;
import fun.rockstarity.client.commands.BugCommand;
import fun.rockstarity.client.commands.ClanInvestCommand;
import fun.rockstarity.client.commands.ConfigsCommand;
import fun.rockstarity.client.commands.FakePlayerCommand;
import fun.rockstarity.client.commands.FriendsCommand;
import fun.rockstarity.client.commands.HClipCommand;
import fun.rockstarity.client.commands.HelpCommand;
import fun.rockstarity.client.commands.InventoryCommand;
import fun.rockstarity.client.commands.InvseeCommand;
import fun.rockstarity.client.commands.LuaCommand;
import fun.rockstarity.client.commands.MacroCommand;
import fun.rockstarity.client.commands.PrefixCommand;
import fun.rockstarity.client.commands.ReHubCommand;
import fun.rockstarity.client.commands.ReconCommand;
import fun.rockstarity.client.commands.ScoreCommand;
import fun.rockstarity.client.commands.TaksaCommand;
import fun.rockstarity.client.commands.TargetCommand;
import fun.rockstarity.client.commands.TeleportCommand;
import fun.rockstarity.client.commands.VClipCommand;
import fun.rockstarity.client.commands.WayCommand;
import lombok.Getter;
import lombok.Setter;
import ru.kotopushka.antiautistleak.obfuscator.includes.annotations.compile.ReleaseCompileToNativeCalls;

/**
 * @author ConeTin
 * @since 9 дек. 2023 г.
 */

@Getter
public class Commands extends HashMap<Class, Command> {
	
	@Setter
	private String prefix = ".";

	public Commands() {
		Arrays.stream(new Command[] {
				new ConfigsCommand(),
				new FakePlayerCommand(),
				new VClipCommand(),
				new HClipCommand(),
				new ReHubCommand(),
				new FriendsCommand(),
				new MacroCommand(),
				new BindCommand(),
				new PrefixCommand(),
				new ReconCommand(),
				new WayCommand(),
				new BotCommand(),
				new BugCommand(),
				new HelpCommand(),
				//new ProfileCommand(),
				new TargetCommand(),
				new ClanInvestCommand(),
				new TeleportCommand(),
				new InventoryCommand(),
				new InvseeCommand(),
				new ScoreCommand(),
				new LuaCommand(),
				new TaksaCommand(),
		}).forEach(this::add);
	}
	@ReleaseCompileToNativeCalls
	public boolean execute(String msg) {
	    if (!msg.startsWith(prefix)) {
	        msg = prefix + msg;
	    }
	    return executeInternal(msg);
	}
	@ReleaseCompileToNativeCalls
	public boolean executeRaw(String msg) {
	    return executeInternal(msg);
	}
	
	@ReleaseCompileToNativeCalls
	private boolean executeInternal(String msg) {
	    if (!msg.startsWith(prefix)) return false;

	    String[] m = msg.substring(prefix.length()).trim().split(" ");
	    if (m.length == 0 || m[0].isEmpty()) {
	        Chat.msg("Команда введена неверно. Используйте " + prefix + "help для получения списка команд.");
	        return true;
	    }

	    String inputCmd = m[0];

	    List<Command> sorted = new ArrayList<>(this.values());
	    sorted.sort((a, b) -> {
	        int lenA = Arrays.stream(a.getInfo().names()).mapToInt(String::length).max().orElse(0);
	        int lenB = Arrays.stream(b.getInfo().names()).mapToInt(String::length).max().orElse(0);
	        return Integer.compare(lenB, lenA);
	    });

	    for (Command cmd : sorted) {
	        for (String name : cmd.getInfo().names()) {
	            if (inputCmd.equalsIgnoreCase(name)) {
	                String[] args = Arrays.copyOfRange(m, 1, m.length);
	                try {
	                    cmd.execute(args);
	                } catch (Exception e) {
	                    Debugger.print(e);
	                }
	                return true;
	            }
	        }
	    }

	    String suggestion = getCorrection(inputCmd);
	    if (suggestion != null) {
	        Chat.msg("Команда введена неверно. Возможно, вы имели в виду: " + prefix + suggestion);
	    } else {
	        Chat.msg("Команда введена неверно. Используйте " + prefix + "help для получения списка команд.");
	    }

	    return true;
	}
	@ReleaseCompileToNativeCalls
    private String getCorrection(String input) {
        String closest = null;
        int minDistance = Integer.MAX_VALUE;

        for (Command cmd : this.values()) {
            for (String name : cmd.getInfo().names()) {
                if (name.toLowerCase().startsWith(input.toLowerCase())) {
                    return name;
                }

                int distance = TextUtility.levenshteinDistance(input.toLowerCase(), name.toLowerCase());
                if (distance < minDistance) {
                    minDistance = distance;
                    closest = name;
                }
            }
        }

        return (minDistance <= input.length()) ? closest : null;
    }
	@ReleaseCompileToNativeCalls
	public void add(Command cmd) {
		if (cmd.getClass().isAnnotationPresent(CmdInfo.class))
			cmd.setInfo(cmd.getClass().getAnnotation(CmdInfo.class));
			
		this.put(cmd.getClass(), cmd);
	}
	

	public <T extends Command> T get(Class<T> moduleClass) {
        return moduleClass.cast(super.get(moduleClass));
    }
}
