package fun.rockstarity.api.render.ui.clickgui.esp.elmts;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.helpers.render.Stencil;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.render.shaders.list.Outline;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.clickgui.esp.ESPElement;
import fun.rockstarity.api.render.ui.clickgui.esp.ESPSettings;
import fun.rockstarity.api.render.ui.rect.Rect;
import net.minecraft.entity.LivingEntity;

/**
 * @author ConeTin
 * @since 6 РѕРєС‚. 2024вЂЇРі.
 */

public class ESPBoxes extends ESPElement {
	
	public ESPBoxes() {
		super("Р‘РѕРєСЃС‹");
	}

	@Override
	public void drawPreview(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, float anim) {
		super.drawPreview(matrixStack, mouseX, mouseY, partialTicks, anim);
		
		rect.setWidth(50);
		rect.setHeight(120);
		
		if (activeAnim.finished(false)) return;
		
		ESPSettings settings = rock.getClickGui().getWindow().getEspSettings();
		
		Stencil.init();
		Round.draw(matrixStack, settings.getPreviewRect(), 7, FixColor.WHITE);
		Stencil.read(1);
		
		drawOnEntity(matrixStack, mc.player, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), anim);
		
		Stencil.finish();
	}

	@Override
	public void drawOnEntity(MatrixStack matrixStack, LivingEntity entity, float x, float y, float width, float height, float anim) {
		anim *= activeAnim.get();
		
		FixColor color1 = Style.getPoint(0).alpha(anim);
		FixColor color2 = Style.getPoint(90).alpha(anim);
		FixColor color3 = Style.getPoint(180).alpha(anim);
		FixColor color4 = Style.getPoint(230).alpha(anim);
        
		if (rock.getFriendsHandler().isFriend(entity)) {
			Outline.draw(matrixStack, new Rect(x, y, width, height), 2, 1, FixColor.GREEN);
		} else {
			Outline.draw(matrixStack, new Rect(x, y, width, height), 2, 1, color1, color2, color3, color4);
		}
	}
	
	@Override
	public boolean clicked(double mouseX, double mouseY, int button) {
		rect.setWidth(50);
		rect.setHeight(120);
		return super.clicked(mouseX, mouseY, button);
	}

}
