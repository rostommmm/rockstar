package fun.rockstarity.client.modules.other;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;

/**
 * @author Malecharik
 * @since 17 Mar 2024 22:06:39
 */

@Info(name = "MultiActions", desc = "Мульти задачи", type = Category.OTHER)
public class MultiActions extends Module {

	@Override
	public void onEvent(Event event) {
	}
	
	@Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}
}
