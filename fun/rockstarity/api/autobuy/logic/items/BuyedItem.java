package fun.rockstarity.api.autobuy.logic.items;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import fun.rockstarity.api.autobuy.logic.interfaces.IAutoBuyItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.item.Item;


@Getter
@AllArgsConstructor
public class BuyedItem implements IAutoBuyItem {

	private final String name;
	private final Item item;
	private final int price, count;
	private final boolean enchanted;
	private final Map<String, Integer> enchants = new HashMap<>();
	private final boolean status;
	private final long time;
	private final AutoBuyItem autoBuyItem;

	public void update(Map<String, Integer> enchants) {
		this.enchants.clear();
		for (Entry<String, Integer> set : enchants.entrySet()) {
			this.enchants.put(set.getKey(), set.getValue());
		}
	}
	
}
