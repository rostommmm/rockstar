package fun.rockstarity.client.modules.render;

import java.util.ArrayList;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.EventType;
import fun.rockstarity.api.events.list.game.EventKill;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.events.list.render.EventRender3D;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.globals.emotions.instance.ActionEmotion;
import fun.rockstarity.api.render.globals.emotions.instance.EmotionType;
import fun.rockstarity.api.render.particles.Particle3D;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.util.math.vector.Vector3d;

/**
 * @author ConeTin
 * @since 15 мар. 2024 г.
 */

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Info(name="KillEffects", desc="Красивый эффект при убийстве", type=Category.RENDER)
public class KillEffects extends Module {
	
	Mode mode = new Mode(this, "Режим");

	Mode.Element soul /* goodman */ = new Mode.Element(mode, "Душа");
	Mode.Element emotions = new Mode.Element(mode, "Эмоции");
	Mode.Element daynight = new Mode.Element(mode, "День-Ночь");
	
	Mode emotion = new Mode(this, "Эмоция").hide(() -> !mode.is(emotions));

	Mode.Element random = new Mode.Element(emotion, "Случайная");
	EmotionMode dep = new EmotionMode(emotion, EmotionType.DEB);
	EmotionMode masturbate = new EmotionMode(emotion, EmotionType.MASTURBATE);
	EmotionMode floss = new EmotionMode(emotion, EmotionType.FLOSS); // Типо фортнайт танец
	EmotionMode griddy = new EmotionMode(emotion, EmotionType.GET_GRIDDY); // Типо фортнайт танец #2
	
	@Getter
	CheckBox thirdPerson = new CheckBox(this, "3-е лицо").hide(() -> !mode.is(emotions));
	
	// Душа
	ArrayList<Particle3D> particles = new ArrayList<>();
	
	// Эмоции
	TimerUtility timerUtility = new TimerUtility();
	
	@Getter @NonFinal
	PointOfView previous; // Какой режим F5 был включен у чела до включения эффекта
	@NonFinal long time;
	@Getter 
	Animation dayNightAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	
	public KillEffects() {
		super(5);
	}
	
	@Override
	@EventType({EventKill.class, EventUpdate.class})
	public void onEvent(Event event) {
		if (mode.is(soul)) {
			this.drawSoulGoodman(event); // Душа
		} else if (mode.is(emotions)) {
			this.handleEmotions(event); // Эмоции
		} else if (mode.is(daynight)) {
			this.handleDayNight(event); // День-Ночь
		}
	}
	
	private void handleDayNight(Event event) {
		if (event instanceof EventKill e) {
			dayNightAnim.setEasing(Easing.EASE_OUT_BACK);
			dayNightAnim.setForward(true);
			dayNightAnim.setSpeed(3000);
			time = mc.world.getWorldInfo().getDayTime();
		}
		
		if (event instanceof EventRender3D && dayNightAnim.isForward()) {
			mc.world.setDayTime((long) (time + 24000F * dayNightAnim.get()));
			
			if (dayNightAnim.finished()) {
				dayNightAnim.setForward(false);
			}
		}
	}
	
	private void handleEmotions(Event event) {
		if (event instanceof EventKill e) {
			final boolean prev = thirdPerson.get();
			if (prev) previous = mc.getGameSettings().getPointOfView();
			EmotionMode emotionMode = emotion.is(random) ? (EmotionMode) emotion.getElements().get(MathUtility.randomInt(1, emotion.getElements().size()))
					: (EmotionMode) emotion.getCurrent();
			
			if (prev) mc.getGameSettings().setPointOfView(PointOfView.THIRD_PERSON_FRONT);
			rock.getEmotions().playEmotion(mc.player, emotionMode.type);
			ActionEmotion currentEmotion = new ActionEmotion(mc.player);
			time = currentEmotion.getTime();
			timerUtility.reset();
		}
		
		if (event instanceof EventUpdate && timerUtility.passed(time) && !timerUtility.passed(time+100)) {
			mc.getGameSettings().setPointOfView(previous);
		}
	}

	private void drawSoulGoodman(Event event) {
		if (event instanceof EventKill e) {
			/*
			for (int i = 0; i<360; i+=20) {
				Vector3d pos = e.getTarget().getPositionVec();
				double motionX = Math.sin(Math.toRadians(i))*0.1f;
				double motionZ = Math.cos(Math.toRadians(i))*0.1f;
				this.particles.add(new Particle3D(pos, new Vector3d(motionX, 0, motionZ)));
			}
			*/
			this.particles.add(new Particle3D(e.getTarget().getPositionVec(), new Vector3d(0, 0, 0))); // Спавним партикл
		}
		
		if (event instanceof EventRender3D e) {
			ArrayList<Particle3D> removing = new ArrayList<>();
			
			Render.startImageRendering("masks/glow.png");
			
			for (Particle3D particle : this.particles) {
				if (particle.getAlpha() < 0.3f) {
		    		particle.setMotion(mc.player.getPositionVec().subtract(particle.getPosition()).mul(0.05f / Math.max((float) Minecraft.debugFPS, 5) * 200,0.05f / Math.max((float) Minecraft.debugFPS, 5) * 200,0.05f / Math.max((float) Minecraft.debugFPS, 5) * 200));;
		    	}
				
				particle.render(e);
				particle.update();
				
				if (particle.getAlpha() < 0) removing.add(particle);
			}
			
			Render.finishImageRendering();
			
			this.particles.removeAll(removing);
		}
	}
	
	class EmotionMode extends Mode.Element {

		private EmotionType type;
		
		public EmotionMode(Mode parent, EmotionType type) {
			super(parent, type.getLocalized());
			this.type = type;
		}
		
	}
	
	@Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}
}
