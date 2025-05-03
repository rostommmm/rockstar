package fun.rockstarity.client.modules.move;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.player.Move;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Slider;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

@Info(name = "Jesus", desc = "Позваоляет ходить по поверхностям жидкостей", type = Category.MOVE)
public class Jesus extends Module {
	Mode jesusMode = new Mode(this, "Режим");
	Mode.Element def = new Mode.Element(jesusMode, "Обычный");
	Mode.Element grimOld = new Mode.Element(jesusMode, "Grim Old");
	Slider speed = new Slider(grimOld, "Скорость").min(0.5f).max(1.5f).set(1.f).inc(0.05f);
	Mode.Element meta = new Mode.Element(jesusMode, "MetaHvH/AnACI");

	@Override
	@NativeInclude
	public void onEvent(Event event) {
		if (event instanceof EventUpdate) {
			if (jesusMode.is(def)) {
				if (mc.player.isInWater() || mc.player.isInLava()) {
					mc.player.getMotion().y = 0.011;

					if (Move.isMoving()) {
						if (Move.getSpeed() < 1.0f) {
							mc.player.getMotion().x *= 0.7 * 1.74;
							mc.player.getMotion().z *= 0.7 * 1.74;
						}
					}
				}
			} else if (jesusMode.is(meta)) {
				if (mc.player.isInWater() || mc.player.isInLava()) {
					EffectInstance speedEffect = mc.player.getActivePotionEffect(Effects.SPEED);
					EffectInstance DeEffect = mc.player.getActivePotionEffect(Effects.SLOWNESS);
					float appliedSpeed = getAppliedSpeed(speedEffect, DeEffect);

					Move.setSpeed(appliedSpeed);

					boolean isMoving = mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown()
							|| mc.gameSettings.keyBindLeft.isKeyDown() || mc.gameSettings.keyBindRight.isKeyDown();

					if (!isMoving) {
						mc.player.getMotion().x = 0.0;
						mc.player.getMotion().z = 0.0;
					}

					mc.player.getMotion().y = mc.gameSettings.keyBindJump.isKeyDown() ? 0.019 : 0.003;
				}
			} else {
				mc.player.setVelocity(
						mc.player.getMotion().x * speed.get(),
						0.01D,
						mc.player.getMotion().z * speed.get()
				);

				mc.player.setVelocity(
						mc.player.getMotion().x * speed.get(),
						0.00D,
						mc.player.getMotion().z * speed.get()
				);

				mc.player.setVelocity(
						mc.player.getMotion().x * speed.get(),
						-0.1D,
						mc.player.getMotion().z * speed.get()
				);

				mc.player.setVelocity(
						mc.player.getMotion().x * speed.get(),
						0.035D,
						mc.player.getMotion().z * speed.get()
				);

				mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
				mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.STOP_SPRINTING));
			}
		}
	}

	private static float getAppliedSpeed(EffectInstance speedEffect, EffectInstance DeEffect) {
		ItemStack offHandItem = mc.player.getHeldItemOffhand();
		String itemName = offHandItem.getDisplayName().getString();
		float appliedSpeed = 0F;

		if (itemName.contains("Ломтик Дыни") && speedEffect != null && speedEffect.getAmplifier() == 2) {
			appliedSpeed = 0.4283F * 1.15F;
		} else {
			if (speedEffect != null) {
				if (speedEffect.getAmplifier() == 2) appliedSpeed = 0.44f * 1.15F;
				else if (speedEffect.getAmplifier() == 1) appliedSpeed = 0.44f;
			} else appliedSpeed = 0.44f * 0.68F;
		}

		if (DeEffect != null) appliedSpeed *= 0.85f;

		return appliedSpeed;
	}

	@Override
	public void onEnable() {
	}
	
	@Override
	public void onDisable() {
	}
}
