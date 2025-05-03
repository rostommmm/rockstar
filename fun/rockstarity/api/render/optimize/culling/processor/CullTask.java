package fun.rockstarity.api.render.optimize.culling.processor;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import com.mojang.blaze3d.systems.RenderSystem;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.helpers.render.PositionTracker;
import fun.rockstarity.api.render.optimize.culling.util.OcclusionCullingInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.chunk.Chunk;
import net.optifine.reflect.Reflector;

public class CullTask implements Runnable, IAccess {

    public boolean requestCull = false;
    public boolean disableEntityCulling = false;
    public boolean disableBlockEntityCulling = false;

    private final OcclusionCullingInstance culling;
    private final Minecraft client = Minecraft.getInstance();
    private final int sleepDelay = CullingConfig.sleepDelay;
    private final int hitboxLimit = CullingConfig.hitboxLimit;
    private final Set<TileEntityType<?>> blockEntityWhitelist;
    private final Set<EntityType<?>> entityWhistelist;
    public long lastTime = 0;

    // reused preallocated vars
    private Vector3d lastPos = new Vector3d(0, 0, 0);
    private Vector3d aabbMin = new Vector3d(0, 0, 0);
    private Vector3d aabbMax = new Vector3d(0, 0, 0);

    public CullTask(OcclusionCullingInstance culling, Set<TileEntityType<?>> blockEntityWhitelist,
            Set<EntityType<?>> entityWhistelist) {
        this.culling = culling;
        this.blockEntityWhitelist = blockEntityWhitelist;
        this.entityWhistelist = entityWhistelist;
    }

    @Override
    public void run() {
        while (client.isRunning()) { // client.isRunning() returns false at the start?!?
            try {
                Thread.sleep(sleepDelay);
                
                process();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Shutting down culling task!");
    }
    
    private void process() {
    	if (Player.isInGame() && client.player.ticksExisted > 10) {
            // getEyePosition can use a fixed delta as its debug only anyway
            Vector3d cameraMC = //rock.isDebugging() ? client.player.getEyePosition(0) : 
            	client.gameRenderer.getActiveRenderInfo().getProjectedView();
            
            if (requestCull
                    || !(cameraMC.x == lastPos.x && cameraMC.y == lastPos.y && cameraMC.z == lastPos.z)) {
                long start = System.currentTimeMillis();
                requestCull = false;
                lastPos.set(cameraMC.x, cameraMC.y, cameraMC.z);
                Vector3d camera = lastPos;
                culling.resetCache();
                cullBlockEntities(cameraMC, camera);
                cullEntities(cameraMC, camera);
                cullParticles(cameraMC, camera);
                //cullChunks(cameraMC, camera);
                lastTime = (System.currentTimeMillis() - start);
                //System.out.println(lastTime);
            }
        }
    }
    
    private void cullParticles(Vector3d cameraMC, Vector3d camera) {
        Collection<IParticleRenderType> collection = ParticleManager.TYPES;

        for (IParticleRenderType iparticlerendertype : collection)
        {
            if (iparticlerendertype != IParticleRenderType.NO_RENDER)
            {
            	if (mc.particles.byType.get(iparticlerendertype) == null) continue;
                Iterator<Particle> iterable = mc.particles.byType.get(iparticlerendertype).iterator();

                if (iterable != null)
                {
                	Particle cullable = null;
                    while (iterable.hasNext()) {
						try {
							cullable = iterable.next();
						} catch (NullPointerException | ConcurrentModificationException
								| ArrayIndexOutOfBoundsException ex) {
							break; // We are not synced to the main thread, so NPE's/CME are allowed here and way
									// less
									// overhead probably than trying to sync stuff up for no really good reason
						}
                    	AxisAlignedBB boundingBox = cullable.getBoundingBox();
                        if (boundingBox.getXSize() > hitboxLimit || boundingBox.getYSize() > hitboxLimit
                                || boundingBox.getZSize() > hitboxLimit) {
                            cullable.setCulled(false); // To big to bother to cull
                            
                            continue;
                        }
                        aabbMin.set(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
                        aabbMax.set(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
                        boolean visible = culling.isAABBVisible(aabbMin, aabbMax, camera);
                        cullable.setCulled(!visible);
                    }
                }
            }
        }
    }

    private void cullChunks(Vector3d cameraMC, Vector3d camera) {
        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
            	int chunkX = client.player.chunkCoordX + x;
            	int chunkZ = client.player.chunkCoordZ + z;
            	
                Chunk chunk = client.world.getChunk(chunkX, chunkZ);
                
                float height = chunk.getHeight();
                //height = 5;
                AxisAlignedBB boundingBox = new AxisAlignedBB(chunkX*16, 0, chunkZ*16, chunkX*16+16, height, chunkZ*16+16);
                
                if (!PositionTracker.isInView(boundingBox)) {
                	chunk.setCulled(true);
                }
                
                aabbMin.set(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
                aabbMax.set(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
                
                boolean visible = culling.isAABBVisible(aabbMin, aabbMax, camera);
                chunk.setCulled(!visible);
            }
        }
    }
    
    private void cullEntities(Vector3d cameraMC, Vector3d camera) {
        if (disableEntityCulling) {
            return;
        }
        Entity entity = null;
        Iterator<Entity> iterable = client.world.getAllEntities().iterator();
        while (iterable.hasNext()) {
            try {
                entity = iterable.next();
            } catch (NullPointerException | ConcurrentModificationException | ArrayIndexOutOfBoundsException ex) {
                break; // We are not synced to the main thread, so NPE's/CME are allowed here and way
                       // less
                       // overhead probably than trying to sync stuff up for no really good reason
            }
            if (entity == null) {
                // assume the iterator is broken, cancel the loop
                // https://github.com/tr7zw/EntityCulling/issues/168
                break;
            }
            //if (!(entity instanceof Cullable)) {
            //    continue; // Not sure how this could happen outside from mixin screwing up the inject into
            //              // Entity
            //}
            if (entityWhistelist.contains(entity.getType())) {
                continue;
            }
            Entity cullable = entity;
            if (mc.isEntityGlowing(entity) || isSkippableArmorstand(entity)) {
                cullable.setCulled(false);
                continue;
            }
            if (entity.getPositionVec().distanceTo(cameraMC) > CullingConfig.tracingDistance) {
                cullable.setCulled(false); // If your entity view distance is larger than tracingDistance just
                                           // render it
                continue;
            }
            AxisAlignedBB boundingBox = entity.getRenderBoundingBox();
            if (boundingBox.getXSize() > hitboxLimit || boundingBox.getYSize() > hitboxLimit
                    || boundingBox.getZSize() > hitboxLimit) {
                cullable.setCulled(false); // To big to bother to cull
                
                continue;
            }
            aabbMin.set(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
            aabbMax.set(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
            boolean visible = culling.isAABBVisible(aabbMin, aabbMax, camera);
            cullable.setCulled(!visible);
            //if (cullable.getName().getString().equals(mc.player.getNameClear()) && cullable != mc.player)
            //Debugger.overlay(cullable.isCulled() && cullable.isPrevCulled() ? "РќРµ СЂРµРЅРґРµСЂСЋ" : "Р РµРЅРґРµСЂСЋ");
        }
    }

    private void cullBlockEntities(Vector3d cameraMC, Vector3d camera) {
        if (disableBlockEntityCulling || mc.world == null) {
            return;
        }
        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
                Chunk chunk = client.world.getChunk(client.player.chunkCoordX + x,
                        client.player.chunkCoordZ + z);
                Iterator<Entry<BlockPos, TileEntity>> iterator = chunk.getTileEntityMap().entrySet().iterator();
                Entry<BlockPos, TileEntity> entry;
                while (iterator.hasNext()) {
                    try {
                        entry = iterator.next();
                    } catch (NullPointerException | ConcurrentModificationException ex) {
                        break; // We are not synced to the main thread, so NPE's/CME are allowed here and way
                               // less
                               // overhead probably than trying to sync stuff up for no really good reason
                    }
                    if (entry == null) {
                        // assume the iterator is broken, cancel the loop
                        // https://github.com/tr7zw/EntityCulling/issues/168
                        break;
                    }
                    if (blockEntityWhitelist.contains(entry.getValue().getType())) {
                        continue;
                    }
                    TileEntity cullable = entry.getValue();
                    if (!cullable.isForcedVisible()) {
                        BlockPos pos = entry.getKey();
                        if (closerThan(pos, cameraMC, 64)) { // 64 is the fixed max tile view distance
                        	AxisAlignedBB boundingBox = new AxisAlignedBB(pos);
                            if (boundingBox.getXSize() > hitboxLimit || boundingBox.getYSize() > hitboxLimit
                                    || boundingBox.getZSize() > hitboxLimit) {
                                cullable.setCulled(false); // To big to bother to cull
                                continue;
                            }
                            aabbMin.set(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
                            aabbMax.set(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
                            boolean visible = culling.isAABBVisible(aabbMin, aabbMax, camera);
                            cullable.setCulled(!visible);
                        }
                    }
                }

            }
        }
    }

    private boolean isSkippableArmorstand(Entity entity) {
        return entity instanceof ArmorStandEntity ent && ent.isInvisible();
    }

    // Vec3i forward compatibility functions
    private static boolean closerThan(BlockPos blockPos, Vector3d Vector3d, double d) {
        return distSqr(blockPos, Vector3d.x, Vector3d.y, Vector3d.z, true) < d * d;
    }

    private static double distSqr(BlockPos blockPos, double d, double e, double f, boolean bl) {
        double g = bl ? 0.5D : 0.0D;
        double h = (double) blockPos.getX() + g - d;
        double i = (double) blockPos.getY() + g - e;
        double j = (double) blockPos.getZ() + g - f;
        return h * h + i * i + j * j;
    }
}