package fun.rockstarity.client.modules.render;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.EventType;
import fun.rockstarity.api.events.list.player.EventJump;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventMove;
import fun.rockstarity.api.events.list.player.EventTrace;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.events.list.render.player.EventClampPitch;
import fun.rockstarity.api.events.list.render.player.EventModels;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import lombok.Getter;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.util.math.vector.Vector3f;

/**
 * @author ConeTin
 * @since 15 мар. 2024 г.
 */

@Info(name="FreeLook", desc="Позволяет смотреть по сторонам не поворачивая камеру", type=Category.RENDER)
public class FreeLook extends Module {
	
	private final CheckBox thirdPerson = new CheckBox(this, "3-е лицо").set(true); 
	private final CheckBox noClampPitch = new CheckBox(this, "Не ограничивать"); // Типо не ограничивать pitch чтобы можно было колобком
	private final CheckBox back = new CheckBox(this, "Возвращать назад").desc("Возвращать обратно исходную позицию прицела"); // Возвращать питч и яв обратно при выключении
	
	private PointOfView previous; // Какой режим F5 был включен у чела до включения FreeLook
	@Getter private Vector3f rotation; // Ротация до включения FreeLook
	
	@Override
	@EventType({EventUpdate.class, EventMotion.class, EventTrace.class, EventMove.class, EventJump.class, EventClampPitch.class, EventModels.class})
	public void onEvent(Event event) {
		if (previous == null) {
			this.previous = mc.getGameSettings().getPointOfView();
			this.rotation = new Vector3f(
					mc.player.rotationYaw, 
					mc.player.rotationPitch,
					mc.player.renderYawOffset
			);
		}
		
		// Если чекбокс "3-е лицо" включен переключаем на третее лицо
		if (event instanceof EventUpdate && thirdPerson.get()) mc.getGameSettings().setPointOfView(PointOfView.THIRD_PERSON_BACK);
		
		if (event instanceof EventMotion e) {
			// Заменяем пакеты, сообщая серверу что мы повёрнуты на this.rotation
			e.setYaw(this.rotation.x);
			e.setPitch(this.rotation.y);
			
			// Визуально отображаем поворот на this.rotation
			mc.player.rotationYawHead = this.rotation.x;
			mc.player.renderYawOffset = this.rotation.x;
		}
		
		if (event instanceof EventModels model) {
			if (model.getOwner() == mc.player) {
				model.bipedHead.rotateAngleX = this.rotation.y * ((float)Math.PI / 180F);
			    model.bipedHeadwear.rotateAngleX = model.bipedHead.rotateAngleX;
			    model.bipedHeadwear.rotateAngleY = model.bipedHead.rotateAngleY;
			}
		}
		
		if (event instanceof EventTrace e && this.rotation != null) { // "Поворачиваем" голову на this.rotation, чтобы при ударе в воздух ударялась то, что находится там
			e.setYaw(this.rotation.x);
			e.setPitch(this.rotation.y);
			e.cancel();
		}
		
		// Исправления движений, чтобы игрок двигался как если бы двигался, смотря на this.rotation
		if (event instanceof EventMove e) {
			e.setYaw(this.rotation.x);
			e.setPitch(this.rotation.y);
		}
		
		if (event instanceof EventJump e) {
			e.setYaw(this.rotation.x);
		}
		
		// Если чекбокс "Не ограничивать" включен, то отменяем ивент ограничивания
		if (event instanceof EventClampPitch e && this.noClampPitch.get()) e.cancel();
	}
	
	@Override
	public void onEnable() {
		// Записываем в переменную вид F5 + ротацию
		this.previous = mc.getGameSettings().getPointOfView();
		this.rotation = new Vector3f(
				mc.player.rotationYaw, 
				mc.player.rotationPitch,
				mc.player.renderYawOffset
		);
	}	
	
	@Override
	public void onDisable() {
		if (this.back.get() && this.rotation != null) { // Если чекбокс "Возвращать назад" включен, возвращаем ротацию на изначальную
			mc.player.rotationYaw = this.rotation.x;
			mc.player.rotationPitch = this.rotation.y;
		}
		
		// Меняем вид F5 на изначальный
		mc.getGameSettings().setPointOfView(this.previous);
	}
	
}
