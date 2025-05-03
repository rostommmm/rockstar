package fun.rockstarity.client.modules.render.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.function.Supplier;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Function;
import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.binds.Bind;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.inputs.EventKey;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.events.list.render.ui.shaders.EventBlur;
import fun.rockstarity.api.helpers.game.Binds;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.helpers.render.Stencil;
import fun.rockstarity.api.helpers.system.TextUtility;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Select.Element;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.animation.infinity.InfinityAnimation;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.fonts.FontSize;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.client.modules.other.Assist;
import fun.rockstarity.client.modules.player.MiddleClick;
import fun.rockstarity.client.modules.render.Interface;
import fun.rockstarity.client.modules.render.Interface.UIElement;
import lombok.AllArgsConstructor;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.CooldownTracker;
import net.minecraft.util.math.vector.Vector2f;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author ConeTin
 * @since 26 июл. 2024 г.
 */

public class ItemsBinds extends UIElement {
	
	private float x, y, width, height;
	
	private final List<ItemBind> items = new ArrayList<>();
	
	private final Select select = new Select(this, "Предметы");

	private ItemBind createBind(String name, Item item, Supplier<ArrayList<Bind>> bindSupplier, Supplier<Boolean> conditionSupplier) {
		ItemBind itemBind = new ItemBind(name, item, () -> {
			if (bindSupplier.get().isEmpty()) 
				return "";
			
	        Bind bind = bindSupplier.get().get(0);
	        if (conditionSupplier.get()) {
	            return Binds.getName(bind.getKey(), bind.getScancode());
	        }
	        
	        return "";
	    });
		
		new Element(select, itemBind.name).set(select.getElements().size() <= 4);
		items.add(itemBind);
		
	    return itemBind;
	}
	
	private ItemBind createBind(String name, Item item, Function<Assist, ArrayList<Bind>> bindFunc, Function<Assist, Boolean> condFunc) {
	    return createBind(name, item, () -> bindFunc.apply(rock.getModules().get(Assist.class)), () -> condFunc.apply(rock.getModules().get(Assist.class)));
	}
	
	private final ItemBind trap = createBind(
			"Трапка", 
			Items.NETHERITE_SCRAP,
			assist -> assist.getTrapBind().getBinds(), 
			assist -> assist.getTrap().get()
		);

	private final ItemBind pearl = createBind(
			"Жемчуг", 
			Items.ENDER_PEARL,
			() -> rock.getModules().get(MiddleClick.class).getPearlBind().getBinds(), 
			() -> rock.getModules().get(MiddleClick.class).getClickPearl().get()
		);
	
	private final ItemBind gaple = createBind(
			"Чарка", 
			Items.ENCHANTED_GOLDEN_APPLE,
			assist -> assist.getCharGappleBind().getBinds(), 
			assist -> assist.getCharGapple().get()
		);
	
	private final ItemBind dezorent = createBind(
			"Дезориентация", 
			Items.ENDER_EYE,
			assist -> assist.getDezorentBind().getBinds(), 
			assist -> assist.getDezorent().get()
		);
	
	private final ItemBind plast = createBind(
			"Пласт", 
			Items.DRIED_KELP,
			assist -> assist.getAutoplastBind().getBinds(), 
			assist -> assist.getAutoplast().get()
		);
	
	private final ItemBind smerch = createBind(
			"Огненный смерч", 
			Items.FIRE_CHARGE,
			assist -> assist.getSmerchBind().getBinds(), 
			assist -> assist.getSmerch().get()
		);
	
	private final ItemBind godaura = createBind(
			"Божья аура", 
			Items.PHANTOM_MEMBRANE,
			assist -> assist.getAuraBind().getBinds(), 
			assist -> assist.getAura().get()
		);
	
	private final ItemBind pilb = createBind(
			"Явная пыль", 
			Items.SUGAR,
			assist -> assist.getPilbBind().getBinds(), 
			assist -> assist.getPilb().get()
		);
	
	private final Slider count = new Slider(this, "В строчке").min(1).max(5).inc(1).set(3);
	@NativeInclude
	public ItemsBinds(Interface ui, Select select) {
		super(select, "Предметы", new Rect(206, 58, 34, 20));
		this.set(true);
	}
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventBlur e) {
			MatrixStack ms = e.getMatrixStack();
			FontSize font = semibold.get(16);
			
			Vector2f[] poses = {
					new Vector2f(-10,-10),
					new Vector2f(sr.getScaledWidth()-this.draggable.getWidth()+10,-10),
					new Vector2f(sr.getScaledWidth()-this.draggable.getWidth()+10,sr.getScaledHeight()-height+10),
					new Vector2f(-10,sr.getScaledHeight()+10)
			};
			
			Vector2f closestPoint = new Vector2f(0,0);
			float minDistanceSquared = Float.MAX_VALUE;
			for (Vector2f pose : poses) {
			    float dx = pose.x - this.draggable.getX();
			    float dy = pose.y - this.draggable.getY();
			    float distanceSquared = dx * dx + dy * dy;
			    if (distanceSquared < minDistanceSquared) {
			        minDistanceSquared = distanceSquared;
			        closestPoint = pose;
			    }
			}
			
			if (minDistanceSquared > 4000) {
				Vector2f[] centerPoses = {
						new Vector2f(sr.getScaledWidth()/2,-10),
						new Vector2f(sr.getScaledWidth()/2,sr.getScaledHeight()+10),
						new Vector2f(sr.getScaledWidth()+10,sr.getScaledHeight()/2),
						new Vector2f(-10,sr.getScaledHeight()/2)
				};
				
				Vector2f closestPoint1 = new Vector2f(0,0);
				float minDistanceSquared1 = Float.MAX_VALUE;
				for (Vector2f pose : centerPoses) {
				    float dx = pose.x - this.draggable.getX();
				    float dy = pose.y - this.draggable.getY();
				    float distanceSquared = dx * dx + dy * dy;
				    if (distanceSquared < minDistanceSquared1) {
				        minDistanceSquared1 = distanceSquared;
				        closestPoint1 = pose;
				    }
				}
				
				closestPoint = closestPoint1.y == sr.getScaledHeight()/2 ? closestPoint1.withY(this.draggable.getY()) : closestPoint1.withX(this.draggable.getX());
			}
			
			x = MathUtility.interpolate(closestPoint.x, this.draggable.getX(), this.showing.get());
			y = MathUtility.interpolate(closestPoint.y, this.draggable.getY(), this.showing.get());
			
			float maxWidth = 0;
			float xOff = 0;
			float yOff = 0;
			
			for (ItemBind item : items) {
				Rect rect = item.render(ms, x + xOff, y + yOff, true);
				
				height = rect.getHeight();
				
				xOff += rect.getWidth();
				
				maxWidth = Math.max(maxWidth, xOff);
				
				if (xOff / rect.getWidth() >= count.get() && rect.getWidth() != 0) {
					yOff += rect.getWidth()+5;
					xOff = 0;
				}
			}
			
			this.draggable.setWidth(Math.max(35, maxWidth-5));
			this.draggable.setHeight(Math.max(35, yOff-5));
		}
		
		if (event instanceof EventRender2D e) {
			MatrixStack ms = e.getMatrixStack();
			FontSize font = semibold.get(16);
			
			float xOff = 0;
			float yOff = 0;
			
			for (ItemBind item : items) {
				Rect rect = item.render(ms, x + xOff, y + yOff, false);
				
				xOff += rect.getWidth();

				if (xOff / rect.getWidth() >= count.get() && rect.getWidth() != 0) {
					yOff += rect.getWidth()+5;
					xOff = 0;
				}
			}
		}
		
		if (event instanceof EventKey e) {
			for (ItemBind item : items) {
				if (mc.currentScreen == null && Binds.getName(e.getKey() == e.getScancode() ? -1 : e.getKey(), e.getScancode()).equals(item.key.get())) {
					item.pressAnim.setForward(!e.isReleased());
					if (!mc.player.getCooldownTracker().hasCooldown(item.item)) {
						item.useTimer.reset();
					}
				}
			}
		}
	}
	
	@AllArgsConstructor
	class ItemBind {
		
		String name;
		Item item;
		Supplier<String> key;
		final Animation pressAnim = new Animation().setEasing(Easing.BOTH_CUBIC).setSpeed(300);
		final Animation cdAnim = new Animation().setEasing(Easing.BOTH_CUBIC).setSpeed(300);
		final InfinityAnimation widthAnim = new InfinityAnimation();
		final TimerUtility useTimer = new TimerUtility();
		
		Rect render(MatrixStack ms, float x, float y, boolean blur) {
			float size = 35;
			Rect rect = new Rect(x, y, size, size);
			FixColor[] circle = Interface.getCircle(showing.get());
			FontSize font = bold.get(14);
			float height = 10;
			String key = TextUtility.fixWidth(this.key.get(), font, size-10);
			
			cdAnim.setForward(mc.player.getCooldownTracker().hasCooldown(item));
			
			if (mc.player.getCooldownTracker().hasCooldown(item)) {
				CooldownTracker.Cooldown cooldown = mc.player.getCooldownTracker().cooldowns.get(item);
				if (cooldown != null) {
				    float totalTicks = (float)(cooldown.expireTicks - cooldown.createTicks);
				    float remainingTicks = (float)cooldown.expireTicks - mc.player.getCooldownTracker().ticks;
				    float seconds = remainingTicks / 20f;
				    String cd = TextUtility.formatNumberOld(seconds) + " s";
				    key = cd;
				}
			}

			if ((key.isEmpty()
					&& !(mc.currentScreen instanceof ChatScreen)
					|| !select.get(name).get())
					&& !(mc.currentScreen instanceof ChatScreen && items.indexOf(this) == 0 && select.getToggled().isEmpty()))
				return new Rect(x,y,0,size);

			float width = font.getWidth(key) + 8;
			float alpha = (blur ? 1 : 0.5f) * showing.get();

			if (!blur) Render.glow(ms, rect, showing.get());
			
			Round.draw(ms, rect, 4, rock.getThemes().getFirstColor().move(FixColor.WHITE, hover()).move(FixColor.RED, mc.player.getCooldownTracker().hasCooldown(item) && useTimer.passed(300) ? pressAnim.get()*0.2f : 0).alpha(alpha));
			
			if (showing.get() > 0.5f) {
				if (mc.player.getCooldownTracker().hasCooldown(item) && useTimer.passed(300)) {
					Render.initRotate(x+size/2F, y+size/2F, (float) Math.sin(Math.toRadians(pressAnim.get()*350))*20);
					Render.drawItem(item, x + size/4F-1, y + size/4F-1, 1.2f);
					Render.endRotate();
				} else {
					Render.scale(x+size/2F, y+size/2F, 1 - 0.3f * pressAnim.get());
					Render.drawItem(item, x + size/4F-1, y + size/4F-1, 1.2f);
					Render.end();
				}
			}
			
			if (!cdAnim.finished(false)) {
				Round.draw(ms, rect, 4, rock.getThemes().getFirstColor().alpha(0.3f*cdAnim.get()*showing.get()));

				float clockX = x + size/2F;
				float clockY = y + size/2F;
				float clockSize = 12 - 3 * pressAnim.get();
				Round.draw(ms, new Rect(clockX - clockSize/2, clockY - clockSize/2, clockSize, clockSize), clockSize / 2, rock.getThemes().getTextFirstColor().alpha(showing.get()*cdAnim.get()));
				
				// Гл хуйни ебал
				GL11.glEnable(GL11.GL_LINE_SMOOTH);
				GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
				GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
				GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST);
				
				int hours = Calendar.getInstance().getTime().getHours();
				if (hours > 12) hours -= 12;

				float factor = clockSize / 10F;

				// Часовая стрелка
				GL11.glPushMatrix();
				GL11.glTranslated(clockX, clockY, 0);
				GL11.glRotated(mc.player.ticksExisted/10F*30F - 180, 0, 0, 1);
				Round.draw(ms, new Rect(-0.62f * factor, -0.5f * factor, 1.25f * factor, 3f * factor), 0.62f * factor, rock.getThemes().getSecondColor().alpha(showing.get() * cdAnim.get()));
				GL11.glPopMatrix();

				// Минутная стрелка
				GL11.glPushMatrix();
				GL11.glTranslated(clockX, clockY, 0);
				GL11.glRotated(mc.player.ticksExisted*30F - 180, 0, 0, 1);
				Round.draw(ms, new Rect(-0.62f * factor, -0.5f * factor, 1.25f * factor, 4f * factor), 0.62f * factor, rock.getThemes().getFirstColor().alpha(showing.get() * cdAnim.get()));
				GL11.glPopMatrix();

				// Гл хуйни ебал х2, оффаем чтобы не багались остальные визуалы
				GL11.glDisable(GL11.GL_LINE_SMOOTH);
				GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
			}
			
			if (!blur) Render.outline(ms, rect, showing.get());
			
			if (!key.isBlank() && !blur) {
				FixColor c = Interface.outline() ? circle[2] : rock.getThemes().getFirstColor().alpha(showing.get());
				FixColor c1 = Interface.outline() ? circle[3] : rock.getThemes().getFirstColor().alpha(showing.get());
				
				widthAnim.animate(width, 50);
				
				Rect rect1 = new Rect(x + size/2F - widthAnim.get()/2F, y + size - height/2, widthAnim.get(), height);
				
				Render.glow(ms, rect1, showing.get());
				Round.draw(ms, rect1, 3, c, c1, c, c1);
				
				Stencil.init();
				Round.draw(ms, rect1, 3, FixColor.WHITE);
				Stencil.read(1);
				font.draw(ms, key, x + size/2F - font.getWidth(key)/2F - 0.5f, y + size - height/2, rock.getThemes().getTextFirstColor().alpha(showing.get()));
				Stencil.finish();
			}
			
			return rect.width(rect.getWidth()+5);
		}
		
	}
}
