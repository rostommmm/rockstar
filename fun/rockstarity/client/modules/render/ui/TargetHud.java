package fun.rockstarity.client.modules.render.ui;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Stream;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventKill;
import fun.rockstarity.api.events.list.game.EventTotemBreak;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.Hover;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.render.PositionTracker;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.helpers.render.Stencil;
import fun.rockstarity.api.helpers.system.TextUtility;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.animation.infinity.InfinityAnimation;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.render.particles.ParticleEngine;
import fun.rockstarity.api.render.shaders.list.Glow;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.client.modules.combat.AimAssist;
import fun.rockstarity.client.modules.combat.AimBot;
import fun.rockstarity.client.modules.combat.Aura;
import fun.rockstarity.client.modules.combat.Aura;
import fun.rockstarity.client.modules.other.NameProtect;
import fun.rockstarity.client.modules.render.Cosmetics;
import fun.rockstarity.client.modules.render.Interface;
import fun.rockstarity.client.modules.render.Interface.UIElement;
import fun.rockstarity.client.modules.render.TargetESP;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author ConeTin
 * @since 19 мар. 2024 г.
 */

public class TargetHud extends UIElement {
	
	private final Animation size = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	private final Animation showing = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	private final Animation waiting = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(800);
	
	private final Animation hoverNickAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	private final Animation copiedAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	private final Animation healthHide = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	private final Animation goldenHide = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);

	private final Animation killing = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(500);
	private final Animation killingWait = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(1000);
	
	private final Animation hurt = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);

	private final CheckBox magnet = new CheckBox(this, "Привязка к цели").onEnable(() -> {
		TargetESP targetESP = rock.getModules().get(TargetESP.class);
		if (targetESP.get()) {
			targetESP.set(false);
			rock.getAlertHandler().alert("TargetESP выключен", AlertType.INFO);
		}
	});
	
	private final CheckBox particles = new CheckBox(this, "Частицы").set(true).hide(() -> magnet.get());
	private final InfinityAnimation unitAnim = new InfinityAnimation();
	private final InfinityAnimation healthAnim = new InfinityAnimation();
	private final InfinityAnimation gappleAnim = new InfinityAnimation();
	private final InfinityAnimation silentHealthAnim = new InfinityAnimation();
	
	private final CheckBox additions = new CheckBox(this, "Обводка/Свечение").set(true).hide(() -> !Interface.outline() && !Interface.glow());
	
	private final Mode mode = new Mode(this, "Режим здоровья");
	private final Mode.Element circle = new Mode.Element(mode, "Круг");
	private final Mode.Element circle2 = new Mode.Element(mode, "Круг 2");
	private final Mode.Element bar = new Mode.Element(mode, "Полоска");
	private final Mode.Element none = new Mode.Element(mode, "Ничего");
	
	private final Mode healthMode = new Mode(this, "Отображение здоровья в..").hide(() -> circle.get() || none.get());
	private final Mode.Element percents = new Mode.Element(healthMode, "Процентах").set();
	private final Mode.Element value = new Mode.Element(healthMode, "Единицах");
	
	private final CheckBox rayTrace = new CheckBox(this, "Показывать при наводке");
	private final CheckBox copy = new CheckBox(this, "Копирование никнейма").set(true);
	
	private final CheckBox healthcolor = new CheckBox(this, "Цвет от хп");
	
	private ParticleEngine particleEngine = new ParticleEngine();
	@Getter
	private LivingEntity target, lastTarget;
	
	private boolean killed;
	@Setter
	private int mouseX, mouseY;
	
	private Rect nickname;
	private boolean copied;
	
	private float healthRw = -1;
	@NativeInclude
	public TargetHud(Interface ui, Select select) {
		super(select, "Цель", new Rect(200,200,0,0));
		
		this.set(true);
	}
	
	public void onEvent(Event event) {
		if (event instanceof EventRender2D e) 
			renderTargetHud(e);
		
		if (event instanceof EventKill e && e.getTarget() == this.target)
			this.killed = true;
		
		if (event instanceof EventTotemBreak e && e.getEntity() == this.target) 
			this.killed = true;
		
		super.onEvent(event);
	}
	
	private void renderTargetHud(EventRender2D event) {
		MatrixStack ms = event.getMatrixStack();
		
		if (!(mc.currentScreen instanceof ChatScreen)) {
			this.mouseX = 0;
			this.mouseY = 0;
		}

		if (nickname != null && !Hover.isHovered(nickname, mouseX, mouseY)) {
			this.copied = false;
		}
		
		float width = mode.is(circle) || mode.is(none) ? 112 : 138;
		float height = 38;
		
		float x = this.draggable.getX(), y = this.draggable.getY();
		
		Aura aura = rock.getModules().get(Aura.class);
		target = Stream.of(
        		rock.getModules().get(Aura.class).getTarget(),
        		rock.getModules().get(AimAssist.class).getTarget(), 
        		rock.getModules().get(AimBot.class).getTarget()
        ).filter(Objects::nonNull).findFirst().orElse(null);
		
		if (rock.getModules().containsKey(Aura.class) && target == null) {
			target = rock.getModules().get(Aura.class).getTarget();
		}
        
		
		RayTraceResult traceResult = mc.objectMouseOver;
		
		if (rayTrace.get() && target == null && traceResult != null && traceResult.getType() == RayTraceResult.Type.ENTITY) {
			Entity entity = ((EntityRayTraceResult) traceResult).getEntity();
			
			if (!(entity instanceof LivingEntity)) return;
			
			target = (LivingEntity) entity;
		}
		
		if (this.target == null && mc.currentScreen instanceof ChatScreen)
			this.target = mc.player;
		
		

		
		{ // Расчитываем анимации
			if (!super.showing.isForward() || !this.get()) {
				this.waiting.setForward(false);

				this.size.setForward(false);
				this.showing.setForward(false);
				
				if (this.target != null && this.target.getHealth() <= 0) this.killed = true;
				
				this.killing.setForward(false);
				this.killingWait.setForward(false);
				
				if (this.killing.finished()) this.killed = false;
			} else {
				this.waiting.setForward((!(mc.currentScreen instanceof ChatScreen) && this.target == null) || !this.killing.finished(false));
				
				this.size.setForward(this.target != null || !this.showing.finished(false) || !this.killing.finished(false));
				this.showing.setForward(this.size.finished() && (this.target != null || !waiting.finished()) || !this.killing.finished(false));
				
				if (this.target != null && this.target.getHealth() <= 0) this.killed = true;
				
				this.killing.setForward(this.killed || !this.killingWait.finished(false));
				this.killingWait.setForward(this.killed);
				
				if (this.killing.finished()) this.killed = false;
			}
		}
		
		if (this.target == null) {
			this.killed = false;
			this.target = this.lastTarget == null ? mc.player : this.lastTarget;
		}
		
		if (target != null && magnet.get() && !(mc.currentScreen instanceof ChatScreen) && !target.isDead() && mc.world.getAllEntities().contains(target)) {
			float posx = (float) (target.lastTickPosX + (target.getPosX() - target.lastTickPosX) * mc.getRenderPartialTicks());
			float posy = (float) (target.lastTickPosY + (target.getPosY() - target.lastTickPosY) * mc.getRenderPartialTicks());
			float posz = (float) (target.lastTickPosZ + (target.getPosZ() - target.lastTickPosZ) * mc.getRenderPartialTicks());

			double[] pos = Render.worldToScreen(posx, posy + target.getHeight()/2f, posz);
            if (pos == null) return;
            
            if (PositionTracker.isInView(target)) {
            	x = (float) pos[0] - width/2F;
            	y = (float) pos[1] - height/2F;
            }
		}
		Rect rect = new Rect(x,y,width,height);

		if (!this.size.finished(false)) { // Рендеринг
			// Анимация на здоровье
			int start = 270;
			float max = target.getMaxHealth();
			float unit = target == null ? 20 : (Server.isServerForHPFix() ? target.getRealHealth() : (target.getHealth()+target.getAbsorptionAmount()));
			float health = target == null ? 1 : Math.max(0, Math.min(1, (Server.isServerForHPFix() ? target.getRealHealth() / 20F : (target.getHealth()-target.getAbsorptionAmount()) / (target.getMaxHealth()))));
			float silentHealth = target == null ? 1 : Math.max(0, Math.min(1, (Server.isServerForHPFix() ? target.getRealHealth() / 20F : (target.getHealth()) / (target.getMaxHealth()))));
			float gapple = target == null ? 1 : Math.max(0, Math.min(1, (Server.isServerForHPFix() ? 0 : (target.getAbsorptionAmount() / max))));
			this.healthAnim.animate(health, 100);
			gappleAnim.animate(gapple, 100);
			silentHealthAnim.animate(silentHealth, 100);
			unitAnim.animate(unit, 100);
			
			// Анимация на размер гуишки (открытие/закрытие)
			Render.scale(x + width / 2, y + height / 2, this.size.get());
			
			float alpha = this.size.get();
			float glowAlpha = this.showing.get();
			
			FixColor upLeft = Style.getOutlinePoint(0).alpha(alpha);
			FixColor upRight = Style.getOutlinePoint(45).alpha(alpha);
			FixColor downRight = Style.getOutlinePoint(90).alpha(alpha);
			FixColor downLeft = Style.getOutlinePoint(135).alpha(alpha);
			
			if (!this.killing.finished(false)) {
				//Glow.draw(ms, new Rect(this.draggable.getX()+5,this.draggable.getY()+4,this.draggable.getWidth()-10,this.draggable.getHeight()-8), (rock.getThemes().getCurrent() == rock.getThemes().getLightTheme() ? 5 : 7) + 5 * this.killing.get(), 9.5f + 10 * this.killing.get(), upLeft, upRight, downLeft, downRight);
				Round.draw(ms, rect, 4, rock.getThemes().getFirstColor().alpha(alpha*(1-this.killing.get()*0.2f)));
				float offset = 10 * killing.get();
				Glow.draw(ms, rect, offset, 0.7f * killing.get(), 10 + offset, upLeft, upRight, downLeft, downRight);

				float off = 0.5f;
				//Round.draw(ms, new Rect(this.draggable.getX()-off,this.draggable.getY()-off,this.draggable.getWidth()+off*2,this.draggable.getHeight()+off*2), 5+off, upLeft, upRight, downLeft, downRight);
			}
			
			Round.draw(ms, rect, 4, rock.getThemes().getFirstColor().move(FixColor.WHITE, hover()).alpha(alpha*(1-this.killing.get()*0.2f)));
			
			if (additions.get()) {
				Render.glow(ms, rect, showing.get());
			}
			
			if (additions.get()) {
				Render.outline(ms, rect, showing.get());
			}
			
			if (!magnet.get()) {
				GL11.glEnable(GL11.GL_SCISSOR_TEST);
				Render.scissor(x, y, draggable.getWidth(), draggable.getHeight());
			}
			
			float size = 27;
			float animPromotion = magnet.get() ? 0 : this.showing.get() * height - height;
			
			Round.draw(ms, new Rect(x + height / 2 - size/2 + animPromotion, y + height / 2 - size/2, size, size), size / 2, rock.getThemes().getSecondColor().alpha(this.showing.get() * 0.8f));
			
			Stencil.init();
			size = 26;
			Round.draw(ms, new Rect(x + height / 2 - size/2 + animPromotion, y + height / 2 - size/2, size, size), size / 2, rock.getThemes().getSecondColor().alpha(this.showing.get()));
			Stencil.read(1);

            RenderSystem.disableDepthTest();
			InventoryScreen.drawEntityOnScreen((int) (x + height / 2 + animPromotion), (int) (y + height / 2 + 5 + target.getHeight() * 20 - (rock.getModules().get(Cosmetics.class).getModel(target) == 1 ? 10 : 0)), 25, -20, 10, target);
			RenderSystem.enableDepthTest();
			
			Stencil.finish();
			size = 27;

			Render.drawCircle(x + height / 2 + animPromotion, y + height / 2, size/2, start, start+360, rock.getThemes().getThirdColor().alpha(this.showing.get()));
			
			if (mode.is(circle) && !mode.is(none)) {
				if (healthcolor.get()) {
					FixColor color = (healthAnim.get() > 0.5f ? FixColor.ORANGE.move(FixColor.GREEN, (healthAnim.get()-0.5f) * 2) : FixColor.RED.move(FixColor.ORANGE, healthAnim.get() * 2));
					Render.drawCircle(x + height / 2 + animPromotion, y + height / 2, size/2, start, start + (int) (361 * this.healthAnim.get()), color.alpha(this.showing.get()));
				} else {
					Render.drawUICircle(x + height / 2 + animPromotion, y + height / 2, size/2, start, start + (int) (361 * this.healthAnim.get()), 1.5f, this.showing.get());
				}
				
				if (gappleAnim.get() > 0 && !Server.isFT()) {
					Render.drawCircle(x + height / 2 + animPromotion, y + height / 2, size/2, start - (int) (gappleAnim.get() * 360), start, FixColor.YELLOW.alpha(this.showing.get()));
				}
			} else if (mode.is(circle2) && !mode.is(none)) {
				Render.drawCircle(x + width - size/2 - 5, y + height / 2, size/2, start, start+360, rock.getThemes().getThirdColor().alpha(this.showing.get()));
				
				if (healthcolor.get()) {
					FixColor color = (healthAnim.get() > 0.5f ? FixColor.ORANGE.move(FixColor.GREEN, (healthAnim.get()-0.5f) * 2) : FixColor.RED.move(FixColor.ORANGE, healthAnim.get() * 2));
					Render.drawCircle(x + width - size/2 - 5, y + height / 2, size/2, start, start + (int) (361 * this.healthAnim.get()), 4.5f, color.alpha(this.showing.get()));

				} else {
					Render.drawUICircle(x + width - size/2 - 5, y + height / 2, size/2, start, start + (int) (361 * this.healthAnim.get()), 4.5f, this.showing.get());
				}
				
				if (gappleAnim.get() > 0 && !Server.isFT()) {
					Render.drawCircle(x + width - size/2 - 5, y + height / 2, size/2, start - (int) (gappleAnim.get() * 360), start, 4.5f, FixColor.YELLOW.alpha(this.showing.get()));
				}
				String text = percents.get() ? Math.round((silentHealthAnim.get() + gappleAnim.get()) * 100F) + "%" : Math.round(unitAnim.get()) + "";
				if (percents.get()) {
					this.bold.get(14).draw(ms, text, x + width - size/2 - 5.5f - bold.get(14).getWidth(text)/2, y + height / 2 - 5, rock.getThemes().getTextFirstColor().alpha(this.showing.get()));
 				} else {
 					this.semibold.get(17).draw(ms, text, x + width - size/2 - 5.5f - semibold.get(17).getWidth(text)/2, y + height / 2 - 6, rock.getThemes().getTextFirstColor().alpha(this.showing.get()));
 				}
			}
			
			
			Stencil.init();
			Round.draw(ms, new Rect(x + height / 2 + animPromotion + 17, y + 5, 112 / 2 + 10, 12), 2, rock.getThemes().getSecondColor().alpha(this.showing.get()));
			Stencil.read(1);
			if (copied || !this.copiedAnim.finished(false)) {
				this.bold.get(16).draw(ms, NameProtect.correct(target.getName().getString()), x + height, y + 7 + animPromotion + 12 * this.copiedAnim.get(), rock.getThemes().getTextFirstColor().alpha(this.showing.get() * (1-this.copiedAnim.get())));
				this.bold.get(16).draw(ms, "Скопировано", x + height, y + 7 + animPromotion - 12 + 12 * this.copiedAnim.get(), rock.getThemes().getTextFirstColor().alpha(this.showing.get() * this.copiedAnim.get()));
			} else {
				nickname = this.bold.get(16).draw(ms, NameProtect.correct(target.getName().getString()), x + height, y + 7 + animPromotion, rock.getThemes().getTextFirstColor().alpha(this.showing.get()));
			}
			this.hoverNickAnim.setForward(Hover.isHovered(nickname, mouseX, mouseY) && copy.get());
			Stencil.finish();
			
			Round.draw(ms, new Rect(x + height + animPromotion + 55 - 20 * this.hoverNickAnim.get(), y + 7, 10 + 20 * this.hoverNickAnim.get(), 10), 2, rock.getThemes().getFirstColor().move(FixColor.WHITE, hover()).alpha(0), rock.getThemes().getFirstColor().move(FixColor.WHITE, hover()).alpha(this.showing.get()), rock.getThemes().getFirstColor().move(FixColor.WHITE, hover()).alpha(0), rock.getThemes().getFirstColor().move(FixColor.WHITE, hover()).alpha(this.showing.get()));
			
			this.copiedAnim.setForward(copied);
			Stencil.init();
			Round.draw(ms, new Rect(Math.min(x + height + nickname.getWidth(), x + 112 - 16), y + 7 + .5f, 10, 9), 0, FixColor.RED);
			Stencil.read(1);
			Render.image("icons/copy.png", Math.min(x + height + nickname.getWidth() + 1, x + 112 - 15), y + 7 + 1.5f + 12 * this.copiedAnim.get(), 7, 7, rock.getThemes().getTextSecondColor().alpha(this.showing.get() * this.hoverNickAnim.get() * (1-this.copiedAnim.get())));
			Stencil.finish();
			Render.image("icons/yes.png", Math.min(x + height + bold.get(16).getWidth("Скопировано") + 2, x + 112 - 15), y + 7 + 1.9f - 12 + 12 * this.copiedAnim.get(), 7, 7, FixColor.GREEN.alpha(this.showing.get() * this.hoverNickAnim.get() * this.copiedAnim.get()));

			if (mode.is(bar) && !mode.is(none)) {
				boolean gold = gappleAnim.get() > 0.1f;
				float sum = MathHelper.clamp(healthAnim.get() + gappleAnim.get(), 0, 1);
				healthHide.setForward(sum > 0.3f);
				goldenHide.setForward(gold);
				hurt.setForward(target.hurtTime >= 5);
				float barY = y + height - 17 - animPromotion;
				float barWidth = (width - height*2F + 3);
				String text = percents.get() ? Math.round((silentHealthAnim.get() + gappleAnim.get()) * 100F) + "%" : Math.round(unitAnim.get()) + "";
				
				Round.draw(ms, new Rect(x + height, barY, barWidth, 8), 2, rock.getThemes().getSecondColor().alpha(this.showing.get() * 0.8f));
				FixColor[] circle = Interface.getCircle(alpha);
				FixColor c = circle[0], // Style.getMain().alpha(this.showing.get()), 
						c1 = circle[2], // Style.getSecond().alpha(this.showing.get()),
						g = FixColor.YELLOW.alpha(this.showing.get() * goldenHide.get());
				
				if (healthcolor.get()) {
					c = (healthAnim.get() > 0.5f ? FixColor.ORANGE.move(FixColor.GREEN, (healthAnim.get()-0.5f) * 2) : FixColor.RED.move(FixColor.ORANGE, healthAnim.get() * 2));
					c1 = c.darker(0.5f);
				}
				
				Interface ui = rock.getModules().get(Interface.class);

				//float offset = 2;
				//Glow.draw(ms, new Rect(x + height, barY, barWidth * healthAnim.get(), 8).size(hurt.get()*0.5f).size(-1), offset, ui.getAlpha().get(), 10 + offset, c, c1, c, c1);
				Round.draw(ms, new Rect(x + height, barY, barWidth * healthAnim.get(), 8).size(hurt.get()*0.5f), 2, c, c1, c, c1);
				if (!goldenHide.finished(false)) {
					Round.draw(ms, new Rect(x + height + MathHelper.clamp(barWidth * healthAnim.get(), 2, barWidth - barWidth * gappleAnim.get()) + 2, barY, barWidth * gappleAnim.get() - 2, 8).size(hurt.get()*0.5f), 2, g);
				}
				if (particles.get() && this.target.hurtTime > 0 && this.target.getHealth() > 0 && this.showing.finished()) {
					particleEngine.spawn(x + height - 1 + barWidth * sum, barY + MathUtility.random(1, 7), MathUtility.random(2, 5), MathUtility.random(-1, 1), gold ? FixColor.YELLOW : c1);
				}
				FixColor textColor = (c.getRed() + c.getGreen() + c.getBlue()) / 3F > 200 ? FixColor.BLACK : FixColor.WHITE;
				if (gappleAnim.get() > healthAnim.get() * 0.5f)
					semibold.get(10).draw(ms, text, x + height + barWidth * sum / 2F - semibold.get(10).getWidth(text)/2F + 0.5f, barY + 1.5f, FixColor.BLACK.alpha(showing.get() * healthHide.get()));
				semibold.get(10).draw(ms, text, x + height + barWidth *  sum/ 2F - semibold.get(10).getWidth(text)/2F, barY + 1, textColor.alpha(showing.get() * healthHide.get()));
				
				ArrayList<ItemStack> stacks = new ArrayList<>();
				target.getArmorInventoryList().forEach((stack) -> {
					if (!stack.isEmpty()) {
						stacks.add(stack);
					}
				});
				
				float xOff = 0, yOff = 0;
				for (int i = 0; i < 4; i++) {
					//float yAnim = yOff != 0 ? height - showing.get() * height : showing.get() * height - height;
					float xAnim = magnet.get() ? 0 : height - showing.get() * height;
					Round.draw(ms, new Rect(x + width - height/2F - 10 + xOff + xAnim, y + height/2f - 10 + yOff, 9, 9), 2, rock.getThemes().getSecondColor().alpha(this.showing.get() * 0.8f));
					if (i < stacks.size() && showing.get() > 0.5f) {
						Render.drawStackOld(stacks.get(stacks.size()-i-1), x + width - height/2F - 10 + xOff + xAnim, y + height/2f - 10 + yOff, 0.6F);
					}
					yOff += 11;
					if (yOff > 21) {
						yOff = 0;
						xOff += 11;
					}
				}
			} else {
				ArrayList<ItemStack> stacks = new ArrayList<>();
				if (!target.getHeldItemMainhand().isEmpty()) stacks.add(target.getHeldItemMainhand());
				if (!target.getHeldItemOffhand().isEmpty()) stacks.add(target.getHeldItemOffhand());
				target.getArmorInventoryList().forEach((stack) -> {
					if (!stack.isEmpty()) {
						stacks.add(stack);
					}
				});
				
				for (int i = 0; i < 6; i++) {
					Round.draw(ms, new Rect(x + height + i * 11, y + 20 + height - this.showing.get() * height, 9, 9), 2, rock.getThemes().getSecondColor().alpha(this.showing.get() * 0.8f));
				}
				
				float xOff = 0;
				if (!this.showing.finished(false))
					for (ItemStack stack : stacks) {
						Render.drawStackOld(stack, x + height + xOff, y + 20 + height - this.showing.get() * height, 0.6F);
						
						xOff += 11;
					}
			}
			
			if (!magnet.get())
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
			Render.end(); // ЗАКРЫВАЕМ МАТРИЦУ!!
			
			// Дальше идут партиклы

			
			if (particles.get()) {
				size /= 2;
				x = mode.is(circle) ? x + height / 2 + animPromotion : x + width - size / 2 - 11;
				y = y + height / 2;

				float i = (float) (start + (int) (361 * this.healthAnim.get()));

				final float cos = (float) (Math.cos(i * 3.141592653589793 / 180.0) * size * 1.0);
				final float sin = (float) (Math.sin(i * 3.141592653589793 / 180.0) * size * 1.0);
				final float modifCos = (float) (Math.cos((i + 90 + MathUtility.random(30, -30)) * 3.141592653589793 / 180.0)
						* size * 0.5);
				final float modifSin = (float) (Math.sin((i + 90 + MathUtility.random(30, -30)) * 3.141592653589793 / 180.0)
						* size * 0.5);
				
				float gappleVal = healthAnim.get() * (target.getAbsorptionAmount() * 10);
				
				boolean amoutBool = target.getAbsorptionAmount() > 0 && gappleVal > 0 && !Server.isFT();

				if (this.target.hurtTime > 0 && this.target.getHealth() > 0 && this.showing.finished() && !mode.is(bar)) {
					FixColor color = healthcolor.get() ? (healthAnim.get() > 0.5f ? FixColor.ORANGE.move(FixColor.GREEN, (healthAnim.get()-0.5f) * 2) : FixColor.RED.move(FixColor.ORANGE, healthAnim.get() * 2)) : Style.getSecond();
					particleEngine.spawn(x + cos - 0.5f, y + sin - 0.5f, modifCos, modifSin, amoutBool ? FixColor.YELLOW : color);
				}
			}
		}
		
		particleEngine.render(ms);
		
		this.draggable.setWidth(width);
		this.draggable.setHeight(height);
		
		if (this.target != null) this.lastTarget = this.target;
	}
	
	public void mouseClicked(double mouseX, double mouseY, int button) {
		if (nickname == null || !get()) return;
		if (Hover.isHovered(nickname, mouseX, mouseY) && copy.get()) {
			TextUtility.copyText(target.getName().getString());
			//rock.getAlertHandler().alert("Ваш ник скопирован!", AlertType.INFO);
			this.copied = true;
		}
	}
	
}
