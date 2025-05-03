package fun.rockstarity.client.modules.render.cosmetics;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.client.modules.render.Cosmetics;
import fun.rockstarity.client.modules.render.Cosmetics.Cosmetic;
import fun.rockstarity.client.modules.render.FreeLook;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3f;

/**
 * @author ConeTin
 * @since 5 мая 2024 г.
 */

public class Crown extends Cosmetic {
	
	public Crown(Cosmetics ui, Select select) {
		super(select, "Корона");
	}
	
	@Override
	public void onEvent(Event event) {
	}
	
	public void render(MatrixStack ms) {
		if (this.showing.finished(false)) return;
		
		Render.startFlatRender();
		
		LivingEntity entity = mc.player;
		
		double x = entity.lastTickPosX + (entity.getPosX() - entity.lastTickPosX) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getX();
		double y = entity.lastTickPosY + (entity.getPosY() - entity.lastTickPosY) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getY() + mc.player.getEyeHeight() + 0.1;
		double z = entity.lastTickPosZ + (entity.getPosZ() - entity.lastTickPosZ) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getZ();

		float miniSize = 0.08f;
		
		FreeLook freelook = rock.getModules().get(FreeLook.class);
		
		for (int i = 0; i<360; i++) {
			float sin = (float) Math.sin(Math.toRadians(i)) * 0.5f;
			float cos = (float) Math.cos(Math.toRadians(i)) * 0.5f;
			
			ms.push();
			ms.translate(sin, 0.4f, cos);
			if (mc.getGameSettings().getPointOfView() == PointOfView.THIRD_PERSON_BACK)
				ms.rotate(Vector3f.XP.rotationDegrees(freelook.get() ? mc.player.rotationYaw : 180));
           	
            Render.flatImage(ms, "masks/glow.png", (float) -miniSize / 2, -miniSize / 2, (float) -miniSize / 2, miniSize, miniSize, Style.getPoint(i*3).alpha(this.showing.get()));
            
            ms.pop();
            
            for (int i1 = 0; i1<10; i1++) {
				ms.push();
               	ms.translate(sin, 0.5 + Math.sin(Math.toRadians(i*8)) * (0.1f-i1*0.01f) - i1 * 0.02f, cos);
               	if (mc.getGameSettings().getPointOfView() == PointOfView.THIRD_PERSON_BACK)
               		ms.rotate(Vector3f.XP.rotationDegrees(freelook.get() ? mc.player.rotationYaw : 180));
               	
                Render.flatImage(ms, "masks/glow.png", (float) -miniSize / 2, -miniSize / 2, (float) -miniSize / 2, miniSize, miniSize, Style.getPoint(i*3).alpha(this.showing.get()));
               
                ms.pop();
			}
		}
		
		Render.endFlatRender();
	}
	
}
