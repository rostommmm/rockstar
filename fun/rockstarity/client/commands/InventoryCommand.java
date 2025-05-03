package fun.rockstarity.client.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fun.rockstarity.api.commands.CmdInfo;
import fun.rockstarity.api.commands.Command;
import fun.rockstarity.api.commands.CommandParameter;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.interfaces.Jsonable;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import lombok.Getter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author ConeTin
 * @since 26 июл. 2024 г.
 */
@NativeInclude
@Getter
@CmdInfo(names = { "inv", "inventory", "slot", "slots" }, desc = "Позволяет сохранять и загружать положение предметов в инвентаре")
public class InventoryCommand extends Command implements Jsonable {

    private final Map<String, Map<Integer, Integer>> inventories = new HashMap<>();

    CommandParameter save = new CommandParameter(this, "save", "create", "add", "сохранить");
    CommandParameter load = new CommandParameter(this, "load", "use", "загрузить");
    
    @Override
    public void execute(String[] args) {
        if (contains(args[0], save)) {
            saveInventory(args.length > 1 ? args[1] : "default");
            rock.getAlertHandler().alert("Положение предметов сохранено", AlertType.INFO);
        } else if (contains(args[0], load)) {
            loadInventory(args.length > 1 ? args[1] : "default");
        }
    }

    private void saveInventory(String name) {
        if (mc.player == null) return;

        Map<Integer, Integer> inventory = new HashMap<>();
        for (int i = 0; i <= 45; i++) {
            ItemStack stack = mc.player.container.getSlot(i).getStack();
            if (!stack.isEmpty()) {
                inventory.put(i, Item.getIdFromItem(stack.getItem()));
            }
        }
        inventories.put(name, inventory);
    }

    public void loadInventory(String name) {
        if (inventories.containsKey(name)) {
            for (Entry<Integer, Integer> entry : inventories.get(name).entrySet()) {
                int slotIndex = entry.getKey();
                Item item = Item.getItemById(entry.getValue());
                Player.moveItem(Player.findItem(45, item), slotIndex, true);
            }
            rock.getAlertHandler().alert("Положение предметов загружено", AlertType.INFO);
        } else {
            rock.getAlertHandler().alert("Сохранение с этим именем не найдено", AlertType.ERROR);
        }
    }

    @Override
    public JsonObject save() {
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<String, Map<Integer, Integer>> entry : inventories.entrySet()) {
            JsonObject innerJson = new JsonObject();
            for (Map.Entry<Integer, Integer> innerEntry : entry.getValue().entrySet()) {
                innerJson.addProperty(innerEntry.getKey().toString(), innerEntry.getValue());
            }
            jsonObject.add(entry.getKey(), innerJson);
        }
        return jsonObject;
    }

    @Override
    public void load(JsonElement jsonElement) {
        inventories.clear();
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            JsonObject innerJson = entry.getValue().getAsJsonObject();
            Map<Integer, Integer> innerMap = new HashMap<>();
            for (Map.Entry<String, JsonElement> innerEntry : innerJson.entrySet()) {
                Integer intKey = Integer.valueOf(innerEntry.getKey());
                Integer value = innerEntry.getValue().getAsInt();
                innerMap.put(intKey, value);
            }
            inventories.put(entry.getKey(), innerMap);
        }
    }
}