package fun.rockstarity.client.modules.other;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventWorldChange;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.client.modules.player.FreeCam;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

@Info(name = "BaseFinder", type = Category.OTHER, desc = "Находит базы игроков.")
public class BaseFinder extends Module {

    private final CheckBox preview = new CheckBox(this, "Предпоказ базы");

    private BlockPos pos;
    private ArrayList<BlockPos> baseCache = new ArrayList<>();

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            for (Entity entity : mc.world.getAllEntities()) {
                if (entity instanceof ArmorStandEntity) {
                    ArmorStandEntity armorStand = (ArmorStandEntity) entity;
                    pos = entity.getPosition();
                    if (!baseCache.contains(pos) && isArmorStandValid(armorStand)) {
                        Chat.msg(String.format("Мы нашли базу на координатах (%s)", pos.getCoordinatesAsString()));
                        // "вселяемся в арморстенд" чтобы увидеть что внутри базы
                        if (preview.get()) {
                            rock.getModules().get(FreeCam.class).set(true);
                            rock.getModules().get(FreeCam.class).onEnable();
                            mc.player.setPosition(armorStand.getPositionVec().x, armorStand.getPositionVec().y, armorStand.getPositionVec().z);
                        }
                    }
                    baseCache.add(pos);
                }
            }
        }

        if (event instanceof EventWorldChange || (event instanceof EventUpdate && mc.gameSettings.keyBindSneak.isPressed())) {
            mc.setRenderViewEntity(mc.player);
            if (event instanceof EventWorldChange) baseCache.clear();
        }
    }

    public boolean isArmorStandValid(ArmorStandEntity armorStand) {
        String armorStandName = armorStand.getDisplayName().getString();
        return armorStand.isInvisible() && armorStandName.toLowerCase().contains("владелец");
    }

    @Override
    public void onDisable() {
        mc.setRenderViewEntity(mc.player);
        if (preview.get()) rock.getModules().get(FreeCam.class).set(false);
        this.baseCache.clear();
    }

    @Override
    public void onEnable() {}
}
