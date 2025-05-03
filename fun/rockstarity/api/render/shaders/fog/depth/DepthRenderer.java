package fun.rockstarity.api.render.shaders.fog.depth;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.client.modules.render.World;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.FramebufferConstants;
import net.minecraft.util.math.MathHelper;

public enum DepthRenderer implements IAccess {
    INSTANCE;
    private int depthCopyFbo;
    private int depthCopyColorBuffer;
    private int depthCopyDepthBuffer;
    private int width, height;

    private DepthShader shader() {
        return rock.getDepthShader();
    }

    public void render3D() {
        init();
        render();
    }

    public void init() {
        if (width != sr.getFramebufferWidth() || height != sr.getFramebufferHeight()) {
            width = sr.getFramebufferWidth();
            height = sr.getFramebufferHeight();
            if (depthCopyFbo != 0) {
                deleteDepthCopyFramebuffer();
            }
        }
        if (depthCopyFbo == 0) {
            createDepthCopyFramebuffer();
        }
    }

    private void render() {
        final Minecraft mc = Minecraft.getInstance();
        final Framebuffer framebuffer = mc.getFramebuffer();
        final World fogBlur = rock.getModules().get(World.class);

        updateDepthTexture(framebuffer);
        shader().setNear(0.05F);
        shader().far((float) (mc.getGameSettings().renderDistanceChunks * 16));
        
        shader().bind();
        blit(framebuffer);
        shader().unbind();
        
        shader().distance(fogBlur.getDistance().get());
        shader().clientColor(true);
        shader().saturation((float) MathHelper.clamp(1F - fogBlur.getSaturation().get(), 0, 1));
        shader().color1(fogBlur.getColorWheel().getColor1());
        shader().color2(fogBlur.getColorWheel().getColor2());
        shader().color3(fogBlur.getColorWheel().getColor3());
        shader().color4(fogBlur.getColorWheel().getColor4());
        shader().blurBuffer(DepthBlur.INSTANCE.BLURRED.framebufferTexture);
        shader().minecraftBuffer(mc.getFramebuffer().framebufferTexture);
        framebuffer.bindFramebuffer(true);
        GlStateManager.activeTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(0);
        GlStateManager.enableBlend();
        GlStateManager.enableTexture();
        GlStateManager.clearCurrentColor();
    }

    private void updateDepthTexture(final Framebuffer framebuffer) {
        GlStateManager.bindFramebuffer(GL30.GL_READ_FRAMEBUFFER, framebuffer.framebufferObject);
        GlStateManager.bindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, depthCopyFbo);
        GL30.glBlitFramebuffer(0, 0,
                framebuffer.framebufferWidth,
                framebuffer.framebufferHeight,
                0, 0,
                framebuffer.framebufferWidth,
                framebuffer.framebufferHeight,
                GL30.GL_DEPTH_BUFFER_BIT,
                GL30.GL_NEAREST);
    }

    private void createDepthCopyFramebuffer() {
        final Framebuffer framebuffer = Minecraft.getInstance().getFramebuffer();
        depthCopyFbo = GlStateManager.genFramebuffers();
        depthCopyColorBuffer = createTexture(framebuffer.framebufferWidth, framebuffer.framebufferHeight, GL11.GL_RGBA8, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE);
        depthCopyDepthBuffer = createTexture(framebuffer.framebufferWidth, framebuffer.framebufferHeight, GL30.GL_DEPTH_COMPONENT, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT);
        GlStateManager.bindFramebuffer(FramebufferConstants.GL_FRAMEBUFFER, depthCopyFbo);
        GlStateManager.framebufferTexture2D(FramebufferConstants.GL_FRAMEBUFFER, FramebufferConstants.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, depthCopyColorBuffer, 0);
        GlStateManager.framebufferTexture2D(FramebufferConstants.GL_FRAMEBUFFER, FramebufferConstants.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, depthCopyDepthBuffer, 0);
        GlStateManager.bindFramebuffer(FramebufferConstants.GL_FRAMEBUFFER, 0);
        shader().depthBuffer(depthCopyDepthBuffer);
    }

    private void deleteDepthCopyFramebuffer() {
        shader().depthBuffer(0);
        GlStateManager.deleteFramebuffers(depthCopyFbo);
        depthCopyFbo = 0;
        TextureUtil.releaseTextureId(depthCopyColorBuffer);
        depthCopyColorBuffer = 0;
        TextureUtil.releaseTextureId(depthCopyDepthBuffer);
        depthCopyDepthBuffer = 0;
    }

    private int createTexture(final int width, final int height, final int internalFormat, final int format, final int type) {
        final int texture = TextureUtil.generateTextureId();
        GlStateManager.bindTexture(texture);
        GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL14.GL_DEPTH_TEXTURE_MODE, GL11.GL_LUMINANCE);
        GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_COMPARE_MODE, GL14.GL_NONE);
        GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_COMPARE_FUNC, GL11.GL_LEQUAL);
        GlStateManager.texImage2D(GL11.GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, type, null);
        GlStateManager.bindTexture(0);
        return texture;
    }

    private void blit(final Framebuffer framebuffer) {
        final int width = framebuffer.framebufferWidth;
        final int height = framebuffer.framebufferHeight;
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
        setupMatrices(width, height);
        framebuffer.bindFramebuffer(true);
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(0, height, 0).tex(0, 0).endVertex();
        buffer.pos(width, height, 0).tex(1, 0).endVertex();
        buffer.pos(width, 0, 0).tex(1, 1).endVertex();
        buffer.pos(0, 0, 0).tex(0, 1).endVertex();
        tessellator.draw();
        restoreMatrices();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    private void setupMatrices(final int width, final int height) {
        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        RenderSystem.ortho(0, width, height, 0, 1000, 3000);
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        RenderSystem.translated(0, 0, -2000);
        RenderSystem.viewport(0, 0, width, height);
    }

    private void restoreMatrices() {
        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        RenderSystem.popMatrix();
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
        RenderSystem.popMatrix();
    }
}
