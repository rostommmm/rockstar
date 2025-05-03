package fun.rockstarity.api.render.ui.clickgui.esp.elmts;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.render.ui.clickgui.esp.ESPTextElement;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;

/**
 * @author ConeTin
 * @since 28 РѕРєС‚. 2024вЂЇРі.
 */

public class ESPRightItem extends ESPTextElement {
	private final CheckBox hideIfEmpty = new CheckBox(this, "РќРµ РѕС‚РѕР±СЂР°Р¶Р°С‚СЊ РµСЃР»Рё РїСѓСЃС‚Рѕ");

	public ESPRightItem() {
		super("РџСЂР°РІР°СЏ СЂСѓРєР°");
	}

	@Override
	public void drawOnEntity(MatrixStack matrixStack, LivingEntity entity, float x, float y, float width, float height, float anim) {
		String name = entity.getName().getString();
		String item = entity.getHeldItemMainhand().getItem() != Items.AIR ? entity.getHeldItemMainhand().getDisplayName().getString() : "РџСѓСЃС‚Рѕ";
		
		if (hideIfEmpty.get() && (entity.getHeldItemMainhand().isEmpty() || entity.getHeldItemMainhand().getItem() == Items.AIR)) return;
		
		setText(item);
		
		super.drawOnEntity(matrixStack, entity, x, y, width, height, anim);
	}
	
}
