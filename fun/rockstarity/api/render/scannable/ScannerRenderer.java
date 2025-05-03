/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package fun.rockstarity.api.render.scannable;

import org.lwjgl.opengl.GL30;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.render.scannable.shader.ScanEffectShader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.FramebufferConstants;
import net.minecraft.client.util.JSONBlendingMode;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector4f;

public enum ScannerRenderer {
    INSTANCE;

    private int depthCopyFbo;
    private int depthCopyColorBuffer;
    private int depthCopyDepthBuffer;
    private static final JSONBlendingMode RESET_BLEND_STATE;
    private long currentStart;
    private float radius;

    public void ping(Vector3d pos) {
        this.currentStart = System.currentTimeMillis();
        ScanEffectShader.INSTANCE.setCenter(pos);
    }

    public static void render(MatrixStack matrixStack, Matrix4f projectionMatrix) {
        INSTANCE.doRender(matrixStack, projectionMatrix);
    }

    public void doRender(MatrixStack matrixStack, Matrix4f projectionMatrix) {
        boolean shouldRender;
        int adjustedDuration = ScanManager.computeScanGrowthDuration();
        boolean bl = shouldRender = this.currentStart > 0L && adjustedDuration > (int)(System.currentTimeMillis() - this.currentStart);
        if (shouldRender) {
            if (this.depthCopyFbo == 0) {
                this.createDepthCopyFramebuffer();
            }
            this.render(matrixStack.getLast().getMatrix(), projectionMatrix);
        } else {
            if (this.depthCopyFbo != 0) {
                this.deleteDepthCopyFramebuffer();
            }
            this.currentStart = 0L;
        }
    }

    private void render(Matrix4f viewMatrix, Matrix4f projectionMatrix) {
        Minecraft mc = Minecraft.getInstance();
        Framebuffer framebuffer = mc.getFramebuffer();
        this.updateDepthTexture(framebuffer);
        Style themeColor = Style.getCurrent();
        float r = (float)themeColor.getSecond().getRed() / 255.0f;
        float g = (float)themeColor.getSecond().getGreen() / 255.0f;
        float b = (float)themeColor.getSecond().getBlue() / 255.0f;
        float a = (float)themeColor.getSecond().getAlpha() / 255.0f;
        float r1 = (float)themeColor.getMain().getRed() / 255.0f;
        float g1 = (float)themeColor.getMain().getGreen() / 255.0f;
        float b1 = (float)themeColor.getMain().getBlue() / 255.0f;
        float a1 = (float)themeColor.getMain().getAlpha() / 255.0f;
        float r2 = (float)themeColor.getMain().getRed() / 255.0f;
        float g2 = (float)themeColor.getMain().getGreen() / 255.0f;
        float b2 = (float)themeColor.getMain().getBlue() / 255.0f;
        float a2 = (float)themeColor.getMain().getAlpha() / 255.0f;
        ScanEffectShader.INSTANCE.setOuterColor(new Vector4f(r1, g1, b1, a));
        ScanEffectShader.INSTANCE.setMidColor(new Vector4f(0.0f, 0.0f, 0.0f, 1.0f));
        ScanEffectShader.INSTANCE.setInnerColor(new Vector4f(r2, g2, b2, a2));
        ScanEffectShader.INSTANCE.setScanlineColor(new Vector4f(r, g, b, a));
        Matrix4f invertedViewMatrix = new Matrix4f(viewMatrix);
        invertedViewMatrix.invert();
        ScanEffectShader.INSTANCE.setInverseViewMatrix(invertedViewMatrix);
        Matrix4f invertedProjectionMatrix = new Matrix4f(projectionMatrix);
        invertedProjectionMatrix.invert();
        ScanEffectShader.INSTANCE.setInverseProjectionMatrix(invertedProjectionMatrix);
        Vector3d position = mc.gameRenderer.getActiveRenderInfo().getProjectedView();
        ScanEffectShader.INSTANCE.setPosition(position);
        int adjustedDuration = ScanManager.computeScanGrowthDuration();
        float radius = ScanManager.computeRadius(this.currentStart, adjustedDuration);
        ScanEffectShader.INSTANCE.setRadius(radius);
        RESET_BLEND_STATE.apply();
        ScanEffectShader.INSTANCE.bind();
        this.blit(framebuffer);
        ScanEffectShader.INSTANCE.unbind();
        this.radius = radius;
    }

    private void updateDepthTexture(Framebuffer framebuffer) {
        GlStateManager.bindFramebuffer(36008, framebuffer.framebufferObject);
        GlStateManager.bindFramebuffer(36009, this.depthCopyFbo);
        GL30.glBlitFramebuffer(0, 0, framebuffer.framebufferWidth, framebuffer.framebufferHeight, 0, 0, framebuffer.framebufferWidth, framebuffer.framebufferHeight, 256, 9728);
    }

    private void createDepthCopyFramebuffer() {
        Framebuffer framebuffer = Minecraft.getInstance().getFramebuffer();
        this.depthCopyFbo = GlStateManager.genFramebuffers();
        this.depthCopyColorBuffer = this.createTexture(framebuffer.framebufferWidth, framebuffer.framebufferHeight, 32856, 6408, 5121);
        this.depthCopyDepthBuffer = this.createTexture(framebuffer.framebufferWidth, framebuffer.framebufferHeight, 6402, 6402, 5126);
        GlStateManager.bindFramebuffer(FramebufferConstants.GL_FRAMEBUFFER, this.depthCopyFbo);
        GlStateManager.framebufferTexture2D(FramebufferConstants.GL_FRAMEBUFFER, FramebufferConstants.GL_COLOR_ATTACHMENT0, 3553, this.depthCopyColorBuffer, 0);
        GlStateManager.framebufferTexture2D(FramebufferConstants.GL_FRAMEBUFFER, FramebufferConstants.GL_DEPTH_ATTACHMENT, 3553, this.depthCopyDepthBuffer, 0);
        GlStateManager.bindFramebuffer(FramebufferConstants.GL_FRAMEBUFFER, 0);
        ScanEffectShader.INSTANCE.setDepthBuffer(this.depthCopyDepthBuffer);
    }

    private void deleteDepthCopyFramebuffer() {
        ScanEffectShader.INSTANCE.setDepthBuffer(0);
        GlStateManager.deleteFramebuffers(this.depthCopyFbo);
        this.depthCopyFbo = 0;
        TextureUtil.releaseTextureId(this.depthCopyColorBuffer);
        this.depthCopyColorBuffer = 0;
        TextureUtil.releaseTextureId(this.depthCopyDepthBuffer);
        this.depthCopyDepthBuffer = 0;
    }

    private int createTexture(int width, int height, int internalFormat, int format, int type) {
        int texture = TextureUtil.generateTextureId();
        GlStateManager.bindTexture(texture);
        GlStateManager.texParameter(3553, 10242, 10497);
        GlStateManager.texParameter(3553, 10243, 10497);
        GlStateManager.texParameter(3553, 10241, 9728);
        GlStateManager.texParameter(3553, 10240, 9728);
        GlStateManager.texParameter(3553, 34891, 6409);
        GlStateManager.texParameter(3553, 34892, 0);
        GlStateManager.texParameter(3553, 34893, 515);
        GlStateManager.texImage2D(3553, 0, internalFormat, width, height, 0, format, type, null);
        GlStateManager.bindTexture(0);
        return texture;
    }

    private void blit(Framebuffer framebuffer) {
        int width = framebuffer.framebufferWidth;
        int height = framebuffer.framebufferHeight;
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
        this.setupMatrices(width, height);
        framebuffer.bindFramebuffer(false);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(0.0, height, 0.0).tex(0.0f, 0.0f).endVertex();
        buffer.pos(width, height, 0.0).tex(1.0f, 0.0f).endVertex();
        buffer.pos(width, 0.0, 0.0).tex(1.0f, 1.0f).endVertex();
        buffer.pos(0.0, 0.0, 0.0).tex(0.0f, 1.0f).endVertex();
        tessellator.draw();
        this.restoreMatrices();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    private void setupMatrices(int width, int height) {
        RenderSystem.matrixMode(5889);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        RenderSystem.ortho(0.0, width, height, 0.0, 1000.0, 3000.0);
        RenderSystem.matrixMode(5888);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        RenderSystem.translated(0.0, 0.0, -2000.0);
        RenderSystem.viewport(0, 0, width, height);
    }

    private void restoreMatrices() {
        RenderSystem.matrixMode(5889);
        RenderSystem.popMatrix();
        RenderSystem.matrixMode(5888);
        RenderSystem.popMatrix();
    }

    public float getRadius() {
        return this.radius;
    }

    static {
        RESET_BLEND_STATE = new JSONBlendingMode();
    }
}

