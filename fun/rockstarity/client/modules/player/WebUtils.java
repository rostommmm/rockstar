package fun.rockstarity.client.modules.player;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.helpers.player.Inventory;
import fun.rockstarity.api.helpers.player.Move;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Select.Element;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.client.modules.combat.Aura;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;

/**
 * @author Malecharik
 * @since 15 Mar 2024 16:44:50
 */


@Info(name = "WebUtils", desc = "Ускоряет скорость в паутине", type = Category.PLAYER)
public class WebUtils extends Module {
	private final Select util = new Select(this, "Выбор");
	private final Element hightJump = new Element(util, "Высокий прыжок");
	private final Element player = new Element(util, "Муровать таргета");
	private final Element boostSpeed = new Element(util, "Ускоряться").set(true);
	private final Element fly = new Element(util, "Fly Y");
	private final Slider flySpeed = new Slider(fly, "Скорость полета").min(0.1f).max(0.5f).inc(0.05f).set(0.4f).desc("Позволяет выбрать скорость полета вверх");
	
	private final Slider speed = new Slider(this, "Скорость").min(0.25f).max(5f).inc(0.5f).set(0.25f).hide(() -> !this.boostSpeed.get());
	private final Slider motion = new Slider(this, "Сила прыжка").min(0.50f).max(5f).inc(0.50f).set(1f).hide(() -> !this.hightJump.get());
	
	private final Mode mode = new Mode(this, "Форма").hide(() -> !this.player.get());
	private final Mode.Element place = new Mode.Element(mode, "Коробки");
	private final Mode.Element penis = new Mode.Element(mode, "Пениса");
	
	/*
	 * Обрабатываем событие EventMotion
	 */
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventMotion) {
			Aura aura = rock.getModules().get(Aura.class);
			if (Player.isInWeb()) { //Проверяем находится ли игрок находится в паутине
				if (this.boostSpeed.get()) Move.setSpeed(this.speed.get()); //Если елемент ускоряться включен, ускоряем игрока
				if (this.hightJump.get()) mc.player.getMotion().y += this.motion.get(); //Если елемент высокий прыжок включен, добавляем к y заданный слайдер
			}
			
            if (this.player.get() && aura.getTarget() != null && !Player.targetIsInWeb()) {
                int i = Inventory.findItem(45, Items.COBWEB);

                if (i != -1) {
                    BlockPos targetPos = aura.getTarget().getPosition();

                    if (this.mode.is(penis)) {
                        BlockPos[] positions = {
                            targetPos.add(0, 1, 0),
                            targetPos.add(1, 0, 0),
                            targetPos.add(-1, 0, 0),
                            targetPos.add(0, 2, 0),
                        };
                        
                        this.place(i, positions);

                    } else if (this.mode.is(place)) {
                        BlockPos[] positions = {
                            targetPos.add(0, 1, 0),
                            targetPos.add(0, 0, 0),
                            targetPos.add(0, -1, 0),
                            targetPos.add(1, 0, 0),
                            targetPos.add(1, 0, -1),
                            targetPos.add(-1, 0, 0),
                            targetPos.add(0, 0, 1),
                            targetPos.add(0, 0, -1)
                        };

                        this.place(i, positions);
                    }
                }
            }
            
            /*
            if (aura.getTarget() != null) {
                BlockPos targetPos = aura.getTarget().getPosition();

                int i = Inventory.findItem(45, Items.SWEET_BERRIES);
                place(i, new BlockPos[] {targetPos});
            }
            */
        }

		if (util.is(fly)) {
			if (Player.isInWeb()) {
				mc.player.getMotion().y += flySpeed.get();
			}
		}
	}
	
	private void place(int i, BlockPos[] positions) {
        for (BlockPos pos : positions) {
            if (mc.world.isAirBlock(pos)) {
				if (i == 40) {
					BlockRayTraceResult result = new BlockRayTraceResult(pos.down().getVec().add(0.5, 0.5, 0.5), Direction.UP, pos.down(), false);
					mc.player.connection.sendPacket(new CPlayerTryUseItemOnBlockPacket(Hand.OFF_HAND, result));
					mc.playerController.func_217292_a(mc.player, mc.world, Hand.OFF_HAND, result);
				
				} else if (i >= 36) {
					mc.player.connection.sendPacket(new CHeldItemChangePacket(i-36));
					BlockRayTraceResult result = new BlockRayTraceResult(pos.down().getVec().add(0.5, 0.5, 0.5), Direction.UP, pos.down(), false);
					mc.player.connection.sendPacket(new CPlayerTryUseItemOnBlockPacket(Hand.MAIN_HAND, result));
					mc.playerController.func_217292_a(mc.player, mc.world, Hand.MAIN_HAND, result);
					mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
				
				} else {
					mc.playerController.windowClick(0, i, 0, ClickType.PICKUP, mc.player);
					mc.playerController.windowClick(0, 44, 0, ClickType.PICKUP, mc.player);
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
