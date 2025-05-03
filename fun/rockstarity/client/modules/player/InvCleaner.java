package fun.rockstarity.client.modules.player;

import fun.rockstarity.api.autobuy.logic.items.MinecraftItem;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventTick;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.ItemSelect;
import fun.rockstarity.api.modules.settings.list.Slider;
import net.minecraft.inventory.container.ClickType;

@Info(name = "InvCleaner", desc = "Выкидывает ненужные предметы", type = Category.PLAYER)
public class InvCleaner extends Module {
	
    ItemSelect itemSelect = new ItemSelect(this, "Предметы");
    
    Slider speed = new Slider(this, "Задержка").set(100).min(0).max(1000).inc(50);
    
    TimerUtility timer = new TimerUtility();

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventTick && timer.passed(speed.get())) {
            for (MinecraftItem item : itemSelect.getItems()) {
                int slot = Player.findItem(45, item.getItem());
                if (slot != -1) {
                    mc.playerController.windowClick(0, slot, 1, ClickType.THROW, mc.player);
                    timer.reset();
                    break;
                }
            }
        }
    }

    @Override
    public void onEnable() {}
    @Override
    public void onDisable() {}
}
