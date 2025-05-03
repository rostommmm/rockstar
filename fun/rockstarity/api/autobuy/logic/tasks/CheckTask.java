package fun.rockstarity.api.autobuy.logic.tasks;

import fun.rockstarity.api.helpers.game.ItemUtility;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CClickWindowPacket;

/**
 * @author ConeTin
 * @since 21 Р°РїСЂ. 2024 Рі.
 */

@Getter @Setter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CheckTask {
	
	String seller;
	String name;
	int price;
	Runnable onSuccess;
	Runnable onFake;
	
	public boolean check(ItemStack stack) {
		int price = ItemUtility.getPrice(stack);
		String seller = ItemUtility.getSeller(stack);
		
		return this.price == price
				&& name.equals(stack.getDisplayName().getString())
				&& this.seller.equals(seller);
	}
	
}
