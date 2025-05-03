package fun.rockstarity.api.helpers.render.gif;

import static org.lwjgl.opengl.GL11.GL_QUADS;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.rect.Rect;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class GifRender implements IAccess {
    private final List<Integer> frames = new ArrayList<>();

    public GifRender(ResourceLocation resourceLocation) {
        try (InputStream inputStream = mc.getResourceManager().getResource(resourceLocation).getInputStream()) {
            GifImage gifImage = new GifImage();
            gifImage.loadFrom(inputStream);

            for (BufferedImage frame : gifImage.getFrames()) {
                frames.add(loadTexture(frame));
            }
        } catch (IOException e) {
        	e.printStackTrace();
            throw new RuntimeException("Failed to load GIF from resource: " + resourceLocation, e);
        }
    }
    
    public GifRender(File file) {
    	GifImage gifImage = new GifImage();
        try {
			gifImage.loadFrom(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

        for (BufferedImage frame : gifImage.getFrames()) {
            frames.add(loadTexture(frame));
        }
    }
    
    public GifRender(InputStream inputStream) {
    	GifImage gifImage = new GifImage();
        gifImage.loadFrom(inputStream);

        for (BufferedImage frame : gifImage.getFrames()) {
            frames.add(loadTexture(frame));
        }
    }
    
    public void draw(MatrixStack stack, float x, float y, float width, float height, float alpha, double speed) {
    	this.draw(stack, x, y, width, height, alpha, speed, false);
    }

    public void draw(MatrixStack stack, float x, float y, float width, float height, float alpha, double speed, boolean bloom) {
        bindTexture(speed);

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        if (bloom) GlStateManager.blendFunc(770, 1);
        else RenderSystem.defaultBlendFunc();
        GlStateManager.disableAlphaTest();

        //GL11.glTexParameteri(3553, 10240, 9729);
       // GL11.glTexParameteri(3553, 10241, 9729);
        
        FixColor color = FixColor.WHITE.alpha(alpha);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
        buffer.pos(stack.getLast().getMatrix(), x, y, 0)
                .color(color)
                .tex(0, 0)
                .endVertex();
        buffer.pos(stack.getLast().getMatrix(), x, y + height, 0)
                .color(color)
                .tex(0, 1)
                .endVertex();
        buffer.pos(stack.getLast().getMatrix(), x + width, y + height, 0)
                .color(color)
                .tex(1, 1)
                .endVertex();
        buffer.pos(stack.getLast().getMatrix(), x + width, y, 0)
                .color(color)
                .tex(1, 0)
                .endVertex();
        Tessellator.getInstance().draw();

        GlStateManager.enableAlphaTest();
        GlStateManager.popMatrix();
    }
    
    public void drawRound(MatrixStack stack, float x, float y, float width, float height, float round, float alpha, double speed) {
        bindTexture(speed);

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        RenderSystem.defaultBlendFunc();
        GlStateManager.disableAlphaTest();

        //GL11.glTexParameteri(3553, 10240, 9729);
       // GL11.glTexParameteri(3553, 10241, 9729);
        
        Round.drawTextured(stack, new Rect(x,y,width,height), round, alpha);

        GlStateManager.enableAlphaTest();
        GlStateManager.popMatrix();
    }

    public int loadTexture(BufferedImage image) {
        try {
        	int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
            ByteBuffer buffer = ByteBuffer.allocateDirect(pixels.length * 4);

            for (int pixel : pixels) {
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
            buffer.flip();

            int textureID = GlStateManager.genTexture();
            GlStateManager.bindTexture(textureID);
           // GL11.glTexParameteri(3553, 10240, 9729);
           // GL11.glTexParameteri(3553, 10241, 9729);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL20.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL20.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, image.getWidth(), image.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
           // GL11.glTexParameteri(3553, 10240, 9729);
            //GL11.glTexParameteri(3553, 10241, 9729);
            GlStateManager.bindTexture(0);
            
            return textureID;
		} catch (Exception e) {
			return 0;
		}
    }
    
    public void bindTexture(double speed) {
        int frameIndex = (int) (System.currentTimeMillis() / speed % frames.size());
        int textureID = frames.get(frameIndex);
        GlStateManager.bindTexture(textureID);
    }
}
