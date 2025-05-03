package fun.rockstarity.api.scripts.wrappers;

import java.awt.Font;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.luaj.vm2.LuaFunction;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.game.Binds;
import fun.rockstarity.api.helpers.render.Converter;
import fun.rockstarity.api.helpers.render.PositionTracker;
import fun.rockstarity.api.helpers.render.Stencil;
import fun.rockstarity.api.helpers.render.gif.GifRender;
import fun.rockstarity.api.helpers.secure.Web;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.shaders.list.Bloom;
import fun.rockstarity.api.render.shaders.list.Blur;
import fun.rockstarity.api.render.shaders.list.BlurNew;
import fun.rockstarity.api.render.shaders.list.Glow;
import fun.rockstarity.api.render.shaders.list.Gray;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.fonts.FontSize;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.api.scripts.wrappers.base.ColorBase;
import fun.rockstarity.api.scripts.wrappers.base.LivingEntityBase;
import fun.rockstarity.api.scripts.wrappers.base.PotionBase;
import fun.rockstarity.api.secure.nativeapi.NativeHelper;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.PotionSpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.potion.Effect;
import net.minecraft.util.ResourceLocation;

public class Render implements IAccess {
	Map<String, FontSize> fontMap = new HashMap<>();
	Map<String, ResourceLocation> rsMap = new HashMap<>();
	Map<String, GifRender> gifMap = new HashMap<>();
	@Setter
	private static MatrixStack stack;
	
	public void init_stencil() {
		Stencil.init();
	}

	public void read_stencil(int i) {
		Stencil.read(i);
	}

	public void finish_stencil() {
		Stencil.finish();
	}
	
	public static void glow(float x, float y, float width, float height, float radius, float soft, ColorBase color1, ColorBase color2, ColorBase color3, ColorBase color4) {
		Glow.draw(stack, new Rect(x,y,width,height), radius, soft, 10 + radius, new FixColor(color1), new FixColor(color2), new FixColor(color3), new FixColor(color4));
    }
	
	public static void glow(float x, float y, float width, float height, float radius, float soft, ColorBase color) {
		Glow.draw(stack, new Rect(x,y,width,height), radius, soft, 10 + radius, new FixColor(color), new FixColor(color), new FixColor(color), new FixColor(color));
    }

    public static void glow(float x, float y, float width, float height, ColorBase color) {
    	glow(x,y,width,height,4,4,color,color,color,color);
    }
    
    public static void glow(float x, float y, float width, float height, ColorBase color1, ColorBase color2, ColorBase color3, ColorBase color4) {
    	glow(x,y,width,height,4,4,color1,color2,color3,color4);
    }
	
	public void blur(float x, float y, float width, float height, float round, int radius) {
		Stencil.init();
		Round.draw(stack, new Rect(x, y, width, height), round, FixColor.WHITE);
		Stencil.read(1);
		BlurNew.renderBlur(radius);
		Stencil.finish();
	}

	public void blur(float x, float y, float width, float height, int radius) {
		Stencil.init();
		Round.draw(stack, new Rect(x, y, width, height), 0, FixColor.WHITE);
		Stencil.read(1);
		BlurNew.renderBlur(radius);
		Stencil.finish();
	}
	
	public void blur(float x, float y, float width, float height) {
		Stencil.init();
		Round.draw(stack, new Rect(x, y, width, height), 0, FixColor.WHITE);
		Stencil.read(1);
		BlurNew.renderBlur(2);
		Stencil.finish();
	}
	
	public void blur(int radius, LuaFunction func) {
		Stencil.init();
		func.call();
		Stencil.read(1);
		BlurNew.renderBlur(radius);
		Stencil.finish();
	}
	
	public void blur(LuaFunction func) {
		Stencil.init();
		func.call();
		Stencil.read(1);
		BlurNew.renderBlur(5);
		Stencil.finish();
	}
	
	public void gray(float x, float y, float width, float height, float round, float radius) {
		Stencil.init();
		Round.draw(stack, new Rect(x, y, width, height), round, FixColor.WHITE);
		Stencil.read(1);
		Gray.render(radius);
		Stencil.finish();
	}

	public void gray(float x, float y, float width, float height, float radius) {
		Stencil.init();
		Round.draw(stack, new Rect(x, y, width, height), 0, FixColor.WHITE);
		Stencil.read(1);
		Gray.render(radius);
		Stencil.finish();
	}
	
	public void gray(float x, float y, float width, float height) {
		Stencil.init();
		Round.draw(stack, new Rect(x, y, width, height), 0, FixColor.WHITE);
		Stencil.read(1);
		Gray.render(1);
		Stencil.finish();
	}
	
	public void gray(float radius, LuaFunction func) {
		Stencil.init();
		func.call();
		Stencil.read(1);
		Gray.render(radius);
		Stencil.finish();
	}
	
	public void gray(LuaFunction func) {
		Stencil.init();
		func.call();
		Stencil.read(1);
		Gray.render(1);
		Stencil.finish();
	}
	
	public void bloom(int shadow, int offset, LuaFunction func) {
		Bloom.init();
		func.call();
		Bloom.finish(stack, shadow, offset);
	}
	
	public void bloom(LuaFunction func) {
		Bloom.init();
		func.call();
		Bloom.finish(stack, 2, 4);
	}

	public void rect(float x, float y, float width, float height, float round, ColorBase color) {
		Round.draw(stack, new Rect(x, y, width, height), round, new FixColor(color));
	}
	
	public void rect(float x, float y, float width, float height, ColorBase color) {
		Round.draw(stack, new Rect(x, y, width, height), 0, new FixColor(color));
	}
	
	public void gradient(float x, float y, float width, float height, float round, ColorBase color, ColorBase color1) {
		Round.draw(stack, new Rect(x, y, width, height), round, new FixColor(color), new FixColor(color1), new FixColor(color), new FixColor(color1));
	}
	
	public void gradient(float x, float y, float width, float height, float round, ColorBase color, ColorBase color1, ColorBase color2, ColorBase color3) {
		Round.draw(stack, new Rect(x, y, width, height), round, new FixColor(color), new FixColor(color1), new FixColor(color2), new FixColor(color3));
	}
	
	public void gradient(float x, float y, float width, float height, ColorBase color, ColorBase color1) {
		Round.draw(stack, new Rect(x, y, width, height), 0, new FixColor(color), new FixColor(color1), new FixColor(color), new FixColor(color1));
	}
	
	public void gradient(float x, float y, float width, float height, ColorBase color, ColorBase color1, ColorBase color2, ColorBase color3) {
		Round.draw(stack, new Rect(x, y, width, height), 0, new FixColor(color), new FixColor(color1), new FixColor(color2), new FixColor(color3));
	}
	
	public void gif(String path, float x, float y, float width, float height, float alpha, float speed) {
		GifRender gif;
		if (!gifMap.containsKey(path)) {
			if (path.contains("http")) {
				gif = new GifRender(Converter.getInputStream(path));
				gifMap.put(path, gif);
			} else {
				String path1 = getPath(path);
				gif = new GifRender(new File(path1));
				gifMap.put(path, gif);
			}
		} else {
			gif = gifMap.get(path);
		}
		gif.draw(stack, x, y, width, height, alpha, speed);
	}
	
	public void image(String path, float x, float y, float width, float height, ColorBase color) {
		ResourceLocation rs;
		if (!rsMap.containsKey(path)) {
			if (path.contains("http")) {
				rs = NativeHelper.getImageResource(path);
				rsMap.put(path, rs);
			} else {
				String path1 = getPath(path);
				TextureManager textureManager = Minecraft.getInstance().getTextureManager();
				DynamicTexture dynamicTexture = new DynamicTexture(Converter.loadNativeImage(path1));
				rs = textureManager.getDynamicTextureLocation("custom", dynamicTexture);
				rsMap.put(path, rs);
			}
		} else {
			rs = rsMap.get(path);
		}
		fun.rockstarity.api.helpers.render.Render.image(rs, x, y, width, height, new FixColor(color));
	}
	
	public void effect(PotionBase eff, float x, float y, float width, float height, float alpha) {
		PotionSpriteUploader potionspriteuploader = this.mc.getPotionSpriteUploader();
		Effect effect = eff.getEffect();
		TextureAtlasSprite textureatlassprite = potionspriteuploader.getSprite(effect);
		this.mc.getTextureManager().bindTexture(textureatlassprite.getAtlasTexture().getTextureLocation());
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
		IngameGui.blit(stack, x, y, mc.getIngameGUI().getBlitOffset(), width, height, textureatlassprite);
		RenderSystem.disableBlend();
	}

	public void entity(float x, float y, float size, float yaw, float pitch, LivingEntityBase entity) {
		InventoryScreen.drawEntityOnScreen(x, y, size, (float) yaw, (float) pitch, entity.getEntity());
	}

	public void entity(float x, float y, float size, float yaw, float pitch) {
		InventoryScreen.drawEntityOnScreen(x, y, size, (float) yaw, (float) pitch, mc.player);
	}

	public void head(float x, float y, float width, float height, LivingEntityBase player) {
		if (!(player.getEntity() instanceof AbstractClientPlayerEntity ent) || player == null)
			return;

		RenderSystem.enableBlend();
		mc.getTextureManager().bindTexture(ent.getLocationSkin());
		fun.rockstarity.api.helpers.render.Render.drawScaledCustomSizeModalRect(x, y, (float) 8.0, (float) 8.0, 8, 8,width, height, 64.0F, 64.0F);
		RenderSystem.disableBlend();
	}

	public void head(float x, float y, float width, float height) {
		RenderSystem.enableBlend();
		mc.getTextureManager().bindTexture(mc.player.getLocationSkin());
		fun.rockstarity.api.helpers.render.Render.drawScaledCustomSizeModalRect(x, y, (float) 8.0, (float) 8.0, 8, 8,width, height, 64.0F, 64.0F);
		RenderSystem.disableBlend();
	}
		
	public void text(String text, float x, float y, ColorBase color) {
		bold.get(20).draw(stack, text, x, y, new FixColor(color));
	}
	
	public void text(String text, float x, float y) {
		bold.get(20).draw(stack, text, x, y, FixColor.WHITE);
	}
	
	public float text_width(String text) {
		return bold.get(20).getWidth(text);
	}
	
	public String key_name(int key) {
		return Binds.getName(key, -1);
	}
	
	public void text(String path, int size, String text, float x, float y) {
		FontSize customFont;
		if (!fontMap.containsKey(path + size)) {
			if (path.contains("http")) {
				fontMap.put(path + size, customFont = new FontSize(Web.getFullFont(path, Font.PLAIN, size), size));
			} else {
				String path1 = getPath(path);
				fontMap.put(path + size, customFont = new FontSize(new File(getPath(path)), size));
			}
		} else {
			customFont = fontMap.get(path + size);
		}
		customFont.draw(stack, text, x, y, FixColor.WHITE);
	}
	
	public void text(String path, int size, String text, float x, float y, ColorBase color) {
		FontSize customFont;
		if (!fontMap.containsKey(path + size)) {
			if (path.contains("http")) {
				fontMap.put(path + size, customFont = new FontSize(Web.getFullFont(path, Font.PLAIN, size), size));
			} else {
				String path1 = getPath(path);
				fontMap.put(path + size, customFont = new FontSize(new File(getPath(path)), size));
			}
		} else {
			customFont = fontMap.get(path + size);
		}
		customFont.draw(stack, text, x, y, new FixColor(color));
	}

	public float text_width(String path, int size, String text) {
		FontSize customFont;
		if (!fontMap.containsKey(path + size)) {
			if (path.contains("http")) {
				fontMap.put(path + size, customFont = new FontSize(Web.getFullFont(path, Font.PLAIN, size), size));
			} else {
				String path1 = getPath(path);
				fontMap.put(path + size, customFont = new FontSize(new File(getPath(path)), size));
			}
		} else {
			customFont = fontMap.get(path + size);
		}
		return customFont.getWidth(text);
	}
	
	public void text(int size, String text, float x, float y, ColorBase color) {
		bold.get(size).draw(stack, text, x, y, new FixColor(color));
	}

	public float text_width(int size, String text) {
		return bold.get(size).getWidth(text);
	}
	
	public ColorBase alpha(ColorBase color, float alpha) {
		FixColor color1 = new FixColor(color).alpha(alpha);
		return new ColorBase(color1);
	}
	
	public double[] world_to_screen(double x, double y, double z) {
		 double[] pos = fun.rockstarity.api.helpers.render.Render.worldToScreen(x, y, z);
         if (pos == null) return new double[] { -100, -100 };
         
         if (PositionTracker.isInView(x, y, z)) {
         	return pos;
         }
         
		return new double[] { -100, -100 };
	}
	
	private String getPath(String path) {
		return path.contains(":/") ? path : rock.getPath() + "scripts/" + path;
	}
}