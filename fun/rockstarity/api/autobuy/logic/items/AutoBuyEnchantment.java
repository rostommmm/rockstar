package fun.rockstarity.api.autobuy.logic.items;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.enchantment.Enchantment;

/**
 * @author ConeTin
 * @since 3 РјР°СЏ 2024 Рі.
 */


@Getter @Setter
@AllArgsConstructor
public class AutoBuyEnchantment {

	private Enchantment enchantment;
	private int lvl;
	
}
