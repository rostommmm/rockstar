package fun.rockstarity.api.events.list.render.ui;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ConeTin
 * @since 14 июн. 2024 г.
 */

@Getter
@AllArgsConstructor
public class EventHotbarSlot extends Event {
	
	private final MatrixStack matrixStack;
	private final float partialTicks;
	private final int x, y, slot;
	
}
