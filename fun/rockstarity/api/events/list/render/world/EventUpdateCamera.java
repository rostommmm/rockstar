package fun.rockstarity.api.events.list.render.world;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.renderer.ActiveRenderInfo;

@Getter @Setter
@AllArgsConstructor
public class EventUpdateCamera extends Event {
	
    private final MatrixStack matrixStack;
    
}
