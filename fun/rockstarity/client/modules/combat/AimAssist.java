package fun.rockstarity.client.modules.combat;

import fun.rockstarity.api.ClientInfo;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventTick;
import fun.rockstarity.api.events.list.player.EventJump;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventMove;
import fun.rockstarity.api.events.list.player.EventTrace;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.math.VectorUtility;
import fun.rockstarity.api.helpers.math.aura.AuraUtility;
import fun.rockstarity.api.helpers.math.aura.Rotation;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.helpers.render.ColorUtility;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.client.modules.other.Globals;
import javafx.animation.Interpolator;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

@Info(name = "AimAssist", desc = "Помогает в аиме при пвп на ближнем оружии", type = Category.COMBAT)
public class AimAssist extends Module {
    private final Select targets = new Select(this, "Цели").desc("Цели на которых будет наводиться игрок");

    private final Select.Element players = new Select.Element(targets, "Игроки").set(true);
    private final Select.Element invisibles = new Select.Element(targets, "Невидимые").set(true).hide(() -> !this.players.get());
    private final Select.Element naked = new Select.Element(targets, "Голые").set(true).hide(() -> !this.players.get());
    private final Select.Element bots = new Select.Element(targets, "Боты").hide(() -> !this.players.get());
    private final Select.Element mobs = new Select.Element(targets, "Мобы");
    private final Select.Element rockUser = new Select.Element(targets, "Пользователи " + ClientInfo.NAME).hide(() -> !rock.getModules().get(Globals.class).get());
    private final CheckBox saveTarget = new CheckBox(this, "Сохранять цель").set(true);

    private final Slider fov = new Slider(this, "Поле зрения").min(1).max(180).set(90).inc(1).desc("Поле зрения на которой будет работать наводка");
    private final CheckBox fovCircle = new CheckBox(this, "Круг FOV'a").hide(() -> this.fov.get() > 90).desc("Рендерит круг поле зрения");
    private final CheckBox silent = new CheckBox(this, "Свободный аим").desc("Делает вашу камеру свободной");
    private final CheckBox lock = new CheckBox(this, "Фиксировать").hide(silent::get).desc("Фиксирует камеру на противнике");
    private final CheckBox rayAim = new CheckBox(this, "Только доводка").desc("Не наводит прицел на игрока, если уже навелся");

    private final CheckBox verticalAim = new CheckBox(this, "Наводка по вертикали").desc("Включает или отключает вертикальную наводку").set(true);

    private final Slider speed = new Slider(this, "Скорость").min(1).max(10).inc(0.1f).set(2);

    private float yaw, pitch;
    @Getter private LivingEntity target = null;
    Animation anim = new Animation().setSpeed(170).setSize(1).setEasing(Easing.EASE_IN_OUT_QUART);

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventTick) {
            // Вычисляем таргета
            LivingEntity target = AuraUtility.calculateTarget(mc.player.getPositionVec(), 4.0, this.players.get(), this.mobs.get(), this.invisibles.get(), this.naked.get(), this.bots.get(), false, rockUser.get(), true, false, false, false, false);
            anim.setForward(!MathUtility.rayTraceWithBlock(4, yaw, pitch, mc.player, target, false));

            if (((target == null || this.target == null || !mc.world.getAllEntities().contains(this.target)
                    || this.target.isDead()) && !saveTarget.get()) || (this.target == null && saveTarget.get())) {
                this.target = target;
            }

            if (target == null || Player.getFOV(target) > fov.get()) { // Если нет цели или фов до цели больше, чем в слайдере
                yaw = mc.player.rotationYaw;
                pitch = mc.player.rotationPitch;
                return; // Завершаем процесс
            }

            if (!silent.get() && !lock.get()) { // Если выключена Фиксация и Сайлент наводка, сохраняем yaw и pitch
                yaw = mc.player.rotationYaw;
                pitch = mc.player.rotationPitch;
            }

            Vector3d pos = VectorUtility.getBestVector(target, 0);

            float shortestYawPath = (float) ((((((Math.toDegrees(Math.atan2(pos.z, pos.x)) - 90) - yaw) % 360) + 540) % 360) - 180);
            float targetYaw = yaw + shortestYawPath;
            float targetPitch = (float) -Math.toDegrees(Math.atan2(pos.y, Math.hypot(pos.x, pos.z)));

            yaw = AuraUtility.fixDeltaNonVanillaMouse(yaw, pitch).x;
            pitch = AuraUtility.fixDeltaNonVanillaMouse(yaw, pitch).y;

            yaw = (float) Interpolator.LINEAR.interpolate(yaw, targetYaw, getAIRotationSpeed() * (rayAim.get() ? anim.get() : 1));
            pitch = !verticalAim.get() ? mc.player.rotationPitch : (float) Interpolator.LINEAR.interpolate(pitch,
                    targetPitch, MathUtility.random(0.04, 0.05) / Math.max((float) Minecraft.debugFPS, 5) * 75 * (rayAim.get() ? anim.get() : 1));

            Vector2f correctedRotation = Rotation.correctRotation(yaw, pitch);
            yaw = correctedRotation.x;
            pitch = correctedRotation.y;

            if (!silent.get()) {
                mc.player.rotationYaw = yaw;
                mc.player.rotationPitch = pitch;
            }
        }

        Style color = Style.current; // Цвет из темы

        if (event instanceof EventRender2D && fovCircle.get() && fov.get() <= 90) {
            Render.drawCircle((float) sr.getScaledWidth() / 2, (float) sr.getScaledHeight() / 2, 0, 360, fov.get() * 2, 1, false, color.getCurrent().getMain().getRGB()); // Рисуем круг в центре экрана и с радиусом заданный через слайдер (fov.get())
        }

        if (silent.get()) { // Если Сайлент наводка включена
            if (event instanceof EventTrace eventTrace) { // "Поворачиваем" голову на цель, чтобы при ударе в воздух ударялась цель
                eventTrace.setYaw(yaw);
                eventTrace.setPitch(pitch);
                eventTrace.cancel();
            }

            if (event instanceof EventMotion eventMotion) { // Отправляем пакеты о том, что мы повернулись + визуально поворачиваем голову(от ф5)
                eventMotion.setYaw(yaw);
                eventMotion.setPitch(pitch);
                mc.player.rotationYawHead = yaw;
                mc.player.rotationPitchHead = pitch;
                mc.player.renderYawOffset = AuraUtility.calculateCorrectYawOffset(yaw);
            }

            // Дальше идут фиксы движения чтобы ач ниче не заподозрил
            if (event instanceof EventMove eventMoveFix) {
                eventMoveFix.setYaw(yaw);
                eventMoveFix.setPitch(pitch);
            }

            if (event instanceof EventJump eventJump) {
                eventJump.setYaw(yaw);
            }
        }
    }

    private float getAIRotationSpeed() {
        return speed.get() / MathUtility.random(28, 35) / Math.max((float) Minecraft.debugFPS, 5) * 75;
    }

    @Override
    public void onDisable() {
        target = null;
    }

    @Override
    public void onEnable() {

    }
}