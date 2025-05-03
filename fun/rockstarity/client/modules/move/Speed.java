package fun.rockstarity.client.modules.move;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventWorldChange;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.player.EventJump;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventTrace;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.player.Bypass;
import fun.rockstarity.api.helpers.player.Inventory;
import fun.rockstarity.api.helpers.player.Move;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import javafx.animation.Interpolator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CConfirmTeleportPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CEntityActionPacket.Action;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author Malecharik
 * @since 9 Mar 2024 00:21:15
 */


@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Info(name = "Speed", desc = "Ускоряет игрока", type = Category.MOVE)
public class Speed extends Module {
	@NonFinal boolean wasTimer;
	@NonFinal double pol;
	@NonFinal boolean alreadyUsed;
	@NonFinal int lastSlot = -1;
	TimerUtility timers = new TimerUtility();

	Mode mode = new Mode(this, "Режим");
	Mode.Element vanila = new Mode.Element(mode, "Vanila");
	Mode.Element airtick = new Mode.Element(mode, "Intave");
	Mode.Element ft = new Mode.Element(mode, "FunTime");
	Mode.Element ft2 = new Mode.Element(mode, "Коллизия");
	Mode modeCollision = new Mode(ft2, "Режим коллизии").hide(() -> !mode.is(ft2));
	Mode.Element oldCollision = new Mode.Element(modeCollision, "Старый");
	Mode.Element newCollision = new Mode.Element(modeCollision, "Новый");
	Mode.Element spookySilent = new Mode.Element(modeCollision, "Spooky тихий");
	Mode.Element custom = new Mode.Element(modeCollision, "Кастом");
	CheckBox onGround = new CheckBox(ft2, "Работать на земле");
	Mode speedModification = new Mode(custom, "Режим модификации");
	Mode.Element target = new Mode.Element(speedModification, "Таргет");
	Mode.Element accelerate = new Mode.Element(speedModification, "Ускорение");
	Slider speed2 = new Slider(custom, "Скорость").min(1).max(3).inc(0.05f).set(1.5f);
	Slider distance = new Slider(custom, "Дистанция").min(0.5f).max(2).inc(0.1f).set(0.5f);
	CheckBox check = new CheckBox(custom, "Проверять на кулдаун").desc("Если вы получаете урон, или у вас кулдаун руки, то вы не ускоряетесь");
	//Mode.Element ft3 = new Mode.Element(mode, "FunTime");
	Mode.Element hw = new Mode.Element(mode, "HolyWorld");
	Mode hwMode = new Mode(hw, "Режим");
	Mode.Element hwFast = new Mode.Element(hwMode, "Быстрый");
	Mode.Element hwSlow = new Mode.Element(hwMode, "Пассивный медленный");
	Slider delay = new Slider(hwSlow, "Задержка").min(10).max(150).inc(2).set(50);
	Slider speed1 = new Slider(hwSlow, "Скорость").min(1).max(1.2f).inc(0.01f).set(1.05f);
	Mode.Element st = new Mode.Element(mode, "Spooky");
	Mode.Element stTimer = new Mode.Element(mode, "Spooky Timer");
	Mode.Element slow = new Mode.Element(mode, "TimerSlow");
	Mode.Element timer = new Mode.Element(mode, "TimerFast");
	Mode.Element matrixFlag = new Mode.Element(mode, "Matrix flag");
	Mode.Element longHop = new Mode.Element(mode, "LongHop");
	Mode.Element meta = new Mode.Element(mode, "MetaHvH/AnACI");
	Mode.Element elytra = new Mode.Element(mode, "Элитры");
	
	Slider speed = new Slider(ft, "Скорость").min(1).max(15).inc(1).set(5).hide(() -> !mode.is(ft));
	
	@NonFinal BlockPos pos;
	@NonFinal BlockState state;
	
	@NonFinal BlockPos startBlock;
	TimerUtility timerUtility = new TimerUtility();
	TimerUtility timerUtil = new TimerUtility();
	@NonFinal boolean boosting;
	
	@NonFinal int jumps;
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventUpdate && stTimer.get()) { // By 4upikkCoder
			if (timerUtil.passed(1100)) {
	            boosting = true;
	        }
	        if (timerUtil.passed(7000)) {
	            boosting = false;
	            timerUtil.reset();
	        }
	        if (boosting) {
	            if (mc.player.isOnGround() && !mc.gameSettings.keyBindJump.isPressed()) {
	                mc.player.jump();
	            }
	            mc.timer.timerSpeed = (mc.player.ticksExisted % 2 == 0 ? 1.5f : 1.2f);
	        } else {
	            mc.timer.timerSpeed = (0.05f);
	        }
		}
		
		if (ft.get()) {
			if (event instanceof EventMotion) {
				if (Player.getBlock(0, -.05f, 0) != Blocks.AIR) {
	            	mc.player.getMotion().x *= 1 + 0.01f * speed.get();
					mc.player.getMotion().z *= 1 + 0.01f * speed.get();
	            }
			}
		}

		if (longHop.get()) {
			if (mc.player.fallDistance >= 0.04f && Move.isMoving()) {
				float f = mc.player.rotationYawHead;
				float f2 = mc.player.rotationPitch;
				double d3 = -Math.sin((double)f / 180.0 * Math.PI) * Math.cos((double)f2 / 180.0 * Math.PI);
				double d2 = Math.cos((double)f / 180.0 * Math.PI) * Math.cos((double)f2 / 180.0 * Math.PI);
				mc.player.setVelocity(d3, -0.6, d2);
			}
		}
		
		if (airtick.get()) {
			if (event instanceof EventJump) {
				jumps++;
				
				if (jumps >= 32) {
					rock.getAlertHandler().alert("Отключаю спиды чтобы избежать флага", AlertType.ERROR);
		    		set(false);
		    		onDisable();
				}
			}
			if (event instanceof EventMotion) {
				for (int i = 0; i < 10; i++) {
					mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, Action.PRESS_SHIFT_KEY));
				}
				mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, Action.RELEASE_SHIFT_KEY));
			}
			if (event instanceof EventMotion
					&& (mc.player.getMotion().y > 0 && mc.player.getMotion().y < 0.10f
					|| mc.player.fallDistance > 0.3f && mc.player.fallDistance < 0.4f)) {
				
				mc.player.getMotion().x *= 1.07f;
				mc.player.getMotion().z *= 1.07f;
			}
			if (event instanceof EventReceivePacket e) {
		    	if (e.getPacket() instanceof SPlayerPositionLookPacket) {
		    		rock.getAlertHandler().alert("Обнаружен флаг. Выключаю спиды" + (rock.isDebugging() ? " " + jumps : ""), AlertType.ERROR);
		    		set(false);
		    		onDisable();
		    	}
		    }
		}

		if (mode.is(meta)) {
			if (event instanceof EventUpdate) {
				ItemStack offHandItem = mc.player.getHeldItemOffhand();
				EffectInstance speedEffect = mc.player.getActivePotionEffect(Effects.SPEED);
				EffectInstance DeEffect = mc.player.getActivePotionEffect(Effects.SLOWNESS);
				float appliedSpeed = getAppliedSpeed(offHandItem, speedEffect, DeEffect);

				Move.setSpeed(appliedSpeed);
			}
		}
		

 		if (mode.is(st)) {
 			if (event instanceof EventUpdate) {
 				if (!Bypass.via() || Inventory.findItemNoChanges(44, Items.ICE) == -1) {
 					rock.getAlertHandler().alert("Для использования этого режима необходим лёд в хотбаре и версия 1.17.1", AlertType.ERROR);
 					set(false);
 					return;
 				}
 
 				BlockPos pos = mc.player.getPosition().add(0,-1,0);
 				if ((mc.world.isAirBlock(pos) || mc.world.getBlockState(pos).getBlock().canSpawnInBlock() || (!mc.world.isAirBlock(pos.up()) && mc.world.getBlockState(pos.up()).getBlock().canSpawnInBlock())) || !mc.getGameSettings().keyBindJump.isKeyDown() || !mc.player.isOnGround())
 	                return;
 
 				int i = Inventory.findItemNoChanges(44, Items.ICE);
 				boolean inHotbar = i <= 8;
 				if(i != -1 && inHotbar) {
 					Bypass.send(mc.player.rotationYaw, 90);
 
 					if (inHotbar) {
 						mc.player.connection.sendPacket(new CHeldItemChangePacket(i));
 						mc.player.inventory.currentItem = i;
 					} else {
 						mc.playerController.pickItem(i);
 					}
 					//mc.player.connection.sendPacket(new CHeldItemChangePacket(i));
 					this.pos = pos;
 					this.state = mc.world.getBlockState(pos);
 
 					//mc.player.connection.sendPacket(new CHeldItemChangePacket(i-36));
 					mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.START_DESTROY_BLOCK, pos, Direction.UP));
 					mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.UP));
 		            mc.player.connection.sendPacket(new CPlayerTryUseItemOnBlockPacket(Hand.MAIN_HAND, new BlockRayTraceResult(pos.down().getVec().add(0.5, 1, 0.5), Direction.UP, pos.down(), true)));
 		            mc.world.setBlockState(pos, Blocks.ICE.getDefaultState());
 		            //mc.player.getMotion().x /= 0.98F;
 					//mc.player.getMotion().y /= 0.98F;
 		            if (!inHotbar) {
 						mc.playerController.pickItem(i);
 					} else {
 
 					}
 
 
 					//mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
 				} else {
 				 	rock.getAlertHandler().alert("Этот режим работает только с блоками льда в хотбаре", AlertType.INFO);
 					set(false);
 					onDisable();
 				}
 
 
 				//if (!mc.player.isSneaking()) return;
 				//Move.setSpeed(pol = (float) Interpolator.LINEAR.interpolate(pol, this.speed.get(), 0.1f / Math.max((float) Minecraft.debugFPS, 5) * 175));
 			}
 		}
		
		
		if (mode.is(hw)) {
			if (hwMode.is(hwFast)) {
				if (event instanceof EventUpdate) {
					BlockPos pos = mc.player.getPosition().add(0, -1, 0);
					if ((mc.world.isAirBlock(pos) || mc.world.getBlockState(pos).getBlock().canSpawnInBlock() || (!mc.world.isAirBlock(pos.up()) && mc.world.getBlockState(pos.up()).getBlock().canSpawnInBlock())) || !mc.getGameSettings().keyBindJump.isKeyDown() || !mc.player.isOnGround())
						return;

					int i = Inventory.findItemNoChanges(44, Items.ICE);
					boolean inHotbar = i <= 8;
					if (i != -1 && inHotbar) {
						if (Bypass.via()) {
							Bypass.send(mc.player.rotationYaw, 90);
						}

						mc.player.inventory.currentItem = i;
						//mc.player.connection.sendPacket(new CHeldItemChangePacket(i));
						this.pos = pos;
						this.state = mc.world.getBlockState(pos);

						//mc.player.connection.sendPacket(new CHeldItemChangePacket(i-36));
						mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.START_DESTROY_BLOCK, pos, Direction.UP));
						mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.UP));
						mc.player.connection.sendPacket(new CPlayerTryUseItemOnBlockPacket(Hand.MAIN_HAND, new BlockRayTraceResult(pos.down().getVec().add(0.5, 1, 0.5), Direction.UP, pos.down(), true)));
						mc.world.setBlockState(pos, Blocks.ICE.getDefaultState());
						//mc.player.getMotion().x /= 0.98F;
						//mc.player.getMotion().y /= 0.98F;

						//mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
					} else {
						rock.getAlertHandler().alert("Этот режим работает только с блоками льда в хотбаре", AlertType.INFO);
						set(false);
						onDisable();
					}


					//if (!mc.player.isSneaking()) return;
					//Move.setSpeed(pol = (float) Interpolator.LINEAR.interpolate(pol, this.speed.get(), 0.1f / Math.max((float) Minecraft.debugFPS, 5) * 175));
				}

				if (event instanceof EventMotion e && !Bypass.via()) {
					e.setPitch(90);
				}
			} else {
				if (event instanceof EventMotion) {
					if (timerUtility.passed(delay.get()) && mc.player.isOnGround() && !mc.gameSettings.keyBindJump.isKeyDown()) {
						mc.player.getMotion().x *= speed1.get();
						mc.player.getMotion().z *= speed1.get();
						timerUtility.reset();
					}
				}
			}
		}
		
		if (this.mode.is(this.elytra)) {
			if (event instanceof EventMotion e) {
				if (!mc.player.isElytraFlying()) {
		            if (mc.player.isOnGround()) {
		            	mc.player.startFallFlying();
		                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
		                mc.player.getMotion().y = -0.36;
		            }
		        }
			}
			
			Player.look(event, mc.player.rotationYaw, (float) (pol = Interpolator.LINEAR.interpolate(pol, 90, 0.7f)), true);
		}
		
		if (this.mode.is(this.ft2)) {
			if (event instanceof EventMotion) {
				if (modeCollision.is(oldCollision)) {
					for (Entity entity : mc.world.getAllEntities()) {
						if (!(entity instanceof LivingEntity)) continue;

						final double speed = Math.hypot(Math.abs(entity.prevPosX - entity.getPosX()), Math.abs(entity.prevPosZ - entity.getPosZ()));

						if (Server.isFT()) {
							if (mc.player.getDistance(entity) < 1.5f && speed < 0.1f) {
								final float p = mc.world.getBlockState(mc.player.getPosition().add(mc.player.getMotion().x, mc.player.getMotion().y, mc.player.getMotion().z)).getBlock().getSlipperiness();
								final float f = mc.player.isOnGround() ? p * 0.21f : 0.61f;
								final float f2 = mc.player.isOnGround() ? p : 0.81f;
								mc.player.setVelocity(mc.player.getMotion().getX() / f * f2, mc.player.getMotion().getY(), mc.player.getMotion().getZ() / f * f2);
								break;
							}
						} else {
							if (mc.player.getDistance(entity) < 1.5f && speed < 0.1f) {
								final float p = mc.world.getBlockState(mc.player.getPosition().add(mc.player.getMotion().x, mc.player.getMotion().y, mc.player.getMotion().z)).getBlock().getSlipperiness();
								final float f = mc.player.isOnGround() ? p * 0.91f : 0.81f;
								final float f2 = mc.player.isOnGround() ? p : 0.99f;
								mc.player.setVelocity(mc.player.getMotion().getX() / f * f2, mc.player.getMotion().getY(), mc.player.getMotion().getZ() / f * f2);
								break;
							}
						}
					}
				} else if (modeCollision.is(newCollision)) {
					if (canBoostFromEntity()) mc.player.jumpMovementFactor *= 2.5f;
				} else if (modeCollision.is(custom)){
					if (canBoostFromEntityCustom()) {
						if (speedModification.is(target)) {
							mc.player.jumpMovementFactor += mc.player.jumpMovementFactor * speed2.get();

							if (Server.isServerForHPFix()) mc.player.jumpMovementFactor -= mc.player.jumpMovementFactor * 0.5f;

							mc.player.jumpMovementFactor = (float) MathHelper.clamp(mc.player.jumpMovementFactor, 0.026, mc.player.jumpMovementFactor);
						} else {
							float value = speed2.get() == 1 ? 0.02f : speed2.get() /
									(Server.isServerForHPFix() ? 20 : 10);
							mc.player.setMotion(mc.player.getMotion().mul(1 + value, 1, 1 + value));
						}
					}
				} else {
					if (canBoostFromEntity()) {
						float value = 1.05f / 10;
						mc.player.jumpMovementFactor *= 1.05f;
						mc.player.setMotion(mc.player.getMotion().mul(1 + value, 1, 1 + value));
					}
				}
			}
		}

//		if (this.mode.is(this.ft)) { //НА СВЕТЛОЕ БУДУЩЕЕ :((
//			if (event instanceof EventUpdate) {
//				/*
//				if (mc.player.isPotionActive(Effects.SPEED)) {
//					if (mc.player.isOnGround()) {
//						mc.player.jump();
//						mc.player.getMotion().y -= 0.38f;
//						mc.player.getMotion().x *= 1.01f;
//						mc.player.getMotion().z *= 1.01f;
//					} else {
//						//mc.player.getMotion().y = -0.38f;
//						//mc.player.getMotion().x *= 1.02f;
//						//mc.player.getMotion().z *= 1.02f;
//					}
//				} else {
//					if (mc.player.isOnGround()) {
//						mc.player.jump();
//						mc.player.getMotion().y -= 0.4f;
//						mc.player.getMotion().x *= 1.01f;
//						mc.player.getMotion().z *= 1.01f;
//					}
//				}
//				*/
//
//				
//				BlockPos pos = mc.player.getPosition();
//	            BlockPos target = pos.up(-1);
//	            BlockPos target1 = pos.up(0);
//
//
//	            Vector3d vector3d = new Vector3d(0.5f,0.5f,0.5f);
//	            BlockRayTraceResult result = new BlockRayTraceResult(
//	                    vector3d,
//	                    Direction.UP,
//	                    target,
//	                    false
//	            );
//	            Vector3d vector3d1 = new Vector3d(0.5f,0.5f,0.5f);
//	            BlockRayTraceResult result1 = new BlockRayTraceResult(
//	                    vector3d1,
//	                    Direction.UP,
//	                    target1,
//	                    false
//	            );
//
//
//
//	            int slot = -1; 
//	            for (int i = 0; i < 9; i++) { 
//	                if (mc.player.inventory.getStackInSlot(i).getItem() == Items.TRIPWIRE_HOOK) { 
//	                    slot = i; 
//	                    break;
//	                }
//	            }
//
//	            if (slot != -1) { 
//	                mc.player.connection.sendPacket(new CHeldItemChangePacket(slot));
//	                
//	                mc.player.connection.sendPacket(new CPlayerTryUseItemOnBlockPacket(Hand.MAIN_HAND, result1));
//	                
//	                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
//	                
//	                mc.player.getMotion().x *= 1.1;
//	                mc.player.getMotion().z *= 1.1;
//	            }
//	            
//	            
//			}
//			
//			if (event instanceof EventMotion e) {
//				this.pol = (float) Interpolator.LINEAR.interpolate(this.pol, 90, 0.7f);
//				mc.player.rotationPitchHead = (float) this.pol;
//				e.setPitch((float) this.pol);
//			}
//			
//			if (event instanceof EventTrace e) {
//				e.setPitch((float) this.pol);
//				e.cancel();
//			}
//		}
//		
		/*
		 * Обрабатываем событие Update
		 */
		if (event instanceof EventUpdate) {
			
			if (this.mode.is(this.timer)) {
		        if (this.timers.passed(1150L)) {
		            if (mc.player.isOnGround() && !mc.getGameSettings().keyBindJump.isPressed()) {
		                mc.player.jump();
		            }
		            mc.timer.timerSpeed = mc.player.ticksExisted % 2 == 0 ? 1.5f : 1.2f;
		        } else if (this.timers.passed(7000L)) {
		            mc.timer.timerSpeed = 0.05f;
		            this.timers.reset();
		        }
			} else if (this.mode.is(this.slow)) {
				//Отменяем нажатие прыжка дабы избежать флагов
				mc.getGameSettings().keyBindJump.setPressed(false);
				//Если игрок на земле, прыгаем
				if (mc.player.isOnGround()) mc.player.jump();
				//Если дистанция падения меньше 0.01 то уменьшаем скорость, а если больше ускоряем
				if (mc.player.fallDistance < .01) {
					mc.timer.timerSpeed = 0.1f;
				} else {
					mc.timer.timerSpeed = 3;
				}
			} else if (this.mode.is(this.matrixFlag)) {
	            if (mc.player.getMotion().y != -0.0784000015258789) {
	                this.timers.reset();
	            }
	            if (this.timers.passed(100)) {
					mc.player.getMotion().y = 0.4229;
					Move.setSpeed(1.953);
	            }
			}
		}
		
		if (mode.is(vanila)) {
			if (event instanceof EventUpdate) {
				if (Move.isMoving()) {
					if (mc.player.isOnGround()) {
						mc.gameSettings.keyBindJump.setPressed(false);
						mc.timer.reset();
						mc.player.jump();
					}
					
					if (mc.player.getMotion().y > 0.003) {
						mc.player.getMotion().x *= 1.0011;
		                mc.player.getMotion().z *= 1.0011;
		                mc.timer.timerSpeed = 1.03f;
					}
				}
			}
		}
		
		if (event instanceof EventReceivePacket e) {
			
			IPacket packet = e.getPacket();
			if (packet instanceof SPlayerPositionLookPacket tpPacket && this.mode.is(this.matrixFlag)) {
				mc.player.setPosition(tpPacket.getX(), tpPacket.getY(), tpPacket.getZ());
				mc.player.connection.sendPacket(new CConfirmTeleportPacket(tpPacket.getTeleportId()));
				mc.player.getMotion().y = 0.4229;
				Move.setSpeed(1.953);
				event.cancel();
			}
		}
		if (this.mode.is(this.matrixFlag)) {
			if (event instanceof EventWorldChange) {
				this.toggle();
			}
		}
	}

	private static float getAppliedSpeed(ItemStack offHandItem, EffectInstance speedEffect, EffectInstance DeEffect) {
		String itemName = offHandItem.getDisplayName().getString();
		float appliedSpeed = 0;
		if (speedEffect != null) {
			if (speedEffect.getAmplifier() == 2) {
				appliedSpeed = 0.34f * 1.155F;
				if (itemName.contains("Ломтик Дыни")) {
					if (speedEffect.getAmplifier() == 2) appliedSpeed = 0.41755F; else {
						appliedSpeed = 0.41755F * 0.52F;
					}
				}
			}
			else if (speedEffect.getAmplifier() == 1) appliedSpeed = 0.35f;
		}
		else appliedSpeed = 0.34f * 0.68F;
		if (DeEffect != null) appliedSpeed *= 0.835f;
		if (!mc.player.isOnGround()) appliedSpeed *= 1.435F;

		return appliedSpeed;
	}

	private boolean canBoostFromEntity() {
		AxisAlignedBB aabb = mc.player.getBoundingBox().grow(0.5);
		int armorstans = mc.world.getEntitiesWithinAABB(ArmorStandEntity.class, aabb).size();
		boolean canBoost = armorstans > 1 || mc.world.getEntitiesWithinAABB(LivingEntity.class, aabb).size() > 1;

		AxisAlignedBB aabb1 = mc.player.getBoundingBox().grow(-0.25);
		int armorstans1 = mc.world.getEntitiesWithinAABB(ArmorStandEntity.class, aabb1).size();
		boolean canBoost1 = armorstans1 > 1 || mc.world.getEntitiesWithinAABB(LivingEntity.class, aabb1).size() > 1;

		return canBoost && !canBoost1 && (mc.player.swingProgress < 0.3 || mc.player.hurtTime < 5) &&
				(onGround.get() || !mc.player.isOnGround())
				&& !(mc.player.isInWater() || mc.player.isInLava() || mc.player.isOnLadder())
				&& Move.isMoving() && !(mc.player.fallDistance > 1 && mc.player.fallDistance < 1.14);
	}

	private boolean canBoostFromEntityCustom() {
		AxisAlignedBB aabb = mc.player.getBoundingBox().grow(distance.get() - 0.5);
		int armorstans = mc.world.getEntitiesWithinAABB(ArmorStandEntity.class, aabb).size();
		boolean canBoost = armorstans > 1 || mc.world.getEntitiesWithinAABB(LivingEntity.class, aabb).size() > 1;

		AxisAlignedBB aabb1 = mc.player.getBoundingBox().grow(-0.25);
		int armorstans1 = mc.world.getEntitiesWithinAABB(ArmorStandEntity.class, aabb1).size();
		boolean canBoost1 = armorstans1 > 1 || mc.world.getEntitiesWithinAABB(LivingEntity.class, aabb1).size() > 1;
		
		return canBoost && !canBoost1 && (mc.player.swingProgress < 0.3 || mc.player.hurtTime < 5) &&
				(onGround.get() || !mc.player.isOnGround())
				&& !(mc.player.isInWater() || mc.player.isInLava() || mc.player.isOnLadder())
				&& Move.isMoving() && !(mc.player.fallDistance > 1 && mc.player.fallDistance < 1.14);
	}
	
	@Override
	@NativeInclude
	public void onEnable() {
		pol = Move.getSpeed();
		this.startBlock = mc.player.getPosition();
//		if (mode.is(ft3))
//			Move.setSpeed(0);
		jumps = 0;
	}
	
	/*
	 * Обрабатываем выключение
	 */
	
	@Override
	@NativeInclude
	public void onDisable() {
		mc.timer.reset();
		pol = Move.getSpeed();
		wasTimer = true;
		this.alreadyUsed = false;
		mc.getGameSettings().keyBindJump.setPressed(false);
	}
}
