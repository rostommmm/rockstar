package fun.rockstarity.client.modules.combat;

import java.util.Comparator;

import com.viaversion.viaversion.velocity.providers.VelocityVersionProvider;
import fun.rockstarity.api.ClientInfo;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.EventType;
import fun.rockstarity.api.events.list.game.EventDamage;
import fun.rockstarity.api.events.list.game.EventTick;
import fun.rockstarity.api.events.list.game.inputs.EventInput;
import fun.rockstarity.api.events.list.game.packet.EventSendPacket;
import fun.rockstarity.api.events.list.player.EventJump;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventMotionMove;
import fun.rockstarity.api.events.list.player.EventMove;
import fun.rockstarity.api.events.list.player.EventPostMotion;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.DamageUtility;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.math.VectorUtility;
import fun.rockstarity.api.helpers.math.aura.AuraUtility;
import fun.rockstarity.api.helpers.math.aura.IdealHitUtility;
import fun.rockstarity.api.helpers.math.aura.Rotation;
import fun.rockstarity.api.helpers.player.Bypass;
import fun.rockstarity.api.helpers.player.FallingPlayer;
import fun.rockstarity.api.helpers.player.Move;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Select.Element;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.animation.infinity.RotationAnimation;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import fun.rockstarity.api.render.ui.alerts.Tooltip;
import fun.rockstarity.client.modules.move.AutoSprint;
import fun.rockstarity.client.modules.other.Globals;
import fun.rockstarity.client.modules.player.AutoPearl;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.item.UseAction;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CEntityActionPacket.Action;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

import static net.minecraft.util.math.MathHelper.wrapDegrees;

/**
 * Я когда-нибудь уйду
 * И сурсы эти солью
 * Долбаёбы не поймут
 * Каково быть на виду
 * 
 * Я когда-нибудь уйду (Я, я, я, я-я)
 * И сурсы эти солью (Все сурсы солью)
 * Долбаёбы не поймут (Не понять, никогда)
 * Каково быть на виду (Это так, это так)
 * Я когда-нибудь уйду (Я, я, я, я-я)
 * И сурсы эти солью (Все сурсы солью)
 * Долбаёбы не поймут (Не понять, никогда)
 * Каково быть на виду (Это так, это так)
 * Я когда нибудь уйду...
 * 
 * Каждый класс, каждый метод - новое кино (Go)
 * Кто мне друг? Кто мне враг? Думаю, никто (Шоу)
 * Пастинг, кубы-игра - тупо шапито (Цирк)
 * Конетинка в этом цирке - самый главный клоун (Клоун)
 * Я мечтал, что буду популярен (Эй, ха, а-а)
 * Я всё еще мечтаю...
 * Я заработал только мешки под глазами
 * А чё мне дали эти кубы?
 * Нихуя не дали
 * "Ты же этого всю жизнь хотел
 * Ну а хули ты теперь тут ноешь?"
 * - Конечно, ною, я же пастерок
 * Я недоволен
 * 
 * Я когда-нибудь уйду (Я, я, я, я-я)
 * И сурсы эти солью (Все сурсы солью)
 * Долбаёбы не поймут (Не понять, никогда)
 * Каково быть на виду (Это так, это так)
 * Я когда-нибудь уйду (Я, я, я, я-я)
 * И сурсы эти солью (Все сурсы солью)
 * Долбаёбы не поймут (Не понять, никогда)
 * Каково быть на виду (Это так, это так)
 * Я когда нибудь уйду...
 */

/**
 * @author ConeTin
 * @since 5 дек. 2023 г.
 */

@FieldDefaults(level = AccessLevel.PROTECTED)
@Info(name="Aura", desc="Бьёт женщин и детей", type=Category.COMBAT, module={"KillAura", "AttackAura", "HitAura"})
public class AuraOld extends Module {
	
	final Mode mode = new Mode(this, "Режим");
	final Mode.Element classic = new Mode.Element(mode, "Классический");
	//final Mode.Element funtime = new Mode.Element(mode, "FunTime2");
	final Mode.Element funtime = new Mode.Element(mode, "FunTime");
	final Mode.Element snap = new Mode.Element(mode, "Снап");
	@Getter final Mode.Element spooky = new Mode.Element(mode, "Spooky");
	
	@Getter final CheckBox adaptDistance = new CheckBox(spooky, "Фикс дистанции");
	final Slider spookySpeed = new Slider(spooky, "Добавочная скорость").min(-0.5f).max(0.5f).inc(0.05f).set(-0.15f).desc("Добавочная скорость наводки на цель");
	final Slider animSpeed = new Slider(spooky, "Скорость анимации").min(3).max(15).inc(1).set(8).desc("Скорость анимации для наводки. Чем больше - тем медленнее");

	//final CheckBox adaptYaw = new CheckBox(spooky, "Адаптивный Yaw");
	//final CheckBox freezeYaw = new CheckBox(spooky, "Замораживать Yaw");
	
	final CheckBox airCrits = new CheckBox(this, "Воздушные криты").desc("Зависает в воздухе, позволяя бить на дистанции 6 блоков");
	
	@Getter final CheckBox onlyCrits = new CheckBox(this, "Только криты").set(true).desc("Aura будет бить только тогда, когда может нанести критический удар").hide(() -> airCrits.get());
	final Slider range = new Slider(this, "Дистанция").min(2).max(6).inc(0.1f).set(3.0f).desc("Дистанция, на которой Aura может ударить").hide(() -> airCrits.get());
	final Slider rangaTip = new Slider(this, "Дистанция наводки").min(0f).max(2).inc(0.1f).set(0.5f).desc("Дистанция, на которой Aura будет искать цель. Плюсуется к обычной дистанции");
	final Select targets = new Select(this, "Цели").min(1).desc("Сущности, которых будет бить Aura");
	
	final Select.Element players = new Select.Element(targets, "Игроки").set(true);
	final Select.Element invisibles = new Select.Element(targets, "Невидимые").set(true).hide(() -> !this.players.get());
	final Select.Element naked = new Select.Element(targets, "Голые").set(true).hide(() -> !this.players.get());
	final Select.Element friends = new Select.Element(targets, "Друзья").hide(() -> !this.players.get());
	final Select.Element bots = new Select.Element(targets, "Боты").hide(() -> !this.players.get());
	final Select.Element mobs = new Select.Element(targets, "Мобы");
	final Select.Element rockUser = new Select.Element(targets, "Пользователи " + ClientInfo.NAME).hide(() -> !rock.getModules().get(Globals.class).get());

	final Mode correction  = new Mode(this, "Коррекция движения").desc("Корректировка движений для обхода некоторых античитов");
	final Mode.Element no = new Mode.Element(correction, "Нет");
	final Mode.Element focused = new Mode.Element(correction, "Сфокусированная");
	final Mode.Element silent = new Mode.Element(correction, "Незаметная");
	
	@Getter final CheckBox notifBrack = new CheckBox(this, "Увед. о ломании щита").set(true).desc("Выводить уведомление при ломании щита сопернику");
	
	final CheckBox raycast = new CheckBox(this, "Проверка наводки").set(true).desc("Aura будет бить только тогда, когда навелась на соперника").hide(() -> airCrits.get());
	
	final Select additions = new Select(this, "Дополнения").desc("Различные дополнения для Aura");
	
	final Element onlyWeapon = new Element(additions, "Только с оружием");
	@Getter final Element walls = new Element(additions, "Через стены").set(true);
	final Element notEat = new Element(additions, "Не бить если ешь");
	final Element tickSelect = new Element(additions, "Умный выбор тика");
	final Element attackOnGround = new Element(additions, "Бить на земле").set(true);

	final Mode sort = new Mode(this, "Сортировка");
	final Mode.Element fov = new Mode.Element(sort, "По полю зрения");
	final Mode.Element distance = new Mode.Element(sort, "По дистанции");
	final Mode.Element health = new Mode.Element(sort, "По здоровью");
	
	final Mode sprint = new Mode(this, "Сброс спринта");
	final Mode.Element legit = new Mode.Element(sprint, "Легитный");
	final Mode.Element classicSprint = new Mode.Element(sprint, "Обычный");
	final Mode.Element none = new Mode.Element(sprint, "Нет");
	final Mode.Element always = new Mode.Element(sprint, "Постоянно");
	final Mode.Element superlegit = new Mode.Element(sprint, "Универсальный");

	final Mode boost = new Mode(this, "Ускорение");
	final Mode.Element notBoost = new Mode.Element(boost, "Нет");
	final Mode.Element direct = new Mode.Element(boost, "Прямо");
	final Mode.Element toTarget = new Mode.Element(boost, "На цель").ifEnabled(true);
	

	final Mode boostMode = new Mode(toTarget, "Режим");
	final Mode.Element funtimeBoost = new Mode.Element(boostMode, "FunTime");
	final Mode.Element spookyBoost = new Mode.Element(boostMode, "Spooky");
	final Mode.Element custom = new Mode.Element(boostMode, "Свой");
	
	final Slider fallingSpeed = new Slider(toTarget, "Скорость падения").min(0).max(0.4f).inc(0.05f).set(0.3f).desc("Скорость, c которой игрок будет ускоряться падая").hide(() -> funtimeBoost.get() || spookyBoost.get());
	final Slider jumpSpeed = new Slider(toTarget, "Скорость прыжка").min(0).max(0.4f).inc(0.05f).set(0.3f).desc("Скорость, c которой игрок будет ускоряться прыгая").hide(() -> funtimeBoost.get() || spookyBoost.get());
	final Slider groundSpeed = new Slider(toTarget, "Скорость на земле").min(0).max(0.4f).inc(0.05f).set(0).desc("Скорость, c которой игрок будет ускоряться на земле").hide(() -> funtimeBoost.get() || spookyBoost.get());
	final Slider centrifugalForce = new Slider(toTarget, "Центробежная сила").min(0).max(0.3f).inc(0.05f).set(0).desc("Отклонение от центра для вращения").hide(() -> funtimeBoost.get() || spookyBoost.get());
	final Slider multiplier = new Slider(toTarget, "Множитель").min(-0.2f).max(3).inc(0.05f).set(-0.1f).desc("Множитель ускорения. Для FunTime желательно ставить значения до 0, для SpookyTime можно ставить значения вполть до 3. Если не знаете, что ставить под конкретно ваш сервер, оствьте 0 или -0.1").hide(() -> false);
	
	final CheckBox pres = new CheckBox(this, "Преследование").desc("Не меняет цель даже если появилась другая, более приоритетная цель");
	
	final Slider speed = new Slider(this, "Скорость").min(170).max(470).inc(10).set(370).desc("Скорость, на которой Aura будет наводится").hide(() -> !mode.is(funtime));
	
	@Setter CheckBox hitlogger;

	@Getter @Setter Vector3f rotation = Vector3f.ZERO;
	@Getter final RotationAnimation rotAnim = new RotationAnimation();
	@Getter final RotationAnimation spookyAnim = new RotationAnimation();
	@Getter final RotationAnimation rotAnimAdv = new RotationAnimation();
	@Getter final RotationAnimation fakeAnim = new RotationAnimation();
	
	@Getter @Setter
	LivingEntity target, prevTarget;
	
	boolean requireCritical;
	@Getter final TimerUtility attackTimer = new TimerUtility();
	final TimerUtility disableTimer = new TimerUtility();
	final TimerUtility swapDirectionYawTimer = new TimerUtility();
	boolean directionYaw;
	@Getter int attacks;
	
	// Для логгера
	boolean logged;
	float prevTargetHealth;
	int hitCounter;
	
	// Для снапов
	boolean attacked;
	int rotationTicks = -1;
	
	int aimTicks;
	
	double[] air = null;
	boolean disabled, firstHit;
	
	int hitsUnderBlocks;

	Animation anim = new Animation().setSpeed(170).setSize(1).setEasing(Easing.EASE_IN_OUT_QUART);
	Animation anim1 = new Animation().setSpeed(170).setSize(1).setEasing(Easing.EASE_IN_OUT_QUART);
	Animation anim3 = new Animation().setSpeed(170).setSize(1).setEasing(Easing.EASE_IN_OUT_QUART);
	Animation anim4 = new Animation().setSpeed(170).setSize(1).setEasing(Easing.EASE_IN_OUT_QUART);
	
	public AuraOld() {
		super(10);
	}
	
	@Override
	public void onAllEvent(Event event) {
		/*
		if (event instanceof EventRender3D e && this.prevTarget != null && MultiPoints.getBestPoint(this.prevTarget) != null) {
			MatrixStack ms = e.getMatrixStack();
			
			Render.startImageRendering();
			
			float miniSize = 0.3f;

			for (Vector3d vec : MultiPoints.getPoints()) {
				if (!MathUtility.canSeen(vec.add(this.prevTarget.getPositionVec()))) continue;
				
				ms.push();
				Vector3d pos = vec.add(this.prevTarget.getPositionVec()).sub(mc.getRenderManager().info.getProjectedView());
				
				ms.translate(pos.x, pos.y, pos.z);
				ms.rotate(mc.getRenderManager().info.getRotation());
				
	            Render.drawCleanImage(ms, "masks/glow.png", (float) -miniSize / 2, -miniSize / 2, (float) -miniSize / 2, miniSize, miniSize, Style.getPoint(3));
	            
	            ms.pop();
			}
			
			Render.finishImageRendering();
		}
		*/
		
		if (event instanceof EventUpdate && mc.player.ticksExisted > 50) {
			// Уведомление если что-то не работает на HolyWorld
			if (Server.isHW()) {
				String server = "HolyWorld";
			 	if (walls.get()) rock.getAlertHandler().alert(Tooltip.create("\"Через стены\" не работает на " + server), AlertType.INFO);
			 	if (!raycast.get()) rock.getAlertHandler().alert(Tooltip.create("\"Проверка наводки\" желательно включать на " + server), AlertType.INFO);
			}
			
			// Уведомление если что-то не работает на FunTime
			if (Server.isFT()) {
				String server = "FunTime";
				if (!raycast.get()) rock.getAlertHandler().alert(Tooltip.create("\"Проверка наводки\" желательно включать на " + server), AlertType.INFO);
			}
			
			// Уведомление если что-то не работает на ReallyWorld
			if (Server.isRW()) {
				String server = "ReallyWorld";
				if (!raycast.get()) rock.getAlertHandler().alert(Tooltip.create("\"Проверка наводки\" желательно включать на " + server), AlertType.INFO);
			}
		}
		
		if (((mode.is(funtime) && !disableTimer.passed(500)) || get()) && prevTarget != null) {
			if (event instanceof EventTick) {
				if (mode.is(spooky)) {
					updateAdvanced();
				} else {
					this.calculateRotation();
				}
			}
			
			if (event instanceof EventMotion e) {
				if (rock.getModules().get(AutoPearl.class).getTick() > 0) return;
				rotate(e); // Отправляем серверу данные о том, что наша голова наведена на цель
				if (mc.player.isOnGround()) {
					IdealHitUtility.setJumped(false);
				}
			}
			
			correctMovement(event); // Корректируем движения для того чтобы не детектил ач
		}
	}
	
	@Override
	@EventType({EventTick.class, EventMotion.class, EventPostMotion.class, EventUpdate.class})
	public void onEvent(Event event) {
		if (this.onlyWeapon.get() && !(mc.player.getHeldItemMainhand().getItem() instanceof SwordItem || mc.player.getHeldItemMainhand().getItem() instanceof AxeItem)) return;
		// Каждый тик процесса ищем цель и наводимся
		if (event instanceof EventUpdate) {
			this.findTarget(this.range() + this.getAdditionalRange());
		}
		
		if (event instanceof EventUpdate) {
			if (target == null || mc.player.isOnGround()) {
				rock.getModules().get(AutoSprint.class).setCanSprint(true);
			}
		}
		
		// Если цели нет, то код прерывается
		if (target == null) return;
		
		handlePackets(event);
		
		if (event instanceof EventMotion e) {
			if (rock.getModules().get(AutoPearl.class).getTick() > 0) return;
			rotate(e); // Отправляем серверу данные о том, что наша голова наведена на цель
			if (mc.player.isOnGround()) {
				IdealHitUtility.setJumped(false);
			}
		}
		
		Criticals criticals = rock.getModules().get(Criticals.class);
		if (event instanceof EventMotionMove e && criticals.get() && criticals.getMode().is(criticals.getFuntime()) && mc.player.isOnGround()) {
			float range = Server.isFT() ? 2.8f : this.range();
			boolean result = !this.raycast.get() || MathUtility.rayTraceWithBlock(7, this.rotation.x, this.rotation.y, mc.player, target, false);
			
			boolean canAttack = AuraUtility.distanceTo(AuraUtility.getPoint(this.target)) <= range &&
					mc.player.getCooledAttackStrength() >= IdealHitUtility.getAICooldown();
			
			if (canAttack || !attackTimer.passed(200L)) {
				e.setMotion(Vector3d.ZERO.add(0, e.getMotion().y, 0));
				Move.setSpeed(0);
			}
		}

		if (event instanceof EventDamage e && e.getTarget() == prevTarget) {
			prevTarget.lastHit.reset();
		}
		
		if (FallingPlayer.fromPlayer(mc.player).findFall(IdealHitUtility.getAIFallDistance()) && always.get()) {
			mc.getGameSettings().keyBindSprint.setPressed(false);
            mc.player.setSprinting(false);
            rock.getModules().get(AutoSprint.class).setCanSprint(false);
		}
		
		if (event instanceof EventUpdate) {
			if (canHit() && !mc.player.isOnGround() && (IdealHitUtility.canAIFall() && FallingPlayer.fromPlayer(mc.player).findFall(IdealHitUtility.getAIFallDistance()) || criticals.get()) && legit.get()) {
				//if (mc.player.isSprinting()) {
					mc.getGameSettings().keyBindSprint.setPressed(false);
		            mc.player.setSprinting(false);
		            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, Action.STOP_SPRINTING));
					mc.player.serverSprintState = false;
					rock.getModules().get(AutoSprint.class).setCanSprint(false);
		       // }
			}
			
			if (canHit() && !mc.player.isOnGround() && (IdealHitUtility.canAIFall() && (FallingPlayer.fromPlayer(mc.player).findFall(IdealHitUtility.getAIFallDistance(), 2) || canCritical()) || criticals.get()) && superlegit.get()) {
				//if (mc.player.isSprinting()) {
				mc.getGameSettings().keyBindForward.setPressed(false);
					mc.getGameSettings().keyBindSprint.setPressed(false);
		            mc.player.setSprinting(false);
		            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, Action.STOP_SPRINTING));
					mc.player.serverSprintState = false;
					rock.getModules().get(AutoSprint.class).setCanSprint(false);
		       // }
			}
		}
		
		if (event instanceof EventUpdate) {
			if (hitlogger != null && hitlogger.get() && this.attackTimer.passed(200L) && !this.logged) {
				float healthDiff = this.prevTargetHealth - (this.target.getHealth() + this.target.getAbsorptionAmount());
				float requiredDamage = DamageUtility.predictDamage(this.target);

				String result = "Hit passed (" + String.format("%.1f", healthDiff) + "hp)";
				if (this.prevTargetHealth <= this.target.getHealth() + this.target.getAbsorptionAmount()) {
					result = "Hit missed (" + hitCounter + ")";
				}
				
				Chat.debug(result);
				this.logged = true;
			}
			
			if (mc.player.isOnGround()) this.attacked = false;
			
			if (MathUtility.rayTraceWithBlock(6, rotation.x, rotation.y, mc.player, target, false)) {
				aimTicks++;
			} else {
				aimTicks = 0;
			}
			
			if (direct.get()) {
				double speed = Math.hypot(Math.abs(this.target.prevPosX - this.target.getPosX()), Math.abs(this.target.prevPosZ - this.target.getPosZ()));
				//double speed = Math.hypot(this.target.getMotion().x, this.target.getMotion().z);
				
				//if (mc.player.getDistance(this.target) < (Server.is("infinity") ? 2 : 1.5f) && (speed < 0.1f || Server.is("infinity"))) {
				if (Player.collideWith(this.target)) {
					float p = mc.world.getBlockState(mc.player.getPosition().add(mc.player.getMotion().x, mc.player.getMotion().y, mc.player.getMotion().z)).getBlock().getSlipperiness();
					float f = mc.player.isOnGround() ? p * 1 : (Server.is("infinity") ? 0.91f : 0.81f);
					float f2 = mc.player.isOnGround() ? p : 0.99f;
		            
					//if (mc.player.fallDistance > 0)
					//	mc.player.getMotion().y *= 1.1f;
					mc.player.setVelocity(mc.player.getMotion().getX() / f * f2, mc.player.getMotion().getY(), mc.player.getMotion().getZ() / f * f2);
				} else {
					if (mc.player.fallDistance > 0.5f && Move.getSpeed() == 0) {
						//mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.START_DESTROY_BLOCK, mc.player.getPosition(), Direction.UP));
			            //mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.STOP_DESTROY_BLOCK, mc.player.getPosition(), Direction.UP));
					//	mc.player.getMotion().y *= 2f;
					}
				}
			} else if (toTarget.get()) {
				if (Player.collideWith(target, multiplier.get()) /*mc.player.fallDistance > 0*/) {
				    // Получаем позицию игрока и цели
				    Vector3d playerPos = mc.player.getPositionVec();
				    Vector3d targetPos = target.getPositionVec();
				    
				    // Вычисляем вектор направления к цели
				    Vector3d direction = targetPos.subtract(playerPos).normalize();
				    
				    // Получаем коэффициенты трения (оригинальная логика)
				    float p = mc.world.getBlockState(mc.player.getPosition().add(mc.player.getMotion().x, mc.player.getMotion().y, mc.player.getMotion().z))
				            .getBlock().getSlipperiness();
				    float f = mc.player.isOnGround() ? p * 1 : (Server.is("infinity") ? 0.91f : 0.81f);
				    float f2 = mc.player.isOnGround() ? p : 0.99f;

				    // Сохраняем вертикальную скорость
				    double motionY = mc.player.getMotion().y;
				    
				    float ground = spookyBoost.get() ? 0.05f : funtimeBoost.get() ? 0 : groundSpeed.get();
				    float falling = spookyBoost.get() ? 0.05f : funtimeBoost.get() ? 0.3f : fallingSpeed.get();
				    float jump = spookyBoost.get() ? 0.05f : funtimeBoost.get() ? 0.2f : jumpSpeed.get();
				    
				    // Центробежная сила дада (космонавтики)
				    float gradus = System.currentTimeMillis() / 100;
				    float centrifugal = funtimeBoost.get() || spookyBoost.get() ? 0f : centrifugalForce.get();
				    float deviationX = (float) Math.cos(Math.toDegrees(gradus)) * centrifugal;
				    float deviationZ = (float) Math.sin(Math.toDegrees(gradus)) * centrifugal;
				    direction = direction.add(deviationX, 0, deviationZ);
				    
				    // Применяем направление к цели с учетом коэффициентов трения
				    double speed = mc.player.isOnGround() ? ground : mc.player.fallDistance > 0 ? falling : jump; // Настройте силу притяжения
				    double newX = direction.x * speed * f2 / f;
				    double newZ = direction.z * speed * f2 / f;
				    
				    // Устанавливаем новую скорость
				    mc.player.setVelocity(
				    		mc.player.getMotion().x
				    		+ newX, 
				    		motionY, 
				    		mc.player.getMotion().z
				    		+ newZ);
				}
			}
		}
		
		if (event instanceof EventUpdate) {
			if (!mode.is(snap) || canAttack() || this.rotationTicks >= -10)
				this.tryAttack();
		}
		
		if (this.target != null) this.prevTarget = this.target; // Оставляем предыдущую цель для использования в дальнейшем
	}
	
	private void rotate(EventMotion e) {
		if (mode.is(snap) && !Bypass.via()) {
			if (this.canAttack()) {
				this.rotationTicks += 1;
				if (this.attacked) return;
			} else {
				return;
			}
		}
		
		// Устанавливаем визуальную ротацию (Которую видит ток чел)
		if (!mc.player.isElytraFlying() && !mc.player.isSwimming() && target != null && !(snap.get() && Bypass.via())) {
			boolean rot360 = false;
			boolean realRotation =  //rock.getUser().getId() == 10 ||// rock.isDebugging() ||
					!mode.is(spooky) || !mode.is(funtime);
		/*
			if (!realRotation && (mode.is(funtime))) {
				Vector2f rot = realRotation ? new Vector2f(this.rotation.x,  this.rotation.y) : Rotation.get(this.prevTarget.getPositionVec().add(0,1,0));
				
				mc.player.renderYawOffset = !attackTimer.passed(1) ? rot.x : mc.player.rotationYaw; // Поворот тела
				mc.player.rotationYawHead = !attackTimer.passed(1) ? rot.x : mc.player.rotationYaw; // Поворот головы (влево-вправо)
				mc.player.rotationPitchHead = !attackTimer.passed(1) ? rot.y : mc.player.rotationPitch; // Поворот головы (вверх-вниз)
			} else {*/
				Vector2f rot = realRotation ? new Vector2f(this.rotation.x,  this.rotation.y) : new Vector2f(fakeAnim.getYaw(), fakeAnim.getPitch());
				
				//if (!realRotation)
				//	rot.x += MathUtility.random(-5, 5);
				
				mc.player.renderYawOffset = rot.x + (rot360 ? - 20 + 380 * mc.player.getCooledAttackStrength(0) : 0); // Поворот тела
				mc.player.rotationYawHead = rot.x + (rot360 ? - 20 + 380 * mc.player.getCooledAttackStrength(0) : 0); // Поворот головы (влево-вправо)
				mc.player.rotationPitchHead = rot.y; // Поворот головы (вверх-вниз)
			//}
		}
		
		if (snap.get() && Bypass.via() && attackTimer.passed(5)) {
			return;
		}

		// Устанавливаем серверную ротацию
		e.setYaw(rotation.x);  // Поворот головы (влево-вправо)
		e.setPitch(rotation.y); // Поворот головы (вверх-вниз)

		//mc.player.rotationYaw = rotation.x;
		//mc.player.rotationPitch = rotation.y;
		mc.player.renderYawOffset = rotation.z;
	}
	
	private void tryAttack() {
		if (mode.is(snap)) {
			if (Player.getBlock(0,2,0) == Blocks.AIR) {
				this.rotationTicks--;
			} else {
				this.rotationTicks = 0;
			}
		}
		
		if (!canAttack()) return; // Проверяем что игрок может атаковать
		
		rock.getModules().get(AutoSprint.class).setCanSprint(true);
		
		if (Server.isRW() && mc.player.getActiveHand() == Hand.OFF_HAND) {
		//	mc.getConnection().sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
		}

		// Криты на воде
		if ((mc.player.isInWater() || Player.getBlock(0,-0.2f,0) == Blocks.WATER) && Player.getBlock(0,1,0) == Blocks.AIR && mc.getGameSettings().keyBindJump.isKeyDown()) {
			if (mc.player.fallDistance == 0.0f) {
				return;
			}
		}
		
		if (Bypass.via() && snap.get() && !rock.getModules().get(Criticals.class).canCritical()) {
			Bypass.send(rotation.x, rotation.y);
		} else 
			if (air != null) mc.player.connection.sendPacket(new CPlayerPacket.RotationPacket(rotation.x, rotation.y, mc.player.isOnGround()));
		
		// Фикс удара через щит
		boolean blocking = mc.player.isHandActive() && mc.player.getActiveItemStack().getItem().getUseAction(mc.player.getActiveItemStack()) == UseAction.BLOCK;
		if (blocking) mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));
		
		if (((mc.player.isSprinting() || mc.player.serverSprintState) && !(mc.player.isInWater() || Player.getBlock(0,-0.2f,0) == Blocks.WATER)) && classicSprint.get()) { // Нужно для того чтобы были криты (В майнкрафте если бить во время спринта, то крита не будет)
			mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, Action.STOP_SPRINTING));
			mc.player.serverSprintState = false;
			mc.player.setSprinting(false);
		}
//		
//		if (!Move.isMoving()) {
//			mc.playerController.windowClick(0, 13, 0, ClickType.PICKUP, mc.player);
//			mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
//			mc.playerController.windowClick(0, 13, 0, ClickType.PICKUP, mc.player);
//		}
		
		/*
		boolean throughWalls = true;
		
		if (throughWalls) {
			for (float i = 0; i < range(); i += 0.5f) {
				Vector3d pos = mc.player.getEyePosition(1).add(mc.player.getCustomLook(rotation.x, rotation.y).mul(i));
				mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.START_DESTROY_BLOCK, new BlockPos(pos), Direction.UP));
				mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.STOP_DESTROY_BLOCK, new BlockPos(pos), Direction.UP));
			}
		}
		*/
		
		this.attackTimer.reset();
		
		mc.playerController.attackEntity(mc.player, target); // Атакуем
		mc.player.swingArm(Hand.MAIN_HAND); // Взмах рукой
		AuraUtility.tryBreakShield(); // Ломаем щит

		if (blocking) mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(mc.player.getActiveHand()));
		
		attacked = true;
		firstHit = false;
		
		attacks++;
		
		//Debugger.overlay(IdealHitUtility.getAICooldown() + " - " + mc.player.fallDistance);
		//Debugger.overlay(mc.player.fallDistance + "");
		
		if (Player.getBlock(0, 2, 0) != Blocks.AIR) {
			hitsUnderBlocks++;
		} else {
			hitsUnderBlocks = 0;
		}
		
		// Логгер
		this.logged = false;
		this.prevTargetHealth = this.target.getHealth() + this.target.getAbsorptionAmount();
		if (this.rotationTicks < 0 && mode.is(snap)) rotationTicks = -5;
	}
	
	private void findTarget(float range) {
		LivingEntity target = AuraUtility.calculateTarget(mc.player.getPositionVec(), range, this.players.get(), this.mobs.get(), this.invisibles.get(), this.naked.get(), this.bots.get(), this.friends.get(), rockUser.get(), this.sort.is(this.fov), this.sort.is(this.distance), this.sort.is(this.health), false, false);
		
		if (target == null || this.target == null || !mc.world.getAllEntities().contains(this.target) || this.target.isDead() || !pres.get()) {
			this.target = target;
		}
	}
	
	public void focus(LivingEntity target) {
		if (AuraUtility.isValidTarget(target, players.get(), mobs.get(), invisibles.get(), naked.get(), bots.get(), friends.get(), rockUser.get())) {
			this.target = target;
		}
	}
	
	public void focus(float range) {
		LivingEntity target = AuraUtility.calculateTarget(mc.player.getPositionVec(), range, this.players.get(), this.mobs.get(), this.invisibles.get(), this.naked.get(), this.bots.get(), this.friends.get(), rockUser.get(), true, false, false, false, false);
		this.target = target;
	}
	
	private void correctMovement(Event event) {
		if ((!(event instanceof EventJump) && !(event instanceof EventMove) && !(event instanceof EventInput))) return;
		
		if (mode.is(snap)) {
			if (!this.canAttack()) {
				return;
			}
			
			if (attacked || Bypass.via()) {
				return;
			}
		}
		
		if (!this.correction.is(this.no)) {
			if (event instanceof EventMove e) { // Корректируем направление движения (Direct-MoveFix)
				e.setYaw(this.rotation.x);
				e.setPitch(this.rotation.y);
			}
			
			if (event instanceof EventInput e && (!Server.isFS() || !Server.isFT() || !mc.player.isInWater())) { // Корректируем нажатые клавиши (Input-MoveFix)
				e.setYaw(this.rotation.x, correction.is(silent) || target == null ? mc.player.rotationYaw : Rotation.get(prevTarget.getPositionVec()).x);
			}
			
			if (event instanceof EventJump e) {
				e.setYaw(this.rotation.x);
			}
		}
		
		if (event instanceof EventJump e) {
			
			IdealHitUtility.setJumped(true);
		}
	}
	
	public void calculateRotation() {
		if (Float.isNaN(this.rotation.x)) this.rotation.x = 0;
        if (Float.isNaN(this.rotation.y)) this.rotation.y = 0;
        
		if (this.target == null) {			
			//resetRotation(); // Если цели нет то устанавливаем ротацию на оригинальную
			float targetYaw = mc.player.rotationYaw;
			float targetPitch = mc.player.rotationPitch;
			
			float yawDiff = Math.abs(this.rotAnim.getYaw() - targetYaw);       // Разница между текущей и оригинальной ротацией YAW
			
			int yawSpeed = (int) (getAIRotationSpeed(yawDiff));
			int pitchSpeed = MathUtility.randomInt(100, 110);
			
			this.rotAnim.animate(new Vector2f(targetYaw, targetPitch),
					yawSpeed, 	  // Скорость YAW, чем меньше, тем быстрее
					pitchSpeed	  // Скорость PITCH, чем меньше, тем быстрее
			);
			
			// GCD фикс чтобы имитировать то, что игрок двигает мышкой
			Vector2f correctedRotation = Rotation.correctRotation(
					this.rotAnim.getYaw(),
					this.rotAnim.getPitch()
			);

			// Итоговая ротация
			this.rotation = new Vector3f(correctedRotation.x, correctedRotation.y, correctedRotation.x);
			return;
		}
		
		boolean canCritical = (!mc.player.isOnGround()) || mc.player.fallDistance > 0
				//|| this.target.getHealth() <= 1f
				//|| mc.player.isOnGround()
				|| mc.player.isPotionActive(Effects.BLINDNESS)
				|| mc.player.isPotionActive(Effects.LEVITATION)
				|| mc.player.isPotionActive(Effects.SLOW_FALLING)
				|| mc.player.isInLava()
				|| mc.player.isInWater()
				|| mc.player.isOnLadder()
				|| mc.player.isPassenger()
				|| Player.isInWeb()
				|| mc.player.abilities.isFlying
				|| rock.getModules().get(Criticals.class).canCritical()
				|| !this.onlyCrits.get();
		
		boolean inWater = (!mc.player.isInWater() && Player.getBlock(0,-0.5f,0) == Blocks.WATER) && !mc.player.isSwimming() && Server.isFT();
		
		float range = this.range();
		boolean canAttack = this.target != null && AuraUtility.distanceTo(AuraUtility.getPoint(this.target)) <= range && this.attackTimer.passed((long) speed.get()) && canCritical;
		boolean preCanAttack = this.target != null && AuraUtility.distanceTo(AuraUtility.getPoint(this.target)) <= range && canCritical;
		
		ElytraTarget elytraTarget = rock.getModules().get(ElytraTarget.class);
		
		boolean fastRot = Server.is("infinity") || Server.isRW() || Server.isHW() || Server.is("hvh") || (elytraTarget.get() && prevTarget != null && this.prevTarget.isElytraFlying()); //|| mc.isSingleplayer(); // Условия при которых ротация будет моментальной
		if (!prevTarget.getBacktrack().isEmpty())
		this.prevTarget.getBacktrack().sort(Comparator.comparingDouble(pos -> MathUtility.squirt(mc.player.getDistanceSq(pos.getPos()))));
		Vector3d pos = AuraUtility.getPoint(this.prevTarget).subtract(mc.player.getEyePosition(mc.getRenderPartialTicks())).normalize();
		
		float shortestYawPath = (float) ((((((Math.toDegrees(Math.atan2(pos.z, pos.x)) - 90) - this.rotation.x) % 360) + 540) % 360) - 180);
		
		if (swapDirectionYawTimer.passed(450L) && shortestYawPath > 0 != directionYaw) {
			directionYaw = shortestYawPath > 0;
			swapDirectionYawTimer.reset();
		}
		
		float findPitch = (float) Math.min(90, -Math.toDegrees(Math.atan2(pos.y, Math.hypot(pos.x, pos.z))));
		
		float targetYaw = canAttack || !mode.is(funtime) || inWater ? this.rotation.x + shortestYawPath : rotation.x;
		float targetPitch = canAttack || !mode.is(funtime) || inWater ? (!Server.isFT() ? findPitch : Math.min(findPitch + (inWater ? MathUtility.random(10, 30) : MathUtility.random(-2, 2)), 90)) : rotation.y;
		
		if (((!preCanAttack && mode.is(funtime) && MathUtility.rayTraceWithBlock(7, this.rotation.x, this.rotation.y, mc.player, target, false))) && target != null) {
			for (int i = 0; i < 18; i++) {
				targetYaw = this.rotation.x + shortestYawPath + i * 10;
				if (!MathUtility.rayTraceWithBlock(7, targetYaw, targetPitch, mc.player, target, false)) {
					targetYaw += 10;
					break;
				}
			}
		}
		
		float yawDiff = Math.abs(this.rotAnim.getYaw() - targetYaw);       // Разница между текущей и оригинальной ротацией YAW
		
		int yawSpeed = (int) (fastRot ? 1 : getAIRotationSpeed(yawDiff));
		int pitchSpeed = fastRot ? 1 : MathUtility.randomInt(100, 150);

		targetPitch = MathHelper.clamp(targetPitch, -90, 90);
		this.rotAnim.animate(new Vector2f(targetYaw, targetPitch),
				yawSpeed, 	  // Скорость YAW, чем меньше, тем быстрее
				pitchSpeed	  // Скорость PITCH, чем меньше, тем быстрее
		);
		
		// GCD фикс чтобы имитировать то, что игрок двигает мышкой
		Vector2f correctedRotation = Rotation.correctRotation(
				this.rotAnim.getYaw(),
				this.rotAnim.getPitch()
		);
		
		fakeAnim.animate(new Vector2f(this.rotation.x + shortestYawPath + MathUtility.random(-5, 5), findPitch + MathUtility.random(-5, 5)), 
				mode.is(spooky) ? yawSpeed/3 : yawSpeed * 4, 
				pitchSpeed * 13);
		
		if (!Float.isNaN(correctedRotation.x) && !Float.isNaN(correctedRotation.y)) {
			// Итоговая ротация
			this.rotation = new Vector3f(correctedRotation.x, correctedRotation.y, AuraUtility.calculateCorrectYawOffset(rotAnim.getYaw(), rotation.z));
		}
	}
	
	/**
	 * Метод для получения скорости наводки YAW
	 * @param diff - Разница между интерполированной ротацией и обычной
	 * @return Возвращает скорость ротации
	 */
	private float getAIRotationSpeed(float diff) {
		/*
		if (Player.collideWith(this.target))
			return MathUtility.randomInt(150, 200);
		*/

		return MathHelper.clamp(
			diff, 						    	// Разница между интерполированной ротацией и обычной
			MathUtility.randomInt(1, 50),   	// Максимальная возможная скорость
			MathUtility.randomInt(150, 200)		// Минимальная возможная скорость - втф??? ты по-моему перепутал - не перепутал, у нас скорость в миллисекундах, чем больше - тем медленнее
		);

		/*
		return MathHelper.clamp((180 - diff)
				/ 180, 0.01f, MathUtility.random(0.50f, 0.70f))
				/ Math.max((float) Minecraft.debugFPS, 5) * 45;
		*/
	}
	
	private boolean canAttack() {
		return (canHit() && canCritical()) || canGroundAttack();
	}
	
	private boolean canHit() {
		//if (true) return false;
		
		float range = this.range();
		
		if (Server.isFT()) {
			range = Math.min(range, 2.8f);
		}
		
		boolean result = !this.raycast.get() || MathUtility.rayTraceWithBlock(range, this.rotation.x, this.rotation.y, mc.player, target, false);
		
		// Если включен "Не бить если ешь"
		if (this.notEat.get() && (mc.player.getHeldItemOffhand().getUseAction() == UseAction.EAT || mc.player.getHeldItemMainhand().getUseAction() == UseAction.EAT) && mc.player.getActiveHand() == Hand.OFF_HAND && mc.player.isHandActive())
			return false;
		
		if (Player.getBlock(0,2,0) != Blocks.AIR && !mc.getGameSettings().keyBindSneak.isPressed() && mc.player.fallDistance > 0)
			this.requireCritical = true;
		
		if (Player.getBlock(0,2,0) == Blocks.AIR || !mc.getGameSettings().keyBindJump.isKeyDown()) this.requireCritical = false;
		//if (Server.isFT() && Player.isFalling()) return false; 
		
		boolean criticals = rock.getModules().get(Criticals.class).canCritical();
		
		boolean rangeCheck = AuraUtility.distanceTo(AuraUtility.getPoint(this.prevTarget)) <= range && mc.player.getDistance(prevTarget) <= 6;
		
		ElytraTarget elytra = rock.getModules().get(ElytraTarget.class);
		
		if (elytra.get()) {
			rangeCheck = AuraUtility.distanceTo(AuraUtility.getPoint(this.prevTarget)) <= elytra.overrideRange(prevTarget);
		}

		return this.target != null && rangeCheck &&
				mc.player.getCooledAttackStrength() >= IdealHitUtility.getAICooldown() && (mc.player.getHeldItemMainhand().getItem() == Items.AIR || (criticals && Server.isHW()) ? attackTimer.passed(500) : attackTimer.passed(370))
				 && result && (!tickSelect.get() || target.lastHit.passed(500)) && rock.getModules().get(ElytraTarget.class).canAttack(prevTarget);
				//&& (!mode.is(spooky) || aimTicks > 1);
	}

	private boolean canGroundAttack() {
		float range = this.range();

		if (Server.isFT()) {
			range = Math.min(range, 2.8f);
		}

		boolean result = !this.raycast.get() || MathUtility.rayTraceWithBlock(range, this.rotation.x, this.rotation.y, mc.player, target, false);
		if (this.notEat.get() && (mc.player.getHeldItemOffhand().getUseAction() == UseAction.EAT || mc.player.getHeldItemMainhand().getUseAction() == UseAction.EAT) && mc.player.getActiveHand() == Hand.OFF_HAND && mc.player.isHandActive()) return false;

		boolean rangeCheck = AuraUtility.distanceTo(AuraUtility.getPoint(prevTarget)) <= range && mc.player.getDistance(prevTarget) <= 6;

		ElytraTarget elytra = rock.getModules().get(ElytraTarget.class);

		if (elytra.get()) {
			rangeCheck = AuraUtility.distanceTo(AuraUtility.getPoint(this.prevTarget)) <= elytra.overrideRange(prevTarget);
		}

		return attackOnGround.get() && !mc.gameSettings.keyBindJump.isKeyDown() && mc.player.isOnGround()
				&& target.lastHit.passed(500) && attackTimer.passed(500) && mc.player.getCooledAttackStrength() >= IdealHitUtility.getAICooldown()
				&& this.target != null && rangeCheck && result;
	}
	
	private boolean canCritical() {
		if (airCrits.get())
			return mc.player.fallDistance > 0;
		
		if (this.requireCritical && mc.player.isOnGround()) {
			this.requireCritical = false; // Криты в 2 блока (когда потолок над головой)
			if (hitsUnderBlocks > 2)
				return true;
		}
		
		double yDiff = (double)((int) mc.player.getPosY()) - mc.player.getPosY();
        boolean bl4 = yDiff == -0.01250004768371582;
        boolean bl5 = yDiff == -0.1875;
		
		return (!mc.player.isOnGround() && mc.player.fallDistance > IdealHitUtility.getAIFallDistance() && IdealHitUtility.canAIFall())
				//|| this.target.getHealth() <= 1f
				//|| mc.player.isOnGround()
				|| (bl5 || bl4) && !mc.player.isSneaking()
				|| mc.player.isPotionActive(Effects.BLINDNESS)
				|| mc.player.isPotionActive(Effects.LEVITATION)
				|| mc.player.isPotionActive(Effects.SLOW_FALLING)
				|| mc.player.isInLava()
				|| mc.player.isInWater()
				|| mc.player.isOnLadder()
				|| mc.player.isPassenger()
				|| Player.isInWeb()
				|| mc.player.abilities.isFlying
				|| rock.getModules().get(Criticals.class).canCritical()
				|| !this.onlyCrits.get();
	}
	
	private float getAdditionalRange() {
		ElytraTarget elytraTarget = rock.getModules().get(ElytraTarget.class);
		
		if (elytraTarget.get()) 
			return elytraTarget.getRange();
		
		return this.rangaTip.get();
	}
	
	private void resetRotation() {
		this.rotation = new Vector3f(
				mc.player.rotationYaw, 
				mc.player.rotationPitch,
				mc.player.renderYawOffset
		);
		
		rotAnim.animate(new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch), 1, 1);
		
		/*
		this.rotation = this.targetRotation = new Vector3f(
				mc.player.rotationYaw, 
				mc.player.rotationPitch,
				mc.player.renderYawOffset
		);
		*/
	}
	
	private float range() {
		return airCrits.get() && !firstHit ? 6 : range.get();
	}
	
	private void handlePackets(Event event) {
		if (!airCrits.get()) return;
		
		if (event instanceof EventUpdate) {
			if (mc.player.isOnGround()) disabled = false;
			if (this.air == null && mc.player.isOnGround()) {
				mc.player.getMotion().y = 0.42f;
			}
		}
		
		if (disabled) return;
		
		if (mc.player.fallDistance > 0.08f && this.air == null && !this.disabled) {
			air = new double[] {0,0,0,0,0,0,0,0,0,0,0,0};
			Vector3d pos = mc.player.getPositionVec();
			Vector3d motion = mc.player.getMotion();
			
			air[0] = pos.x;
			air[1] = pos.y;
			air[2] = pos.z;
			air[3] = mc.player.rotationYaw;
			air[4] = mc.player.rotationPitch;
			air[5] = mc.player.abilities.getFlySpeed();
			air[6] = mc.player.abilities.isFlying ? 1 : 0;
			air[7] = motion.x;
			air[8] = motion.y;
			air[9] = motion.z;
			air[10] = mc.player.isOnGround() ? 1 : 0;
		}
		
		if (air != null) {
			mc.player.setPosition(air[0], air[1], air[2]);
			mc.player.setMotion(0, 0, 0);
			
			if (target == null || mc.player.getDistance(target) > 6) {
				mc.player.abilities.isFlying = air[6] == 1;
				mc.player.abilities.setFlySpeed((float) air[5]);
				mc.player.setPosition(air[0], air[1], air[2]);
				mc.player.setMotion(0, 0, 0);
				mc.player.setOnGround(air[10] == 1);
				firstHit = true;
				disabled = true;
				air = null;
			}
			
			if (event instanceof EventUpdate) {
				air[11]++;
				if (air[11] > 40 && Server.isFT() /*|| Server.isHW()*/) {
					disabled = true;
					air = null;
					firstHit = true;
				}
			}
		}
		
		if (event instanceof EventSendPacket && airCrits.get() && air != null) {
			IPacket packet = ((EventSendPacket) event).getPacket();
			
			if ((
				packet instanceof CPlayerPacket ||
				packet instanceof CPlayerPacket.PositionRotationPacket ||
				packet instanceof CPlayerPacket.PositionPacket ||
				packet instanceof CPlayerPacket.RotationPacket
			)) {
				event.cancel();
			}
		}
	}
	
	@Override
	public void onEnable() {
		resetRotation();
		firstHit = true;
	}
	
	@Override
	public void onDisable() {
		if (this.air != null) {
			mc.player.abilities.isFlying = air[6] == 1;
			mc.player.abilities.setFlySpeed((float) air[5]);
			mc.player.setPositionAndRotation(air[0], air[1], air[2], (float) air[3], (float) air[4]);
			mc.player.setMotion(Vector3d.ZERO);
			//mc.player.setMotion(air[7], air[8], air[9]);
			mc.player.setOnGround(air[10] == 1);
		}
		this.air = null;
		this.target = null;
		rock.getModules().get(AutoSprint.class).setCanSprint(true);
		disableTimer.reset();
	}
	
	public boolean predictAttack() {
		return target != null && canHit() && !mc.player.isOnGround() && (IdealHitUtility.canAIFall() && (FallingPlayer.fromPlayer(mc.player).findFall(IdealHitUtility.getAIFallDistance(), 1) || canCritical()));
	}

	private void updateAdvanced() {
		if (prevTarget != null && target != null) {
			rotation.x = mc.player.rotationYawHead;
			rotation.y = mc.player.rotationPitchHead;
			rotation.z = mc.player.rotationYawHead;

			anim.setForward(Math.abs(mc.player.getPosYEye() - target.getPosY()) < 1.98f);
			anim1.setForward(MathUtility.rayTraceWithBlock(range.get(),
					mc.player.rotationYawHead, mc.player.rotationPitchHead, mc.player, target, false));
			anim1.setSpeed(250);
			anim3.setForward(AuraUtility.getAngle(target) > 40 &&
					AuraUtility.getAngle(target) < 60);
			anim4.setForward(AuraUtility.getAngle(target) > 90);

			float yaw, pitch;

			Vector3d vec = VectorUtility.getBestVector(target, MathUtility.randomNew(-60, 60) / 180 / 15);

			float shortestYawPath = (float) ((((((Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90) - rotation.x)) + 540)) - 180);
			float yawToTarget = rotation.x + shortestYawPath;
			float pitchToTarget = (float) -Math.toDegrees(Math.atan2(vec.y, Math.hypot(vec.z, vec.x)));

			float yawDelta = (wrapDegrees(yawToTarget - rotation.x));
			float pitchDelta = (wrapDegrees(pitchToTarget - rotation.y));

			yawDelta = AuraUtility.GENIUSCLAMPERR$$$(yawDelta, true) * MathUtility.randomNew(0.8, 0.9);
			pitchDelta = AuraUtility.GENIUSCLAMPERR$$$(pitchDelta, false);
			yawDelta = AuraUtility.fixDeltaNonVanillaMouse(yawDelta, pitchDelta).x;
			pitchDelta = AuraUtility.fixDeltaNonVanillaMouse(yawDelta, pitchDelta).y;

			pitchDelta /= 2.5f;
			pitchDelta *= (float) (0.13f + 0.17 * anim1.get() + 0.6f * anim.get()) / 1000 * MathUtility.randomNew(840, 960);

			float recDelta = Math.min(Math.abs(yawDelta), 90);
			yawDelta = yawDelta > 0 ? recDelta : -recDelta;

			yawDelta *= (float) (1 - anim3.get() / 3);
			yawDelta *= (float) (1 - 0.15 * anim4.get());
			pitchDelta *= (float) (1 + 0.2 * anim4.get());
			yawDelta *= 1.2f + spookySpeed.get();
			
			yaw = rotation.x + yawDelta;
			pitch = rotation.y + pitchDelta;
			pitch = MathHelper.clamp(pitch, -90, 90);

			this.spookyAnim.easing(Easing.BOTH_CIRC).animate(new Vector2f(yaw, pitch),
                    (int) animSpeed.get(),
                    (int) ((int) animSpeed.get() * 1.5));

			this.rotAnimAdv.easing(Easing.LINEAR).animate(new Vector2f(spookyAnim.getYaw(), spookyAnim.getPitch()),
					30 - (int) animSpeed.get() * 3,
					30 - (int) ((int) animSpeed.get() * 1.5) * 3);
			
			Vector2f correctedRotation = Rotation.correctRotation(rotAnimAdv.getYaw(), rotAnimAdv.getPitch());
			yaw = correctedRotation.x;
			pitch = correctedRotation.y;

			rotation = new Vector3f(yaw, pitch, AuraUtility.calculateCorrectYawOffset(yaw, rotation.z));
		} else {
			resetRotation();
		}
	}

}