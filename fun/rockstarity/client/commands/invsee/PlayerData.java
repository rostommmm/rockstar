package fun.rockstarity.client.commands.invsee;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * @author ConeTin
 * @since 16 Р°РїСЂ. 2025вЂЇРі.
 */

@Getter
@RequiredArgsConstructor
public class PlayerData {
	
	private final PlayerEntity player;
	private final Map<Item, ItemStack> inventory = new HashMap<>();

}
