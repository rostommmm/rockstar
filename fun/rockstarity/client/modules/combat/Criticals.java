package fun.rockstarity.client.modules.combat;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.EventType;
import fun.rockstarity.api.events.list.player.EventAttack;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.player.Bypass;
import fun.rockstarity.api.helpers.player.Move;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import fun.rockstarity.api.render.ui.alerts.Tooltip;
import fun.rockstarity.api.via.ViaLoadingBase;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author ConeTin
 * @since 4 авг. 2024 г.
 */

/**
 * Холиворлд
 * 
 * @author ConeTin
 * @since 9 авг. 2024 г.
 */

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Info(name="Criticals", desc="Бьет критами на земле", type=Category.COMBAT)
public class Criticals extends Module {
	
	Mode mode = new Mode(this, "Режим");

	Mode.Element vanila = new Mode.Element(mode, "Обычные");
	Mode.Element ncp = new Mode.Element(mode, "NCP");
	Mode.Element funtime = new Mode.Element(mode, "RWCollision");
	Mode.Element grim = new Mode.Element(mode, "Grim");
	Mode.Element spooky = new Mode.Element(mode, "SpookyTime");
	
	@Override
	@EventType({EventAttack.class})
	public void onEvent(Event event) {
		if (event instanceof EventUpdate && mc.player.ticksExisted > 100) {
			if ((mode.is(grim) || mode.is(spooky)) && !Bypass.via()) {
			 	rock.getAlertHandler().alert(Tooltip.create("Этот Criticals работает только с VIA 1.17+"), AlertType.INFO);
			}
		}
		
		if (event instanceof EventAttack e) {
			EventMotion eventMotion = new EventMotion(mc.player.rotationYaw, mc.player.rotationPitch, mc.player.isOnGround()).hook();
			Vector3d pos = mc.player.getPositionVec();
			
			if (!canCritical()) return;
			
			if (mode.is(grim)) {
				if (!mc.player.isOnGround()) {
					if (Bypass.via()) {
						mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(pos.x, pos.y - 1e-6, pos.z, eventMotion.getYaw(), eventMotion.getPitch(), false));
						mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(pos.x, pos.y + 1e-6, pos.z, eventMotion.getYaw(), eventMotion.getPitch(), false));
						mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(pos.x, pos.y, pos.z, eventMotion.getYaw(), eventMotion.getPitch(), false));
					}
				}
			} else if (mode.is(funtime)) {
				if (mc.player.hurtTime == 0) {
					mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(pos.x, pos.y + 1e-6, pos.z, eventMotion.getYaw(), eventMotion.getPitch(), true));
					mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(pos.x, pos.y, pos.z, eventMotion.getYaw(), eventMotion.getPitch(), false));
				}
			} else if (mode.is(spooky)) {
				if (Bypass.via()) {
					if (!mc.player.isOnGround()) {
						mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(pos.x, pos.y - 1e-6, pos.z, eventMotion.getYaw(), eventMotion.getPitch(), false));
					} else {
						//mc.player.setMotion(Vector3d.ZERO);
						mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(pos.x, pos.y + 1e-6, pos.z, eventMotion.getYaw(), eventMotion.getPitch(), false));
						mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(pos.x, pos.y, pos.z, eventMotion.getYaw(), eventMotion.getPitch(), false));
					}
				}
				return;
			} else if (mode.is(vanila)) {
				if (!mc.player.isOnGround()) {
					mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(pos.x, pos.y - 1e-6, pos.z, eventMotion.getYaw(), eventMotion.getPitch(), false));
				} else {
					mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(pos.x, pos.y + 1e-6, pos.z, eventMotion.getYaw(), eventMotion.getPitch(), true));
					mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(pos.x, pos.y, pos.z, eventMotion.getYaw(), eventMotion.getPitch(), false));
				}
				return;
			} else {
				mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(pos.x, pos.y + 0.000000271875,
						pos.z, eventMotion.getYaw(), eventMotion.getPitch(), false));
				mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(pos.x, pos.y,
						pos.z, eventMotion.getYaw(), eventMotion.getPitch(), false));
				return;
			}
			
			mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
		}
	}
	
	public boolean canCritical() {
		return this.get() && ((mode.is(funtime) && mc.player.isOnGround()) || mode.is(vanila) || (Bypass.via() && mode.is(spooky)) || (!mc.player.isOnGround() && mode.is(grim) && Bypass.via()));
	}
	
	@NativeInclude
	@Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}
	
}
