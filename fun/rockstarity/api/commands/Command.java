package fun.rockstarity.api.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import lombok.Getter;
import lombok.Setter;

/**
 * @author ConeTin
 * @since 9 дек. 2023 г.
 */

public class Command implements IAccess {
	
	@Getter @Setter
	private CmdInfo info;
	@Getter
	private final List<CommandParameter> parameters = new ArrayList<>();
	
	public void execute(String[] args) {}
	
	protected boolean contains(String arg, String... texts) {
		return Arrays.asList(texts).contains(arg.toLowerCase());
	}
	
	protected boolean contains(String arg, CommandParameter cmd) {
		return Arrays.asList(cmd.getNames()).contains(arg.toLowerCase());
	}
	
	public void onEvent(Event event) {}
	
	public void error() {
		rock.getAlertHandler().alert("Неверное использование команды", AlertType.ERROR);
	}

	public void msg(Object object) {
		Chat.msg(object);
	}
	
	public ArrayList<String> getSuggestions(String[] args, String full){
        return new ArrayList<>();
    }
}
