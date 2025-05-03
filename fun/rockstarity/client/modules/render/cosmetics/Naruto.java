package fun.rockstarity.client.modules.render.cosmetics;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.render.player.EventModels;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.client.modules.render.Cosmetics;
import fun.rockstarity.client.modules.render.Cosmetics.Cosmetic;
import net.minecraft.util.math.MathHelper;

public class Naruto extends Cosmetic {
	
	public Naruto(Cosmetics ui, Select select) {
		super(select, "РќР°СЂСѓС‚Рѕ");
	}
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventModels model && model.getOwner() == mc.player) {
			model.bipedRightArm.rotateAngleX = MathHelper.cos(0.6662F) * 2.0F * model.limbSwingAmount;
            model.bipedLeftArm.rotateAngleX = MathHelper.cos(0.6662F) * 2.0F * model.limbSwingAmount; 
            model.bipedRightArm.rotateAngleZ = (MathHelper.cos(0.2812F) - 1.0F) * 1.0F * model.limbSwingAmount;
            model.bipedLeftArm.rotateAngleZ = (MathHelper.cos(0.2812F) - 1.0F) * 1.0F * model.limbSwingAmount;
		}
	}
}
