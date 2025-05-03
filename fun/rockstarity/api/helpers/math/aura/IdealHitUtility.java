package fun.rockstarity.api.helpers.math.aura;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.events.list.game.inputs.EventInput;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.client.modules.combat.Aura;
import fun.rockstarity.client.modules.combat.Criticals;
import fun.rockstarity.client.modules.combat.Aura;
import fun.rockstarity.client.modules.combat.ElytraTarget;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Items;
import net.minecraft.item.ShovelItem;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

/**
 * @author ConeTin
 * @since 1 июн. 2024 г.
 */


@UtilityClass
public class IdealHitUtility implements IAccess {
	
	@Setter @Getter
	private boolean jumped;
	
	public float getAICooldown() {
		

		
		if (mc.player.getHeldItemMainhand().getItem() == Items.AIR) {
			return 1;
		}
		
		if (mc.player.isPotionActive(Effects.BLINDNESS)
		 || mc.player.isPotionActive(Effects.LEVITATION)
		 || mc.player.isPotionActive(Effects.SLOW_FALLING)
		 || mc.player.isInLava()
		 || mc.player.isInWater()
		 || mc.player.isOnLadder()
		 || mc.player.isPassenger()
		 || Player.isInWeb()
		 || mc.player.isElytraFlying()
		 || mc.player.abilities.isFlying
		 || !rock.getModules().get(Aura.class).getOnlyCrits().get())
		 //|| Server.isRW())
			return 0.944f;
		
		//if (isBlockBelow()) 
		//	return 0.93f;
		
		if (mc.player.getHeldItemMainhand().getItem() instanceof AxeItem || mc.player.getHeldItemMainhand().getItem() instanceof ShovelItem) return 0.99f;
		
		ElytraTarget elytraTarget = rock.getModules().get(ElytraTarget.class);
		
		if (elytraTarget.get())// || criticals) 
			return 1;
			
		return 0.944f;
	}
	
	public float getAIFallDistance() {
		
		//if (!jumped) // Если игрок спрыгивает с блока
		//	return 0.01f;
		
		//if (isBlockBelow())
		//	return 0.05f;
		
		if (Server.is("spooky")) {
			return rock.getModules().get(Aura.class).getAttacks() % Math.round(MathUtility.random(4, 10)) == 0 ? MathUtility.random(0.15, 0.23) : 0;
		}
		
		return 0;
	}
	
	public float getNewFallDistance(LivingEntity target) {
		if (rock.getModules().get(Aura.class).getCritsBypass().get()) //|| Server.is("spooky") && Player.collideWith(target))
			return MathUtility.random(0, 0.3f);
		
		Aura aura = rock.getModules().get(Aura.class);

		if (Server.is("spooky")) {
			int attacks = rock.getModules().get(Aura.class).getAttacks();
			
			if (Player.collideWith(target) && !(Player.getBlock(0, 2, 0) != Blocks.AIR && Player.getBlock(0, -1, 0) != Blocks.AIR && mc.getGameSettings().keyBindJump.isKeyDown())) {
				return aura.getFdCount() >= 10 ? 0.3f : 0.15f;
			}
			
			return aura.getFdCount() >= 10 ? 0.15f : 0;
		}
		
		if (rock.getModules().get(Criticals.class).get() && Server.isRW()) {
			return 1;
		}
		
		return 0;
	}
	
	public boolean canAIFall() {
		Aura aura = rock.getModules().get(Aura.class);

		if (Player.getBlock(0, 2, 0) != Blocks.AIR && Player.getBlock(0, -1, 0) != Blocks.AIR && Server.is("spooky") && aura.getFdCount() > 8) {
			return false;
		}
		
		return ((Player.getBlock(0, 3, 0) == Blocks.AIR && Player.getBlock(0, 2, 0) == Blocks.AIR && Player.getBlock(0, 1, 0) == Blocks.AIR)
				|| mc.player.fallDistance < (Player.getBlock(0, 2, 0) != Blocks.AIR ? 0.08f : 0.6f)
				|| mc.player.fallDistance > 1.2f);
	}
	
	private boolean isBlockBelow() {
		Vector3d pos = mc.player.getPositionVec().add(0, -1, 0);
		AxisAlignedBB hitbox = mc.player.getBoundingBox();
		
		float off = 0.15f;
		
		return !isAir(hitbox.minX-off, pos.y, hitbox.minZ-off)
			|| !isAir(hitbox.maxX+off, pos.y, hitbox.minZ-off)
			|| !isAir(hitbox.minX-off, pos.y, hitbox.maxZ+off)
			|| !isAir(hitbox.maxX+off, pos.y, hitbox.maxZ+off);
	}
	
	private boolean isAir(double x, double y, double z) {
		return mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.AIR;
	}
	
}
