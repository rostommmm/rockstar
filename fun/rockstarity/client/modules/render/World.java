package fun.rockstarity.client.modules.render;

import com.mojang.blaze3d.platform.GlStateManager;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.EventType;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.events.list.render.EventRender3D;
import fun.rockstarity.api.events.list.render.entity.EventRenderFog;
import fun.rockstarity.api.events.list.render.world.EventFogColor;
import fun.rockstarity.api.events.list.render.world.EventFogDistance;
import fun.rockstarity.api.events.list.render.world.EventPostUpdateRender;
import fun.rockstarity.api.events.list.render.world.EventPreUpdateRender;
import fun.rockstarity.api.events.list.render.world.EventSkybox;
import fun.rockstarity.api.events.list.render.world.EventStarsColor;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.helpers.system.ThreadManager;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.ColorPicker;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Mode.Element;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.shaders.Shader;
import fun.rockstarity.api.render.shaders.fog.ColorWheel;
import fun.rockstarity.api.render.shaders.fog.CustomFramebuffer;
import fun.rockstarity.api.render.shaders.fog.depth.DepthRenderer;
import fun.rockstarity.api.render.shaders.list.WhiteCharm;
import fun.rockstarity.api.render.ui.alerts.Alert;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import fun.rockstarity.api.secure.nativeapi.NativeHelper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.network.play.server.SUpdateTimePacket;
import net.optifine.CustomSky;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;

/**
 * @author Malecharik
 * @since 15 Mar 2024 23:34:59
 */


@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Info(name="World", desc="Изменение внешнего вида мира", type=Category.RENDER, module = {"Fog", "FullBright", "NightMode", "Ambience"})
public class World extends Module {
	
	@NonFinal WhiteCharm white;
    @NonFinal Framebuffer framebuffer;
    
	Mode skybox = new Mode(this, "Небо").hide(() -> CustomSky.worldSkyLayers == null);
	Mode.Element classic = new Mode.Element(skybox, "Обычное");
	Mode.Element a1 = new Mode.Element(skybox, "Ели");
	Mode.Element a2 = new Mode.Element(skybox, "Руины");
	Mode.Element a3 = new Mode.Element(skybox, "Яркие тучи");
	Mode.Element a4 = new Mode.Element(skybox, "Озеро");
	Mode.Element a5 = new Mode.Element(skybox, "Ночные облака");
	Mode.Element a6 = new Mode.Element(skybox, "Облачный космос");
	Mode.Element a7 = new Mode.Element(skybox, "Ясная ночь");
	Mode.Element a8 = new Mode.Element(skybox, "Ясный вечер");

	CheckBox timeChange = new CheckBox(this, "Изменение времени");
	Mode timeMode = new Mode(timeChange, "Режим");
	Mode.Element eveMorning = new Mode.Element(timeMode, "Раннее утро");
	Mode.Element morning = new Mode.Element(timeMode, "Утро");
	Mode.Element day = new Mode.Element(timeMode, "День");
	Mode.Element evening = new Mode.Element(timeMode, "Вечер");
	Mode.Element eveEvening = new Mode.Element(timeMode, "Поздний вечер");
	Mode.Element nightMode = new Mode.Element(timeMode, "Ночь");
	Mode.Element realTime = new Mode.Element(timeMode, "Текущее время");
	Mode.Element custom = new Mode.Element(timeMode, "Кастом");
	Slider time = new Slider(timeChange, "Время").min(0f).max(24000f).set(13000f).inc(1000f).hide(() -> !timeMode.is(custom));

	public CheckBox rezSky = new CheckBox(this, "Изменение неба");
	CheckBox rezStars = new CheckBox(this, "Добавление звезд");
	ColorPicker skyColor = new ColorPicker(rezSky, "Цвет неба").add(FixColor.WHITE);
	ColorPicker starsColor = new ColorPicker(rezStars, "Цвет звёзд").add(FixColor.WHITE);

	CheckBox fog = new CheckBox(this, "Туман").ifEnabled(true);
	
	Mode mode = new Mode(fog, "Режим тумана");
	Mode.Element fogDefault = new Mode.Element(mode, "Обычный");
	Mode.Element fogBeat = new Mode.Element(mode, "Улучшенный");
	Mode.Element fogBlur = new Mode.Element(mode, "Размытие");
	@Getter ColorPicker fogColor = new ColorPicker(fog, "Цвет тумана").hide(() -> mode.is(fogBlur));
    Slider distance = new Slider(fog, "Дальность тумана").min(0).max(0.5f).inc(0.01f).set(0.2f).hide(() -> !mode.is(fogBlur));
    Slider saturation = new Slider(fog, "Насыщенность тумана").min(0.05f).max(0.95f).inc(0.05f).set(0.25F).hide(() -> !mode.is(fogBlur));
	Slider fogFrom = new Slider(fog, "Туман от").min(1).max(200).inc(5).set(10).hide(() -> !mode.is(fogBeat));
	Slider fogTo = new Slider(fog, "Туман до").min(1).max(200).inc(5).set(50).hide(() -> !mode.is(fogBeat));
	CheckBox gamma = new CheckBox(this, "Включить яркость").onDisable(() -> mc.getGameSettings().setGamma(0.35f));
	CheckBox dynamic = new CheckBox(this, "Динамическая яркость").hide(() -> !this.gamma.get()).onEnable(() -> {
		if (mc.getGameSettings().getGamma() > 1)
			mc.getGameSettings().setGamma(0.35f);
	}).desc("Игрок будет излучать свечение");
	@Getter CheckBox night = new CheckBox(this, "Ночной режим").hide(this.gamma::get);

	CheckBox lightStars = new CheckBox(this, "Яркие звезды");
	Slider depth = new Slider(this, "Глубина").min(1f).max(10f).inc(0.5f).set(4f).hide(() -> !this.night.get());
	ColorWheel colorWheel = new ColorWheel();
	@NonFinal CustomFramebuffer buffer;
	
	Mode modeWeather = new Mode(this, "Погода");
	
	Mode.Element no = new Mode.Element(modeWeather, "Ясно");
	Element rain = new Element(modeWeather, "Дождь");
	Element thunder = new Element(modeWeather, "Гроза");
	
	@NonFinal int prev;
	@NonFinal boolean loading;
	
	public World() {
	}
	
	@Override
	@EventType({EventRender2D.class, EventPreUpdateRender.class, EventPostUpdateRender.class, EventUpdate.class, EventRender3D.class, EventReceivePacket.class, EventFogDistance.class, EventRender3D.class})
	public void onEvent(Event event) {
		if (event instanceof EventUpdate) {
			if (white == null) {
				rock.getDepthShader().initialize();
				white = new WhiteCharm();
				DepthRenderer.INSTANCE.init();
				framebuffer = new Framebuffer(1, 1, false);
				buffer = new CustomFramebuffer(true);
			}
		}
		
		if (white == null) return;
		
		if (event instanceof EventSkybox e && !classic.get() && !loading) {
			int index = skybox.getElements().indexOf(skybox.getCurrent());
			if (prev != index) {
				loading = true;
				ThreadManager.run(() -> {
					Alert alert = rock.getAlertHandler().alert("Загружаю скайбокс", AlertType.WAIT);
					NativeHelper.getImageResource("masks/sky/" + index + ".png");
					alert.hide();
					loading = false;
				});
			} else {
				e.setTexture(NativeHelper.getImageResource("masks/sky/" + index + ".png"));
				e.cancel();;
			}
			prev = index;
		}
		
		/*
		if (event instanceof EventRender2D e && rock.getClickGui() != null) {
    		e.getMatrixStack().push();
    		e.getMatrixStack().translate(0, 500, 0);
    		//e.getMatrixStack().rotate(Vector3f.ZP.rotationDegrees(180));
    		
    		rock.getClickGui().getWindow().getRenderer().render(e.getMatrixStack(), 0, 0, e.getPartialTicks());
    		
    		e.getMatrixStack().pop();
		}
		*/
		
		if (Player.isInGame()) {
			if (modeWeather.is(no)) {
				mc.world.setRainStrength(0);
				mc.world.setThunderStrength(0);
			} else if (modeWeather.is(rain)) {
				mc.world.setRainStrength(1);
			} else {
				mc.world.setThunderStrength(1);
			}
		}
		
		if (this.mode.is(fogBlur) && fog.get()) {
			if (event instanceof EventRender2D e) {
				colorWheel.update();
			}
			
			if (event instanceof EventPreUpdateRender e) {
	    		buffer.setup();
	            this.mc.worldRenderer.updateCameraAndRender(
	                    e.getMatrixStackIn(),
	                    e.getPartialTicks(),
	                    e.getFinishTimeNano(),
	                    e.isDrawBlockOutline(),
	                    e.getActiverenderinfo(),
	                    e.getGameRenderer(),
	                    e.getLightmapTexture(),
	                    e.getMatrix4f(),
	                    true
	            );
	            buffer.stop();
	    	}
	    	
	    	if (event instanceof EventPostUpdateRender) {
	    		DepthRenderer.INSTANCE.render3D();
	    	}
		}
		
		if (event instanceof EventUpdate) {
			//mc.world.setRainStrength(1);
			if (this.gamma.get() && !this.dynamic.get()) mc.getGameSettings().setGamma(30);
		}
		
		if (event instanceof EventStarsColor e) {
			e.setColor(starsColor.get());
			e.cancel();
		}
		
		if (Player.isInGame()) if (timeChange.get()) handleTimeForChange();
		
		if (event instanceof EventReceivePacket e) {
			if (e.getPacket() instanceof SUpdateTimePacket) e.cancel();
		}
		
		if (event instanceof EventRenderFog e && fog.get()) {
			//e.cancel();
		}
		
		if (event instanceof EventFogColor e && this.mode.is(this.fogBeat) && fog.get()) {
			e.setColor(fogColor.get());
		}
		
		if (event instanceof EventFogDistance e && this.mode.is(this.fogBeat) && fog.get()) {
			e.setInner(Math.min(fogFrom.get(), fogTo.get()));
			e.setOuter(Math.max(fogFrom.get(), fogTo.get()+1));
		}
		
		/*
		if (event instanceof EventRenderWorld e) {
			
			framebuffer = Shader.createFrameBuffer(framebuffer);
			framebuffer.framebufferClear();
			framebuffer.bindFramebuffer(true);
			
			framebuffer.unbindFramebuffer();
			mc.getFramebuffer().bindFramebuffer(true);
			
			
			GL11.glPushMatrix();
			
			double x = mc.player.lastTickPosX + (mc.player.getPosX() - mc.player.lastTickPosX) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getX();
			double y = mc.player.lastTickPosY + (mc.player.getPosY() - mc.player.lastTickPosY) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getY();
			double z = mc.player.lastTickPosZ + (mc.player.getPosZ() - mc.player.lastTickPosZ) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getZ();
			GL11.glTranslated(x, y, z);
			
			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GlStateManager.enableDepthTest();
			GL11.glShadeModel(GL11.GL_SMOOTH);
			
			GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			for (int i = 0; i <= 360; i++) {
				double cos = Math.cos(Math.toRadians(i)) * 10;
				double sin = Math.sin(Math.toRadians(i)) * 10;
				GL11.glVertex3d(cos, 10, sin);
				GL11.glVertex3d(cos, -10, sin);
			}
			GL11.glEnd();
			
			GL11.glPopMatrix();
		}
		
		if (event instanceof EventRender2D e) {
			if (framebuffer != null) {
				mc.getFramebuffer().bindFramebuffer(true);
				Stencil.init();
				Render.bindTexture(framebuffer.framebufferTexture);
                Shader.drawQuads(e.getMatrixStack());
                Stencil.read(1);
                Round.draw(e.getMatrixStack(), new Rect(0, 0, sr.getScaledWidth(), sr.getScaledHeight()), 0, FixColor.RED);
                Stencil.finish();
                mc.getFramebuffer().bindFramebuffer(false);
	            
	            GlStateManager.disableAlphaTest();
			}
		}*/
		
	}
	
    private void renderColor(float force, EventRender2D e) {
	    GlStateManager.enableBlend();
	    OpenGlHelper.glBlendFunc(770, 771, 1, 0);
	    framebuffer = white.createFrameBuffer(framebuffer);
	
	    framebuffer.framebufferClear();
	    framebuffer.bindFramebuffer(true);
	    white.start();
	    setupUniforms2(force);
	    Render.bindTexture((mc.getFramebuffer()).framebufferTexture);
	
	    Shader.drawQuads(e.getMatrixStack());
	    framebuffer.unbindFramebuffer();
	    white.finish();
	
	    mc.getFramebuffer().bindFramebuffer(true);
	    white.start();
	    setupUniforms2(force);
	    Render.bindTexture(framebuffer.framebufferTexture);
	    Shader.drawQuads(e.getMatrixStack());
	    white.finish();
	
	    Render.resetColor();
	    GlStateManager.bindTexture(0);
    }
    
    private void setupUniforms2(float force) {
    	white.setInt("textureIn", 0);
	    white.setFloat("force", force);
    }

	private void handleTimeForChange() {
		if (mc.world != null) {
			Animation dayNight = rock.getModules().get(KillEffects.class).getDayNightAnim();

			if (timeMode.is(custom))
				mc.world.setDayTime((long) (this.time.get() + (dayNight.isForward() ? 24000F * dayNight.get() : 0)));

			if (timeMode.is(realTime)) {
				LocalTime localTime = LocalTime.now(ZoneId.systemDefault());
				int localHour = localTime.getHour();
				int localMinute = localTime.getMinute();
				long time = (long) (localHour * 1000) + (long) ((double) localMinute * 16.666666666666668);
				mc.world.setDayTime(time);
			}

			if (timeMode.is(eveMorning)) mc.world.setDayTime(6000);
			if (timeMode.is(morning)) mc.world.setDayTime(10000);
			if (timeMode.is(day)) mc.world.setDayTime(14000);
			if (timeMode.is(evening)) mc.world.setDayTime(17000);
			if (timeMode.is(eveEvening)) mc.world.setDayTime(19000);
			if (timeMode.is(nightMode)) mc.world.setDayTime(22000);
		}
	}
    
    @Override
	public void onDisable() {
    	if (gamma.get()) mc.getGameSettings().setGamma(0.35f);
		mc.world.setRainStrength(0);
		mc.world.setThunderStrength(0);
	}

	@Override
	public void onEnable() {
		
	}
	
}
