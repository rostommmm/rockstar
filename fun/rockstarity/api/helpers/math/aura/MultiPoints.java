package fun.rockstarity.api.helpers.math.aura;

import java.util.ArrayList;
import java.util.Comparator;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.math.MathUtility;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;

/**
 * @author ConeTin
 * @since 10 июн. 2024 г.
 */

@UtilityClass
public class MultiPoints implements IAccess {
	
	@Getter
	private final ArrayList<Vector3d> points = new ArrayList<>();

	public void update(LivingEntity target) {
		PlayerBox newBox = new PlayerBox(target);
		
		if (target.getBox() == null || !target.getBox().equals(newBox)) {
			target.setBox(newBox);
			updatePoints(target);
		}
	}
	
	public void updatePoints(LivingEntity target) {
		float step = 0.19f;
		
		points.clear();

		for (double x = target.getPosX() - target.getBox().getSize().x/2; x < target.getPosX() + target.getBox().getSize().x/2; x += step) {
			for (double y = target.getPosY(); y < target.getPosY() + target.getBox().getSize().y; y += step) {
				for (double z = target.getPosZ() - target.getBox().getSize().z/2; z < target.getPosZ() + target.getBox().getSize().z/2; z += step) {
					points.add(new Vector3d(x,y,z).subtract(target.getPositionVec()));
				}
			}
		}
	}
	
	public Vector3d getBestPoint(LivingEntity target) {
		if (points.isEmpty()) return null;
		
		ArrayList<Vector3d> visible = new ArrayList<>();
		
		for (Vector3d point : points) {
			point = point.add(target.getPositionVec());
			if (MathUtility.canSeen(point))
				visible.add(point);
		}
		
		visible.sort(Comparator.comparingDouble(MathUtility::getDistanceFromEye));
		
		return visible.isEmpty() ? null : visible.get(0);
	}
	
}
