package fun.rockstarity.client.modules.move;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventTick;
import fun.rockstarity.api.events.list.game.EventWorldChange;
import fun.rockstarity.api.events.list.game.packet.EventSendPacket;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.helpers.player.Move;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.helpers.system.TextUtility;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.draggables.Draggable;
import fun.rockstarity.api.render.ui.rect.Rect;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Difficulty;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author Malecharik
 * @since 12 Mar 2024 22:33:58
 */


@Info(name = "Timer", desc = "Ускоряет мир для вас", type = Category.MOVE)
public class Timer extends Module {

	Mode mode = new Mode(this, "Режим");

	Mode.Element classic = new Mode.Element(mode, "Обычный");
	Mode.Element funtime = new Mode.Element(mode, "FunTime");

	private final CheckBox smart = new CheckBox(this, "Умный");
	
	private final Slider speed = new Slider(this, "Скорость").min(0.1f).max(10f).inc(0.1f).set(5f).hide(() -> funtime.get());
	
	private float charge = 100, prevCharge = 100;
	private long last;
	
	protected final Draggable draggable = new Draggable("Таймер", new Rect(0, 0, 0, 0));
	protected final Animation showing = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);

	@Override
	public void onEvent(Event event) {
		if (event instanceof EventUpdate) {
			if (funtime.get()) {
				if (!smart.get() || charge > 10) {
					mc.player.getMotion().x *= 1.05;
					mc.player.getMotion().z *= 1.05;
					
					if (mc.player.fallDistance > 0)
						mc.player.getMotion().y *= 1.05;
				}
			} else if (classic.get()) {
				if (smart.get()) {
					mc.timer.timerSpeed = this.charge > 10 ? this.speed.get() : 1;
				} else {
					mc.timer.timerSpeed = this.speed.get();
				}
			}
		}
	}

	@Override
	public void onAllEvent(Event event) {
		this.smartCalculation(event); //Если чекбокс умный включен то вызываем расчет 
	}
	
	/*
	 * Расчитываем умный таймер
	 */
	@NativeInclude
	private void smartCalculation(Event event) {
		if (this.smart.get()) {
			if (classic.get()) {
				if (event instanceof EventSendPacket e && e.getPacket() instanceof CPlayerPacket) { // Проверяем, является ли событие отправкой пакета и пакетом игрока
					if (System.currentTimeMillis() - this.last < 1000) { // Если прошло менее секунды с предыдущего вызова метода
						float value = (float) (0.05 - (float) (System.currentTimeMillis() - this.last) / 1000L) * 400; // Рассчитываем значение для вычета из заряда
						this.charge -= Math.max(0, value); // Вычитаем значение из заряда, не допуская отрицательного значения
					}
					if (Move.isMoving()) this.charge += 0.5f; // Если персонаж двигается увеличиваем заряд
					this.charge = Math.max(0, Math.min(100, this.charge)); // Ограничиваем заряд в пределах от 0 до 100
					this.last = System.currentTimeMillis(); // Обновляем время последнего вызова метода
				}
			} else if (funtime.get()) {
				if (event instanceof EventWorldChange) {
					charge += 7;
				}
				
				if (event instanceof EventUpdate) {
					if (mc.world.getDifficulty() == Difficulty.EASY) {
						charge = 100;
					}
					
					charge += 0.006f;
					
					if (get()) {
						charge -= 2.5f;
					}
					
					charge = MathHelper.clamp(charge, 0, 100);
				}
			}
		}
		
		if (event instanceof EventUpdate) {
			for (Module module : rock.getModules().values()) {
			//	System.out.println(module.getInfo().name() + " - " + module.getInfo().type().getName());
			}
		}
		
		if (event instanceof EventRender2D e) {
			if (!smart.get()) {
				draggable.setWidth(0);
				draggable.setHeight(0);
				return;
			}
			
			showing.setForward(charge < 100 && charge > 0 || mc.currentScreen instanceof ChatScreen);
			
			if (showing.finished(false)) return;
			
			FixColor bgColor = rock.getThemes().getFirstColor().alpha(showing.get());
			FixColor textColor = rock.getThemes().getTextFirstColor().alpha(showing.get());
			
			String type = "Timer";
			MatrixStack ms = e.getMatrixStack();
            String remained = String.format("Заряд: %s%%", TextUtility.formatNumber(charge));
            
			float width = 40 + semibold.get(10).getWidth(remained);
            float height = 20;
            
            if (draggable.getX() == 0 && draggable.getY() == 0) { 
            	draggable.setX(sr.getScaledWidth()/2F - width/2F);
            	draggable.setY(sr.getScaledHeight() - 100 - height/2F);
            }
            
            float x = draggable.getX();
            float y = draggable.getY();
            
            draggable.setWidth(width);
            draggable.setHeight(height);
            
            Round.draw(ms, new Rect(x, y, width, height), 2, bgColor);
            bold.get(12).draw(ms, type, x + 5, y + 3, textColor);

        	semibold.get(10).draw(ms, remained, x + 5, y + 10, textColor);
            float size = 12;
            int from = 270;
            Round.draw(ms, new Rect(x + width - height/2F - size/2F, y + height/2F - size/2F, size, size), size/2F-0.1f, rock.getThemes().getSecondColor().alpha(showing.get()));
			Render.drawClientCircle(x + width - height/2F, y + height/2F, size/2F-1, from, from + (int) (361 * charge / 100F), showing.get());
		}
	}
	
	/*
	 * Обрабатываем выключение
	 */
	@Override
	@NativeInclude
	public void onDisable() {
		mc.timer.reset(); //Ресетаем таймер на 1
	}
	
	@Override
	public void onEnable() {
		
	}
	
}
