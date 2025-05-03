package fun.rockstarity.client.modules.player;

import java.util.Comparator;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventSpawn;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventTrace;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.secure.Debugger;
import fun.rockstarity.client.modules.combat.Aura;
import lombok.Getter;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

/**
 * @author Malecharik
 * @since 8 мая 2024 г. 21:34:09
 */


@Info(name = "AutoPearl", desc = "Кидает перл за таргетом", type = Category.PLAYER)
public class AutoPearl extends Module {
	
	private Runnable postSyncAction;
    private final TimerUtility delayTimer = new TimerUtility();
    private BlockPos targetBlock;
    private int lastPearlId;
    private int lastOurPearlId;
    private float rotationYaw, rotationPitch;
    @Getter private int tick;
    
    public AutoPearl() {
    	super(11);
    }
	
	@Override
    public void onEvent(Event event) {
		if (tick > 1) {
			Player.look(event, rotationYaw, rotationPitch, false);
		}
		if (event instanceof EventSpawn e) {
			if (e.getEntity() instanceof EnderPearlEntity)
	            mc.world.getPlayers().stream()
	                    .min(Comparator.comparingDouble((p) -> p.getDistanceSq(e.getEntity().getPositionVec())))
	                    .ifPresent((player) -> {
	                        if (player.equals(mc.player))
	                            lastOurPearlId = e.getEntity().getEntityId();
	                    });
		}
		
		if (event instanceof EventMotion e && rock.getModules().get(Aura.class).getPrevTarget() != null) {
			if (postSyncAction != null) {
				if (tick > 2) {
					postSyncAction.run();
					postSyncAction = null;
					tick = 0;
				} else {
					tick++;
				}
			}
			
			// Анти селфкилл
	        if (mc.player.getHealth() < 5)
	            return;
	        
	        // Антиспам
	        if (!delayTimer.passed(1000))
	            return;
	       
	        for (Entity ent : mc.world.getAllEntities()) {
	            if (!(ent instanceof EnderPearlEntity)) continue;
	            if (ent.getEntityId() == lastPearlId || ent.getEntityId() == lastOurPearlId) continue;
	            mc.world.getPlayers().stream()
	            		.filter(p -> p == rock.getModules().get(Aura.class).getTarget() || p == rock.getModules().get(Aura.class).getPrevTarget())
	                    .min(Comparator.comparingDouble((p) -> p.getDistanceSq(ent.getPositionVec())))
	                    .ifPresent((player) -> {
	                        if (!player.equals(mc.player)) {
	                            targetBlock = calcTrajectory(ent);
	                            lastPearlId = ent.getEntityId();
	                        }
	                    });
	        }

	        // Анти NPE
	        if (targetBlock == null)
	            return;
	        
	        // Нет смысла кидать если кидают в нас
	        if (mc.player.getDistance(targetBlock.getVec()) < 9)
	            return;
	        
	        this.rotationPitch = (float) (-Math.toDegrees(calcTrajectory(targetBlock)));
	        this.rotationYaw = (float) Math.toDegrees(Math.atan2(targetBlock.getZ() + 0.5f - mc.player.getPosZ(), targetBlock.getX() + 0.5f - mc.player.getPosX())) - 90.0f;
	        BlockPos tracedBP = checkTrajectory(rotationYaw, rotationPitch);
	        
	        if (tracedBP == null || targetBlock.distanceSq(tracedBP.getVec()) > 36)
	            return;
	        
	        tick++;

	        postSyncAction = () -> {
	            int epSlot = findEPSlot();
	            int originalSlot = mc.player.inventory.currentItem;
	            if (epSlot != -1) {
	                mc.player.inventory.currentItem = epSlot;
	                mc.player.connection.sendPacket(new CHeldItemChangePacket(epSlot));
	                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
	                mc.player.connection.sendPacket(new CAnimateHandPacket(Hand.MAIN_HAND));
	                mc.player.inventory.currentItem = originalSlot;
	                mc.player.connection.sendPacket(new CHeldItemChangePacket(originalSlot));
	            }
	        };
	        targetBlock = null;
	        delayTimer.reset();
		}
	}
	
	private int findEPSlot() {
        int epSlot = -1;
        if (mc.player.getHeldItemMainhand().getItem() == Items.ENDER_PEARL)
            epSlot = mc.player.inventory.currentItem;
        if (epSlot == -1)
            for (int l = 0; l < 9; ++l)
                if (mc.player.inventory.getStackInSlot(l).getItem() == Items.ENDER_PEARL) {
                    epSlot = l;
                    break;
                }
        return epSlot;
    }

    private float calcTrajectory(BlockPos bp) {
        double a = Math.hypot(bp.getX() + 0.5f - mc.player.getPosX(), bp.getZ() + 0.5f - mc.player.getPosZ());
        double y = 6.125 * ((bp.getY() + 1f) - (mc.player.getPosY() + (double) mc.player.getEyeHeight(mc.player.getPose())));
        y = 0.05000000074505806 * ((0.05000000074505806 * (a * a)) + y);
        y = Math.sqrt(9.37890625 - y);
        double d = 3.0625 - y;
        y = Math.atan2(d * d + y, 0.05000000074505806 * a);
        d = Math.atan2(d, 0.05000000074505806 * a);
        return (float) Math.min(y, d);
    }

    private BlockPos calcTrajectory(Entity e) {
        return traceTrajectory(e.getPosX(), e.getPosY(), e.getPosZ(), e.getMotion().x, e.getMotion().y, e.getMotion().z);
    }

    private BlockPos checkTrajectory(float yaw, float pitch) {
        if (Float.isNaN(pitch))
            return null;
        float yawRad = yaw / 180.0f * 3.1415927f;
        float pitchRad = pitch / 180.0f * 3.1415927f;
        double x = mc.player.getPosX() - MathHelper.cos(yawRad) * 0.16f;
        double y = mc.player.getPosY() + mc.player.getEyeHeight(mc.player.getPose()) - 0.1000000014901161;
        double z = mc.player.getPosZ() - MathHelper.sin(yawRad) * 0.16f;
        double motionX = -MathHelper.sin(yawRad) * MathHelper.cos(pitchRad) * 0.4f;
        double motionY = -MathHelper.sin(pitchRad) * 0.4f;
        double motionZ = MathHelper.cos(yawRad) * MathHelper.cos(pitchRad) * 0.4f;
        final float distance = MathHelper.sqrt((float) (motionX * motionX + motionY * motionY + motionZ * motionZ));
        motionX /= distance;
        motionY /= distance;
        motionZ /= distance;
        motionX *= 1.5f;
        motionY *= 1.5f;
        motionZ *= 1.5f;
        if (!mc.player.isOnGround()) motionY += mc.player.getMotion().getY();
        return traceTrajectory(x, y, z, motionX, motionY, motionZ);
    }

    private BlockPos traceTrajectory(double x, double y, double z, double mx, double my, double mz) {
    	Vector3d lastPos;
        for (int i = 0; i < 300; i++) {
            lastPos = new Vector3d(x, y, z);
            x += mx;
            y += my;
            z += mz;
            mx *= 0.99;
            my *= 0.99;
            mz *= 0.99;
            my -= 0.03f;
            Vector3d pos = new Vector3d(x, y, z);
            BlockRayTraceResult bhr = mc.world.rayTraceBlocks(new RayTraceContext(lastPos, pos, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, mc.player));
            if (bhr != null && bhr.getType() == RayTraceResult.Type.BLOCK) return bhr.getPos();

            for (Entity ent : mc.world.getAllEntities()) {
                if (ent instanceof ArrowEntity || ent == mc.player || ent instanceof EnderPearlEntity) continue;
                if (ent.getBoundingBox().intersects(new AxisAlignedBB(x - 0.3, y - 0.3, z - 0.3, x + 0.3, y + 0.3, z + 0.2)))
                    return null;
            }

            if (y <= -65) break;
        }
        return null;
    }
    
    @Override
    public void onEnable() {
    		
    }
    
    @Override
    public void onDisable() {
    		
    }
}