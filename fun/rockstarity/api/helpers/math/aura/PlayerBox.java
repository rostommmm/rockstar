package fun.rockstarity.api.helpers.math.aura;

import fun.rockstarity.api.helpers.game.Chat;
import lombok.Getter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

/**
 * @author ConeTin
 * @since 10 июн. 2024 г.
 */

@Getter
public class PlayerBox {
	
	private final LivingEntity entity;
	private Vector3d size;
	
	public PlayerBox(LivingEntity entity) {
		this.entity = entity;
		AxisAlignedBB box = this.entity.getBoundingBox();
		
		this.size = new Vector3d(box.maxX - box.minX, box.maxY - box.minY, box.maxZ - box.minZ);
	}
	
	public boolean equals(PlayerBox info) {
		if (this == info) {
			return true;
		} else if (!(info instanceof PlayerBox)) {
			return false;
		} else {
			return this.size.equals(info.size);
		}
	}

}
