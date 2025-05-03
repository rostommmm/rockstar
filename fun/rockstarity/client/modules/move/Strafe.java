package fun.rockstarity.client.modules.move;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.inputs.EventInput;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.player.EventAction;
import fun.rockstarity.api.events.list.player.EventDamageReceive;
import fun.rockstarity.api.events.list.player.EventJump;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventMotionMove;
import fun.rockstarity.api.events.list.player.EventMove;
import fun.rockstarity.api.events.list.player.EventPostMotionMove;
import fun.rockstarity.api.events.list.player.EventTrace;
import fun.rockstarity.api.helpers.math.aura.Rotation;
import fun.rockstarity.api.helpers.player.DamagePlayerUtil;
import fun.rockstarity.api.helpers.player.MoveUtils;
import fun.rockstarity.api.helpers.player.StrafeMovement;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Slider;
import lombok.Getter;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoulSandBlock;
import net.minecraft.block.material.Material;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

import java.awt.event.ActionEvent;

/**
 * @author ConeTin
 * @since 2 дек. 2023 г.
 */


@Info(name="Strafe", desc="Позволяет двигаться во все стороны с одинаковой скоростью", type=Category.MOVE)
public class Strafe extends Module {
	private final Mode mode = new Mode(this, "Режим");
	private final Mode.Element matrix = new Mode.Element(mode, "Matrix");
	private final Mode.Element univer = new Mode.Element(mode, "Универсальные");
	
	private final CheckBox damageBoost = new CheckBox(this, "Ускорение от урона").set(true).hide(() -> !mode.is(matrix));
	private final Slider boost = new Slider(this, "Сила ускорения").min(0.1f).max(5).inc(0.1f).set(0.7f).desc("Сила ускорения при получении урона").hide(() -> !damageBoost.get());

    private final DamagePlayerUtil damageUtil = new DamagePlayerUtil();
    private final StrafeMovement strafeMovement = new StrafeMovement();
	
	@Getter
	private float yaw;
	
	public Strafe() {
		super(0);
	}
	
	@Override
	public void onEvent(Event event) {
		if (this.mode.is(this.univer)) {
			this.handlePackets(event);
			// Направляем игрока в нужную сторону
			if (event instanceof EventInput e) {
				// Если яв игрока не изменился после того как он был на земле
				// То отжимаем клавишу "вперёд" и юзаем мувфикс ауры
				if (yaw == Rotation.getDirection()) {
					e.setForward((mc.player.movementInput.moveForward != 0F || mc.player.movementInput.moveStrafe != 0F) ? 1 : 0);
					e.setStrafe(0);
				} else {
					e.setYaw(yaw+mc.player.rotationYaw);
				}
			}
			
			// Используем директ мувфикс на яв движения
			if (event instanceof EventMove e) {
				e.setYaw(yaw+mc.player.rotationYaw);
				//e.setPitch(0);
			}
			
			if (event instanceof EventJump e) {
				e.setYaw(yaw+mc.player.rotationYaw);
			}
			
			if (event instanceof EventTrace e) {
				e.setYaw(yaw+mc.player.rotationYaw);
				e.cancel();
			}
			
			if (event instanceof EventMotion e) {
				// Если игрок на земле обновляем яв движения
				//if (mc.player.fallDistance <= 0)
					yaw = Rotation.getDirection()-mc.player.rotationYaw;
				// Заменяем яв игрока на яв движения при отправлении пакетов
				e.setYaw(yaw+mc.player.rotationYaw);
				//e.setPitch(0);
				//mc.player.renderYawOffset = Rotation.getBody();
				
				//mc.player.rotationYawHead = yaw+mc.player.rotationYaw;
			}
		} else {
            if (event instanceof EventAction e) handleEventAction(e);
            if (event instanceof EventMotionMove e) handleEventMove(e);
            if (event instanceof EventPostMotionMove e) handleEventPostMove(e);
            if (event instanceof EventReceivePacket e) handleEventPacket(e);
            if (event instanceof EventDamageReceive e) handleDamageEvent(e);
        }
	}

	@NativeInclude
	private void handlePackets(Event event) {
		// "NoServerRots"
		if (event instanceof EventReceivePacket e) {
			if(e.getPacket() instanceof SPlayerPositionLookPacket packet) {
				packet.setYaw(mc.player.rotationYaw);
				packet.setPitch(mc.player.rotationPitch);
			}
		}
	}

    private void handleDamageEvent(EventDamageReceive damage) {
        if (damageBoost.get()) {
            damageUtil.processDamage(damage);
        }
    }

    private void handleEventAction(EventAction action) {
        if (strafes()) handleStrafesEventAction(action);
        if (strafeMovement.isNeedSwap()) handleNeedSwapEventAction(action);
    }

    private void handleEventMove(EventMotionMove eventMove) {
        if (strafes()) {
            handleStrafesEventMove(eventMove);
        } else {
            strafeMovement.setOldSpeed(0);
        }
    }

    private void handleEventPostMove(EventPostMotionMove eventPostMove) {
        strafeMovement.postMove(eventPostMove.getSpeed());
    }

    private void handleEventPacket(EventReceivePacket packet) {
        if (damageBoost.get()) damageUtil.onPacketEvent(packet);
        handleReceivePacketEventPacket(packet);
    }

    private void handleStrafesEventAction(EventAction action) {
        if (CEntityActionPacket.lastUpdatedSprint != strafeMovement.isNeedSprintState()) {
            action.setSprintState(!CEntityActionPacket.lastUpdatedSprint);
        }
    }

    private void handleStrafesEventMove(EventMotionMove eventMove) {
        if (damageBoost.get()) this.damageUtil.time(700L);

        final float damageSpeed = boost.get() / 10.0F;
        final double speed = strafeMovement.calculateSpeed(eventMove, damageBoost.get(), damageUtil.isNormalDamage(), false, damageSpeed);

        MoveUtils.MoveEvent.setMoveMotion(eventMove, speed);
    }

    private void handleNeedSwapEventAction(EventAction action) {
        action.setSprintState(!mc.player.serverSprintState);
        strafeMovement.setNeedSwap(false);
    }

    private void handleReceivePacketEventPacket(EventReceivePacket packet) {
        if (packet.getPacket() instanceof SPlayerPositionLookPacket) strafeMovement.setOldSpeed(0);
    }

    public boolean strafes() {
        if (isInvalidPlayerState()) return false;
        BlockPos playerPosition = new BlockPos(mc.player.getPositionVec());
        BlockPos abovePosition = playerPosition.up();
        BlockPos belowPosition = playerPosition.down();
        if (isSurfaceLiquid(abovePosition, belowPosition)) return false;
        if (isPlayerInWebOrSoulSand(playerPosition)) return false;
        return isPlayerAbleToStrafe();
    }

    private boolean isInvalidPlayerState() {
        return mc.player == null || mc.world == null
                || mc.player.isSneaking()
                || mc.player.isElytraFlying()
                || mc.player.isInWater()
                || mc.player.isInLava();
    }

    private boolean isSurfaceLiquid(BlockPos abovePosition, BlockPos belowPosition) {
        Block aboveBlock = mc.world.getBlockState(abovePosition).getBlock();
        Block belowBlock = mc.world.getBlockState(belowPosition).getBlock();
        return aboveBlock instanceof AirBlock && belowBlock == Blocks.WATER;
    }

    private boolean isPlayerInWebOrSoulSand(BlockPos playerPosition) {
        Material playerMaterial = mc.world.getBlockState(playerPosition).getMaterial();
        Block oneBelowBlock = mc.world.getBlockState(playerPosition.down()).getBlock();
        return playerMaterial == Material.WEB || oneBelowBlock instanceof SoulSandBlock;
    }

    private boolean isPlayerAbleToStrafe() {
        return !mc.player.abilities.isFlying && !mc.player.isPotionActive(Effects.LEVITATION);
    }

    @Override
    public void onEnable() {
        strafeMovement.setOldSpeed(0);
    }

	@Override
	public void onDisable() {
		
	}
	
}
