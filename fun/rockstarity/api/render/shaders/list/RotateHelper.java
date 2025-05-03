package fun.rockstarity.api.render.shaders.list;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;

import org.lwjgl.opengl.GL13;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.render.shaders.Shader;
import fun.rockstarity.api.render.ui.mainmenu.Page;
import fun.rockstarity.api.render.ui.mainmenu.screens.MainPageScreen;
import net.minecraft.client.shader.Framebuffer;

/**
 * @author ConeTin
 * @since 09 недрочаб. 2024 г.
 */

public class RotateHelper extends Shader {
	
	private static final RotateHelper shader = new RotateHelper();
    private static Framebuffer framebuffer;

	public static void init() {
		framebuffer = Shader.createFrameBuffer(framebuffer);
		framebuffer.framebufferClear();
		framebuffer.bindFramebuffer(true);
	}
	
	public static void end() {
		framebuffer.unbindFramebuffer();
		mc.getFramebuffer().bindFramebuffer(true);
	}

	public static void draw(MatrixStack ms) {
		if (framebuffer != null) {
			GlStateManager.enableBlend();
			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			Render.setAlphaLimit(0);
			
			Render.bindTexture(framebuffer.framebufferTexture);
		    shader.start();
		    shader.setFloat("alpha", 1);
		    shader.drawQuads();
		    shader.finish();
		    
		    GlStateManager.disableBlend();
		}

	}
	
	@Override
	public String getCode() {
		return readShader("alpha");
	}
	
}
