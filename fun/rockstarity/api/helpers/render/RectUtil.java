package fun.rockstarity.api.helpers.render;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.render.color.FixColor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;

public class RectUtil implements IAccess {

    public static final List<Vec2fColored> VERTEXES = new ArrayList<>();

    static final int[] LEFT_UP = new int[]{-90, 0}, RIGHT_UP = new int[]{0, 90}, RIGHT_DOWN = new int[]{90, 180}, LEFT_DOWN = new int[]{180, 270};

    public static void drawRect(MatrixStack matrix, double x, double y, double width, double height, int color1, int color2, int color3, int color4, boolean bloom, boolean texture) {
        VERTEXES.clear();
        VERTEXES.add(new Vec2fColored(x, y, color1));
        VERTEXES.add(new Vec2fColored(x + width, y, color2));
        VERTEXES.add(new Vec2fColored(x + width, y + height, color3));
        VERTEXES.add(new Vec2fColored(x, y + height, color4));
        drawVertexesList2D(matrix, VERTEXES, GL11.GL_POLYGON, texture, bloom);
    }

    public static void drawForceRect(MatrixStack matrix, double x, double y, double width, double height, int color1, int color2, int color3, int color4, boolean bloom, boolean texture) {
        x = MathUtility.step(x, 0.5);
        y = MathUtility.step(y, 0.5);
        width = MathUtility.step(width, 0.5);
        height = MathUtility.step(height, 0.5);
        drawRect(matrix, x, y, width, height, color1, color2, color3, color4, bloom, texture);
    }

    public static void drawGradientV(MatrixStack matrix, double x, double y, double width, double height, int color, int color2, boolean bloom) {
        drawRect(matrix, x, y, width, height, color, color, color2, color2, bloom, false);
    }

    public static void drawGradientH(MatrixStack matrix, double x, double y, double width, double height, int color, int color2, boolean bloom) {
        drawRect(matrix, x, y, width, height, color, color2, color2, color, bloom, false);
    }

    public static void drawRect(MatrixStack matrix, double x, double y, double width, double height, int color, boolean bloom) {
        drawRect(matrix, x, y, width, height, color, color, color, color, bloom, false);
    }

    public static void drawRect(MatrixStack matrix, double x, double y, double width, double height, int color, boolean bloom, boolean texture) {
        drawRect(matrix, x, y, width, height, color, color, color, color, bloom, texture);
    }

    public static void drawForceRect(MatrixStack matrix, double x, double y, double width, double height, int color, boolean bloom, boolean texture) {
        drawForceRect(matrix, x, y, width, height, color, color, color, color, bloom, texture);
    }

    public static void drawRect(MatrixStack matrix, double x, double y, double width, double height, int color) {
        drawRect(matrix, x, y, width, height, color, false);
    }

    public static List<Vec2fColored> generateRadiusCircledVertexes(double x, double y, double radius1, double radius2, double startRadius, double endRadius, double step, boolean doublepart, int color) {
        final List<Vec2fColored> VERTEXES = new ArrayList<>();
        double radius = startRadius;
        while (radius <= endRadius) {
            if (radius > endRadius) radius = endRadius;
            double x1 = Math.sin(Math.toRadians(radius)) * radius1;
            double y1 = -Math.cos(Math.toRadians(radius)) * radius1;
            VERTEXES.add(new Vec2fColored(x + x1, y + y1, color));
            if (doublepart) {
                x1 = Math.sin(Math.toRadians(radius)) * radius2;
                y1 = -Math.cos(Math.toRadians(radius)) * radius2;
                VERTEXES.add(new Vec2fColored(x + x1, y + y1, color));
            }
            radius += step;
        }
        return VERTEXES;
    }

    public static void drawDuadsSegment(MatrixStack matrix, double x, double y, double radius, double expand, int color1, int color2, boolean bloom, int[] side) {
        VERTEXES.clear();
        int index = 0;
        for (Vec2fColored vec2f : generateRadiusCircledVertexes(x, y, radius, radius + expand, side[0], side[1], 9, true, -1)) {
            VERTEXES.add(new Vec2fColored(vec2f.getX(), vec2f.getY(), index % 2 == 0 ? color1 : color2));
            index++;
        }
        drawVertexesList2D(matrix, VERTEXES, GL12.GL_TRIANGLE_STRIP, false, bloom);
    }

    public static void drawDuadsCircle(MatrixStack matrix, float x, float y, double radius, double c360, float width, int color1, int color2, boolean bloom) {
        VERTEXES.clear();
        int index = 0;
        for (Vec2fColored vec2f : generateRadiusCircledVertexes(x, y, radius, 0, 180, 180 + c360 - 1, 1, false, -1)) {
            VERTEXES.add(new Vec2fColored(vec2f.getX(), vec2f.getY(), index % 2 == 0 ? color1 : color2));
            index++;
        }
        GL11.glPointSize(width);
        drawVertexesList2D(matrix, VERTEXES, GL11.GL_POINTS, false, bloom);
        GL11.glPointSize(1F);
    }

    public static void drawShadowSegment(MatrixStack matrix, double x, double y, double radiusRound, double radiusShadow, int color, boolean sageColor, int[] side, boolean bloom) {
        int c = sageColor ? 0 : ColorUtility.replAlpha(color, 0);
        drawDuadsSegment(matrix, x, y, radiusRound, radiusShadow, color, c, bloom, side);
    }

    public static void drawRoundSegment(MatrixStack matrix, double x, double y, double radius, int color, int[] side, boolean bloom) {
        drawDuadsSegment(matrix, x, y, 0, radius, color, color, bloom, side);
    }

    public static void drawShadowSegmentsExtract(MatrixStack matrix, double x, double y, double width, double height, double radiusStart, double radiusEnd, int color1, int color2, int color3, int color4, boolean sageColor, boolean bloom) {
        drawShadowSegment(matrix, x, y, radiusStart, radiusEnd, color1, sageColor, LEFT_UP, bloom);
        drawShadowSegment(matrix, x + width, y, radiusStart, radiusEnd, color2, sageColor, RIGHT_UP, bloom);
        drawShadowSegment(matrix, x + width, y + height, radiusStart, radiusEnd, color3, sageColor, RIGHT_DOWN, bloom);
        drawShadowSegment(matrix, x, y + height, radiusStart, radiusEnd, color4, sageColor, LEFT_DOWN, bloom);
    }

    public static void drawRoundSegments(MatrixStack matrix, double x, double y, double width, double height, double radius, int color1, int color2, int color3, int color4, boolean bloom) {
        drawRoundSegment(matrix, x, y, radius, color1, LEFT_UP, bloom);
        drawRoundSegment(matrix, x + width, y, radius, color2, RIGHT_UP, bloom);
        drawRoundSegment(matrix, x + width, y + height, radius, color3, RIGHT_DOWN, bloom);
        drawRoundSegment(matrix, x, y + height, radius, color4, LEFT_DOWN, bloom);
    }

    public static void drawLimitersSegments(MatrixStack matrix, double x, double y, double width, double height, double radius, double appendOffsets, int color1, int color2, int color3, int color4, boolean sageColor, boolean retainZero, boolean bloom) {
        int c5 = retainZero ? sageColor ? 0 : ColorUtility.replAlpha(color1, 0) : color1;
        int c6 = retainZero ? sageColor ? 0 : ColorUtility.replAlpha(color2, 0) : color2;
        int c7 = retainZero ? sageColor ? 0 : ColorUtility.replAlpha(color3, 0) : color3;
        int c8 = retainZero ? sageColor ? 0 : ColorUtility.replAlpha(color4, 0) : color4;

        //up
        drawRect(matrix, x + appendOffsets, y - radius, width - appendOffsets, radius, c5, c6, color2, color1, bloom, false);
        //down
        drawRect(matrix, x + appendOffsets, y + height + appendOffsets, width - appendOffsets, radius, color4, color3, c7, c8, bloom, false);
        //left
        drawRect(matrix, x - radius, y + appendOffsets, radius, height - appendOffsets, c5, color1, color4, c8, bloom, false);
        //right
        drawRect(matrix, x + width + appendOffsets, y + appendOffsets, radius, height - appendOffsets, color2, c6, c7, color3, bloom, false);
    }


    public static void drawRoundedRectShadowed(MatrixStack matrix, double x, double y, double width, double height, double round, double shadowSize, int color1, int color2, int color3, int color4, boolean bloom, boolean sageColor, boolean rect, boolean shadow) {
        double roundMax = Math.min(width, height) / 2;
        round = Math.max(Math.min(round, roundMax), 0);

        x += round;
        y += round;
        width -= (round * 2);
        height -= (round * 2);

        if (rect) {
            drawRect(matrix, x, y, width, height, color1, color2, color3, color4, bloom, false);
            if (round != 0) {
                drawLimitersSegments(matrix, x, y, width, height, round, 0, color1, color2, color3, color4, true, false, bloom);
                drawRoundSegments(matrix, x, y, width, height, round, color1, color2, color3, color4, bloom);
            }
        }

        if (shadow && shadowSize != 0) {
            drawLimitersSegments(matrix, x - round, y - round, width + round, height + round, shadowSize, round, color1, color2, color3, color4, sageColor, true, bloom);
            drawShadowSegmentsExtract(matrix, x, y, width, height, round, shadowSize, color1, color2, color3, color4, sageColor, bloom);
        }
    }

    public static void drawHeadSide(MatrixStack matrixStack, double x, double y, double z, double width, double height, HeadSide head, int color) {
        matrixStack.push();
        float[] uv = calculateUV(head.x1, head.y1, head.x2, head.y2, head.size, head.size);

        Matrix4f matrix4f = matrixStack.getLast().getMatrix();

        BUILDER.begin(GlStateManager.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        BUILDER.pos(matrix4f, (float) x, (float) (y + height), (float) z).tex(uv[0], uv[1]).color(color).endVertex();
        BUILDER.pos(matrix4f, (float) (x + width), (float) (y + height), (float) z).tex(uv[2], uv[1]).color(color).endVertex();
        BUILDER.pos(matrix4f, (float) (x + width), (float) y, (float) z).tex(uv[2], uv[3]).color(color).endVertex();
        BUILDER.pos(matrix4f, (float) x, (float) y, (float) z).tex(uv[0], uv[3]).color(color).endVertex();

        TESSELLATOR.draw();

        matrixStack.pop();
    }

    private static float[] calculateUV(int x1, int y1, int x2, int y2, int texWidth, int texHeight) {
        float u1 = (float) x1 / texWidth;
        float v1 = (float) y1 / texHeight;
        float u2 = (float) x2 / texWidth;
        float v2 = (float) y2 / texHeight;

        return new float[]{u1, v1, u2, v2};
    }

    @RequiredArgsConstructor
    public enum HeadSide {
        FRONT(8, 8, 16, 16),
        BACK(24, 8, 32, 16),
        RIGHT(0, 8, 8, 16),
        LEFT(16, 8, 24, 16),
        TOP(8, 0, 16, 8),
        BOTTOM(16, 0, 24, 8),
        //----------------------------------//
        O_FRONT(40, 8, 48, 16),
        O_BACK(56, 8, 64, 16),
        O_RIGHT(32, 8, 40, 16),
        O_LEFT(48, 8, 56, 16),
        O_TOP(40, 0, 48, 8),
        O_BOTTOM(48, 0, 56, 8);
        private final int x1, y1, x2, y2;
        private final int size = 64;
    }

    public static void setupRenderRect(boolean texture, boolean bloom) {
        if (texture) RenderSystem.enableTexture();
        else RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.disableCull();
        RenderSystem.shadeModel(7425);
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, bloom ? GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA : GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.alphaFunc(GL11.GL_GREATER, 0F);
        GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glEnable(GL11.GL_POINT_SMOOTH);
    }

    public static void endRenderRect(boolean bloom) {
        RenderSystem.enableAlphaTest();
        if (bloom)
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.shadeModel(7424);
        RenderSystem.enableCull();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.enableTexture();
        RenderSystem.clearCurrentColor();
    }

    public static void drawVertexesList2D(MatrixStack matrix, List<Vec2fColored> vec2c, int begin, boolean texture, boolean bloom) {
        setupRenderRect(texture, bloom);
        BUILDER.begin(begin, texture ? DefaultVertexFormats.POSITION_TEX_COLOR : DefaultVertexFormats.POSITION_COLOR);
        int counter = 0;
        for (final Vec2fColored vec : vec2c) {
            float[] rgba = ColorUtility.getRGBAf(vec.getColor());
            BUILDER.pos(matrix.getLast().getMatrix(), (float) vec.getX(), (float) vec.getY());
            counter = getCounter(texture, counter, rgba);
        }
        TESSELLATOR.draw();
        endRenderRect(bloom);
    }

    public static void drawVertexesList2D(MatrixStack matrix, List<Vec2fColored> vec2c, int begin, boolean texture, boolean bloom, float alphaPC) {
        setupRenderRect(texture, bloom);
        BUILDER.begin(begin, texture ? DefaultVertexFormats.POSITION_TEX_COLOR : DefaultVertexFormats.POSITION_COLOR);
        int counter = 0;
        for (final Vec2fColored vec : vec2c) {
            float[] rgba = ColorUtility.getRGBAf(vec.getColor());
            rgba[3] *= alphaPC;
            BUILDER.pos(matrix.getLast().getMatrix(), (float) vec.getX(), (float) vec.getY());
            counter = getCounter(texture, counter, rgba);
        }
        TESSELLATOR.draw();
        endRenderRect(bloom);
    }

    public static void drawVertexesList2D(MatrixStack matrix, List<Vec2fColored> vec2c, int begin, boolean texture, boolean bloom, int anotherColor) {
        setupRenderRect(texture, bloom);
        BUILDER.begin(begin, texture ? DefaultVertexFormats.POSITION_TEX_COLOR : DefaultVertexFormats.POSITION_COLOR);
        int counter = 0;
        for (final Vec2fColored vec : vec2c) {
            float[] rgba = ColorUtility.getRGBAf(anotherColor);
            BUILDER.pos(matrix.getLast().getMatrix(), (float) vec.getX(), (float) vec.getY());
            counter = getCounter(texture, counter, rgba);
        }
        TESSELLATOR.draw();
        endRenderRect(bloom);
    }

    private static int getCounter(boolean texture, int counter, float[] rgba) {
        if (texture) BUILDER.tex(counter == 0 || counter == 3 ? 0 : 1, counter == 0 || counter == 1 ? 0 : 1);
        BUILDER.color(rgba[0], rgba[1], rgba[2], rgba[3]);
        BUILDER.endVertex();
        counter++;
        return counter;
    }


    @Getter
    public static class Vec2fColored {
        @Getter
        public static double x, y;
        @Getter
        public static int color;

        public Vec2fColored(double x1, double y1) {
            x = x1;
            y = y1;
            color = -1;
        }

        public Vec2fColored(double x1, double y1, int color1) {
            x = x1;
            y = y1;
            color = color1;
        }
    }
}
