package fun.rockstarity.client.modules.render.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.events.list.render.ui.shaders.EventBlur;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.Hover;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.helpers.render.Stencil;
import fun.rockstarity.api.helpers.system.TextUtility;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Mode.Element;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.animation.infinity.InfinityAnimation;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import fun.rockstarity.api.render.ui.fonts.FontSize;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.api.schedules.Schedule;
import fun.rockstarity.client.modules.render.Interface;
import fun.rockstarity.client.modules.render.Interface.UIElement;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ConnectingScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.renderer.texture.PotionSpriteUploader;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.vector.Vector2f;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Schedules extends UIElement {
	
	@NonFinal float x, y, width, height;
	
	@Getter
	List<Schedule> schedules = new ArrayList<>();
	
	CheckBox removeIfEmpty, sameWidth;

	Mode mode = new Mode(this, "Время");
	Element none, secs, small, big;

	@NonFinal
	boolean joinft;

	@NonFinal
	int anarchy;

	Animation emptyAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	Animation chatAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	final Slider count;

	InfinityAnimation widthAnim = new InfinityAnimation();
	@NativeInclude
	public Schedules(Interface ui, Select select) {
		super(select, "События", new Rect(106, 83, 0, 0));
		
		removeIfEmpty = new CheckBox(this, "Скрывать при отсутствии").set(true);
		sameWidth = new CheckBox(this, "Одна длина");
		
		none = new Mode.Element(mode, "Нет");
		secs = new Mode.Element(mode, "Секунды");
		small = new Mode.Element(mode, "Компактное").set();
		big = new Mode.Element(mode, "Большое");
		
		count = new Slider(this, "Кол-во событий").min(1).max(10).inc(1).set(5);
		
		set(true);
	}
	
	public void onEvent(Event event) {
		if (event instanceof EventBlur e) {
			MatrixStack ms = e.getMatrixStack();
			
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
			width = 70;
			height = 16;
			FontSize font = semibold.get(14);
			
			float offset = 2;
			float rectHeight = 15;
			float yOff = rectHeight + offset + 1;
			
			List<Schedule> schedules = new ArrayList<>(rock.getScheduleManager().getSchedules());
			
			Collections.sort(schedules, (a, b)-> {
				return Float.compare(a.getSeconds(), b.getSeconds());
			});
			
			synchronized(schedules) {
				Iterator i = schedules.iterator();
				while (i.hasNext()) {
					Schedule schedule = (Schedule) i.next();
					
					float iconSize = 10;
					float reversedAnim = 1 - schedule.getShowingAnim().get();
					String text = "an" + schedule.getAnarchy();
					if (none.get())
						text = "Анархия " + schedule.getAnarchy();
					int sec = schedule.getSeconds();
					String duration = getTime(schedule.getSeconds());
					
					float width = this.sameWidth.get() ? this.widthAnim.get() - (!none.get() ? font.getWidth(duration) + 12 : 0) : font.getWidth(text) + 10;
					
					// Правая часть
					if (blur())
						Round.draw(ms, new Rect(x, y + yOff - rectHeight * reversedAnim, width, rectHeight), 3, rock.getThemes().getFirstColor().alpha(schedule.getShowingAnim().get() * this.showing.get()));
					
					yOff += (rectHeight + offset) * schedule.getShowingAnim().get();
				}
			}
		}
		
		if (event instanceof EventRender2D e) {
			String name = getTitle() + " FT";
			
			MatrixStack ms = e.getMatrixStack();
			FontSize font = semibold.get(14);
			float rectHeight = 15;
			float offset = 2;
			float yOff = rectHeight + offset + 1;
			Schedule toRemove = null;
			
			List<EffectInstance> original = mc.player.getActivePotionEffects().stream().sorted(Comparator.comparing(EffectInstance::getDuration)).toList();
			
			float maxWidth = 0;
			
			PotionSpriteUploader potionspriteuploader = this.mc.getPotionSpriteUploader();
			
			
			List<Schedule> schedules = new ArrayList<>(rock.getScheduleManager().getSchedules());
			
			Collections.sort(schedules, (a, b)-> {
				return Float.compare(a.getSeconds(), b.getSeconds());
			});
			
			Render.glow(ms, new Rect(x, y, this.sameWidth.get() ? this.widthAnim.get() : font.getWidth(name) + 23, rectHeight), showing.get() * emptyAnim.get());
			
			synchronized(schedules) {
				Iterator i = schedules.iterator();
				while (i.hasNext()) {
					Schedule schedule = (Schedule) i.next();
					
					float iconSize = 10;
					float reversedAnim = 1 - schedule.getShowingAnim().get();
					String text = "an" + schedule.getAnarchy();
					if (none.get())
						text = "Анархия " + schedule.getAnarchy();
					int sec = schedule.getSeconds();
					String duration = getTime(schedule.getSeconds());
					
					float width = this.sameWidth.get() ? this.widthAnim.get() - (!none.get() ? font.getWidth(duration) + 12 : 0) : font.getWidth(text) + 10;
					
					// Анимки
					schedule.getShowingAnim().setForward(schedule.getSeconds() > 0 && schedules.indexOf(schedule) < count.get());
					schedule.getSecondAnim().setForward(schedule.getShowingAnim().get() < 0.5f);
					
					if (glow()) {
						Stencil.init();
						Round.draw(ms, new Rect(x - 0.25f, y + yOff - rectHeight * reversedAnim - 0.25f, width + 0.5f, rectHeight), 4, rock.getThemes().getSecondColor().alpha(showing.get()));
						Stencil.read(0);
						
						Render.glow(ms, new Rect(x - 0.25f, y + yOff - rectHeight * reversedAnim - 0.25f, width + 0.5f, rectHeight + 0.75f), showing.get() * schedule.getShowingAnim().get(), false);
						
						if (!none.get()) {
							Render.glow(ms, new Rect(x + width + 2, y + yOff - rectHeight * reversedAnim, font.getWidth(duration) + 10, rectHeight), showing.get() * schedule.getShowingAnim().get(), false);
						}
						
						Stencil.finish();
					}
					ms.push();
					ms.translate(0, 0, 9);
					
					// Делаем ему обрезание
					Stencil.init();
					Round.draw(ms, new Rect(x - 2, y + yOff - offset, width + 100, rectHeight + offset*2), 3, rock.getThemes().getFirstColor().alpha(schedule.getShowingAnim().get()));
					Stencil.read(1);
	
					FixColor backColor = rock.getThemes().getFirstColor();
	
					if (blur()) {
					    Round.draw(ms, new Rect(x, y + yOff - rectHeight * reversedAnim, width, rectHeight), 3, backColor.move(FixColor.WHITE, hover()).alpha(0.5f * schedule.getShowingAnim().get() * this.showing.get()));
					} else {
					    Round.draw(ms, new Rect(x, y + yOff - rectHeight * reversedAnim, width, rectHeight), 3, backColor.move(FixColor.WHITE, hover()).alpha(schedule.getShowingAnim().get() * this.showing.get()));
					}
	
					font.draw(ms, text, x + 5, y + 2.5f + yOff - rectHeight * reversedAnim, rock.getThemes().getTextFirstColor().alpha(schedule.getShowingAnim().get() * this.showing.get()));
					
					Render.outline(ms, new Rect(x, y + yOff - rectHeight * reversedAnim, width, rectHeight), showing.get() * schedule.getShowingAnim().get());
	
					Stencil.finish();
					
					if (!none.get()) {
						Round.draw(ms, new Rect(x + width + 2, y + yOff - rectHeight * reversedAnim, font.getWidth(duration) + 10, rectHeight), 3, rock.getThemes().getFirstColor().move(FixColor.WHITE, hover()).alpha(schedule.getShowingAnim().get() * this.showing.get()));
						font.draw(ms, duration, x + width + 6.5f, y + yOff - rectHeight * reversedAnim + 2.5f, rock.getThemes().getTextFirstColor().alpha(schedule.getShowingAnim().get() * this.showing.get()));
						if (schedule.getShowingAnim().get() > 0.5f)
							Render.outline(ms, new Rect(x + width + 2f, y + yOff - rectHeight * reversedAnim, font.getWidth(duration) + 10, rectHeight), showing.get() * schedule.getShowingAnim().get());
					}
					yOff += (rectHeight + offset) * schedule.getShowingAnim().get();
					
					maxWidth = Math.max(font.getWidth(text) + 10 + (!none.get() ? font.getWidth(duration) + 12 : 0), maxWidth);
					
					if (schedule.getShowingAnim().finished(false)) toRemove = schedule;

					ms.pop();
				}
			}
			
			rectHeight++;
			
			this.emptyAnim.setForward(mc.currentScreen instanceof ChatScreen || !schedules.isEmpty() || !this.removeIfEmpty.get());

			// Головка
			if (!this.emptyAnim.finished(false)) {
				Round.draw(ms, new Rect(x, y, this.sameWidth.get() ? this.widthAnim.get() : font.getWidth(name) + 23, rectHeight), 3, rock.getThemes().getFirstColor().move(FixColor.WHITE, hover()).alpha(this.showing.get() * this.emptyAnim.get()));
				Render.image("icons/hud/schedules.png", x + rectHeight/2 - 4, y + rectHeight/2 - 4.5f, 9, 9, rock.getThemes().getTextFirstColor().alpha(this.showing.get() * this.emptyAnim.get()));
				font.draw(ms, name, x + rectHeight/2 + 9, y + 3, rock.getThemes().getTextFirstColor().alpha(this.showing.get() * this.emptyAnim.get()));
				
				Render.outline(ms, new Rect(x - 0.25f, y, (this.sameWidth.get() ? this.widthAnim.get() : font.getWidth(name) + 23), rectHeight), showing.get() * emptyAnim.get());
				
				maxWidth = Math.max(font.getWidth(name) + 23, maxWidth);
			}
			
			chatAnim.setForward(mc.currentScreen instanceof ChatScreen);
			
			font.draw(ms, "ЛКМ - Перейти на анархию", x + 1, y + yOff, rock.getThemes().getTextFirstColor().alpha(this.showing.get() * this.emptyAnim.get() * chatAnim.get()));
			yOff += 8;
			font.draw(ms, "ПКМ - Скопировать анархию", x + 1, y + yOff, rock.getThemes().getTextFirstColor().alpha(this.showing.get() * this.emptyAnim.get() * chatAnim.get()));
			yOff += 8;
			
			if (this.sameWidth.get())
				this.widthAnim.animate(Math.max(schedules.isEmpty() ? 0 : 70, maxWidth), 50);
			
			if (toRemove != null) {
				this.schedules.remove(toRemove);
			}
			
			this.draggable.setWidth(maxWidth);
			this.draggable.setHeight(rectHeight);
		}
		if (joinft && event instanceof EventUpdate){
			rock.getAlertHandler().alert("Перехожу на анархию", AlertType.INFO);
			mc.player.sendChatMessage("/an" + anarchy);
			joinft = false;
		}
	}
	
	public void mouseClicked(double mouseX, double mouseY, int button) {
		FontSize font = semibold.get(14);
		float offset = 2;
		float rectHeight = 15;
		float yOff = rectHeight + offset + 1;
		
		List<Schedule> schedules = new ArrayList<>(rock.getScheduleManager().getSchedules());
		
		Collections.sort(schedules, (a, b)-> {
			return Float.compare(a.getSeconds(), b.getSeconds());
		});
		
		synchronized(schedules) {
			Iterator i = schedules.iterator();
			while (i.hasNext()) {
				Schedule schedule = (Schedule) i.next();
				if (schedules.indexOf(schedule) >= 5) continue;
				
				
				float iconSize = 10;
				float reversedAnim = 1 - schedule.getShowingAnim().get();
				String text = "an" + schedule.getAnarchy();
				if (none.get())
					text = "Анархия " + schedule.getAnarchy();
				int sec = schedule.getSeconds();
				String duration = getTime(schedule.getSeconds());
				
				float width = this.sameWidth.get() ? this.widthAnim.get() - (!none.get() ? font.getWidth(duration) + 12 : 0) : font.getWidth(text) + 10;
				
				if (Hover.isHovered(x, y + yOff - rectHeight * reversedAnim, width, rectHeight, mouseX, mouseY)) {
					if (button == 0) {
						if (!Server.isFT()){
							joinft = true;
							mc.world.sendQuittingDisconnectingPacket();
							mc.displayGuiScreen(new ConnectingScreen(new MainMenuScreen(), mc, "mc.funtime.su", 25565));
							anarchy = schedule.getAnarchy();
							return;
						}
						rock.getAlertHandler().alert("Перехожу на анархию", AlertType.INFO);
						mc.player.sendChatMessage("/an" + schedule.getAnarchy());
					} else if (button == 1) {
						rock.getAlertHandler().alert("Анархия скопирована", AlertType.INFO);
						TextUtility.copyText("/an" + schedule.getAnarchy());
					}
				}
				
				yOff += (rectHeight + offset) * schedule.getShowingAnim().get();
			}
		}
	}
	
	private String getTime(int time) {
		 if (secs.get()) {
			return time + " сек";
		} else if (small.get()) {
			String minutes = time / 60 + "";
	        String seconds = time % 60 + "";
	        
	        if (seconds.length() == 1)
	        	seconds = "0" + seconds;

			return minutes + ":" + seconds;
		} else if (big.get()) {
			String minutes = time / 60 == 0 ? "" : time / 60 + " мин ";
	        String seconds = time % 60 + " сeк";

			return minutes + seconds;
		}
		return "";
	}
	

	private String getTitle() {
		return rock.getModules().get(Interface.class).getEnglish().get() ? "Schedules" : getName();
	}
	
}
