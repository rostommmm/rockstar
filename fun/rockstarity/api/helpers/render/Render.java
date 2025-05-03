package fun.rockstarity.api.helpers.render;

import static org.lwjgl.opengl.GL11.GL_GREATER;
import static org.lwjgl.opengl.GL11.glScissor;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.Reacher;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.render.shaders.fog.Vector2d;
import fun.rockstarity.api.render.shaders.list.Glow;
import fun.rockstarity.api.render.shaders.list.Outline;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.api.secure.nativeapi.FastNative;
import fun.rockstarity.client.modules.render.Beautifully;
import fun.rockstarity.client.modules.render.GlowESP;
import fun.rockstarity.client.modules.render.Interface;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.optifine.Config;
import net.optifine.shaders.Shaders;

@UtilityClass
public class Render implements IAccess {
	
	public void glow(MatrixStack ms, Rect rect, float alpha) {
		glow(ms, rect, alpha, true);
	}
	
	public void glow(MatrixStack ms, Rect rect, float alpha, boolean stencil) {
		if (!Interface.glow()) return;
		
		Interface ui = rock.getModules().get(Interface.class);
		
		float offset = ui.getOffset().get();
		
		FixColor[] circle = Interface.getCircle(alpha);
		
		if (stencil) {
			Stencil.init();
			Round.draw(ms, rect, 4, FixColor.WHITE);
			Stencil.read(0);
		}
		
		Glow.draw(ms, rect.size(-1), offset, ui.getAlpha().get(), offset, circle[0], circle[1], circle[2], circle[3]);

		if (stencil) {
			Stencil.finish();
		}
	}
	
	public void outline(MatrixStack ms, Rect rect, float alpha) {
		if (!Interface.outline()) return;
		Interface ui = rock.getModules().get(Interface.class);

		FixColor[] circle = Interface.getCircle(alpha);
		Outline.draw(ms, rect.y(rect.getY()).size(-0.25f), 4, ui.getOutlineWidth().get()/4F, circle[0], circle[1], circle[2], circle[3]);
	}
	
	public void grid(MatrixStack matrixStack, FixColor color, float step) {
		step = Math.max(1, step);
		
		for (int x = 0; x < sr.getScaledWidth(); x += step) {
			Round.draw(matrixStack, new Rect(x, 0, 1, sr.getScaledHeight()), 0, color);
		}
		for (int y = 0; y < sr.getScaledWidth(); y += step) {
			Round.draw(matrixStack, new Rect(0, y, sr.getScaledWidth(), 1), 0, color);
		}
	}
	
	public void gridMask(MatrixStack matrixStack, FixColor color, Rect rect, float offset) {
		FixColor c = color;
		
		Stencil.init();
		Render.grid(matrixStack, FixColor.WHITE, sr.getScaledWidth()/15);
		Stencil.read(1);
		Glow.draw(matrixStack, rect.size(-1), offset, 0.2f, 10 + offset, c, c, c, c);
		Stencil.finish();
	}
	
	public void gridMask(MatrixStack matrixStack, FixColor color, Rect rect) {
		gridMask(matrixStack, color, rect, 75);
	}
	
	public Vector3d cameraPos() {
        return mc.gameRenderer.getActiveRenderInfo().getProjectedView();
    }
	
	public void drawItem(ItemStack itemStack, float x, float y, float alpha) {
		Reacher.ITEMS_ALPHA = alpha;
		mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, (int) x, (int) y);
		mc.getItemRenderer().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, (int) x, (int) y, null);
		Reacher.ITEMS_ALPHA = 1;
	}

    public void setupOrientationMatrix(MatrixStack matrix, double x, double y, double z) {
        matrix.translate(x - cameraPos().x, y - cameraPos().y, z - cameraPos().z);
    }

    public void rotateToCamera(MatrixStack matrix) {
        matrix.rotate(mc.getRenderManager().getCameraOrientation());
    }

	// Ротейт
    public void initRotate(float x, float y, float value) {
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0);
        GL11.glRotatef(value, 0, 0, 1);
        GL11.glTranslatef(-x, -y, 0);
    }
    
    public void endRotate() {
    	GL11.glPopMatrix();
    }
    
    public void setColor(final int color) {
    	GL11.glColor4ub((byte)(color >> 16 & 0xFF), (byte)(color >> 8 & 0xFF), (byte)(color & 0xFF), (byte)(color >> 24 & 0xFF));
    }
	
	public void color(FixColor color) {
		float r = color.getRed() / 255.0F;
	    float g = color.getGreen() / 255.0F;
	    float b = color.getBlue() / 255.0F;
	    float a = color.getAlpha() / 255.0F;
	    GlStateManager.color4f(r, g, b, a);
	}
	
    public void color(int color, float alpha) {
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        GlStateManager.color4f(r, g, b, alpha);
    }
    
	public void drawStack(ItemStack itemStack, float x, float y, float size) {
		mc.getItemRenderer().zLevel = -900;
        GlStateManager.pushMatrix();
        GlStateManager.disableBlend();
        mc.getTextureManager().bindTexture(ContainerScreen.INVENTORY_BACKGROUND);
        GlStateManager.translated(x, y, 0);
        GlStateManager.scalef(size, size, size); // Устанавливаем размер иконки
        mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, 0, 0); // Рендерим иконку
        mc.getItemRenderer().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, 0, 0, itemStack.getCount() > 1 ? itemStack.getCount() + "" : "");
        GlStateManager.popMatrix();
	}
	
	public void drawStackOld(ItemStack itemStack, float x, float y, float size) {
        GlStateManager.pushMatrix();
        GlStateManager.disableBlend();
        mc.getTextureManager().bindTexture(ContainerScreen.INVENTORY_BACKGROUND);
        GlStateManager.translated(x, y, 0);
        GlStateManager.scalef(size, size, size); // Устанавливаем размер иконки
        mc.getItemRenderer().renderItemAndEffectIntoGUIOld(itemStack, 0, 0); // Рендерим иконку
        mc.getItemRenderer().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, 0, 0, itemStack.getCount() > 1 ? itemStack.getCount() + "" : "");
        GlStateManager.popMatrix();
	}
	
	public void drawItem(Item item, float x, float y) {
		drawStack(new ItemStack(item), x, y);
	}
	
	public void drawItem(Item item, float x, float y, float size) {
		ItemStack itemStack = new ItemStack(item);
		
		GlStateManager.pushMatrix();
        GlStateManager.disableBlend();
        mc.getTextureManager().bindTexture(ContainerScreen.INVENTORY_BACKGROUND);
        GlStateManager.translated(x, y, 0);
        GlStateManager.scalef(size, size, size); // Устанавливаем размер иконки
        mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, 0, 0); // Рендерим иконку
        GlStateManager.popMatrix();
	}
	
    public void drawStack(ItemStack stack, float x, float y) {
        GL11.glPushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.translated(x, y, 0);
        GlStateManager.scaled(0.6, 0.6, 0.6);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        mc.getItemRenderer().renderItemIntoGUI(stack, 0, 0);
        mc.getItemRenderer().renderItemOverlayIntoGUI(mc.fontRenderer, stack, 0, 0,
                stack.getCount() > 1 ? stack.getCount() + "" : "");
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GL11.glPopMatrix();
        GL11.glDisable(GL11.GL_DEPTH_TEST);
	}
    
    private Vector2d calculateScreenPositiond(Vector3f result3f, double fov) {
        float halfHeight = mc.getMainWindow().getScaledHeight() / 2.0F;
        float scaleFactor = halfHeight / (result3f.getZ() * (float) Math.tan(Math.toRadians(fov / 2.0F)));
        Beautifully aspect = rock.getModules().get(Beautifully.class);
        if (result3f.getZ() < 0.0F) {
            return new Vector2d(-result3f.getX() * scaleFactor / (aspect.get() ? aspect.getAspectRatio().get() : 1) + mc.getMainWindow().getScaledWidth() / 2.0F, mc.getMainWindow().getScaledHeight() / 2.0F - result3f.getY() * scaleFactor);
        }
        return new Vector2d(Float.MAX_VALUE, Float.MAX_VALUE);
    }
    
    private Vector2f calculateScreenPosition(Vector3f result3f, double fov) {
        float halfHeight = mc.getMainWindow().getScaledHeight() / 2.0F;
        float scaleFactor = halfHeight / (result3f.getZ() * (float) Math.tan(Math.toRadians(fov / 2.0F)));
        Beautifully aspect = rock.getModules().get(Beautifully.class);
        if (result3f.getZ() < 0.0F) {
            return new Vector2f(-result3f.getX() * scaleFactor / (aspect.get() ? aspect.getAspectRatio().get() : 1) + mc.getMainWindow().getScaledWidth() / 2.0F, mc.getMainWindow().getScaledHeight() / 2.0F - result3f.getY() * scaleFactor);
        }
        return new Vector2f(Float.MAX_VALUE, Float.MAX_VALUE);
    }
    
    public double[] worldToScreen(double x, double y, double z) {
        Vector3d camera_pos = mc.getRenderManager().info.getProjectedView();
        Beautifully aspect = rock.getModules().get(Beautifully.class);
        Quaternion camera_rotation_conj = Minecraft.getInstance().getRenderManager().getCameraOrientation().copy();
        camera_rotation_conj.conjugate();
        Vector3f result3f = new Vector3f((float)(camera_pos.x - x), (float)(camera_pos.y - y), (float)(camera_pos.z - z));
        result3f.transform(camera_rotation_conj);

        if (mc.getGameSettings().viewBobbing) {
            Entity renderViewEntity = mc.getRenderViewEntity();
            if (renderViewEntity instanceof PlayerEntity) {
                PlayerEntity playerentity = (PlayerEntity) renderViewEntity;
                float distwalked_modified = playerentity.distanceWalkedModified;

                float f = distwalked_modified - playerentity.prevDistanceWalkedModified;
                float f1 = -(distwalked_modified + f * mc.getRenderPartialTicks());
                float f2 = MathHelper.lerp(mc.getRenderPartialTicks(), playerentity.prevCameraYaw, playerentity.cameraYaw);
                Quaternion q2 = new Quaternion(Vector3f.XP, Math.abs(MathHelper.cos(f1 * (float) Math.PI - 0.2F) * f2) * 5.0F, true);
                q2.conjugate();
                result3f.transform(q2);

                Quaternion q1 = new Quaternion(Vector3f.ZP, MathHelper.sin(f1 * (float) Math.PI) * f2 * 3.0F, true);
                q1.conjugate();
                result3f.transform(q1);

                Vector3f bob_translation = new Vector3f((MathHelper.sin(f1 * (float) Math.PI) * f2 * 0.5F), (-Math.abs(MathHelper.cos(f1 * (float) Math.PI) * f2)), 0.0f);
                bob_translation.y = (-bob_translation.getY());
                result3f.add(bob_translation);
            }
        }

        double fov = (float) mc.gameRenderer.getFOVModifier(mc.getRenderManager().info, mc.getRenderPartialTicks(), true);

        float half_height = (float)Minecraft.getInstance().getMainWindow().getScaledHeight() / 2.0F;
        float scale_factor = half_height / (result3f.getZ() * (float)Math.tan(Math.toRadians((double)(fov / 2.0F))));
        
        
        return new double[]{(double)(-result3f.getX() * scale_factor / (aspect.get() ? aspect.getAspectRatio().get() : 1) + (float)(Minecraft.getInstance().getMainWindow().getScaledWidth() / 2)), (double)((float)(Minecraft.getInstance().getMainWindow().getScaledHeight() / 2) - result3f.getY() * scale_factor)};
    }
    
    public Vector2d project(double x, double y, double z) {
        Vector3d camera_pos = mc.getRenderManager().info.getProjectedView();
        Quaternion cameraRotation = mc.getRenderManager().getCameraOrientation().copy();
        cameraRotation.conjugate();

        Vector3f result3f = new Vector3f((float) (camera_pos.x - x), (float) (camera_pos.y - y), (float) (camera_pos.z - z));
        result3f.transform(cameraRotation);

        if (mc.getGameSettings().viewBobbing) {
            Entity renderViewEntity = mc.getRenderViewEntity();
            if (renderViewEntity instanceof PlayerEntity playerentity) {
                calculateViewBobbing(playerentity, result3f);
            }
        }

        double fov = mc.gameRenderer.getFOVModifier(mc.getRenderManager().info, mc.getRenderPartialTicks(), true);

        return calculateScreenPositiond(result3f, fov);
    }
    
    public Vector2f projectf(double x, double y, double z) {
        Vector3d camera_pos = mc.getRenderManager().info.getProjectedView();
        Quaternion cameraRotation = mc.getRenderManager().getCameraOrientation().copy();
        cameraRotation.conjugate();

        Vector3f result3f = new Vector3f((float) (camera_pos.x - x), (float) (camera_pos.y - y), (float) (camera_pos.z - z));
        result3f.transform(cameraRotation);

        if (mc.getGameSettings().viewBobbing) {
            Entity renderViewEntity = mc.getRenderViewEntity();
            if (renderViewEntity instanceof PlayerEntity playerentity) {
                calculateViewBobbing(playerentity, result3f);
            }
        }

        double fov = mc.gameRenderer.getFOVModifier(mc.getRenderManager().info, mc.getRenderPartialTicks(), true);

        return calculateScreenPosition(result3f, fov);
    }

    private void calculateViewBobbing(PlayerEntity playerentity, Vector3f result3f) {
        float walked = playerentity.distanceWalkedModified;
        float f = walked - playerentity.prevDistanceWalkedModified;
        float f1 = -(walked + f * mc.getRenderPartialTicks());
        float f2 = MathHelper.lerp(mc.getRenderPartialTicks(), playerentity.prevCameraYaw, playerentity.cameraYaw);

        Quaternion quaternion = new Quaternion(Vector3f.XP, Math.abs(MathHelper.cos(f1 * (float) Math.PI - 0.2F) * f2) * 5.0F, true);
        quaternion.conjugate();
        result3f.transform(quaternion);

        Quaternion quaternion1 = new Quaternion(Vector3f.ZP, MathHelper.sin(f1 * (float) Math.PI) * f2 * 3.0F, true);
        quaternion1.conjugate();
        result3f.transform(quaternion1);

        Vector3f bobTranslation = new Vector3f((MathHelper.sin(f1 * (float) Math.PI) * f2 * 0.5F), (-Math.abs(MathHelper.cos(f1 * (float) Math.PI) * f2)), 0.0f);
        bobTranslation.setY(-bobTranslation.getY());
        result3f.add(bobTranslation);
    }
    
    public void drawCircle(final float x, final float y, float start, float end, final float radius, final float width, final boolean filled, final int color) {
    	GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.0f);
    	
    	if (start > end) {
    		final float endOffset = end;
    		end = start;
    		start = endOffset;
    	}
    	GlStateManager.enableBlend();
    	GlStateManager.disableTexture();
    	GlStateManager.glBlendFuncSeparate(770, 771, 1, 0);
    	color(color);
    	GL11.glEnable(2848);
    	GL11.glLineWidth(width);
    	GL11.glBegin(3);
    	for (float i = end; i >= start; i -= 4.0f) {
    		final float cos = (float)(Math.cos(i * 3.141592653589793 / 180.0) * radius * 1.0);
    		final float sin = (float)(Math.sin(i * 3.141592653589793 / 180.0) * radius * 1.0);
    		GL11.glVertex2f(x + cos, y + sin);
    	}
    	GL11.glEnd();
    	GL11.glDisable(2848);
    	GlStateManager.enableTexture();
    	GlStateManager.disableBlend();
    }
    
    public void drawCircle(final int color, final float x, final float y, final float radius, final int start, final int end) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture();
        GlStateManager.blendFuncSeparate(770, 771, 1, 0);
        color(color);
        GL11.glEnable(2848);
        GL11.glLineWidth(2.0f);
        GL11.glBegin(3);
        for (float i = (float)end; i >= start; i -= 4.0f) {
            GL11.glVertex2f((float)(x + Math.cos(i * 3.141592653589793 / 180.0) * (radius * 1.001f)), (float)(y + Math.sin(i * 3.141592653589793 / 180.0) * (radius * 1.001f)));
        }
        GL11.glEnd();
        GL11.glDisable(2848);
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
    }
    
    public void drawCircle(final int color, final float x, final float y, final float radius, final float width, final int start, final int end) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture();
        GlStateManager.blendFuncSeparate(770, 771, 1, 0);
        color(color);
        GL11.glEnable(2848);
        GL11.glLineWidth(width);
        GL11.glBegin(3);
        for (float i = (float)end; i >= start; i -= 4.0f) {
            GL11.glVertex2f((float)(x + Math.cos(i * 3.141592653589793 / 180.0) * (radius * 1.001f)), (float)(y + Math.sin(i * 3.141592653589793 / 180.0) * (radius * 1.001f)));
        }
        GL11.glEnd();
        GL11.glDisable(2848);
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
    }
    
    public void drawTriangle(float x, float y, float size, float vector, int color) {
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture();
        GlStateManager.enableBlend();
        GlStateManager.disableAlphaTest();
        GlStateManager.glBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA.param, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.param, GlStateManager.SourceFactor.ONE.param, GlStateManager.DestFactor.ZERO.param);
        GlStateManager.shadeModel(7425);
        GL11.glTranslated(x, y, 0.0D);
        GL11.glRotatef(vector, 0.0F, 0.0F, 1.0F);
        float alpha = (color >> 24 & 0xFF) / 255.0F;
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        GlStateManager.color4f(red, green, blue, alpha);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(1.0F);
        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glVertex2d(0.0D, size);
        GL11.glVertex2d((1.0F * size), -size);
        GL11.glVertex2d(-(1.0F * size), -size);
        GL11.glEnd();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glRotatef(-vector, 0.0F, 0.0F, 1.0F);
        GL11.glTranslated(-x, -y, 0.0D);
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlphaTest();
        GlStateManager.enableTexture();
        GlStateManager.popMatrix();
    }

    public void drawLine(float startX, float startY, float endX, float endY, int segments) {
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

        GL11.glColor3f(1.0f, 1.0f, 1.0f);

        GL11.glLineWidth(4);

        float dx = (endX - startX) / segments;
        float dy = (endY - startY) / segments;

        GL11.glBegin(GL11.GL_LINE_STRIP);

        for (int i = 0; i <= segments; i++) {
            float x = startX + i * dx;
            float y = startY + i * dy;

            GL11.glVertex2f(x, y);
        }

        GL11.glEnd();
    }
    
    public int loadTexture(BufferedImage image) {
		int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        ByteBuffer buffer = BufferUtils.createByteBuffer(pixels.length * 4);
        
        try {
	        for (int pixel : pixels) {
	            buffer.put((byte)((pixel >> 16) & 0xFF));
	            buffer.put((byte)((pixel >> 8) & 0xFF));
	            buffer.put((byte)(pixel & 0xFF));
	            buffer.put((byte)((pixel >> 24) & 0xFF));
	        }
	        buffer.flip();
        } catch (BufferOverflowException | ReadOnlyBufferException ex) {return -1;}
        
		int textureID = GlStateManager.genTexture();
		GlStateManager.bindTexture(textureID);
		GlStateManager.texParameter(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR);
		GlStateManager.texParameter(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_LINEAR);
		GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, buffer);
		GlStateManager.bindTexture(0);
		
		return textureID;
	}
    
    public void color(int color) {
        color(color, (float) (color >> 24 & 255) / 255.0F);
    }
    
    public void resetColor() {
        GlStateManager.color4f(1, 1, 1, 1);
    }

    public void setAlphaLimit(float limit) {
        GlStateManager.enableAlphaTest();
        GlStateManager.alphaFunc(GL_GREATER, (float) (limit * .01));
    }
    
    public void drawModalRectWithCustomSizedTexture(float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight) {
    	float f = 1.0F / textureWidth;
        float f1 = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldrenderer = tessellator.getBuffer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos((double) x, (double) (y + height), 0.0D).tex((float) (u * f), (float) ((v + (float) height-0.5f) * f1)).tex(0, 1 - 0.01f).endVertex();
        worldrenderer.pos((double) (x + width), (double) (y + height), 0.0D).tex((float) ((u + (float) width) * f), (float) ((v + (float) height-0.5f) * f1)).tex(1, 1 - 0.01f).endVertex();
        worldrenderer.pos((double) (x + width), (double) y, 0.0D).tex((float) ((u + (float) width) * f), (float) (v * f1)).tex(1, 0).endVertex();
        worldrenderer.pos((double) x, (double) y, 0.0D).tex((float) (u * f), (float) (v * f1)).tex(0, 0).endVertex();

        tessellator.draw();
    }
    public static void drawScaledCustomSizeModalRect(float x, float y, float u, float v, float uWidth, float vHeight, float width, float height, float tileWidth, float tileHeight) {
        float f = 1.0F / tileWidth;
        float f1 = 1.0F / tileHeight;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldrenderer = tessellator.getBuffer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0.0D).tex(u * f, (v + vHeight) * f1).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0D).tex((u + uWidth) * f, (v + vHeight) * f1).endVertex();
        worldrenderer.pos(x + width, y, 0.0D).tex((u + uWidth) * f, v * f1).endVertex();
        worldrenderer.pos(x, y, 0.0D).tex(u * f, v * f1).endVertex();
        tessellator.draw();
    }

    public void bindTexture(int texture) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
    }
    
    public void scissor(double x, double y, double width, double height) {
        MainWindow sr = Minecraft.getInstance().getMainWindow();
        final double scale = sr.getGuiScaleFactor();
        double finalHeight = height * scale;
        double finalY = (sr.getScaledHeight() - y) * scale;
        double finalX = x * scale;
        double finalWidth = width * scale;
        glScissor((int) finalX, (int) (finalY - finalHeight), (int) finalWidth, (int) finalHeight);
    }
    
    public void drawCircle(final float x, final float y, final float radius, final int start, final int end, FixColor color) {
        drawCircle(x, y, radius, start, end, 1.5f, color);
    }
    
    public void drawCircle(final float x, final float y, final float radius, final int start, final int end, float width, FixColor color) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture();
        GlStateManager.blendFuncSeparate(770, 771, 1, 0);
        GL11.glEnable(2848);
        GL11.glLineWidth(width);
        GL11.glBegin(3);
        for (float i = (float)end; i >= start; i -= 4.0f) {
        	color(color.getRGB());
            GL11.glVertex2f((float)(x + Math.cos(i * 3.141592653589793 / 180.0) * (radius * 1.001f)), (float)(y + Math.sin(i * 3.141592653589793 / 180.0) * (radius * 1.001f)));
        }
        GL11.glEnd();
        GL11.glDisable(2848);
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
    }

    public void drawUICircle(final float x, final float y, final float radius, final int start, final int end, float alpha) {
        drawClientCircle(x, y, radius, start, end, 1.5f, alpha);
    }

    public void drawUICircle(final float x, final float y, final float radius, final int start, final int end, float width, float alpha) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture();
        GlStateManager.blendFuncSeparate(770, 771, 1, 0);
        GL11.glEnable(2848);
        GL11.glLineWidth(width);
        GL11.glBegin(3);
        for (float i = (float) end; i >= start; i -= 4.0f) {
            color(Interface.getPoint((int) i, alpha).getRGB());
            GL11.glVertex2f((float) (x + Math.cos(i * 3.141592653589793 / 180.0) * (radius * 1.001f)),
                    (float) (y + Math.sin(i * 3.141592653589793 / 180.0) * (radius * 1.001f)));
        }
        GL11.glEnd();
        GL11.glDisable(2848);
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
    }
    
    public void drawClientCircle(final float x, final float y, final float radius, final int start, final int end, float alpha) {
        drawClientCircle(x, y, radius, start, end, 3.5f, alpha);
    }

    public void drawClientCircle(final float x, final float y, final float radius, final int start, final int end, float width, float alpha) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture();
        GlStateManager.blendFuncSeparate(770, 771, 1, 0);
        GL11.glEnable(2848);
        GL11.glLineWidth(width);
        GL11.glBegin(3);
        for (float i = (float) end; i >= start; i -= 4.0f) {
            color(Style.getPoint((int) i).getRGB(), alpha);
            GL11.glVertex2f((float) (x + Math.cos(i * 3.141592653589793 / 180.0) * (radius * 1.001f)),
                    (float) (y + Math.sin(i * 3.141592653589793 / 180.0) * (radius * 1.001f)));
        }
        GL11.glEnd();
        GL11.glDisable(2848);
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
    }
    
    public void drawClientCircle(final float x, final float y, final float radius, final int start, final int end) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture();
        GlStateManager.blendFuncSeparate(770, 771, 1, 0);
        GL11.glEnable(2848);
        GL11.glLineWidth(2.0f);
        GL11.glBegin(3);
        for (float i = (float)end; i >= start; i -= 4.0f) {
        	color(Style.getMain());
            GL11.glVertex2f((float)(x + Math.cos(i * 3.141592653589793 / 180.0) * (radius * 1.001f)), (float)(y + Math.sin(i * 3.141592653589793 / 180.0) * (radius * 1.001f)));
        }
        GL11.glEnd();
        GL11.glDisable(2848);
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
    }
    
    public Rect image(String name, float x, float y, float width, float height, Color color) {
    	GL11.glPushMatrix();
    	GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(770, 771);
        GlStateManager.color4f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
        try {
        	 mc.getTextureManager().bindTexture(FastNative.getImageResource(name));
        } catch (Exception e) {
        //	e.printStackTrace();
        }
        GL11.glTexParameteri(3553, 10240, 9729);
        GL11.glTexParameteri(3553, 10241, 9729);
        Render.drawModalRectWithCustomSizedTexture(x,y,0,0,width,height,width,height);
        GL11.glPopMatrix();
        
        return new Rect(x,y,width,height);
    }
    
    public void image(MatrixStack stack, String name, double x, double y, double z, double width, double height, FixColor color1, FixColor color2, FixColor color3, FixColor color4) {
    	GlStateManager.enableBlend();
        GL11.glBlendFunc(770, 771);
        mc.getTextureManager().bindTexture(FastNative.getImageResource(name));
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);

        Matrix4f matrix = stack.getLast().getMatrix();
        int color1RGB = color1.getRGB();
        int color2RGB = color2.getRGB();
        int color3RGB = color3.getRGB();
        int color4RGB = color4.getRGB();
        GL11.glTexParameteri(3553, 10240, 9729);
        GL11.glTexParameteri(3553, 10241, 9729);
        BUILDER.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP);
        BUILDER.pos(matrix, (float) x, (float) (y + height), (float) z).color((color1RGB >> 16) & 0xFF, (color1RGB >> 8) & 0xFF, color1RGB & 0xFF, color1.getAlpha()).tex(0, 1 - 0.01f).lightmap(0, 240).endVertex();
        BUILDER.pos(matrix, (float) (x + width), (float) (y + height), (float) z).color((color2RGB >> 16) & 0xFF, (color2RGB >> 8) & 0xFF, color2RGB & 0xFF, color2.getAlpha()).tex(1, 1 - 0.01f).lightmap(0, 240).endVertex();
        BUILDER.pos(matrix, (float) (x + width), (float) y, (float) z).color((color3RGB >> 16) & 0xFF, (color3RGB >> 8) & 0xFF, color3RGB & 0xFF, color3.getAlpha()).tex(1, 0).lightmap(0, 240).endVertex();
        BUILDER.pos(matrix, (float) x, (float) y, (float) z).color((color4RGB >> 16) & 0xFF, (color4RGB >> 8) & 0xFF, color4RGB & 0xFF, color4.getAlpha()).tex(0, 0).lightmap(0, 240).endVertex();

        TESSELLATOR.draw();
        GlStateManager.disableBlend();
    }
    
    public void image(ResourceLocation image, float x, float y, float width, float height, Color color) {
    	GL11.glPushMatrix();
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glDepthMask(false);
        GL11.glBlendFunc(770, 771);
        GlStateManager.color4f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
        Minecraft.getInstance().getTextureManager().bindTexture(image);
        GL11.glTexParameteri(3553, 10240, 9729);
        GL11.glTexParameteri(3553, 10241, 9729);
        Render.drawModalRectWithCustomSizedTexture(x,y,0,0,width,height,width,height);
        GL11.glPopMatrix();
    }
    
    public void drawImage(MatrixStack stack, String name, double x, double y, double z, double width, double height, FixColor color) {
    	drawImage(stack, name, x, y, z, width, height, color, color, color, color);
    }
    
    public void drawImage(MatrixStack stack, String name, double x, double y, double z, double width, double height, FixColor color1, FixColor color2, FixColor color3, FixColor color4) {
    	GlStateManager.enableBlend();
    	GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
    	mc.getTextureManager().bindTexture(FastNative.getImageResource(name));
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);

        Matrix4f matrix = stack.getLast().getMatrix();
        int color1RGB = color1.getRGB();
        int color2RGB = color2.getRGB();
        int color3RGB = color3.getRGB();
        int color4RGB = color4.getRGB();
        
        BUILDER.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP);
        BUILDER.pos(matrix, (float) x, (float) (y + height), (float) z).color((color1RGB >> 16) & 0xFF, (color1RGB >> 8) & 0xFF, color1RGB & 0xFF, color1.getAlpha()).tex(0, 1 - 0.01f).lightmap(0, 240).endVertex();
        BUILDER.pos(matrix, (float) (x + width), (float) (y + height), (float) z).color((color2RGB >> 16) & 0xFF, (color2RGB >> 8) & 0xFF, color2RGB & 0xFF, color2.getAlpha()).tex(1, 1 - 0.01f).lightmap(0, 240).endVertex();
        BUILDER.pos(matrix, (float) (x + width), (float) y, (float) z).color((color3RGB >> 16) & 0xFF, (color3RGB >> 8) & 0xFF, color3RGB & 0xFF, color3.getAlpha()).tex(1, 0).lightmap(0, 240).endVertex();
        BUILDER.pos(matrix, (float) x, (float) y, (float) z).color((color4RGB >> 16) & 0xFF, (color4RGB >> 8) & 0xFF, color4RGB & 0xFF, color4.getAlpha()).tex(0, 0).lightmap(0, 240).endVertex();

        TESSELLATOR.draw();
        GlStateManager.disableBlend();
    }
    
    public void startFlatRender() {
    	GlStateManager.depthMask(false);
    	GlStateManager.enableBlend();
    	GlStateManager.enableTexture();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0);
        
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
    	BUILDER.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP);
    }

    public void flatImage(MatrixStack stack, String name, double x, double y, double z, double width, double height, FixColor color) {
    	flatImage(stack, name, x, y, z, width, height, color, color, color, color);
    }
    
    public void flatImage(MatrixStack stack, String name, double x, double y, double z, double width, double height, FixColor color1, FixColor color2, FixColor color3, FixColor color4) {
        Matrix4f matrix = stack.getLast().getMatrix();
        int color1RGB = color1.getRGB();
        int color2RGB = color2.getRGB();
        int color3RGB = color3.getRGB();
        int color4RGB = color4.getRGB();
        
        mc.getTextureManager().bindTextureFlat(FastNative.getImageResource(name));
        
        BUILDER.pos(matrix, (float) x, (float) (y + height), (float) z).color((color1RGB >> 16) & 0xFF, (color1RGB >> 8) & 0xFF, color1RGB & 0xFF, color1.getAlpha()).tex(0, 1 - 0.01f).lightmap(0, 240).endVertex();
        BUILDER.pos(matrix, (float) (x + width), (float) (y + height), (float) z).color((color2RGB >> 16) & 0xFF, (color2RGB >> 8) & 0xFF, color2RGB & 0xFF, color2.getAlpha()).tex(1, 1 - 0.01f).lightmap(0, 240).endVertex();
        BUILDER.pos(matrix, (float) (x + width), (float) y, (float) z).color((color3RGB >> 16) & 0xFF, (color3RGB >> 8) & 0xFF, color3RGB & 0xFF, color3.getAlpha()).tex(1, 0).lightmap(0, 240).endVertex();
        BUILDER.pos(matrix, (float) x, (float) y, (float) z).color((color4RGB >> 16) & 0xFF, (color4RGB >> 8) & 0xFF, color4RGB & 0xFF, color4.getAlpha()).tex(0, 0).lightmap(0, 240).endVertex();
    }
    
    public void endFlatRender() {
    	TESSELLATOR.draw();
    	GlStateManager.disableBlend();
    	GlStateManager.depthMask(true);
    }
    
    public void startImageRendering(String name) {
    	GlStateManager.depthMask(false);
    	GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0);
        
        mc.getTextureManager().bindTexture(FastNative.getImageResource(name));
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
    }
    
    public void drawCleanImage(MatrixStack stack, double x, double y, double z, double width, double height, FixColor color) {
    	drawCleanImage(stack, x, y, z, width, height, color, color, color, color);
    }
    
    public void drawCleanImage(MatrixStack stack, double x, double y, double z, double width, double height, FixColor color1, FixColor color2, FixColor color3, FixColor color4) {
        Matrix4f matrix = stack.getLast().getMatrix();
        int color1RGB = color1.getRGB();
        int color2RGB = color2.getRGB();
        int color3RGB = color3.getRGB();
        int color4RGB = color4.getRGB();

        BUILDER.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP);
        BUILDER.pos(matrix, (float) x, (float) (y + height), (float) z).color((color1RGB >> 16) & 0xFF, (color1RGB >> 8) & 0xFF, color1RGB & 0xFF, color1.getAlpha()).tex(0, 1 - 0.01f).lightmap(0, 240).endVertex();
        BUILDER.pos(matrix, (float) (x + width), (float) (y + height), (float) z).color((color2RGB >> 16) & 0xFF, (color2RGB >> 8) & 0xFF, color2RGB & 0xFF, color2.getAlpha()).tex(1, 1 - 0.01f).lightmap(0, 240).endVertex();
        BUILDER.pos(matrix, (float) (x + width), (float) y, (float) z).color((color3RGB >> 16) & 0xFF, (color3RGB >> 8) & 0xFF, color3RGB & 0xFF, color3.getAlpha()).tex(1, 0).lightmap(0, 240).endVertex();
        BUILDER.pos(matrix, (float) x, (float) y, (float) z).color((color4RGB >> 16) & 0xFF, (color4RGB >> 8) & 0xFF, color4RGB & 0xFF, color4.getAlpha()).tex(0, 0).lightmap(0, 240).endVertex();

        TESSELLATOR.draw();
    }
    
    public void finishImageRendering() {
    	GlStateManager.disableBlend();
    	GlStateManager.depthMask(true);
    }
    
    public void enableSmoothLine(final float width) {
    	 GL11.glDisable(3008);
         GL11.glEnable(3042);
         GL11.glBlendFunc(770, 771);
         GL11.glDisable(3553);
         GL11.glDisable(2929);
         GL11.glDepthMask(false);
         GL11.glEnable(2884);
         GL11.glEnable(2848);
         GL11.glHint(3154, 4354);
         GL11.glHint(3155, 4354);
         GL11.glLineWidth(width);
    }
    
    public void disableSmoothLine() {
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDisable(3042);
        GL11.glEnable(3008);
        GL11.glDepthMask(true);
        GL11.glCullFace(1029);
        GL11.glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glHint(3155, 4352);
    }
    
    public void scale(float x, float y, float scale) {
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0);
        GL11.glScalef(scale, scale, 1);
        GL11.glTranslatef(-x, -y, 0);
    }
    
    public void scale(float x, float y, float z, float scale) {
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, z);
        GL11.glScalef(scale, scale, 1);
        GL11.glTranslatef(-x, -y, 0);
    }
    
    public void end() {
    	GL11.glPopMatrix();
    }
    
    public void drawBoxing(AxisAlignedBB boundingBox) {
        BUILDER.begin(3, DefaultVertexFormats.POSITION);
        BUILDER.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        BUILDER.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
        BUILDER.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
        BUILDER.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
        BUILDER.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        TESSELLATOR.draw();
        BUILDER.begin(3, DefaultVertexFormats.POSITION);
        BUILDER.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        BUILDER.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
        BUILDER.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        BUILDER.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        BUILDER.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        TESSELLATOR.draw();
        BUILDER.begin(1, DefaultVertexFormats.POSITION);
        BUILDER.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        BUILDER.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        BUILDER.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
        BUILDER.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
        BUILDER.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
        BUILDER.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        BUILDER.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
        BUILDER.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        TESSELLATOR.draw();
    }
    
    public void drawBoxing(AxisAlignedBB axisalignedbb, float red, float green, float blue, float alpha) {
    	BUILDER.begin(7, DefaultVertexFormats.POSITION_TEX);
    	BUILDER.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
    	BUILDER.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
    	BUILDER.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
    	BUILDER.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
    	BUILDER.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
    	BUILDER.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
    	BUILDER.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
    	BUILDER.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        TESSELLATOR.draw();
        BUILDER.begin(7, DefaultVertexFormats.POSITION_TEX);
        BUILDER.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        BUILDER.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        BUILDER.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        BUILDER.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        BUILDER.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        BUILDER.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        BUILDER.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        BUILDER.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        TESSELLATOR.draw();
        BUILDER.begin(7, DefaultVertexFormats.POSITION_TEX);
        BUILDER.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        BUILDER.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        BUILDER.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        BUILDER.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        BUILDER.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        BUILDER.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        BUILDER.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        BUILDER.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        TESSELLATOR.draw();
        BUILDER.begin(7, DefaultVertexFormats.POSITION_TEX);
        BUILDER.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        BUILDER.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        BUILDER.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        BUILDER.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        BUILDER.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        BUILDER.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        BUILDER.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        BUILDER.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        TESSELLATOR.draw();
        BUILDER.begin(7, DefaultVertexFormats.POSITION_TEX);
        BUILDER.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        BUILDER.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        BUILDER.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        BUILDER.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        BUILDER.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        BUILDER.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        BUILDER.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        BUILDER.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        TESSELLATOR.draw();
        BUILDER.begin(7, DefaultVertexFormats.POSITION_TEX);
        BUILDER.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        BUILDER.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        BUILDER.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        BUILDER.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        BUILDER.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        BUILDER.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        BUILDER.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        BUILDER.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        TESSELLATOR.draw();
    }
    
	public void drawEntity3D(MatrixStack ms, LivingEntity player, Vector3d pos, float alpha) {
		ActiveRenderInfo activeRenderInfo = mc.gameRenderer.getActiveRenderInfo();

		GlowESP.SILENT_RENDERING = true;
		
		ms.push();
	   	
		GlStateManager.enableBlend();
		GlStateManager.depthMask(false);
		GlStateManager.disableTexture();
		GL11.glShadeModel(7425);
		GlStateManager.disableCull();
		GlStateManager.enableDepthTest();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_DST_ALPHA);
		GlStateManager.glBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA.param, GlStateManager.DestFactor.ONE.param, GlStateManager.SourceFactor.ZERO.param, GlStateManager.DestFactor.ONE.param);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		
		
		Vector3d cameraPos = mc.getRenderManager().info.getProjectedView();
		try {
			double x = pos.x - mc.getRenderManager().info.getProjectedView().getX(),
					y = pos.y - mc.getRenderManager().info.getProjectedView().getY(),
		        	z = pos.z - mc.getRenderManager().info.getProjectedView().getZ();
	        
			EntityRendererManager renderManager = mc.getRenderManager();
			
			if (renderManager == null)
				return;

			float partialTicks = mc.getRenderPartialTicks();
			
			Vector3d camera = activeRenderInfo.getProjectedView();
			
			Reacher.ENTITY_ALPHA = alpha;
			Reacher.SILENT = true;
			
			
			if (Config.isShaders()) {
				Shaders.nextEntity(player);
			}
			
			renderManager.renderEntityStaticSilent(
					player, 
					x, 
					y, 
					z, 
					MathHelper.lerp(partialTicks, player.prevRotationYaw, player.rotationYaw), 
					partialTicks, ms, 
					mc.getRenderTypeBuffers().getBufferSource(), 
					renderManager.getPackedLight(player, partialTicks)
			);
			
			Reacher.ENTITY_ALPHA = 1;
			Reacher.SILENT = false;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		
		GlStateManager.blendFunc(770, 771);
		GlStateManager.shadeModel(7424);
		GlStateManager.disableBlend();
		GlStateManager.enableTexture();
		GlStateManager.depthMask(true);
		GlStateManager.enableCull();
		ms.pop();
		GlowESP.SILENT_RENDERING = false;
	}
    
    public void blockEsp(BlockPos blockPos, int color) {
        double x = blockPos.getX(),
        	   y = blockPos.getY(),
        	   z = blockPos.getZ();
        GL11.glPushMatrix();
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glLineWidth(1);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        
        Render.color(color);
        Render.drawBoxing(new AxisAlignedBB(x, y, z, x + 1, y + 1.0, z + 1));
        Render.drawBoxing(new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1));
        GL11.glLineWidth(2);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        Render.resetColor();
        GL11.glPopMatrix();
    }
    
    public void drawRect(MatrixStack matrix, float x, float y, float width, float height, FixColor color) {
    	float minX = x;
    	float minY = y;
    	float maxX = x + width;
    	float maxY = y + height;
    	
    	float f3 = color.getAlpha() / 255.0F;
        float f = color.getRed() / 255.0F;
        float f1 = color.getGreen() / 255.0F;
        float f2 = color.getBlue() / 255.0F;
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(matrix.getLast().getMatrix(), (float)minX, (float)maxY, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(matrix.getLast().getMatrix(), (float)maxX, (float)maxY, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(matrix.getLast().getMatrix(), (float)maxX, (float)minY, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(matrix.getLast().getMatrix(), (float)minX, (float)minY, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.finishDrawing();
        WorldVertexBufferUploader.draw(bufferbuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
    
}
