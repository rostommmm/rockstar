package fun.rockstarity.api.render.ui.alerts;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.events.list.game.client.EventAlert;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.secure.Debugger;
import fun.rockstarity.api.sounds.Sound;
import fun.rockstarity.client.modules.other.Sounds;
import fun.rockstarity.client.modules.render.Interface;
import lombok.Getter;

/**
 * @author ConeTin
 * @since 9 дек. 2023 г.
 */

public class AlertHandler implements IAccess {
	
	@Getter
	private final ArrayList<Alert> alerts = new ArrayList<>();
	private static final TimerUtility timer = new TimerUtility();

	public void render(MatrixStack matrixStack) {
		try {
			alerts.stream().forEach(alert -> {
				alert.render(matrixStack);
			});
			
			alerts.removeIf(alert -> alert.getShow().finished(false));
		} catch (ConcurrentModificationException e) {
			Debugger.print(e);
		}
	}
	
	public void renderBackground(MatrixStack matrixStack) {
		try {
			alerts.stream().forEach(alert -> {
				alert.renderBackground(matrixStack);
			});
			
			alerts.removeIf(alert -> alert.getShow().finished(false));
		} catch (ConcurrentModificationException e) {
			Debugger.print(e);
		}
	}
	
	/**
	 * Метод для вывода уведомления
	 * @param text - Текст уведомления
	 * @param type - Тип уведомления
	 * @return Возвращает это же уведомление
	 */
	public Alert alert(String text, AlertType type) {
		if (text != null && text.isEmpty()) return null;
		
		new EventAlert(text, type.getName()).hook();
		
		float height = 25;
		Alert alert = new Alert(text, type, 0, 0, 25, height);
		if (rock.getModules().get(Interface.class).getAlerts().get())
			alerts.add(alert);
		
		Sounds sounds = rock.getModules().get(Sounds.class);
		
		if (sounds.get() && sounds.getModule().get() && timer.passed(50)) {
			switch (type) {
			case SUCCESS:
				if (sounds.get()) new Sound("on" + sounds.getModifier()).play();
				break;
			case ERROR:
				if (sounds.get()) new Sound("off" + sounds.getModifier()).play();
				break;
			case INFO:
				if (sounds.get()) new Sound("info" + sounds.getModifier()).play();
				break;
			default:
				break;
			}
			timer.reset();
		}
		
		return alert;
	}
	
}
