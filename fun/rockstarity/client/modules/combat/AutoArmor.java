package fun.rockstarity.client.modules.combat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Slider;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

@NativeInclude
@Info(name="AutoArmor", desc="Автоматически надевает на вас броню", type=Category.COMBAT)
public class AutoArmor extends Module {
	
	private final Slider passed = new Slider(this, "Задержка").min(1).max(1000).inc(1).set(10).desc("Задержка с которой будет надеваться броня");
	
	private final CheckBox onlyCt = new CheckBox(this, "Только в PVP");
	private final CheckBox ignoreElytra = new CheckBox(this, "Игнор элитры").desc("Не надевать нагрудник вместо элитры");
	
	private final TimerUtility timerUtils = new TimerUtility();
	
    private boolean isNullOrEmpty(ItemStack stack) {
        return !(stack != null && !stack.isEmpty());
    }
    
    @Override
    public void onEvent(Event event) {
        if (event instanceof EventMotion) {
            PlayerInventory inventory = mc.player.inventory;

            int[] bestArmorSlots = new int[4];
            int[] bestArmorValues = new int[4];
            if (Server.hasCT() || !this.onlyCt.get()) {
                evaluateCurrentArmorValues(inventory, bestArmorSlots, bestArmorValues);

                ArrayList<Integer> types = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
                Collections.shuffle(types);

                for (int i : types) {
                    int bestSlot = bestArmorSlots[i];
                    if (bestSlot == -1) {
                        continue;
                    }
                    ItemStack oldArmor = inventory.armorItemInSlot(i);
                    if (!oldArmor.isEmpty() && inventory.getFirstEmptyStack() == -1) {
                        continue;
                    }
                    if (ignoreElytra.get() && mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() instanceof ElytraItem && i == 2) continue;
                    transferArmorItem(inventory, bestSlot, i);
                    break;
                }
            }
        }
    }

    private void evaluateCurrentArmorValues(PlayerInventory inventory, int[] bestArmorSlots, int[] bestArmorValues) {
        for (int type = 0; type < 4; type++) {
            bestArmorSlots[type] = -1;
            ItemStack stack = inventory.armorItemInSlot(type);
            if (!stack.isEmpty() && stack.getItem() instanceof ArmorItem) {
                ArmorItem item = (ArmorItem) stack.getItem();
                bestArmorValues[type] = getArmorValue(item, stack);
            }
        }

        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof ArmorItem) {
                ArmorItem item = (ArmorItem) stack.getItem();
                int armorType = item.getEquipmentSlot().getIndex();
                int armorValue = getArmorValue(item, stack);
                if (armorValue > bestArmorValues[armorType]) {
                    bestArmorSlots[armorType] = slot;
                    bestArmorValues[armorType] = armorValue;
                }
            }
        }
    }


    private void transferArmorItem(PlayerInventory inventory, int bestSlot, int armorType) {
        if (bestSlot < 9) {
            bestSlot += 36;
        }
        if (timerUtils.passed((long) passed.get())) {
            ItemStack oldArmor = inventory.armorItemInSlot(armorType);
            if (!oldArmor.isEmpty()) {
                mc.playerController.windowClick(0, 8 - armorType, 0, ClickType.QUICK_MOVE, mc.player);
            }
            mc.playerController.windowClick(0, bestSlot, 0, ClickType.QUICK_MOVE, mc.player);
            timerUtils.reset();
        }
    }

    private int getArmorValue(ArmorItem item, ItemStack stack) {
        int armorPoints = item.getDamageReduceAmount();
        int prtPoints = 0;
        int armorToughness = (int) item.getToughness();
        int armorType = item.getArmorMaterial().getDamageReductionAmount(EquipmentSlotType.LEGS);
        Enchantment protection = Enchantments.PROTECTION;
        int prtLvl = EnchantmentHelper.getEnchantmentLevel(protection, stack);
        DamageSource dmgSource = DamageSource.causePlayerDamage(mc.player);
        prtPoints = protection.calcModifierDamage(prtLvl, dmgSource);
        return armorPoints * 5 + prtPoints * 3 + armorToughness + armorType;
    }
	
    @Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}
	
}
