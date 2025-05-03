package fun.rockstarity.api.render.shaders.list;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.shaders.Shader;
import fun.rockstarity.api.render.shaders.blur.GaussianKernel;
import fun.rockstarity.api.render.ui.rect.Rect;
import lombok.experimental.UtilityClass;
import net.minecraft.client.shader.Framebuffer;

/**
 * @author ConeTin
 * @since 8 апр. 2025 г.
 * 
 * Special thanks for Rise :3
 */

@UtilityClass
public class GaussianBlur implements IAccess {

    private Shader blurProgram = new Shader() {
    	public String getCode() { return
    			"""
#version 120

uniform sampler2D u_diffuse_sampler;
uniform sampler2D u_other_sampler;
uniform vec2 u_texel_size;
uniform vec2 u_direction;
uniform float u_radius;
uniform float u_kernel[128];

void main()
{
    vec2 uv = gl_TexCoord[0].st;

    float alpha = texture2D(u_other_sampler, uv).a;
    if (u_direction.x == 0.0 && alpha == 0.0) {
        discard;
    }

    float half_radius = u_radius / 2.0;
    vec4 pixel_color = texture2D(u_diffuse_sampler, uv) * u_kernel[0];

    for (float f = 1; f <= u_radius; f++) {
        vec2 offset = f * u_texel_size * u_direction;
        pixel_color += texture2D(u_diffuse_sampler, uv - offset) * u_kernel[int(f)];
        pixel_color += texture2D(u_diffuse_sampler, uv + offset) * u_kernel[int(f)];
    }

    gl_FragColor = vec4(pixel_color.rgb, u_direction.x == 0.0 ? alpha : 1.0);
}
    			"""; };
    };
    private Framebuffer inputFramebuffer = new Framebuffer(sr.getFramebufferWidth(), sr.getFramebufferHeight(), true);
    private Framebuffer outputFramebuffer = new Framebuffer(sr.getFramebufferWidth(), sr.getFramebufferHeight(), true);
    private GaussianKernel gaussianKernel = new GaussianKernel(0);
    
    public void start() {
    	inputFramebuffer.bindFramebuffer(true);
    }
    
    public void end() {
    	end(8, 2);
    }
    
    public void end(int radius, float compression) {
        final int programId = blurProgram.getId();

        outputFramebuffer.bindFramebuffer(true);
        blurProgram.start();

        if (gaussianKernel.getSize() != radius) {
            gaussianKernel = new GaussianKernel(radius);
            gaussianKernel.compute();

            final FloatBuffer buffer = BufferUtils.createFloatBuffer(radius);
            buffer.put(gaussianKernel.getKernel());
            buffer.flip();

            blurProgram.setFloat("u_radius", radius);
            blurProgram.setFloatBuffer("u_kernel", buffer);
            blurProgram.setInt("u_diffuse_sampler", 0);
            blurProgram.setInt("u_other_sampler", 20);
        }

        blurProgram.setFloat("u_texel_size", 1.0F / sr.getFramebufferWidth(), 1.0F / sr.getFramebufferHeight());
        blurProgram.setFloat("u_direction", compression, 0.0F);

        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
        mc.getFramebuffer().bindFramebufferTexture();
        Shader.drawQuads();

        mc.getFramebuffer().bindFramebuffer(true);
        blurProgram.setFloat("u_direction", 0.0F, compression);
        outputFramebuffer.bindFramebufferTexture();
        GL13.glActiveTexture(GL13.GL_TEXTURE20);
        inputFramebuffer.bindFramebufferTexture();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        Shader.drawQuads();
        GlStateManager.disableBlend();

        blurProgram.finish();
    }
    
    public void draw(MatrixStack ms, int radius, float compression) {
    	start();
    	Round.draw(ms, new Rect(0, 0, sr.getScaledWidth(), sr.getScaledHeight()), 0, FixColor.WHITE);
    	end(radius, compression);
    }
    
}	
