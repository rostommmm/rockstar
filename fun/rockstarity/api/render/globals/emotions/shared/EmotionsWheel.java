package fun.rockstarity.api.render.globals.emotions.shared;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.inputs.EventKey;
import fun.rockstarity.api.events.list.game.inputs.EventMouseMove;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.helpers.render.Stencil;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.list.LightTheme;
import fun.rockstarity.api.render.globals.emotions.Emotions;
import fun.rockstarity.api.render.globals.emotions.instance.EmotionType;
import fun.rockstarity.api.render.shaders.fog.Vector2d;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.client.modules.other.Globals;

/**
 * @author ConeTin
 * @since 6 Р°РІРі. 2024вЂЇРі.
 */

public class EmotionsWheel implements IAccess {
	
	private final Emotions emotions;
	private final Animation wheelAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300); // РђРЅРёРјР°С†РёСЏ РєРѕР»РµСЃР° РѕР±РѕР·СЂРµРЅРёСЏ
	private final Animation[] elementAnims = new Animation[8]; // РђРЅРёРјР°С†РёСЏ РЅР° РєР°Р¶РґС‹Р№ СЌР»РµРјРµРЅС‚
	private double mouseX, mouseY;
	private EmotionType emotion;
	
	public EmotionsWheel(Emotions emotions) {
		this.emotions = emotions;
		for (int i = 0; i < 8; i++) {
			elementAnims[i] = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
		}
	}
	
	public void onEvent(Event event) {
	    if (mc.currentScreen == null) {
	        if (event instanceof EventKey e) {
	            if (rock.getModules().get(Globals.class).getWheelBind().getBindByKey(e).isPresent()) {
	                this.wheelAnim.setForward(!e.isReleased());

	                if (!e.isReleased()) {
	                    this.mouseX = this.mouseY = 0;
	                    this.emotion = null;
	                } else {
	                    if (this.emotion != null)
	                        rock.getEmotions().playEmotion(mc.player, this.emotion);
	                }
	            }
	        }

	        if (event instanceof EventMouseMove e) {
	            if (this.wheelAnim.isForward()) {
	                this.mouseX += e.getXVelocity();
	                this.mouseY += e.getYVelocity();
	                e.cancel();
	            }
	        }

	        if (event instanceof EventRender2D e) 
	            this.drawWheel(e);
	    } else {
	        this.wheelAnim.setForward(false);
	        this.emotion = null;
	    }
	}

	
	private void drawWheel(EventRender2D event) {
		MatrixStack ms = event.getMatrixStack();
		
		if (this.wheelAnim.finished(false)) return;
		
		Round.draw(ms, new Rect(0, 0, sr.getScaledWidth(), sr.getScaledHeight()), 0, FixColor.BLACK.alpha(0.4f * this.wheelAnim.get()));
		
		float x = sr.getScaledWidth() / 2F;
		float y = sr.getScaledHeight() / 2F;
		
		GL11.glPushMatrix();

		GL11.glTranslated(x, y, 0); // РўСЂР°РЅСЃР»РµР№С‚ Рє С†РµРЅС‚СЂСѓ СЌРєСЂР°РЅР°
		//Round.draw(ms, new Rect((float)mouseX-5,(float)mouseY-5,10,10), 5, FixColor.WHITE);
		
		double distance = Double.MAX_VALUE;
		int closest = 0;
		
		for (int i = 0; i < 8; i++) {
			float width = 190/2 * (1.5f - 0.5f * this.wheelAnim.get());
			float height = 117/2 * (1.5f - 0.5f * this.wheelAnim.get());
			float radius = 150 - 50 * this.wheelAnim.get() ;
			
			double xPos = Math.cos(Math.toRadians(360/8F*i+90)) * -radius;
			double yPos = Math.sin(Math.toRadians(360/8F*i+90)) * -radius;
			
			//Round.draw(ms, new Rect((float)xPos-5,(float)yPos-5,10,10), 5, FixColor.WHITE);
			
			double dist = new Vector2d(this.mouseX, this.mouseY).distance(new Vector2d(xPos, yPos));
			
			if (dist < distance) {
				closest = i;
				distance = dist;
			}
		}
		
		GL11.glRotated(-45F, 0, 0, 1);
		
		for (int i = 0; i < 8; i++) {
			float width = 190/2 * (1.5f - 0.5f * this.wheelAnim.get());
			float height = 117/2 * (1.5f - 0.5f * this.wheelAnim.get());
			float radius = 150 - 50 * this.wheelAnim.get() ;
			
			this.elementAnims[i].setEasing(Easing.TARGETESP_EASE_OUT_BACK);
			this.elementAnims[i].setForward(i == closest);
			
			radius += this.elementAnims[i].get() * 5F;
			width *= 1 + this.elementAnims[i].get() * 0.1f;
			height *= 1 + this.elementAnims[i].get() * 0.1f;
			
			// Р РѕС‚РµР№С‚С‹ С…СѓРµР№С‚С‹
			GL11.glRotated(360/8F, 0, 0, 1);
			Render.image("masks/wheel_element.png", -width/2, -radius - height/2, width, height, 
					rock.getThemes().getFirstColor().move((i > EmotionType.values().length - 1 ? 
					rock.getThemes().getSecondColor().move(FixColor.RED, 0.03f)  : 
					rock.getThemes().getThirdColor()), this.elementAnims[i].get()).alpha(this.wheelAnim.get() * (rock.getThemes().getCurrent() instanceof LightTheme ? 0.7f : 1)));
			
			if (i == closest)
				this.emotion = i > EmotionType.values().length - 1 ? null :  EmotionType.values()[i];
			
			Stencil.init();
			Render.image("masks/wheel_element.png", -width/2, -radius - height/2 + 0.5f, width, height-1.5f, rock.getThemes().getFirstColor().alpha(this.wheelAnim.get()));
			Stencil.read(1);
			
			GL11.glPushMatrix();
			GL11.glRotated(-360/8F*i, 0, 0, 1);
			double xPos = Math.cos(Math.toRadians(360/8F*i+90)) * -radius;
			double yPos = Math.sin(Math.toRadians(360/8F*i+90)) * -radius;
			float emoteSize = height + 20 * this.elementAnims[i].get();
			float bgSize = height + 120 * this.elementAnims[i].get();
			float noChosedSize = height + 50 + 70 * this.elementAnims[i].get();
			
			if (i > EmotionType.values().length - 1) {
				Render.image("masks/emotions/floss.png", (float) xPos - noChosedSize/2, (float) yPos - noChosedSize/2, noChosedSize, noChosedSize, FixColor.WHITE.alpha(0.05f * this.wheelAnim.get() + 0.05f * this.elementAnims[i].get()));
			} else {
				if (i == closest)
					Render.image("masks/emotions/" + EmotionType.values()[i].getName() +  ".png", (float) xPos - bgSize/2, (float) yPos - bgSize/2, bgSize, bgSize, FixColor.WHITE.alpha(0.1f * this.wheelAnim.get()));
				
				Render.image("masks/emotions/" + EmotionType.values()[i].getName() +  ".png", (float) xPos - emoteSize/2, (float) yPos - emoteSize/2, emoteSize, emoteSize, FixColor.WHITE.alpha(this.wheelAnim.get()));
			}
			GL11.glPopMatrix();
			
			Stencil.finish();
		}
		
		GL11.glPopMatrix();
		
		String title = "Р’С‹Р±РµСЂРё РЅСѓР¶РЅСѓСЋ СЌРјРѕС†РёСЋ";
		String subTitle = "Р’С‹Р±РµСЂРё СЌРјРѕС†РёСЋ РёР· СЃРїРёСЃРєР° Рё РѕС‚РїСѓСЃС‚Рё РєР»Р°РІРёС€Сѓ";
		bold.get(24).draw(ms, title, x - bold.get(24).getWidth(title) / 2, y - 50 * (1-this.wheelAnim.get()) - 200, FixColor.WHITE.alpha(this.wheelAnim.get()));
		bold.get(17).draw(ms, subTitle, x - bold.get(17).getWidth(subTitle) / 2, y - 50 * (1-this.wheelAnim.get()) - 185, rock.getThemes().getDarkTheme().getTextFirstColor().alpha(this.wheelAnim.get()));
		
		String current = this.emotion == null ? "Р­РјРѕС†РёСЏ РЅРµ РІС‹Р±СЂР°РЅР°" : this.emotion.getLocalized();
		bold.get(24).draw(ms, current, x - bold.get(24).getWidth(current) / 2, y + 50 * (1-this.wheelAnim.get()) + 150, FixColor.WHITE.alpha(this.wheelAnim.get() * this.elementAnims[closest].get()));
		
		Round.draw(ms, new Rect(-100,-100,10,10), 1, FixColor.WHITE); // $$$ FIX
	}

}
