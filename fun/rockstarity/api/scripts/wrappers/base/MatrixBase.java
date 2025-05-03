package fun.rockstarity.api.scripts.wrappers.base;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.util.math.vector.Vector3f;

public class MatrixBase {
	private MatrixStack stack;
	
	public MatrixBase(MatrixStack stack) {
		this.stack = stack;
	}
	
	public void rotateX(float val) {
		stack.rotate(Vector3f.XP.rotationDegrees(val));
	}
	
	public void rotateY(float val) {
		stack.rotate(Vector3f.ZP.rotationDegrees(val));
	}
	
	public void rotateZ(float val) {
		stack.rotate(Vector3f.ZP.rotationDegrees(val));
	}
	
	public void rotate(float x, float y, float z) {
		rotateX(x); rotateY(y); rotateZ(z);
	}
	
	public void scale(float x, float y, float z) {
		stack.scale(x, y, z);
	}
	
	public void translate(double x, double y, double z) {
		stack.translate(x, y, z);
	}
}