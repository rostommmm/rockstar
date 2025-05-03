package fun.rockstarity.api.helpers.game.proxy;

import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProxyUtility {
	@Getter
    private String proxyIp;
    @Getter
    private int proxyPort;
    @Getter
    private boolean enabled;
    
    public void setProxy(String ip, int port) {
        proxyIp = ip;
        proxyPort = port;
        enabled = true;
    }
    
    public void disableProxy() {
        enabled = false;
    }
}
