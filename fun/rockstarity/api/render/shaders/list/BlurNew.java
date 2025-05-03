package fun.rockstarity.api.render.shaders.list;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.IRenderCall;
import com.mojang.blaze3d.systems.RenderSystem;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.helpers.render.Stencil;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.shaders.Shader;
import fun.rockstarity.api.render.shaders.blur.GaussianKernel;
import fun.rockstarity.api.render.ui.rect.Rect;
import lombok.experimental.UtilityClass;
import net.minecraft.client.shader.Framebuffer; 

/**
 * @author ConeTin
 * @since 3 дек. 2023 г.
 */

public class BlurNew extends Shader implements IAccess {
	
	private static final ConcurrentLinkedQueue<IRenderCall> renderQueue = Queues.newConcurrentLinkedQueue();
	private static final BlurNew blurShader = new BlurNew();

	private static Framebuffer framebuffer = new Framebuffer(1, 1, false, mc.IS_RUNNING_ON_MAC);

	public static void setupUniforms(float dir1, float dir2, float radius) {
		blurShader.setInt("textureIn", 0);
		blurShader.setFloat("texelSize", 1.0F / (float) sr.getFramebufferWidth(),
				1.0F / (float) sr.getFramebufferHeight());
		blurShader.setFloat("direction", dir1, dir2);
		blurShader.setFloat("radius", radius);

		GaussianKernel gaussianKernel = new GaussianKernel((int) radius);
		gaussianKernel.compute();

		final FloatBuffer buffer = BufferUtils.createFloatBuffer((int) radius);
		buffer.put(gaussianKernel.getKernel());
		buffer.flip();
		RenderSystem.glUniform1(blurShader.get("weights"), buffer);
	}

	public static void registerRenderCall(IRenderCall rc) {
		renderQueue.add(rc);
	}

	public static void draw(int radius) {
		if (renderQueue.isEmpty())
			return;
		Stencil.init();
		while (!renderQueue.isEmpty()) {
			renderQueue.poll().execute();
		}
		Stencil.read(1);
		renderBlur(radius);
		Stencil.finish();
	}

	public static void renderBlur(float radius) {
		GlStateManager.enableBlend();
		GlStateManager.color4f(1, 1, 1, 1);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		framebuffer = createFrameBuffer(framebuffer);

		if (framebuffer != null) {
			framebuffer.framebufferClear(mc.IS_RUNNING_ON_MAC);
			framebuffer.bindFramebuffer(true);
			blurShader.start();
			setupUniforms(1, 0, radius);

			GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.getFramebuffer().framebufferTexture);

			Shader.drawQuads();
			framebuffer.unbindFramebuffer();
			blurShader.finish();

			mc.getFramebuffer().bindFramebuffer(true);
			blurShader.start();
			setupUniforms(0, 1, radius);

			GL11.glBindTexture(GL11.GL_TEXTURE_2D, framebuffer.framebufferTexture);
			Shader.drawQuads();
			blurShader.finish();
		}

		GL11.glColor4d(1, 1, 1, 1);
		GlStateManager.bindTexture(0);
	}
	
    @Override
    public String getCode() {
    	return """
#version 120

uniform sampler2D textureIn;
uniform vec2 texelSize, direction;
uniform float radius;
uniform float weights[256];

#define offset texelSize * direction

void main() {
    vec3 blr = texture2D(textureIn, gl_TexCoord[0].st).rgb * weights[0];

    for (float f = 1.0; f <= radius; f++) {
        blr += texture2D(textureIn, gl_TexCoord[0].st + f * offset).rgb * (weights[int(abs(f))]);
        blr += texture2D(textureIn, gl_TexCoord[0].st - f * offset).rgb * (weights[int(abs(f))]);
    }

    gl_FragColor = vec4(blr, 1.0);
}

    			""";
    }
    

    public static Framebuffer createFrameBuffer(Framebuffer framebuffer) {
        if (framebuffer == null || framebuffer.framebufferWidth != sr.getFramebufferWidth() || framebuffer.framebufferHeight != sr.getFramebufferHeight()) {
            if (framebuffer != null) {
                framebuffer.deleteFramebuffer();
            }
            try {
                return new Framebuffer(sr.getFramebufferWidth(), sr.getFramebufferHeight(), true, mc.IS_RUNNING_ON_MAC);
            } catch (Exception ex) {
                return null;
            }
        }
        return framebuffer;
    }
}
