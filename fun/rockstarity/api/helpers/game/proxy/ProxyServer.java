package fun.rockstarity.api.helpers.game.proxy;

import net.minecraft.client.gui.widget.button.Button;

public class ProxyServer {
	public static boolean proxyEnabled = false;
	public static Proxy proxy = new Proxy();
	public static Proxy lastUsedProxy = new Proxy();
	public static Button proxyMenuButton;

	public ProxyServer() {
		onInitialize();
	}

	public void onInitialize() {
		AccountsProxy.loadProxyAccounts();
	}

	public static String getLastUsedProxyIp(){
		return lastUsedProxy.ipPort.isEmpty() ? "none" : lastUsedProxy.getIp();
	}
}
