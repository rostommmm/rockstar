package fun.rockstarity.client.modules.other;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.helpers.system.TextUtility;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Input;
import net.minecraft.network.play.server.SChatPacket;

/**
 * @author Malecharik
 * @since 16 Mar 2024 13:15:08
 */


@Info(name="AutoAuth", desc="Автоматически входит на сервер", type=Category.OTHER)
public class AutoAuth extends Module {
	
	private final CheckBox random = new CheckBox(this, "Рандом пароль").desc("Случайный пароль для аккаунта на сервере");
	private final Input passwords = new Input(this, "Пароль..").hide(random::get).desc("Введите пароль, который будет использоваться для автоматического входа");
	
	@Override
	public void onEvent(Event event) {
	    if (event instanceof EventReceivePacket e && e.getPacket() instanceof SChatPacket packet) {
	        String message = packet.getChatComponent().getString().toLowerCase();
	        String password = this.random.get() ? TextUtility.getRandomNick() : this.passwords.get();
	        //System.out.println("Received message: " + message);
	        if (message.contains("Зарегистрируйтесь") || message.contains("/reg")) {
	            mc.player.sendChatMessage(String.format("/reg %s %s", password, password));
	        } else if (message.contains("Авторизуйтесь") || message.contains("/login") || (message.contains("/l") && message.matches("/l(\\s|$)"))) {
	            mc.player.sendChatMessage(String.format("/l %s", password));
	        }
	    }
	}
	
	@Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}
}
