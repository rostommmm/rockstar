package fun.rockstarity.client.modules.render;

import java.util.Arrays;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventWorldChange;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.events.list.render.EventRender3D;
import fun.rockstarity.api.events.list.render.ui.EventHotbarSlot;
import fun.rockstarity.api.events.list.render.ui.shaders.EventBlur;
import fun.rockstarity.api.events.list.render.world.EventCamera;
import fun.rockstarity.api.events.list.render.world.EventCameraPosition;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Position;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Select.Element;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.animation.infinity.InfinityAnimation;
import fun.rockstarity.api.render.animation.infinity.RotationAnimation;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.render.shaders.list.Glass;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.client.modules.player.FreeCam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.settings.AttackIndicatorStatus;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.optifine.CustomItems;
import net.optifine.shaders.Shaders;

/**
 * @author ConeTin
 * @since 4 июн. 2024 г.
 */

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Info(name="Beautifully", desc="Визуально улучшает игру", type=Category.RENDER)
public class Beautifully extends Module {
	
	Select utils = new Select(this, "Анимация");
	Element zoom = new Element(utils, "Зум").set(true);
	Element tab = new Element(utils, "Таб").set(true);
	Element inventory = new Element(utils, "Инвентарь").set(true);
	Element f5 = new Element(utils, "Третье лицо").set(true);
	Element hotBar = new Element(utils, "Хотбар").set(true);
	Element chat = new Element(utils, "Чат").set(true);
	Element changeWorld = new Element(utils, "Переход между мирами").set(true);
	Element damage = new Element(utils, "Урон").set(false);
	
	Slider zoomSize = new Slider(this, "Сила приближения").min(1).max(4).inc(0.1f).set(2).hide(() -> !this.zoom.get()).text(2, "Обыч");
	public CheckBox customF3 = new CheckBox(this, "Кастомный F5").hide(() -> !f5.get());
	public Position positionF5 = new Position(customF3, "Позиция"); // я знаю что эт ф5 но я пидорас и пох
	public Slider distance = new Slider(customF3, "Дистанция камеры").min(-3).max(6).inc(0.1f).set(4);
	
	Select replace = new Select(this, "Заменять");
	Element crosshair = new Element(replace, "Прицел");
	Element hotbar = new Element(replace, "Хотбар");
	Element f3 = new Element(replace, "F3");
	Element bars = new Element(replace, "Полоски").hide(() -> !hotbar.get());
	CheckBox animCross = new CheckBox(this, "Анимировать").hide(() -> !this.crosshair.get());
	
	Select realistic = new Select(this, "Реалистичность");
	Element realCamera = new Element(realistic, "Камера");
	Element itemPhysics = new Element(realistic, "Физика предметов");
	Element realBlur = new Element(realistic, "Размытие в движении").hide(() -> !rock.getModules().get(Interface.class).getBlur().get()).hide(() -> true);
	Element position = new Element(realistic, ":3").hide(() -> !rock.isDebugging());
	
	Select client = new Select(this, "Клиент").hide(() -> !Shaders.shaderPackLoaded);
	Element bloom = new Element(client, "Свечение 3д визуалов");
	
	Slider aspectRatio = new Slider(this, "Соотношение сторон").min(0.5f).max(2f).inc(0.1f).set(1).desc("Изменяет соотношение сторон").text(1.0f, "Выкл");
	
	RotationAnimation camera = new RotationAnimation();
	//InfinityAnimation brightness = new InfinityAnimation();
	
	// Хотбар
	Animation[] items = new Animation[9];
	InfinityAnimation slot = new InfinityAnimation();
	
	InfinityAnimation healthAnim = new InfinityAnimation();
	InfinityAnimation goldenAnim = new InfinityAnimation();
	InfinityAnimation foodAnim = new InfinityAnimation();
	InfinityAnimation armorAnim = new InfinityAnimation();
	InfinityAnimation waterAnim = new InfinityAnimation();
	InfinityAnimation saturationAnim = new InfinityAnimation();
	
	InfinityAnimation x = new InfinityAnimation();
	InfinityAnimation y = new InfinityAnimation();
	InfinityAnimation z = new InfinityAnimation();
	InfinityAnimation yaw = new InfinityAnimation();
	InfinityAnimation pitch = new InfinityAnimation();
	
	Animation changeWorldAnim = new Animation().setEasing(Easing.EASE_OUT_CIRC).setSpeed(300);
	
	Animation hideUI = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);

	
	public Beautifully() {
		this.set(true);
		Arrays.setAll(items, item -> new Animation().setEasing(Easing.EASE_OUT_CIRC).setSpeed(300));
	}
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventRender3D e && bloom.get()) {
			Glass.draw(FixColor.WHITE, 0, 0, 0);
			Glass.end();
		}
		
		// > Переход между мирами
		if (changeWorld.get()) {
			if (event instanceof EventCameraPosition e && Player.isInGame()) {
				changeWorldAnim.setEasing(Easing.BOTH_CIRC);
				changeWorldAnim.setSpeed(300);
				changeWorldAnim.setForward(mc.isGameFocused());
				Vector2f rot = e.getRotation();
				e.setRotation(new Vector2f(
						rot.x,
						rot.y + (90-rot.y) * (1-changeWorldAnim.get())
				));
			}
			
			if (event instanceof EventWorldChange) {
				changeWorldAnim.setForward(false);
			}
		}
		
		// > Реалистичный поворот камеры
		if (event instanceof EventCamera e) {
			MatrixStack ms = e.getStack();
			camera.animate(new Vector2f(mc.player.rotationYaw, -mc.player.movementInput.moveStrafe * 20), 60, 450);
			
			if (realCamera.get()) {
				ms.rotate(Vector3f.ZP.rotationDegrees(MathHelper.clamp(camera.getPitch(), -20, 20) + MathHelper.clamp((mc.player.rotationYaw-camera.getYaw())/2F, -10, 10)));
			}
		}
		
		// > Моушен блюр
		if (event instanceof EventBlur e && realBlur.get() && !rock.getModules().get(FreeCam.class).get()) {
			//Round.draw(e.getMatrixStack(), new Rect(0,0,sr.getScaledWidth(),sr.getScaledHeight()), 0.1f, FixColor.WHITE.alpha(Math.min(Math.abs(mc.player.rotationYaw-camera.getYaw())/50F, 0.7f)));
		}
		
		// > Хотбар
		if (hotbar.get() && (event instanceof EventBlur || event instanceof EventRender2D)) {
			hideUI.setForward(!rock.getModules().get(Interface.class).getHideUi().get() || mc.isGameFocused());
			
			GL11.glPushMatrix();
			GL11.glTranslated(0, 50 - 50 * hideUI.get(), 0);
			if (event instanceof EventBlur e) {
				renderHotbar(e.getPartialTicks(), e.getMatrixStack(), true);
			}
			
			if (event instanceof EventRender2D e) {
				renderHotbar(e.getPartialTicks(), e.getMatrixStack(), false);
			}
			GL11.glPopMatrix();
		}
		
		// > Интерполяция позиции и камеры (:3)
		if (event instanceof EventCameraPosition e && position.get()) {
			int speed = 1;
			Vector3d pos = e.getPosition();
			e.setPosition(new Vector3d(
					x.animate((float) pos.x, speed),
					y.animate((float) pos.y, speed),
					z.animate((float) pos.z, speed)
			));
			
			speed = 1;
			Vector2f rot = e.getRotation();
			e.setRotation(new Vector2f(
					yaw.animate((float) rot.x, speed),
					pitch.animate((float) rot.y, speed)
			));
		}
	}
	
	protected void renderHotbar(float partialTicks, MatrixStack matrixStack, boolean blur)
    {
        PlayerEntity playerentity = !(this.mc.getRenderViewEntity() instanceof PlayerEntity) ? null : (PlayerEntity)this.mc.getRenderViewEntity();
        if (playerentity != null && !mc.getGameSettings().hideGUI)
        {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            ItemStack itemstack = playerentity.getHeldItemOffhand();
            HandSide handside = playerentity.getPrimaryHand().opposite();
            Beautifully beautifully = rock.getModules().get(Beautifully.class);
            mc.ingameGUI.getOpening().setForward(mc.currentScreen instanceof ChatScreen && !rock.isPanic() && beautifully.get() && beautifully.getHotBar().get());
            int i = sr.getScaledWidth() / 2;
            int j = mc.ingameGUI.getBlitOffset();
            int k = 182; 
            int l = 91;
            mc.ingameGUI.setBlitOffset(-90);
            //this.blit(matrixStack, i - 91, (int) (sr.getScaledHeight() - 22 - 14 * mc.ingameGUI.getOpening().get()), 0, 0, 182, 22);
            //this.blit(matrixStack, i - 91 - 1 + playerentity.inventory.currentItem * 20, (int) (sr.getScaledHeight() - 22 - 1 - 14 * mc.ingameGUI.getOpening().get()), 0, 22, 24, 22);
            
            float hpRound = 2;
            
            if (blur) {
            	Round.draw(matrixStack, new Rect(i - 91, (sr.getScaledHeight() - 26.5f - 14 * mc.ingameGUI.getOpening().get()), 182, 21), 3, rock.getThemes().getSecondColor());
            	
            	if (mc.playerController.shouldDrawHUD() && bars.get()) {
            		Round.draw(matrixStack, new Rect(i - 91, sr.getScaledHeight() - 36 - 14 * mc.ingameGUI.getOpening().get(), 85, 7), hpRound, FixColor.RED.move(FixColor.WHITE, 0.5f).move(FixColor.BLACK, 0.7f));
                    
                	Round.draw(matrixStack, new Rect(i + 6, sr.getScaledHeight() - 36 - 14 * mc.ingameGUI.getOpening().get(), 85, 7), hpRound, FixColor.YELLOW.move(FixColor.ORANGE, 0.7f).move(FixColor.WHITE, 0.2f).move(FixColor.BLACK, 0.5f));
            	}
            } else {
            	Render.glow(matrixStack, new Rect(i - 91, sr.getScaledHeight() - 27.5f - 14 * mc.ingameGUI.getOpening().get(), 182, 22), 1);
            	
            	float alpha = 1;
            	
            	if (rock.getModules().get(Interface.class).getBlur().get()) {
            		Round.draw(matrixStack, new Rect(i - 91, sr.getScaledHeight() - 27 - 14 * mc.ingameGUI.getOpening().get(), 182, 22), 3, rock.getThemes().getFirstColor().alpha(0.5f));

                	Round.draw(matrixStack, new Rect(i - 91 - 1 + 3 + slot.animate(playerentity.inventory.currentItem * 20, 50), sr.getScaledHeight() - 25 - 14 * mc.ingameGUI.getOpening().get(), 18, 18), 2, rock.getThemes().getSecondColor().alpha(0.5f));
                	
                	if (mc.playerController.shouldDrawHUD() && bars.get()) {
                		Round.draw(matrixStack, new Rect(i - 91, sr.getScaledHeight() - 36 - 14 * mc.ingameGUI.getOpening().get(), 85, 7), hpRound, FixColor.RED.move(FixColor.WHITE, 0.2f).move(FixColor.BLACK, 0.5f).alpha(0.5f));
                    	
                    	Round.draw(matrixStack, new Rect(i + 6, sr.getScaledHeight() - 36 - 14 * mc.ingameGUI.getOpening().get(), 85, 7), hpRound, FixColor.YELLOW.move(FixColor.ORANGE, 0.7f).move(FixColor.WHITE, 0.2f).move(FixColor.BLACK, 0.5f).alpha(0.5f));
                	}
            	} else {
            		FixColor upLeft = Style.getPoint(0).alpha(alpha);
        			FixColor upRight = Style.getPoint(90).alpha(alpha);
        			FixColor downLeft = Style.getPoint(180).alpha(alpha);
        			FixColor downRight = Style.getPoint(270).alpha(alpha);
        			
                	Round.draw(matrixStack, new Rect(i - 91 - 1 + 3 + slot.animate(playerentity.inventory.currentItem * 20, 50), sr.getScaledHeight() - 25 - 14 * mc.ingameGUI.getOpening().get(), 18, 18), 2, upLeft, upRight, downLeft, downRight);
                }
            	
            	Render.outline(matrixStack, new Rect(i - 91, sr.getScaledHeight() - 27 - 14 * mc.ingameGUI.getOpening().get(), 182, 22), 1);
            	
            	if (mc.playerController.shouldDrawHUD()) {
            		if (bars.get()) {
            			float healthCoff = healthAnim.animate(mc.player.getHealth() / mc.player.getMaxHealth(), 50);
                    	Round.draw(matrixStack, new Rect(i - 91, sr.getScaledHeight() - 36 - 14 * mc.ingameGUI.getOpening().get(), 85 * healthCoff, 7), hpRound, FixColor.RED.move(FixColor.WHITE, 0.2f));
                    	
                    	float goldenCoff = goldenAnim.animate(mc.player.getAbsorptionAmount() / 20F, 50);
                    	Round.draw(matrixStack, new Rect(i - 91, sr.getScaledHeight() - 36 - 14 * mc.ingameGUI.getOpening().get(), 85 * goldenCoff, 7), hpRound, FixColor.YELLOW.move(FixColor.WHITE, 0.2f));
                    	
                    	float armorCoff = armorAnim.animate(playerentity.getTotalArmorValue() / 20F, 50);
                    	Round.draw(matrixStack, new Rect(i - 91, sr.getScaledHeight() - 45 - 14 * mc.ingameGUI.getOpening().get(), 85 * armorCoff, 7), hpRound, FixColor.WHITE.move(FixColor.GRAY, 0.2f));
                    	
                    	
                    	float foodCoff = foodAnim.animate(mc.player.getFoodStats().getFoodLevel() / 20F, 50);
                    	Round.draw(matrixStack, new Rect(i + 91 - 85 * foodCoff, sr.getScaledHeight() - 36 - 14 * mc.ingameGUI.getOpening().get(), foodCoff * 85, 7), hpRound, FixColor.ORANGE);
                    	
                    	float saturationCoff = saturationAnim.animate(mc.player.getFoodStats().getSaturationLevel() / 20F, 50);
                    	Round.draw(matrixStack, new Rect(i + 91 - 85 * saturationCoff, sr.getScaledHeight() - 36 - 14 * mc.ingameGUI.getOpening().get(), saturationCoff * 85, 7), hpRound, FixColor.YELLOW);
                    	
                    	float waterCoff = waterAnim.animate(playerentity.getAir() < playerentity.getMaxAir() ? Math.min(playerentity.getAir(), playerentity.getMaxAir()) / (float)playerentity.getMaxAir() : 0, 50);
                    	Round.draw(matrixStack, new Rect(i + 91 - 85 * waterCoff, sr.getScaledHeight() - 45 - 14 * mc.ingameGUI.getOpening().get(), waterCoff * 85, 7), hpRound, FixColor.BLUE.move(FixColor.WHITE, 0.7f));
                    }
            		
            		String exp = this.mc.player.experienceLevel + "";
                	bold.get(14).draw(matrixStack, exp, sr.getScaledWidth()/2F - bold.get(14).getWidth(exp)/2F - 0.5f, sr.getScaledHeight() - 37 - 14 * mc.ingameGUI.getOpening().get(), FixColor.GREEN.alpha(alpha));
            	}
            	//if (rock.getModules().get(Interface.class).getOutline().get())
            	//	Round.drawOutlined(matrixStack, new Rect(i - 91, (int) (sr.getScaledHeight() - 27 - 14 * mc.ingameGUI.getOpening().get()), 182, 22), 3, 0.5f, FixColor.TRANSPARENT, new FixColor(198, 198, 198, 100).alpha(alpha));
            }
            
            if (!itemstack.isEmpty())
            {
                if (handside == HandSide.LEFT)
                {
                	if (!blur) {
        				Render.glow(matrixStack, new Rect(i - 91 - 28, (sr.getScaledHeight() - 27.5f - 14 * mc.ingameGUI.getOpening().get()), 22, 22), 1);
        			}
                	
                	if (blur) {
                    	Round.draw(matrixStack, new Rect(i - 91 - 28, (sr.getScaledHeight() - 27.5f - 14 * mc.ingameGUI.getOpening().get()), 22, 22), 3, rock.getThemes().getSecondColor());
                	} else if (rock.getModules().get(Interface.class).getBlur().get()) {
                    	Round.draw(matrixStack, new Rect(i - 91 - 28, sr.getScaledHeight() - 27 - 14 * mc.ingameGUI.getOpening().get(), 22, 22), 3, rock.getThemes().getFirstColor().alpha(0.5f));
                	}
                	
                	if (!blur) {
                    	Render.outline(matrixStack, new Rect(i - 91 - 28, (sr.getScaledHeight() - 27 - 14 * mc.ingameGUI.getOpening().get() + 50 - 50 * hideUI.get()), 22, 22), 1);
            		}
                //    this.blit(matrixStack, i - 91 - 29, (int) (sr.getScaledHeight() - 23 - 14 * mc.ingameGUI.getOpening().get()), 24, 22, 29, 24);
                }
                else
                {
                	if (!blur) {
        				Render.glow(matrixStack, new Rect(i + 91 + 6, (sr.getScaledHeight() - 27.5f - 14 * mc.ingameGUI.getOpening().get()), 22, 22), 1);
        			}
                	
                	if (blur) {
                    	Round.draw(matrixStack, new Rect(i + 91 + 6, (sr.getScaledHeight() - 27.5f - 14 * mc.ingameGUI.getOpening().get()), 22, 22), 3, rock.getThemes().getSecondColor());
                	} else if (rock.getModules().get(Interface.class).getBlur().get()) {
                    	Round.draw(matrixStack, new Rect(i + 91 + 6, sr.getScaledHeight() - 27 - 14 * mc.ingameGUI.getOpening().get(), 22, 22), 3, rock.getThemes().getFirstColor().alpha(0.5f));
                	}
                	
                	if (!blur) {
                    	Render.outline(matrixStack, new Rect(i + 91 + 6, (sr.getScaledHeight() - 27 - 14 * mc.ingameGUI.getOpening().get() + 50 - 50 * hideUI.get()), 22, 22), 1);
            		}
                //    this.blit(matrixStack, i + 91, (int) (sr.getScaledHeight() - 23 - 14 * mc.ingameGUI.getOpening().get()), 53, 22, 29, 24);
                }
            }
            
            if (blur) return;
            
            mc.ingameGUI.setBlitOffset(j);
            RenderSystem.enableRescaleNormal();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            CustomItems.setRenderOffHand(false);
            int k1 = sr.getScaledHeight() - 16 - 3 - 5;
            for (int i1 = 0; i1 < 9; ++i1)
            {
                int j1 = i - 90 + i1 * 20 + 2;
                new EventHotbarSlot(matrixStack, partialTicks, j1, (int) (k1 - 14 * mc.ingameGUI.getOpening().get()), i1).hook();
                
                items[i1].setForward(mc.player.inventory.currentItem == i1);
                Render.scale(j1+8, (int) (k1 - 14 * mc.ingameGUI.getOpening().get())+8, 1 - items[i1].get()/5f);
                this.renderHotbarItem(j1, (int) (k1 - 14 * mc.ingameGUI.getOpening().get()), partialTicks, playerentity, playerentity.inventory.mainInventory.get(i1));
                Render.end();
            }

            if (!itemstack.isEmpty())
            {
                CustomItems.setRenderOffHand(true);

                if (handside == HandSide.LEFT)
                {
                	Rect rect = new Rect(i - 91 - 26, (int) (sr.getScaledHeight() - 25 - 14 * mc.ingameGUI.getOpening().get()), 19, 19);
                	this.renderHotbarItem((int) (rect.getX() + (rect.getWidth() - 16) / 2), (int) (rect.getY() + (rect.getHeight() - 16) / 2), partialTicks, playerentity, itemstack);

                }
                else
                {
                	Rect rect = new Rect(i + 91 + 8, (int) (sr.getScaledHeight() - 25 - 14 * mc.ingameGUI.getOpening().get()), 19, 19);
                    this.renderHotbarItem((int) (rect.getX() + (rect.getWidth() - 16) / 2), (int) (rect.getY() + (rect.getHeight() - 16) / 2), partialTicks, playerentity, itemstack);
                }

                CustomItems.setRenderOffHand(false);
            }

            if (this.mc.getGameSettings().attackIndicator == AttackIndicatorStatus.HOTBAR)
            {
                float f = this.mc.player.getCooledAttackStrength(0.0F);

                if (f < 1.0F)
                {
                    int j2 = sr.getScaledHeight() - 20;
                    int k2 = i + 91 + 6;

                    if (handside == HandSide.RIGHT)
                    {
                        k2 = i - 91 - 22;
                    }

                    this.mc.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);
                    int l1 = (int)(f * 19.0F);
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                    //this.blit(matrixStack, k2, j2, 0, 94, 18, 18);
                    //this.blit(matrixStack, k2, j2 + 18 - l1, 18, 112 - l1, 18, l1);
                }
            }

            RenderSystem.disableRescaleNormal();
            RenderSystem.disableBlend();
        }
    }
	
	private void renderHotbarItem(int x, int y, float partialTicks, PlayerEntity player, ItemStack stack)
    {
    	if (stack.getItem() == Items.SLIME_BLOCK) {
    		//return;
    	}
    	
        if (!stack.isEmpty())
        {
            float f = (float)stack.getAnimationsToGo() - partialTicks;

            if (f > 0.0F)
            {
                RenderSystem.pushMatrix();
                float f1 = 1.0F + f / 5.0F;
                RenderSystem.translatef((float)(x + 8), (float)(y + 12), 0.0F);
                RenderSystem.scalef(1.0F / f1, (f1 + 1.0F) / 2.0F, 1.0F);
                RenderSystem.translatef((float)(-(x + 8)), (float)(-(y + 12)), 0.0F);
            }

            mc.getItemRenderer().renderItemAndEffectIntoGUI(player, stack, x, y);

            if (f > 0.0F)
            {
                RenderSystem.popMatrix();
            }

            mc.getItemRenderer().renderItemOverlays(this.mc.fontRenderer, stack, x, y);
        }
    }
	
	public boolean hotbar() {
		return get() && hotbar.get();
	}
	
	public boolean bars() {
		return get() && hotbar.get() && bars.get();
	}
	
	@Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}
	
}
