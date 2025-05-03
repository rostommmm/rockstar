package fun.rockstarity.api.render.globals.emotions.instance;

import lombok.Getter;

/**
 * @author ConeTin
 * @since 6 Р°РІРі. 2024вЂЇРі.
 */

public enum EmotionType {
	DEB("deb", "Р”РµР±"),
	FLOSS("floss", "Р¤Р»РѕСЃСЃ"),
    MASTURBATE("masturbate", "РњР°СЃС‚СѓСЂР±Р°С†РёСЏ"),
    HELLO("hello", "РџСЂРёРІРµС‚СЃС‚РІРёРµ"),
   // CONDITIONS("conditions", "РќР° РєРѕРЅРґРёС†РёСЏС…"),
    GET_GRIDDY("get_griddy", "\"Get Griddy\""),
	HAPPY("happy", "Р Р°РґРѕСЃС‚СЊ");
//	CRY("masturbate", "РњР°СЃС‚СѓСЂР±Р°С†РёСЏ"),
//	CLAP("masturbate", "РњР°СЃС‚СѓСЂР±Р°С†РёСЏ");
	
	@Getter
	private String name, localized;
	
	EmotionType(String name, String localized) {
		this.name = name;
		this.localized = localized;
	}
	
	public static EmotionType getFromName(String name) {
		for (EmotionType type : EmotionType.values()) {
    		if (type.getName().equals(name)) return type;
		}
		
		return DEB;
	}
	
}
