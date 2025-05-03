package fun.rockstarity.api.render.shaders.list;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.render.shaders.Shader;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.client.modules.combat.Aura;
import net.minecraft.entity.LivingEntity; 

/**
 * @author ConeTin
 * @since 3 дек. 2023 г.
 */

public class Gray extends Shader {
	
	private static final Gray shader = new Gray();
	
	private static final int TEXTURE_UNIT_INDEX = 0; // Индекс текстурного юнита

    public static void render(float radius) {
    	Render.resetColor();
	    GlStateManager.enableBlend();
	    GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	    Render.setAlphaLimit(0);

	    GL13.glBindTexture(GL13.GL_TEXTURE_2D, mc.getFramebuffer().framebufferTexture);
	    shader.start();
	    shader.setInt("textureIn", 0);
	    shader.setFloat("grayAmount", radius);
	    //shader.setFloat("size", (float) (rect.getWidth() * sr.getGuiScaleFactor()), (float) (rect.getHeight() * sr.getGuiScaleFactor()));
	    Shader.drawQuads();
	    shader.finish();

	    GlStateManager.disableBlend();
	    GlStateManager.bindTexture(0);
    }

	@Override
	public String getCode() {
		return readShader("gray");
	}
	
}
