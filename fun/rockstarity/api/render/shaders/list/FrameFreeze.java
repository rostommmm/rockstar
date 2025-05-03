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
import fun.rockstarity.api.render.ui.mainmenu.Page;
import fun.rockstarity.api.render.ui.mainmenu.screens.MainPageScreen;
import net.minecraft.client.shader.Framebuffer;

/**
 * @author ConeTin
 * @since 20 окт. 2024 г.
 */

public class FrameFreeze extends Shader {
	
	private static final FrameFreeze shader = new FrameFreeze();
    private static Framebuffer framebuffer;

    public static void save() {
        ((MainPageScreen)Page.MAIN.getScreen()).getBgAnim().setForward(false);
        
        framebuffer = Shader.createFrameBuffer(framebuffer);
        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(true);
        
        int currentTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        
        GL13.glBindTexture(GL13.GL_TEXTURE_2D, mc.getFramebuffer().framebufferTexture);
        Shader.drawQuads();
        
        framebuffer.unbindFramebuffer();
        mc.getFramebuffer().bindFramebuffer(true);
        
        GL13.glBindTexture(GL13.GL_TEXTURE_2D, currentTexture);
    }

	public static void draw(MatrixStack ms, float alpha) {
		if (framebuffer != null) {
			/*
		    Render.bindTexture(framebuffer.framebufferTexture);
		    shader.start();
		    shader.setFloat("alpha", 0.5f);
		    shader.drawQuads();
		    shader.finish();
		    */
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
