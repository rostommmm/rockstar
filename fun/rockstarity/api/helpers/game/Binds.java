package fun.rockstarity.api.helpers.game;

import java.util.HashMap;

import net.minecraft.client.util.InputMappings;

/**
 * @author ConeTin
 * @since 12 РґРµРє. 2023 Рі.
 */

public class Binds {
	
	public static HashMap<String, Integer> KEYS = new HashMap<>();
	
	public static void add(String nameIn, int keyCode) {
		KEYS.put(nameIn.replace("key.keyboard.", "").replace("key.", "").replace(".", "").replace("left", keyCode == 263 || keyCode == 262 ? "left" : "l").replace("right",  keyCode == 263 || keyCode == 262 ? "right" : "r").replace("printscreen", "prtsc").replace("graveaccent", "grave"), keyCode);
	}

	public static String getName(int key, int scan) {
	    String str = InputMappings.getInputByCode(key, scan).toString()
	            .replace("SCANCODE", "")
	            .replace("key.keyboard.", "")
	            .replace("key.", "")
	            .replace(".", "")
	            .replace("left", "l")
	            .replace("right", "r")
	            .replace("printscreen", "prtsc")
	            .replace("graveaccent", "grave")
	            .replace("control", "ctrl").toUpperCase();
		
	    if (key == -1) {
	        switch (scan) {
	            case 0: str = "Р›РљРњ"; break;
	            case 1: str = "РџРљРњ"; break;
	            case 2: str = "РљРѕР»РµСЃРёРєРѕ"; break;
	            case 3: str = "MOUSE4"; break;
	            case 4: str = "MOUSE5"; break;
	            case 5: str = "MOUSE6"; break;
	            default: str = "MOUSE" + scan; break;
	        }
	    }
	    
	    if (key < 0 && scan < 0) {
	        str = "NONE";
	    }
		
		return str;
	}
	
}
