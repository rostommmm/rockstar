package fun.rockstarity.api.events.list.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ConeTin
 * @since 7 дек. 2023 г.
 * @in FirstPersonRenderer
 */

@Getter
@AllArgsConstructor
public class EventRenderItem extends Event {
	
	private final boolean right;
	private final float progress;
	private final MatrixStack matrixStack;
	
}