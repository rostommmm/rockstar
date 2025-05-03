package fun.rockstarity.api.events.list.render.ui.shaders;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ConeTin
 * @since 23 июл. 2024 г.
 */

@Getter
@AllArgsConstructor
public class EventBlur extends Event {

	private final MatrixStack matrixStack;
	
	private final float partialTicks;
	
}
