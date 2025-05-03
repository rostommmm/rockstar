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
public class EventRenderFog extends Event {
	
	private final boolean pre;
	
}