package fun.rockstarity.client.modules.render;

import fun.rockstarity.api.modules.settings.list.*;
import it.unimi.dsi.fastutil.booleans.BooleanSet;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.EventType;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.events.list.render.entity.EventRenderItem;
import fun.rockstarity.api.events.list.render.player.EventRenderHand;
import fun.rockstarity.api.events.list.render.world.EventViewModel;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.helpers.render.Stencil;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.render.shaders.Shader;
import fun.rockstarity.api.render.shaders.list.Blur;
import fun.rockstarity.api.render.shaders.list.Glass;
import fun.rockstarity.api.render.shaders.list.Reversed;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.client.modules.combat.Aura;
import lombok.Getter;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3f;

/**
 * @author ConeTin
 * @since 7 дек. 2023 г.
 */


@Info(name="Hand", desc="Изменение внешнего вида руки", type=Category.RENDER, module= {"Swing", "Animation"})
public class Hand extends Module {
	
	private final Position left = new Position(this, "Левая рука");
	private final Position right = new Position(this, "Правая рука");
	
	private final Slider leftSize = new Slider(this, "Размер левой руки").min(0.1f).max(1.5f).inc(0.025f).set(1).text(1, "Обыч.");
	private final Slider rightSize = new Slider(this, "Размер правой руки").min(0.1f).max(1.5f).inc(0.025f).set(1).text(1, "Обыч.");
	
	@Getter
	private final Slider speed = new Slider(this, "Плавность").min(1).max(20).inc(1f).set(11);
	
	private final Mode mode = new Mode(this, "Анимация");
	
	private final Mode.Element defaultAnim = new Mode.Element(mode, "Обычная");
	private final Mode.Element touch = new Mode.Element(mode, "Касание");
	private final Mode.Element slant = new Mode.Element(mode, "Наклон");
	private final Mode.Element blocking = new Mode.Element(mode, "Блок");
	private final Mode.Element tap = new Mode.Element(mode, "Тап");
	private final Mode.Element glide = new Mode.Element(mode, "Гладкий");
	private final Mode.Element forward = new Mode.Element(mode, "Вперед");
	private final Mode.Element bonk = new Mode.Element(mode, "Боньк");
	private final Mode.Element self = new Mode.Element(mode, "На себя");
	
	@Getter
	private final CheckBox onlyAura = new CheckBox(this, "Только с Aura");
	
	private final Mode func = new Mode(this, "Функция анимации").hide(() -> mode.is(defaultAnim));
	
	private final Mode.Element linear = new Mode.Element(func, "Линейная");
	private final Mode.Element circ = new Mode.Element(func, "Плавная");
	private final Mode.Element back = new Mode.Element(func, "Возвращение");
	private final Mode.Element elastic = new Mode.Element(func, "Эластичная");
	
	private final CheckBox imitation = new CheckBox(this, "Имитация 1.8");
	
	@Getter private final CheckBox shaderHand = new CheckBox(this, "Использовать шейдер").desc("Делает ваши руки более красивыми");
	@Getter private final CheckBox item360 = new CheckBox(this, "Вращение на 360").desc("Вращает предметы в руке на 360");
	@Getter private final Mode select = new Mode(this, "По..").hide(() -> !item360.get());
	@Getter private final Mode.Element vertical = new Mode.Element(select, "Вертикали");
	@Getter private final Mode.Element gorizont = new Mode.Element(select, "Горизонтали");
	@Getter private final Select selectedHand = new Select(item360, "Выбор руки");
	@Getter private final Select.Element rightHAND = new Select.Element(selectedHand, "Правая");
	@Getter private final Select.Element leftHAND = new Select.Element(selectedHand, "Левая");
	@Getter private final Slider speed360 = new Slider(item360, "Скорость").min(1).max(4).inc(0.1f).set(2).desc("Чем больше скорость в слайдере - тем медленнее двигаются руки.");
	@Getter private final CheckBox lock = new CheckBox(this, "Заблокировать").desc("Блокирует движение рук при поворотах, убирает плавность в общем");
	
	@Getter
	private final TimerUtility clickTimer = new TimerUtility(),
			hitTimer = new TimerUtility();
	
	private Framebuffer framebuffer;
	private final MatrixStack matrices = new MatrixStack();
	
	@Override
	@EventType({EventRenderItem.class, EventViewModel.class})
	public void onEvent(Event event) {
		if (shaderHand.get()) {
			if (event instanceof EventRenderHand hand) {
				if (hand.isPre()) {
					framebuffer = Shader.createFrameBuffer(framebuffer);
					framebuffer.framebufferClear();
					framebuffer.bindFramebuffer(true);
				} else {
					framebuffer.unbindFramebuffer();
					mc.getFramebuffer().bindFramebuffer(true);
				}
			}
			
			if (event instanceof EventRender2D e) {
				/*
				Blur.stencilFramebuffer = Shader.createFrameBuffer(Blur.stencilFramebuffer);
				Blur.stencilFramebuffer.framebufferClear();
				Blur.stencilFramebuffer.bindFramebuffer(false);
				
				Blur.stencilFramebuffer.unbindFramebuffer();
	            Blur.renderBlur(e.getMatrixStack(), Blur.stencilFramebuffer.framebufferTexture, 3, 1);
	            */
	            
				if (framebuffer != null) {
			        GL11.glPushMatrix();
	                GlStateManager.enableAlphaTest();
	                GlStateManager.alphaFunc(516, 0.0f);
	                GlStateManager.enableBlend();
	                OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

					mc.getFramebuffer().bindFramebuffer(true);
					
					Stencil.init();
					Render.bindTexture(framebuffer.framebufferTexture);
					Shader.drawQuads();
					Stencil.read(1);
					if (Player.collisionPredict(mc.player.getPositionVec())) {
						/*// glass
						Glass.draw();
						Shader.drawQuads();
						Glass.end();
						*/
						Reversed.render(Style.getMain());
						
						Blur.stencilFramebuffer = Shader.createFrameBuffer(Blur.stencilFramebuffer);

						Blur.stencilFramebuffer.framebufferClear();
						Blur.stencilFramebuffer.bindFramebuffer(false);
						Round.draw(e.getMatrixStack(), new Rect(0,0,sr.getScaledWidth(), sr.getScaledHeight()), 0, FixColor.WHITE);
			            Blur.stencilFramebuffer.unbindFramebuffer();
			            Blur.renderBlur(e.getMatrixStack(), Blur.stencilFramebuffer.framebufferTexture, 3, 1);
					}
					Stencil.finish();
					
		            mc.getFramebuffer().bindFramebuffer(false);
		            
		            GlStateManager.disableAlphaTest();
	                GL11.glPopMatrix();
				}
			}
		}
		
		if (event instanceof EventRenderItem e) {
			float x = e.getProgress();
			float progress = x;
			
			if (func.is(back)) {
				float c1 = 2;
				float c3 = c1 + 1;

				progress = c3 * x * x * x - c1 * x * x;
			} else if (func.is(elastic)) {
				float c4 = (float) ((2 * Math.PI) / 3);
				progress = (float) (x == 0
						  ? 0
								  : x == 1
								  ? 1
								  : -Math.pow(2, 9 * x - 9) * Math.sin((x * 5 - 5.75) * c4));
			} else if (func.is(circ)) {
				progress = (float) (1 - Math.sqrt(1 - Math.pow(x, 2)));
			}
			
			if (!hitTimer.passed(900) && this.imitation.get()) {
				float time = (clickTimer.getElapsed() / 300f) * 2f;

				progress = time > 1 ? 2 - time : time;
				if (clickTimer.passed(300)) {
					clickTimer.reset();
				}
			}
			
			MatrixStack stack = e.getMatrixStack();
			//Если чекбокс только с аурой включен и в ауре не найден таргет повторяем код
			if (this.onlyAura.get() && rock.getModules().get(Aura.class).getTarget() == null) return;
			// Проверям что рука правая
			if (e.isRight() && !mc.player.getHeldItemMainhand().isEmpty()) {
				if (mode.is(touch)) {
    				stack.rotate(Vector3f.XP.rotationDegrees(-90));
					stack.rotate(Vector3f.ZP.rotationDegrees(90));
					
					stack.rotate(Vector3f.XP.rotationDegrees(90 * -0.015F));
					stack.rotate(Vector3f.YP.rotationDegrees(90));
					
					stack.translate(-0.3, 0, 1.3f);

					stack.translate(0, 0, 0.2 * progress);
					stack.rotate(Vector3f.XP.rotationDegrees(progress * 15.0f));
				} else if (mode.is(slant)) {
					float rotate = 35;
					
					stack.translate(0, 0, -0.3 * progress);
					stack.rotate(Vector3f.XP.rotationDegrees(progress * -rotate));
					stack.rotate(Vector3f.ZP.rotationDegrees(progress * rotate));
				} else if (this.mode.is(this.blocking)) {
					stack.rotate(Vector3f.XP.rotationDegrees(-90));
					stack.rotate(Vector3f.ZP.rotationDegrees(90));
					
					stack.rotate(Vector3f.YP.rotationDegrees(40));
					stack.rotate(Vector3f.ZP.rotationDegrees(-20));
					
					stack.translate(-0.0, 0.3f, 0.9f);

					stack.translate(0, 0.6 * progress, -0.6 * progress);
					stack.rotate(Vector3f.XP.rotationDegrees(progress * -45.0f));
				} else if (this.mode.is(this.tap)) {
					stack.rotate(Vector3f.XP.rotationDegrees(-60));
					stack.rotate(Vector3f.ZP.rotationDegrees(60));
					
					stack.rotate(Vector3f.XP.rotationDegrees(60 * -0.015F));
					stack.rotate(Vector3f.YP.rotationDegrees(60));
					
					stack.translate(0.6, 0, 1.f);

					stack.translate(0.4 * progress, 0, 0);
				} else if (this.mode.is(this.bonk)) {
					//stack.translate(0, 0.3, 0);
					//stack.rotate(Vector3f.ZN.rotationDegrees(45));

					stack.translate(0, 0.5f * progress, -1f * progress);
					stack.rotate(Vector3f.XN.rotationDegrees(progress * 75.0f));
				} else if (this.mode.is(this.glide)) {
					stack.rotate(Vector3f.XP.rotationDegrees(-90));
					stack.rotate(Vector3f.ZP.rotationDegrees(90));
					
					stack.rotate(Vector3f.XP.rotationDegrees(90 * -0.015F));
					stack.rotate(Vector3f.YP.rotationDegrees(90));
					
					stack.rotate(Vector3f.XP.rotationDegrees(50));
					stack.rotate(Vector3f.YP.rotationDegrees(-30));
					
					stack.translate(0.2, 0.5f, 1.3f);

					stack.translate(0,0.7f * progress + 0.2f,-0.7f * progress);
					stack.rotate(Vector3f.XP.rotationDegrees(progress * -70));
				} else if (this.mode.is(this.self/* на себя */)) {
					/*
					stack.rotate(Vector3f.XP.rotationDegrees(-90));
					stack.rotate(Vector3f.ZP.rotationDegrees(90));
					
					stack.rotate(Vector3f.XP.rotationDegrees(90 * -0.015F));
					stack.rotate(Vector3f.YP.rotationDegrees(90));
					
					stack.rotate(Vector3f.XP.rotationDegrees(50));
					stack.rotate(Vector3f.YP.rotationDegrees(-30));
					
					stack.translate(0.2, 0.5f, 1.3f);

					stack.translate(0,0.4f * progress + 0.2f,-0.5f * progress);
					stack.rotate(Vector3f.XP.rotationDegrees(progress * -45));
					*/
					
					stack.rotate(Vector3f.XP.rotationDegrees(-90));
					stack.rotate(Vector3f.ZP.rotationDegrees(90));
					
					stack.rotate(Vector3f.XP.rotationDegrees(90 * -0.015F));
					stack.rotate(Vector3f.YP.rotationDegrees(90));
					
					stack.rotate(Vector3f.YP.rotationDegrees(-30));
					
					stack.translate(0.2, -0.2f, 1.3f);
					
					//stack.translate(0, 0.5f * progress, -0.8f * progress);

					//stack.translate(0, 0.35f * progress, -0.7f * progress);
					
					stack.rotate(Vector3f.XP.rotationDegrees(progress * -45.0f));
					
					stack.translate(0, 0.7f * progress, -0.35f * progress);
				} else if (this.mode.is(this.forward)) {
					stack.rotate(Vector3f.XP.rotationDegrees(-90));
					stack.rotate(Vector3f.ZP.rotationDegrees(90));
					
					stack.rotate(Vector3f.XP.rotationDegrees(90 * -0.015F));
					stack.rotate(Vector3f.YP.rotationDegrees(90));
					
					stack.rotate(Vector3f.XP.rotationDegrees(50));
					stack.rotate(Vector3f.YP.rotationDegrees(-30));
					
					stack.translate(0.2, 0.5f, 1.3f);

					stack.translate(0.9f * progress, 0.2f + 0.35 * progress, 0.3 * progress);
					stack.rotate(Vector3f.YP.rotationDegrees(progress * 50));
					stack.rotate(Vector3f.ZP.rotationDegrees(progress * -20));
				}
				
				if (!mode.is(defaultAnim))
					event.cancel();
			}
		}
		
		if (event instanceof EventViewModel e) {
			MatrixStack stack = e.getMatrixStack();
			float rightSize = this.rightSize.get()-1;
			float leftSize = this.leftSize.get()-1;
			
			
			if (e.isRight()) 
				stack.translate(right.getX() - rightSize, -right.getY() + rightSize/2, rightSize);
        	else
        		stack.translate(left.getX() + leftSize, -left.getY() + leftSize/2, leftSize);
		}
	}
	
	@Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}
}
