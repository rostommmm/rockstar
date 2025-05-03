package fun.rockstarity.client.modules.render.ui;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.ClientInfo;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.events.list.render.ui.shaders.EventBloom;
import fun.rockstarity.api.events.list.render.ui.shaders.EventBlur;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.Hover;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.helpers.render.Stencil;
import fun.rockstarity.api.helpers.system.TextUtility;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Select.Element;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.animation.infinity.InfinityAnimation;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.render.shaders.list.BlurNew;
import fun.rockstarity.api.render.shaders.list.Glass;
import fun.rockstarity.api.render.shaders.list.Glow;
import fun.rockstarity.api.render.shaders.list.Outline;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import fun.rockstarity.api.render.ui.fonts.FontSize;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.client.modules.render.Interface;
import fun.rockstarity.client.modules.render.Interface.UIElement;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;
import ru.kotopushka.j2c.sdk.annotations.VMProtect;
import ru.kotopushka.j2c.sdk.enums.VMProtectType;

/**
 * @author ConeTin
 * @since 19 мар. 2024 г.
 */
public class Watermark extends UIElement {
	
	private final Select information;
	
	private final WatermarkElement username, fps, server, time, uid, ping;
	
	private final InfinityAnimation fpsAnim = new InfinityAnimation();
	private final InfinityAnimation widthAnim = new InfinityAnimation();

	private final CheckBox shortName;
	
	private float x, y, leftWidth, width, height;
	
	private final SimpleDateFormat format = new SimpleDateFormat("HH:mm");
	private final CheckBox copy = new CheckBox(this, "Копирование айпи").set(true);

	private final Animation hoverIPAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	private final Animation copiedAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	private boolean copied;
	private Rect servIP;
	@NativeInclude
	public Watermark(Interface ui, Select select) {
		super(select, "Ватермарка", new Rect(6,6,0,0));
		information = new Select(this, "Информация").draggable(true).hide(() -> !this.get()).min(1).max(4);
		
		// Имя пользователя
		this.username = new WatermarkElement(information, "Имя") {
			public void render(MatrixStack ms, float x, float y) {
				FontSize font = semibold.get(16);
				float size = 12*secondAnim.get();
				
				Render.initRotate(x + 6, y + 11, thirdAnim.get() * 360F);
				rock.getUser().drawAvatar(ms, x + 6 - size/2, y + 11 - size/2, size, size/2F, showing.get() * showingAnim.get() * secondAnim.get());
				Render.endRotate();
				
				font.draw(ms, rock.getUser().getName(), x + 15, y + 5.5f, rock.getThemes().getTextFirstColor().alpha(showing.get() * showingAnim.get()));
			}
			
			public float getWidth() {
				return 21 + semibold.get(16).getWidth(rock.getUser().getName());
			}
		}.set(true);
		
		// Частота кадров
		this.fps = new WatermarkElement(information, "FPS") {
			private final InfinityAnimation fpsUpdate = new InfinityAnimation();
			
			public void render(MatrixStack ms, float x, float y) {
				FontSize font = semibold.get(16);
				
				Round.draw(ms, new Rect(x + 2 - 2 * thirdAnim.get(), y + 9 - 2 * thirdAnim.get(), 6, 6), 1, rock.getThemes().getTextFirstColor().alpha(showing.get() * showingAnim.get() * thirdAnim.get()));

				Round.drawOutlined(ms, new Rect(x + 1 + thirdAnim.get(), y + 8 + thirdAnim.get(), 6, 6), 1, 1, rock.getThemes().getTextFirstColor().alpha(showing.get() * showingAnim.get() * secondAnim.get()), rock.getThemes().getFirstColor().alpha(showing.get() * showingAnim.get() * thirdAnim.get()));
				
				font.draw(ms, (int) fpsUpdate.animate(mc.debugFPS, 50) + " fps", x + 11, y + 5.5f, rock.getThemes().getTextFirstColor().alpha(showing.get() * showingAnim.get()));
			}
			
			public float getWidth() {
				return 11 + semibold.get(16).getWidth((int) fpsUpdate.get() + " fps") + 6;
			}
		}.set(true);
		
		// Сервер
		this.server = new WatermarkElement(information, "Сервер") {
			public void render(MatrixStack ms, float x, float y) {
				FontSize font = semibold.get(16);
				
				Render.image("icons/hud/server.png", x + 0.5f * thirdAnim.get(), y + 6 - .5f * thirdAnim.get(), 10, 10, rock.getThemes().getTextFirstColor().alpha(showing.get() * showingAnim.get() * secondAnim.get() * (1-hoverIPAnim.get())));
				Render.image("icons/hud/server.png", x, y + 6.5f, 10, 10, rock.getThemes().getFirstColor().alpha(showing.get() * showingAnim.get() * secondAnim.get() * (1-hoverIPAnim.get())));
				Render.image("icons/hud/server.png", x - 0.5f, y + 6 + thirdAnim.get(), 10, 10, rock.getThemes().getTextFirstColor().alpha(showing.get() * showingAnim.get() * secondAnim.get() * (1-hoverIPAnim.get())));
				
				//Stencil.init();
				//Round.draw(ms, new Rect(x + 0.5f * thirdAnim.get(), y + 6 - .5f * thirdAnim.get(), 10, 10), 0, FixColor.RED);
				//Stencil.read(1);
				Render.image("icons/copy.png", x + 1.5f * thirdAnim.get(), y + 6 - .5f * thirdAnim.get() + 12 * copiedAnim.get()+2, 8, 8, rock.getThemes().getTextSecondColor().alpha(showing.get() * hoverIPAnim.get() * (1-copiedAnim.get())));
				Render.image("icons/yes.png", x + 0.5f * thirdAnim.get(), y + 6 - .5f * thirdAnim.get(), 10, 10, FixColor.GREEN.alpha(showing.get() * hoverIPAnim.get() * copiedAnim.get()));
				//Stencil.finish();
				
				x += 13;
				
				servIP = font.draw(ms, Server.getServerName(shortName.get()), x, y + 5.5f, rock.getThemes().getTextFirstColor().alpha(showing.get() * showingAnim.get()));
				servIP.setX(servIP.getX()-servIP.getWidth());
				
				float coff = sr.getGuiScaleFactorF();
				boolean hover = Hover.isHovered(servIP, mc.mouseHelper.getMouseX() / coff, mc.mouseHelper.getMouseY() / coff);
				if (servIP != null && !hover) {
					copied = false;
				}
				
				hoverIPAnim.setForward(hover && copy.get());
				copiedAnim.setForward(copied);
			}
			
			public float getWidth() {
				return 13 + semibold.get(16).getWidth(Server.getServerName(shortName.get())) + 6;
			}
		}.set(true);
		
		// Текущее время часы:минуты
		this.time = new WatermarkElement(information, "Время") {
			public void render(MatrixStack ms, float x, float y) {
				FontSize font = semibold.get(16);
				float size = 9 * secondAnim.get();
				Round.draw(ms, new Rect(x + 5 - size/2, y + 6.5f + 4.5f - size/2, size, size), size / 2, rock.getThemes().getTextFirstColor().alpha(showing.get() * showingAnim.get() * secondAnim.get()));
				
				// Гл хуйни ебал
				GL11.glEnable(GL11.GL_LINE_SMOOTH);
				GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
				GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
				GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST);
				
				int hours = Calendar.getInstance().getTime().getHours();
				if (hours > 12) hours -= 12;

				// Часовая стрелка
				GL11.glPushMatrix();
				GL11.glTranslated(x + 5, y + 11, 0);
				GL11.glRotated(hours / 12F * 360F * thirdAnim.get() - 180, 0, 0, 1);
				Round.draw(ms, new Rect(-0.62f, -0.5f, 1.25f, 3), 0.62f, rock.getThemes().getSecondColor().alpha(showing.get() * showingAnim.get()));
				GL11.glPopMatrix();
				
				// Минутная стрелка
				GL11.glPushMatrix();
				GL11.glTranslated(x + 5, y + 11, 0);
				GL11.glRotated(Calendar.getInstance().getTime().getMinutes() / 60F * 360F * thirdAnim.get() - 180, 0, 0, 1);
				Round.draw(ms, new Rect(-0.62f, -0.5f, 1.25f, 4), 0.62f, rock.getThemes().getFirstColor().alpha(showing.get() * showingAnim.get()));
				GL11.glPopMatrix();
				
				// Гл хуйни ебал х2, оффаем чтобы не багались остальные визуалы
				GL11.glDisable(GL11.GL_LINE_SMOOTH);
				GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
				
				// Остальное
				x += 13;
				font.draw(ms, format.format(Calendar.getInstance().getTime()), x, y + 5.5f, rock.getThemes().getTextFirstColor().alpha(showing.get() * showingAnim.get()));
			}
			
			public float getWidth() {
				return 13 + semibold.get(16).getWidth(format.format(Calendar.getInstance().getTime())) + 6;
			}
		}.set(true);
		
		// UID пользователя
		this.uid = new WatermarkElement(information, "UID") {
			private final Animation[] lineAnims = new Animation[7];
			
			public void render(MatrixStack ms, float x, float y) {
				FontSize font = semibold.get(16);
				for (int i = 0; i < 7; i++) {
					if (lineAnims[i] == null)
						lineAnims[i] = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
					lineAnims[i].setForward(i == 0 ? showingAnim.finished() : lineAnims[i-1].get() > 0.3f && lineAnims[i-1].isForward());
					Render.image("icons/hud/finger/line" + (i+1) + ".png", x, y + 6, 10, 10, rock.getThemes().getTextFirstColor().alpha(showing.get() * showingAnim.get() * lineAnims[i].get()));
				}
				x += 13;
				font.draw(ms, rock.getUser().getUid() + " uid", x, y + 5.5f, rock.getThemes().getTextFirstColor().alpha(showing.get() * showingAnim.get()));
			}
			
			public float getWidth() {
				return 13 + semibold.get(16).getWidth(rock.getUser().getUid() + " uid") + 6;
			}
		};
		
		// Ping пользователя
		this.ping = new WatermarkElement(information, "Пинг") {
			public void render(MatrixStack ms, float x, float y) {
				FontSize font = semibold.get(16);
				int ping = Server.ping();
				int[] pings = { 75, 150, 300, 450, 600, 5000 };
				float size = 4;
				
				for (int i = 0; i < 3; i++) {
					float alpha = MathHelper.clamp(thirdAnim.get() * 3f - i, 0, 1);
					Round.draw(ms, new Rect(x + 3.5f * i, y + 15 - size, 2.5f, size), 0.7f, rock.getThemes().getTextFirstColor().alpha((ping <= pings[4-i*2] || ping <= pings[5-i*2] && mc.player.ticksExisted % 20 <= 10 ? 0.5f + 0.5f * alpha : 0.5f) * showing.get() * showingAnim.get() * secondAnim.get()));
					size += 2;
				}
				
				x += 13;
				font.draw(ms, ping + " ms", x, y + 5.5f, rock.getThemes().getTextFirstColor().alpha(showing.get() * showingAnim.get()));
			}
			
			public float getWidth() {
				return 13 + semibold.get(16).getWidth(Server.ping() + " ms") + 6;
			}
		};
		
		this.shortName = new CheckBox(this, "Короткое имя сервера").desc("Сокращает имена сервера серверов (FunTime -> FT)").hide(() -> !this.server.get());
		this.set(true);
	}
	
	public void onEvent(Event event) {

		if (event instanceof EventBlur e) {
			MatrixStack ms = e.getMatrixStack();
			FontSize font = semibold.get(16);
			
			this.leftWidth = 23.5f + font.getWidth(ClientInfo.NAME.toString()) + 9;
			this.width = (float) (
					information.getElements()
					.stream()
					.mapToDouble(elmt -> ((WatermarkElement) elmt).getWidth() * ((WatermarkElement) elmt).getShowingAnim().get())
					.sum() + this.leftWidth + 8);
			this.height = 22;
			
			Vector2f[] poses = {
					new Vector2f(-10,-10),
					new Vector2f(sr.getScaledWidth()-width+10,-10),
					new Vector2f(sr.getScaledWidth()-width+10,sr.getScaledHeight()-height+10),
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
			
			this.x = MathUtility.interpolate(closestPoint.x, this.draggable.getX(), this.showing.get());
			this.y = MathUtility.interpolate(closestPoint.y, this.draggable.getY(), this.showing.get());
			
			if (blur())
				Round.draw(ms, new Rect(x, y, this.leftWidth, height+0.5f), 4, 0, 4, 0, FixColor.RED.alpha(showing.get()));
			
			//Round.draw(ms, new Rect(0, 100, sr.getScaledWidth(), sr.getScaledHeight()), 4, 0, 4, 0, FixColor.RED.alpha(showing.get()));
			
			//float offset = 50;
			//Glow.draw(ms, new Rect(100,100,100,100).size(-1), offset, 1, 10 + offset, FixColor.WHITE, FixColor.WHITE, FixColor.WHITE, FixColor.WHITE);
		}
		
		if (event instanceof EventRender2D e) {
			MatrixStack ms = e.getMatrixStack();
			FontSize font = semibold.get(16);
			
			float widthValue = Float.isNaN(this.widthAnim.get()) ? this.width : this.widthAnim.get();
			
			if (blur())
				Round.draw(ms, new Rect(x, y, this.leftWidth, height), 4, 0, 4, 0, rock.getThemes().getFirstColor().move(FixColor.WHITE, hover()).alpha(0.5f * showing.get()));
			else
				Round.draw(ms, new Rect(x, y, this.leftWidth, height), 4, 0, 4, 0, rock.getThemes().getSecondColor().move(FixColor.WHITE, hover()).alpha(showing.get()));
			
			Render.glow(ms, new Rect(x, y, widthValue, height), showing.get());

			// Правый рект
			Round.draw(ms, new Rect(x + this.leftWidth, y, widthValue - this.leftWidth, height), 0, 4, 0, 4, rock.getThemes().getFirstColor().move(FixColor.WHITE, hover()).alpha(showing.get()));
			
			FixColor[] circle = Interface.getCircle(showing.get());

			if (Interface.glow()) {
				FixColor[] glowCircle = Interface.getCircle(showing.get() * 0.5f);
	        	Render.drawImage(ms, "masks/particles/Glow.png", x, y - 3, 0, 28, 28, glowCircle[0], glowCircle[1], glowCircle[2], glowCircle[3]);
			}
 			Render.image(ms, "icons/hud/logo.png", x + 8, y + 5, 0, 12, 12, circle[0], circle[1], circle[2], circle[3]);
			
			font.draw(ms, ClientInfo.NAME.toString(), x + 23.5f, y + 5.5f, (rock.getThemes().getCurrent() == rock.getThemes().getDarkTheme() ? FixColor.WHITE : rock.getThemes().getTextFirstColor()).alpha(showing.get()));
			
			Stencil.init();
			Round.draw(ms, new Rect(x + this.leftWidth, y, widthValue - this.leftWidth, height), 0, 4, 0, 4, rock.getThemes().getFirstColor().alpha(showing.get()));
			Stencil.read(1);
			
			if (rock.isNewYear()) {
				if (width > 150) {
					Render.image("events/snow.png", x + leftWidth - 10, y - 15, 32, 32, rock.getThemes().getFirstColor().move(rock.getThemes().getTextFirstColor(), 0.05f).move(FixColor.WHITE, hover()).alpha(showing.get()));
					Render.image("events/snow.png", x + leftWidth + 21, y - 13, 22, 22, rock.getThemes().getFirstColor().move(rock.getThemes().getTextFirstColor(), 0.05f).move(FixColor.WHITE, hover()).alpha(showing.get()));
				}
				
				Render.image("events/snow.png", x + widthValue + 10 - 32, y - 15, 32, 32, rock.getThemes().getFirstColor().move(rock.getThemes().getTextFirstColor(), 0.05f).move(FixColor.WHITE, hover()).alpha(showing.get()));
				Render.image("events/snow.png", x + widthValue - 21 - 22, y - 13, 22, 22, rock.getThemes().getFirstColor().move(rock.getThemes().getTextFirstColor(), 0.05f).move(FixColor.WHITE, hover()).alpha(showing.get()));
			}
			
			float xOff = this.leftWidth + 6;
			
			for (Element uncasted : information.getElements()) {
				WatermarkElement elmt = (WatermarkElement) uncasted;
				
				elmt.getShowingAnim().setForward(elmt.get());
				
				if (!elmt.getShowingAnim().finished(false)) {
					elmt.getSecondAnim().setForward(elmt.getShowingAnim().get() >= 0.7f);
					elmt.getThirdAnim().setForward(elmt.getSecondAnim().finished());
					elmt.getXAnim().animate(xOff, 50);
					
					elmt.render(ms, x + elmt.getXAnim().get(), y);
				}

				xOff += elmt.getWidth() * elmt.getShowingAnim().get();
			}

			Stencil.finish();
			
			Render.outline(ms, new Rect(x, y, widthValue, height), showing.get());
			
			this.draggable.setWidth(width);
			this.draggable.setHeight(height);
			
			this.widthAnim.animate(width, 0);
		}
	}

	public void mouseClicked(double mouseX, double mouseY, int button) {
		if (servIP == null || !get()) return;
		if (Hover.isHovered(servIP, mouseX, mouseY) && copy.get()) {
			TextUtility.copyText(Server.getIP());
			rock.getAlertHandler().alert("Айпи сервера скопировано", AlertType.SUCCESS);
			copied = true;
		}
	}
	
	@Setter @Getter
	class WatermarkElement extends Select.Element {
		
		protected final InfinityAnimation xAnim = new InfinityAnimation();
		protected final Animation showingAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
		protected final Animation secondAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
		protected final Animation thirdAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(500);

		private String text;
		
		public WatermarkElement(Select parent, String name) {
			super(parent, name);
		}
		
		public WatermarkElement set(boolean val) {
			super.set(val);
			return this;
		}
		
		public void render(MatrixStack ms, float x, float y) {
			
		}
		
		public float getWidth() {
			return 0;
		}
		
	}
	
}
