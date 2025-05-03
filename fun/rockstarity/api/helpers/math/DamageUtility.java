package fun.rockstarity.api.helpers.math;

import java.util.Map.Entry;

import com.google.common.collect.Multimap;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.events.list.player.EventKeepSprint;
import fun.rockstarity.api.helpers.game.Chat;
import lombok.experimental.UtilityClass;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.boss.dragon.EnderDragonPartEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effects;
import net.minecraft.stats.Stats;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

/**
 * @author ConeTin
 * @since 21 июл. 2024 г.
 */

@UtilityClass
public class DamageUtility implements IAccess {

	public float predictDamage(Entity targetEntity) {
		if (!(targetEntity instanceof LivingEntity target)) {
			return 0;
		}
		
		double dmg = 0;

        for (EquipmentSlotType equipmentslottype : EquipmentSlotType.values())
        {
            Multimap<Attribute, AttributeModifier> multimap = mc.player.getHeldItemMainhand().getAttributeModifiers(equipmentslottype);

            if (!multimap.isEmpty())
            {
                for (Entry<Attribute, AttributeModifier> entry : multimap.entries())
                {
                    AttributeModifier attributemodifier = entry.getValue();
                    double d0 = attributemodifier.getAmount();

                    boolean flag = false;

                    if (mc.player != null)
                    {
                        if (attributemodifier.getID() == Item.ATTACK_DAMAGE_MODIFIER)
                        {
                            d0 = d0 + mc.player.getBaseAttributeValue(Attributes.ATTACK_DAMAGE);
                            d0 = d0 + (double)EnchantmentHelper.getModifierForCreature(mc.player.getHeldItemMainhand(), CreatureAttribute.UNDEFINED);
                            dmg = d0;
                            
                            flag = true;
                        }
                        else if (attributemodifier.getID() == Item.ATTACK_SPEED_MODIFIER)
                        {
                            d0 += mc.player.getBaseAttributeValue(Attributes.ATTACK_SPEED);
                            flag = true;
                        }
                    }
                }
            }
        }
        
		float f = (float) ((float)mc.player.getAttributeValue(Attributes.ATTACK_DAMAGE) * dmg);
		
		float f1;

        if (targetEntity instanceof LivingEntity)
        {
            f1 = EnchantmentHelper.getModifierForCreature(mc.player.getHeldItemMainhand(), ((LivingEntity)targetEntity).getCreatureAttribute());
        }
        else
        {
            f1 = EnchantmentHelper.getModifierForCreature(mc.player.getHeldItemMainhand(), CreatureAttribute.UNDEFINED);
        }
        
        float f2 = mc.player.getCooledAttackStrength(0.5F);
       // f = f * (0.2F + f2 * f2 * 0.8F);
        f1 = f1 * f2;
        
        if (f > 0.0F || f1 > 0.0F)
        {
            boolean flag = f2 > 0.9F;
            boolean flag1 = false;
            int i = 0;
            i = i + EnchantmentHelper.getKnockbackModifier(mc.player);

            if (mc.player.isSprinting() && flag)
            {
                ++i;
                flag1 = true;
            }

            boolean flag2 = flag && mc.player.fallDistance > 0.0F && !mc.player.isOnGround() && !mc.player.isOnLadder() && !mc.player.isInWater() && !mc.player.isPotionActive(Effects.BLINDNESS) && !mc.player.isPassenger() && targetEntity instanceof LivingEntity;
            flag2 = flag2 && !mc.player.isSprinting();

            if (flag2)
            {
                f *= 1.5F;
            }

            f = f + f1;
            
            boolean flag3 = false;
            double d0 = (double)(mc.player.distanceWalkedModified - mc.player.prevDistanceWalkedModified);

            if (flag && !flag2 && !flag1 && mc.player.isOnGround() && d0 < (double)mc.player.getAIMoveSpeed())
            {
                ItemStack itemstack = mc.player.getHeldItem(Hand.MAIN_HAND);

                if (itemstack.getItem() instanceof SwordItem)
                {
                    flag3 = true;
                }
            }

            float f4 = 0.0F;
            boolean flag4 = false;
            int j = EnchantmentHelper.getFireAspectModifier(mc.player);
            
            Vector3d vector3d = targetEntity.getMotion();
            float damage = simulateAttackFrom(target, DamageSource.causePlayerDamage(mc.player), f);
            
            return damage;
        }
        return 0;
	}
	
	private float simulateAttackFrom(LivingEntity targetEntity, DamageSource source, float amount) {
		if (targetEntity.isInvulnerableTo(source))
        {
            return 0;
        }
        else if (targetEntity.getShouldBeDead())
        {
            return 0;
        }
        else if (source.isFireDamage() && targetEntity.isPotionActive(Effects.FIRE_RESISTANCE))
        {
            return 0;
        }
        else
        {
            float f = amount;

            boolean flag = false;
            float f1 = 0.0F;

            if (amount > 0.0F && targetEntity.canBlockDamageSource(source))
            {
                f1 = amount;
                amount = 0.0F;

                flag = true;
            }

            boolean flag1 = true;
           
            return simulateDamageEntity(targetEntity, source, amount);
        }
	}
	
	private float simulateDamageEntity(LivingEntity target, DamageSource damageSrc, float damageAmount) {
		if (!target.isInvulnerableTo(damageSrc))
        {
            damageAmount = target.applyArmorCalculations(damageSrc, damageAmount);
            damageAmount = target.applyPotionDamageCalculations(damageSrc, damageAmount);
            float f2 = Math.max(damageAmount, 0.0F);
            //target.setAbsorptionAmount(target.getAbsorptionAmount() - (damageAmount - f2));
            float f = damageAmount - f2;

            if (f2 != 0.0F)
            {
                //float f1 = target.getHealth();
                //target.setHealth(f1 - f2);
                //target.getCombatTracker().trackDamage(damageSrc, f1, f2);
                //target.setAbsorptionAmount(target.getAbsorptionAmount() - f2);
                
                return f2 * 1.5f;
            }
        }
		return 0;
	}
	
}
