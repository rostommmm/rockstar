package fun.rockstarity.client.modules.player;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Select.Element;
import lombok.Getter;
import fun.rockstarity.api.modules.settings.list.Slider;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;

/**
 * @author ConeTin
 * @since 4 июн. 2024 г.
 */


@Info(name="NoDelay", desc="Убирает задежку между кликами и прыжками", type=Category.PLAYER)
public class NoDelay extends Module {
	private final Select utils = new Select(this, "Выбор");
	
	private final Element jumps = new Element(utils, "Прыжок").set(true);
	@Getter private final Element rightClicks = new Element(utils, "ПКМ");
	private final Element leftClicks = new Element(utils, "ЛКМ");
	private final Element hit = new Element(utils, "Ломание");
	private final Element fastEXP = new Element(utils, "Бутыльки опыта");
	
	@Getter private final Slider delay = new Slider(this, "Задержка ПКМ").min(0f).max(4).inc(1).set(0).hide(() -> !rightClicks.get());

	@Override
	public void onEvent(Event event) {
		if (event instanceof EventMotion e) {
			if (this.jumps.get()) mc.player.setJumpTicks(0); // Устанавливаем задержку на Прыжок (0 - нет задержки)
			if (this.leftClicks.get() && mc.getGameSettings().keyBindAttack.isKeyDown()) mc.clickMouse(); // Если игрок зажимает атаку делаем клик
			if (hit.get()) mc.playerController.setBlockHitDelay(0);
		}
		
		if (event instanceof EventUpdate) {
			if (mc.getGameSettings().keyBindUseItem.isKeyDown() && fastEXP.get() && mc.player.getHeldItemMainhand().getItem() == Items.EXPERIENCE_BOTTLE) {
				for (int i = 0; i < 8; i++) {
					mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
					mc.player.swingArm(Hand.MAIN_HAND);
				}
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
