package fun.rockstarity.api.helpers.render;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.opengl.GL11;
import fun.rockstarity.api.IAccess;
import lombok.experimental.UtilityClass;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

import static net.minecraft.client.renderer.WorldRenderer.frustum;

@UtilityClass
public class PositionTracker implements IAccess {
    private final IntBuffer viewport;
    private final FloatBuffer modelview;
    private final FloatBuffer projection;
    private final FloatBuffer vector;

    static {
        viewport = GLAllocation.createDirectIntBuffer(16);
        modelview = GLAllocation.createDirectFloatBuffer(16);
        projection = GLAllocation.createDirectFloatBuffer(16);
        vector = GLAllocation.createDirectFloatBuffer(4);
    }

    public boolean isInView(Entity ent) {
    	if (mc.getRenderViewEntity() == null ||mc.getRenderManager().info == null) return false;
        frustum.setCameraPosition(mc.getRenderManager().info.getProjectedView().x, mc.getRenderManager().info.getProjectedView().y,mc.getRenderManager().info.getProjectedView().z);
        return frustum.isBoundingBoxInFrustum(ent.getBoundingBox()) || ent.ignoreFrustumCheck;
    }

    public boolean isInView(Vector3d vec) {
        assert mc.getRenderViewEntity() != null;
        frustum.setCameraPosition(mc.getRenderManager().info.getProjectedView().x, mc.getRenderManager().info.getProjectedView().y,mc.getRenderManager().info.getProjectedView().z);
        return frustum.isBoundingBoxInFrustum(new AxisAlignedBB(vec.add(-0.5,-0.5, -0.5), vec.add(0.5,0.5, 0.5)));
    }
    
    public boolean isInView(double x, double y, double z) {
        return isInView(new Vector3d(x, y, z));
    }
    
    public boolean isInView(AxisAlignedBB box) {
        if (mc.getRenderViewEntity() == null) {
            return false;
        }
        return frustum.isBoundingBoxInFrustum(box);
    }

}