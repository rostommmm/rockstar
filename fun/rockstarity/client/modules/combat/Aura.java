package fun.rockstarity.client.modules.combat;

import java.util.Objects;

import fun.rockstarity.api.ClientInfo;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventDamage;
import fun.rockstarity.api.events.list.game.EventTick;
import fun.rockstarity.api.events.list.game.inputs.EventInput;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.math.aura.AuraUtility;
import fun.rockstarity.api.helpers.math.aura.Boost;
import fun.rockstarity.api.helpers.math.aura.IdealHitUtility;
import fun.rockstarity.api.helpers.math.aura.RotationMode;
import fun.rockstarity.api.helpers.math.aura.ai.AIPredictor;
import fun.rockstarity.api.helpers.math.aura.modes.ClassicRotation;
import fun.rockstarity.api.helpers.math.aura.modes.FunTimeRotation;
import fun.rockstarity.api.helpers.math.aura.modes.NeuroRotation;
import fun.rockstarity.api.helpers.math.aura.modes.SpookyRotation;
import fun.rockstarity.api.helpers.player.Bypass;
import fun.rockstarity.api.helpers.player.FallingPlayer;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Position;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.secure.Debugger;
import fun.rockstarity.client.modules.move.AutoSprint;
import fun.rockstarity.client.modules.other.Globals;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.UseAction;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CEntityActionPacket.Action;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author ConeTin
 * @since 20 мар. 2025 г.
 */
@NativeInclude
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Info(name = "Aura", desc = "Автоматически убивает сущностей", type = Category.COMBAT)
public class Aura extends Module {

	// Настройки
	Mode mode = new Mode(this, "Режим");
	@Getter ClassicRotation classic = new ClassicRotation(mode);
	@Getter NeuroRotation neuro = (NeuroRotation) new NeuroRotation(mode).set();
	@Getter FunTimeRotation funtime = (FunTimeRotation) new FunTimeRotation(mode);
	@Getter SpookyRotation spooky = (SpookyRotation) new SpookyRotation(mode);

	@Getter Slider range = new Slider(this, "Дистанция атаки").min(2).max(6).inc(0.1f).set(3).desc("Дистанция, на которой Aura будет пытаться ударить");
	Slider rotRange = new Slider(this, "Дистанция наводки").min(0).max(2).inc(0.1f).set(1).desc("Дистанция, на которой Aura будет наводится на противника. Плюсуется к обычной дистанции").hide(() -> !rock.getModules().get(Aura.class).getRaytrace().get());

	Select others = new Select(this, "Другое");
	@Getter Select.Element onlyCrits = new Select.Element(others, "Только криты").set(true);
	CheckBox smartCrits = new CheckBox(onlyCrits, "Умные криты").set(true).hide(() -> !onlyCrits.get());
	CheckBox watercrits = new CheckBox(onlyCrits, "Криты на воде").set(true);
	@Getter CheckBox critsBypass = new CheckBox(onlyCrits, "Рандомизировать").hide(() -> Server.is("spooky"));
	@Getter CheckBox autoStop = new CheckBox(onlyCrits, "Авто стоп").hide(() -> Server.is("spooky"));
	@Getter Select.Element walls = new Select.Element(others, "Через стены").set(true);
	Select.Element notEat = new Select.Element(others, "Не бить если ешь");
	@Getter Select.Element raytrace = new Select.Element(others, "Проверка наводки").set(true);
	Select.Element onlyWeapon = new Select.Element(others, "Только с оружием");
	Select.Element sword = new Select.Element(others, "Автоматически брать меч");

	Mode correction  = new Mode(this, "Коррекция движения").desc("Изменяет направление движения персонажа на то, куда смотрит Aura").hide(() -> (rock.getModules().get(Aura.class).getVisualRot().get() || rock.getModules().get(Aura.class).getClientLook().get()) && rock.getModules().get(Aura.class).getAdditional().get());
	Mode.Element no = new Mode.Element(correction, "Нет");
	Mode.Element focused = new Mode.Element(correction, "Сфокусированная");
	Mode.Element silent = new Mode.Element(correction, "Незаметная").set();
	Mode.Element targeting = new Mode.Element(correction, "Таргетированная");

	// Дополнительные
	@Getter CheckBox additional = new CheckBox(this, "Дополнительно");

	Select settings = new Select(additional, "Настройки").desc("Выберите дополнительные настройки");
	Select.Element autoAttack = new Select.Element(settings, "Авто атака").set(true);
	// --- Removed .desc() ---
	@Getter Select.Element visualRot = new Select.Element(settings, "От первого лица");
	@Getter Select.Element clientLook = new Select.Element(settings, "Client Look");
	// --- End Removed ---
	Select.Element fromShield = new Select.Element(settings, "Через щит").set(true);
	Select.Element visual360 = new Select.Element(settings, "Визуальный 360").hide(() -> visualRot.get() || clientLook.get());
	@Getter Select.Element multipoint = new Select.Element(settings, "Мультипоинты").set(true).hide(() -> !classic.get());
	Select.Element critSync = new Select.Element(settings, "Синхр. критов");

	Mode attackMode = new Mode(additional, "Режим атаки").hide(() -> !autoAttack.get());
	Mode.Element attack19 = new Mode.Element(attackMode, "1.9+");
	Mode.Element attack18 = new Mode.Element(attackMode, "1.8");

	Slider attackSpeedFrom = new Slider(additional, "Задержка атаки от").min(50).max(500).inc(50).set(150).hide(() -> attack19.get() || !autoAttack.get());
	Slider attackSpeedTo = new Slider(additional, "Задержка атаки до").min(50).max(500).inc(50).set(50).hide(() -> attack19.get() || !autoAttack.get());

	Slider hitchance = new Slider(additional, "Шанс попадания").min(1).max(100).inc(1).set(100).desc("Шанс успешного попадания по цели, зависит от скорости наводки").hide(() -> !autoAttack.get());

	Mode fovMode  = new Mode(additional, "Режим поля зрения").desc("Определяет, каким образом будет вычисляться поле зрения").hide(() -> (visualRot.get() || clientLook.get()) || rock.getModules().get(Aura.class).getFovClosed().get() == 180);
	Mode.Element fovPlayer = new Mode.Element(fovMode, "От игрока");
	Mode.Element fovRotation = new Mode.Element(fovMode, "От ротации");
	@Getter Slider fovClosed = new Slider(additional, "Поле зрения").min(5).max(180).inc(1).set(180).desc("Поле зрения, при котором будет действовать Aura");

	@Getter Position offset = new Position(additional, "Отклонение").minX(-15).minY(-15).maxX(15).maxY(15).x(0).y(0).desc("Выберите на сколько наводка будет отводиться");

	Select debug = new Select(additional, "Debug").desc("Настройки для кодеров").hide(() -> !rock.isDebugging());
	Select.Element printfd = new Select.Element(debug, "Выводить falldistance");

	// Таргетинг
	Mode sort = new Mode(this, "Сортировка").desc("Выберите принцип, по которому будут сортироваться цели");
	Mode.Element fov = new Mode.Element(sort, "По полю зрения");
	Mode.Element distance = new Mode.Element(sort, "По дистанции");
	Mode.Element health = new Mode.Element(sort, "По здоровью");
	Mode.Element lowArmor = new Mode.Element(sort, "По кому больше урона");
	Mode.Element omgDamage = new Mode.Element(sort, "По тому что больше сносит");

	Select targets = new Select(this, "Цели").min(1).desc("Сущности, которых будет бить Aura");
	Select.Element players = new Select.Element(targets, "Игроки").set(true);
	Select.Element invisibles = new Select.Element(targets, "Невидимые").set(true).hide(() -> !this.players.get());
	Select.Element naked = new Select.Element(targets, "Голые").set(true).hide(() -> !this.players.get());
	Select.Element friends = new Select.Element(targets, "Друзья").hide(() -> !this.players.get());
	Select.Element bots = new Select.Element(targets, "Боты").hide(() -> !this.players.get());
	Select.Element mobs = new Select.Element(targets, "Мобы");
	Select.Element rockUser = new Select.Element(targets, "Пользователи " + ClientInfo.NAME).hide(() -> !rock.getModules().get(Globals.class).get());

	CheckBox pres = new CheckBox(this, "Преследование").desc("Не меняет цель даже если появилась другая, более приоритетная цель");

	@Getter Boost boost = new Boost(this);

	// Поля с хуйней
	@NonFinal @Getter float randomFactor;
	@NonFinal @Getter @Setter LivingEntity target, prevTarget;
	@NonFinal @Getter int attacks;
	@NonFinal @Getter @Setter int fdCount;
	@NonFinal @Getter @Setter boolean snapTick;
	@NonFinal int skipTicks;
	@NonFinal boolean nextMiss, shield, waitMiss, canSync;
	TimerUtility backTimer = new TimerUtility();
	@Getter TimerUtility attackTimer = new TimerUtility();
	TimerUtility missTimer = new TimerUtility();

	TimerUtility hurtTimer = new TimerUtility();

	public Aura() {
		super(10);
	}

	@Override
	public void onAllEvent(Event event) {
		if (event instanceof EventUpdate) {
			if (neuro.get()) AIPredictor.updateModel(neuro.getModel().getCurrent().getName());
		}

		if (target != null || !backTimer.passed(250)) {
			if (event instanceof EventTick && !funtime.get() || event instanceof EventUpdate && funtime.get()) {
				// Апдейт ротации
				rotation().update(target);
			}

			// Determine correction mode index
			int correctionModeIndex = no.get() ? 0 : silent.get() ? 1 : targeting.get() ? 3 : 2;

			// Apply server-side rotations and movement correction
			if (!classic.getFullpacket().get()) {
				// Calculate visual yaw offset for visual360 (only if camera isn't locked)
				float visualYawOffset = (visual360.get() && additional.get() && !clientLook.get() && !visualRot.get())
						? (-20 + 380 * mc.player.getCooledAttackStrength(0))
						: 0;
				Player.look(event, rotation().getYaw(), rotation().getPitch(), true, correctionModeIndex, rotation().getYaw() + visualYawOffset);
			}

			// Force client camera view if visualRot OR clientLook is enabled
			if (event instanceof EventRender2D && (visualRot.get() || clientLook.get()) && additional.get()) {
				mc.player.rotationYaw = rotation().getYaw();
				mc.player.rotationPitch = rotation().getPitch();
			}
		}
	}

	@Override
	public void onEvent(Event event) {
		if (weaponCheck()) {
			if (event instanceof EventUpdate && target != null) {
				onDisable();
			}
			return;
		}

		boost.onEvent(event, target);

		if (event instanceof EventReceivePacket e && e.getPacket() instanceof SPlaySoundEffectPacket play &&
				(play.getSound() == SoundEvents.ENTITY_PLAYER_ATTACK_CRIT
						|| play.getSound() == SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK
						|| play.getSound() == SoundEvents.ENTITY_PLAYER_ATTACK_STRONG
						|| play.getSound() == SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP
						|| play.getSound() == SoundEvents.ENTITY_PLAYER_ATTACK_WEAK) && !attackTimer.passed(300) && waitMiss) {
			waitMiss = false;
		}

		if (event instanceof EventInput e) {
			//	e.setJump(true);
			//	e.setForward(1);
		}

		if (event instanceof EventUpdate) {
			// Calculate FOV yaw based on settings
			float fovCheckYaw = (fovMode.is(fovPlayer) || visualRot.get() || clientLook.get()) ? mc.player.rotationYaw : rotation().getYaw();

			// Апдейт таргета
			LivingEntity newTarget = AuraUtility.calculateTarget(
					mc.player.getPositionVec(),
					range.get() + (raytrace.get() ? rotRange.get() : 0) + rock.getModules().get(ElytraTarget.class).getRange(),
					players.get(), mobs.get(), invisibles.get(), naked.get(), bots.get(), friends.get(), rockUser.get(), // Таргеты
					sort.is(fov), sort.is(distance), sort.is(health), sort.is(omgDamage), sort.is(lowArmor), // Сортировка
					additional.get() ? fovClosed.get() : 180,
					fovCheckYaw, // Use calculated FOV yaw
					walls.get()
			);

			if (newTarget == null || this.target == null || !mc.world.getAllEntities().contains(this.target) || this.target.isDead() || !pres.get()) {
				if (newTarget == null && this.target != null) {
					rock.getModules().get(AutoSprint.class).setCanSprint(true);
					onDisable();
					backTimer.reset();
				}
				this.target = newTarget;
			}

			if (this.target != null) {
				prevTarget = this.target;
			}

			// Автостопы
			if (autoStop.get()) {
				//Debugger.overlay(100 + attacks / 2 % 3 * 50 + "");
				if (!attackTimer.passed(attacks / 3 % 3 * 50)) {
					mc.player.movementInput.moveForward = 0;
					mc.player.movementInput.moveStrafe = 0;

					mc.getGameSettings().keyBindSprint.setPressed(false);
					mc.player.setSprinting(false);
					rock.getModules().get(AutoSprint.class).setCanSprint(false);
				} else {
					rock.getModules().get(AutoSprint.class).setCanSprint(true);
				}
			}

			if (attackTimer.passed(300) && waitMiss) {
				//mc.player.connection.sendPacket(new CAnimateHandPacket(Hand.MAIN_HAND));
				//mc.player.connection.sendPacket(new CAnimateHandPacket(Hand.OFF_HAND));
				// в случае если мисс
				waitMiss = false;
			}

			snapTick = false;
		}

		if (event instanceof EventInput e && target != null && Player.getBlock(0, 2, 0) != Blocks.AIR && Server.is("spooky")) {
			//e.setSneak(true);
		}

		// Ротейтимся, корректируем мувмент, делаем визуальную ротацию (Visual part moved to onAllEvent)
		if (target != null) {
			if (event instanceof EventUpdate) {
				canSync = false;
				for (PlayerEntity entity : mc.world.getPlayers()) {
					if (mc.player.getDistance(entity) < 4 && rock.getFriendsHandler().isFriend(entity)) {
						canSync = true;
					}
				}
			}

			if (sword.get()) getBestSword();

			// Attack logic
			if (event instanceof EventUpdate) {
				switch (hit()) {
					case PASS: // В случае успешного удара
						boolean predictHit = !mc.player.isOnGround() && IdealHitUtility.canAIFall() && (FallingPlayer.fromPlayer(mc.player).findFall(IdealHitUtility.getNewFallDistance(target)) || canCritical()) && skipTicks <= 0;

						if (predictHit) {
							shield = mc.player.isHandActive() && mc.player.getActiveItemStack().getItem().getUseAction(mc.player.getActiveItemStack()) == UseAction.BLOCK && (!additional.get() || fromShield.get());
							if (shield)
								mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));


							if (!mc.player.isSwimming()) {
								if (mc.player.isSprinting()) {
									mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, Action.STOP_SPRINTING));
									mc.player.serverSprintState = false;
								}

								if (autoStop.get()) {
									mc.player.movementInput.moveForward = 0;
									mc.player.movementInput.moveStrafe = 0;
								}

								mc.getGameSettings().keyBindSprint.setPressed(false);
								mc.player.setSprinting(false);
								rock.getModules().get(AutoSprint.class).setCanSprint(false);

								skipTicks = 1;
							}
							//return;
						}

						if (canCritical()) {
//						if ((Player.getBlock(0, 1, 0) != Blocks.AIR || mc.world.getBlock(target.getPosition()) != Blocks.AIR) && spookyWalls.get()) {
//							Vector3d pos = mc.player.getPositionVec();
//							Bypass.send(pos.x, pos.y, pos.z, rotation().getYaw(), rotation().getPitch(), false);
//						}
							attack();
							nextMiss = false;
						}
						break;
					case MISS: // В случае промаха
						mc.player.swingArm(Hand.MAIN_HAND);
						mc.player.resetCooldown();
						nextMiss = false;
						break;
					default:
						break;
				}
			}
		}
	}

	private void attack() {
		if ((watercrits.get() || !additional.get()) && (mc.player.isInWater() || Player.getBlock(0,-0.2f,0) == Blocks.WATER) && Player.getBlock(0,1,0) == Blocks.AIR && mc.getGameSettings().keyBindJump.isKeyDown()) {
			if (mc.player.fallDistance == 0.0f) {
				return;
			}
		}

		if (classic.getFullpacket().get()) {
			Bypass.send(rotation().getYaw(), rotation().getPitch());
		}

		if (!autoStop.get()) {
			rock.getModules().get(AutoSprint.class).setCanSprint(true);
		}

		mc.playerController.attackEntity(mc.player, target);
		mc.player.swingArm(Hand.MAIN_HAND);
		AuraUtility.tryBreakShield();

		if (shield) mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(mc.player.getActiveHand()));

		if (printfd.get())
			Debugger.overlay(mc.player.fallDistance + "");

		if (mc.player.fallDistance > 0.1f) {
			fdCount = 0;
		} else {
			fdCount++;
		}

		if (Server.isFT() && classic.getSnap().get()) {
			snapTick = true;
		}

		randomFactor = MathUtility.random(0, 1);
		waitMiss = true;
		attackTimer.reset();
		skipTicks = 0;
		attacks++;
	}

	private HitResult hit() {
		if (!autoAttack.get() && additional.get())
			return HitResult.NONE;

		if (additional.get() && critSync.get() && mc.player.hurtTime > 0 && canSync)
			return HitResult.NONE;

		if (attack19.get()
				? mc.player.getCooledAttackStrength() <= 0.92f || !attackTimer.passed(/*(mc.player.getHeldItemMainhand().getItem() instanceof SwordItem || mc.player.getHeldItemMainhand().getItem() instanceof AxeItem) ? 500 : 500 + attacks % 4 * 100*/ 500)
				: !attackTimer.passed(MathUtility.random(attackSpeedFrom.get(), attackSpeedTo.get())))
			return HitResult.NONE;

		if (!MathUtility.rayTraceWithBlock(range.get(), rotation().getYaw(), rotation().getPitch(), mc.player, target, false) && raytrace.get()) {
			if (nextMiss && additional.get()) {
				return missTimer.passed(hitchance.get() / 1.1F) && hitchance.get() < 99 ? HitResult.MISS : HitResult.NONE;
			}

			nextMiss = true;
			missTimer.reset();
			return HitResult.NONE;
		}

		if (notEat.get() && (mc.player.getHeldItemOffhand().getUseAction() == UseAction.EAT
				|| mc.player.getHeldItemMainhand().getUseAction() == UseAction.EAT) && mc.player.isHandActive())
			return HitResult.NONE;

		return HitResult.PASS;
	}

	public boolean canCritical() {
		double yDiff = (double)((int) mc.player.getPosY()) - mc.player.getPosY();
		boolean bl4 = yDiff == -0.01250004768371582;
		boolean bl5 = yDiff == -0.1875;

		//Debugger.overlay(IdealHitUtility.canAIFall() + " - " + IdealHitUtility.getNewFallDistance(target));
		return (!mc.player.isOnGround() && mc.player.fallDistance > IdealHitUtility.getNewFallDistance(target) && IdealHitUtility.canAIFall() || target != null && Player.getBlock(0, 2, 0) != Blocks.AIR && Player.getBlock(0, -1, 0) != Blocks.AIR && Server.is("spooky") && mc.player.isOnGround() && fdCount > 8)
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
				|| smartCrits.get() && mc.player.isOnGround() && !mc.gameSettings.keyBindJump.isKeyDown()
				|| !this.onlyCrits.get();
	}


	private void findTarget(float range) {
		LivingEntity target = AuraUtility.calculateTarget(mc.player.getPositionVec(), range, this.players.get(), this.mobs.get(), this.invisibles.get(), this.naked.get(), this.bots.get(), this.friends.get(), rockUser.get(), this.sort.is(this.fov), this.sort.is(this.distance), this.sort.is(this.health), sort.is(omgDamage), sort.is(lowArmor));

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

	private boolean weaponCheck() {
		return onlyWeapon.get() && !(mc.player.getHeldItemMainhand().getItem() instanceof SwordItem || mc.player.getHeldItemMainhand().getItem() instanceof AxeItem);
	}

	private void getBestSword() {
		float dmg = 1;
		int bestItem = -1;

		for (int i = 0; i < 9; i++) {
			ItemStack i1 = mc.player.inventory.getStackInSlot(i);
			if (i1.getItem() instanceof SwordItem && getSwordStrength(i1) > dmg) {
				dmg = getSwordStrength(i1);
				bestItem = i;
			}

			if (bestItem != -1) mc.player.inventory.currentItem = bestItem;
		}
	}

	private float getSwordStrength(ItemStack stack) {
		if (stack.getItem() instanceof SwordItem sword) {
			float sharpness = EnchantmentHelper.getEnchantmentLevel(Objects.requireNonNull(Enchantment.getEnchantmentByID(16)), stack) * 1.25F;
			float fireAspect = EnchantmentHelper.getEnchantmentLevel(Objects.requireNonNull(Enchantment.getEnchantmentByID(20)), stack) * 1.5F;
			return sword.getAttackDamage() + sharpness + fireAspect;
		}
		return 0;
	}

	@Override
	public void onEnable() {
		rotation().reset(0);
	}

	@Override
	public void onDisable() {
		rock.getModules().get(AutoSprint.class).setCanSprint(true);
		if (target != null)
			backTimer.reset();
		target = null;
		rotation().reset((visualRot.get() || clientLook.get()) && additional.get() ? 0 : 1);
	}

	public RotationMode rotation() {
		return (RotationMode) mode.getCurrent();
	}

	enum HitResult {
		NONE,
		MISS,
		PASS
	}
}