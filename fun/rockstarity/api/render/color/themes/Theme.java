package fun.rockstarity.api.render.color.themes;

import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.color.FixColor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * @author ConeTin
 * @since 12 мар. 2024 г.
 */

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Theme {
	
	final String name;
	final FixColor firstColor, secondColor, thirdColor, foursColor, textFirstColor, textSecondColor;
	Animation selectAnimation = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	final int id;
	static int lastID;

	public Theme(String name,
				FixColor firstColor,
				FixColor secondColor,
				FixColor thirdColor,
				FixColor foursColor,
				FixColor textFirstColor,
				FixColor textSecondColor) {
		this.name = name;
		this.firstColor = firstColor;
		this.secondColor = secondColor;
		this.thirdColor = thirdColor;
		this.foursColor = foursColor;
		this.textFirstColor = textFirstColor;
		this.textSecondColor = textSecondColor;
		this.id = lastID++;
	}
	
}
