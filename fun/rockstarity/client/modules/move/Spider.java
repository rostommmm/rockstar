package fun.rockstarity.client.modules.move;

import baritone.api.utils.Rotation;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventTrace;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.player.Inventory;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.secure.Debugger;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author Malecharik
 * @since 15 Mar 2024 13:31:22
 */


@Info(name = "Spider", desc = "Позволяет забраться на стены", type = Category.MOVE)
public class Spider extends Module {
	/**
	 * Старые значения, которые мы запоминаем при включении модуля и используем при
	 * его выключении
	 */
	@Getter
	@Setter
	private int oldRightClickTimer, oldPitch;

	private boolean wasPressed;

	/**
	 * питч, который будет установлен при включении модуля
	 */
	private float pitch = 82;
	private final Mode mode = new Mode(this, "Выбор");
	private final Mode.Element matrix = new Mode.Element(mode, "Matrix");
	private final Mode.Element grim = new Mode.Element(mode, "Grim");
	private final Mode.Element ftBlocks = new Mode.Element(mode, "FunTime (Неполн. блоки)");
	private final Mode.Element ftTraps = new Mode.Element(mode, "FunTime (Трапки)");
	private final Mode.Element spooky = new Mode.Element(mode, "Spooky");

	private final Slider speedMatrix = new Slider(this, "Скорость").min(1).max(20).inc(1).set(6)
			.hide(() -> !this.mode.is(this.matrix));

	private final TimerUtility timer = new TimerUtility();
	private int ticks;
	
	@Override
	@NativeInclude
	public void onEvent(Event event) {
		
		if (spooky.get()) {
			//if (!mc.player.collidedHorizontally) return;
	    	

            try {
            	boolean useSlime = false;
            	if (Player.isLookEvent(event) || event instanceof EventUpdate) {

                	for (int i = 0; i < 7; i++) {
                		if (Player.getBlock(1, i, 0) == Blocks.AIR) {
                			if (i == 6)
                				useSlime = true;
                		} else {
                			break;
                		}
                	}
                	
                	for (int i = 0; i < 7; i++) {
                		if (Player.getBlock(0, i, 1) == Blocks.AIR) {
                			if (i == 6)
                				useSlime = true;
                		} else {
                			break;
                		}
                	}
                	
                	for (int i = 0; i < 7; i++) {
                		if (Player.getBlock(-1, i, 0) == Blocks.AIR) {
                			if (i == 6)
                				useSlime = true;
                		} else {
                			break;
                		}
                	}
                	
                	for (int i = 0; i < 7; i++) {
                		if (Player.getBlock(0, i, -1) == Blocks.AIR) {
                			if (i == 6)
                				useSlime = true;
                		} else {
                			break;
                		}
                	}
                	
                	Player.look(event, mc.player.rotationYaw, useSlime ? 51 : 82, true);
            	}
            	
            	if (event instanceof EventUpdate && mc.getObjectMouseOver() instanceof BlockRayTraceResult obj) {
    	    		//mc.timer.timerSpeed = 1.1f;
    	    		//mc.player.connection.sendPacketSilent(new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), mc.player.isOnGround()));
    	    		//mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
    	    		//mc.setRightClickDelayTimer(0);
    	           // mc.rightClickMouse();
    	        	
                	Direction dir = Direction.UP;
                	Direction dir1 = Direction.fromAngle(mc.player.rotationYaw-180);
    	    		BlockPos pos = obj.getPos();
    	    		
    	    		if (mc.world.getBlock(pos) != Blocks.SLIME_BLOCK) {
    	    			pos = pos.add(dir1.getDirectionVec());
    	    		}

                	// orig =  §r(-1386.9702829863338, 110.0, 492.5236505086448), up, Mutable{x=-1387, y=109, z=492}, false
                	// fake = §r(-1386.5, 111.0, 492.5), up, BlockPos{x=-1387, y=109, z=492}, true
                	if (mc.gameSettings.keyBindJump.isKeyDown() && useSlime) {
                    	mc.player.getMotion().y = 0.6f;
                	}
                	
                	if (Player.findItem(Items.SLIME_BLOCK) != -1)
	    				mc.player.inventory.currentItem = Player.findItem(Items.SLIME_BLOCK);
                	
	                //mc.player.connection.sendPacket(new CPlayerTryUseItemOnBlockPacket(Hand.MAIN_HAND, obj = new BlockRayTraceResult(pos.getVec().add(0.5, 0.5f, 1), dir1, pos, true)));
                	mc.playerController.func_217292_a(mc.player, mc.world, Hand.MAIN_HAND, obj);

    	    	}
            } catch (Exception e) {
			}
		}
		
		if (ftTraps.get()) {
			if (event instanceof EventMotion e) {
				if (mc.player.collidedHorizontally) {
					if (mc.player.isOnGround() || timer.passed(300)) {
						mc.player.getMotion().y = 0.42f; // 42 братуха
						if (mc.player.isOnGround()) timer.reset();
					}
				}
			}
		}
		
		if (this.mode.is(this.ftBlocks)) {
			if (event instanceof EventMotion e) {
				Vector3d pos = mc.player.getPositionVec().add(0, 1.5f, 0);
				AxisAlignedBB hitbox = mc.player.getBoundingBox();
				
				float off = -0.1f;
				//mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.AIR
				boolean isAir = mc.world.getBlockState(new BlockPos(hitbox.minX-off, pos.y, hitbox.minZ-off)).getBlock() != Blocks.AIR
					|| mc.world.getBlockState(new BlockPos(hitbox.maxX+off, pos.y, hitbox.minZ-off)).getBlock() != Blocks.AIR
					|| mc.world.getBlockState(new BlockPos(hitbox.minX-off, pos.y, hitbox.maxZ+off)).getBlock() != Blocks.AIR
					|| mc.world.getBlockState(new BlockPos(hitbox.maxX+off, pos.y, hitbox.maxZ+off)).getBlock() != Blocks.AIR;
				
				if (isAir && !mc.player.isInWater()) {
					mc.getGameSettings().keyBindSneak.setPressed(false);
					if (mc.getGameSettings().keyBindJump.isKeyDown()) {
						mc.player.getMotion().y = 0.6f;
					}
					e.setGround(true);
				}
			}
		}
		
		if (this.mode.is(this.grim)) {
			if (event instanceof EventUpdate && Player.getBlock(0, 2, 0) == Blocks.AIR) {
	            mc.rightClickMouse();

					if (mc.player.isOnGround()) {
						mc.player.jump();
					}
				}
			
			if (event instanceof EventMotion e) {
				e.setPitch(this.pitch);
				mc.player.rotationPitchHead = this.pitch;
			}

			if (event instanceof EventTrace e) {
				e.setPitch(this.pitch);
				e.cancel();
			}
		}

		/*
		 * Обрабатываем мод Matrix
		 */
		if (this.mode.is(this.matrix) && event instanceof EventMotion e) {
				if (!mc.player.collidedHorizontally)
					return; // Если игрок не коллизится поторяем проверку

				// Высчитываем задержку
				if (this.timer.passed(MathHelper.clamp(500 - ((long) this.speedMatrix.get() / 2 * 100), 0, 1000))) {
					// Если определенное время прошло выполняем действия
					mc.player.jump();
					mc.player.getMotion().y = 0.42f;
					mc.player.collidedVertically = true;
					mc.player.collidedHorizontally = true;
					timer.reset();
					e.setGround(true);
				}
			}
		
	}

	@Override
	public void onDisable() {
		if (this.mode.is(this.grim)) {
			mc.setRightClickDelayTimer(getOldRightClickTimer());
			mc.getGameSettings().keyBindUseItem.setPressed(this.wasPressed);
		}
	}

	@Override
	public void onEnable() {
		if (this.mode.is(this.grim)) {
			setOldRightClickTimer(mc.getRightClickDelayTimer());
			this.wasPressed = mc.getGameSettings().keyBindUseItem.isKeyDown();
		}
		/*
		if (spooky.get()) {
			int slime = Player.findItem(Items.SLIME_BLOCK);
			if (slime == -1) {
    			Chat.msg("Положите слайм-блоки в инвентарь");
    			set(false);
    			return;
    		}
		}
		*/
	}
}
