package fun.rockstarity.api.render.particles;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.render.animation.infinity.InfinityAnimation;
import fun.rockstarity.api.render.color.FixColor;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class Particle2D implements IAccess {
	
	@Getter
	private float x, y, motionX, motionY, alpha = 1;
	private FixColor color;
	@Getter
	private final InfinityAnimation deviation = new InfinityAnimation();
	private float deviateValue = 0;
	
	public Particle2D(float x, float y, float motionX, float motionY, FixColor color) {
		this.x = x;
		this.y = y;
		
		float angle = MathUtility.random(0, 360);
		float rand = MathUtility.random(0, 1);
		
		this.motionX = motionX;
		this.motionY = motionY;
		
		this.color= color;
	}
	
	public void render(MatrixStack ms) {
		this.x += this.motionX / Math.max((float) Minecraft.debugFPS, 5) * 5;
		this.y += this.motionY / Math.max((float) Minecraft.debugFPS, 5) * 5;
		
		float slowingSpeed = 0.99f;
		this.motionX *= slowingSpeed;
		this.motionY *= slowingSpeed;
		
		float size = 10;
		float miniSize = 2;
		
		FixColor color = this.color;
		
		if (deviation.get() >= deviateValue) {
			deviateValue = MathUtility.random(20, 50);
			deviation.animate(deviateValue, (int) MathUtility.random(1550, 300));
		} else if (deviation.get() <= deviateValue) {
			deviateValue = MathUtility.random(20, 50);
			deviation.animate(deviateValue, (int) MathUtility.random(1550, 300));
		}
		
		ms.push();
        GlStateManager.depthMask(false);
       	ms.translate(this.x, this.y, 0);
       	Render.drawImage(ms, "masks/glow.png", (float) -size / 2, -size / 2, 0, size, size, color.alpha(this.alpha/2));
       	Render.drawImage(ms, "masks/glow.png", (float) -miniSize / 2, -miniSize / 2, 0, miniSize, miniSize, color.alpha(this.alpha));
        GlStateManager.depthMask(true);
        ms.pop();
        
        this.alpha -= 0.035f / Math.max((float) Minecraft.debugFPS, 5) * 15;
	}
	
}