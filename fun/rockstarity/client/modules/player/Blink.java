package fun.rockstarity.client.modules.player;

import java.util.ArrayList;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.Reacher;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.packet.EventSendPacket;
import fun.rockstarity.api.events.list.render.world.EventRenderWorldEntities;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import fun.rockstarity.api.render.ui.alerts.Tooltip;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CUseEntityPacket;
import net.minecraft.util.math.vector.Vector3d;

/**
 * @author Malecharik
 * @since 12 мая 2024 г. 13:46:07
 */


@Info(name = "Blink", desc = "Замораживает вас для сервера", type = Category.PLAYER)
public class Blink extends Module {
	private final ArrayList<IPacket<?>> packets = new ArrayList<>();
	private final TimerUtility timer = new TimerUtility();

	private final CheckBox pulse = new CheckBox(this, "Пульсация");
	private final Slider pulseTimer = new Slider(this, "Время").min(1).max(20).set(5).inc(1).hide(() -> !pulse.get());
	private final CheckBox entity = new CheckBox(this, "Отображение");
	private final CheckBox svo = new CheckBox(this, "Не показывать от первого").hide(() -> !entity.get());
	private MatrixStack matrixStack;
	private Vector3d lastPos;
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventSendPacket e) {

			packets.add(e.getPacket());
			event.cancel();
			
			if (timer.passed((long) pulseTimer.get() * 110) && pulse.get()) {
				onDisable();
				onEnable();
				timer.reset();
			}
		}
		
		if (event instanceof EventRenderWorldEntities e && entity.get() && lastPos != null && (mc.getGameSettings().getPointOfView() != PointOfView.FIRST_PERSON || !svo.get())) {
			Render.drawEntity3D(e.getMatrix(), mc.player, lastPos, 1);
		}
	}
	
	@Override
	public void onDisable() {
		if (!mc.isSingleplayer()) {
			for (IPacket<?> p : packets) {
				mc.player.connection.sendPacketSilent(p);
			}
		}
		packets.clear();
		lastPos = null;
	}

	@Override
	public void onEnable() {
//		RemoteClientPlayerEntity fake = new RemoteClientPlayerEntity(mc.world,mc.player.getGameProfile());
//		Vector3d pos = mc.player.getPositionVec();
//		if (this.entity.get()) {
//			fake.inventory = mc.player.inventory;
//			fake.setHealth(mc.player.getHealth());
//			fake.setPositionAndRotation(pos.x, pos.y, pos.z, mc.player.rotationYaw, mc.player.rotationPitch);
//		    fake.rotationYawHead = mc.player.rotationYawHead;
//		    mc.world.addEntity(-13_37, fake);
//		}
		lastPos = mc.player.getPositionVec();
	    if (Server.isFT() && this.pulse.get() && this.pulseTimer.get() > 6) {
	    	rock.getAlertHandler().alert(Tooltip.create("Оптимальное значение под FunTime - 6"), AlertType.INFO);
	    }
	}
}
