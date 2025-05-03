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
import fun.rockstarity.api.helpers.render.Stencil;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.shaders.Shader;
import fun.rockstarity.api.render.ui.rect.Rect;
import lombok.experimental.UtilityClass;
import net.minecraft.client.shader.Framebuffer; 

/**
 * @author ConeTin
 * @since 3 дек. 2023 г.
 */

@UtilityClass
public class Blur implements IAccess {
	
	public Framebuffer stencilFramebuffer = new Framebuffer(1, 1, false);
	public long renderTicks;

    private Shader kawaseDown = new Shader() {
    	public String getCode() { return readShader("kawaseDown"); };
    };
    private Shader kawaseUp = new Shader() {
    	public String getCode() { return readShader("kawaseUp"); };
    };

    private Framebuffer framebuffer = new Framebuffer(1, 1, false);

    private int currentIterations;

    private final List<Framebuffer> framebufferList = new ArrayList<>();

    private void initFramebuffers(float iterations) {
        for (Framebuffer framebuffer : framebufferList) {
            framebuffer.deleteFramebuffer();
        }
        framebufferList.clear();

        framebufferList.add(framebuffer = Shader.createFrameBuffer(framebuffer));

        for (int i = 1; i <= iterations; i++) {
            Framebuffer currentBuffer = new Framebuffer((int) (sr.getScaledWidth() / Math.pow(2, i)), (int) (sr.getScaledHeight() / Math.pow(2, i)), false);
            currentBuffer.setFramebufferFilter(GL11.GL_LINEAR);
            GlStateManager.bindTexture(currentBuffer.framebufferTexture);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL14.GL_MIRRORED_REPEAT);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL14.GL_MIRRORED_REPEAT);
            GlStateManager.bindTexture(0);

            framebufferList.add(currentBuffer);
        }
    }

    public void renderBlur(MatrixStack ms, int stencilFrameBufferTexture, int iterations, int offset) {
        if (/*currentIterations != iterations ||*/ framebuffer.framebufferWidth != sr.getFramebufferWidth() || framebuffer.framebufferHeight != sr.getFramebufferHeight()) {
        	initFramebuffers(5);
            currentIterations = iterations;
        }

        renderFBO(ms, framebufferList.get(1), mc.getFramebuffer().framebufferTexture, kawaseDown, offset);

        // вниз
        for (int i = 1; i < iterations; i++) {
            renderFBO(ms, framebufferList.get(i + 1), framebufferList.get(i).framebufferTexture, kawaseDown, offset);
        }

        // вверх
        for (int i = iterations; i > 1; i--) {
            renderFBO(ms, framebufferList.get(i - 1), framebufferList.get(i).framebufferTexture, kawaseUp, offset);
        }

        Framebuffer lastBuffer = framebufferList.get(0);
        lastBuffer.framebufferClear();
        lastBuffer.bindFramebuffer(false);
        kawaseUp.start();
        kawaseUp.setFloat("offset", offset, offset);
        kawaseUp.setInt("inTexture", 0);
        kawaseUp.setInt("check", 1);
        kawaseUp.setInt("textureToCheck", 16);
        kawaseUp.setFloat("halfpixel", 1.0f / lastBuffer.framebufferWidth, 1.0f / lastBuffer.framebufferHeight);
        kawaseUp.setFloat("iResolution", lastBuffer.framebufferWidth, lastBuffer.framebufferHeight);
        GL13.glActiveTexture(GL13.GL_TEXTURE16);
        Render.bindTexture(stencilFrameBufferTexture);
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

    public void renderOld(MatrixStack ms) {
    	if (framebufferList.isEmpty()) return;
    	 
    	mc.getFramebuffer().bindFramebuffer(true);
        Render.bindTexture(framebufferList.get(0).framebufferTexture);
        Render.setAlphaLimit(0);
        GlStateManager.enableBlend();
        Shader.drawQuads(ms);
        GlStateManager.bindTexture(0);
    }
    
    private void renderFBO(MatrixStack ms, Framebuffer framebuffer, int framebufferTexture, Shader shader, float offset) {
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


    public static void renderBlur(MatrixStack ms, int radius) {
    	Blur.stencilFramebuffer = Shader.createFrameBuffer(Blur.stencilFramebuffer);

		Blur.stencilFramebuffer.framebufferClear();
		Blur.stencilFramebuffer.bindFramebuffer(false);
		Round.draw(ms, new Rect(0, 0, sr.getScaledWidth(), sr.getScaledHeight()), 0, FixColor.WHITE);
        Blur.stencilFramebuffer.unbindFramebuffer();
        Blur.renderBlur(ms, Blur.stencilFramebuffer.framebufferTexture, (int) (radius * 2) - 1, 1);
    }

    public static void renderBlur(MatrixStack ms, int radius, Runnable data) {
        Stencil.init();
        data.run();
        Stencil.read(1);
        Blur.renderBlur(ms, radius);
        Stencil.finish();
    }
    
    public static void drawBlurredRect(MatrixStack ms, float x, float y, float width, float height, int radius) {
    	Stencil.init();
        Round.draw(ms, new Rect(x, y, width, height), radius, FixColor.WHITE);
        Stencil.read(1);
        Blur.renderBlur(ms, radius);
        Stencil.finish();
    }
}
