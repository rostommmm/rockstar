package fun.rockstarity.api.events.list.render.ui;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ConeTin
 * @since 9 янв. 2025 г.
 */

@Getter
@AllArgsConstructor
public abstract class AEventRender2D extends Event {
	
	private final MatrixStack matrixStack;
	private final float partialTicks;
	
}
