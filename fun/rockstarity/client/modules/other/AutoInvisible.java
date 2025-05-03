package fun.rockstarity.client.modules.other;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.math.InventoryUtility;
import fun.rockstarity.api.helpers.player.Inventory;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.potion.*;
import net.minecraft.util.Hand;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Info(name="AutoInvisible", desc="Автоматически пьет зелье невидимости", type= Category.OTHER)
public class AutoInvisible extends Module {

    @Getter
    private final Map<String, EffectInstance> effects = new TreeMap<>();

    private boolean isUsingPotion;
    private boolean hasThrownBottle;

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            boolean hasInvisibility = mc.player.isPotionActive(Effects.INVISIBILITY);
            
            if (!hasInvisibility) {
            	ItemStack offhandItem = mc.player.getHeldItemOffhand();
            	boolean hasPotionInOffhand = isInvisibilityPotion(offhandItem);
            	
            	if (!hasPotionInOffhand) {
            		int potionSlot = findPotion();
            		if (potionSlot != -1) {
            			InventoryUtility.moveItem(potionSlot, 45, true);
            			return;
            		}
            	}
            	
            	if (hasPotionInOffhand) {
            		isUsingPotion = true;
                    mc.getGameSettings().keyBindUseItem.setPressed(true);
                    hasThrownBottle = false;
            	}
            } else if (isUsingPotion) {
            	mc.getGameSettings().keyBindUseItem.setPressed(false);
                isUsingPotion = false;
                ItemStack offhandItem = mc.player.getHeldItemOffhand();
                if (offhandItem.getItem() == Items.GLASS_BOTTLE) {
                    mc.playerController.windowClick(0, 45, 1, ClickType.THROW, mc.player);
                    hasThrownBottle = true;
                }
            }
        }
    }
    @NativeInclude
    private int findPotion() {
        for (int i = 0; i < mc.player.inventory.mainInventory.size(); ++i) {
            ItemStack stack = mc.player.inventory.mainInventory.get(i);
            List<EffectInstance> effects = PotionUtils.getEffectsFromStack(stack);
            for (EffectInstance effect : effects) {
            	if (effect.getPotion() == Effects.INVISIBILITY) {
            		return i < 9 ? 36 + i : i;
            	}
            }
        }
        return -1;
    }
    
    private boolean isInvisibilityPotion(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        
        List<EffectInstance> effects = PotionUtils.getEffectsFromStack(stack);
        for (EffectInstance effect : effects) {
            if (effect.getPotion() == Effects.INVISIBILITY) {
                return true;
            }
        }
        return false;
    }
    @NativeInclude
    @Override
    public void onEnable() {
        isUsingPotion = false;
        hasThrownBottle = false;
    }
    @NativeInclude
    @Override
    public void onDisable() {
        if (isUsingPotion) {
            mc.getGameSettings().keyBindUseItem.setPressed(false);
        }
        isUsingPotion = false;
        hasThrownBottle = false;
    }
}
