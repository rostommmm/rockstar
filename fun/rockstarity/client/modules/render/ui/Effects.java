package fun.rockstarity.client.modules.render.ui;

import java.util.*;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.events.list.render.ui.shaders.EventBlur;
import fun.rockstarity.api.helpers.game.Binds;
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
import fun.rockstarity.api.render.ui.alerts.AlertType;
import fun.rockstarity.api.render.ui.fonts.FontSize;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.client.modules.render.Interface;
import fun.rockstarity.client.modules.render.Interface.UIElement;
import lombok.Getter;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.renderer.texture.PotionSpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.EffectUtils;
import net.minecraft.util.math.vector.Vector2f;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author Malecharik
 * @since 3 РёСЋРЅ. 2024 Рі. 18:53:27
 */
public class Effects extends UIElement {
	
	private float x, y, width, height;
	
	@Getter
	private final Map<String, EffectInstance> effects = new TreeMap<>();
	
	private final CheckBox removeIfEmpty, showTime, sameWidth, highlightHarmful, alert, highBen;
	
	private final Animation emptyAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	
	private final InfinityAnimation widthAnim = new InfinityAnimation();

	private Map<Effect, Boolean> ended = new HashMap<>();
	@NativeInclude
	public Effects(Interface ui, Select select) {
		super(select, "Зелья", new Rect(6, 83, 0, 0));
		
		this.removeIfEmpty = new CheckBox(this, "РЎРєСЂС‹РІР°С‚СЊ РїСЂРё РѕС‚СЃСѓС‚СЃС‚РІРёРё").set(true);
		this.showTime = new CheckBox(this, "Р’СЂРµРјСЏ").set(true);
		this.alert = new CheckBox(this, "РЈРІРµРґРѕРјР»СЏС‚СЊ РѕР± РѕРєРѕРЅС‡Р°РЅРёРё СЌС„С„РµРєС‚Р°");
		this.sameWidth = new CheckBox(this, "РћРґРЅР° РґР»РёРЅР°");
		highlightHarmful = new CheckBox(this, "РџРѕРјРµС‡Р°С‚СЊ РѕС‚СЂРёС†Р°С‚РµР»СЊРЅС‹Рµ").set(true);
		highBen = new CheckBox(this, "РўРѕР»СЊРєРѕ РІР°Р¶РЅС‹Рµ");
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
			width = 70;
			height = 16;
			FontSize font = semibold.get(14);
			
			float offset = 2;
			float rectHeight = 15;
			float yOff = rectHeight + offset + 1;
			
			List<EffectInstance> effects = new ArrayList<>(this.effects.values());
			
			Collections.sort(effects, (a, b)-> {
				String textA = String.format("%s %s %s",
					    I18n.format(a.getEffectName()),
					    I18n.format("enchantment.level." + (a.getAmplifier() + 1)).replace("enchantment.level.", ""),
					    this.showTime.get() ? EffectUtils.getPotionDurationString(a, 1) : ""
				);
				String textB = String.format("%s %s %s",
					    I18n.format(b.getEffectName()),
					    I18n.format("enchantment.level." + (b.getAmplifier() + 1)).replace("enchantment.level.", ""),
					    this.showTime.get() ? EffectUtils.getPotionDurationString(b, 1) : ""
				);
				return Float.compare(font.getWidth(textA), font.getWidth(textB));
			});
			Collections.reverse(effects);
			
			for (EffectInstance eff : effects) {
				float leftSize = 16;
				float iconSize = 10;
				float reversedAnim = 1 - eff.getShowingAnim().get();
				String text = String.format("%s %s",
					    I18n.format(eff.getEffectName()),
					    I18n.format("enchantment.level." + (eff.getAmplifier() + 1)).replace("enchantment.level.", "")
				);
				String duration = EffectUtils.getPotionDurationString(eff, 1);
				
				float width = this.sameWidth.get() ? this.widthAnim.get() - (showTime.get() ? font.getWidth(duration) + 12 : 0) : font.getWidth(text) + 10 + leftSize;



				if (alert.get()) {
					Effect potion = eff.getPotion();
					if (!mc.player.isPotionActive(potion) && potion.getEffectType() != EffectType.HARMFUL) {
						if (!ended.getOrDefault(potion, false)) {
							rock.getAlertHandler().alert("Р­С„С„РµРєС‚ " + text + " Р·Р°РєРѕРЅС‡РёР»СЃСЏ", AlertType.ERROR);
							ended.put(potion, true);
						}
					}

					if (mc.player.isPotionActive(potion) && potion.getEffectType() != EffectType.HARMFUL) {
						ended.put(potion, false);
					}
				}

				// РџСЂР°РІР°СЏ С‡Р°СЃС‚СЊ
				if (blur())
					Round.draw(ms, new Rect(x + leftSize, y + yOff - rectHeight * reversedAnim, width - leftSize, rectHeight), 0, 3, 0, 3, rock.getThemes().getFirstColor().alpha(eff.getShowingAnim().get() * this.showing.get()));
				
				yOff += (rectHeight + offset) * eff.getShowingAnim().get();
			}
		}
		
		if (event instanceof EventRender2D e) {
			MatrixStack ms = e.getMatrixStack();
			FontSize font = semibold.get(14);
			float rectHeight = 15;
			float offset = 2;
			float yOff = rectHeight + offset + 1;
			EffectInstance toRemove = null;
			
			List<String> effectNames = new ArrayList<>();
			List<EffectInstance> original = mc.player.getActivePotionEffects().stream().sorted(Comparator.comparing(EffectInstance::getDuration)).toList();
			
			for (EffectInstance eff : original) {
				if (eff == null || eff.getRealName() == null) continue;
				if (!this.effects.containsKey(eff.getRealName())) {
					this.effects.put(eff.getRealName(), eff);
				} else {
					this.effects.replace(eff.getRealName(), eff);
				}
				effectNames.add(eff.getRealName());
			}
			
			float maxWidth = 0;
			
			PotionSpriteUploader potionspriteuploader = this.mc.getPotionSpriteUploader();
			
			
			List<EffectInstance> effects = new ArrayList<>(this.effects.values());
			
			Collections.sort(effects, (a, b)-> {
				String textA = String.format("%s %s %s",
					    I18n.format(a.getEffectName()),
					    I18n.format("enchantment.level." + (a.getAmplifier() + 1)).replace("enchantment.level.", ""),
					    this.showTime.get() ? EffectUtils.getPotionDurationString(a, 1) : ""
				);
				String textB = String.format("%s %s %s",
					    I18n.format(b.getEffectName()),
					    I18n.format("enchantment.level." + (b.getAmplifier() + 1)).replace("enchantment.level.", ""),
					    this.showTime.get() ? EffectUtils.getPotionDurationString(b, 1) : ""
				);
				return Float.compare(font.getWidth(textA), font.getWidth(textB));
			});
			Collections.reverse(effects);
			
			Render.glow(ms, new Rect(x, y, this.sameWidth.get() ? this.widthAnim.get() : font.getWidth(getTitle()) + 23, rectHeight), showing.get() * emptyAnim.get());
			
			for (EffectInstance eff : effects) {
				float leftSize = 16;
				float iconSize = 10;
				float reversedAnim = 1 - eff.getShowingAnim().get();
				String text = String.format("%s %s",
					    I18n.format(eff.getEffectName()),
					    I18n.format("enchantment.level." + (eff.getAmplifier() + 1)).replace("enchantment.level.", "")
				);
				String duration = EffectUtils.getPotionDurationString(eff, 1);
				
				float width = this.sameWidth.get() ? this.widthAnim.get() - (this.showTime.get() ? font.getWidth(duration) + 12 : 0) : font.getWidth(text) + 10 + leftSize;
				
				// РђРЅРёРјРєРё
				eff.getShowingAnim().setForward(effectNames.contains(eff.getRealName()));
				eff.getSecondAnim().setForward(eff.getShowingAnim().get() < 0.5f);
				
				if (glow()) {
					Stencil.init();
					Round.draw(ms, new Rect(x - 0.25f, y + yOff - rectHeight * reversedAnim - 0.25f, width + 0.5f, rectHeight), 4, rock.getThemes().getSecondColor().alpha(showing.get()));
					Stencil.read(0);
					
					Render.glow(ms, new Rect(x - 0.25f, y + yOff - rectHeight * reversedAnim - 0.25f, width + 0.5f, rectHeight + 0.75f), showing.get() * eff.getShowingAnim().get(), false);
					
					if (showTime.get()) {
						Render.glow(ms, new Rect(x + width + 2, y + yOff - rectHeight * reversedAnim, font.getWidth(duration) + 10, rectHeight), showing.get() * eff.getShowingAnim().get(), false);
					}
					
					Stencil.finish();
				}
				
				ms.push();
				ms.translate(0, 0, 9);
				
				// Р”РµР»Р°РµРј РµРјСѓ РѕР±СЂРµР·Р°РЅРёРµ
				Stencil.init();
				Round.draw(ms, new Rect(x - 2, y + yOff - offset, width + 100, rectHeight + offset*2), 3, rock.getThemes().getFirstColor().alpha(eff.getShowingAnim().get()));
				Stencil.read(1);
				
				// Р›РµРІР°СЏ С‡Р°СЃС‚СЊ
				Round.draw(ms, new Rect(x, y + yOff - rectHeight * reversedAnim, leftSize, rectHeight), 3, 0, 3, 0, rock.getThemes().getFirstColor().move(FixColor.WHITE, hover()).alpha(eff.getShowingAnim().get() * this.showing.get()));
				
				Effect effect = eff.getPotion();
				TextureAtlasSprite textureatlassprite = potionspriteuploader.getSprite(effect);
				this.mc.getTextureManager().bindTexture(textureatlassprite.getAtlasTexture().getTextureLocation());
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.showing.get());
				IngameGui.blit(ms, x + leftSize / 2 - iconSize / 2, y + yOff + 16 / 2 - iconSize / 2 - rectHeight * reversedAnim, new IngameGui(mc).getBlitOffset(), iconSize, iconSize, textureatlassprite);
				RenderSystem.disableBlend();
				
				FixColor backColor = rock.getThemes().getFirstColor().move(FixColor.RED, highlightHarmful.get() && eff.getPotion().getEffectType() == EffectType.HARMFUL ? (blur() ? 0.2f : 0.1f) : 0);

				if (blur()) {
				    Round.draw(ms, new Rect(x + leftSize, y + yOff - rectHeight * reversedAnim, width - leftSize, rectHeight), 0, 3, 0, 3, backColor.move(FixColor.WHITE, hover()).alpha(0.5f * eff.getShowingAnim().get() * this.showing.get()));
				} else {
				    Round.draw(ms, new Rect(x + leftSize, y + yOff - rectHeight * reversedAnim, width - leftSize, rectHeight), 0, 3, 0, 3, backColor.move(FixColor.WHITE, hover()).alpha(eff.getShowingAnim().get() * this.showing.get()));
				}

				font.draw(ms, text, x + leftSize + 4, y + 2.5f + yOff - rectHeight * reversedAnim, rock.getThemes().getTextFirstColor().alpha(eff.getShowingAnim().get() * this.showing.get()));
				
				Render.outline(ms, new Rect(x, y + yOff - rectHeight * reversedAnim, width, rectHeight), showing.get() * eff.getShowingAnim().get());

				Stencil.finish();
				
				if (this.showTime.get()) {
					Round.draw(ms, new Rect(x + width + 2, y + yOff - rectHeight * reversedAnim, font.getWidth(duration) + 10, rectHeight), 3, rock.getThemes().getFirstColor().move(FixColor.WHITE, hover()).alpha(eff.getShowingAnim().get() * this.showing.get()));
					font.draw(ms, duration, x + width + 6.5f, y + yOff - rectHeight * reversedAnim + 2.5f, (eff.getDuration() / 20 < 10 ? FixColor.RED : rock.getThemes().getTextFirstColor()).alpha(eff.getShowingAnim().get() * this.showing.get()));
					if (eff.getShowingAnim().get() > 0.5f)
						Render.outline(ms, new Rect(x + width + 2f, y + yOff - rectHeight * reversedAnim, font.getWidth(duration) + 10, rectHeight), showing.get() * eff.getShowingAnim().get());
				}
				yOff += (rectHeight + offset) * eff.getShowingAnim().get();
				
				maxWidth = Math.max(font.getWidth(text) + 10 + leftSize + (this.showTime.get() ? font.getWidth(duration) + 12 : 0), maxWidth);
				
				if (eff.getShowingAnim().finished(false)) toRemove = eff;

				ms.pop();
			}
			
			rectHeight++;
			
			this.emptyAnim.setForward(mc.currentScreen instanceof ChatScreen || !effects.isEmpty() || !this.removeIfEmpty.get());

			// Р“РѕР»РѕРІРєР°
			if (!this.emptyAnim.finished(false)) {
				Round.draw(ms, new Rect(x, y, this.sameWidth.get() ? this.widthAnim.get() : font.getWidth(getTitle()) + 23, rectHeight), 3, rock.getThemes().getFirstColor().move(FixColor.WHITE, hover()).alpha(this.showing.get() * this.emptyAnim.get()));
				Render.image("icons/hud/potions.png", x + rectHeight/2 - 4, y + rectHeight/2 - 4.5f, 9, 9, rock.getThemes().getTextFirstColor().alpha(this.showing.get() * this.emptyAnim.get()));
				font.draw(ms, getTitle(), x + rectHeight/2 + 9, y + 3, rock.getThemes().getTextFirstColor().alpha(this.showing.get() * this.emptyAnim.get()));
				
				Render.outline(ms, new Rect(x - 0.25f, y, (this.sameWidth.get() ? this.widthAnim.get() : font.getWidth(getTitle()) + 23), rectHeight), showing.get() * emptyAnim.get());
				
				maxWidth = Math.max(font.getWidth(getTitle()) + 23, maxWidth);
			}
			
			if (this.sameWidth.get())
				this.widthAnim.animate(Math.max(effects.isEmpty() ? 0 : 70, maxWidth), 50);
			
			if (toRemove != null) {
				this.effects.remove(toRemove.getRealName(), toRemove);
			}
			
			this.draggable.setWidth(maxWidth);
			this.draggable.setHeight(yOff);
		}
	}
	
	private String getTitle() {
		return rock.getModules().get(Interface.class).getEnglish().get() ? "Effects" : getName();
	}
	
}
