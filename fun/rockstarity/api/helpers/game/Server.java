package fun.rockstarity.api.helpers.game;

import java.util.ArrayList;
import java.util.Arrays;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;
import net.minecraft.client.gui.overlay.BossOverlayGui;
import net.minecraft.client.gui.overlay.PlayerTabOverlayGui;
import net.minecraft.network.play.server.SUpdateTimePacket;
import net.minecraft.util.math.MathHelper;

public class Server implements IAccess {
	public static int FT_ANARCHY = -1, RW_GRIEF = -1;
	
	private  static String lastIP = "mc.funtime.su";
	
	public  static String getIP() {
		if (!Player.isInGame()) return "mainmenu";
		if (mc.isSingleplayer()) return "local";
		if (mc.getCurrentServerData() != null) return lastIP = mc.getCurrentServerData().serverIP.toLowerCase();
		
		return lastIP;
	}

	public  static boolean isSunWay() {
		String ip = getIP();
		return ip.contains("sunw");
	}
	
	public  static boolean isSaturn() {
		String ip = getIP();
		return ip.contains("saturn");
	}
	
	public static boolean isSR() {
		String ip = getIP();
		return ip.contains("sunmc");
	}

	public  static boolean isFS() {
		String ip = getIP();
		return ip.contains("funsky");
	}

	public static boolean isBravo() {
		String ip = getIP();
		return ip.contains("bravohvh");
	}
	
	public static boolean isRW() {
		String ip = getIP();
		return ip.contains("reallyworld") || ip.contains("playrw");
	}

	public static boolean isServerForHPFix() {
		String ip = getIP();
		return isFT() || isFS() || isRW() || isSaturn() || ip.contains("legend") || ip.contains("wildgrief") || ip.contains("lighttime");
	}
	
	public static String getServerName(boolean shortName) {
		String ip = getIP();
		String[] parts = ip.split("\\.");
		
		if (mc.isSingleplayer())
			return applyCase(ip, shortName);
		
        if (parts.length == 3)
            return applyCase(parts[1], shortName);
        
        if (parts.length == 2)
            return applyCase(parts[0], shortName);
        
        if (ip.contains(":"))
        	return ip.split(":")[0];
        
		return ip;
	}
	
	private static String applyCase(String server, boolean shortName) {
		server = server.replace("-", "");
		
		ArrayList<Data> datas = new ArrayList<>();
		String[] suffixes = { "legacy", "bars", "world", "best", "times", "time", "shine", "sky", "lands", "land", "trainer", "server", "blaze", "mine", "lord", "cube" , "grief", "craft", "rise", "force", "project" };
		
		Arrays.stream(suffixes).forEach(suffix -> datas.add(genData(suffix)));
		
		Arrays.stream(new Data[] {
				new Data("mc", "MC", "-MC"),
				new Data("hvh", "HVH", "-HVH"),
				new Data("pvp", "PVP", "PVP")
		}).forEach(datas::add);
		
		if (mc.isSingleplayer() && !shortName)
			server = "LocalHost";
		
		if (isSR())
			server = shortName ? "SR" : "SunRise";
		
		if (isSaturn())
			server = shortName ? "S-X" : "SaturnX";
		
		if (isSunWay())
			server = shortName ? "SW" : "SunWay";
		
		for (Data data : datas) {
			if (server.contains(data.orig)) {
				if (shortName) {
					String rightPart = server.replace(data.orig, "");
					server = server.substring(0, 1).toUpperCase() + data.small;
				} else {
					server = server.replace(data.orig, data.big);
					server = server.substring(0, 1).toUpperCase() + server.substring(1);
				}
				
				return server;
			}
		}

		server = server.substring(0, 1).toUpperCase() + server.substring(1);
		
		return server;
	}
	
	public static boolean isFT() {
		String ip = getIP();
		return ip.contains("funtime") || ip.contains("ft") || ip.contains("FunTime");
	}
	
	public static boolean isHW() {
		String ip = getIP();
		return ip.contains("holyworld") || ip.contains("hollyworld");
	}
	
	public static boolean is(String str) {
		return getIP().contains(str);
	}
	
	public static boolean hasCT() {
		return BossOverlayGui.CT;
	}
	
	public static int getTimeCT() {
		return BossOverlayGui.timeCT;
	}

	public static int ping() {
		return PlayerTabOverlayGui.getPlayerPings() == null || !PlayerTabOverlayGui.getPlayerPings().containsKey(mc.player.getName().getString()) ? 0 : PlayerTabOverlayGui.getPlayerPings().get(mc.player.getName().getString());
	}

	public static void warningFT() {
		if (!isFT()) {
			rock.getAlertHandler().alert("Этот модуль подходит под FunTime!", AlertType.INFO);
		}
	}
	
	private static Data genData(String full) {
		return new Data(full, full.substring(0, 1).toUpperCase() + full.substring(1), full.substring(0, 1).toUpperCase());
	}

	 static class Data {
		private  static String orig, big, small;

		public Data(String ori, String bi, String smal) {
			orig = ori;
			big = bi;
			small = smal;
		}
	}
}
