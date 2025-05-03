package fun.rockstarity.client.modules.render;

import java.util.ConcurrentModificationException;

import org.lwjgl.opengl.GL11;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.EventType;
import fun.rockstarity.api.events.list.game.EventKill;
import fun.rockstarity.api.events.list.game.EventTotemBreak;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.events.list.render.ui.shaders.EventBlur;
import fun.rockstarity.api.helpers.math.Hover;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.ColorPicker;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.animation.infinity.InfinityAnimation;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.draggables.Draggable;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.client.modules.render.ui.Alerts;
import fun.rockstarity.client.modules.render.ui.ArmorHud;
import fun.rockstarity.client.modules.render.ui.Bps;
import fun.rockstarity.client.modules.render.ui.Coordinates;
import fun.rockstarity.client.modules.render.ui.Effects;
import fun.rockstarity.client.modules.render.ui.ItemsBinds;
import fun.rockstarity.client.modules.render.ui.KeyBinds;
import fun.rockstarity.client.modules.render.ui.Schedules;
import fun.rockstarity.client.modules.render.ui.Staffs;
import fun.rockstarity.client.modules.render.ui.TargetHud;
import fun.rockstarity.client.modules.render.ui.Watermark;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.screen.ChatScreen;

/**
 * @author ConeTin
 * @since 3 дек. 2023 г.
 */


@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Info(name="Interface", desc="2D визуалы клиента", type=Category.RENDER)
public class Interface extends Module {
	
	static InfinityAnimation circleAnim = new InfinityAnimation();
	
	Select elements = new Select(this, "Элементы");
	
	Select additions = new Select(this, "Дополнения");
	
	Select.Element outline = new Select.Element(additions, "Обводка").set(true);
	Select.Element blur = new Select.Element(additions, "Размытие").set(true);
	Select.Element glow = new Select.Element(additions, "Свечение");
	Select.Element upInChat = new Select.Element(additions, "Поднятие в чате");
	Select.Element english = new Select.Element(additions, "Англ. вариант");
	
	Select whenCollapsing = new Select(this, "При сворачивании").desc("Действия, которые будут происходить тогда, когда свернута игра");
	
	Select.Element hideUi = new Select.Element(whenCollapsing, "Скрывать худ").set(true);
	Select.Element blurFrame = new Select.Element(whenCollapsing, "Размытие").hide(() -> !blur.get()).set(true);
	Select.Element fpsLimit = new Select.Element(whenCollapsing, "Ограничивать фпс").set(true);
	
	Animation blurAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	
	Slider blurOffset = new Slider(this, "Сила размытия").min(1).max(3).inc(1).set(2).hide(() -> !blur.get());
	
	ColorPicker color = new ColorPicker(this, "Цвет").set(true).add(new FixColor(198, 198, 198)).desc("Цвет обводки/свечения. Можно выбрать несколько и установить клиентский");

	Slider offset = new Slider(this, "Сила свечения").min(3).max(25).inc(0.5f).set(5).hide(() -> !glow.get());
	Slider alpha = new Slider(this, "Прозрачность свечения").min(0.1f).max(0.5f).inc(0.1f).set(0.3f).hide(() -> !glow.get());
	
	Slider size = new Slider(this, "Размер").desc("Настройка размера интерфейса").min(0.8f).max(1.1f).inc(0.1f).set(1)
			.text(0.8f, "Маленький")
			.text(0.9f, "Меньше обычного")
			.text(1.0f, "Обычный")
			.text(1.1f, "Большой");
	
	Slider outlineWidth = new Slider(this, "Ширина обводки").desc("Настройка размера интерфейса").min(1).max(4).inc(0.5f).set(4).text(4, "Обычный").hide(() -> !outline.get());
	
	TargetHud targetHud;
	KeyBinds keyBinds;
	Alerts alerts;
	Effects effects;
	Coordinates coordinates;
	Schedules schedules;
	
	public Interface() {
		super(3);
		new Watermark(this, this.elements);
		this.targetHud = new TargetHud(this, elements);
		coordinates = new Coordinates(this, elements);
		new ArmorHud(this, elements);
		this.keyBinds = new KeyBinds(this, elements);
		this.alerts = new Alerts(this, elements);
		new Bps(this, elements);
		this.effects = new Effects(this, elements);
		schedules = new Schedules(this, elements);
		new ItemsBinds(this, elements);
		new Staffs(this, elements);
	}
	
	@Override
	@EventType({EventRender2D.class, EventBlur.class, EventKill.class, EventTotemBreak.class})
	public void onEvent(Event event) {
		if (event instanceof EventBlur e && blurFrame.get() && blur.get()) {
			blurAnim.setForward(!mc.isGameFocused());
			Round.draw(e.getMatrixStack(), new Rect(0,0,sr.getScaledWidth(),sr.getScaledHeight()), 0.1f, FixColor.WHITE.alpha(blurAnim.get()));
		}

		try {
			this.elements.getElements().forEach(element -> {
				UIElement elmt = ((UIElement) element);
				
				if (elmt == null) return;
				elmt.update();
				if (elmt.get() || !elmt.showing.finished(false)) {
					if (event instanceof EventRender2D) {
						//elmt.draggabling.setForward(elmt.draggable.isDragging());
						/*
						Render.scale(
								elmt.draggable.getX() + elmt.draggable.getWidth() / 2f,
								elmt.draggable.getY() + elmt.draggable.getHeight() / 2f,
								elmt.draggabling.get() / 15f + 1
						);
						*/
					}
					
					if (!elmt.showing.finished(false)) {
						if (event instanceof EventRender2D || event instanceof EventBlur) {
							Render.scale(elmt.draggable.getX(), elmt.draggable.getY(), size.get());
							if (elmt.draggable.getY() > sr.getScaledHeight() - 100 && (elmt instanceof Bps || elmt instanceof ArmorHud || elmt instanceof Coordinates || elmt instanceof ItemsBinds) && upInChat.get()) {
								GL11.glTranslated(0, (1-mc.getIngameGUI().getAnim().get()) * 14, 0);
							}
 						}
						elmt.onEvent(event);
						if (event instanceof EventRender2D || event instanceof EventBlur) {
							Render.end();
						}
					}
					
					//if (event instanceof EventRender2D) Render.end();
				}
			});
		} catch (ConcurrentModificationException e) {}
	}
	
	public static class UIElement extends Select.Element implements IAccess {
		
		protected final Draggable draggable;
		protected final Animation showing = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
		protected final Animation hover = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
		//protected final Animation draggabling = new Animation().setEasing(Easing.BOTH_CUBIC).setSpeed(200);

		public UIElement(Select parent, String name, Rect rect) {
			super(parent, name);
			Interface ui = (Interface) parent.getParent();
			this.draggable = new Draggable(name, rect) {
				@Override
				public void setWidth(float width) {
					this.width = width * ui.size.get();
				}
				
				@Override
				public void setHeight(float height) {
					this.height = height * ui.size.get();
				}
			};
		}
		
		public void onEvent(Event event) {}
		
		public void mouseReleased(double mouseX, double mouseY, int button) {}
		public void mouseClicked(double mouseX, double mouseY, int button) {}
		
		public void keyPressed(int keyCode, int scanCode, int modifiers) {}
		
		public void update() {
			this.showing.setForward((this.get() && !mc.getGameSettings().hideGUI && !this.mc.getGameSettings().showDebugInfo && !rock.getModules().get(Interface.class).getHideUi().get()) || (this.get() && !mc.getGameSettings().hideGUI && !this.mc.getGameSettings().showDebugInfo && mc.isGameFocused()));
			if (this.showing.finished(false)) {
				this.draggable.setWidth(0);
				this.draggable.setHeight(0);
			}
		}
		
		public float hover() {
			float coff = sr.getGuiScaleFactorF();
			hover.setForward((draggable.isSelected() || Hover.isHovered(draggable, mc.mouseHelper.getMouseX() / coff, mc.mouseHelper.getMouseY() / coff)) && mc.currentScreen instanceof ChatScreen);
			return hover.get() * 0.1f;
		}
		
		public boolean glow() {
			return Interface.glow();
		}

		public boolean blur() {
			return Interface.blur();
		}
		
		public boolean outline() {
			return Interface.outline();
		}
		
		public FixColor[] getCircle() {
			return getCircle(1);
		}
		
		public FixColor[] getCircle(float alpha) {
			return Interface.getCircle(alpha);
		}
	}
	
	public static FixColor[] getCircle() {
		return getCircle(1);
	}
	
	public static FixColor[] getCircle(float alpha) {
		ColorPicker color = rock.getModules().get(Interface.class).getColor();
		circleAnim.animate(System.currentTimeMillis()/1000F, 50);
		int val = (int) circleAnim.get();
		return new FixColor[] {
				color.get(val).alpha(alpha),
				color.get(val+90).alpha(alpha),
				color.get(val+180).alpha(alpha),
				color.get(val+270).alpha(alpha)
		};
	}
	
	public static FixColor getPoint(int point, float alpha) {
		ColorPicker color = rock.getModules().get(Interface.class).getColor();
		circleAnim.animate(System.currentTimeMillis()/1000F, 50);
		int val = (int) circleAnim.get();
		return color.get(point).alpha(alpha);
	}
	
	
	public static boolean glow() {
		return rock.getModules().get(Interface.class).getGlow().get();
	}

	public static boolean blur() {
		return rock.getModules().get(Interface.class).getBlur().get();
	}
	
	public static boolean outline() {
		return rock.getModules().get(Interface.class).getOutline().get();
	}
	
	@Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}
	
}