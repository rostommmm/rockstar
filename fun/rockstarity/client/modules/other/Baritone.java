package fun.rockstarity.client.modules.other;

import java.util.ArrayList;
import java.util.List;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import net.minecraft.client.Minecraft;

@Info(name="Baritone", desc="Включение/отключение Baritone", type=Category.OTHER)
public class Baritone extends Module {

    @Override
    public void onEvent(Event event) {
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }
}
