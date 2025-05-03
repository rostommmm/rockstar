package fun.rockstarity.api.via;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventWorldChange;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Server;
import lombok.experimental.UtilityClass;
import net.minecraft.client.gui.screen.ConnectingScreen;
import net.minecraft.client.gui.screen.DirtMessageScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Difficulty;

/**
 * @author ConeTin
 * @since 22 февр. 2025 г.
 */

@UtilityClass
public class ViaFixer implements IAccess {

	public static boolean reconnected, fixerJoin;
	
	public void onEvent(Event event) {
		if (event instanceof EventWorldChange && ViaFixer.fixerJoin && (Server.isHW() || Server.isFT() || Server.is("spooky")) && ViaLoadingBase.getInstance().getTargetVersion().isNewerThanOrEqualTo(ProtocolVersion.v1_17) && mc.world.getDifficulty() != Difficulty.NORMAL) {
			fixerJoin = false;
            reconnected = true;
            
			boolean flag = mc.isIntegratedServerRunning();
	        boolean flag1 = mc.isConnectedToRealms();
	        mc.world.sendQuittingDisconnectingPacket();

	        if (flag)
	        {
	        	mc.unloadWorld(new DirtMessageScreen(new TranslationTextComponent("menu.savingLevel")));
	        }
	        else {
	        	mc.unloadWorld();
	        }
	        
            mc.displayGuiScreen(new ConnectingScreen(new MainMenuScreen(), mc, ConnectingScreen.IP, ConnectingScreen.PORT));
		}
	}
	
}
