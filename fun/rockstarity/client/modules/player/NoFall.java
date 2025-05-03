package fun.rockstarity.client.modules.player;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventWorldChange;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import fun.rockstarity.api.render.ui.alerts.Tooltip;
import fun.rockstarity.api.via.ViaLoadingBase;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.client.CEntityActionPacket.Action;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;

/**
 * @author Malecharik
 * @since 24 Mar 2024 13:40:36
 */


@Info(name="NoFall", desc="Убирает урон от падения", type=Category.PLAYER)
public class NoFall extends Module {
	
	Mode mode = new Mode(this, "Режим");

	Mode.Element vanilla = new Mode.Element(mode, "Обычный");
	Mode.Element grimOld = new Mode.Element(mode, "Grim Old");
	Mode.Element funtime = new Mode.Element(mode, "FunTime");
	Mode.Element holyworld = new Mode.Element(mode, "HolyWorld");
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventUpdate && mc.player.ticksExisted > 100) {
			// Уведомление если что-то не работает на HolyWorld
			if (Server.isHW() && mode.is(holyworld) && !ViaLoadingBase.getInstance().getTargetVersion().isNewerThanOrEqualTo(ProtocolVersion.v1_17)) {
			 	rock.getAlertHandler().alert(Tooltip.create("Этот NoFall работает только с VIA 1.17+"), AlertType.INFO);
			}
		}
		
		if (event instanceof EventMotion e) {
			if (mode.is(vanilla)) {
				e.setGround(true);
			}
		}
		
		if (event instanceof EventUpdate) {
			if (mode.is(funtime)) {
				if (mc.player.fallDistance > 2) {
					mc.player.getMotion().y = 0.005;
				}
			} else if (mode.is(holyworld)) {
				if (ViaLoadingBase.getInstance().getTargetVersion().isNewerThanOrEqualTo(ProtocolVersion.v1_17)) {
					if (mc.player.fallDistance > 2.5) {
						Vector3d pos = mc.player.getPositionVec();
						EventMotion eventMotion = new EventMotion(mc.player.rotationYaw, mc.player.rotationPitch, mc.player.isOnGround()).hook();
						mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(pos.x, pos.y + 1e-7, pos.z, eventMotion.getYaw(), eventMotion.getPitch(), false));
	                    mc.player.fallDistance = 0;
	                }
				}
			} else if (mode.is(grimOld)) {
				if (mc.player.fallDistance > 2.5) {
					mc.player.connection.sendPacket(
							new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), true));
					mc.player.getMotion().y -= 0.91231425;
				}
			}
		}
		
		if (event instanceof EventWorldChange && mode.is(funtime)) {
			this.set(false);
		}
	}
	
	@Override
	public void onDisable() {
		
	}

	@Override
	public void onEnable() {
		
	}
	
}
