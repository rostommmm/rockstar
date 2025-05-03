package fun.rockstarity.api.helpers.math.aura;

import java.util.HashSet;
import java.util.Set;

import fun.rockstarity.api.helpers.game.Chat;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.ItemStack;

/**
 * @author ConeTin
 * @since 29 РёСЋРЅ. 2024 Рі.
 */

@UtilityClass
public class BotUtility {
	@Getter
	private final Set<String> verifed = new HashSet<>();
	
	public boolean isRWBot(LivingEntity ent) {
		String name = ent.getScoreboardName();
		
		if (verifed.contains(name))
			return false;
		
		if (ent.getAbsorptionAmount() != 0
			|| ent.isHandActive()
			|| ent.getMotion().y != 0
			//|| ent.getTotalArmorValue() == 0
			//|| isFullArmor(ent, ArmorMaterial.NETHERITE)
			//|| isFullArmor(ent, ArmorMaterial.DIAMOND)
			//|| isFullArmor(ent, ArmorMaterial.LEATHER)
			//|| ent.getHealth() == 20
			)
			
			verifed.add(name);
		/*
		if (ent.getAbsorptionAmount() != 0)
			Chat.debug("ent.getAbsorptionAmount() != 0");
		
		if (ent.isHandActive())
			Chat.debug("ent.isHandActive()");
		
		if (ent.getMotion().y != 0)
			Chat.debug("ent.getMotion().y != 0");
		*/
		
		return true;
	}

	public boolean isFullArmor(LivingEntity entity, ArmorMaterial material) {
		for (ItemStack item : entity.getArmorInventoryList()) {
			if (item.getItem() instanceof DyeableArmorItem armor && armor.getArmorMaterial() != material) {
				return false;
			}
		}
		return true;
	}
	
}
