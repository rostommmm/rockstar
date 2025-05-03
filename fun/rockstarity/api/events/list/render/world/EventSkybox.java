package fun.rockstarity.api.events.list.render.world;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.ResourceLocation;

@Getter @Setter
@AllArgsConstructor
public class EventSkybox extends Event {
	
    private ResourceLocation texture;
    
}
