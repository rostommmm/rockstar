package fun.rockstarity.api.render.globals.emotions.instance;

import fun.rockstarity.Rockstar;
import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.events.list.render.entity.EventRenderEntity;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.globals.emotions.instance.list.ConditionsEmotion;
import fun.rockstarity.client.modules.render.GlowESP;
import fun.rockstarity.client.modules.render.KillEffects;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.LivingEntity;

/**
 * @author ConeTin
 * @since 6 авг. 2024 г.
 */

public class Emotion implements IAccess {
	
	@Getter protected final LivingEntity player; // Игрок, воспроизводящий анимку
	@Getter protected final long time; // Время, которое будет проигрываться анимация
	@Getter protected final Animation emotionAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300); // Анимация эмоции
	protected boolean emotionPlaying; // Анимация запущена или нет
	protected final TimerUtility emotionTimer = new TimerUtility(); // Таймер исчезания анимаций
	private final TimerUtility timerUtility = new TimerUtility();
	
	public Emotion(LivingEntity player, long time) {
		this.player = player;
		this.time = time;
		this.emotionTimer.reset(); // Сбрасываем таймер
		this.emotionPlaying  = true;
		this.emotionAnim.setForward(true);
	}
	
	public Emotion(LivingEntity player) {
		this(player, 2500L);
	}
	
	public void onEvent(Event event) {
		if (event instanceof EventUpdate && this.emotionPlaying) {
			// Запускаем анимацию только через 500 мс, чтобы чел успел ее увидеть
			if (this.emotionTimer.passed(500L)) this.emotionAnim.setForward(true);
			
			if (this.emotionTimer.passed(this.time)) { // Задержка на прекращение анимации
				this.emotionAnim.setForward(false); // Останавливаем анимацию
				
				// TODO убирание треьтего лица и в целом починить конец анимации(допустим здвинг части тела)
				
				this.emotionPlaying = false;
			}
		}
		
		// Не рендерим предметы в руке пока играет анимка (ибо они хуево выглядят)
		if (event instanceof EventRenderEntity e && e.getEntity() == player && (!this.emotionAnim.finished(false) || this.emotionPlaying) && !(this instanceof ConditionsEmotion)) {
			GlowESP.SILENT_RENDERING = e.isPre();
		}
    }
	
	/**
	 * На будущее
	 */
	protected boolean canAnimateHands() {
		return !(player instanceof ClientPlayerEntity) || Minecraft.getInstance().getGameSettings().getPointOfView() != PointOfView.FIRST_PERSON;
	}
	
	// Метод для сдвигания части тела (мне лень прост)
	protected float moveProp(float originalOffset, float newOffset) {
		return MathUtility.interpolate(originalOffset, newOffset, this.emotionAnim.get());
	}
	
}
