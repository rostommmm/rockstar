package fun.rockstarity.client.modules.render.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.events.list.render.EventRender2D;
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
import fun.rockstarity.api.render.shaders.list.Glow;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import fun.rockstarity.api.render.ui.fonts.FontSize;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.client.modules.render.Interface;
import fun.rockstarity.client.modules.render.Interface.UIElement;
import lombok.Getter;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.renderer.texture.PotionSpriteUploader;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.math.vector.Vector2f;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;


public class Staffs extends UIElement {
	
	private float x, y, width, height;
	
	private List<String> lastStaffs = new ArrayList<>();
	private List<String> lastTab = new ArrayList<>();
	@Getter
	private List<String> staffs = new ArrayList<>();
	private List<String> notVanish = new ArrayList<>();
	private List<String> tab = new ArrayList<>();
	
	private final CheckBox removeIfEmpty, sameWidth;
	
	//private final Select alert = new Select(this, "Уведомлять о..");
	//private final Select.Element join = new Select.Element(alert, "Заходе");
	//private final Select.Element spec = new Select.Element(alert, "Спеке").set(true);
	
	private final Animation emptyAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	
	private final InfinityAnimation widthAnim = new InfinityAnimation();
	@NativeInclude
	public Staffs(Interface ui, Select select) {
		super(select, "Персонал", new Rect(6, 83, 0, 0));
		
		removeIfEmpty = new CheckBox(this, "Скрывать при отсутствии").set(true);
		sameWidth = new CheckBox(this, "Одна длина");
		set(true);
	}
	
	public void onEvent(Event event) {
		if (event instanceof EventUpdate) {
			//List<String> newStaffs = getVanishedStaff();
			
//			if (join.get()) {
//				for (String staff : newStaffs) {
//					if (!staffs.contains(staff)) {
//						rock.getAlertHandler().alert(staff + " зашёл на сервер", AlertType.INFO);
//					}
//				}
//			}
			
			//staffs = newStaffs;
			updateVanished();
			
		    for (String staff : staffs) {
		        if (!lastStaffs.contains(staff)) {
		            rock.getAlertHandler().alert(staff + " зашёл на сервер!", AlertType.INFO);
		        }
		    }
		    
		    for (String staff : tab) {
		        if (!lastTab.contains(staff)) {
		            rock.getAlertHandler().alert(staff + " зашёл в спектаторы!", AlertType.ERROR);
		        }
		    }
		    
		    lastStaffs = new ArrayList<>(staffs);
		    lastTab = new ArrayList<>(tab);
		}
		
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
			
			for (String staff : staffs) {
				float leftSize = 16;
				float iconSize = 10;
				
				float width = this.sameWidth.get() ? this.widthAnim.get() : font.getWidth(staff) + 10 + leftSize;
				
				// Правая часть
				if (blur())
					Round.draw(ms, new Rect(x + leftSize, y + yOff, width - leftSize, rectHeight), 0, 3, 0, 3, rock.getThemes().getFirstColor().alpha(showing.get()));
				
				yOff += (rectHeight + offset);
			}
		}
		
		if (event instanceof EventRender2D e) {
			MatrixStack ms = e.getMatrixStack();
			FontSize font = semibold.get(14);
			float rectHeight = 15;
			float offset = 2;
			float yOff = rectHeight + offset + 1;
			
			float maxWidth = 0;
			
			PotionSpriteUploader potionspriteuploader = this.mc.getPotionSpriteUploader();
			
			Render.glow(ms, new Rect(x, y, this.sameWidth.get() ? this.widthAnim.get() : font.getWidth(getTitle()) + 23, rectHeight), showing.get() * emptyAnim.get());
			
			tab.clear();
			
			for (NetworkPlayerInfo player : mc.player.connection.getPlayerInfoMap()) {
			    if (mc.isSingleplayer() || player.getPlayerTeam() == null) break;
			    
		        String name = Arrays.asList(player.getPlayerTeam().getMembershipCollection().toArray()).toString().replace("[", "").replace("]", "");
		        String prefix = player.getPlayerTeam().getPrefix().getString().replace("⚡ ", "");
				
				if (isPrefixValid(prefix))
			        tab.add((prefix + name).trim());
			}
			
//			if (spec.get()) {
//				for (String staff : tab) {
//					if (!notVanish.contains(staff)) {
//						rock.getAlertHandler().alert(staff + " зашёл в спек", AlertType.ERROR);
//					}
//				}
//			}
			
			//notVanish = new ArrayList<>(tab);

			for (String staff : staffs) {
				staff = staff.trim();
				
				float leftSize = 16;
				float iconSize = 10;
				
				float width = this.sameWidth.get() ? this.widthAnim.get(): font.getWidth(staff) + 10 + leftSize;
				
				if (glow()) {
					Stencil.init();
					if (staffs.indexOf(staff) != 0) 
						Round.draw(ms, new Rect(x - 2, y + yOff - rectHeight - 4, width + 100, rectHeight), 4, rock.getThemes().getSecondColor().alpha(showing.get()));
					Round.draw(ms, new Rect(x - 0.25f, y + yOff, width + 0.5f, rectHeight), 4, rock.getThemes().getSecondColor().alpha(showing.get()));
					Stencil.read(0);
					
					Render.glow(ms, new Rect(x - 0.25f, y + yOff, width + 0.5f, rectHeight + 0.75f), showing.get() , false);
					
					Stencil.finish();
				}
				
				
				// Делаем ему обрезание
				Stencil.init();
				Round.draw(ms, new Rect(x - 2, y + yOff - offset, width + 100, rectHeight + offset*2), 3, rock.getThemes().getFirstColor());
				Stencil.read(1);
				
				// Левая часть
				Round.draw(ms, new Rect(x, y + yOff, leftSize, rectHeight), 3, 0, 3, 0, rock.getThemes().getFirstColor().move(FixColor.WHITE, hover()).alpha(showing.get()));
				
				
				FixColor backColor = rock.getThemes().getFirstColor();

				if (blur()) {
				    Round.draw(ms, new Rect(x + leftSize, y + yOff, width - leftSize, rectHeight), 0, 3, 0, 3, backColor.move(FixColor.WHITE, hover()).alpha(0.5f  * showing.get()));
				} else {
					Round.draw(ms, new Rect(x + leftSize, y + yOff, width - leftSize, rectHeight), 0, 3, 0, 3, backColor.move(FixColor.WHITE, hover()).alpha(showing.get()));
				}
				
				FixColor color = (tab.contains(staff) ? FixColor.GREEN : FixColor.RED).alpha(showing.get());
				Glow.draw(ms, new Rect(x + 6, y + yOff + 5, 5, 5), 10, 0.5f, 5, color, color, color, color);
				Round.draw(ms, new Rect(x + 6, y + yOff + 5, 5, 5), 2.5f, color);
				

				font.draw(ms, staff, x + leftSize + 4, y + 2.5f + yOff, rock.getThemes().getTextFirstColor().alpha(showing.get()));
				
				Render.outline(ms, new Rect(x, y + yOff, width, rectHeight), showing.get() );

				Stencil.finish();
				
				yOff += (rectHeight + offset) ;
				
				maxWidth = Math.max(font.getWidth(staff) + 10 + leftSize, maxWidth);
			}
			
			rectHeight++;
			
			this.emptyAnim.setForward(mc.currentScreen instanceof ChatScreen || !staffs.isEmpty() || !this.removeIfEmpty.get());

			// Головка
			if (!this.emptyAnim.finished(false)) {
				Round.draw(ms, new Rect(x, y, this.sameWidth.get() ? this.widthAnim.get() : font.getWidth(getTitle()) + 23, rectHeight), 3, rock.getThemes().getFirstColor().move(FixColor.WHITE, hover()).alpha(this.showing.get() * this.emptyAnim.get()));
				Render.image("icons/hud/staff.png", x + rectHeight/2 - 4, y + rectHeight/2 - 4.5f, 9, 9, rock.getThemes().getTextFirstColor().alpha(this.showing.get() * this.emptyAnim.get()));
				font.draw(ms, getTitle(), x + rectHeight/2 + 9, y + 3, rock.getThemes().getTextFirstColor().alpha(this.showing.get() * this.emptyAnim.get()));
				
				Render.outline(ms, new Rect(x - 0.25f, y, (this.sameWidth.get() ? this.widthAnim.get() : font.getWidth(getTitle()) + 23), rectHeight), showing.get() * emptyAnim.get());
				
				maxWidth = Math.max(font.getWidth(getTitle()) + 23, maxWidth);
			}
			
			if (this.sameWidth.get())
				this.widthAnim.animate(Math.max(staffs.isEmpty() ? 0 : 70, maxWidth), 50);
			
			this.draggable.setWidth(maxWidth);
			this.draggable.setHeight(yOff);
		}
	}
	
	
	public void updateVanished() {
		staffs.clear();
		
		for (ScorePlayerTeam s : mc.world.getScoreboard().getTeams()) {
			if (mc.isSingleplayer())
				continue;

		    String name = Arrays.asList(s.getMembershipCollection().toArray()).toString().replace("[", "").replace("]", "");
			String prefix = s.getPrefix().getString().replace("⚡ ", "");
			
			if (isPrefixValid(prefix) || name.equals("ZipinJZ"))
				staffs.add((prefix + name).trim());
		}
	}
	

	private String getTitle() {
		return rock.getModules().get(Interface.class).getEnglish().get() ? "Staff List" : getName();
	}

	public boolean isPrefixValid(String prefix) {
		//System.out.println(prefix);
	return // самые популярные префиксы
			(prefix.toLowerCase().contains("ʜᴇʟᴘᴇʀ") ||
					prefix.toLowerCase().contains("developer") ||
					prefix.toLowerCase().contains("moder") ||
					prefix.toLowerCase().contains("admin") ||
					prefix.toLowerCase().contains("st helper") ||
					prefix.toLowerCase().contains("d helper") ||
					prefix.toLowerCase().contains("helper") ||
					prefix.toLowerCase().contains("owner") ||
					prefix.toLowerCase().contains("staff") ||
					prefix.toLowerCase().contains("хелпер") ||
					prefix.toLowerCase().contains("wne") ||
					prefix.toLowerCase().contains("own") ||
					prefix.toLowerCase().contains("supp") ||
					prefix.toLowerCase().contains("yt") ||
					prefix.toLowerCase().contains("youtu") ||
					prefix.toLowerCase().contains("tiktok") ||
					prefix.toLowerCase().contains("ст. модер") ||
					prefix.toLowerCase().contains("ст. сотрудник") ||
					prefix.toLowerCase().contains("ст. стажер") ||
					prefix.toLowerCase().contains("гл. модер") ||
					prefix.toLowerCase().contains("гл. админ") ||
					prefix.toLowerCase().contains("ст.модер") ||
					prefix.toLowerCase().contains("ст.сотрудник") ||
					prefix.toLowerCase().contains("ст.стажер") ||
					prefix.toLowerCase().contains("гл.модер") ||
					prefix.toLowerCase().contains("гл.админ") ||
					prefix.toLowerCase().contains("ст. модёр") ||
					prefix.toLowerCase().contains("ст. стажёр") ||
					prefix.toLowerCase().contains("гл. модёр") ||
					prefix.toLowerCase().contains("модер") ||
					prefix.toLowerCase().contains("мoдeр") ||
					prefix.toLowerCase().contains("стажер") ||
					prefix.toLowerCase().contains("стажёр") ||
					prefix.toLowerCase().contains("админ") ||
					prefix.toLowerCase().contains("сотрудник") ||
					prefix.toLowerCase().contains("куратор") ||
					prefix.toLowerCase().contains("мл.сотрудник") ||
					prefix.toLowerCase().contains("поддержка") ||
					//  prefix.toLowerCase().contains("сквид") ||
					prefix.toLowerCase().contains("модератор"))
					&& !prefix.toLowerCase().contains("d.helper")
					&& !prefix.toLowerCase().contains("ранг")
					&& !prefix.toLowerCase().contains("статус");
	}
	
}
