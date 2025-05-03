package fun.rockstarity.api.render.shaders.list;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.shaders.Shader;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.api.secure.nativeapi.FastNative; 

/**
 * @author ConeTin
 * @since 3 дек. 2023 г.
 */

public class Round extends Shader {
	
	private static final Round shader = new Round();
	private static final Shader textured = new Shader() {
		@Override
		public String getCode() {
			return readShader("texturedRound");
		}
	};
	private static final Shader outlined = new Shader() {
		@Override
		public String getCode() {
			return readShader("roundOutline");
		}
	};
	
	public static void draw(MatrixStack matrixStack, Rect rect, float round, FixColor color) {
		draw(matrixStack, rect, round, color, color, color, color);
	}
	
	public static void draw(MatrixStack matrixStack, Rect rect, float topLeft, float topRight, float bottomLeft, float bottomRight, FixColor color) {
		draw(matrixStack, rect, topLeft, topRight, bottomLeft, bottomRight, color, color, color, color);;
	}
	
	public static void draw(MatrixStack matrixStack, Rect rect, float round, FixColor gradientColor1, FixColor gradientColor2, FixColor gradientColor3, FixColor gradientColor4) {
		draw(matrixStack, rect, round, round, round, round, gradientColor1, gradientColor2, gradientColor3, gradientColor4);
	}
	
	public static void draw(MatrixStack matrixStack, Rect rect, float topLeft, float topRight, float bottomLeft, float bottomRight, FixColor gradientColor1, FixColor gradientColor2, FixColor gradientColor3, FixColor gradientColor4) {
	    if (!Shader.FLAT_RENDERER) {
	    	GlStateManager.enableBlend();
		    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		    Render.setAlphaLimit(0);
	    }

	    shader.start();
	    int err = GL11.glGetError(); // Проверка после активации
	    if (err != 0) System.out.println("Error after shader.start(): " + err);

	    shader.setFloat("size", (float) (rect.getWidth() * sr.getGuiScaleFactor()), (float) (rect.getHeight() * sr.getGuiScaleFactor()));
	    shader.setFloat("gradientColor1", gradientColor1.getRed() / 255F, gradientColor1.getGreen() / 255F, gradientColor1.getBlue() / 255F, gradientColor1.getAlpha() / 255F);
	    shader.setFloat("gradientColor2", gradientColor2.getRed() / 255F, gradientColor2.getGreen() / 255F, gradientColor2.getBlue() / 255F, gradientColor2.getAlpha() / 255F);
	    shader.setFloat("gradientColor3", gradientColor3.getRed() / 255F, gradientColor3.getGreen() / 255F, gradientColor3.getBlue() / 255F, gradientColor3.getAlpha() / 255F);
	    shader.setFloat("gradientColor4", gradientColor4.getRed() / 255F, gradientColor4.getGreen() / 255F, gradientColor4.getBlue() / 255F, gradientColor4.getAlpha() / 255F);
		// FOR ORIGINAL SHADER
//	    shader.setFloat("cornerRadius", (float) (topLeft * sr.getGuiScaleFactor()), (float) (bottomLeft * sr.getGuiScaleFactor()), (float) (topRight * sr.getGuiScaleFactor()), (float) (bottomRight * sr.getGuiScaleFactor()));

		float add = 0.0f;
		shader.setFloat("cornerRadius", (float) (topRight * sr.getGuiScaleFactor()) + add, (float) (bottomRight * sr.getGuiScaleFactor() + add), (float) (topLeft * sr.getGuiScaleFactor() + add), (float) (bottomLeft * sr.getGuiScaleFactor()) + add);
	    drawQuads(matrixStack, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
	    shader.finish();
	    err = GL11.glGetError(); // Проверка после деактивации
	    if (err != 0) System.out.println("Error after shader.finish(): " + err);
	    
	    if (!Shader.FLAT_RENDERER) {
	    	GlStateManager.disableBlend();
	    }
	}
	
	public static void drawTextured(MatrixStack matrixStack, String texture, Rect rect, float round, float alpha) {
		mc.getTextureManager().bindTexture(FastNative.getImageResource(texture));
		
	    GlStateManager.enableBlend();
	    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	    Render.setAlphaLimit(0);

	    textured.start();
	    textured.setFloat("rectSize", (float) (rect.getWidth() * sr.getGuiScaleFactor()), (float) (rect.getHeight() * sr.getGuiScaleFactor()));
	    textured.setInt("inTexture", 0);
	    textured.setFloat("alpha", alpha);
	    textured.setFloat("radius", (float) (round * sr.getGuiScaleFactor()));
	    drawQuads(matrixStack, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
	    textured.finish();

	    GlStateManager.disableBlend();
	}
	
	public static void drawTextured(MatrixStack matrixStack, Rect rect, float round, float alpha) {
	    GlStateManager.enableBlend();
	    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	    Render.setAlphaLimit(0);

	    textured.start();
	    textured.setFloat("rectSize", (float) (rect.getWidth() * sr.getGuiScaleFactor()), (float) (rect.getHeight() * sr.getGuiScaleFactor()));
	    textured.setInt("inTexture", 0);
	    textured.setFloat("alpha", alpha);
	    textured.setFloat("radius", (float) (round * sr.getGuiScaleFactor()));
	    drawQuads(matrixStack, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
	    textured.finish();

	    GlStateManager.disableBlend();
	}
	
	public static void drawOutlined(MatrixStack matrixStack, Rect rect, float round, float outline, FixColor color, FixColor outlineColor) {
	    drawOutlined(matrixStack, rect, round, outline, color, outlineColor, false);
	}

	public static void drawOutlined(MatrixStack matrixStack, Rect rect, float round, float outline, FixColor color, FixColor outlineColor, boolean inner) {
	    GlStateManager.enableBlend();
	    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	    Render.setAlphaLimit(0);

	    outlined.start();
	    outlined.setFloat("rectSize", (float) (rect.getWidth() * sr.getGuiScaleFactor()), (float) (rect.getHeight() * sr.getGuiScaleFactor()));
	    outlined.setFloat("color", color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
	    outlined.setFloat("outlineColor", outlineColor.getRed() / 255F, outlineColor.getGreen() / 255F, outlineColor.getBlue() / 255F, outlineColor.getAlpha() / 255F);
	    outlined.setFloat("radius", (float) (round * sr.getGuiScaleFactor()));
	    if (inner) {
	    	drawQuads(matrixStack, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
	    } else {
	    	drawQuads(matrixStack, rect.getX() - outline, rect.getY() - outline, rect.getWidth() + outline * 2, rect.getHeight() + outline * 2);
	    }
	    outlined.finish();

	    GlStateManager.disableBlend();
	}

	@Override
	public String getCode() {
		return readShader("round");
	}
	
}
