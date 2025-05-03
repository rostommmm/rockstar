package fun.rockstarity.client.commands;

import fun.rockstarity.api.commands.CmdInfo;
import fun.rockstarity.api.commands.Command;
import fun.rockstarity.api.commands.CommandParameter;
import fun.rockstarity.api.helpers.game.Chat;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.util.text.TextFormatting;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author Malecharik
 * @since 9 Mar 2024 13:20:17
 */
@NativeInclude
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@CmdInfo(names = { "fp", "fakeplayer" }, desc = "Создает фейк игрока")
public class FakePlayerCommand extends Command {
	
	CommandParameter add = new CommandParameter(this, "add");
	CommandParameter del = new CommandParameter(this, "del", "remove", "delete");
	
	@Override
	public void execute(String[] args) {
		if (args.length == 0) {
			this.error();
			return;
		}

		if (args.length == 1) {
			if (contains(args[0], add)) {
				RemoteClientPlayerEntity fakePlayer = new RemoteClientPlayerEntity(mc.world, mc.player.getGameProfile());
				fakePlayer.inventory = mc.player.inventory;
				fakePlayer.copyLocationAndAnglesFrom(mc.player);
				fakePlayer.rotationYawHead = mc.player.rotationYawHead;
				mc.world.addEntity(-1337, fakePlayer);
				Chat.msg(TextFormatting.AQUA + "[Rockstar] " + TextFormatting.RESET + "Бот успешно добавился!", "Удалить последнего фейк игрока", () -> {
					mc.world.removeEntityFromWorld(-1337);
				});;
			} else if (contains(args[0], del)) {
				 mc.world.removeEntityFromWorld(-1337);
			}
		} else {
			if (contains(args[0], add)) {
				RemoteClientPlayerEntity fakePlayer = new RemoteClientPlayerEntity(mc.world, mc.player.getGameProfile());			
				fakePlayer.inventory = mc.player.inventory;
				fakePlayer.copyLocationAndAnglesFrom(mc.player);
				fakePlayer.rotationYawHead = mc.player.rotationYawHead;
				mc.world.addEntity(-Integer.parseInt(args[1]), fakePlayer);
				Chat.msg(TextFormatting.AQUA + "[Rockstar] " + TextFormatting.RESET + "Бот номер " + args[1] + " успешно добавлен!", "Удалить данного фейк игрока", () -> {
					mc.world.removeEntityFromWorld(-Integer.parseInt(args[1]));
				});
			} else if (contains(args[0], del)) {
				mc.world.removeEntityFromWorld(-Integer.parseInt(args[1]));
			}
		}
	}
}
