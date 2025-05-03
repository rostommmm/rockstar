package fun.rockstarity.api.binds;

import org.json.simple.JSONObject;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.ui.fonts.FontSize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author ConeTin
 * @since 24 июл. 2024 г.
 */

@Getter
@RequiredArgsConstructor
public class BindInfo<T> implements IAccess {
	
	private final Animation showingAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300), secondAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	private boolean hiding;
	
	private final String text;
	private final Type type;
	private final String key;
	private final T value;

	public void hide() {
		this.hiding = true;
	}
	
	public float getLeftWidth() {
		FontSize font = semibold.get(14);
		
		switch (type) {
		case ENABLE:
		case DISABLE:
			return 19;

		case MODE:
		case SLIDER:
			return 9 + font.getWidth(value.toString());

		default:
			break;
		}
		return 19;
	}
	
	public enum Type {
		ENABLE, DISABLE, SLIDER, MODE;
	}
	
	public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("hiding", this.hiding);
        jsonObject.put("text", this.text);
        jsonObject.put("type", this.type.ordinal());
        jsonObject.put("value", this.value.toString());
        jsonObject.put("key", this.key);
        return jsonObject;
    }
    
    public static BindInfo fromJson(JSONObject jsonObject) {
        boolean hiding = (Boolean) jsonObject.get("hiding");
        String text = (String) jsonObject.get("text");
        Type type = Type.values()[((Long) jsonObject.get("type")).intValue()];
        String key = (String) jsonObject.get("key");
        Object value = parseValue((String) jsonObject.get("value"), type);

        BindInfo bindInfo = new BindInfo<>(text, type, key, value);
        bindInfo.hiding = hiding;
        return bindInfo;
    }
    
    private static Object parseValue(String valueStr, Type type) {
        if (type == Type.ENABLE || type == Type.DISABLE) {
            return Boolean.parseBoolean(valueStr);
        } else if (type == Type.SLIDER) {
            return Float.parseFloat(valueStr);
        } else if (type == Type.MODE) {
            return valueStr;
        }
        return valueStr;
    }
	
}
