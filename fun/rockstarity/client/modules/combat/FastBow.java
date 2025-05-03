package fun.rockstarity.client.modules.combat;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Slider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;


@Info(name="FastBow", desc="Автоматически спамит луком или арбалетом", type=Category.COMBAT)
public class FastBow extends Module {
	
	private final Select targets = new Select(this, "Предмет").desc("Предметы на которые будет действовать модуль");
	
	private final Select.Element bow = new Select.Element(targets, "Лук").set(true);
	private final Select.Element crossbow = new Select.Element(targets, "Арбалет").set(true);
	
	private final Slider range = new Slider(this, "Задержка").min(4).max(30).inc(1f).set(4f).desc("Задержка выстрела");
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventUpdate) {
			if(bow.get()) {
				useItem(Items.BOW);
			}
			if(crossbow.get()) {
				useItem(Items.CROSSBOW);
			}
		}
	}
	
	private void useItem(Item items) {
		int ticks = (int) (range.get() + MathUtility.random(-2, 2));
		if(mc.player.getActiveItemStack().getItem() == items && mc.player.isHandActive() && mc.player.getItemInUseMaxCount() >=	 ticks) {
			mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, mc.player.getHorizontalFacing()));
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            mc.player.stopActiveHand();
		}
	}
	
	@Override
	public void onDisable() {

	}

	@Override
	@NativeInclude
	public void onEnable() {
		
	}
	
}
