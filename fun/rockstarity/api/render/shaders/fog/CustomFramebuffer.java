package fun.rockstarity.api.render.shaders.fog;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.IAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.math.vector.Matrix4f;

public class CustomFramebuffer extends Framebuffer implements IAccess {
    private boolean linear;

    public CustomFramebuffer(int width, int height, boolean useDepth) {
        super(width, height, useDepth, Minecraft.IS_RUNNING_ON_MAC);
    }

    public CustomFramebuffer(boolean useDepth) {
        super(1, 1, useDepth, Minecraft.IS_RUNNING_ON_MAC);
    }

    private static boolean resizeFramebuffer(CustomFramebuffer framebuffer) {
        if (needsNewFramebuffer(framebuffer)) {
            framebuffer.createBuffers(Math.max(mc.getMainWindow().getFramebufferWidth(), 1), Math.max(mc.getMainWindow().getFramebufferHeight(), 1), Minecraft.IS_RUNNING_ON_MAC);
            return true;
        }
        return false;
    }

    public CustomFramebuffer setLinear() {
        this.linear = true;
        return this;
    }

    @Override
    public void setFramebufferFilter(int framebufferFilterIn) {
        super.setFramebufferFilter(this.linear ? 9729 : framebufferFilterIn);
    }

    public void setup() {
        resizeFramebuffer(this);
        this.framebufferClear(Minecraft.IS_RUNNING_ON_MAC);
        this.bindFramebuffer(false);
    }

    public static void drawQuads(MatrixStack matrix, double x, double y, double width, double height) {
        Matrix4f matrix4f = matrix.getLast().getMatrix();
        BUILDER.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        BUILDER.pos(matrix4f, (float) x, (float) y, 0).tex(0, 1).endVertex();
        BUILDER.pos(matrix4f, (float) x, (float) (y + height), 0).tex(0, 0).endVertex();
        BUILDER.pos(matrix4f, (float) (x + width), (float) (y + height), 0).tex(1, 0).endVertex();
        BUILDER.pos(matrix4f, (float) (x + width), (float) y, 0).tex(1, 1).endVertex();
        TESSELLATOR.draw();
    }

    public static void drawQuads(double x, double y, double width, double height) {
    	BUILDER.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        BUILDER.pos(x, y, 0).tex(0, 1).endVertex();
        BUILDER.pos(x, y + height, 0).tex(0, 0).endVertex();
        BUILDER.pos(x + width, y + height, 0).tex(1, 0).endVertex();
        BUILDER.pos(x + width, y, 0).tex(1, 1).endVertex();
        TESSELLATOR.draw();
    }

    public static void drawQuads(MatrixStack matrix) {
        Vector2d window = ScaleMath.getMouse(mc.getMainWindow().getScaledWidth(), mc.getMainWindow().getScaledHeight());
        double width = window.x;
        double height = window.y;
        drawQuads(matrix, 0, 0, width, height);
    }

    public static void drawQuads() {
        Vector2d window = ScaleMath.getMouse(mc.getMainWindow().getScaledWidth(), mc.getMainWindow().getScaledHeight());
        double width = window.x;
        double height = window.y;
        drawQuads(0, 0, width, height);
    }

    public static void drawQuads(MatrixStack matrix, double width, double height) {
        drawQuads(matrix, 0, 0, width, height);
    }

    public static void drawQuads(double width, double height) {
        drawQuads(0, 0, width, height);
    }

    public static void drawQuads(MatrixStack matrix, double x, double y, double width, double height, int color) {
        Matrix4f matrix4f = matrix.getLast().getMatrix();
        BUILDER.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
        BUILDER.pos(matrix4f, (float) x, (float) y, 0).color(color).tex(0, 1).endVertex();
        BUILDER.pos(matrix4f, (float) x, (float) (y + height), 0).color(color).tex(0, 0).endVertex();
        BUILDER.pos(matrix4f, (float) (x + width), (float) (y + height), 0).color(color).tex(1, 0).endVertex();
        BUILDER.pos(matrix4f, (float) (x + width), (float) y, 0).color(color).tex(1, 1).endVertex();
        TESSELLATOR.draw();
    }

    public static void drawQuads(double x, double y, double width, double height, int color) {
    	BUILDER.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
        BUILDER.pos(x, y, 0).color(color).tex(0, 1).endVertex();
        BUILDER.pos(x, y + height, 0).color(color).tex(0, 0).endVertex();
        BUILDER.pos(x + width, y + height, 0).color(color).tex(1, 0).endVertex();
        BUILDER.pos(x + width, y, 0).color(color).tex(1, 1).endVertex();
        TESSELLATOR.draw();
    }

    public static void drawQuads(MatrixStack matrix, int color) {
        Vector2d window = ScaleMath.getMouse(mc.getMainWindow().getScaledWidth(), mc.getMainWindow().getScaledHeight());
        double width = window.x;
        double height = window.y;
        drawQuads(matrix, 0, 0, width, height, color);
    }

    public static void drawQuads(int color) {
        Vector2d window = ScaleMath.getMouse(mc.getMainWindow().getScaledWidth(), mc.getMainWindow().getScaledHeight());
        double width = window.x;
        double height = window.y;
        drawQuads(0, 0, width, height, color);
    }

    public static void drawQuads(MatrixStack matrix, double width, double height, int color) {
        drawQuads(matrix, 0, 0, width, height, color);
    }

    public static void drawQuads(double width, double height, int color) {
        drawQuads(0, 0, width, height, color);
    }

    public void draw() {
        this.bindFramebufferTexture();
        drawQuads();
    }

    public void draw(int color) {
        this.bindFramebufferTexture();
        drawQuads(color);
    }

    public void draw(Framebuffer framebuffer) {
        framebuffer.bindFramebufferTexture();
        drawQuads();
    }

    public void stop() {
        unbindFramebuffer();
        mc.getFramebuffer().bindFramebuffer(true);
    }

    public static CustomFramebuffer createFrameBuffer(CustomFramebuffer framebuffer) {
        return createFrameBuffer(framebuffer, false);
    }

    public static CustomFramebuffer createFrameBuffer(CustomFramebuffer framebuffer, boolean depth) {
        if (needsNewFramebuffer(framebuffer)) {
            if (framebuffer != null) {
                framebuffer.deleteFramebuffer();
            }
            return new CustomFramebuffer(mc.getMainWindow().getFramebufferWidth(), mc.getMainWindow().getFramebufferHeight(), depth);
        }
        return framebuffer;
    }

    public static boolean needsNewFramebuffer(CustomFramebuffer framebuffer) {
        return framebuffer == null || framebuffer.framebufferWidth != mc.getMainWindow().getFramebufferWidth() || framebuffer.framebufferHeight != mc.getMainWindow().getFramebufferHeight();
    }
}