package fun.rockstarity.api.binds;

import fun.rockstarity.api.modules.settings.Setting;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.ui.alerts.Alert;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author ConeTin
 * @since 2 дек. 2023 г.
 */

@Getter @Setter
@AllArgsConstructor
public class Bind {
	
	private boolean enabled;
	private String text;
	private Bindable parent;
	private final Animation showingAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300), secondAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	private int key, scancode;
	private boolean holding;
	private BindType type;
	private Alert alert;
	
	public Bind(int key) {
		this.key = key;
		this.scancode = -1;
		this.type = BindType.TOGGLE;
	}
	
	public Bind(int key, BindType type) {
		this.key = key;
		this.scancode = -1;
		this.type = type;
	}

	public Bind(int key, int scancode, BindType type) {
		this.key = key;
		this.scancode = scancode;
		this.type = type;
	}

}
