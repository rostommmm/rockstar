package fun.rockstarity.api.render.shaders.list;

import static org.lwjgl.opengl.GL11.GL_LINEAR;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GLUtil;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.shaders.Shader;
import fun.rockstarity.api.render.ui.rect.Rect;
import lombok.experimental.UtilityClass;
import net.minecraft.client.shader.Framebuffer;

/**
 * @author ConeTin
 * @since 8 апр. 2025 г.
 * 
 * Special thanks for Tenacity :3
 */

@UtilityClass
public class KawaseBlur implements IAccess {
	
	public Framebuffer stencilFramebuffer = new Framebuffer(1, 1, false);

	private Shader kawaseDown = new Shader() {
    	public String getCode() { return "#version 120\n" +
                "\n" +
                "uniform sampler2D inTexture;\n" +
                "uniform vec2 offset, halfpixel, iResolution;\n" +
                "\n" +
                "void main() {\n" +
                "    vec2 uv = vec2(gl_FragCoord.xy / iResolution);\n" +
                "    vec4 sum = texture2D(inTexture, gl_TexCoord[0].st) * 4.0;\n" +
                "    sum += texture2D(inTexture, uv - halfpixel.xy * offset);\n" +
                "    sum += texture2D(inTexture, uv + halfpixel.xy * offset);\n" +
                "    sum += texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset);\n" +
                "    sum += texture2D(inTexture, uv - vec2(halfpixel.x, -halfpixel.y) * offset);\n" +
                "    gl_FragColor = vec4(sum.rgb * .125, 1.0);\n" +
                "}\n"; };
    };
    private Shader kawaseUp = new Shader() {
    	public String getCode() { return "#version 120\n" +
                "\n" +
                "uniform sampler2D inTexture, textureToCheck;\n" +
                "uniform vec2 halfpixel, offset, iResolution;\n" +
                "uniform int check;\n" +
                "\n" +
                "void main() {\n" +
                "    vec2 uv = vec2(gl_FragCoord.xy / iResolution);\n" +
                "    vec4 sum = texture2D(inTexture, uv + vec2(-halfpixel.x * 2.0, 0.0) * offset);\n" +
                "    sum += texture2D(inTexture, uv + vec2(-halfpixel.x, halfpixel.y) * offset) * 2.0;\n" +
                "    sum += texture2D(inTexture, uv + vec2(0.0, halfpixel.y * 2.0) * offset);\n" +
                "    sum += texture2D(inTexture, uv + vec2(halfpixel.x, halfpixel.y) * offset) * 2.0;\n" +
                "    sum += texture2D(inTexture, uv + vec2(halfpixel.x * 2.0, 0.0) * offset);\n" +
                "    sum += texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset) * 2.0;\n" +
                "    sum += texture2D(inTexture, uv + vec2(0.0, -halfpixel.y * 2.0) * offset);\n" +
                "    sum += texture2D(inTexture, uv + vec2(-halfpixel.x, -halfpixel.y) * offset) * 2.0;\n" +
                "\n" +
                "    gl_FragColor = vec4(sum.rgb /12.0, mix(1.0, texture2D(textureToCheck, gl_TexCoord[0].st).a, check));\n" +
                "}\n"; };
    };

    public Framebuffer framebuffer = new Framebuffer(1, 1, false);

    public void setupUniforms(float offset) {
        kawaseDown.setFloat("offset", offset, offset);
        kawaseUp.setFloat("offset", offset, offset);
    }

    private int currentIterations;

    private final List<Framebuffer> framebufferList = new ArrayList<>();

    private void startFramebuffers(float iterations) {
        for (Framebuffer framebuffer : framebufferList) {
            framebuffer.deleteFramebuffer();
        }
        framebufferList.clear();

        //Have to make the framebuffer null so that it does not try to delete a framebuffer that has already been deleted
        framebufferList.add(framebuffer = Shader.createFrameBuffer(null));


        for (int i = 1; i <= iterations; i++) {
            Framebuffer currentBuffer = new Framebuffer((int) (sr.getFramebufferWidth() / Math.pow(2, i)), (int) (sr.getFramebufferHeight() / Math.pow(2, i)), false);
            currentBuffer.setFramebufferFilter(GL_LINEAR);
            GlStateManager.bindTexture(currentBuffer.framebufferTexture);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL14.GL_MIRRORED_REPEAT);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL14.GL_MIRRORED_REPEAT);
            GlStateManager.bindTexture(0);

            framebufferList.add(currentBuffer);
        }
    }


    public void renderBlur(int stencilFrameBufferTexture, int iterations, int offset) {
        if (currentIterations != iterations || framebuffer.framebufferWidth != sr.getFramebufferWidth() || framebuffer.framebufferHeight != sr.getFramebufferHeight()) {
            startFramebuffers(iterations);
            currentIterations = iterations;
        }

        renderFBO(framebufferList.get(1), mc.getFramebuffer().framebufferTexture, kawaseDown, offset);

        //Downsample
        for (int i = 1; i < iterations; i++) {
            renderFBO(framebufferList.get(i + 1), framebufferList.get(i).framebufferTexture, kawaseDown, offset);
        }

        //Upsample
        for (int i = iterations; i > 1; i--) {
            renderFBO(framebufferList.get(i - 1), framebufferList.get(i).framebufferTexture, kawaseUp, offset);
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
        Shader.drawQuads();
        kawaseUp.finish();


        mc.getFramebuffer().bindFramebuffer(true);
        Render.bindTexture(framebufferList.get(0).framebufferTexture);
        Render.setAlphaLimit(0);
        RenderSystem.enableBlend();
        Shader.drawQuads();
        GlStateManager.bindTexture(0);

    }

    private void renderFBO(Framebuffer framebuffer, int framebufferTexture, Shader shader, float offset) {
        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(false);
        shader.start();
        Render.bindTexture(framebufferTexture);
        shader.setFloat("offset", offset, offset);
        shader.setInt("inTexture", 0);
        shader.setInt("check", 0);
        shader.setFloat("halfpixel", 1.0f / framebuffer.framebufferWidth, 1.0f / framebuffer.framebufferHeight);
        shader.setFloat("iResolution", framebuffer.framebufferWidth, framebuffer.framebufferHeight);
        Shader.drawQuads();
        shader.finish();
    }

    public void renderBlur(MatrixStack ms, int iterations, int offset) {
    	stencilFramebuffer = Shader.createFrameBuffer(Blur.stencilFramebuffer);

		stencilFramebuffer.framebufferClear();
		stencilFramebuffer.bindFramebuffer(false);
		Round.draw(ms, new Rect(0, 0, sr.getScaledWidth(), sr.getScaledHeight()), 0, FixColor.WHITE);
        Blur.stencilFramebuffer.unbindFramebuffer();
        renderBlur(Blur.stencilFramebuffer.framebufferTexture, iterations, offset);
    }
    
}