package fun.rockstarity.api.render.cursor;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CursorUtility {
	@Setter @Getter
	private boolean aimedHand;
	private CursorType current = CursorType.DEFAULT;
	
	public void setType(CursorType type) {
		current = type;
	}
	
	public CursorType getType() {
		return current;
	}
}
