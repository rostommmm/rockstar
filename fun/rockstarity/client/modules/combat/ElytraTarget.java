package fun.rockstarity.client.modules.combat;

import static org.lwjgl.opengl.GL11.glLineWidth;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import fun.rockstarity.api.helpers.math.InventoryUtility;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import fun.rockstarity.api.Reacher;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventSpawn;
import fun.rockstarity.api.events.list.game.inputs.EventInput;
import fun.rockstarity.api.events.list.game.inputs.EventKey;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.game.packet.EventSendPacket;
import fun.rockstarity.api.events.list.player.EventAttack;
import fun.rockstarity.api.events.list.player.EventJump;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventMotionMove;
import fun.rockstarity.api.events.list.player.EventMove;
import fun.rockstarity.api.events.list.player.EventTrace;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.events.list.render.EventRender3D;
import fun.rockstarity.api.events.list.render.world.EventRenderWorldEntities;
import fun.rockstarity.api.helpers.game.BoostUtility;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.game.PredictUtility;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.math.aura.AuraUtility;
import fun.rockstarity.api.helpers.math.aura.IdealHitUtility;
import fun.rockstarity.api.helpers.math.aura.Rotation;
import fun.rockstarity.api.helpers.player.Bypass;
import fun.rockstarity.api.helpers.player.Inventory;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Binding;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import fun.rockstarity.api.secure.Debugger;
import fun.rockstarity.client.modules.render.GlowESP;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.overlay.PlayerTabOverlayGui;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.Items;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.client.CUseEntityPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket.Action;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.optifine.Config;
import net.optifine.shaders.Shaders;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author ConeTin
 * @since 25 авг. 2024 г.
 * 
 * Кирюха етк если ты еще продолжишь мои байпасы пастить то завтра кряк минседа
 * 
 * Ладно, кирюха, извини, но все равно много не пасть!!
 */

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Info(name="ElytraTarget", desc="Использует элитру и фейрверки для таргета", type=Category.COMBAT)
public class ElytraTarget extends Module {
	
	final Select utilities = new Select(this, "Утилиты").desc("Различные утилиты для улучшения работы");

	final Select.Element freezeDummy = new Select.Element(utilities, "Замораживать игрока");
	final Select.Element targetPearl = new Select.Element(utilities, "Следовать за перлами").set(true);
	final Select.Element switchTarget = new Select.Element(utilities, "Менять цель на ударяющего").set(true);
	final Select.Element firework = new Select.Element(utilities, "Фейерверк при замедлении").set(true);
	final Select.Element swapElytra = new Select.Element(utilities, "Надевать элитру").set(true);
	final Select.Element resolver = new Select.Element(utilities, "Resolver").set(true);
	final Select.Element predict = new Select.Element(utilities, "Предикт").set(true).hide(() -> !resolver.get());
	final Select.Element changeChest = new Select.Element(utilities, "Сменять нагрудник если падаешь");

	final CheckBox predictDefensive = new CheckBox(predict, "Анти Defensive").set(true);
	final CheckBox span = new CheckBox(predict, "Перегон").set(true);
	final CheckBox doubleRot = new CheckBox(predict, "Двойная ротация").set(true).hide(() -> !span.get() || !Bypass.via() || !Server.isBravo()).desc("Использует одновременно 2 ротации. Первая для направления движения, вторая для ударов");
	
	final Select autoleave = new Select(this, "Улетать").desc("При каких условиях улетать от цели");

	final Select.Element health = new Select.Element(autoleave, "При маленьком здоровье");
	final Select.Element cooldown = new Select.Element(autoleave, "Когда ещё не можешь ударить").set(true);
	final Select.Element useItem = new Select.Element(autoleave, "При использовании предметов").set(true);
	final Select.Element bind = new Select.Element(autoleave, "При нажатии клавиши");
	
	final CheckBox notUse = new CheckBox(this, "Минимизировать траты").set(true).hide(() -> !freezeDummy.get()).desc("Не использует фейерверк тогда, когда можно обойтись без него");
	
	
	final Slider useCooldown = new Slider(this, "Задержка фейерверка").min(50).max(3000).inc(100).set(500).desc("Задержка на использование фейерверка");
	
	final CheckBox swapChest = new CheckBox(this, "Свапать нагрудник").set(true).hide(() -> !freezeDummy.get()).desc("Берёт нагрудник при заморозке");
	
	final Slider useCooldownLeave = new Slider(this, "Задержка при ливе").min(50).max(3000).inc(100).set(500).desc("Задержка на использование фейерверка при отлёте(ливе)").hide(() -> autoleave.getToggled().isEmpty());
	final CheckBox onlyGround = new CheckBox(this, "Только на земле").set(true).hide(() -> !health.get()).desc("Не улетает при маленьком хп если цель не летает на элитрах").hide(() -> !health.get());
	final Slider leaveHealth = new Slider(this, "Здоровье для лива").min(1).max(20).inc(0.5f).set(10).desc("Здоровье, при котором игрок будет улетать от цели").hide(() -> !health.get());
	
	final Select visuals = new Select(this, "Визуалы").desc("Визуальные дополнения");

	final Select.Element leaveLines = new Select.Element(visuals, "Линии отлива").hide(() -> autoleave.getToggled().isEmpty() );
	final Select.Element targetLine = new Select.Element(visuals, "Линия полёта");
	final Select.Element misses = new Select.Element(visuals, "Миссы противника");
	final Select.Element resolverPos = new Select.Element(visuals, "Настоящая позиция противника").set(true);
	
	final Select leaveVectors = new Select(this, "Векторы лива").desc("Направления движения, по которым игрок будет двигаться во время лива. Можно перетаскивать для сортировки").draggable(true).hide(() -> autoleave.getToggled().isEmpty());
	
	final LeaveVector up = new LeaveVector(leaveVectors, "Вверх", new Vector3d(0, 20, 0));
	final LeaveVector down = new LeaveVector(leaveVectors, "Вниз", new Vector3d(0, -20, 0));
	final LeaveVector east = new LeaveVector(leaveVectors, "Восток", new Vector3d(20, 0, 0));
	final LeaveVector west = new LeaveVector(leaveVectors, "Запад", new Vector3d(-20, 0, 0));
	final LeaveVector south = new LeaveVector(leaveVectors, "Юг", new Vector3d(0, 0, 20));
	final LeaveVector north = new LeaveVector(leaveVectors, "Север", new Vector3d(0, 0, -20));
	
	
	final Select desync = new Select(this, "Десинхронизация при").desc("Выберите условия для десинхронизации вашей позиции у других игроков");

	final Select.Element defensive = new Select.Element(desync, "Ударе (Defensive)").set(true);
	
	
	final CheckBox swapVector = new CheckBox(this, "Менять вектор").set(true).desc("Изменяет вектор на противоположный при ударе").hide(() -> autoleave.getToggled().isEmpty());

	
	final Binding changeTarget = new Binding(this, "Смена цели").desc("При нажатии клавиши целью будет назначаться ближайшая к прицелу сущность");


	final Binding leaveBind = new Binding(this, "Бинд отлёта").desc("При нажатии клавиши игрок будет улетать от цели").hide(() -> !bind.get());

	
	final Slider slot = new Slider(this, "Выбор слота").min(1).max(9).inc(1).set(7).desc("Выберите слот, в который будут перетаскиваться фейерверки");

	// TODO final CheckBox autoThirdPerson = new CheckBox(this, "3-е лицо").set(false);
	
	final TimerUtility useTimer = new TimerUtility();
	
	Entity pearlEntity;
	Vector3d lastPos;
	boolean prevFreezed;
	
	Vector3d leaveVec = Vector3d.ZERO;
	Vector3d lastVec = Vector3d.ZERO;
	
	Vector3d defensivePos;
	@Getter
	boolean defensiveActive, lastDefensive;
	final ArrayList<IPacket<?>> packets = new ArrayList<>();
	final TimerUtility defensiveTimer = new TimerUtility();

	ItemStack currentStack = ItemStack.EMPTY;
	boolean bindLeaving;
	
	@NativeInclude
	public ElytraTarget() {
		super(14);
	}
    
	@Override
	public void onEvent(Event event) {
		try {
			Aura aura = rock.getModules().get(Aura.class);
			LivingEntity target = aura.getTarget();
			
			if (!Player.isInGame()) return;
			
			if (event instanceof EventUpdate) {
				this.currentStack = mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST);

				if (changeChest.get() && mc.player.getMotion().y > 4 && target != null && target.getMotion().y > 4 &&
						(Math.abs(mc.player.getPosY() - target.getPosY()) > 5)) {
					changeChestPlate(currentStack);
				} else if (changeChest.get() && target != null && target.getMotion().y < 5) {
					changeChestPlate(currentStack);
				}
				
				if (event instanceof EventMotion) {
				//	mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, Action.START_SPRINTING));
				//	mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, Action.RELEASE_SHIFT_KEY));
				}
				
				if (Player.findItem(45, Items.ELYTRA) == -1) {
				//	Chat.msg("Элитры нет.");
				//	this.set(false);
				//	return;
				}
				
				if (Inventory.findItemNoChanges(44, Items.FIREWORK_ROCKET) == -1) {
					Chat.msg("Фейерверков нет.");
					this.set(false);
					return;
				}
			}
			
			if (!aura.get()) {
				aura.setTarget(null);
			}
			
			if (event instanceof EventKey e && !(mc.currentScreen instanceof ChatScreen) && changeTarget.getBindByKey(e).isPresent() && !e.isReleased() && aura.get() && mc.isGameFocused()) {
				aura.focus(100);
			}
			
			if (event instanceof EventKey e && bind.get() && !(mc.currentScreen instanceof ChatScreen) && leaveBind.getBindByKey(e).isPresent() && aura.get() && mc.isGameFocused()) {
				bindLeaving = !e.isReleased();
			}
			
			if (event instanceof EventUpdate) {
				
				double motion = Math.hypot(mc.player.getPosY() - mc.player.prevPosY, Math.hypot(mc.player.getPosX() - mc.player.prevPosX, mc.player.getPosZ() - mc.player.prevPosZ)) * 20D;

				if (motion < BoostUtility.lastSpeed && firework.get()) {
					useFirework();
				}

				BoostUtility.lastSpeed = motion;
				
				if (target != null && mc.player.getDistance(target) < 10) {
					if (target.lastSwing.passed(100) && !target.lastSwing.passed(400) && target.tryAttack && mc.player.hurtTime == 0 && misses.get() && !isLeaving(target)) {
						rock.getAlertHandler().alert(target.getName().getString() + " missed shot due to resolver", AlertType.INFO);
						target.tryAttack = false;
					}
				}
				
				for (PlayerEntity ent : mc.world.getPlayers()) {
					if (mc.player.getDistance(ent) < 6 && ent.lastSwing.passed(200) && !ent.lastSwing.passed(400) && ent.tryAttack && mc.player.hurtTime > 0 && switchTarget.get()) {
						aura.focus(ent);
			            
						ent.tryAttack = false;
					}
				}
			}
			
			if (event instanceof EventSpawn e) {
				if (e.getEntity() instanceof EnderPearlEntity ent) {
					mc.world.getPlayers().stream()
			            .min(Comparator.comparingDouble((p) -> p.getDistanceSq(e.getEntity().getPositionVec()) + (mc.player == p || p.getDistance(e.getEntity().getPositionVec()) > 5 ? 10000 : 0)))
			            .ifPresent((player) -> {
			            	if (aura.getPrevTarget() != null && player == aura.getPrevTarget() && targetPearl.get() && ent.getDistance(player) < 5) {
			            		this.pearlEntity = e.getEntity();
			            		
			            		//Chat.debug("pearl finded");
			            	}
			            });
				}
			}
			
			handleDesync(event);
			
			if (target != null || this.lastPos != null) {
				boolean leave = canLeave(target);
				
					for (Entity entity : mc.world.getAllEntities()) {
		    			if (entity instanceof EnderCrystalEntity crystalEntity && mc.player.getDistance(crystalEntity) < 10) {
		    				leave = true;
		    			}
					}
				
				//defensiveActive = mc.player.getDistance(target) > 4;w
				
				defensiveActive = !leave;
				
				
				Vector3d targetVec = this.lastPos != null ? this.lastPos : getPos(target);
				if (targetVec == null) {
					targetVec = target.getPositionVec().add(0, target.getEyeHeight(), 0);
				}
				
				List<Vector3d> leaveVectors = new ArrayList<>();
				for (Select.Element elmt : this.leaveVectors.getToggled()) {
					leaveVectors.add(target == null ? lastPos.add(((LeaveVector)elmt).vec) : target.getEyePosition(0).add(((LeaveVector)elmt).vec));
				}
				
				if (event instanceof EventRender3D e) {
					if (leave && leaveLines.get() && leaveVec != Vector3d.ZERO && target != null) {
						for (Vector3d vector : leaveVectors) {
			                if (MathUtility.canSeen(vector) && vector.y < 400 && (!swapVector.get() || leaveVec != vector)) {
			                	drawLine3D(e.getMatrixStack(), target.getPositionVec().add(0,mc.player.getHeight()/2F,0), vector, FixColor.GREEN);
			                    break;
			                } else {
			                	drawLine3D(e.getMatrixStack(), target.getPositionVec().add(0,mc.player.getHeight()/2F,0), vector, FixColor.RED);
			                }
			            }
					}
					
					if (targetLine.get() && !leave) {
						drawLine3D(e.getMatrixStack(), mc.player.getPositionVec().add(0,mc.player.getHeight()/2F,0), targetVec, FixColor.GREEN);
					}
				}
				
				
				
				
				
				
				
				// Ротация
				if (event instanceof EventJump || event instanceof EventMove || event instanceof EventTrace || event instanceof EventInput || event instanceof EventMotion) {
					if (leave && leaveVec == Vector3d.ZERO) {
						for (Vector3d vector : leaveVectors) {
			                if (MathUtility.canSeen(vector) && vector.y < 400 && (!swapVector.get() || !lastVec.equals(vector))) {
			                	leaveVec = vector;
			                    break;
			                }
			            }
					}
					
					if (!leave && leaveVec != Vector3d.ZERO) {
					//	leaveVec = Vector3d.ZERO;
					}
					
		            
					Vector2f rotation = Rotation.get(leave ? leaveVec : targetVec);
					
					
					//if (this.lastPos != null || leave) {
						Player.look(event, rotation.x, rotation.y, false);
					//}
				}
				
				
				
				
				
				
				if (event instanceof EventAttack e) {
					if (e.getTarget() == target && leave) {
					//	useFirework();
					}
					
					for (Vector3d vector : leaveVectors) {
		                if (MathUtility.canSeen(vector) && vector.y < 255 && (!swapVector.get() || !lastVec.equals(vector.sub(target.getEyePosition(0))))) {
		                	leaveVec = vector;
		                	//Chat.debug("Вектор изменен на " + leaveVec.sub(target.getEyePosition(0)));
		                    break;
		                }
		            }
					
					if (doubleRot.get() && target != null && mc.player.getDistance(target.getPositionVec()) > mc.player.getDistance(target.getPositionVec().add(target.getMotion())) && isLeaving(target)) {
						Vector3d pos = mc.player.getPositionVec();
						Vector2f rotation = Rotation.get(target.getPositionVec().add(0, target.getHeight()/2F, 0));
						Bypass.send(pos.x, pos.y - 1e-6, pos.z, rotation.x, rotation.y, false);
					}
					
					lastVec = leaveVec.sub(target.getEyePosition(0));
				}

				if (event instanceof EventRenderWorldEntities e && resolverPos.get() && target != null && target.getResolvedPos() != null) {
					Render.drawEntity3D(e.getMatrix(), target, getPos(target), 0.2f);
				}
				
				if (event instanceof EventMotionMove e) {
		            for (Vector3d vector : leaveVectors) {
		                if (vector.equals(leaveVec)) {
		                	leaveVec = vector;
		                    break;
		                }
		            }
		            boolean canFreeze = (this.freezeDummy.get() && target != null && mc.player.getDistance(target) < 3 && !(leave)) || leave && mc.player.getDistance(leaveVec) < 1;
		            
		            if (canFreeze) {
						e.setMotion(Vector3d.ZERO);
					}
					if (swapElytra.get()) {
						if (canFreeze && Player.getChest() == Items.ELYTRA) {
							int item = Inventory.getChestplate();
							Player.moveItem(item < 46 ? item : 6, 6, true);
						}

						if (!canFreeze && Player.getChest() != Items.ELYTRA) {
							int item = Player.findItem(45, Items.ELYTRA);
							Player.moveItem(item < 46 ? item : 6, 6, true);
						}
					}
					
					prevFreezed = canFreeze;
				}
				
				if (event instanceof EventUpdate) {
					if (mc.world.getAllEntities().contains(pearlEntity)) {
						lastPos = this.pearlEntity.getPositionVec();
					} else {
						if (this.lastPos != null && mc.player.getDistance(this.lastPos) < 3) {
							this.lastPos = null;
							this.pearlEntity = null;
						}
					}
					
					boolean elytra = Player.find(38).getItem() == Items.ELYTRA;
					if (elytra && !mc.player.isHandActive()) {
						if (mc.player.isElytraFlying()) {
							boolean canFreeze = (this.freezeDummy.get() && target != null && mc.player.getDistance(target) < 3 && mc.player.isElytraFlying() && !(leave)) || leave && mc.player.getDistance(leaveVec) < 1;
				            
							boolean canUse = !(canFreeze && notUse.get());
							if (/*!(EventMotion.LAST_PITCH > 0 && isLeaving(target)) && */this.useTimer.passed(leave && mc.player.getDistance(target) < 6 ? useCooldownLeave.get() : (long) useCooldown.get()) && canUse) {
				                useFirework();
							}
						} else {
							if (mc.player.isOnGround() && !mc.getGameSettings().keyBindJump.isPressed()) {
								mc.player.jump();
							} else {
								if (mc.player.fallDistance > 0.08f) {
									mc.player.startFallFlying();
					                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
					                useFirework();
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			Chat.debug("чек консоль");
			Debugger.print(e);
		}
	}
	@NativeInclude
	private void handleDesync(Event event) {
		Aura aura = rock.getModules().get(Aura.class);
		LivingEntity target = aura.getTarget();
		boolean canUse = target != null && target.getResolvedPos() != null && mc.player.getDistance(target.getResolvedPos()) < 20 && !isLeaving(target);
		
		if (event instanceof EventUpdate && target != null) {
		//	Chat.debug(isLeaving(target));
		}
		
		if (event instanceof EventSendPacket e && defensive.get() && defensiveActive && !mc.isSingleplayer() && canUse) {
			//if (!(e.getPacket() instanceof CPlayerPacket.PositionPacket && e.getPacket() instanceof CPlayerPacket.PositionRotationPacket && e.getPacket() instanceof CPlayerPacket.RotationPacket
			//		&& e.getPacket() instanceof CUseEntityPacket && e.getPacket() instanceof CAnimateHandPacket))
			
			packets.add(e.getPacket());
			event.cancel();
		}
		
		if (event instanceof EventReceivePacket e) {
	    	if (e.getPacket() instanceof SPlayerPositionLookPacket) {
	    		//Chat.debug(defensiveActive + " - " + defensiveTimer.getElapsed());
	    	}
	    }
		
		if (event instanceof EventUpdate) {
			if ((!defensive.get() || !defensiveActive || defensiveTimer.passed(1000) || !canUse) && !mc.isSingleplayer()) {
				for (IPacket<?> p : packets) {
					mc.player.connection.sendPacketSilent(p);
				}
				packets.clear();
				defensivePos = mc.player.getPositionVec();
				defensiveTimer.reset();
			}
			
			if (!lastDefensive && defensiveActive) {
				defensivePos = mc.player.getPositionVec();
				defensiveTimer.reset();
			}
			
			lastDefensive = defensiveActive;
		}
		
		if (event instanceof EventRenderWorldEntities e && defensive.get() && defensiveActive && defensivePos != null && canUse) {
			Render.drawEntity3D(e.getMatrix(), mc.player, defensivePos, 0.2f);
		}
	}
	
	public boolean isLeaving(LivingEntity target) {
		//mc.player.getDistance(target.getPositionVec()) < mc.player.getDistance(target.getPositionVec().add(target.getMotion()))
		if (target.isElytraFlying() && target.lastSwing.passed(2000))
			target.leaving.reset();
		
		return target.lastSwing.passed(2000) && target.isElytraFlying();
	}
	@NativeInclude
	private boolean canLeave(LivingEntity target) {
		if (target == null || isLeaving(target)) 
			return false;
		
		if (health.get() && (target.isElytraFlying() || !onlyGround.get()) && (mc.player.getHealth() + mc.player.getAbsorptionAmount() < leaveHealth.get())) 
			return true;
		
		if (cooldown.get() && mc.player.getCooledAttackStrength() < IdealHitUtility.getAICooldown()) 
			return true;
		
		if (useItem.get() && mc.player.isHandActive()) 
			return true;
		
		if (bind.get() && bindLeaving)
			return true;
		
		return false;
	}
	@NativeInclude
	private void useFirework() {
		int firework = Inventory.findItemNoChanges(44, Items.FIREWORK_ROCKET);
		if (firework >= 0) {
			if (firework != 45) {
				if (firework < 9) {
					mc.player.connection.sendPacket(new CHeldItemChangePacket(firework));
					mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
					mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
				} else {
					Player.moveItem(firework, 35 + (int) slot.get(), true);
					mc.player.connection.sendPacket(new CHeldItemChangePacket((int) slot.get() - 1));
					mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
					mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
				}
			} else {
				mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
			}

			this.useTimer.reset();
		} else {
			Chat.msg("Фейерверков нет.");
			onDisable();
			this.set(false);
		}
	}
	
	private void drawLine3D(MatrixStack ms, Vector3d from, Vector3d to, FixColor color) {
		if (from == null || to == null) return;
		
    	ms.push();

		// Устанавливаем режимы отрисовки (чтобы все заебись было тип)
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        
        GL11.glDepthMask(false);
    	GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0);
        
        Vector3d renderOffset = mc.getRenderManager().info.getProjectedView();

        // Меняем позицию на позицию отрисовки(чтобы они не рендерились в пизде)
        ms.translate(-renderOffset.x, -renderOffset.y, -renderOffset.z);
        
        glLineWidth(3);
        
        BUILDER.begin(1, DefaultVertexFormats.POSITION_COLOR);
        
        Matrix4f matrix = ms.getLast().getMatrix();

        BUILDER.pos(matrix, (float) from.x, (float) from.y, (float) from.z).color(color).endVertex();
        BUILDER.pos(matrix, (float) to.x, (float) to.y, (float) to.z).color(color).endVertex();

        
        TESSELLATOR.draw();

        // Выключаем режимы рендеринга
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        Render.resetColor();
        
        ms.pop();
	}
	
	@Override
	@NativeInclude
	public void onEnable() {
		if (swapElytra.get() && Player.find(38).getItem() != Items.ELYTRA) {
			int item = Player.findItem(45, Items.ELYTRA);
			Player.moveItemOld(item < 46 ? item : 6, 6, true);
		}
	}

	@Override
	@NativeInclude
	public void onDisable() {
		if (swapElytra.get() &&  Player.find(38).getItem() == Items.ELYTRA) {
			int item = Inventory.getChestplate();
			Player.moveItemOld(item < 46 ? item : 6, 6, true);
		}
		this.lastPos = null;
		this.pearlEntity = null;
	}
	
	public float getRange() {
		return get() ? 125 : 0;
	}
	
	public Vector3d getPos(LivingEntity entity) {
		Map<String, Integer> pings = PlayerTabOverlayGui.getPlayerPings();
		String targetName = entity.getName().getString();
		int targetPing = pings.containsKey(targetName) ? pings.get(targetName) : 0;
		Vector3d defaultPos = resolver.get() && entity.getResolvedPos() != null ? entity.getResolvedPos() : entity.getPositionVec();
		
		if (!predict.get()) {
			return defaultPos;
		}
		
		defaultPos = predictDefensive.get() && entity.getResolvedPos() != null && entity.isElytraFlying()
				? PredictUtility.predictElytraPos(entity, entity.getResolvedPos(), (int) (entity.getLastResolve().getElapsed()/50))
						: entity.getPositionVec();
		
		Vector3d leavePos = span.get() && isLeaving(entity)
				? entity.getPositionVec().add(entity.getMotion().scale(2 + Server.ping() / 50F)) // PredictUtility.predictElytraPos(entity, entity.getPositionVec(), targetPing/50+5)
				: defaultPos;
		
		return resolver.get() && entity.getResolvedPos() != null
    			? leavePos
    			: entity.getPositionVec();
	}
	
	private BlockPos calcTrajectory(Entity e) {
        return traceTrajectory(e.getPosX(), e.getPosY(), e.getPosZ(), e.getMotion().x, e.getMotion().y, e.getMotion().z);
    }
	@NativeInclude
	private BlockPos traceTrajectory(double x, double y, double z, double mx, double my, double mz) {
    	Vector3d lastPos;
        for (int i = 0; i < 300; i++) {
            lastPos = new Vector3d(x, y, z);
            x += mx;
            y += my;
            z += mz;
            mx *= 0.99;
            my *= 0.99;
            mz *= 0.99;
            my -= 0.03f;
            Vector3d pos = new Vector3d(x, y, z);
            BlockRayTraceResult bhr = mc.world.rayTraceBlocks(new RayTraceContext(lastPos, pos, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, mc.player));
            if (bhr != null && bhr.getType() == RayTraceResult.Type.BLOCK) return bhr.getPos();

            for (Entity ent : mc.world.getAllEntities()) {
                if (ent instanceof ArrowEntity || ent == mc.player || ent instanceof EnderPearlEntity) continue;
                if (ent.getBoundingBox().intersects(new AxisAlignedBB(x - 0.3, y - 0.3, z - 0.3, x + 0.3, y + 0.3, z + 0.2)))
                    return null;
            }

            if (y <= -65) break;
        }
        return null;
    }
	
	public float overrideRange(LivingEntity target) {
		return Server.isBravo() && doubleRot.get() && target != null && mc.player.getDistance(target.getPositionVec()) > mc.player.getDistance(target.getPositionVec().add(target.getMotion())) && isLeaving(target) ? 6 : 3;
	}
	
	public boolean canAttack(LivingEntity target) {
		return true;//AuraUtility.distanceTo(AuraUtility.getPoint(target)) < 2.5f && target.getLastResolve().getElapsed() <= 50L || !isLeaving(target) || !get();
	}

	private void changeChestPlate(ItemStack stack) {
		if (mc.currentScreen != null) {
			return;
		}
		if (stack.getItem() != Items.ELYTRA) {
			int elytraSlot = getItemSlot(Items.ELYTRA);
			if (elytraSlot >= 0) {
				InventoryUtility.moveItem(elytraSlot, 6);
				return;
			}
		}
		int armorSlot = getChestPlateSlot();
		if (armorSlot >= 0) {
			InventoryUtility.moveItem(armorSlot, 6);
		}
	}

	private int getItemSlot(Item input) {
		int slot = -1;
		for (int i = 0; i < 36; i++) {
			ItemStack s = mc.player.inventory.getStackInSlot(i);
			if (s.getItem() == input) {
				slot = i;
				break;
			}
		}
		if (slot < 9 && slot != -1) {
			slot = slot + 36;
		}
		return slot;
	}

	private int getChestPlateSlot() {
		Item[] items = {Items.NETHERITE_CHESTPLATE, Items.DIAMOND_CHESTPLATE};

		for (Item item : items) {
			for (int i = 0; i < 36; ++i) {
				Item stack = mc.player.inventory.getStackInSlot(i).getItem();
				if (stack == item) {
					if (i < 9) {
						i += 36;
					}
					return i;
				}
			}
		}
		return -1;
	}
	
	class LeaveVector extends Select.Element {
		
		Vector3d vec;

		public LeaveVector(Select parent, String name, Vector3d vec) {
			super(parent, name);
			this.vec = vec;
			set(true);
		}
		
	}
}
