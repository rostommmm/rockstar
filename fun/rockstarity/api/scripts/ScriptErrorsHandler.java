package fun.rockstarity.api.scripts;

import fun.rockstarity.api.helpers.game.Chat;

public class ScriptErrorsHandler {

	public static void handle(String error) {
		String msg = error.split("\n")[error.split("\n").length-1];
		Chat.msg(editMessage(Integer.parseInt(msg.split(" ")[0].split(":")[1]), msg));
	}
	
	private static String editMessage(int line, String msg) {
		for (ScriptErrors error : ScriptErrors.values()) {
			if (msg.contains(error.getOrig())) {
				return "РћС€РёР±РєР° РІ " + line + " СЃС‚СЂРѕРєРµ. " + error.getRu();
			}
		}
		
		return msg;
	}
	
}