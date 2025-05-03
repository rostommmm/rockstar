package fun.rockstarity.client.modules.render.ui;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.events.list.render.ui.shaders.EventBloom;
import fun.rockstarity.api.events.list.render.ui.shaders.EventBlur;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.helpers.render.Stencil;
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
 * @since 26 РёСЋР». 2024 Рі.
 */

public class Bps extends UIElement {
	
	private float x, y, leftWidth, rightWidth, width, height;
	
	private String speed;
	
	private final CheckBox kmh = new CheckBox(this, "РљРёР»РѕРјРµС‚СЂС‹ РІ С‡Р°СЃ");
	private final CheckBox yCheck = new CheckBox(this, "РЈС‡РёС‚С‹РІР°С‚СЊ Y");
	
	private final InfinityAnimation widthAnim = new InfinityAnimation();
	private final InfinityAnimation speedAnim = new InfinityAnimation();
	@NativeInclude
	public Bps(Interface ui, Select select) {
		super(select, "РЎРєРѕСЂРѕСЃС‚СЊ", new Rect(6, 58, 0, 0));
		this.set(true);
	}
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventBlur e) {
			MatrixStack ms = e.getMatrixStack();
			FontSize font = semibold.get(16);
			
			float hellModif = (mc.world.getDimensionType() == DimensionType.NETHER_TYPE ? 1/8f : 8);
			double motion = !yCheck.get()
					? Math.hypot(mc.player.getPosX() - mc.player.prevPosX, mc.player.getPosZ() - mc.player.prevPosZ)
					: Math.hypot(mc.player.getPosY() - mc.player.prevPosY, Math.hypot(mc.player.getPosX() - mc.player.prevPosX, mc.player.getPosZ() - mc.player.prevPosZ));
			
			speed = String.format("%.2f " + (kmh.get() ? "km/h" : "bps"), motion * (kmh.get() ? 20F * 3.6F : 20F));
			
			height = 20;
			width = 34 + font.getWidth(speed);
			
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
			rightWidth = 14 + font.getWidth(speed);
			
			if (blur())
				Round.draw(ms, new Rect(x + leftWidth, y - 0.5f, widthAnim.get(), height), 0, 4, 0, 4, rock.getThemes().getFirstColor().alpha(this.showing.get()));
			
			this.draggable.setWidth(width);
			this.draggable.setHeight(height);
		}
		
		if (event instanceof EventRender2D e) {
			MatrixStack ms = e.getMatrixStack();
			FontSize font = semibold.get(16);
			boolean isHell = mc.world.getDimensionType() == DimensionType.NETHER_TYPE;
			FixColor redColor = rock.getThemes().getFirstColor(); //rock.getThemes().getFirstColor().move(FixColor.RED, 0.2f);
			FixColor color = isHell ? redColor : rock.getThemes().getFirstColor();
			
			Render.glow(ms, new Rect(x, y, leftWidth + widthAnim.get(), height), showing.get());
			
			Round.draw(ms, new Rect(x, y, leftWidth, height), 4, 0, 4, 0, color.move(FixColor.WHITE, hover()).alpha(this.showing.get()));
			if (blur())
				Round.draw(ms, new Rect(x + leftWidth, y, widthAnim.get(), height), 0, 4, 0, 4, color.move(FixColor.WHITE, hover()).alpha(0.5f * this.showing.get()));
			else 
				Round.draw(ms, new Rect(x + leftWidth, y, widthAnim.get(), height), 0, 4, 0, 4, rock.getThemes().getSecondColor().move(FixColor.WHITE, hover()).alpha(this.showing.get()));
			
			//Render.image("icons/hud/speed.png", x + 5, y + 3.5f, 12, 12, rock.getThemes().getTextFirstColor().alpha(0.5f * this.showing.get()));
			Round.draw(ms, new Rect(x + 5, y + 4f, 12, 12), 6, rock.getThemes().getTextFirstColor().alpha(this.showing.get()));
			
			for (int i = 0; i < 5; i++) {
				float x = (float) (this.x + 10 + Math.cos(Math.toRadians(45 * i)) * 4);
				float y = (float) (this.y + 8.5f - Math.sin(Math.toRadians(45 * i)) * 4);
			//	Round.draw(ms, new Rect(x, y, 2, 2), 1, rock.getThemes().getFirstColor().alpha(this.showing.get()));
			}
			Round.draw(ms, new Rect(x + 9.5f, y + 10.5f, 3, 3), 1.5f, rock.getThemes().getFirstColor().alpha(this.showing.get()));
			
			GL11.glPushMatrix();
			// Р“Р» С…СѓР№РЅРё РµР±Р°Р»
			GL11.glEnable(GL11.GL_LINE_SMOOTH);
			GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
			GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
			GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST);
			
			GL11.glTranslated(x + 11, y + 12f, 0);
			GL11.glRotated(-60 + 120 * speedAnim.get(), 0, 0, 1);
			Round.draw(ms, new Rect(-0.7f, -6, 1.5f, 6), 0.75f, rock.getThemes().getFirstColor().alpha(this.showing.get()));
			
			// Р“Р» С…СѓР№РЅРё РµР±Р°Р» С…2, РѕС„С„Р°РµРј С‡С‚РѕР±С‹ РЅРµ Р±Р°РіР°Р»РёСЃСЊ РѕСЃС‚Р°Р»СЊРЅС‹Рµ РІРёР·СѓР°Р»С‹
			GL11.glDisable(GL11.GL_LINE_SMOOTH);
			GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
			
			GL11.glPopMatrix();

			Stencil.init();
			Round.draw(ms, new Rect(x + leftWidth, y, widthAnim.get(), height), 0, 4, 0, 4, color.alpha(this.showing.get()));
			Stencil.read(1);
			font.draw(ms, speed, x + 27, y + 4.5f, rock.getThemes().getTextFirstColor().alpha(this.showing.get()));
			Stencil.finish();
			
			Render.outline(ms, new Rect(x, y, leftWidth + widthAnim.get(), height), showing.get());
			
			speedAnim.animate(Math.min((float) (Math.hypot(mc.player.getPosX() - mc.player.prevPosX, mc.player.getPosZ() - mc.player.prevPosZ) * 20) / 10F, 1), 50);
			widthAnim.animate(12 + font.getWidth(speed), 50);
		}
	}
	
}
