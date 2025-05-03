package fun.rockstarity.client.modules.render.ui;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.events.list.render.ui.shaders.EventBlur;
import fun.rockstarity.api.helpers.math.Hover;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.helpers.render.Stencil;
import fun.rockstarity.api.helpers.system.TextUtility;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.animation.infinity.InfinityAnimation;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.render.shaders.list.Glow;
import fun.rockstarity.api.render.shaders.list.Outline;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.fonts.FontSize;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.client.modules.render.Interface;
import fun.rockstarity.client.modules.render.Interface.UIElement;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.world.DimensionType;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author ConeTin
 * @since 25 РёСЋР». 2024 Рі.
 */

public class Coordinates extends UIElement {
	
	private float x, y, leftWidth, rightWidth, width, height;
	
	private String cordsText, hellCordsText;
	
	private final CheckBox hellCords = new CheckBox(this, "РљРѕРѕСЂРґРёРЅР°С‚С‹ Р°РґР°");
	private final CheckBox copy = new CheckBox(this, "РљРѕРїРёСЂРѕРІР°РЅРёРµ РєРѕРѕСЂРґРёРЅР°С‚").set(true);
	
	private final Animation hellCordsAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);

	private final Animation hoverNickAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	private final Animation copiedAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	private boolean copied;
	private Rect cords;

	private final InfinityAnimation leftWidthAnim = new InfinityAnimation();
	private final InfinityAnimation rightWidthAnim = new InfinityAnimation();
	@NativeInclude
	public Coordinates(Interface ui, Select select) {
		super(select, "РљРѕРѕСЂРґРёРЅР°С‚С‹", new Rect(6, 33, 0, 0));
		this.set(true);
	}
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventBlur e) {
			MatrixStack ms = e.getMatrixStack();
			FontSize font = semibold.get(16);
			
			float hellModif = (mc.world.getDimensionType() == DimensionType.NETHER_TYPE ? 1/8f : 8);
			hellCordsText = String.format("%s %s %s", (int) (mc.player.getPosX() / hellModif), (int) (mc.player.getPosY()), (int) (mc.player.getPosZ() / hellModif));
			cordsText = String.format("%s %s %s", (int) mc.player.getPosX(), (int) mc.player.getPosY(), (int) mc.player.getPosZ());
			
			height = 20;
			width = 34 + font.getWidth(cordsText) + (this.hellCords.get() ? 16 + font.getWidth(hellCordsText) : 0);
			
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
			leftWidth = 22;
			rightWidth = 14 + font.getWidth(hellCordsText);
			
			if (blur())
				Round.draw(ms, new Rect(x + leftWidth, y - 0.5f, leftWidthAnim.get(), height), 0, 4, 0, 4, rock.getThemes().getFirstColor().alpha(this.showing.get()));
			
			this.hellCordsAnim.setForward(this.hellCords.get());
			
			if (!hellCordsAnim.finished(false) && blur()) {
				Round.draw(ms, new Rect(x + leftWidth + 15 + font.getWidth(cordsText), y - 0.5f, rightWidthAnim.get(), height), 4, rock.getThemes().getFirstColor().alpha(this.hellCordsAnim.get() * this.showing.get()));
			}
			
			this.draggable.setWidth(width);
			this.draggable.setHeight(height);
		}
		
		if (event instanceof EventRender2D e) {
			MatrixStack ms = e.getMatrixStack();
			FontSize font = semibold.get(16);
			boolean isHell = mc.world.getDimensionType() == DimensionType.NETHER_TYPE;
			FixColor redColor = rock.getThemes().getFirstColor(); //rock.getThemes().getFirstColor().move(FixColor.RED, 0.2f);
			FixColor color = isHell ? redColor : rock.getThemes().getFirstColor();
			
			if (glow()) {
				Render.glow(ms, new Rect(x, y, leftWidth + leftWidthAnim.get(), height), showing.get());
				
				if (!hellCordsAnim.finished(false)) {
					Render.glow(ms, new Rect(x + leftWidth + leftWidthAnim.get() + 3, y-0.5f, rightWidthAnim.get(), height), showing.get() * hellCordsAnim.get());
				}
			}
			
			Round.draw(ms, new Rect(x, y, leftWidth, height), 4, 0, 4, 0, color.move(FixColor.WHITE, hover()).alpha(this.showing.get()));
			if (blur())
				Round.draw(ms, new Rect(x + leftWidth, y, leftWidthAnim.get(), height), 0, 4, 0, 4, color.move(FixColor.WHITE, hover()).alpha(0.5f * this.showing.get()));
			else 
				Round.draw(ms, new Rect(x + leftWidth, y, leftWidthAnim.get(), height), 0, 4, 0, 4, rock.getThemes().getSecondColor().move(FixColor.WHITE, hover()).alpha(this.showing.get()));
			
			Render.image("icons/hud/coords.png", x + 5, y + 3.5f, 12, 12, rock.getThemes().getTextFirstColor().alpha(this.showing.get()));

			Stencil.init();
			Round.draw(ms, new Rect(x + leftWidth, y, leftWidthAnim.get(), height), 0, 4, 0, 4, color.alpha(this.showing.get()));
			Stencil.read(1);
			if (copied || !this.copiedAnim.finished(false)) {
				font.draw(ms, "РЎРєРѕРїРёСЂРѕРІР°РЅРѕ", x + 27, y + 4.5f - 12 + 12 * copiedAnim.get(), rock.getThemes().getTextFirstColor().alpha(showing.get() * copiedAnim.get()));
				font.draw(ms, cordsText, x + 27, y + 4.5f + 12 * copiedAnim.get(), rock.getThemes().getTextFirstColor().alpha(showing.get()));
			} else {
				cords = font.draw(ms, cordsText, x + 27, y + 4.5f, rock.getThemes().getTextFirstColor().alpha(showing.get()));
			}
			Stencil.finish();
			
			float coff = sr.getGuiScaleFactorF();
			boolean hover = Hover.isHovered(cords, mc.mouseHelper.getMouseX() / coff, mc.mouseHelper.getMouseY() / coff);
			if (cords != null && !hover) {
				copied = false;
			}
			
			hoverNickAnim.setForward(hover && copy.get());
			copiedAnim.setForward(copied);
			
			Stencil.init();
			Round.draw(ms, new Rect(x + leftWidth, y, leftWidthAnim.get(), height), 0, FixColor.RED);
			Stencil.read(1);
			Render.image("icons/copy.png", x + 30 + cords.getWidth(), y + 6 + 12 * copiedAnim.get(), 7, 7, rock.getThemes().getTextSecondColor().alpha(showing.get() * hoverNickAnim.get() * (1-copiedAnim.get())));
			Render.image("icons/yes.png", x + 31 + font.getWidth("РЎРєРѕРїРёСЂРѕРІР°РЅРѕ"), y + 6.5f - 12 + 12 * copiedAnim.get(), 7, 7, FixColor.GREEN.alpha(showing.get() * hoverNickAnim.get() * copiedAnim.get()));
			Stencil.finish();
			
			Render.outline(ms, new Rect(x, y, leftWidth + leftWidthAnim.get(), height), showing.get());
			
			if (!hellCordsAnim.finished(false)) {
				FixColor hellColor = isHell ? rock.getThemes().getFirstColor() : redColor;
				
				if (blur())
					Round.draw(ms, new Rect(x + leftWidth + leftWidthAnim.get() + 3, y, rightWidthAnim.get(), height), 4, hellColor.alpha(0.5f * this.hellCordsAnim.get() * this.showing.get()));
				else 
					Round.draw(ms, new Rect(x + leftWidth + leftWidthAnim.get() + 3, y, rightWidthAnim.get(), height), 4, rock.getThemes().getSecondColor().alpha(this.hellCordsAnim.get() * this.showing.get()));

				Stencil.init();
				Round.draw(ms, new Rect(x + leftWidth + leftWidthAnim.get() + 3, y, rightWidthAnim.get(), height), 4, hellColor.alpha(0.5f * this.hellCordsAnim.get() * this.showing.get()));
				Stencil.read(1);
				font.draw(ms, hellCordsText, x + leftWidth + 10 + leftWidthAnim.get(), y + 4.5f, rock.getThemes().getTextFirstColor().alpha(this.hellCordsAnim.get() * this.showing.get()));
				Stencil.finish();
				
				Render.outline(ms, new Rect(x + leftWidth + leftWidthAnim.get() + 3, y, rightWidthAnim.get(), height), showing.get() * hellCordsAnim.get());
			}
			
			leftWidthAnim.animate(12 + 10 * hoverNickAnim.get() + font.getWidth(copied ? "РЎРєРѕРїРёСЂРѕРІР°РЅРѕ" : cordsText), 50);
			rightWidthAnim.animate(font.getWidth(hellCordsText) + 14, 50);
		}
	}

	public void mouseClicked(double mouseX, double mouseY, int button) {
		if (cords == null || !get()) return;
		if (Hover.isHovered(cords, mouseX, mouseY) && copy.get()) {
			TextUtility.copyText(cordsText);
			copied = true;
		}
	}
}
