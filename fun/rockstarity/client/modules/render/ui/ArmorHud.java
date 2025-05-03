package fun.rockstarity.client.modules.render.ui;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.Reacher;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.animation.infinity.InfinityAnimation;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.client.modules.player.Reach;
import fun.rockstarity.client.modules.render.Interface;
import fun.rockstarity.client.modules.render.Interface.UIElement;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.vector.Vector2f;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author Malecharik
 * @since 4 РјР°СЏ 2024 Рі. 19:04:48
 */

public class ArmorHud extends UIElement {
	
	private final CheckBox percents = new CheckBox(this, "РџСЂРѕС‡РЅРѕСЃС‚СЊ РІ РїСЂРѕС†РµРЅС‚Р°С…").hide(() -> !this.get());
	private final Animation percentsAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
    private ItemStack[] demoArmor = new ItemStack[4];
    private final TimerUtility timer = new TimerUtility();
	
	@NativeInclude
	public ArmorHud(Interface ui, Select select) {
		super(select, "Р‘СЂРѕРЅСЏ", new Rect(60, 40, 0, 0));

		this.set(true);
	}
	
	private void refreshDemoArmor() {
	    Item[][] armorPieces = {
	        { Items.LEATHER_HELMET, Items.IRON_HELMET, Items.GOLDEN_HELMET, Items.DIAMOND_HELMET, Items.NETHERITE_HELMET },
	        { Items.LEATHER_CHESTPLATE, Items.IRON_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.NETHERITE_CHESTPLATE },
	        { Items.LEATHER_LEGGINGS, Items.IRON_LEGGINGS, Items.GOLDEN_LEGGINGS, Items.DIAMOND_LEGGINGS, Items.NETHERITE_LEGGINGS },
	        { Items.LEATHER_BOOTS, Items.IRON_BOOTS, Items.GOLDEN_BOOTS, Items.DIAMOND_BOOTS, Items.NETHERITE_BOOTS }
	    };
	    
	    for (int i = 0; i < 4; i++) {
	        int randomIndex = (int)(Math.random() * armorPieces[i].length);
	        demoArmor[i] = new ItemStack(armorPieces[i][randomIndex]);
	        
	        if (Math.random() < 0.7) {
	            int maxDamage = demoArmor[i].getMaxDamage();
	            int damage = (int)(Math.random() * maxDamage * 0.8);
	            demoArmor[i].setDamage(damage);
	        }
	    }
	}
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventRender2D e) {
			this.renderArmor(e.getMatrixStack());
		}
	}
	
    private boolean shouldShowDemoArmor() {
        boolean hasArmor = false;
        for (ItemStack item : mc.player.getArmorInventoryList()) {
            if (!item.isEmpty()) {
                hasArmor = true;
                break;
            }
        }
        
        return (!hasArmor && mc.currentScreen instanceof ChatScreen);
    }
	
	private void renderArmor(MatrixStack matrixStack) {
		float height = 16;
		int itemCount = 0;
		
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
		
		//Chat.msg(minDistanceSquared);
		
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
		
		float x = MathUtility.interpolate(closestPoint.x, this.draggable.getX(), this.showing.get()), 
			  y = MathUtility.interpolate(closestPoint.y, this.draggable.getY(), this.showing.get());
		
		float maxWidth = 0;


		boolean vertical = draggable.getX() <= 25 || draggable.getX() >= sr.getScaledWidth() - 80;
		
		ItemStack[] armorItems = new ItemStack[4];
		
		boolean showDemoArmor = shouldShowDemoArmor();
		
		if (showDemoArmor && timer.passed(2000)) {
            refreshDemoArmor();
            timer.reset();
        }

		if (showDemoArmor) {
			for (int i = 0; i < 4; i++) {
				armorItems[i] = demoArmor[3 - i];
			}
		} else {
            int index = 0;
            for (ItemStack itemStack : mc.player.getArmorInventoryList()) {
                armorItems[index++] = itemStack;
            }
		}
		for (int i = armorItems.length - 1; i >= 0; i--) {
		    ItemStack itemStack = armorItems[i];
		    if (itemStack.isEmpty() && !showDemoArmor) continue;
		    
		    if (itemStack.isEmpty()) {
                if (vertical) {
                    y += 20;
                } else {
                    x += 20;
                }
                continue;
		    }
		    
	        mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, (int) x, (int) y);
	        mc.getItemRenderer().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, (int) x, (int) y, null);
	        
	        
	        int durability = itemStack.getMaxDamage() - itemStack.getDamage();
	        float durabilityPercentage = (float) durability / itemStack.getMaxDamage() * 100;
	        if (Float.isNaN(durabilityPercentage)) durabilityPercentage = 100;
	        String durabilityString = String.format("%.0f%%", durabilityPercentage);
	        
	        float textWidth = bold.get(12).getWidth(durabilityString);
	        this.percentsAnim.setForward(this.percents.get());
	        if (!this.percentsAnim.finished(false)) bold.get(12).draw(matrixStack, durabilityString, (int) x + 8 - textWidth / 2 + 0.8f, vertical ? y - 5.6f : (int) y - 8f, FixColor.WHITE.alpha(this.showing.get() * this.percentsAnim.get()));
			
	        maxWidth = Math.max(maxWidth, textWidth);
	        
	        if (vertical) {
	        	y += 20;
	        } else {
	        	x += 20;
	        }
	        
	        itemCount++;
		}
		
	    if (vertical) {
	        this.draggable.setWidth(maxWidth);
	        this.draggable.setHeight(itemCount * 20);
	    } else {
			this.draggable.setWidth(x - this.draggable.getX());
			this.draggable.setHeight(height);
	    }
	}
}
