package fun.rockstarity.api.helpers.math;

import fun.rockstarity.api.IAccess;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;

import static net.minecraft.util.math.MathHelper.clamp;

public class VectorUtility implements IAccess {
    // до лучших времен, да

    public static Vector3d getBestVector(LivingEntity target, float jitterOnBoxValue) {
        double yExpand = clamp(mc.player.getPosYEye() - target.getPosYEye(), target.getHeight() / 2, target.getHeight())
                / (mc.player.isElytraFlying() ? 10 : !mc.gameSettings.keyBindJump.isKeyDown() && mc.player.isOnGround() ?
                target.isSneaking() ? 0.8F : 0.6f : 1F);

        Vector3d finalVector = target.getPositionVec().add(0, yExpand, 0);
        return finalVector.add(jitterOnBoxValue, jitterOnBoxValue / 2, jitterOnBoxValue).subtract(mc.player.getEyePosition(1)).normalize();
    }
}
