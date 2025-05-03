package fun.rockstarity.client.modules.player;

import baritone.api.utils.RotationUtils;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventTick;
import fun.rockstarity.api.events.list.game.inputs.EventInput;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.math.aura.AuraUtility;
import fun.rockstarity.api.helpers.math.aura.IdealHitUtility;
import fun.rockstarity.api.helpers.math.aura.Rotation;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.client.modules.other.Baritone;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static net.minecraft.util.math.MathHelper.wrapDegrees;

@Info(name = "CreeperFarm", desc = "Автоматический фарм на криперах. Под анархии фт/спуки и т.п.", type = Category.PLAYER)
public class AutoCreeperFarm extends Module {
	
	private boolean retreating;
	private long lastHitTime;
	
    @Override
    public void onEvent(Event event) {
        Entity creeperTarget = AuraUtility.calculateCreeper(50);

        if (event instanceof EventMotion e) {
            long now = System.currentTimeMillis();

            if (retreating) {
                double dx = mc.player.getPosX() - creeperTarget.getPosX();
                double dz = mc.player.getPosZ() - creeperTarget.getPosZ();
                double dist = Math.sqrt(dx*dx + dz*dz);
                if (dist > 6 || now - lastHitTime > 300) {
                    retreating = false;
                } else {
                    mc.player.getMotion().x = dx / dist * 0.3;
                    mc.player.getMotion().z = dz / dist * 0.3;
                    return;
                }
            }

            boolean canSee = MathUtility.rayTraceWithBlock(3,
                    e.getYaw(), e.getPitch(), mc.player, creeperTarget, false);
            boolean inRange = AuraUtility.distanceTo(AuraUtility.getPoint((LivingEntity) creeperTarget)) <= 3 &&
                    mc.player.getDistance(creeperTarget) <= 6;

            if ((!mc.gameSettings.keyBindJump.isKeyDown() && mc.player.isOnGround() || mc.player.fallDistance > 0)
                    && mc.player.getCooledAttackStrength() >= IdealHitUtility.getAICooldown()
                    && inRange && canSee) {
                mc.playerController.attackEntity(mc.player, creeperTarget);
                mc.player.swingArm(Hand.MAIN_HAND);
                lastHitTime = now;
                retreating = true;
            }
        }
    }

    @Override
    public void onEnable() {
		rock.getModules().get(Baritone.class).set(true);
        mc.player.sendChatMessage("#follow entity creeper");
		mc.player.sendChatMessage("#allowBreak false");
    }

    @Override
    public void onDisable() {
        mc.player.sendChatMessage("#stop");
    }
}
