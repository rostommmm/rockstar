package fun.rockstarity.api.helpers.math.aura;

import static net.minecraft.util.math.MathHelper.wrapDegrees;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.connection.globals.ClientAPI;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import fun.rockstarity.api.sounds.Sound;
import fun.rockstarity.client.modules.combat.Aura;
import fun.rockstarity.client.modules.combat.BackTrack;
import fun.rockstarity.client.modules.combat.ElytraTarget;
import fun.rockstarity.client.modules.other.Sounds;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

/**
 * @author ConeTin
 * @since 6 дек. 2023 г.
 */


@UtilityClass
public class AuraUtility implements IAccess {
	
	public LivingEntity calculateTarget(Vector3d from, double range, boolean players, boolean mobs, boolean invisibles, boolean naked, boolean bots, boolean friends, boolean users, boolean fovsort, boolean distantsort, boolean healthSort, boolean damageTakenSort, boolean damageDealtSort) {
		return calculateTarget(from, range, players, mobs, invisibles, naked, bots, friends, users, fovsort, distantsort, healthSort, damageTakenSort, damageDealtSort, 180, mc.player.rotationYaw, true);
	}
	
	public LivingEntity calculateTarget(Vector3d from, double range, boolean players, boolean mobs, boolean invisibles, boolean naked, boolean bots, boolean friends, boolean users, boolean fovsort, boolean distantsort, boolean healthSort, boolean damageTakenSort, boolean damageDealtSort, float fov, float rotation, boolean walls) {

		    var targets = new ArrayList<LivingEntity>();
		    List<String> targetHandlerTargets = rock.getTargetHandler().getTarget();

		    for (Entity ent : mc.world.getAllEntities()) {
		        if (isValidTarget(ent, players, mobs, invisibles, naked, bots, friends, users, fov, rotation)) {
		            double dist = from.add(new Vector3d(0, mc.player.getEyeHeight(), 0))
		                .distanceTo(getPoint((LivingEntity) ent, true));
		            if (dist < range) {
		                if (!walls && !MathUtility.canSeen(getPoint((LivingEntity) ent))) continue;
		                targets.add((LivingEntity) ent);
		            }
		        }
		    }

		    targets.sort((e1, e2) -> Boolean.compare(
		        targetHandlerTargets.contains(e2.getName().getString()),
		        targetHandlerTargets.contains(e1.getName().getString())
		    ));

		    if (fovsort) {
		        targets.sort(Comparator.comparingDouble(Player::getAngle));
		    } else if (distantsort) {
		        targets.sort(Comparator.comparingDouble(mc.player::getDistance));
		    } else if (healthSort) {
		        targets.sort(Comparator.comparingDouble(LivingEntity::getHealth));
		    } else if (damageTakenSort) {
		        targets.sort(Comparator.comparingDouble(AuraUtility::estimateTargetDPS));
		    } else if (damageDealtSort) {
		        targets.sort(Comparator.comparingDouble(AuraUtility::estimateMyDPSOnTarget));
		    }

		    return targets.isEmpty() ? null : targets.get(0);
		}
	
	public LivingEntity calculateCreeper(double range) {
		var targets = new ArrayList<LivingEntity>();
		List<String> targetHandlerTargets = rock.getTargetHandler().getTarget();

		for (Entity ent : mc.world.getAllEntities()) {
			if (isValidTarget(ent) && Math.abs(mc.player.getPosY() - ent.getPosY()) < 4) {
				double dist = mc.player.getPositionVec()
						.add(new Vector3d(0, mc.player.getEyeHeight(), 0)).distanceTo(getPoint((LivingEntity) ent, true));
				if (dist < range) {
					targets.add((LivingEntity) ent);
				}
			}
		}

		targets.sort((e1, e2) -> Boolean.compare(targetHandlerTargets.contains(e2.getName().getString()), targetHandlerTargets.contains(e1.getName().getString())));
		targets.sort(Comparator.comparingDouble(mc.player::getDistance));

		return targets.isEmpty() ? null : targets.get(0);
	}

	public boolean isValidTarget(Entity ent, boolean players, boolean mobs, boolean invisibles, boolean naked, boolean bots, boolean friends, boolean users) {
		return isValidTarget(ent, players, mobs, invisibles, naked, bots, friends, users, 180, mc.player.rotationYaw);
	}
	
	public boolean isValidTarget(Entity ent, boolean players, boolean mobs, boolean invisibles, boolean naked, boolean bots, boolean friends, boolean users, float fov, float rotation) {
	    if (!((ent instanceof PlayerEntity && players) || ((ent instanceof MobEntity || ent instanceof AnimalEntity || ent instanceof VillagerEntity) && mobs))) {
	        return false;
	    }
	    if (Player.getAngle(ent, rotation) > fov) return false;
	    if (ent instanceof PlayerEntity && !naked && ((LivingEntity) ent).getTotalArmorValue() == 0) return false;
	    if (((LivingEntity) ent).getHealth() == 0) return false;
	    if (((LivingEntity) ent).getTotalArmorValue() == 0 && ent.isInvisible() && !invisibles) return false;
	    if (ent == mc.player) return false;
	    if (rock.getFriendsHandler().isFriend(ent.getName().getString()) && !friends) return false;
	    if (Server.isRW() && !bots && BotUtility.isRWBot((LivingEntity) ent)) return false;
	    if (ClientAPI.isRockstar(ent.getName().getString()) && !users && !rock.getTargetHandler().isTarget(ent)) return false;
	    if (ent instanceof PlayerEntity && (((LivingEntity) ent).getHealth() == 0 || !ent.getUniqueID().equals(PlayerEntity.getOfflineUUID(ent.getName().getString())) && ent.getEntityId() != -1337) && !bots) {
	        return false;
	    }
	    return true;
	}

	public boolean isValidTarget(Entity ent) {
		return ent instanceof CreeperEntity;
	}

	private double estimateTargetDPS(LivingEntity entity) {
	    if (!(entity instanceof PlayerEntity)) return 0;

	    PlayerEntity player = (PlayerEntity) entity;
	    ItemStack weapon = player.getHeldItemMainhand();
	    double baseDamage = 1.0;

	    if (weapon.getItem() instanceof SwordItem) {
	        baseDamage = ((SwordItem) weapon.getItem()).getAttackDamage();

	        int sharpnessLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.SHARPNESS, weapon);
	        if (sharpnessLevel > 0) {
	            baseDamage += 1.0 + (sharpnessLevel - 1) * 0.5;
	        }
	    }

	    return baseDamage;
	}
	
	private double estimateMyDPSOnTarget(LivingEntity entity) {
	    double armor = entity.getTotalArmorValue();
	    double armorToughness = 0.0;

	    ModifiableAttributeInstance toughnessAttr = entity.getAttribute(Attributes.ARMOR_TOUGHNESS);
	    if (toughnessAttr != null) {
	        armorToughness = toughnessAttr.getValue();
	    }

	    ItemStack myWeapon = mc.player.getHeldItemMainhand();
	    double baseDamage = 1.0;

	    if (myWeapon.getItem() instanceof SwordItem) {
	        baseDamage = ((SwordItem) myWeapon.getItem()).getAttackDamage();

	        int sharpnessLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.SHARPNESS, myWeapon);
	        if (sharpnessLevel > 0) {
	            baseDamage += 1.0 + (sharpnessLevel - 1) * 0.5;
	        }
	    }

	    double reduction = Math.min(20.0, armor * 0.04);
	    double reducedDamage = baseDamage * (1.0 - reduction / 25.0);

	    return reducedDamage;
	}

	public Vector3d getPoint(LivingEntity target) {
		return getPoint(target, false);
	}
	
	public Vector3d getPoint(LivingEntity target, boolean silent) {
		if (target == null) return Vector3d.ZERO;
		double hitboxSize = target.getBoundingBox().maxY - target.getBoundingBox().minY;
		double additional = hitboxSize/2;
		Vector3d pos = getBestPoint(mc.player.getEyePosition(mc.timer.renderPartialTicks), target, silent);
		
		if (Server.is("infinity"))
			pos.y = target.getPositionVec().add(0, additional + ((int) mc.player.getPosY() > (int) pos.y ? hitboxSize / 4 : 0), 0).y;
		
		if (rock.getModules().get(BackTrack.class).get() && !target.getBacktrack().isEmpty() && mc.player.getDistance(target) < 10) {
			for (BackTrack.Position track : target.getBacktrack()) {
				Vector3d trackPos = getBestPoint(mc.player.getEyePosition(mc.timer.renderPartialTicks), target).subtract(target.getPositionVec()).add(track.getPos());
				trackPos.y = target.getPositionVec().add(0, additional, 0).y;

				if (distanceTo(trackPos) < distanceTo(pos)) {
					pos = trackPos;
				}
			}
		}
		
		return pos;
	}
	
	public Vector3d getBestPoint(Vector3d pos, LivingEntity entity) {
		return getBestPoint(pos, entity, false);
	}
	
	public Vector3d getBestPoint(Vector3d pos, LivingEntity entity, boolean silent) {
		if (entity == null) return Vector3d.ZERO;
		
		Aura aura = rock.getModules().get(Aura.class);
		
		ElytraTarget elytraTarget = rock.getModules().get(ElytraTarget.class);
		
		if (elytraTarget.get() && elytraTarget.getResolver().get()) {
			Vector3d pos1 = elytraTarget.getPos(entity);
			return (pos1 == null ? entity.getPositionVec() : pos1).add(0, entity.getEyeHeight(), 0);
		}
		
		double safePoint = 0;
        Vector3d fastPoint = new Vector3d(
                MathHelper.clamp(pos.x, 
                		entity.getBoundingBox().minX + safePoint, 
                		entity.getBoundingBox().maxX - safePoint),
                
                MathHelper.clamp(pos.y, 
                		entity.getBoundingBox().minY + safePoint, 
                		entity.getBoundingBox().maxY - safePoint),
                
                MathHelper.clamp(pos.z, 
                		entity.getBoundingBox().minZ + safePoint, 
                		entity.getBoundingBox().maxZ - safePoint)
        );
        
		if (!aura.getWalls().get() && !silent && !MathUtility.canSeen(fastPoint)) {
			MultiPoints.update(entity);
			Vector3d bestPoint = MultiPoints.getBestPoint(entity);
			
			if (bestPoint != null)
				return bestPoint;
		}
        
        return fastPoint;
    }
	
	public double distanceTo(Vector3d point) {
		return mc.player.getPositionVec().add(0,mc.player.getEyeHeight(),0).distanceTo(point);
	}

	public void tryBreakShield() {
		int axe = -1;
		for (int i = 0; i < 9; ++i) {
			if (mc.player.inventory.getStackInSlot(i).getItem() instanceof AxeItem) {
				axe = i;
			}
		}
		
		if (axe >= 0) {
			LivingEntity target = rock.getModules().get(Aura.class).getTarget();
			if (target instanceof PlayerEntity && target.isHandActive() && target.getActiveItemStack().getItem() instanceof ShieldItem) {
				mc.player.connection.sendPacket(new CHeldItemChangePacket(axe));
				mc.playerController.attackEntity(mc.player, target);
				mc.player.connection.sendPacket(new CAnimateHandPacket(Hand.MAIN_HAND));
				mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
				rock.getAlertHandler().alert("Сломали щит " + target.getName().getString(), AlertType.SUCCESS);
				target.breakShield = true;
				Sounds sounds = rock.getModules().get(Sounds.class);
				
				if (sounds.get() && sounds.getShieldbreak().get()) new Sound("nastya/shield_breaked").play();
			}
		}
	}

	public float GENIUSCLAMPERR$$$(float delta, boolean yawValue) {
		float recDelta = Math.min(Math.abs(delta), (float) 90 / 180 / 360 * MathUtility.random(6, 7) * 150 * 120 / (yawValue ? 1 : 5));
		delta = delta > 0 ? recDelta : -recDelta;
		return delta;
	}

	public Vector2f fixDeltaNonVanillaMouse(float delta, float secondDelta) {
		float value = (float) (MathUtility.random(0.1,0.8) + Math.pow(MathUtility.random(-0.3, 0.3), 3));
		if (Math.abs(delta) > 0 && Math.abs(secondDelta) == 0) secondDelta += value;
		if (Math.abs(secondDelta) > 0 && Math.abs(delta) == 0) delta += value;

		return new Vector2f(delta, secondDelta);
	}

	public static float calculateCorrectYawOffset(float yaw, float rotateVector) {
		float direction = 50 * (1 - mc.player.getSwingProgress(1.f));
		return MathHelper.clamp(rotateVector, yaw - direction, yaw + direction);
	}

	public static float calculateCorrectYawOffset(float yaw) {
		float direction = 50 * (1 - mc.player.getSwingProgress(1.f));
		return MathHelper.clamp(yaw, yaw - direction, yaw + direction);
	}

	public float getAngle(Entity entity) {
		double diffX = entity.getPosX() - mc.player.getPosX();
		double diffZ = entity.getPosZ() - mc.player.getPosZ();
		return (float) Math.abs(wrapDegrees((Math.toDegrees(Math.atan2(diffZ, diffX)) - 90) - mc.player.rotationYawHead));
	}

	public float correctRotation(float rot) {
		rot = getSens(rot);
		rot -= rot % getGCDValue();

		return rot;
	}

	public float getSens(float rotation) {
		return getDeltaMouse(rotation) * getGCDValue();
	}

	public float getDeltaMouse(float delta) {
		return Math.round(delta / getGCDValue());
	}

	public float getGCDValue() {
		double realGcd = mc.gameSettings.mouseSensitivity;
		double d4 = realGcd * (double) 0.6F + (double) 0.2F;
		return (float) (d4 * d4 * d4 * 8.0D * 0.15);
	}
}
