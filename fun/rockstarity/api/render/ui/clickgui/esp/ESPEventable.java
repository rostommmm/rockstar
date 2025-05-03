package fun.rockstarity.api.render.ui.clickgui.esp;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.events.Event;
import net.minecraft.entity.LivingEntity;

/**
 * @author ConeTin
 * @since 19 апр. 2025 г.
 */

public class ESPEventable extends ESPElement {
	
	public ESPEventable(String name) {
		super(name);
	}

	public void onEvent(Event event) {}

	@Override
	public void drawOnEntity(MatrixStack matrixStack, LivingEntity entity, float x, float y, float width, float height,
			float anim) {
		
	}

}
