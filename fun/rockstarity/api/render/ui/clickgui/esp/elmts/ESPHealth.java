package fun.rockstarity.api.render.ui.clickgui.esp.elmts;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.ui.clickgui.esp.ESPBar;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * @author ConeTin
 * @since 28 окт. 2024 г.
 */

public class ESPHealth extends ESPBar {

	public ESPHealth() {
		super("Здоровье");
	}

	@Override
	public void drawOnEntity(MatrixStack matrixStack, LivingEntity entity, float x, float y, float width, float height, float anim) {
		float noAnimHealth = Math.max(0, Math.min(1, (Server.isServerForHPFix() ? (entity.getRealHealth()) / (20F) : (entity.getHealth()) / (entity.getMaxHealth()+entity.getAbsorptionAmount()))));
		percent = entity.getHealthAnim().animate(noAnimHealth, 300);
		color = (percent > 0.5f ? FixColor.ORANGE.move(FixColor.GREEN, (percent-0.5f) * 2) : FixColor.RED.move(FixColor.ORANGE, percent * 2));
		super.drawOnEntity(matrixStack, entity, x, y, width, height, anim);
		
       // mc.ingameGUI.func_238452_a_(new TranslationTextComponent("ДОЛБАЁБ ФЛАЙ ВЫКЛЮЧИ"), new TranslationTextComponent("ФЛАЙ ВЫКЛЮЧИ КИКНЕТ"), 20, Math.max(20, 200), 20);
	}
	
}
