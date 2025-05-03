package fun.rockstarity.api.events.list.render.world;

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
public class EventViewModel extends Event {
	
	private final boolean right;
	private final MatrixStack matrixStack;
	
}