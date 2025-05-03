package fun.rockstarity.api.scripts.wrappers.base;

import java.util.ArrayList;

import fun.rockstarity.api.IAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class WorldBase implements IAccess {
	public float timer() {
		return mc.timer.timerSpeed;
	}
	
	public void set_timer(float timer) {
		mc.timer.timerSpeed = timer;
	}
	
	public void reset_timer() {
		mc.timer.reset();
	}
	
	public EntityBase[] entities() {
		ArrayList<EntityBase> entities = new ArrayList<>();
		for (Entity e : mc.world.getAllEntities()) {
			entities.add(new EntityBase(e));
		}
		EntityBase[] array = new EntityBase[entities.size()];
		array = entities.toArray(array);
		return array;
	}
	
	public LivingEntityBase[] living_entities() {
		ArrayList<LivingEntityBase> entities = new ArrayList<>();
		for (Entity e : mc.world.getAllEntities()) {
			if (e instanceof LivingEntity)
				entities.add(new LivingEntityBase((LivingEntity)e));
		}
		LivingEntityBase[] array = new LivingEntityBase[entities.size()];
		array = entities.toArray(array);
		return array;
	}
}