package fun.rockstarity.api.render.ui.alerts;

import java.util.ArrayList;
import java.util.Collections;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.binds.Bind;
import fun.rockstarity.api.binds.Bindable;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.client.modules.render.Interface;
import fun.rockstarity.client.modules.render.ui.Alerts;
import javafx.animation.Interpolator;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;

/**
 * @author ConeTin
 * @since 9 дек. 2023 г.
 */

public class Alert extends Rect implements IAccess {

	@Getter @Setter private Bindable bindable;
	@Getter @Setter private Bind bind;
	private final String text;
	@Getter private final AlertType type;
	private float ticks;
	private boolean closing;
	private float yAnim;
	
	@Getter
	private final Animation show = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300).setSize(1),
			open = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(200).setSize(1);
	
	public Alert(String text, AlertType type, float x, float y, float width, float height) {
		super(x, y, width, height);
		this.type = type;
		this.text = text;
		show.setForward(true);
	}
	
	public void render(MatrixStack matrixStack) {
		float openValue = (open.isForward() || !show.isForward() ? open.get() : 0);
		Alerts alerts = rock.getModules().get(Interface.class).getAlerts();
		boolean big = alerts.getMode().is(alerts.getBig());
		
		ArrayList<Alert> alertsList = new ArrayList<>(rock.getAlertHandler().getAlerts());
		
		if (!big)
			Collections.reverse(alertsList);
		
		yAnim = (float) Interpolator.LINEAR.interpolate(yAnim, alertsList.indexOf(this) * (height+(big ? 5 : (Interface.glow() || Interface.outline() ? 4 : 1))), 0.3f / Math.max((float) Minecraft.debugFPS, 5) * 75);

		if (big) {
			width = 25 + (10 + semibold.get(16).getWidth(text)) * openValue;
			height = 25;
			x = sr.getScaledWidth() - width - 10;
			y = sr.getScaledHeight() - height - 10 - yAnim;
			AlertUtility.drawBig(matrixStack, type, text, x, y, width, height, this.show.get());
		} else {
			width = semibold.get(14).getWidth(text) + 18;
			height = 15;
			x = sr.getScaledWidth() / 2 - width / 2 * rock.getModules().get(Interface.class).getSize().get();
			if (alerts.getPosition().is(alerts.getTop())) {
				y = 10 + yAnim;
			} else if (alerts.getPosition().is(alerts.getCenter())) {
				y = (float) (sr.getScaledHeight() / 2 + 10 + yAnim);
			} else if (alerts.getPosition().is(alerts.getBottom())) {
				y = sr.getScaledHeight() - 90 - yAnim;
			}
			AlertUtility.drawSmall(matrixStack, type, text, x, y, width, height, this.show.get());
		}
		
		ticks += 1 / Math.max((float) Minecraft.debugFPS, 5) * 75;

		if (type != AlertType.WAIT && ticks > 130) show.setForward(false);
		
		open.setForward(show.finished());
	}
	
	public void renderBackground(MatrixStack ms) {
		Round.draw(ms, new Rect(x,y,width,height), 3, rock.getThemes().getFirstColor().alpha(this.show.get()));
	}
	
	public void hide() {
		closing = true;
		show.setForward(false);
	}
	
}
