package fun.rockstarity.api.render.ui.clickgui.esp.elmts;

import java.util.Map;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.render.ui.clickgui.esp.ESPTextElement;
import net.minecraft.client.gui.overlay.PlayerTabOverlayGui;
import net.minecraft.entity.LivingEntity;

/**
 * @author ConeTin
 * @since 28 РѕРєС‚. 2024вЂЇРі.
 */

public class ESPPing extends ESPTextElement {

	public ESPPing() {
		super("РџРёРЅРі");
	}

	@Override
	public void drawOnEntity(MatrixStack matrixStack, LivingEntity entity, float x, float y, float width, float height, float anim) {
		Map<String, Integer> pings = PlayerTabOverlayGui.getPlayerPings();
		String name = entity.getName().getString();
		int ping = pings.containsKey(name) ? pings.get(name) : 0;
		
		setText(ping + " ms");
		
		super.drawOnEntity(matrixStack, entity, x, y, width, height, anim);
	}
	
}
