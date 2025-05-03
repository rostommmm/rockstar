package fun.rockstarity.client.modules.other;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.friends.Friend;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Input;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Select.Element;
import lombok.Getter;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author Malecharik
 * @since 14 апр. 2024 г. 09:17:11
 */

@Getter
@Info(name="NameProtect", desc="Скрывает ваш ник", type=Category.OTHER)
public class NameProtect extends Module {
	
	private final Select select = new Select(this, "Скрывать..");
	
	private final Element self = new Element(select, "Себя");
	private final Element friend = new Element(select, "Друзей");
	private final Element all = new Element(select, "Всех");
	
	private final Input nick = new Input(this, "Свой ник").set("Rockstarov").hide(() -> !self.get());
	private final Input nickFriend = new Input(this, "Ник друга").set("Дружбан").hide(() -> !friend.get());
	@Getter private final Input nickAll = new Input(this, "Ник всех").set("Чушпан").hide(() -> !all.get());

	@Override
	public void onDisable() {

	}

	@NativeInclude
	@Override
	public void onEnable() {
		
	}

	@Override
	public void onEvent(Event event) {
		
	}
	
	public static String correctText(String text) {
		NameProtect prot = rock.getModules().get(NameProtect.class);

		if (!prot.get() || !Player.isInGame()) 
			return text;
			
		if (text.contains(mc.player.getNameClear())) {
			text = text.replace(mc.player.getNameClear(), prot.getNick().get());
			return text;
		}
		
		if (prot.getFriend().get()) {
            for (Friend friend : rock.getFriendsHandler().getFriends()) {
                if (text.contains(friend.getName())) {
                    text = text.replaceAll(friend.getName(), friend.getHiddenName(prot.getNickFriend().get()));
            		return text;
                }
            }
        }
		
		return text;
	}
	
	public static String correct(String nick) {
		NameProtect prot = rock.getModules().get(NameProtect.class);
		
		if (!prot.get() || !Player.isInGame()) 
			return nick;;
		
		if (nick.contains(mc.player.getNameClear())) {
			nick = nick.replace(mc.player.getNameClear(), prot.getNick().get());
			return nick;
		}
		
		if (prot.getFriend().get()) {
            for (Friend friend : rock.getFriendsHandler().getFriends()) {
                if (nick.contains(friend.getName())) {
                    nick = nick.replace(friend.getName(), friend.getHiddenName(prot.getNickFriend().get()));
            		return nick;
                }
            }
        }
		
		if (prot.getAll().get()) {
			nick = prot.getNickAll().get();
        	return nick;
		}
		
		return nick;
	}
	
}
