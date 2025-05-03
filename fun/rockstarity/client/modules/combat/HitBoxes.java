package fun.rockstarity.client.modules.combat;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventTick;
import fun.rockstarity.api.events.list.render.world.EventRenderWorld;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Slider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

@Info(name="HitBoxes", desc="Увеличивает хитбоксы сущностей", type=Category.COMBAT)
public class HitBoxes extends Module {
	
	private final Select targets = new Select(this, "Цели").desc("Сущность у которых будет увеличиваться хитбокс");
	
	private final Select.Element players = new Select.Element(targets, "Игрок").set(true);
	private final Select.Element mobs = new Select.Element(targets, "Мобы").set(true);
	
	private final Slider size = new Slider(this, "Размер хитбоксов").min(0).max(1).inc(0.1f).set(0.3f).desc("Размер хитбоксов по которым можно будет ударить");
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventRenderWorld e) {
			float size = (float) this.size.get();
			
			for (Entity ent : mc.world.getAllEntities()) {
				 if ((ent instanceof PlayerEntity && players.get()) || ((ent instanceof MobEntity || ent instanceof AnimalEntity) && mobs.get())) {
					if (ent == null || ent == mc.player) continue;
					
					ent.setBoundingBox(new AxisAlignedBB(
							ent.getPosX() - size,
							ent.getBoundingBox().minY,
							ent.getPosZ() - size,
							ent.getPosX() + size,
							ent.getBoundingBox().maxY,
							ent.getPosZ() + size));
				 }
			}
		}
	}
	
	@Override
	@NativeInclude
	public void onDisable() {
		for (Entity ent : mc.world.getAllEntities()) {
			 if ((ent instanceof PlayerEntity && players.get()) || ((ent instanceof MobEntity || ent instanceof AnimalEntity) && mobs.get())) {
				if (ent == null || ent == mc.player) continue;
				ent.setBoundingBox(new AxisAlignedBB(
						ent.getPosX() - 0.3F,
						ent.getBoundingBox().minY,
						ent.getPosZ() - 0.3F,
						ent.getPosX() + 0.3F,
						ent.getBoundingBox().maxY,
						ent.getPosZ() + 0.3F));
			 }
		}
	}

	@Override
	public void onEnable() {
		
	}
}
