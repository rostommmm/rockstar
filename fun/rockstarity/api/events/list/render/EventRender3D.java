package fun.rockstarity.api.events.list.render;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.renderer.ActiveRenderInfo;

/**
 * @author ConeTin
 * @since 3 дек. 2023 г.
 */

@Getter
@AllArgsConstructor
public class EventRender3D extends Event {

	private final MatrixStack matrixStack;
	
	private final float partialTicks;
	
	private final ActiveRenderInfo renderInfo;
	
}
