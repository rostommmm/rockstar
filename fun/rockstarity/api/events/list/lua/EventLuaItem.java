package fun.rockstarity.api.events.list.lua;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.scripts.wrappers.base.MatrixBase;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EventLuaItem extends Event {
	
	private final boolean right;
	private final float progress;
	private final MatrixBase matrixStack;
	
}