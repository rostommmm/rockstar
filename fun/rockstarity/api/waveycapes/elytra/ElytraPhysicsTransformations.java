package fun.rockstarity.api.waveycapes.elytra;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.helpers.game.Chat;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.optifine.Config;

public class ElytraPhysicsTransformations {
    public static void applyMovementTransformation(MatrixStack matrixStack, LivingEntity livingEntity, float partialTicks)
    {
        if (livingEntity instanceof AbstractClientPlayerEntity) {
            AbstractClientPlayerEntity playerEntity = (AbstractClientPlayerEntity) livingEntity;

            if (!playerEntity.isCrouching() && !playerEntity.isElytraFlying()) {
                double d = MathHelper.lerp(partialTicks, playerEntity.prevChasingPosX, playerEntity.chasingPosX) - MathHelper.lerp(partialTicks, playerEntity.prevPosX, playerEntity.getPosX());
                double e = MathHelper.lerp(partialTicks, playerEntity.prevChasingPosY, playerEntity.chasingPosY) - MathHelper.lerp(partialTicks, playerEntity.prevPosY, playerEntity.getPosY());
                double m = MathHelper.lerp(partialTicks, playerEntity.prevChasingPosZ, playerEntity.chasingPosZ) - MathHelper.lerp(partialTicks, playerEntity.prevPosZ, playerEntity.getPosZ());
                float n = playerEntity.prevRenderYawOffset + (playerEntity.renderYawOffset - playerEntity.prevRenderYawOffset);
                double o = MathHelper.sin(n * ((float)Math.PI / 180F));
                double p = -MathHelper.cos(n * ((float)Math.PI / 180F));
                float q = (float) e * 10.0F;
                q = MathHelper.clamp(q, -6.0F, 32.0F);
                float r = (float) (d * o + m * p) * 100.0F;
                r = MathHelper.clamp(r, 0.0F, 150.0F);
                float s = (float) (d * p - m * o) * 100.0F;
                s = MathHelper.clamp(s, -20.0F, 20.0F);

                if (r < 0.0F) {
                    r = 0.0F;
                }
                
                if (r > 165) {
                    r = 165F;
                }
                
                if (q < -5) {
                	q = -5;
                }

                float t = MathHelper.lerp(partialTicks, playerEntity.prevCameraYaw, playerEntity.cameraYaw);
                q += MathHelper.sin(MathHelper.lerp(partialTicks, playerEntity.prevDistanceWalkedModified, playerEntity.distanceWalkedModified) * 6.0F) * 32.0F * t;
                
                matrixStack.translate(0.0D, 0.2F * (r / 150.0F), 0.0D);
                matrixStack.rotate(Vector3f.XP.rotationDegrees(6.0F + r / 2.0F + q));
                matrixStack.rotate(Vector3f.YP.rotationDegrees(s / 2.0F));
                matrixStack.rotate(Vector3f.ZP.rotationDegrees(s / 2.0F));
            }
        }
    }

    public static float setWingRoll(float originalValue, LivingEntity entity)
    {
        if (entity instanceof AbstractClientPlayerEntity) {
            if (entity.isElytraFlying()) return originalValue;

            double deltaX = Math.abs(entity.prevPosX - entity.getPosX());
            double deltaY = Math.abs(entity.prevPosY - entity.getPosY());
            double deltaZ = Math.abs(entity.prevPosZ - entity.getPosZ());

            float speed = (float) MathHelper.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
            if (speed < 0.08) speed = 0;

            speed /= 3;
            speed = MathHelper.clamp(speed, 0f, 0.5f);

            return originalValue - speed;
        } else return originalValue;
    }
}