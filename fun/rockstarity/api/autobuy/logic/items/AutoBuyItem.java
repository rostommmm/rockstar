package fun.rockstarity.api.autobuy.logic.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import fun.rockstarity.api.autobuy.logic.interfaces.IAutoBuyItem;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.item.Item;


@Getter @Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class AutoBuyItem implements IAutoBuyItem {

	final ArrayList<MinecraftItem> items = new ArrayList<>();
	int maxPrice;
	int minCount;
	int minDurability;
	int sellPrice = 0;
	boolean enchanted;
	final Map<String, Integer> enchants = new HashMap<>();
	boolean parsed;
	int updates;
	
	public AutoBuyItem(Item item, int maxPrice, int sellPrice, boolean enchanted) {
		this.items.add(new MinecraftItem(item));
		this.maxPrice = maxPrice;
		this.sellPrice = sellPrice;
		this.enchanted = enchanted;
	}
	
	public MinecraftItem getFirst() {
		return items.get(0);
	}
	
	public void update(Map<String, Integer> enchants) {
		this.enchants.clear();
		for (Entry<String, Integer> set : enchants.entrySet()) {
			this.enchants.put(set.getKey(), set.getValue());
		}
	}
	
	public void updateItems(List<MinecraftItem> items) {
		this.items.clear();
		for (MinecraftItem set : items) {
			this.items.add(set);
		}
	}
	
	public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("maxPrice", this.maxPrice);
        json.put("minCount", this.minCount);
        json.put("minDurability", this.minDurability);
        json.put("sellPrice", this.sellPrice);
        json.put("enchanted", this.enchanted);
        
        JSONArray itemsArray = new JSONArray();
        for (MinecraftItem entry : this.items) {
            JSONObject itemJson = new JSONObject();
            //itemJson.put("name", entry.getKey());
            itemJson.put("item", Item.getIdFromItem(entry.getItem()));
            itemsArray.add(itemJson);
        }
        json.put("items", itemsArray);

        JSONArray enchantsArray = new JSONArray();
        for (Map.Entry<String, Integer> entry : enchants.entrySet()) {
            JSONObject enchantJson = new JSONObject();
            enchantJson.put("id", entry.getKey());
            enchantJson.put("lvl", entry.getValue());
            enchantsArray.add(enchantJson);
        }
        json.put("enchants", enchantsArray);

        return json;
    }

    public static AutoBuyItem fromJson(JSONObject json) {
    	int maxPrice = ((Long) json.get("maxPrice")).intValue();
    	int minCount = ((Long) json.get("minCount")).intValue();
    	int minDurability = ((Long) json.get("minDurability")).intValue();
        int sellPrice =  ((Long) json.get("sellPrice")).intValue();
        boolean enchanted = (boolean) json.get("enchanted");

        ArrayList<MinecraftItem> items = new ArrayList<>();
        JSONArray itemsArray = (JSONArray) json.get("items");
        if (itemsArray != null) {
            for (Object obj : itemsArray) {
                JSONObject itemJson = (JSONObject) obj;
                long id = (long) itemJson.get("item");
                //String itemName = ((String) itemJson.get("name"));
                items.add(new MinecraftItem(Item.getItemById((int) id)));
            }
        }
        
        Map<String, Integer> enchants = new HashMap<>();
        JSONArray enchantsArray = (JSONArray) json.get("enchants");
        if (enchantsArray != null) {
            for (Object obj : enchantsArray) {
                JSONObject enchantJson = (JSONObject) obj;
                long enchantmentLvl = (long) enchantJson.get("lvl");
                String enchantName = ((String) enchantJson.get("id"));
                enchants.put(enchantName, (int) enchantmentLvl);
            }
        }
        
        AutoBuyItem item1 = new AutoBuyItem(maxPrice, minCount, minDurability, sellPrice, enchanted, false, 0);
        item1.update(enchants);
        item1.updateItems(items);
        return item1;
    }

}
