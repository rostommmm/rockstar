package fun.rockstarity.api.render.shaders.list;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.render.shaders.Shader;
import net.minecraft.client.shader.Framebuffer;

public class Bloom implements IAccess {

    public static Shader kawaseDown = new Shader() {
    	public String getCode() { return readShader("kawaseDownBloom"); };
    };
    public static Shader kawaseUp = new Shader() {
    	public String getCode() { return readShader("kawaseUpBloom"); };
    };

    public static Framebuffer framebuffer = new Framebuffer(1, 1, false);

    private static int currentIterations;

    private static final List<Framebuffer> framebufferList = new ArrayList<>();

    private static void initFramebuffers(float iterations) {
        for (Framebuffer framebuffer : framebufferList) {
            framebuffer.deleteFramebuffer();
        }
        framebufferList.clear();

        framebufferList.add(framebuffer = Shader.createFrameBuffer(null, true));

        for (int i = 1; i <= iterations; i++) {
            Framebuffer currentBuffer = new Framebuffer((int) (sr.getScaledWidth() / Math.pow(2, i)), (int) (sr.getScaledHeight() / Math.pow(2, i)), true);
            currentBuffer.setFramebufferFilter(GL11.GL_LINEAR);

            GlStateManager.bindTexture(currentBuffer.framebufferTexture);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL14.GL_MIRRORED_REPEAT);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL14.GL_MIRRORED_REPEAT);
            GlStateManager.bindTexture(0);

            framebufferList.add(currentBuffer);
        }
    }


    private static void render(MatrixStack ms, int framebufferTexture, int iterations, int offset) {
    	float off = offset * 1;
    	
    	boolean can = false;
    	
    	if (mc.player.ticksExisted % 50 == 0 && can) {
    		kawaseDown = new Shader() {
    	    	public String getCode() { return readShader("kawaseDownBloom"); };
    	    };
    	    kawaseUp = new Shader() {
    	    	public String getCode() { return readShader("kawaseUpBloom"); };
    	    };
    	}
    	
        if (currentIterations != iterations || framebuffer.framebufferWidth != sr.getFramebufferWidth() || framebuffer.framebufferHeight != sr.getFramebufferHeight()) {
        	initFramebuffers(iterations);
            currentIterations = iterations;
        }

        renderFBO(ms ,framebufferList.get(1), framebufferTexture, kawaseDown, 0.1f);

        // вниз
        for (int i = 1; i < iterations; i++) {
            renderFBO(ms ,framebufferList.get(i + 1), framebufferList.get(i).framebufferTexture, kawaseDown, 0.5f);
        }

        // вверх
        for (int i = iterations; i > 1; i--) {
            renderFBO(ms ,framebufferList.get(i - 1), framebufferList.get(i).framebufferTexture, kawaseUp, 0.5f);
        }

        Framebuffer lastBuffer = framebufferList.get(0);
        lastBuffer.framebufferClear();
        lastBuffer.bindFramebuffer(false);
        kawaseUp.start();
        kawaseUp.setFloat("offset", 0.1f, 0.1f);
        kawaseUp.setInt("inTexture", 0);
        kawaseUp.setInt("check", 1);
        kawaseUp.setInt("textureToCheck", 16);
        kawaseUp.setFloat("halfpixel", 1.0f / lastBuffer.framebufferWidth, 1.0f / lastBuffer.framebufferHeight);
        kawaseUp.setFloat("iResolution", lastBuffer.framebufferWidth, lastBuffer.framebufferHeight);
        GL13.glActiveTexture(GL13.GL_TEXTURE16);
        Render.bindTexture(framebufferTexture);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        Render.bindTexture(framebufferList.get(1).framebufferTexture);
        Shader.drawQuads(ms);
        kawaseUp.finish();

        mc.getFramebuffer().bindFramebuffer(true);
        Render.bindTexture(framebufferList.get(0).framebufferTexture);
        Render.setAlphaLimit(0);
        GlStateManager.enableBlend();
        Shader.drawQuads(ms);
        GlStateManager.bindTexture(0);
    }

    private static void renderFBO(MatrixStack ms, Framebuffer framebuffer, int framebufferTexture, Shader shader, float offset) {
    	framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(false);
        shader.start();
        Render.bindTexture(framebufferTexture);
        shader.setFloat("offset", offset, offset);
        shader.setInt("inTexture", 0);
        shader.setInt("check", 0);
        shader.setFloat("halfpixel", 1.0f / framebuffer.framebufferWidth, 1.0f / framebuffer.framebufferHeight);
        shader.setFloat("iResolution", framebuffer.framebufferWidth, framebuffer.framebufferHeight);
        Shader.drawQuads(ms);
        shader.finish();
    }
    
    private static Framebuffer stencilFramebuffer = new Framebuffer(1, 1, false);

	public static void init() {
		stencilFramebuffer = Shader.createFrameBuffer(stencilFramebuffer);
		stencilFramebuffer.framebufferClear(false);
		stencilFramebuffer.bindFramebuffer(false);
	}

	public static void finish(MatrixStack ms,  int shadow, int offset) {
		stencilFramebuffer.unbindFramebuffer();

		render(ms, stencilFramebuffer.framebufferTexture, shadow, offset);
	}
}