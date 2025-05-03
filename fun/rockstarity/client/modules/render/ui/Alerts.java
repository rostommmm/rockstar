package fun.rockstarity.client.modules.render.ui;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventWorldChange;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.events.list.render.ui.shaders.EventBlur;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Mode.Element;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.ui.alerts.AlertUtility;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.client.modules.render.Interface;
import fun.rockstarity.client.modules.render.Interface.UIElement;
import lombok.Getter;
import net.minecraft.client.gui.screen.ChatScreen;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author Malecharik
 * @since 27 мар. 2024 г. 19:09:25
 */

@Getter
public class Alerts extends UIElement {
	
	private float x, y, width, height;
	
	private final CheckBox transparent = new CheckBox(this, "Прозрачный");
	
	private final CheckBox additions = new CheckBox(this, "Обводка/Свечение").set(true).hide(() -> !Interface.outline() && !Interface.glow());
	
	private final Mode mode = new Mode(this, "Режим уведомлений");
	
	private final Element big = new Element(mode, "Большой");
	private final Element small = new Element(mode, "Маленький");
	
	private final Mode position = new Mode(this, "Расположение").hide(() -> mode.is(big));
	
	private final Element top = new Element(position, "Сверху");
	private final Element center = new Element(position, "В центре");
	private final Element bottom = new Element(position, "Снизу");
	
	private final Animation showAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	@NativeInclude
	public Alerts(Interface ui, Select select) {
		super(select, "Уведомления", new Rect(10, 40, 0, 0));
		
		this.position.set(center);
		
		this.set(true);
	}
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventWorldChange e) 
			rock.getAlertHandler().getAlerts().forEach(alert -> alert.hide());
		
		if (event instanceof EventBlur e && this.transparent.get() && blur())
			rock.getAlertHandler().renderBackground(e.getMatrixStack());
		
		if (event instanceof EventRender2D e) {
			GL11.glPushMatrix();
		    GL11.glTranslated(0, 0, 999);
		    rock.getAlertHandler().render(e.getMatrixStack());
		    GL11.glPopMatrix();
		}
		
		if (event instanceof EventRender2D e) {
			this.draggable.setCanDrag(false);
			
			MatrixStack ms = e.getMatrixStack();
			
			String text = rock.getModules().get(Interface.class).getEnglish().get() ? "Alert example" : "Пример уведомления";
			
			boolean big = this.mode.is(this.big);
			
			if (big) {
				width = semibold.get(16).getWidth(text) + 35;
				height = 25;
				x = sr.getScaledWidth() - width - 10;
				y = sr.getScaledHeight() - height - 10;
			} else {
				width = semibold.get(14).getWidth(text) + 18;
				height = 15;
				x = sr.getScaledWidth() / 2 - width / 2 * rock.getModules().get(Interface.class).getSize().get();
				if (this.position.is(top)) {
					y = 10;
				} else if (this.position.is(center)) {
					y = sr.getScaledHeight() / 2 + 10;
				} else if (this.position.is(bottom)) {
					y = sr.getScaledHeight() - 90;
				}
			}
			
			this.showAnim.setForward(mc.currentScreen instanceof ChatScreen && rock.getAlertHandler().getAlerts().isEmpty());
			
			this.draggable.setX(x);
			this.draggable.setY(y);
			this.draggable.setWidth(width);
			this.draggable.setHeight(height);
			draggable.setDragging(false);
			
			if (big)
				AlertUtility.previewBig(ms, x, y, width, height, this.showAnim.get());
			else
				AlertUtility.previewSmall(ms, x, y, width, height, this.showAnim.get());
		}
	}
	
}
