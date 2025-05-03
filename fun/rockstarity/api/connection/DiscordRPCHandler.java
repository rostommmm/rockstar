package fun.rockstarity.api.connection;

import java.time.OffsetDateTime;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.RichPresence;

import fun.rockstarity.api.ClientInfo;
import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.secure.users.User;
import lombok.Getter;

/**
 * @author ConeTin
 * @since 2 апр. 2024 г.
 */

public class DiscordRPCHandler implements IAccess {
	@Getter
	private IPCClient client;
	
	private RichPresence.Builder getBuilder() {
		RichPresence.Builder builder = new RichPresence.Builder();
		
		User user = rock.getUser();
		
		return builder.setDetails(String.format("Build: %s %s", ClientInfo.ACCESS, ClientInfo.VERSION))
				.setState("UID: " + user.getUid())
	            .setStartTimestamp(rock.isDebugging() ? OffsetDateTime.now().minusHours(11).minusMinutes(12) : OffsetDateTime.now())
	            .setLargeImage("https://rockstar.moscow/api/v1/files/premium/gifs/animlogo.gif", "vk.com/rockstarclient")
	            .setSmallImage(user.getAvatar(), user.getName());
	            //.setButton1Text("Сайт")
	            //.setButton1Url("https://rockstar.moscow/index.php")
	            //.setButton2Text("Discord")
	            //.setButton2Url("https://dsc.gg/rockclient");
	}

	public void update() {
	    try {
	        client = new IPCClient(1224735403727257672L);
	        client.setListener(new IPCListener() {
				@Override
				public void onReady(IPCClient client, com.jagrosh.discordipc.entities.User user) {
					client.sendRichPresence(getBuilder().build());
				}
			});
	        client.connect();
	    } catch (Exception e) {
	    }
	}
}
