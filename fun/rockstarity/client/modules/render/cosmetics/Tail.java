package fun.rockstarity.client.modules.render.cosmetics;

import java.util.ArrayList;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.render.EventRender3D;
import fun.rockstarity.api.helpers.player.Move;
import fun.rockstarity.api.helpers.render.PositionTracker;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.client.modules.render.Cosmetics;
import fun.rockstarity.client.modules.render.Cosmetics.Cosmetic;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;

/**
 * @author ConeTin
 * @since 9 мая 2024 г.
 */

public class Tail extends Cosmetic {
	
	private final ArrayList<Vector4f> tail = new ArrayList<>();
	
	public Tail(Cosmetics ui, Select select) {
		super(select, "Хвостик");
	}
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventRender3D e) {
			MatrixStack ms = e.getMatrixStack();
			
			float bigSize = 0.7f;
			float size = 0.3f;
			
			ArrayList<Vector4f> toRemove = new ArrayList<>();
			
			Render.startFlatRender();

			int lineSize = this.tail.size();
          	for (Vector4f pos : this.tail) {
          		int i = this.tail.indexOf(pos);
          		float start = Math.min(i / 10F * 2, 1);
          		//float end = Math.min((lineSize - i) / 10F * 4, 1);
          		
          		
          		double posX = pos.x - mc.getRenderManager().info.getProjectedView().x;
    	        double posY = pos.y - mc.getRenderManager().info.getProjectedView().y;
    	        double posZ = pos.z - mc.getRenderManager().info.getProjectedView().z;
          		
          		if (PositionTracker.isInView(new Vector3d(pos.x, pos.y, pos.z))) {
          			ms.push();
                  	ms.translate(posX, posY, posZ);
                  	ms.rotate(Vector3f.XP.rotationDegrees(90));
                  	
              		Render.flatImage(ms, "masks/glow.png", (float) -bigSize / 2, -bigSize / 2, -0.01f, bigSize, bigSize, Style.getPoint(i*10).alpha(this.showing.get()));
              		Render.flatImage(ms, "masks/glow.png", (float) -size / 2, -size / 2, -0.01f, size, size, Style.getPoint(i*10).alpha(0.2f*this.showing.get()));
              		
              		float ySize = 50 * start;
              		for (int y = 1; y < ySize; y++) {
              			float val = 1 - y / ySize;
              			float lightSize = 0.3f;
              			Render.flatImage(ms, "masks/glow.png", (float) -lightSize / 2, -lightSize / 2, -0.01f - y * 0.015, lightSize, lightSize, Style.getPoint(i*10).alpha(val*0.2f*this.showing.get()));
              		}
              		
              		ms.pop();
          		}
          		
          		pos.w = pos.w - 0.01f;
          		
          		if (pos.w < 0) toRemove.add(pos);
          	}
          	
			Render.endFlatRender();
			
			Vector3d playerPos = mc.player.getPositionVec();
			
			if (mc.player.prevPosX != mc.player.getPosX() || mc.player.prevPosZ != mc.player.getPosZ()) {
				float x = (float) (mc.player.lastTickPosX + (mc.player.getPosX() - mc.player.lastTickPosX) * mc.getRenderPartialTicks());
				float y = (float) (mc.player.lastTickPosY + (mc.player.getPosY() - mc.player.lastTickPosY) * mc.getRenderPartialTicks());
				float z = (float) (mc.player.lastTickPosZ + (mc.player.getPosZ() - mc.player.lastTickPosZ) * mc.getRenderPartialTicks());

				this.tail.add(new Vector4f(x,y,z,1));
			}
			
			this.tail.removeAll(toRemove);
		}
	}
	
}
