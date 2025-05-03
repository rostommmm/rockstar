package fun.rockstarity.api.render.shaders.list;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import com.mojang.blaze3d.platform.GlStateManager;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.render.ColorUtility;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.shaders.Shader;

public class Reversed extends Shader implements IAccess {
	
	private static Reversed shader = new Reversed();
	
    public static void render(FixColor color) {
    	/*
    	if (mc.player.ticksExisted % 50 == 0) {
    		shader = new Reversed();
    		Chat.debug("updated");
    	}
    	*/
    	Render.resetColor();
	    GlStateManager.enableBlend();
	    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	    Render.setAlphaLimit(0);
	    
	    GL13.glBindTexture(GL13.GL_TEXTURE_2D, mc.getFramebuffer().framebufferTexture);
	    shader.start();
	    shader.setInt("textureIn", 0);
	    shader.setFloat("color", ColorUtility.getRGBAFloat(color));
	    shader.setFloat("texelSize", 1F / sr.getScaledWidth(), 1F / sr.getScaledHeight());
	    shader.setFloat("blurRadius", 1);
	    
	    Shader.drawQuads();
	    shader.finish();

	    GlStateManager.disableBlend();
	    GlStateManager.bindTexture(0);
    }
    
	@Override
	public String getCode() {
		return readShader("reversed");
	}
}
