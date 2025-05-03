package fun.rockstarity.api.events.list.render.ui;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ConeTin
 * @since 3 РґРµРє. 2023 Рі.
 */

public class EventRenderPreUI extends AEventRender2D {

	public EventRenderPreUI(MatrixStack matrixStack, float partialTicks) {
		super(matrixStack, partialTicks);
	}
	
}
