/**
 * 
 */
package fun.rockstarity.client.modules.move;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventCollision;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.events.list.render.EventRender3D;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.player.Move;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Slider;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author Malecharik
 * @since 23 Mar 2024 10:26:07
 */


@Info(name = "NoClip", desc = "Ходит сквозь стены", type = Category.MOVE)
public class NoClip extends Module {

	private final CheckBox colliBox = new CheckBox(this, "Только в блоках");
	private final CheckBox custom = new CheckBox(this, "Кастомная скорость");
	private final Slider speedCustom = new Slider(this, "Скорость").min(0.5f).max(5).inc(0.5f).set(1).hide(() -> !this.custom.get());
	
	private boolean prevCollision, collision;
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventUpdate) {
			prevCollision = mc.world.getCollisionShapes(mc.player, mc.player.getBoundingBox()).toList().isEmpty();
			collision = mc.world.getCollisionShapes(mc.player, mc.player.getBoundingBox().shrink(0.0625D)).toList().isEmpty() && prevCollision;
			mc.player.noClip = true;
			
			if (this.custom.get() && !collision) {
				Move.setSpeed(this.speedCustom.get() / 10);
			}
		}
	    if (event instanceof EventCollision e) {
	    	
	    	Vector3d backUp = new Vector3d(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ());
	    	mc.player.setPosition(mc.player.getPositionVec().x, mc.player.getPositionVec().y, mc.player.getPositionVec().z);
	    	mc.player.setPosition(backUp.x, backUp.y, backUp.z);
	    	
	    	if (colliBox.get() && collision) return;
	        
	        if ((mc.player.isSneaking() || (!mc.player.isOnGround() && mc.player.fallDistance == 0) || e.getBlockPos().getY() >= mc.player.getPosY())) {
	            event.cancel();
	        }
	    }
	}
	
	@Override
	@NativeInclude
	public void onDisable() {
		mc.player.noClip = false;
	}
	
	@Override
	public void onEnable() {
		
	}
}
