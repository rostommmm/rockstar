package fun.rockstarity.api.autobuy.bots.interfaces;

import fun.rockstarity.api.autobuy.bots.connection.BotNetworkManager;
import net.minecraft.util.text.ITextComponent;

public interface IBotNetHandler
{
    /**
     * Invoked when disconnecting, the parameter is a ChatComponent describing the reason for termination
     */
    void onDisconnect(ITextComponent reason);

    /**
     * Returns this the NetworkManager instance registered with this NetworkHandlerPlayClient
     */
    BotNetworkManager getNetworkManager();
}
