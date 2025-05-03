package fun.rockstarity.client.modules.move;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventAttack;
import fun.rockstarity.api.events.list.player.EventMotionMove;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.events.list.render.EventRender3D;
import fun.rockstarity.api.helpers.player.Move;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.render.color.themes.Style;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.math.vector.Vector3d;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author ConeTin
 * @since 22 июл. 2024 г.
 */
@NativeInclude
@Info(name = "BoatControl", desc = "Выключает античит возле лодки", type = Category.MOVE)
public class BoatControl extends Module {
	
	private BoatEntity target;

	@Override
	public void onEvent(Event event) {
		if (event instanceof EventUpdate e) {
			this.target = null;
			for (Entity ent : mc.world.getAllEntities()) {
				if (ent instanceof BoatEntity entity && mc.player.getDistance(entity) < 3f) {
					this.target = entity;
					break;
				}
			}
			
			if (this.target != null) {
				mc.player.fallDistance = 1;
			}
		}
		
		if (event instanceof EventRender3D e) {
			MatrixStack ms = e.getMatrixStack();
			if (this.target != null) {
				int len = 361;
				double size = 0.5f;
				int start = (int) mc.getRenderManager().info.getPitch();
				double x = this.target.lastTickPosX + (this.target.getPosX() - this.target.lastTickPosX) * (mc.currentScreen == null ? mc.getRenderPartialTicks() : 0) - mc.getRenderManager().info.getProjectedView().getX();
				double y = this.target.lastTickPosY + (this.target.getPosY() - this.target.lastTickPosY) * (mc.currentScreen == null ? mc.getRenderPartialTicks() : 0) - mc.getRenderManager().info.getProjectedView().getY();
				double z = this.target.lastTickPosZ + (this.target.getPosZ() - this.target.lastTickPosZ) * (mc.currentScreen == null ? mc.getRenderPartialTicks() : 0) - mc.getRenderManager().info.getProjectedView().getZ();
				
				for (int i = start; i <= start + len - 1; i += 1) {
					double s = 0.5f;
					double cos = Math.cos(Math.toRadians(i)) * (4) * s;
					double sin = Math.sin(Math.toRadians(i)) * (4) * s;
					double posX = x + cos;
		            double posY = y;
		            double posZ = z + sin;
		            
		    		ms.push();
		            GlStateManager.depthMask(false);
		           	ms.translate(posX, posY, posZ);
		           	ms.rotate(mc.getRenderManager().info.getRotation());
		           
		            Render.drawImage(ms, "masks/glow.png", -size / 2, -size / 2, -size / 2, size, size, Style.getPoint(i * 10));
		            GlStateManager.depthMask(true);
		            ms.pop();
				}
			}
		}
		
		if (event instanceof EventMotionMove e) {
			if (this.target != null) {
				Move.setSpeed(1);
				if (mc.player.fallDistance > 0.1f) {
				//	mc.player.getMotion().y = 0;
				}
				if (mc.player.getDistance(this.target) > 2.5f) {
					if (mc.player.getPositionVec().add(mc.player.getMotion()).distanceTo(this.target.getPositionVec()) > mc.player.getDistance(this.target)) {
						e.setMotion(Vector3d.ZERO);
					}
				}
			}
		}
		
		if (event instanceof EventAttack e) {
			if (this.target != null) {
				Vector3d pos = mc.player.getPositionVec();
				mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(pos.x, pos.y+0.1f, pos.z, mc.player.rotationYaw, mc.player.rotationPitch, false));
				mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(pos.x, pos.y, pos.z, mc.player.rotationYaw, mc.player.rotationPitch, false));
			}
		}
	}
	
	@Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}
	
}
