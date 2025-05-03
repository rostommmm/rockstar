package fun.rockstarity.api.render.color.themes;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import fun.rockstarity.Rockstar;
import fun.rockstarity.api.helpers.render.ColorUtility;
import fun.rockstarity.api.helpers.secure.Web;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.animation.infinity.InfinityAnimation;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.secure.Debugger;
import fun.rockstarity.api.secure.nativeapi.NativeHelper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * @author ConeTin
 * @since 4 июн. 2024 г.
 */

@FieldDefaults(level = AccessLevel.PRIVATE)
public class Style {
	
	@Getter @Setter
	public static Style current = new Style("Loading", new FixColor[] {new FixColor(124,146,223), new FixColor(86,102,220)});
	
	@Getter
	private static ArrayList<Style> styles = new ArrayList<>();
	
	public static ArrayList<Style> values() {
		return styles;
	}
	
	public static FixColor getMain() {
		return ColorUtility.gradient(10, 1, current.getColors());
	}
	
	public static FixColor getSecond() {
		return ColorUtility.gradient(10, 50, current.getColors());
	}
	
	public static FixColor getPoint(int point) {
		return ColorUtility.gradient(10, point, current.getColors());
	}
	
	public static FixColor getOutlinePoint(int point) {
		return ColorUtility.gradient(10, point, new FixColor[] { 
				current.getColors()[0],
				Rockstar.getInstance().getThemes().getFirstColor(),
				current.getColors()[1],
				Rockstar.getInstance().getThemes().getFirstColor(),
		});
	}
	
	private static InfinityAnimation circleAnim = new InfinityAnimation();
	
	public static FixColor[] getCircle() {
		return getCircle(1);
	}
	
	public static FixColor[] getCircle(float alpha) {
		circleAnim.animate(System.currentTimeMillis()/1000F, 50);
		int val = (int) circleAnim.get();
		return new FixColor[] {
				Style.getPoint(val).alpha(alpha),
				Style.getPoint(val+90).alpha(alpha),
				Style.getPoint(val+180).alpha(alpha),
				Style.getPoint(val+270).alpha(alpha)
		};
	}
	
	@Getter final String name;
	@Getter final FixColor[] colors;
	@Getter Animation selectAnimation = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	
	public Style(String name, FixColor[] colors) {
		this.name = name;
		this.colors = colors;
	}
	
}
