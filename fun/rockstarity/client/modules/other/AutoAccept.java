package fun.rockstarity.client.modules.other;

import fun.rockstarity.api.connection.globals.ClientAPI;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Select.Element;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.util.text.TextFormatting;

/**
 * @author Malecharik
 * @since 26 мар. 2024 г. 23:29:06
 */

@Info(name = "AutoAccept", desc = "Автоматически принимает дуэли или тп", type = Category.OTHER)
public class AutoAccept extends Module {
	private boolean duel;
	private String name;

	private final Select utils = new Select(this, "Выбор");
	private final Element duelsAccept = new Element(utils, "Дуэли");
	private final Element tpaAccept = new Element(utils, "Телепорт").set(true);

	private final Select kits = new Select(this, "Киты").hide(() -> !this.duelsAccept.get());
	private final Element classicKits = new Element(kits, "Классик");
	private final Element totemsKits = new Element(kits, "Тотемы").set(true);
	private final Element noDeBuffKits = new Element(kits, "НоДебафф");
	private final Element cheatsParadiseKits = new Element(kits, "Читерский рай");
	private final Element nezerKits = new Element(kits, "Незеритка");
	private final Element shipiKits = new Element(kits, "Шипы");
	private final Element shieldKits = new Element(kits, "Щит");
	private final Element bowKits = new Element(kits, "Лук");
	private final Element sharKits = new Element(kits, "Шары");

	private final Select accept = new Select(this, "Принимать");
	private final Element all = new Element(accept, "Всех");
	private final Element friends = new Element(accept, "Друзей").hide(() -> all.get());
	private final Element users = new Element(accept, "Пользователей Rockstar").hide(() -> all.get() || !rock.getModules().get(Globals.class).get());

	@Override
	public void onEvent(Event event) {
		if (event instanceof EventReceivePacket receive && receive.getPacket() instanceof SChatPacket packet) {
			String message = TextFormatting.getTextWithoutFormattingCodes(packet.getChatComponent().getString());
			if (message.contains("Набор: ") && duel && duelsAccept.get()) {
				duel = false;
				boolean accept = classicKits.isEnabled() || totemsKits.isEnabled() || noDeBuffKits.isEnabled()
						|| cheatsParadiseKits.isEnabled() || shipiKits.isEnabled() || shieldKits.isEnabled()
						|| bowKits.isEnabled() || nezerKits.isEnabled() || sharKits.isEnabled();
				mc.player.sendChatMessage("/duel " + (accept ? "accept" : "deny") + " " + name);
			}
			if ((message.contains("телепортироваться")) && this.tpaAccept.get()) {
				if (canAccept(message)) {
					mc.player.sendChatMessage("/tpaccept");
				}
			}
			if (message.contains("Ник: ") && duelsAccept.get()) {
				if (canAccept(message)) {
					duel = true;
					name = message.replace("➝ Ник: ", "");
				}
			}
		}
	}
	
	private boolean canAccept(String message) {
		if (all.get()) return true;
		
		if (friends.get()) {
			if ((rock.getFriendsHandler().isFriend(message.split(" ")[1])
					|| rock.getFriendsHandler().isFriend(message.replace("੷ просит телепортироваться к Вам.੷§l [ੲ§l✔੷§l]੷§l [੼§l✗੷§l]", "").replace("੶", ""))
					|| rock.getFriendsHandler().isFriend(message.replace("➝ Ник: ", "")))) {
				return true;
			}
	        if (message.contains("телепортироваться")) {
	            String[] parts = message.split(" ");
	            if (parts.length >= 2 && rock.getFriendsHandler().isFriend(parts[2])) 
	                return true;
	        }
		}
		
		if (users.get() && (
			ClientAPI.getClient(message.split(" ")[1]) != null || 
			ClientAPI.getClient(message.replace("੷ просит телепортироваться к Вам.੷§l [ੲ§l✔੷§l]੷§l [੼§l✗੷§l]", "").replace("੶", "")) != null ||
			ClientAPI.getClient(message.replace("➝ Ник: ", "")) != null))
			return true;
			
		return false;
	}
	
	@Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}
	
}
