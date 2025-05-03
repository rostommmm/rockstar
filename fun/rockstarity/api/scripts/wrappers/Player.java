package fun.rockstarity.api.scripts.wrappers;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.inputs.EventInput;
import fun.rockstarity.api.events.list.player.EventMove;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.player.Move;
import fun.rockstarity.api.scripts.Script;
import fun.rockstarity.api.scripts.wrappers.base.EntityBase;
import fun.rockstarity.api.scripts.wrappers.base.LivingEntityBase;
import fun.rockstarity.api.scripts.wrappers.base.VectorBase;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Foods;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;

public class Player implements IAccess {
	
	public float gold_health() {
		return mc.player.getAbsorptionAmount();
	}

	public void set_jumpticks(int value) {
		mc.player.setJumpTicks(value);
	}

	public void set_clickticks(int value) {
		mc.setRightClickDelayTimer(value);
	}

	public float armor() {
		return mc.player.getFoodStats().getFoodLevel();
	}
	
	public int getAxe() {
		for (int i = 0; i < 9; ++i) {
			ItemStack itemStack = mc.player.inventory.getStackInSlot(i);
			if (itemStack.getItem() instanceof AxeItem) {
				return i;
			}
		}
		return 1;
	}

	public void break_shield(LivingEntityBase base) {
		// Кто не знает - 😀
		// Кто знает - ☠️
		int axe = getAxe();

		if (axe != -1) {
			LivingEntity target = base.getEntity();
			if (target instanceof PlayerEntity && target.isHandActive()
					&& target.getActiveItemStack().getItem() instanceof ShieldItem) {
				mc.player.connection.sendPacket(new CHeldItemChangePacket(axe));
				mc.playerController.attackEntity(mc.player, target);
				mc.player.connection.sendPacket(new CAnimateHandPacket(Hand.MAIN_HAND));
				mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
			}
		}
	}
	
	public boolean chat() {
		return mc.currentScreen instanceof ChatScreen;
	}
	
	public boolean inv() {
		return mc.currentScreen instanceof InventoryScreen;
	}
	
	public boolean watching(float range, float yaw, float pitch, LivingEntityBase base) {
		return MathUtility.rayTraceWithBlock(range, yaw, pitch, mc.player, base.getEntity(), false);
	}
	  
	public void jump() {
        mc.player.jump();
    }
	
	public void msg(String text) {
		mc.player.sendChatMessage(text);
	}
	
	public VectorBase motion() {
		return mc.player.getMotion();
	}
	
	public boolean eating() {
        return mc.player.isHandActive() && !mc.player.getActiveItemStack().isEmpty() && mc.player.getActiveItemStack().getItem().isFood();
	}
	
	public int slot() {
		return mc.player.inventory.currentItem;
	}

	public void set_pos(double x, double y, double z) {
		mc.player.setPosition(x, y, z);
	}
	
	public void set_noclip(boolean noClip) {
		mc.player.noClip = noClip;
	}
	
	public void fix_moves(Event e) {
		if (e instanceof EventInput event) {
			event.setYaw(mc.player.rotationYawHead);
		}
		
		if (e instanceof EventMove) {
			((EventMove) e).setYaw(mc.player.rotationYawHead);
		}
	}
	
	public VectorBase pos() {
		if (Script.isActive3d()) {
			return new Vector3d(
					mc.player.lastTickPosX + (mc.player.getPosX() - mc.player.lastTickPosX) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getX(),
					mc.player.lastTickPosY + (mc.player.getPosY() - mc.player.lastTickPosY) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getY(),
					mc.player.lastTickPosZ + (mc.player.getPosZ() - mc.player.lastTickPosZ) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getZ()
					);
		} else {
			return mc.player.getPositionVec();
		}
	}
	
	public void set_speed(double speed) {
		Move.setSpeed(speed);
	}
	
	public boolean ct() {
		return Server.hasCT();
	}
	
	public double bps() {
		return  Math.hypot(mc.player.getPosY() - mc.player.prevPosY, Math.hypot(mc.player.getPosX() - mc.player.prevPosX, mc.player.getPosZ() - mc.player.prevPosZ) * 20);
	}
	
	public boolean swim() {
		return mc.player.isSwimming();
	}
	
	public double speed() {
		return Move.getSpeed();
	}
	
	public void set_sprint(boolean s) {
		mc.player.setSprinting(s);
	}
	
	public boolean sprint() {
		return mc.player.isSprinting();
	}
	
	public void strafe(float strafe) {
		Move.setSpeed(strafe);
	}
	
	public int ticks() {
		return mc.player.ticksExisted;
	}
	
	public boolean ground() {
		return mc.player.isOnGround();
	}
	
	public void set_ground(boolean ground) {
		mc.player.setOnGround(ground);
	}
	
	public void set_useitem(boolean use) {
		mc.getGameSettings().keyBindUseItem.setPressed(use);
	}
	
	public void set_sneak(boolean sneak) {
		mc.getGameSettings().keyBindSneak.setPressed(sneak);
	}
	
	public boolean sneak() {
		return mc.getGameSettings().keyBindSneak.isPressed();
	}
	
	public boolean collide_horizontal() {
		return mc.player.collidedHorizontally;
	}
	
	public boolean collide_vertical() {
		return mc.player.collidedVertically;
	}
	
	public boolean in_water() {
		return mc.player.isInWater();
	}
	
	public boolean in_lava() {
		return mc.player.isInLava();
	}
	
	public int hurt_time() {
		return mc.player.hurtTime;
	}
	
	public boolean elytra_flying() {
		return mc.player.isElytraFlying();
	}
	
	public EntityBase target() {
		return mc.objectMouseOver.getType() == Type.ENTITY ? new EntityBase(((EntityRayTraceResult) mc.objectMouseOver).getEntity()) : null;
	}
	
	public boolean in_web() {
		BlockPos playerPos = new BlockPos(mc.player.getPosX(), mc.player.getBoundingBox().minY - 0.1, mc.player.getPosZ());
		Block blockUnderPlayer = mc.world.getBlockState(playerPos).getBlock();
		
		return blockUnderPlayer == Blocks.COBWEB;
	}
	
	public float fall_distance() {
		return mc.player.fallDistance;
	}
	
	public void set_fall_distance(float fallDistance) {
		mc.player.fallDistance = fallDistance;
	}
	
	public void attack(EntityBase id) {
		mc.playerController.attackEntity(mc.player, id.getEntity());
	}
	
	public float cooldown() {
		return mc.player.getCooledAttackStrength(0);
	}
	
	public void swing() {
		mc.player.swingArm(Hand.MAIN_HAND);
	}
	
	public double distance(EntityBase id) {
		return mc.player.getDistance(id.getEntity());
	}
	
	public void set_yaw(float yaw) {
		mc.player.rotationYaw = yaw;
	}
	
	public void set_body_yaw(float yaw) {
		mc.player.renderYawOffset = yaw;
	}
	
	public void set_head_yaw(float yaw) {
		mc.player.rotationYawHead = yaw;
	}
	
	public void set_pitch(float pitch) {
		mc.player.rotationPitch = pitch;
	}
	
	public void set_head_pitch(float pitch) {
		mc.player.rotationPitchHead = pitch;
	}
	
	public float yaw() {
		return mc.player.rotationYaw;
	}
	
	public float body_yaw() {
		return mc.player.renderYawOffset;
	}
	
	public float pitch() {
		return mc.player.rotationPitch;
	}
	
	public float head_yaw() {
		return mc.player.rotationYawHead;
	}
	
	public float head_pitch() {
		return mc.player.rotationPitchHead;
	}
	
	public float health() {
		return Server.isFT() ? mc.player.getRealHealth() : mc.player.getHealth();
	}
	
	public float max_health() {
		return mc.player.getMaxHealth();
	}

	public void send_packet(IPacket packet) {
		mc.player.connection.sendPacket(packet);
	}
	
	public boolean in_game() {
		return mc.player != null && mc.world != null;
	}
	
	public int id() {
		return mc.player.getEntityId();
	}
	
	public void set_swim(boolean state) {
		mc.player.setSwimming(state);
	}
	
	public void swap(int slot) {
		mc.player.inventory.currentItem = slot;
	}
	
	public void move_items(int from, int to) {
		fun.rockstarity.api.helpers.player.Player.moveItem(from, to, true);
	}
	
	public void click(int slot) {
		mc.playerController.windowClick(0, slot, 0, ClickType.PICKUP, mc.player);
	}
	
	public void drop_all_items() {
		fun.rockstarity.api.helpers.player.Player.dropAllItems();
	}
	
	public int find(Item item) {
		return fun.rockstarity.api.helpers.player.Player.findItem(45, item);
	}
	
	public int find_blocks() {
		return fun.rockstarity.api.helpers.player.Player.findBlock();
	}
	
	public void disconect() {
		mc.world.sendQuittingDisconnectingPacket();
	}
	
	public int food() {
		return mc.player.getFoodStats().getFoodLevel();
	}
	
	public void get_block(double x, double y, double z) {
		fun.rockstarity.api.helpers.player.Player.getBlock(x, y, z);
	}
	
	public void set_glow(boolean state) {
		for(PlayerEntity entity : mc.world.getPlayers()) {
			entity.setGlowing(state);
		}
	}
	
	public void set_cooldown(Item item, int time) {
		mc.player.getCooldownTracker().setCooldown(item, time);
	}
	
	public boolean third_person() {
		return mc.getGameSettings().getPointOfView() != PointOfView.FIRST_PERSON;
	}
	
	public String name() {
		return mc.player.getNameClear();
	}
	
	public void close_menu() {
		mc.player.closeScreen();
	}
}