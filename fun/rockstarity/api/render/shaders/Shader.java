package fun.rockstarity.api.render.shaders;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform2f;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUniform4f;
import static org.lwjgl.opengl.GL20.glUseProgram;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.helpers.system.FileUtility;
import fun.rockstarity.api.secure.nativeapi.NativeHelper;
import lombok.Getter;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;

/**
 * @author ConeTin
 * @since 3 дек. 2023 г.
 */

public class Shader implements IAccess {
	
	public static boolean FLAT_RENDERER;
	@Getter
	private final int id;
	
	public Shader() {
		int program = glCreateProgram();
		glAttachShader(program, getShader());
		glAttachShader(program, getVertex());
        glLinkProgram(program);
        this.id = program;
	}
	
	public String getCode() {
    	return "";
    }
	
	public void start() {
        glUseProgram(id);
    }

    public void finish() {
        glUseProgram(0);
    }
    
    public int getShader() {
    	return compile(new ByteArrayInputStream(getCode().getBytes()), GL_FRAGMENT_SHADER);
    }
    
    private int compile(InputStream inputStream, int shaderType) {
        int shader = glCreateShader(shaderType);
        glShaderSource(shader, FileUtility.readInputStream(inputStream));
        glCompileShader(shader);
        return shader;
    }
    
    public int getVertex() {
    	return compile(new ByteArrayInputStream(new String(
    			  "#version 120\r\n"
    			+ "\r\n"
    			+ "void main() {\r\n"
    			+ "    gl_TexCoord[0] = gl_MultiTexCoord0;\r\n"
    			+ "    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\r\n"
    			+ "}"
    			).getBytes()), GL_VERTEX_SHADER);
    }
    
    public static void drawQuads(MatrixStack matrixStack, float width, float height) {
        drawQuads(matrixStack, 0, 0, width, height);
    }

    public static void drawQuads(MatrixStack matrixStack) {
        drawQuads(matrixStack, sr.getScaledWidth(), sr.getScaledHeight());
    }
    
    public static void drawQuads() {
        float width = (float) sr.getScaledWidth();
        float height = (float) sr.getScaledHeight();
        GL11.glBegin(GL_QUADS);
        GL11.glTexCoord2f(0, 1);
        GL11.glVertex2f(0, 0);
        GL11.glTexCoord2f(0, 0);
        GL11.glVertex2f(0, height);
        GL11.glTexCoord2f(1, 0);
        GL11.glVertex2f(width, height);
        GL11.glTexCoord2f(1, 1);
        GL11.glVertex2f(width, 0);
        GL11.glEnd();
    }
    

	/**
	 * Объясняю:
	 * В начале большого блока с ректами пишешь Shader.startFlat()
	 * В конце блока пишешь Shader.endFlat()
	 * 
	 * Это позволяет рендерить большой блок ректов в 1 запрос рендера, 
	 * что немножко оптимизует рендер ректов
	 */
    public static void startFlat() {
    	FLAT_RENDERER = true;
    	
    	GlStateManager.enableBlend();
	    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	    Render.setAlphaLimit(0);
    	BUILDER.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX);
    }

    public static void drawQuads(MatrixStack matrixStack, float x, float y, float width, float height) {
    	if (!FLAT_RENDERER) {
        	BUILDER.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX);
    	}
    	
        BUILDER.pos(matrixStack.getLast().getMatrix(), x, y, 0).tex(0, 0).endVertex();
        BUILDER.pos(matrixStack.getLast().getMatrix(), x, y + height, 0).tex(0, 1).endVertex();
        BUILDER.pos(matrixStack.getLast().getMatrix(), x+width, y + height, 0).tex(1, 1).endVertex();
        BUILDER.pos(matrixStack.getLast().getMatrix(), x+width, y, 0).tex(1, 0).endVertex();
        
        if (!FLAT_RENDERER) {
        	TESSELLATOR.draw();
    	}
    }
    
    public static void endFlat() {
    	if (!FLAT_RENDERER) return;
    	
    	TESSELLATOR.draw();
    	GlStateManager.disableBlend();

    	FLAT_RENDERER = false;
    }
    
    public static void drawScaledQuads() {
    	drawQuadsESP(0.0, 0.0, sr.getScaledWidth(), sr.getScaledHeight());
    }
    
    public static void drawQuadsESP(final double x, final double y, final double width, final double height) {
    	GL11.glBegin(GL_QUADS);
        GL11.glTexCoord2f(0.0F, 0.0F);
        if (mc.getGameSettings().fullscreen || sr.getHeight() < 847) {
        	GL11.glVertex2d(x, y + height);
        	GL11.glTexCoord2f(1.0F, 0.0F);
        	GL11.glVertex2d(x + width, y + height);
        }else {
        	GL11.glVertex2d(x, y + height - 0.5f);
        	GL11.glTexCoord2f(1.0F, 0.0F);
            GL11.glVertex2d(x + width, y + height - 0.5f);
        }
        GL11.glTexCoord2f(1.0F, 1.0F);
        GL11.glVertex2d(x + width, y);
        GL11.glTexCoord2f(0.0F, 1.0F);
        GL11.glVertex2d(x, y);
        GL11.glEnd();
    }
    
    public void setFloatBuffer(String name, FloatBuffer floatBuffer) {
        int loc = glGetUniformLocation(id, name);
        GL20.glUniform1fv(loc, floatBuffer);
    }
    
    public void setInt(String name, int... args) {
        int loc = glGetUniformLocation(id, name);
        if (args.length > 1) GL20.glUniform2i(loc, args[0], args[1]);
        else GL20.glUniform1i(loc, args[0]);
    }

    public void setFloat(String name, float... args) {
        int loc = glGetUniformLocation(id, name);
        switch (args.length) {
            case 1:
                glUniform1f(loc, args[0]);
                break;
            case 2:
                glUniform2f(loc, args[0], args[1]);
                break;
            case 3:
                glUniform3f(loc, args[0], args[1], args[2]);
                break;
            case 4:
                glUniform4f(loc, args[0], args[1], args[2], args[3]);
                break;
        }
    }
    
    public int getInt(String name) {
    	return glGetUniformLocation(id, name);
    }
    
    public int get(String name) {
    	return glGetUniformLocation(id, name);
    }
    
    public String readShader(String name) {
    	return NativeHelper.readShader(name);
    }
	
    public static Framebuffer createFrameBuffer(Framebuffer framebuffer) {
        return createFrameBuffer(framebuffer, false);
    }

    public static Framebuffer createFrameBuffer(Framebuffer framebuffer, boolean depth) {
    	if (framebuffer == null)
            return new Framebuffer(sr.getFramebufferWidth(), sr.getFramebufferHeight(), depth);
    	
        if (needsNewFramebuffer(framebuffer)) {
            framebuffer.resize(sr.getFramebufferWidth(), sr.getFramebufferHeight(), depth);
        }
        return framebuffer;
    }

    public static boolean needsNewFramebuffer(Framebuffer framebuffer) {
        return framebuffer.framebufferWidth != sr.getFramebufferWidth() || framebuffer.framebufferHeight != sr.getFramebufferHeight();
    }

    
}
