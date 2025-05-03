package fun.rockstarity.api.scripts;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ScriptErrors {
	TYPE_MISSMATCH("argument type mismatch", "РќРµРІРµСЂРЅС‹Р№ С‚РёРї Р°СЂРіСѓРјРµРЅС‚Р°"),
	NIL_VALUE("attempt to index ? (a nil value)", "РџСѓСЃС‚РѕРµ Р·РЅР°С‡РµРЅРёРµ");
	
	@Getter
	private final String orig, ru;
}