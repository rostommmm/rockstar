package fun.rockstarity.client.commands;

import fun.rockstarity.api.commands.CmdInfo;
import fun.rockstarity.api.commands.Command;
import fun.rockstarity.api.commands.CommandParameter;
import fun.rockstarity.api.modules.Info;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;
@NativeInclude
@CmdInfo(names = {"target"}, desc = "Приоритет таргета")
public class TargetCommand extends Command {
	
	CommandParameter save = new CommandParameter(this, "save", "create", "add");
	CommandParameter clear = new CommandParameter(this, "clear");
	CommandParameter delete = new CommandParameter(this, "delete", "remove", "del");
	CommandParameter list = new CommandParameter(this, "list");
	
	@Override
	public void execute(String[] args) {
		if (contains(args[0], save))
			rock.getTargetHandler().add(args[1]);
		else if (contains(args[0], clear))
			rock.getTargetHandler().clear();
		else if (contains(args[0], delete))
			rock.getTargetHandler().remove(args[1]);
		else if (contains(args[0], list))
			rock.getTargetHandler().list();
	}
}
