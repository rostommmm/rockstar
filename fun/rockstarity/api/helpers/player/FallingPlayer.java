package fun.rockstarity.api.helpers.player;

import fun.rockstarity.api.helpers.math.aura.IdealHitUtility;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class FallingPlayer {
    private final ClientPlayerEntity player;
    private double x, y, z;
    private double motionX, motionY, motionZ;
    private final float yaw;
    private int simulatedTicks;

    public FallingPlayer(ClientPlayerEntity player, double x, double y, double z, double motionX, double motionY, double motionZ, float yaw) {
        this.player = player;
        this.x = x;
        this.y = y;
        this.z = z;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        this.yaw = yaw;
        this.simulatedTicks = 0;
    }

    public static FallingPlayer fromPlayer(ClientPlayerEntity player) {
        return new FallingPlayer(
                player,
                player.getPositionVec().getX(),
                player.getPositionVec().getY(),
                player.getPositionVec().getZ(),
                player.getMotion().x,
                player.getMotion().y,
                player.getMotion().z,
                player.rotationYaw
        );
    }

    public boolean findFall(float fallDist) {
    	Vector3d rotationVec = player.getLookVec();
        double tempMotionX = motionX;
        double tempMotionY = motionY;
        double tempMotionZ = motionZ;

        double d = 0.08;
        float n = MathHelper.cos(player.rotationPitch * 0.017453292f);
        n = (float) (n * n * Math.min(rotationVec.length() / 0.4, 1.0));

        Vector3d vec3d = new Vector3d(tempMotionX, tempMotionY, tempMotionZ).add(0.0, d * (-1.0 + n * 0.75), 0.0);
        tempMotionY = vec3d.y * 0.9800000190734863;

        return tempMotionY < fallDist;
    }
    
    public boolean findFall(float fallDist, int ticks) {
        Vector3d rotationVec = player.getLookVec();
        double tempMotionX = motionX;
        double tempMotionY = motionY;
        double tempMotionZ = motionZ;

        double d = 0.08;
        float n = MathHelper.cos(player.rotationPitch * 0.017453292f);
        n = (float) (n * n * Math.min(rotationVec.length() / 0.4, 1.0));

        for (int i = 0; i < ticks; i++) {
            Vector3d vec3d = new Vector3d(tempMotionX, tempMotionY, tempMotionZ).add(0.0, d * (-1.0 + n * 0.75), 0.0);
            tempMotionY = vec3d.y * 0.9800000190734863;

            if (tempMotionY >= fallDist) {
                return false;
            }
        }

        return true;
    }
    
}
