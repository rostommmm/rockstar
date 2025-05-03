package fun.rockstarity.api.render.ui.clickgui.esp.elmts;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.render.ui.clickgui.esp.ESPTextElement;
import net.minecraft.entity.LivingEntity;

/**
 * @author ConeTin
 * @since 28 РѕРєС‚. 2024вЂЇРі.
 */

public class ESPShifting extends ESPTextElement {

	public ESPShifting() {
		super("РЁРёС„С‚");
	}

	@Override
	public void drawOnEntity(MatrixStack matrixStack, LivingEntity entity, float x, float y, float width, float height, float anim) {
		String name = entity.getName().getString();
		String item = entity.isSneaking() ? "РЎ С€РёС„С‚РѕРј" : "Р‘РµР· С€РёС„С‚Р°";
		
		setText(item);
		
		super.drawOnEntity(matrixStack, entity, x, y, width, height, anim);
	}
	
}
