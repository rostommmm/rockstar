package fun.rockstarity.api.helpers.game;

import java.net.Proxy;

import com.mojang.authlib.Agent;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.connection.globals.ServerAPI;
import fun.rockstarity.api.via.ViaFixer;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

/**
 * @author ConeTin
 * @since 8 дек. 2023 г.
 */

@UtilityClass
public class GameUtility implements IAccess {

	public void changeName(String name) {
		name = name.replace("\\", "");
		
		YggdrasilAuthenticationService service = new YggdrasilAuthenticationService(Proxy.NO_PROXY, "");
		YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication) service.createUserAuthentication(Agent.MINECRAFT);
		auth.logOut();

		mc.setSession(new Session(name,name,"0", "legacy"));
		ServerAPI.updateName();
		
		ViaFixer.reconnected = false;
	}
	
}
