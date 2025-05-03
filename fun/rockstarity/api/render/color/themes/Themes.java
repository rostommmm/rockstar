package fun.rockstarity.api.render.color.themes;

import java.util.ArrayList;
import java.util.Arrays;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import fun.rockstarity.api.render.animation.infinity.ColorAnimation;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.list.DarkTheme;
import fun.rockstarity.api.render.color.themes.list.LightTheme;
import fun.rockstarity.api.secure.Debugger;
import fun.rockstarity.api.secure.nativeapi.NativeHelper;
import lombok.Getter;
import lombok.Setter;

/**
 * @author ConeTin
 * @since 12 мар. 2024 г.
 */

@Getter
public class Themes extends ArrayList<Theme> {
	
	private Theme lightTheme = new LightTheme();
	private Theme darkTheme = new DarkTheme();

	@Setter
	public Theme current = darkTheme;
	
	private static final int SPEED = 50;
	
	private final ColorAnimation firstColor = new ColorAnimation();
	private final ColorAnimation secondColor = new ColorAnimation();
	private final ColorAnimation thirdColor = new ColorAnimation();
	private final ColorAnimation foursColor = new ColorAnimation();
	private final ColorAnimation textFirstColor = new ColorAnimation();
	private final ColorAnimation textSecondColor = new ColorAnimation();
	
	public Themes() {
		Arrays.stream(new Theme[] {
				lightTheme,
				darkTheme
		}).forEach(this::add);
	}
	
	public String getName() {
		return this.current.getName();
	}

	public FixColor getFirstColor() {
		return this.firstColor.animate(this.current.getFirstColor(), SPEED);
	}

	public FixColor getSecondColor() {
		return this.secondColor.animate(this.current.getSecondColor(), SPEED);
	}

	public FixColor getThirdColor() {
		return this.thirdColor.animate(this.current.getThirdColor(), SPEED);
	}
	public FixColor getFoursColor() {
		return this.foursColor.animate(this.current.getFoursColor(), SPEED);
	}

	public FixColor getTextFirstColor() {
		return this.textFirstColor.animate(this.current.getTextFirstColor(), SPEED*5);
	}

	public FixColor getTextSecondColor() {
		return this.textSecondColor.animate(this.current.getTextSecondColor(), SPEED*5);
	}
	
}
