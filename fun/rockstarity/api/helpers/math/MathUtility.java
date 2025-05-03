package fun.rockstarity.api.helpers.math;

import java.util.Optional;
import java.util.function.Predicate;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.math.aura.Rotation;
import fun.rockstarity.api.render.shaders.fog.Vector2d;
import fun.rockstarity.client.modules.combat.BackTrack;
import fun.rockstarity.client.modules.combat.BackTrack.Position;
import lombok.experimental.UtilityClass;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

/**
 * @author ConeTin
 * @since 4 дек. 2023 г.
 */

@UtilityClass
public class MathUtility implements IAccess {
    FastRandom fastRandomize = new FastRandom();
    
    public double squirt(double value) {
    	return Math.sqrt(value);
    }
	
	public double step(double value, double steps) {
        double roundedValue = Math.round(value / steps) * steps;
        return Math.round(roundedValue * 100.0) / 100.0;
    }

    public static boolean isBlockUnder(float under) {
        if (mc.player.getPosY() < 0.0) {
            return false;
        } else {
            AxisAlignedBB aab = mc.player.getBoundingBox().offset(0.0, -under, 0.0);
            return mc.world.getCollisionShapes(mc.player, aab).toList().isEmpty();
        }
    }
    
    public float random(double min, double max) {
		return (float) (min + (max - min) * Math.random());
	}
	
	public int randomInt(int min, int max) {
		return (int) (min + (max - min) * Math.random());
	}

    //апдейтнули рандом ибо никто это не делал еще, пиздец
    public static float randomNew(double min, double max) {
        if (min > max) return (float) (fastRandomize.nextFloat() * (min - max) + max);
        return (float) (fastRandomize.nextFloat() * (max - min) + min);
    }

    public static int randomIntNew(int min, int max) {
        if (min > max) return fastRandomize.nextInt() * (min - max) + max;
        return fastRandomize.nextInt() * (max - min) + min;
    }
	
	public double round(double target, int decimal) {
	    return Math.round(target * Math.pow(10, decimal)) / Math.pow(10, decimal);
	}
	
	public float calculateGaussianValue(float x, float sigma) {
        double output = 1.0 / MathUtility.squirt(2.0 * Math.PI * (sigma * sigma));
        return (float) (output * Math.exp(-(x * x) / (2.0 * (sigma * sigma))));
    }
	
	public float interpolate(double oldValue, double newValue, double interpolationValue){
        return (float) (oldValue + (newValue - oldValue) * interpolationValue);
    }
	
	public float step(float current, float target, float step) {
        float difference = target - current;
        
        if (Math.abs(difference) <= step) {
            return target;
        }
        
        return current + Math.signum(difference) * step;
    }
	
	public Vector2f calculate(final Vector3d to) {
        Vector3d pos = mc.player.getPositionVec().add(0, mc.player.getEyeHeight(), 0);
        Vector3d from = new Vector3d(pos.x, pos.y, pos.z);
        return calculate(from, to);
    }
	
    public Vector2f calculate(final Vector3d from, final Vector3d to) {
        final Vector3d diff = to.sub(from);
        final double distance = Math.hypot(diff.x, diff.z);
        final float yaw = (float) (MathHelper.atan2(diff.z, diff.x) * 180F / Math.PI) - 90F;
        final float pitch = (float) (-(MathHelper.atan2(diff.y, distance) * 180F / Math.PI));
        return new Vector2f(yaw, pitch);
    }
    
    public float calcTrajectory(BlockPos bp) {
        double a = Math.hypot(bp.getX() + 0.5f - mc.player.getPosX(), bp.getZ() + 0.5f - mc.player.getPosZ());
        double y = 6.125 * ((bp.getY() + 1f) - (mc.player.getPosY() + (double) mc.player.getEyeHeight(mc.player.getPose())));
        y = 0.05000000074505806 * ((0.05000000074505806 * (a * a)) + y);
        y = MathUtility.squirt(Math.max(0, 9.37890625 - y));
        double d = 3.0625 - y;
        y = Math.atan2(d * d + y, 0.05000000074505806 * a);
        d = Math.atan2(d, 0.05000000074505806 * a);
        return (float) Math.min(y, d);
    }

    public Vector2f rotationToVec(Vector3d vec) {
        Vector3d eyesPos = mc.player.getEyePosition(1.0f);
        double diffX = vec != null ? vec.x - eyesPos.x : 0;
        double diffY = vec != null ? vec.y - (mc.player.getPosY() + (double) mc.player.getEyeHeight() + 0.5) : 0;
        double diffZ = vec != null ? vec.z - eyesPos.z : 0;

        double diffXZ = MathUtility.squirt(diffX * diffX + diffZ * diffZ);
        float yaw = (float) (Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0);
        float pitch = (float) (-Math.toDegrees(Math.atan2(diffY, diffXZ)));
        yaw = mc.player.rotationYaw + MathHelper.wrapDegrees(yaw - mc.player.rotationYaw);
        pitch = mc.player.rotationPitch + MathHelper.wrapDegrees(pitch - mc.player.rotationPitch);
        pitch = MathHelper.clamp(pitch, -90.0f, 90.0f);

        return new Vector2f(yaw, pitch);
    }
	
	/**
	 * Позволяет получить координаты точек для отрисовки круга (или других целей)
	 * @param point - Точка круга в градусах (0-360)
	 * @param radius - Радиус круга (типо ширина)
	 * @return Точку для отрисовки круга
	 */
	public Vector2d circleCords(float point, float radius) {
		return new Vector2d(
			Math.sin(Math.toRadians(point)) * radius,
			Math.cos(Math.toRadians(point)) * radius
		);
	}
	
	/**
	 * Высчитывает дистанцию от глаза игрока до точки
	 * @param vec - То
	 * @return Дистанцию до точки
	 */
	public double getDistanceFromEye(Vector3d vec) {
		float f = (float)(mc.player.getPosX() - vec.x);
        float f1 = (float)(mc.player.getPosYEye() - vec.y);
        float f2 = (float)(mc.player.getPosZ() - vec.z);
        return MathHelper.sqrt(f * f + f1 * f1 + f2 * f2);
	}
	
	/**
	 * Проверяет за стеной ли точка
	 * @param vec - Точка, которую требуется проверить
	 * @return Значение за стеной - true, не за стеной - false
	 */
	public boolean canSeen(Vector3d vec)
    {
        Vector3d vector3d = mc.player.getPositionVec().add(0,mc.player.getEyeHeight(),0);
        return mc.world.rayTraceBlocks(new RayTraceContext(vector3d, vec, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, mc.player)).getType() == RayTraceResult.Type.MISS;
    }

	public boolean canRender(Vector3d vec)
    {
        Vector3d vector3d = mc.gameRenderer.getActiveRenderInfo().getProjectedView();
        return mc.world.rayTraceBlocks(new RayTraceContext(vector3d, vec, RayTraceContext.BlockMode.VISUAL, RayTraceContext.FluidMode.NONE, mc.player)).getType() == RayTraceResult.Type.MISS;
    }

	
    /**
     * Выполняет трассировку луча и возвращает результат.
     *
     * @param rayTraceDistance дальность трассировки луча
     * @param yaw              горизонтальный угол обзора
     * @param pitch            вертикальный угол обзора
     * @param entity           сущность, для которой выполняется трассировка луча
     * @return результат трассировки луча в игровом мире
     */
    public RayTraceResult rayTrace(double rayTraceDistance,
                                          float yaw,
                                          float pitch,
                                          Entity entity) {
        Vector3d startVec = mc.player.getEyePosition(1.0F);
        Vector3d directionVec = getVectorForRotation(pitch, yaw);
        Vector3d endVec = startVec.add(
                directionVec.x * rayTraceDistance,
                directionVec.y * rayTraceDistance,
                directionVec.z * rayTraceDistance
        );

        return mc.world.rayTraceBlocks(new RayTraceContext(
                startVec,
                endVec,
                RayTraceContext.BlockMode.OUTLINE,
                RayTraceContext.FluidMode.NONE,
                entity)
        );
    }
    
    public RayTraceResult rayTrace(double rayTraceDistance,
            float yaw,
            float pitch,
            Entity entity,
            Vector3d start) {
		Vector3d directionVec = getVectorForRotation(pitch, yaw);
		Vector3d endVec = start.add(
		directionVec.x * rayTraceDistance,
		directionVec.y * rayTraceDistance,
		directionVec.z * rayTraceDistance
		);
		
		return mc.world.rayTraceBlocks(new RayTraceContext(
				start,
		endVec,
		RayTraceContext.BlockMode.OUTLINE,
		RayTraceContext.FluidMode.NONE,
		entity)
		);
	}

    public static EntityRayTraceResult rayTraceEntities(Entity shooter, Vector3d startVec, Vector3d endVec, AxisAlignedBB boundingBox, Predicate<Entity> filter, double distance)
    {
        World world = shooter.world;
        double d0 = distance;
        Entity entity = null;
        Vector3d vector3d = null;

        for (Entity entity1 : world.getEntitiesInAABBexcluding(shooter, boundingBox, filter))
        {
        	if (rock.getModules().get(BackTrack.class).get())
        	for (Position pos : entity1.getBacktrack()) {
        		AxisAlignedBB axisalignedbb1 = entity1.getBoundingBox().grow((double)entity1.getCollisionBorderSize());
        		Vector3d entPos = entity1.getPositionVec();
        		AxisAlignedBB axisalignedbb = new AxisAlignedBB(
        				axisalignedbb1.minX-entPos.x+pos.getPos().x,
        				axisalignedbb1.minY-entPos.y+pos.getPos().y,
        				axisalignedbb1.minZ-entPos.z+pos.getPos().z,
        				axisalignedbb1.maxX-entPos.x+pos.getPos().x,
        				axisalignedbb1.maxY-entPos.y+pos.getPos().y,
        				axisalignedbb1.maxZ-entPos.z+pos.getPos().z
        		);
                Optional<Vector3d> optional = axisalignedbb.rayTrace(startVec, endVec);

                if (axisalignedbb.contains(startVec))
                {
                    if (d0 >= 0.0D)
                    {
                        entity = entity1;
                        vector3d = optional.orElse(startVec);
                        d0 = 0.0D;
                    }
                }
                else if (optional.isPresent())
                {
                    Vector3d vector3d1 = optional.get();
                    double d1 = startVec.squareDistanceTo(vector3d1);

                    if (d1 < d0 || d0 == 0.0D)
                    {
                        if (entity1.getLowestRidingEntity() == shooter.getLowestRidingEntity())
                        {
                            if (d0 == 0.0D)
                            {
                                entity = entity1;
                                vector3d = vector3d1;
                            }
                        }
                        else
                        {
                            entity = entity1;
                            vector3d = vector3d1;
                            d0 = d1;
                        }
                    }
                }
        	}
        	
            AxisAlignedBB axisalignedbb = entity1.getBoundingBox().grow((double)entity1.getCollisionBorderSize());
            Optional<Vector3d> optional = axisalignedbb.rayTrace(startVec, endVec);

            if (axisalignedbb.contains(startVec))
            {

                if (d0 >= 0.0D)
                {
                    entity = entity1;
                    vector3d = optional.orElse(startVec);
                    d0 = 0.0D;
                }
            }
            else if (optional.isPresent())
            {

                Vector3d vector3d1 = optional.get();
                double d1 = startVec.squareDistanceTo(vector3d1);
                
                //if (d1 < d0 || d0 == 0.0D)
                {

                    if (entity1.getLowestRidingEntity() == shooter.getLowestRidingEntity())
                    {
                        if (d0 == 0.0D)
                        {
                            entity = entity1;
                            vector3d = vector3d1;
                        }
                    }
                    else
                    {
                        entity = entity1;
                        vector3d = vector3d1;
                        d0 = d1;
                    }
                }
            }
        }

        return entity == null ? null : new EntityRayTraceResult(entity, vector3d);
    }
    
    public static boolean tracedTo(Entity shooter, Vector3d startVec, Vector3d endVec, AxisAlignedBB boundingBox, Predicate<Entity> filter, double distance, Entity target)
    {
        World world = shooter.world;
        double d0 = distance;

        for (Entity entity1 : world.getEntitiesInAABBexcluding(shooter, boundingBox, filter))
        {
        	if (rock.getModules().get(BackTrack.class).get())
        	for (Position pos : entity1.getBacktrack()) {
        		AxisAlignedBB axisalignedbb1 = entity1.getBoundingBox().grow((double)entity1.getCollisionBorderSize());
        		Vector3d entPos = entity1.getPositionVec();
        		AxisAlignedBB axisalignedbb = new AxisAlignedBB(
        				axisalignedbb1.minX-entPos.x+pos.getPos().x,
        				axisalignedbb1.minY-entPos.y+pos.getPos().y,
        				axisalignedbb1.minZ-entPos.z+pos.getPos().z,
        				axisalignedbb1.maxX-entPos.x+pos.getPos().x,
        				axisalignedbb1.maxY-entPos.y+pos.getPos().y,
        				axisalignedbb1.maxZ-entPos.z+pos.getPos().z
        		);
                Optional<Vector3d> optional = axisalignedbb.rayTrace(startVec, endVec);

                if (axisalignedbb.contains(startVec))
                {
                    if (d0 >= 0.0D)
                    {
                        if (entity1 == target) return true;
                        d0 = 0.0D;
                    }
                }
                else if (optional.isPresent())
                {
                    Vector3d vector3d1 = optional.get();
                    double d1 = startVec.squareDistanceTo(vector3d1);

                    if (d1 < d0 || d0 == 0.0D)
                    {
                        if (entity1.getLowestRidingEntity() == shooter.getLowestRidingEntity())
                        {
                            if (d0 == 0.0D)
                            {
                            	if (entity1 == target) return true;
                            }
                        }
                        else
                        {
                        	if (entity1 == target) return true;
                            d0 = d1;
                        }
                    }
                }
        	}
        	
            AxisAlignedBB axisalignedbb = entity1.getBoundingBox().grow((double)entity1.getCollisionBorderSize());
            Optional<Vector3d> optional = axisalignedbb.rayTrace(startVec, endVec);

            if (axisalignedbb.contains(startVec))
            {

                if (d0 >= 0.0D)
                {
                	if (entity1 == target) return true;
                    d0 = 0.0D;
                }
            }
            else if (optional.isPresent())
            {

                Vector3d vector3d1 = optional.get();
                double d1 = startVec.squareDistanceTo(vector3d1);
                
                //if (d1 < d0 || d0 == 0.0D)
                {

                    if (entity1.getLowestRidingEntity() == shooter.getLowestRidingEntity())
                    {
                        if (d0 == 0.0D)
                        {
                        	if (entity1 == target) return true;
                        }
                    }
                    else
                    {
                    	if (entity1 == target) return true;
                        d0 = d1;
                    }
                }
            }
        }
        
        return false;
    }

    public boolean rayTraceWithBlock(double rayTraceDistance, float yaw, float pitch, Entity entity, Entity target, boolean blocks) {

        RayTraceResult object = null;
        if (target == null) return false;
        if (entity != null && mc.world != null) {
            float partialTicks = mc.getRenderPartialTicks();
            double distance = rayTraceDistance;
            object = rayTrace(rayTraceDistance, yaw, pitch, entity);
            Vector3d vector3d = entity.getEyePosition(partialTicks);
            boolean flag = false;
            double d1 = distance;

            /*
            if (mc.playerController.extendedReach()) {
                d1 = 6.0D;
                distance = d1;
            }
            */

            d1 = d1 * d1;

            if (object != null) {
                d1 = object.getHitVec().squareDistanceTo(vector3d);
            }
            
            Vector3d vector3d1 = getVectorForRotation(pitch, yaw);
            Vector3d vector3d2 = vector3d.add(vector3d1.x * distance, vector3d1.y * distance, vector3d1.z * distance);
            float f = 1.0F;
            AxisAlignedBB axisalignedbb = entity.getBoundingBox().expand(vector3d1.scale(distance)).grow(1.0D, 1.0D, 1.0D);
            boolean traced = tracedTo(entity, vector3d, vector3d2, axisalignedbb, (p_lambda$getMouseOver$0_0_) ->
            {
                return !p_lambda$getMouseOver$0_0_.isSpectator() && p_lambda$getMouseOver$0_0_.canBeCollidedWith();
            }, d1, target);
            
            return traced;
        }
        
        return false;
    }

    /**
     * Возвращает вектор направления в зависимости от заданных углов поворота.
     *
     * @param pitch вертикальный угол поворота
     * @param yaw   горизонтальный угол поворота
     * @return вектор направления
     */
    public Vector3d getVectorForRotation(float pitch, float yaw) {
        float yawRadians = -yaw * ((float) Math.PI / 180) - (float) Math.PI;
        float pitchRadians = -pitch * ((float) Math.PI / 180);

        float cosYaw = MathHelper.cos(yawRadians);
        float sinYaw = MathHelper.sin(yawRadians);
        float cosPitch = -MathHelper.cos(pitchRadians);
        float sinPitch = MathHelper.sin(pitchRadians);

        return new Vector3d(sinYaw * cosPitch, sinPitch, cosYaw * cosPitch);
    }
    
	public float[] getRotVec(Vector3d pos, Vector3d vec) {
		if (vec == null)
			return new float[] { mc.player.rotationYaw, mc.player.rotationPitch };
		double posX = vec.getX() - pos.x;
		double posY = vec.getY() - (pos.y + (double) mc.player.getEyeHeight());
		double posZ = vec.getZ() - pos.z;
		double sqrt = MathHelper.sqrt(posX * posX + posZ * posZ);
		float yaw = (float) (Math.atan2(posZ, posX) * 180.0 / Math.PI) - 90.0f;
		float pitch = (float) (-(Math.atan2(posY, sqrt) * 180.0 / Math.PI));
		float sens = (float) (mc.getGameSettings().mouseSensitivity * 0.6f + 0.2f);
		float pow = sens * sens * sens * 1.2F;
		yaw -= yaw % pow;
		pitch -= pitch % (pow * sens);
		return new float[] { yaw, pitch };
	}
    
	public float[] getRotFromPos(Vector3d pos, Entity e) {
		Vector3d ideal = Rotation.getBestPoint(mc.player.getEyePosition(mc.timer.renderPartialTicks), e);
		double x = ideal.getX(), z = ideal.getZ(), y = ideal.getY();
		return getRotVec(pos, new Vector3d(x, y, z));
	}
}
