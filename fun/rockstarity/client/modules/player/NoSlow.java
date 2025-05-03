package fun.rockstarity.client.modules.player;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.inputs.EventInput;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.player.EventSlow;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.player.Move;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.via.ViaLoadingBase;
import fun.rockstarity.client.modules.combat.Aura;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SHeldItemChangePacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

/**
 * @author ConeTin
 * @since 6 дек. 2023 г.
 */


@Info(name = "NoSlow", desc = "Убирает замедление у предметов", type = Category.PLAYER)
public class NoSlow extends Module {
	
	private final Mode mode = new Mode(this, "Режим");
	private final Mode.Element grim = new Mode.Element(mode, "Grim");
	private final Mode.Element rw = new Mode.Element(mode, "ReallyWorld");
	private final Mode.Element matrix = new Mode.Element(mode, "Matrix");
	private final Mode.Element tick = new Mode.Element(mode, "Motion");
	private final Mode.Element rwUpdated = new Mode.Element(mode, "GrimNew");
	
	private final CheckBox onlyLeft = new CheckBox(this, "Только левая").hide(() -> mode.is(matrix));

	private final Select work = new Select(this, "Работать...");
	private final Select.Element all = new Select.Element(work, "Всегда").set(true);
	private final Select.Element water = new Select.Element(work, "Только в воде").hide(all::get);
	private final Select.Element crossbow = new Select.Element(work, "Только с арбалетом").hide(all::get);
	private final Select.Element ground = new Select.Element(work, "Только на земле").hide(all::get);
	private final Select.Element snow = new Select.Element(work, "На снегу").hide(all::get);
	
	private final Mode rwMode = new Mode(this, "Режим rw").hide(()-> !mode.is(rw)).desc("Настройка для режима \"ReallyWorld\", Авто - Игрок автоматически останавливается, Динамический - Если игрок до этого стоял, NoSlow будет работать, если нет то нет");
	private final Mode.Element auto = new Mode.Element(rwMode, "Авто");
	private final Mode.Element manual = new Mode.Element(rwMode, "Динамический");
	
	private final TimerUtility waitTimer = new TimerUtility();
	private final TimerUtility stop = new TimerUtility();
	private boolean using;
	private boolean moving;
	int ticks = 0;
	
	@Override
	public void onEvent(Event event) {
		long waitTime = 100;
		
		if (event instanceof EventInput e && mode.is(rw) && rwMode.is(auto)) {
			if (!waitTimer.passed(waitTime)) {
				e.setForward(0);
				e.setStrafe(0);
			}
		}
		
		if (event instanceof EventReceivePacket e) {
			if (this.mode.is(matrix) || (mode.is(rw) && rwMode.is(manual) && !waitTimer.passed(waitTime))) return;
			if (e.getPacket() instanceof SHeldItemChangePacket) {
				event.cancel();
			}
		}

		if (event instanceof EventSlow) {
			if (mc.player != null && !mc.player.isElytraFlying() && mc.player.isHandActive()) {
					ticks += 1;
			} else {
				ticks = 0;
			}

			// боже ну и говнище ебаное
			boolean inWater = mc.player.isInWater() || mc.player.isInLava();
			
            if ((work.is(water) && !inWater
            		|| (work.is(crossbow) && mc.player.getHeldItemMainhand().getItem() != Items.CROSSBOW)
            		|| (work.is(snow) && Player.getBlock() != Blocks.SNOW)
            		|| work.is(ground) && (!mc.player.isOnGround() || Player.isBlockUnderWithMotion())
            		|| (mode.is(rw) && rwMode.is(manual) && !waitTimer.passed(waitTime)))
					&& !mode.is(tick) && !mode.is(rwUpdated)) return;
            
			if (this.mode.is(this.grim) || this.mode.is(this.rw)) {
				if ((mc.player.getHeldItemOffhand().getUseAction() == UseAction.BLOCK
						|| mc.player.getHeldItemOffhand().getUseAction() == UseAction.EAT)
						&& mc.player.getActiveHand() == Hand.MAIN_HAND)
					return;

				if (mc.player.getActiveHand() == Hand.MAIN_HAND) {
					if (this.onlyLeft.get())
						return;
					
					if (ViaLoadingBase.getInstance().getTargetVersion().isNewerThanOrEqualTo(ProtocolVersion.v1_17)) {
		            	mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), mc.player.rotationYaw, mc.player.rotationPitch, mc.player.isOnGround()));
		            }
					mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
					event.cancel();
					return;
				}
				if (Server.isRW() && rock.getModules().get(Aura.class).getTarget() != null) return;
				if (mc.player.getActiveHand() != Hand.MAIN_HAND) {
					if (ViaLoadingBase.getInstance().getTargetVersion().isNewerThanOrEqualTo(ProtocolVersion.v1_17)) {
		            	mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), mc.player.rotationYaw, mc.player.rotationPitch, mc.player.isOnGround()));
		            }
					mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
				}
				event.cancel();
			} else {
				if (!mode.is(tick) && !mode.is(rwUpdated)) {
					if (!mc.player.isSprinting() && mc.player.isHandActive()) mc.player.setSprinting(true);
					event.cancel(); //Отменяем евент для Matrix мода
				}
			}

			if (mc.player.isHandActive()) {
				boolean offHandActive = mc.player.isHandActive() && mc.player.getActiveHand() == Hand.OFF_HAND;
				boolean mainHandActive = mc.player.isHandActive() && mc.player.getActiveHand() == Hand.MAIN_HAND;
				if (mc.player.getFoodStats().getFoodLevel() < 6.0F || mc.player.collidedHorizontally || mc.player.moveForward == 0) return;

				if (mode.is(tick)) {
					if (mc.player.getItemInUseCount() > 25 && mc.player.getItemInUseCount() < 5) return;
					if (mc.player.isHandActive() && !mc.player.isPassenger()) {
						mc.playerController.syncCurrentPlayItem();
						if (offHandActive && !mc.player.getCooldownTracker().hasCooldown(mc.player.getHeldItemOffhand().getItem())) {
							mc.player.jumpMovementFactor *= 1.2f;

							if (stop.passed(25)) {
								mc.player.setMotion(mc.player.getMotion().mul(1.00078, 1, 1.00078));
							}
						}

						if (mainHandActive && !mc.player.getCooldownTracker().hasCooldown(mc.player.getHeldItemMainhand().getItem())) {
							if (mc.player.getHeldItemOffhand().getUseAction().equals(UseAction.NONE)) {
								mc.player.jumpMovementFactor *= 1.2f;

								if (stop.passed(25)) {
									mc.player.setMotion(mc.player.getMotion().mul(1.00078, 1, 1.00078));
								}
							}
						}
						mc.playerController.syncCurrentPlayItem();
					}
				} else if (mode.is(rwUpdated)) {
					// что бы ты спросил
					if (mc.player.getItemInUseCount() >= 25) return;

					if (stop.passed(80)) {
						if (mc.player.isHandActive() && !mc.player.isPassenger()) {
							event.cancel();
							stop.reset();
						}
					}
				}
			}
		}

		if (event instanceof EventUpdate) {
			if (mc.player.isHandActive())
				mc.player.setSprinting(true); // TODO бля разобраться с ним
			
			if (mode.is(rw) && rwMode.is(auto)) {
				if (mc.player.isHandActive()) {
					if (!using) {
						waitTimer.reset();
						using = true;
					}
				} else {
					using = false;
				}
			} else {
				//Chat.debug(waitTimer.getElapsed());
				if (Move.isMoving()) {
					moving = true;
					if (!waitTimer.passed(waitTime) || !mc.player.isHandActive())
						waitTimer.reset();
					
					if (waitTimer.passed(waitTime))
						return;
				} else {
					if (moving) {
						waitTimer.reset();
						moving = false;
					}
				}
			}
			if (this.mode.is(matrix)) return;
			if (Server.isRW() && !mc.player.isSprinting() && mc.player.isHandActive()) mc.player.setSprinting(true);
			if (!mc.player.getCooldownTracker().hasCooldown(mc.player.getHeldItemMainhand().getItem())
					&& !mc.player.getCooldownTracker().hasCooldown(mc.player.getHeldItemOffhand().getItem())) {
				if (mc.player.isHandActive() && mc.player.fallDistance < 1) {
					if (mc.player.getActiveHand() == Hand.OFF_HAND) {
						mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem % 8 + 1));
						mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
					}
				}
			}
		}
	}
	
	@Override
	public void onDisable() {
		ticks = 0;
	}

	@Override
	public void onEnable() {
		ticks = 0;
	}
}
