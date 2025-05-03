package fun.rockstarity.api.render.optimize.culling.processor;

import fun.rockstarity.api.render.optimize.culling.util.DataProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

public class Provider implements DataProvider {

    private final Minecraft client = Minecraft.getInstance();
    private ClientWorld world = null;

    @Override
    public boolean prepareChunk(int chunkX, int chunkZ) {
        world = client.world;
        return world != null;
    }

    @Override
    public boolean isOpaqueFullCube(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        //#if MC <= 12101
        //$$ return world.getBlockState(pos).isSolidRender(world, pos);
        //#else
        return world.getBlockState(pos).isSolid();
        //#endif
    }

    @Override
    public void cleanup() {
        world = null;
    }

}