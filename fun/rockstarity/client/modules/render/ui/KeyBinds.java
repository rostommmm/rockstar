package fun.rockstarity.client.modules.render.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.binds.Bind;
import fun.rockstarity.api.binds.BindInfo;
import fun.rockstarity.api.binds.BindInfo.Type;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.events.list.render.ui.shaders.EventBloom;
import fun.rockstarity.api.events.list.render.ui.shaders.EventBlur;
import fun.rockstarity.api.helpers.game.Binds;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.helpers.render.Stencil;
import fun.rockstarity.api.helpers.system.TextUtility;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.Setting;
import fun.rockstarity.api.modules.settings.list.Binding;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Select.Element;
import fun.rockstarity.api.modules.settings.list.Slider;
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
import lombok.Getter;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.util.math.vector.Vector2f;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author Malecharik
 * @since 3 РёСЋРЅ. 2024 Рі. 18:53:27
 */
public class KeyBinds extends UIElement {
	private float x, y, width, height;
	
	@Getter
	private final Map<String, BindInfo> keyBinds = new TreeMap<>();
	
	private final ArrayList<BindInfo> queue = new ArrayList<>();
	
	private final CheckBox removeIfEmpty, showBinds, sameWidth;
	
	private final Animation emptyAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	
	private final InfinityAnimation widthAnim = new InfinityAnimation();
	@NativeInclude
	public KeyBinds(Interface ui, Select select) {
		super(select, "РҐРѕС‚РєРµРё", new Rect(110, 83, 0, 0));
		
		this.removeIfEmpty = new CheckBox(this, "РЎРєСЂС‹РІР°С‚СЊ РµСЃР»Рё РїСѓСЃС‚").set(true);
		this.showBinds = new CheckBox(this, "РљР»Р°РІРёС€Рё");
		this.sameWidth = new CheckBox(this, "РћРґРЅР° РґР»РёРЅР°");
		
		this.set(true);
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
			width = this.sameWidth.get() ? this.widthAnim.get() : semibold.get(14).getWidth(getTitle()) + 23;
			height = 16;
			FontSize font = semibold.get(14);
			
			float offset = 2;
			float rectHeight = 15;
			float yOff = rectHeight + offset + 1;
			
			List<Bind> binds = getBindList();

			Collections.sort(binds, (a, b)-> Float.compare(font.getWidth(a.getText() + (this.showBinds.get() ? a.getKey() : "")), font.getWidth(b.getText()+ (this.showBinds.get() ? b.getKey() : ""))));
			Collections.reverse(binds);
			
			if (blur())
			for (Bind bind : binds) {
				float leftSize = getLeftWidth(bind);
				float reversedAnim = 1 - bind.getShowingAnim().get();
				String text = bind.getText();
				float width = this.sameWidth.get() ? this.widthAnim.get() - (this.showBinds.get() ? font.getWidth(Binds.getName(bind.getKey(), bind.getScancode())) + 12 : 0) : font.getWidth(text) + 10 + leftSize;

				Round.draw(ms, new Rect(x + leftSize, y + yOff - rectHeight * reversedAnim, width - leftSize, rectHeight), 3, rock.getThemes().getFirstColor().alpha(showing.get() * bind.getShowingAnim().get()));
				
				yOff += (rectHeight + offset) * bind.getShowingAnim().get();
			}
			
			this.draggable.setWidth(width);
			this.draggable.setHeight(height);
		}
		
		if (event instanceof EventRender2D e) {
			MatrixStack ms = e.getMatrixStack();
			FontSize font = semibold.get(14);
			float rectHeight = 15;
			float offset = 2;
			float yOff = rectHeight + offset + 1;
			
			List<Bind> binds = getBindList();
			
			float maxWidth = 0;
			
			Collections.sort(binds, (a, b)-> Float.compare(font.getWidth(a.getText() + (this.showBinds.get() ? a.getKey() : "")), font.getWidth(b.getText()+ (this.showBinds.get() ? b.getKey() : ""))));
			Collections.reverse(binds);
			
			for (Bind bind : binds) {
				String text1 = "";
				
				if (bind.getParent() instanceof CheckBox check) {
					text1 = check.getName();
				} else if (bind.getParent() instanceof Module mod) {
					text1 = mod.getInfo().name();
				} else if (bind.getParent() instanceof Slider slider) {
					text1 = slider.getName();
				} else if (bind.getParent() instanceof Mode mode) {
					text1 = mode.getName();
				}
				
				bind.setText(text1);
				
				float leftSize = getLeftWidth(bind);
				float iconSize = 10;
				float reversedAnim = 1 - bind.getShowingAnim().get();
				String text = bind.getText();
				float width = this.sameWidth.get() ? this.widthAnim.get() - (this.showBinds.get() ? font.getWidth(Binds.getName(bind.getKey(), bind.getScancode())) + 12 : 0) : font.getWidth(text) + 10 + leftSize;
				Type type = getType(bind);
				
				// РђРЅРёРјРєРё
				bind.getSecondAnim().setForward(!(type == Type.ENABLE || type == Type.DISABLE ? bind.getShowingAnim().finished() : bind.getShowingAnim().get() > 0.5f));
				
				Float secanim = bind.getSecondAnim().get();
				
				if (glow()) {
					Stencil.init();
					Round.draw(ms, new Rect(x - 0.25f, y + yOff - rectHeight * reversedAnim - 0.25f, width + 0.5f, rectHeight), 4, rock.getThemes().getSecondColor().alpha(showing.get()));
					Stencil.read(0);
					
					float glowOffset = 5;
					Render.glow(ms, new Rect(x - 0.25f, y + yOff - rectHeight * reversedAnim - 0.25f, width + 0.5f, rectHeight + 0.75f), showing.get() * bind.getShowingAnim().get(), false);
					
					if (showBinds.get()) {
						Render.glow(ms, new Rect(x + width + 2, y + yOff - rectHeight * reversedAnim-0.5f, font.getWidth(Binds.getName(bind.getKey(), bind.getScancode())) + 10, rectHeight), showing.get() * bind.getShowingAnim().get(), false);
					}
					
					Stencil.finish();
					
				}
				ms.push();
				ms.translate(0, 0, 9);
				
				// Р”РµР»Р°РµРј РµРјСѓ РѕР±СЂРµР·Р°РЅРёРµ
				Stencil.init();
				Round.draw(ms, new Rect(x-2, y + yOff - offset, width + 100, rectHeight + offset*2), 3, rock.getThemes().getFirstColor().alpha(bind.getShowingAnim().get()));
				Stencil.read(1);
				
				// Р›РµРІР°СЏ С‡Р°СЃС‚СЊ
				//Render.image("icons/hud/off.png", x + leftSize / 2 - iconSize / 2, y + yOff + 16 / 2 - iconSize / 2 - rectHeight * reversedAnim, iconSize, iconSize, rock.getThemes().getTextFirstColor().alpha(0.5f * bind.getShowingAnim().get()));
				
				switch (type) {
				case ENABLE:
					Round.draw(ms, new Rect(x, y + yOff - rectHeight * reversedAnim, leftSize, rectHeight), 3, 0, 3, 0, rock.getThemes().getFirstColor().move(FixColor.WHITE, hover()).alpha(bind.getShowingAnim().get() * this.showing.get()));
					yOff -= 0.5f;
					Round.draw(ms, new Rect(x + leftSize / 2 - iconSize / 2, y + yOff + 16 / 2 - iconSize / 2 - rectHeight * reversedAnim + 2, iconSize, iconSize - 4), iconSize / 2 - 2, rock.getThemes().getTextFirstColor().alpha(bind.getShowingAnim().get() * this.showing.get()));
					Round.draw(ms, new Rect(x + leftSize / 2 - iconSize / 2 + 0.5f, y + yOff + 16 / 2 - iconSize / 2 - rectHeight * reversedAnim + 2.5f, iconSize - 1, iconSize - 5), iconSize / 2 - 3, rock.getThemes().getFirstColor().alpha(bind.getShowingAnim().get() * this.showing.get()));

					Round.draw(ms, new Rect(x + leftSize / 2 - iconSize / 2, y + yOff + 16 / 2 - iconSize / 2 - rectHeight * reversedAnim + 2, iconSize, iconSize - 4), iconSize / 2 - 2, rock.getThemes().getTextFirstColor().alpha(bind.getShowingAnim().get() * (1-secanim) * this.showing.get()));
					Round.draw(ms, new Rect(x + leftSize / 2 - iconSize / 2 + 1.5f + 4 * (1-secanim), y + yOff + 16 / 2 - iconSize / 2 - rectHeight * reversedAnim + 3.5f, 3, 3), 1.5f, rock.getThemes().getTextFirstColor().move(rock.getThemes().getFirstColor(), (1-secanim)).alpha(bind.getShowingAnim().get() * this.showing.get()));
					yOff += 0.5f;
					break;
					
				case DISABLE:
					Round.draw(ms, new Rect(x, y + yOff - rectHeight * reversedAnim, leftSize, rectHeight), 3, 0, 3, 0, rock.getThemes().getFirstColor().move(FixColor.WHITE, hover()).alpha(bind.getShowingAnim().get() * this.showing.get()));
					yOff -= 0.5f;
					Round.draw(ms, new Rect(x + leftSize / 2 - iconSize / 2, y + yOff + 16 / 2 - iconSize / 2 - rectHeight * reversedAnim + 2, iconSize, iconSize - 4), iconSize / 2 - 2, rock.getThemes().getTextFirstColor().alpha(bind.getShowingAnim().get() * this.showing.get()));
					Round.draw(ms, new Rect(x + leftSize / 2 - iconSize / 2 + 0.5f, y + yOff + 16 / 2 - iconSize / 2 - rectHeight * reversedAnim + 2.5f, iconSize - 1, iconSize - 5), iconSize / 2 - 3, rock.getThemes().getFirstColor().alpha(bind.getShowingAnim().get() * this.showing.get()));

					Round.draw(ms, new Rect(x + leftSize / 2 - iconSize / 2, y + yOff + 16 / 2 - iconSize / 2 - rectHeight * reversedAnim + 2, iconSize, iconSize - 4), iconSize / 2 - 2, rock.getThemes().getTextFirstColor().alpha(bind.getShowingAnim().get() * secanim * this.showing.get()));
					Round.draw(ms, new Rect(x + leftSize / 2 - iconSize / 2 + 1.5f + 4 * secanim, y + yOff + 16 / 2 - iconSize / 2 - rectHeight * reversedAnim + 3.5f, 3, 3), 1.5f, rock.getThemes().getTextFirstColor().move(rock.getThemes().getFirstColor(), secanim).alpha(bind.getShowingAnim().get() * this.showing.get()));
					yOff += 0.5f;
					break;

				case SLIDER:
					Round.draw(ms, new Rect(x, y + yOff - rectHeight * reversedAnim, leftSize, rectHeight), 3, 0, 3, 0, rock.getThemes().getFirstColor().move(FixColor.WHITE, hover()).alpha(bind.getShowingAnim().get() * this.showing.get()));
					if (bind.getShowingAnim().get() > 0.5f)
						font.draw(ms, getValue(bind), x + 4 - 4 * secanim, y + 2.5f + yOff - rectHeight * reversedAnim, rock.getThemes().getTextFirstColor().alpha(bind.getShowingAnim().get() * (1-secanim) * this.showing.get()));
					
				case MODE:
					Round.draw(ms, new Rect(x, y + yOff - rectHeight * reversedAnim, leftSize, rectHeight), 3, 0, 3, 0, rock.getThemes().getFirstColor().move(FixColor.WHITE, hover()).alpha(bind.getShowingAnim().get() * this.showing.get()));
					if (bind.getShowingAnim().get() > 0.5f)
						font.draw(ms, getValue(bind), x + 4 - 4 * secanim, y + 2.5f + yOff - rectHeight * reversedAnim, rock.getThemes().getTextFirstColor().alpha(bind.getShowingAnim().get() * (1-secanim) * this.showing.get()));
					
					break;

				default:
					break;
				}
				
				// РџСЂР°РІР°СЏ С‡Р°СЃС‚СЊ
				if (blur())
					Round.draw(ms, new Rect(x + leftSize, y + yOff - rectHeight * reversedAnim, width - leftSize, rectHeight), 0, 3, 0, 3, rock.getThemes().getFirstColor().move(FixColor.WHITE, hover()).alpha(0.5f * bind.getShowingAnim().get() * this.showing.get()));
				else
					Round.draw(ms, new Rect(x + leftSize, y + yOff - rectHeight * reversedAnim, width - leftSize, rectHeight), 0, 3, 0, 3, rock.getThemes().getSecondColor().move(FixColor.WHITE, hover()).alpha(bind.getShowingAnim().get() * this.showing.get()));

				font.draw(ms, text, x + leftSize + 4, y + 2.5f + yOff - rectHeight * reversedAnim, rock.getThemes().getTextFirstColor().alpha(bind.getShowingAnim().get() * this.showing.get()));
				
				// Р‘РёРЅРґ
				if (this.showBinds.get()) {
					Round.draw(ms, new Rect(x + width + 2, y + yOff - rectHeight * reversedAnim, font.getWidth(Binds.getName(bind.getKey(), bind.getScancode())) + 10, rectHeight), 3, rock.getThemes().getFirstColor().move(FixColor.WHITE, hover()).alpha(bind.getShowingAnim().get() * this.showing.get()));
					font.draw(ms, Binds.getName(bind.getKey(), bind.getScancode()), x + width + 6.5f, y + yOff - rectHeight * reversedAnim + 2.5f, rock.getThemes().getTextFirstColor().alpha(bind.getShowingAnim().get() * this.showing.get()));
					if (bind.getShowingAnim().get() > 0.5f)
						Render.outline(ms, new Rect(x + width + 2f, y + yOff - rectHeight * reversedAnim, font.getWidth(Binds.getName(bind.getKey(), bind.getScancode())) + 10, rectHeight), showing.get() * bind.getShowingAnim().get());
				}
				
				Render.outline(ms, new Rect(x, y + yOff - rectHeight * reversedAnim, width, rectHeight), showing.get() * bind.getShowingAnim().get());
				Stencil.finish();

				ms.pop();
				yOff += (rectHeight + offset) * bind.getShowingAnim().get();
				
				maxWidth = Math.max(font.getWidth(text) + 10 + leftSize + (this.showBinds.get() ? font.getWidth(Binds.getName(bind.getKey(), bind.getScancode())) + 12 : 0), maxWidth);
			}
			
			rectHeight++;
			
			this.emptyAnim.setForward(mc.currentScreen instanceof ChatScreen || !binds.isEmpty() || !this.removeIfEmpty.get());
			// Р“РѕР»РѕРІРєР°
			if (!this.emptyAnim.finished(false)) {
				Render.glow(ms, new Rect(x - 0.25f, y - 0.25f, (this.sameWidth.get() ? this.widthAnim.get() : font.getWidth(getTitle()) + 23) + 0.5f, rectHeight + 0.5f), showing.get() * emptyAnim.get());

				Round.draw(ms, new Rect(x, y, this.sameWidth.get() ? this.widthAnim.get() : font.getWidth(getTitle()) + 23, rectHeight), 3, rock.getThemes().getFirstColor().move(FixColor.WHITE, hover()).alpha(this.showing.get() * this.emptyAnim.get()));
				Render.image("icons/hud/bind.png", x + rectHeight/2 - 4, y + rectHeight/2 - 5, 10, 10, rock.getThemes().getTextFirstColor().alpha(this.showing.get() * this.emptyAnim.get()));
				font.draw(ms, getTitle(), x + rectHeight/2 + 9, y + 3, rock.getThemes().getTextFirstColor().alpha(this.showing.get() * this.emptyAnim.get()));
				
				Render.outline(ms, new Rect(x - 0.25f, y, (this.sameWidth.get() ? this.widthAnim.get() : font.getWidth(getTitle()) + 23), rectHeight), showing.get() * emptyAnim.get());
				
				maxWidth = Math.max(font.getWidth(getTitle()) + 23, maxWidth);
			}
			
			if (this.sameWidth.get())
				this.widthAnim.animate(Math.max(binds.isEmpty() ? 0 : 70, maxWidth), 50);
		}
	}
	
	private String getTitle() {
		return rock.getModules().get(Interface.class).getEnglish().get() ? "Hotkeys" : getName();
	}
	
	private float getLeftWidth(Bind bind) {
		FontSize font = semibold.get(14);
		Type type = getType(bind);
		
		switch (type) {
		case ENABLE:
		case DISABLE:
			return 19;

		case MODE:
			return 9 + font.getWidth(bind.getParent() instanceof Mode mode ? mode.getCurrent().getName() : "");
		case SLIDER:
			return 9 + font.getWidth(TextUtility.formatNumber(bind.getParent() instanceof Slider slider ? slider.get() : 0));

		default:
			break;
		}
		return 19;
	}
	
	private String getValue(Bind bind) {
		FontSize font = semibold.get(14);
		Type type = getType(bind);
		
		switch (type) {
		case ENABLE:
		case DISABLE:
			return "";

		case MODE:
			return (bind.getParent() instanceof Mode mode ? mode.getCurrent().getName() : "");
		case SLIDER:
			return TextUtility.formatNumber(bind.getParent() instanceof Slider slider ? slider.get() : 0);

		default:
			break;
		}
		return "";
	}
	
	private Type getType(Bind bind) {
		Type type = Type.ENABLE;
		if (bind.getParent() instanceof CheckBox check) {
			type = Type.ENABLE;
		} else if (bind.getParent() instanceof Module mod) {
			type = Type.ENABLE;
		} else if (bind.getParent() instanceof Slider slider) {
			type = Type.SLIDER;
		} else if (bind.getParent() instanceof Mode mode) {
			type = type.MODE;
		}
        return type;
    }
	
	private ArrayList<Bind> getBindList() {
		ArrayList<Bind> binds = new ArrayList<>();
		
		for (Module mod : rock.getModules().values()) {
			if (!mod.getBinds().isEmpty()) {
				Bind bind = mod.getBinds().get(0);
				
				bind.getShowingAnim().setForward(mod.get());
				if (!bind.getShowingAnim().finished(false)) {
					bind.setParent(mod);
					binds.add(bind);
				}
			}
			
			for (Setting set : mod.getSettings()) {
				if (!set.getBinds().isEmpty() && !(set instanceof Binding) && !set.isHide()) {
					Bind bind = set.getBinds().get(0);
					
					bind.getShowingAnim().setForward(set.isToggled());
					
					if (!bind.getShowingAnim().finished(false)) {
						bind.setParent(set);
						binds.add(bind);
					}
				}
			}
		}
		
		return binds;
	}
	
	public <T> void updateBind(Bind bind, boolean enabled, String text) {
		bind.setEnabled(true);
		bind.setText(text);
	}
	
}
