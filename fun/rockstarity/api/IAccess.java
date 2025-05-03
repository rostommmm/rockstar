package fun.rockstarity.api;

import fun.rockstarity.Rockstar;
import fun.rockstarity.api.helpers.render.gif.GifRender;
import fun.rockstarity.api.render.ui.fonts.CustomFont;
import fun.rockstarity.api.render.ui.rect.Rect;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

/**
 * @author ConeTin
 * @since 2 дек. 2023 г.
 */

public interface IAccess {
	
	// Майнкрафт
	Minecraft mc = Minecraft.getInstance();
	MainWindow sr = mc.getMainWindow();
	
	Tessellator TESSELLATOR = Tessellator.getInstance();
	BufferBuilder BUILDER = TESSELLATOR.getBuffer();
	
	// Рокстар
	Rockstar rock = Rockstar.getInstance();
	
	CustomFont bold = new CustomFont("mulish-bold");
	CustomFont semibold = new CustomFont("mulish-semibold");
	
	Rect fullScreen = new Rect(0,0,sr.getScaledWidth(),sr.getScaledHeight());
	
	//Жаба
	Runtime runTime = Runtime.getRuntime();
	
}
