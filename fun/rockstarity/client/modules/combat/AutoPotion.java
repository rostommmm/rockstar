package fun.rockstarity.client.modules.combat;

import java.util.Comparator;
import java.util.List;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.EventType;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventTrace;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.InventoryUtility;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author ConeTin
 * @since 6 дек. 2023 г.
 */


@Info(name="AutoPotion", desc="Автоматически бафается", type=Category.COMBAT)
public class AutoPotion extends Module {
	
	@AllArgsConstructor
	private enum Buff {
        STRENGTH(Effects.STRENGTH, 5),
        SPEED(Effects.SPEED, 1),
        FIRE_RESIST(Effects.FIRE_RESISTANCE, 12);

        private final Effect effect;
        private final int id;
	}
	
    private final TimerUtility throwTimer = new TimerUtility();
    private final TimerUtility cycleTimer = new TimerUtility();
    private Buff pendingBuff = null;
    private float savedPitch, savedYaw;
    private boolean headDown;
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventMotion motion) {
			if (pendingBuff != null) {
				if (!headDown) {
	                savedPitch = motion.getPitch();
	                savedYaw = motion.getYaw();
	                motion.setPitch(90f);
	                headDown = true;
	                return;
				}
				
	            int slot = findPotionSlot(pendingBuff);
	            if (slot != -1) {
	                throwPotion(slot);
	                throwTimer.reset(); 
	                cycleTimer.reset();
	            }
	            
	            motion.setPitch(savedPitch);
	            motion.setYaw(savedYaw);
	            pendingBuff = null;
	            headDown = false;
	            return;
			}
			
			if (cycleTimer.passed(1300) && throwTimer.passed(100)) {
				for (Buff buff : Buff.values()) {
					if (!mc.player.isPotionActive(buff.effect) && hasPotionInInventory(buff)) {
	                    pendingBuff = buff;
	                    break;
					}
				}
			}
		}
	}
	
	private int findPotionSlot(Buff buff) {
	    for (int i = 0; i < 36; i++) {
	        ItemStack stack = mc.player.inventory.getStackInSlot(i);
	        if (stack.isEmpty() || !(stack.getItem() instanceof SplashPotionItem)) continue;
	        List<EffectInstance> effects = PotionUtils.getEffectsFromStack(stack);
	        for (EffectInstance inst : effects) {
	            if (inst.getPotion() == buff.effect) {
	                return i;
	            }
	        }
	    }
	    return -1;
	}
	
	private boolean hasPotionInInventory(Buff buff) {
	    return findPotionSlot(buff) != -1;
	}
	
    private void throwPotion(int slot) {
        int current = mc.player.inventory.currentItem;
        mc.player.connection.sendPacket(new CHeldItemChangePacket(slot < 9 ? slot : (slot < 36 ? slot - 36 : slot)));
        mc.player.connection.sendPacket(new CPlayerPacket.RotationPacket(mc.player.rotationYaw, 90f, mc.player.isOnGround()));
        mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
        mc.player.connection.sendPacket(new CHeldItemChangePacket(current));
    }
	
	@Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
        cycleTimer.reset();
        throwTimer.reset();
        pendingBuff = null;
        headDown = false;
	}
}
