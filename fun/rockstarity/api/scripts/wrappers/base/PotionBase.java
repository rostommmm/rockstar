package fun.rockstarity.api.scripts.wrappers.base;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.potion.Effect;

@Getter @Setter
@AllArgsConstructor
public class PotionBase {
	private Effect effect;
	private String name, time;
	
	public String name() {
		return name;
	}
	
	public String time() {
		return time;
	}
}