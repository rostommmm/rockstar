package fun.rockstarity.api.events.list.render;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.render.ui.AEventRender2D;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ConeTin
 * @since 3 дек. 2023 г.
 */

public class EventRender2D extends AEventRender2D {

	public EventRender2D(MatrixStack matrixStack, float partialTicks) {
		super(matrixStack, partialTicks);
	}
	
}
