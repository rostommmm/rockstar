package fun.rockstarity.api.render.ui.alerts;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.helpers.render.Stencil;
import fun.rockstarity.api.modules.Modules;
import fun.rockstarity.api.render.animation.infinity.InfinityAnimation;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.shaders.list.Outline;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.client.modules.render.Interface;
import fun.rockstarity.client.modules.render.ui.Alerts;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;

/**
 * @author ConeTin
 * @since 26 июл. 2024 г.
 */

@UtilityClass
public class AlertUtility implements IAccess {
	
	private final TimerUtility timer = new TimerUtility();
	private final InfinityAnimation slotAnim = new InfinityAnimation();
	private int slot;
	private boolean direction;
	
	@Setter @Getter
	private float ticks;
	
	public void previewSmall(MatrixStack ms, float x, float y, float width, float height, float anim) {
		Interface ui = rock.getModules().get(Interface.class);
		Alerts alerts = ui.getAlerts();

		if (rock.getModules().get(Interface.class).getAlerts().getAdditions().get()) {
			Render.glow(ms, new Rect(x,y,width,height), anim);
		}
		Round.draw(ms, new Rect(x,y,width,height), 3, rock.getThemes().getSecondColor().alpha(anim * (alerts.getTransparent().get() ? 0.5f : 1)));
		
		slotAnim.animate(slot, 50);
		
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		Render.scissor(x,y,width,height);
		
		for (AlertType type : AlertType.values()) {
			drawSmallIcon(ms, type, x, y + type.ordinal() * height - slotAnim.get() * height, anim);
		}
		
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		
		if (timer.passed(2000)) {
			if (slot == AlertType.values().length-1 && direction || slot == 0 && !direction) direction = !direction;
			slot += direction ? 1 : -1;
			timer.reset();
		}
		
		Stencil.init();
		Round.draw(ms, new Rect(x,y,width,height), 4, FixColor.WHITE.alpha(anim));
		Stencil.read(1);
		semibold.get(13).draw(ms, rock.getModules().get(Interface.class).getEnglish().get() ? "Alert example" : "Пример уведомления", x + 16, y + 3f, rock.getThemes().getTextFirstColor().alpha(anim));
		Stencil.finish();
		
		if (rock.getModules().get(Interface.class).getAlerts().getAdditions().get()) {
			Render.outline(ms, new Rect(x, y, width, height), anim);
		}
		
		ticks += 1 / Math.max((float) Minecraft.debugFPS, 5) * 75;
	}
	
	public void previewBig(MatrixStack ms, float x, float y, float width, float height, float anim) {
		Interface ui = rock.getModules().get(Interface.class);
		Alerts alerts = ui.getAlerts();

		if (rock.getModules().get(Interface.class).getAlerts().getAdditions().get()) {
			Render.glow(ms, new Rect(x,y,width,height), anim);
		}
		Round.draw(ms, new Rect(x,y,width,height), 4, rock.getThemes().getFirstColor().alpha(anim * (alerts.getTransparent().get() ? 0.5f : 1)));
		
		slotAnim.animate(slot, 50);
		
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		Render.scissor(x,y,width,height);
		
		for (AlertType type : AlertType.values()) {
			drawBigIcon(ms, type, x, y + type.ordinal() * height - slotAnim.get() * height, anim);
		}
		
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		
		if (timer.passed(2000)) {
			if (slot == AlertType.values().length-1 && direction || slot == 0 && !direction) direction = !direction;
			slot += direction ? 1 : -1;
			timer.reset();
		}
		
		Stencil.init();
		Round.draw(ms, new Rect(x,y,width,height), 4, FixColor.WHITE.alpha(anim));
		Stencil.read(1);
		semibold.get(16).draw(ms, rock.getModules().get(Interface.class).getEnglish().get() ? "Alert example" : "Пример уведомления", x + 26, y + 7, rock.getThemes().getTextFirstColor().alpha(anim));
		Stencil.finish();
		
		if (rock.getModules().get(Interface.class).getAlerts().getAdditions().get()) {
			Render.outline(ms, new Rect(x, y, width, height), anim);
		}
		
		ticks += 1 / Math.max((float) Minecraft.debugFPS, 5) * 75;
	}
	
	public void drawSmall(MatrixStack ms, AlertType type, String text, float x, float y, float width, float height, float anim) {
		Alerts alerts = rock.getModules().get(Interface.class).getAlerts();
		
		if (rock.getModules().get(Interface.class).getAlerts().getAdditions().get()) {
			Render.glow(ms, new Rect(x,y,width,height), anim);
		}
		Round.draw(ms, new Rect(x,y,width,height), 3, rock.getThemes().getSecondColor().alpha(anim * (alerts.getTransparent().get() ? 0.5f : 1)));
		
		drawSmallIcon(ms, type, x, y, anim);
		
		Stencil.init();
		Round.draw(ms, new Rect(x,y,width,height), 4, FixColor.WHITE.alpha(anim));
		Stencil.read(1);
		semibold.get(13).draw(ms, text, x + 16, y + 3f, rock.getThemes().getTextFirstColor().alpha(anim));
		Stencil.finish();
		if (rock.getModules().get(Interface.class).getAlerts().getAdditions().get()) {
			Render.outline(ms, new Rect(x, y, width, height), anim);
		}
		ticks += 1 / Math.max((float) Minecraft.debugFPS, 5) * 75;
	}
	
	public void drawBig(MatrixStack ms, AlertType type, String text, float x, float y, float width, float height, float anim) {
		Alerts alerts = rock.getModules().get(Interface.class).getAlerts();
		
		if (rock.getModules().get(Interface.class).getAlerts().getAdditions().get()) {
			Render.glow(ms, new Rect(x,y,width,height), anim);
		}
		Round.draw(ms, new Rect(x,y,width,height), 4, rock.getThemes().getFirstColor().alpha(anim * (alerts.getTransparent().get() ? 0.5f : 1)));
		
		drawBigIcon(ms, type, x, y, anim);
		
		Stencil.init();
		Round.draw(ms, new Rect(x,y,width,height), 4, FixColor.WHITE.alpha(anim));
		Stencil.read(1);
		semibold.get(16).draw(ms, text, x + 26, y + 7, rock.getThemes().getTextFirstColor().alpha(anim));
		Stencil.finish();
		
		if (rock.getModules().get(Interface.class).getAlerts().getAdditions().get()) {
			Render.outline(ms, new Rect(x, y, width, height), anim);
		}
		
		ticks += 1 / Math.max((float) Minecraft.debugFPS, 5) * 75;
	}
	
	public void drawSmallIcon(MatrixStack ms, AlertType type, float x, float y, float anim) {
		Modules alerts = rock.getModules();
		float alpha = (alerts != null && alerts.get(Interface.class).getAlerts().getTransparent().get() ? 0.5f : 1);
		
		switch (type) {
		case WAIT:
			float off = 1;
			Rect rect1 = new Rect(x+3, y+3.5f, 9, 9);
			rect1 = rect1.x(rect1.getX()+off).y(rect1.getY()+off).width(rect1.getWidth()-off*2).height(rect1.getHeight()-off*2);
			
			Outline.draw(ms, rect1, 3f, 1.2f, new FixColor(220, 220, 220).alpha(anim * (0.3f)));
			
			Stencil.init();
			GL11.glPushMatrix();
			GL11.glTranslated(x+3+4.5f, y+3.5f+4.5f, 0);
			GL11.glRotated(ticks*2, 0, 0, 1);
			Round.draw(ms, new Rect(3-4.5f,3-4.5f,9,9), 0, FixColor.WHITE);
			GL11.glPopMatrix();
			Stencil.read(0);
			Outline.draw(ms, rect1, 3, 1.2f, FixColor.WHITE.alpha(anim * alpha));
			Stencil.finish();
			break;
		case SUCCESS:
			Round.draw(ms, new Rect(x+3, y+3, 9,9), 4.5f, FixColor.GREEN.alpha(anim * alpha));
			Render.image("icons/yes.png", x+4.5f, y+5, 6, 6, FixColor.WHITE.alpha(anim * alpha));
			break;
		case ERROR:
			Round.draw(ms, new Rect(x+3, y+3, 9,9), 4.5f, FixColor.RED.alpha(anim * alpha));
			Render.image("icons/xmark.png", x+5.3f, y+5.5f, 4, 4, FixColor.WHITE.alpha(anim * alpha));
			break;
		case INFO:
			Round.draw(ms, new Rect(x+3, y+3, 9,9), 4.5f, FixColor.GREEN.alpha(anim * alpha));
			Render.image("icons/info.png", x+4.5f, y+5, 6, 6, FixColor.WHITE.alpha(anim * alpha));
			break;
		default:
			break;
		}
	}
	
	private void drawBigIcon(MatrixStack ms, AlertType type, float x, float y, float anim) {
		Alerts alerts = rock.getModules().get(Interface.class).getAlerts();
		
		switch (type) {
		case WAIT:
			float off = 1;
			Rect rect1 = new Rect(x+5, y+5.5f, 15, 15);
			rect1 = rect1.x(rect1.getX()+off).y(rect1.getY()+off).width(rect1.getWidth()-off*2).height(rect1.getHeight()-off*2);
			
			Outline.draw(ms, rect1, 7, 2.4f, new FixColor(220, 220, 220).alpha(anim * (0.3f)));
			
			Stencil.init();
			GL11.glPushMatrix();
			GL11.glTranslated(x+5+7.5f, y+5.5f+7.5f, 0);
			GL11.glRotated(ticks*2, 0, 0, 1);
			Round.draw(ms, new Rect(7-7.5f,7-7.5f,15,15), 0, FixColor.WHITE);
			GL11.glPopMatrix();
			Stencil.read(0);
			Outline.draw(ms, rect1, 7, 2.4f, FixColor.WHITE.alpha(anim * (alerts.getTransparent().get() ? 0.5f : 1)));
			Stencil.finish();
			break;
		case SUCCESS:
			Round.draw(ms, new Rect(x+5, y+5, 15,15), 7.5f, FixColor.GREEN.alpha(anim * (alerts.getTransparent().get() ? 0.5f : 1)));
			Render.image("icons/yes.png", x+7f, y+7.5f, 11,11, FixColor.WHITE.alpha(anim * (alerts.getTransparent().get() ? 0.5f : 1)));
			break;
		case ERROR:
			Round.draw(ms, new Rect(x+5, y+5, 15,15), 7.5f, FixColor.RED.alpha(anim * (alerts.getTransparent().get() ? 0.5f : 1)));
			Render.image("icons/xmark.png", x+8.5f, y+8.5f, 8,8, FixColor.WHITE.alpha(anim * (alerts.getTransparent().get() ? 0.5f : 1)));
			break;
		case INFO:
			Round.draw(ms, new Rect(x+5, y+5, 15,15), 7.5f, FixColor.GREEN.alpha(anim * (alerts.getTransparent().get() ? 0.5f : 1)));
			Render.image("icons/info.png", x+7f, y+7.5f, 11,11, FixColor.WHITE.alpha(anim * (alerts.getTransparent().get() ? 0.5f : 1)));
			break;
		default:
			break;
		}
	}

}
