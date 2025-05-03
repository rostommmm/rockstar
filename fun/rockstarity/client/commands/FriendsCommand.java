package fun.rockstarity.client.commands;

import fun.rockstarity.Rockstar;
import fun.rockstarity.api.commands.CmdInfo;
import fun.rockstarity.api.commands.Command;
import fun.rockstarity.api.commands.CommandParameter;
import fun.rockstarity.api.friends.Friend;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

import org.luaj.vm2.ast.Str;

import javax.annotation.Nullable;

/**
 * @author ConeTin
 * @since 9 дек. 2023 г.
 * @author jbk
 * @since 31.01.2025
 */
@NativeInclude
@CmdInfo(names={"friend","friends"}, desc="Позволяет управлять списком друзей")
public class FriendsCommand extends Command {
	
	CommandParameter save = new CommandParameter(this, "add", "create", "save");
	CommandParameter clear = new CommandParameter(this, "clear");
	CommandParameter del = new CommandParameter(this, "delete", "remove");
	CommandParameter list = new CommandParameter(this, "list");

	@Override
	public void execute(String[] args) {
		String friendName = args.length < 2 ? "" : args[1];
		String hiddenFriendName = args.length < 3 ? "" : args[2];
		switch (args[0].toLowerCase()) {
			case "add", "create", "save" -> Rockstar.getInstance().getFriendsHandler().add(friendName, hiddenFriendName);
			case "clear" -> Rockstar.getInstance().getFriendsHandler().clear();
			case "delete", "remove", "del" -> Rockstar.getInstance().getFriendsHandler().remove(friendName);
			case "list" -> Rockstar.getInstance().getFriendsHandler().list();
		}
	}
}