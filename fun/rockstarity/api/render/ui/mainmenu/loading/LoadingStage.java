package fun.rockstarity.api.render.ui.mainmenu.loading;

import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * @author ConeTin
 * @since 19 С„РµРІСЂ. 2025вЂЇРі.
 */

@Getter
@RequiredArgsConstructor
public enum LoadingStage {
	
	AUTH("РђРІС‚РѕСЂРёР·Р°С†РёСЏ"),
	CLIENT("Р—Р°РіСЂСѓР·РєР° РєР»РёРµРЅС‚Р°"),
	FONTS("Р—Р°РіСЂСѓР·РєР° С€СЂРёС„С‚РѕРІ"),
	IMAGES("Р—Р°РіСЂСѓР·РєР° РєР°СЂС‚РёРЅРѕРє");
	
	public static LoadingStage current;
	
	private final String title;
	@Setter private String stage;
	@Setter private float progress;
	
	private final Animation activeAnim = new Animation().setEasing(Easing.EASE_OUT_CIRC).setSpeed(300).setForward(false).finish();
	private final Animation readyAnim = new Animation().setEasing(Easing.EASE_OUT_CIRC).setSpeed(300).setForward(false).finish();
	public static final TimerUtility afterReady = new TimerUtility();	
	
	public void activate() {
		if (current != null) {
			current.stage = "Р“РѕС‚РѕРІРѕ!";
			current.readyAnim.setForward(true);
			current.afterReady.reset();
		}
		activeAnim.setForward(true);
		current = this;
	}
	
	public static void stage(String stage) {
		current.stage = stage;
		System.out.println(current.title + " - " + stage);
	}
	
}
