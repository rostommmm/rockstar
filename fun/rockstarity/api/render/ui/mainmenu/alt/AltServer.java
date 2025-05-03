package fun.rockstarity.api.render.ui.mainmenu.alt;

import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.util.text.ITextComponent;

/**
 * @author ConeTin
 * @since 23 РѕРєС‚. 2024вЂЇРі.
 */

@Getter
@RequiredArgsConstructor
public class AltServer {
	
	private final Animation hoverAnim = new Animation().setEasing(Easing.EASE_OUT_CIRC).setSpeed(500);
	private final String ip;
	@Setter private String ban = "";
	//private ITextComponent donate;

}
