package fun.rockstarity.api.scripts.wrappers.base;

import lombok.AllArgsConstructor;
import net.minecraft.util.math.vector.Vector3d;

@AllArgsConstructor
public class VectorBase {
	public double x,y,z;
	
	public void set(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
}