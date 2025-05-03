package fun.rockstarity.client.modules.combat;

import static net.minecraft.client.settings.KeyBinding.setKeyBindState;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.inputs.EventInput;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import fun.rockstarity.api.secure.Debugger;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.network.play.server.SExplosionPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author ConeTin
 * @since 6 дек. 2023 г.
 */

@Info(name = "Velocity", desc = "Убирает откидывание", type = Category.COMBAT)
public class Velocity extends Module {

	private final Mode mode = new Mode(this, "Режим").desc("Позволяет замедлить/отклонить отбрасывание после удара по вам.");

	private final Mode.Element cancel = new Mode.Element(mode, "Обычный");
	private final Mode.Element compensation = new Mode.Element(mode, "Компенсация");
	private final Mode.Element intave = new Mode.Element(mode, "Intave");
	private final Mode.Element universalGrim = new Mode.Element(mode, "Grim");

	private final Mode grimMode = new Mode(universalGrim, "Режим");
	private final Mode.Element old = new Mode.Element(grimMode, "Old");
	private final Mode.Element latest = new Mode.Element(grimMode, "Latest");

	private final Slider countTo = new Slider(intave, "Сколько замедлять?").min(1).max(6).inc(1).set(4)
			.desc("Выбирает, сколько отбрасываний можно замедлить");
	private final Slider countPost = new Slider(intave, "Сколько пропускать?").min(2).max(10).inc(1).set(4)
			.desc("Выбирает, сколько отбрасываний нужно пропустить");
	private final CheckBox packet = new CheckBox(intave, "Пакетно замедлять");
	private final Slider slow = new Slider(packet, "Сила замедления (в -%)").min(0.01f).max(0.1f).inc(0.001f).set(0.03f);
	private final CheckBox targetFromJump = new CheckBox(intave, "Ускориться").desc("Позволит ускориться после получения урона на земле," +
			" и соответственно втаргетится в противника");
	private final CheckBox logging = new CheckBox(intave, "Логировать").desc("Позволяет логировать все отталкивания");
	private final Mode logMode = new Mode(logging, "Кого логировать?");
	private final Mode.Element packetLog = new Mode.Element(logMode, "Пакетное замедление");
	private final Mode.Element defLog = new Mode.Element(logMode, "Обычное замедление");
	private final Slider speedable = new Slider(targetFromJump, "Сила").min(0.1f).max(2).inc(0.1f).set(0.5f);

	private final CheckBox offPostFlagged = new CheckBox(this, "Вырубить после флага");
	private final Mode compMode = new Mode(this, "Тип компенсации").hide(() -> !mode.is(compensation));;
	private final Mode.Element defaultComp = new Mode.Element(compMode, "Обычная");
	private final CheckBox jumper = new CheckBox(defaultComp, "Прыгать на земле");
	private final Mode.Element damageAngle = new Mode.Element(compMode, "По направлению урона");
	private final CheckBox onlyPlayer = new CheckBox(this, "Только от игроков").hide(() -> !mode.is(compensation) && !compMode.is(damageAngle));
	private final CheckBox noNether = new CheckBox(this, "Не работать если незерка").hide(() -> !mode.is(compensation) && !compMode.is(damageAngle));
	// Вектор последнего движения и флаг получения пакета
	private Vector3d lastMotion = Vector3d.ZERO;
	private boolean packetReceived;
	private final TimerUtility flagTimer = new TimerUtility();
	private int logic = 0;
	private boolean gotVelo;
	private boolean prev;
	float count = 0;
	float countLog = 0;

	@Override
	public void onEvent(Event event) {
        if (noNether.get() && isNether()) {
            return;
        }
		
		if (this.mode.is(this.cancel)) {
			if (event instanceof EventReceivePacket e) {
				IPacket<?> packet = e.getPacket();
				if (packet instanceof SEntityVelocityPacket velocityPacket) {
					if (velocityPacket.getEntityID() == mc.player.getEntityId()){
						event.cancel();
					}
				}
			}
		} else if (this.mode.is(this.compensation) && compMode.is(damageAngle)) {
			if (event instanceof EventReceivePacket e) {
				if (e.getPacket() instanceof SEntityVelocityPacket packet) {
					if (packet.getEntityID() == mc.player.getEntityId()) {
						if (!onlyPlayer.get() || isDamagePlayer()) {
							lastMotion = new Vector3d(packet.motionX, packet.motionY, packet.motionZ);
							this.applyVelocityAction();
						}
					}
				} else if (e.getPacket() instanceof SExplosionPacket) {
					if (!onlyPlayer.get() || isDamagePlayer()) {
						applyVelocityAction();
					}
		        }
			}
			
			if (event instanceof EventUpdate) transform();
		}

		if (event instanceof EventInput e && mode.is(compensation) && (!onlyPlayer.get() || isDamagePlayer())) {
			if (compMode.is(damageAngle)) {
				if (prev) {
					float direction = MathHelper.wrapDegrees((mc.player.rotationYaw) - MathUtility.calculate(mc.player.getPositionVec().add(lastMotion)).x);
					if (mc.player.isOnGround()) e.setJump(true);
					if (direction > 120 || direction < -120) e.setForward(1);
					if (direction > -150 && direction < -60) e.setStrafe(1);
					if (direction > -60 && direction < 60) e.setForward(-1);
					if (direction > 60 && direction < 150) e.setStrafe(-1);
				}
			} else {
				if (mc.player.hurtTime >= 9) count = count + 1;
				if (count > 4 || (mc.player.isOnGround() && !mc.gameSettings.keyBindJump.isKeyDown())) count = 0;

				if (canCancelVelocity()) {
					float forwardFunc = 1.0F;

					if (!mc.player.isOnGround()) e.setForward(forwardFunc);
					if (MathUtility.isBlockUnder(0.01f) && mc.player.isOnGround()) e.setForward(1.0f);

					if (mc.player.isOnGround()) {
						if (mc.player.hurtTime >= 9 && !mc.gameSettings.keyBindJump.isKeyDown()) {
							if (jumper.get()) setKeyBindState(mc.gameSettings.keyBindJump.getDefault(), true);
							e.setForward(forwardFunc);
						}

						if (mc.player.hurtTime == 8) e.setSneak(true);
					}

					if (mc.player.hurtTime < 7) e.setSneak(false);
				} else {
					if (!mc.gameSettings.keyBindSneak.isKeyDown()) e.setSneak(false);
				}
			}

			if (mc.gameSettings.keyBindSneak.isKeyDown()) {
				setKeyBindState(mc.gameSettings.keyBindSneak.getDefault(), true);
			}
		}

		if (mode.is(universalGrim) && canCancelVelocity()) {
			if (event instanceof EventInput e) {
				if (!mc.player.isOnGround()) e.setForward(1);
				if (MathUtility.isBlockUnder(0.01f) && mc.player.isOnGround()) e.setForward(1);

				if (mc.player.getMotion().y > 0 && mc.player.getMotion().y < 0.1f
						|| mc.player.fallDistance > 0.3f && mc.player.fallDistance < 0.4f) {
					float value = grimMode.is(old) ? 0.8f :
							isNether() ? 0.87f : 0.96f;
					if (!grimMode.is(latest) || mc.player.ticksExisted % 3 == 0) {
						mc.player.getMotion().x *= value;
						mc.player.getMotion().z *= value;
					}
				}

				float factorValue = grimMode.is(old) ? 0.8f + (float) mc.player.hurtTime / 40 :
				isNether() ? 0.85f + (float) mc.player.hurtTime / 75 : 1;
				mc.player.jumpMovementFactor *= MathHelper.clamp(factorValue, 0.8f, 1);

				if (mc.player.isOnGround()) {
					setKeyBindState(mc.gameSettings.keyBindJump.getDefault(), true);
					e.setSneak(true);
					if (mc.player.hurtTime < 7) e.setSneak(false);
				}
			}
		}

		if (mode.is(intave) && canCancelVelocity()) {
			if (event instanceof EventInput e) {
				if (mc.player.hurtTime == 9) count += 1;
				if (count >= 0 && count <= countTo.get()) {
					if (!mc.player.isOnGround()) e.setForward(1);
					if (MathUtility.isBlockUnder(0.01f) && mc.player.isOnGround()) e.setForward(1);

					if (packet.get()) {
						for (int i = 0; i < 10; i++) {
							mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.PRESS_SHIFT_KEY));
						}
						mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.RELEASE_SHIFT_KEY));

						mc.player.getMotion().x *= 1 - slow.get();
						mc.player.getMotion().z *= 1 - slow.get();

						if (count <= countTo.get() && canCancelVelocity() && mc.player.ticksExisted % 8 == 0 && logging.get() && logMode.is(packetLog))
							Chat.msg(count + " velocity count packet slowed");
					}

					if (mc.player.getMotion().y > 0 && mc.player.getMotion().y < 0.1f
							|| mc.player.fallDistance > 0.3f && mc.player.fallDistance < 0.4f) {
						mc.player.getMotion().x *= 0.83f;
						mc.player.getMotion().z *= 0.83f;
					}

					if (mc.player.isOnGround()) {
						setKeyBindState(mc.gameSettings.keyBindJump.getDefault(), true);
						if (mc.player.hurtTime < 7) e.setSneak(false);
					}

					if (targetFromJump.get() && mc.gameSettings.keyBindJump.isKeyDown()) {
						mc.player.jumpMovementFactor *= 1 + speedable.get();
					}
				}

				if (logging.get()) {
					if (count > countTo.get() && count < countTo.get() + 2) count = countTo.get() + 2;
					if (count > countTo.get() + countPost.get()) count = 0;

					if (count <= countTo.get() && canCancelVelocity() && mc.player.ticksExisted % 8 == 0 && logging.get() && logMode.is(defLog))
						Chat.msg(count + " velocity count slowed");

					if (count >= countTo.get() + 2 && mc.player.ticksExisted % 8 == 0 && logging.get())
						Chat.msg("number " + count + " velocity skipped");
				}
			}
		}

		if (event instanceof EventReceivePacket e && offPostFlagged.get()) {
			if (mc.player.hurtTime == 9) countLog += 1;

			if (e.getPacket() instanceof SPlayerPositionLookPacket) {
				rock.getAlertHandler().alert("Обнаружен флаг. Выключаю велосити" + (rock.isDebugging() ? ", last velocity registered: " + (int) countLog : ""), AlertType.ERROR);
				set(false);
				onDisable();
			}
		}
	}

	@NativeInclude
    public void transform() {
        if (mc.player.hurtTime > 0 || gotVelo) {
           prev = true;
        }
        if (mc.player.hurtTime == 0) {
        	gotVelo = false;
            if (prev) {
               prev = false;
            }
        }
    }

	@NativeInclude
	private void applyVelocityAction() {
		mc.player.jump();
    	prev = true;
        gotVelo = true;
	}
	
	private boolean isDamagePlayer() {
		DamageSource damageSource = mc.player.getLastDamageSource();
		
		if (damageSource != null) {
			return damageSource.getTrueSource() instanceof PlayerEntity;
		}
		
		return false;
	}
	
	private boolean isNether() {
	    for (ItemStack armor : mc.player.getArmorInventoryList()) {
	        if (armor != null && armor.getItem() instanceof ArmorItem) {
	            ArmorItem armorItem = (ArmorItem) armor.getItem();
	            if (armorItem.getArmorMaterial() == ArmorMaterial.NETHERITE) {
	                return true;
	            }
	        }
	    }
	    return false;
	}

	private boolean canCancelVelocity() {
		return mc.player.hurtTime > 0 && !mc.player.isInWater() && !mc.player.isInLava() && !mc.player.isElytraFlying();
	}
	
	@Override
	public void onDisable() {
		countLog = 0;
	}

	@Override
	public void onEnable() {
		
	}
	
}
