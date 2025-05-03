package fun.rockstarity.client.modules.move;

import java.util.concurrent.ThreadLocalRandom;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.player.Move;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Slider;
import lombok.Getter;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author Malecharik
 * @since 13 Mar 2024 14:00:39
 */
@NativeInclude

@Info(name = "WaterSpeed", desc = "Ускоряет в воде", type = Category.MOVE)
public class WaterSpeed extends Module {
	@Getter
	private final Mode mode = new Mode(this, "Режим");
	
	private final Mode.Element defaults = new Mode.Element(mode, "Matrix");
	@Getter private final Mode.Element grim = new Mode.Element(mode, "Grim");
	private final Mode.Element funtime = new Mode.Element(mode, "FunTime");
	private final Mode.Element intave = new Mode.Element(mode, "Intave");
	
	private final Slider speed = new Slider(this, "Скорость").min(0.10f).max(5f).inc(0.1f).set(1f).hide(() -> !this.mode.is(this.defaults));

	private final CheckBox potion = new CheckBox(this, "Работать с зельем").hide(() -> !mode.is(defaults));
	
	private final TimerUtility debug = new TimerUtility();
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventMotion) {
			if (mode.is(defaults)) handleDefaultMode();
			if (mode.is(intave) && mc.player.hurtTime > 0 && mc.player.isInWater()) {
				mc.player.setMotion(mc.player.getMotion().mul(1.02, 1, 1.02));
			}

			if (mode.is(funtime)) {
				if (mc.player.hurtTime > 0 || !mc.player.isInWater()) return;
				mc.player.setMotion(mc.player.getMotion().mul(1.011, 1, 1.011));

				EffectInstance speedEffect = mc.player.getActivePotionEffect(Effects.SPEED);
				if (speedEffect != null) {
					mc.player.setMotion(mc.player.getMotion().mul(1.0 + 0.11f * 1.155F, 1, 1.0 + 0.11f * 1.155F));
				}
			}
		}
	}

	private void handleDefaultMode() {
	    if (!mc.player.isPotionActive(Effects.SPEED) && potion.get() || !mc.player.isInWater()) return;
	    Move.setSpeed(speed.get());
	    mc.player.getMotion().y += (mc.player.ticksExisted % 2 == 0)? -0.001 : 0.01;
	}
	
	@Override
	public void onDisable() {
		debug.reset();
	}

	@Override
	public void onEnable() {
		
	}
	
}
