package fun.rockstarity.api.events.list.render.world;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.renderer.ActiveRenderInfo;

@Getter @Setter
@AllArgsConstructor
public class EventRenderWorldLast extends Event {
	
	private boolean shadersRender;
    private final MatrixStack matrixStack;
    private final ActiveRenderInfo activeRenderInfo;
    private final float partialTicks;
    
}
