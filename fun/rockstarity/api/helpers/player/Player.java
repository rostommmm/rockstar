package fun.rockstarity.api.helpers.player;

import java.util.OptionalInt;
import java.util.stream.IntStream;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.inputs.EventInput;
import fun.rockstarity.api.events.list.player.EventJump;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventMove;
import fun.rockstarity.api.events.list.player.EventTrace;
import fun.rockstarity.api.helpers.math.aura.Rotation;
import fun.rockstarity.client.modules.combat.Aura;
import lombok.experimental.UtilityClass;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * @author ConeTin
 * @since 11 янв. 2024 г.
 */

@UtilityClass
public class Player implements IAccess {

	public void overlay(Object msg) {
		mc.ingameGUI.setOverlayMessage(new TranslationTextComponent(msg.toString()), false);
	}

	public void overlay(ITextComponent comp) {
		mc.ingameGUI.setOverlayMessage(comp, false);
	}
	
	public boolean isBlockUnderWithMotion() {
        AxisAlignedBB aab = mc.player.getBoundingBox().offset(mc.player.getMotion().x, -1e-1, mc.player.getMotion().z);
        return mc.world.getCollisionShapes(mc.player, aab).toList().isEmpty();
    }
	
	public boolean isFalling() {
		return mc.player.fallDistance > 1.3f || (mc.player.fallDistance > 0 && Player.getBlock(0,-1,0) == Blocks.AIR);
	}
	
    public boolean collisionPredict(Vector3d to) {
    	try {
            boolean prevCollision = mc.world.getCollisionShapes(mc.player, mc.player.getBoundingBox().shrink(0.0625D)).toList().isEmpty();
            Vector3d backUp = new Vector3d(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ());
            mc.player.setPosition(to.x, to.y, to.z);
            boolean collision = mc.world.getCollisionShapes(mc.player, mc.player.getBoundingBox().shrink(0.0625D)).toList().isEmpty() && prevCollision;
            mc.player.setPosition(backUp.x, backUp.y, backUp.z);
            return collision;
    	} catch (Exception e) {
    		e.printStackTrace();
    		return false;
		}
    }
	
    public boolean collideWith(LivingEntity entity) {
		return collideWith(entity, 0);
	}
    
	public boolean collideWith(LivingEntity entity, float grow) {
		AxisAlignedBB box = mc.player.getBoundingBox();
		AxisAlignedBB targetbox = entity.getBoundingBox().grow(grow, 0, grow); //.expand(-0.1f, 0, -0.1f);
		
		if (box.maxX > targetbox.minX
		 && box.maxY > targetbox.minY
		 && box.maxZ > targetbox.minZ
		 && box.minX < targetbox.maxX
		 && box.minY < targetbox.maxY
		 && box.minZ < targetbox.maxZ) return true;

		return false;
	}
	
	public boolean isLookEvent(Event event) {
		return event instanceof EventTrace || event instanceof EventMotion || event instanceof EventMove || event instanceof EventInput || event instanceof EventJump;
	}
	
	public void look(Event event, float yaw, float pitch, boolean visual) {
		look(event, yaw, pitch, visual, 2, yaw);
	}
	
	public void look(Event event, float yaw, float pitch, boolean visual, int correction, float visualYaw) {
		if (event instanceof EventTrace eventTrace) {
			eventTrace.setYaw(yaw);
			eventTrace.setPitch(pitch);
			eventTrace.cancel();
		}
		
		if (event instanceof EventMotion eventMotion) {
			Aura aura = rock.getModules().get(Aura.class);
			//aura.rotation().x = yaw;
			//aura.rotation().y = pitch;
			eventMotion.setYaw(yaw);
			eventMotion.setPitch(pitch);
			if (visual) {
				mc.player.renderYawOffset = visualYaw;
				mc.player.rotationYawHead = visualYaw;
				mc.player.rotationPitchHead = pitch;
			}
		} 
		
		// Дальше идут фиксы движения чтобы ач ниче не заподозрил
		if (event instanceof EventMove eventMoveFix && correction > 0) {
			eventMoveFix.setYaw(yaw);
			eventMoveFix.setPitch(pitch);
		}
		
		if (event instanceof EventInput e && correction == 1) {
			e.setYaw(yaw);
		}
		
		if (event instanceof EventInput e && correction == 3 && rock.getModules().get(Aura.class).getTarget() != null) {
			e.setYaw(yaw, Rotation.get(rock.getModules().get(Aura.class).getTarget().getPositionVec()).x);
		}
		
		if (event instanceof EventJump eventJump) {
			eventJump.setYaw(yaw);
		}
	}
	
    private boolean isBlock(ItemStack stack) {
        return stack != null && stack.getItem() instanceof BlockItem;
    }
	
    public int findBestBlockSlotInHotBar() {
        for (int i = 0; i < 9; i++) {
            if (isBlock(mc.player.inventory.getStackInSlot(i))) {
                return i;
            }
        }
        return -1;
    }
	
	public int findBestToolSlotInHotBar() {
		if (mc.objectMouseOver instanceof BlockRayTraceResult blockRayTraceResult) {
			Block block = mc.world.getBlockState(blockRayTraceResult.getPos()).getBlock();

			int bestSlot = -1;
			float bestSpeed = 1.0f;

			for (int slot = 0; slot < 9; slot++) {
				float speed = mc.player.inventory.getStackInSlot(slot).getDestroySpeed(block.getDefaultState());

				if (speed > bestSpeed) {
					bestSpeed = speed;
					bestSlot = slot;
				}
			}
			return bestSlot;
		}
		return -1;
	}
	
	public boolean isBlockSolid(BlockPos pos) {
        return block(pos).getDefaultState().getMaterial().isSolid();
    }

    public boolean isBlockSolid(final double x, final double y, final double z) {
        return block(new BlockPos(x, y, z)).getDefaultState().getMaterial().isSolid();
    }
    
    public Block block(final BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock();
    }

    public Block block(final double x, final double y, final double z) {
        return mc.world.getBlockState(new BlockPos(x, y, z)).getBlock();
    }
	

	public void moveItemOld(int one, int two, boolean swap) {
		mc.playerController.windowClick(0, one, 0, ClickType.PICKUP, mc.player);
		mc.playerController.windowClick(0, two, 0, ClickType.PICKUP, mc.player);
		if (swap) {
			mc.playerController.windowClick(0, one, 0, ClickType.PICKUP, mc.player);
		}
	}
	
	public int findItemInInv(final int endSlot, final Item ofType) {
	    return IntStream.range(0, endSlot).filter(i -> mc.player.inventory.getStackInSlot(i).getItem() == ofType).map(i -> i == 40 ? 45 : i < 9 ? 36 + i : i).findFirst().orElse(-1);
	}
	
	public void moveItem(int one, int two, boolean swap) {
		mc.playerController.windowClick(0, one, 0, ClickType.PICKUP, mc.player);
		mc.playerController.windowClick(0, two, 0, ClickType.PICKUP, mc.player);
		if (swap) {
			mc.playerController.windowClick(0, one, 0, ClickType.PICKUP, mc.player);
		}
	}
	
	public void packetMove(int one, int two, boolean swap) {
		packetClick(0, one, 0, ClickType.PICKUP, mc.player);
		packetClick(0, two, 0, ClickType.PICKUP, mc.player);
		if (swap) {
			packetClick(0, one, 0, ClickType.PICKUP, mc.player);
		}
	}
	
	public void packetClick(int windowId, int slotId, int mouseButton, ClickType type, PlayerEntity player)
    {
		short short1 = player.openContainer.getNextTransactionID(player.inventory);
        mc.player.connection.sendPacket(new CClickWindowPacket(windowId, slotId, mouseButton, type, find(slotId), short1));
    }
	
	public boolean isInGame() {
		return mc.world != null && mc.player != null;
	}

	public Block getBlock() {
		return getBlock(0, 0, 0);
	}

	public Block getBlock(double x, double y, double z) {
		return !Player.isInGame() ? Blocks.AIR : mc.world.getBlockState(mc.player.getPosition().add(x, y, z)).getBlock();
	}

	public float getAngle(Entity entity) {
		double diffX = entity.getPosX() - mc.player.getPosX();
		double diffZ = entity.getPosZ() - mc.player.getPosZ();
		return (float) Math.abs(MathHelper.wrapDegrees((Math.toDegrees(Math.atan2(diffZ, diffX)) - 90) - mc.player.rotationYaw));
	}
	
	public float getAngle(Entity entity, float rotation) {
		double diffX = entity.getPosX() - mc.player.getPosX();
		double diffZ = entity.getPosZ() - mc.player.getPosZ();
		return (float) Math.abs(MathHelper.wrapDegrees((Math.toDegrees(Math.atan2(diffZ, diffX)) - 90) - rotation));
	}

	public float getFOV(Entity entity) {
		double diffX = entity.getPosX() - mc.player.getPosX();
		double diffY = entity.getPosY() + entity.getEyeHeight() - (mc.player.getPosY() + mc.player.getEyeHeight());
		double diffZ = entity.getPosZ() - mc.player.getPosZ();
		double dist = MathHelper.sqrt(diffX * diffX + diffZ * diffZ);

		float yaw = (float) MathHelper
				.wrapDegrees((Math.toDegrees(Math.atan2(diffZ, diffX)) - 90) - mc.player.rotationYaw);
		float pitch = (float) MathHelper
				.wrapDegrees((-(Math.toDegrees(Math.atan2(diffY, dist)))) - mc.player.rotationPitch);

		return (float) Math.sqrt(Math.abs(yaw) * Math.abs(yaw) + Math.abs(pitch) * Math.abs(pitch));
	}

	public boolean isInWeb() {
		return mc.player.getMotionMultiplier().lengthSquared() > 1.0E-7D;
	}
	
	public boolean targetIsInWeb() {
		Aura aura = rock.getModules().get(Aura.class);
		return aura.getTarget().getMotionMultiplier().lengthSquared() > 1.0E-7D;
	}

	public int findItem(Item input) {
		return IntStream.range(0, 9).filter(i -> mc.player.inventory.getStackInSlot(i).getItem() == input).findFirst()
				.orElse(-1);
	}
	
	public int findItemDefault(final int endSlot, final Item ofType) {
		int slot = -1;
		
		for (int i = 0; i < endSlot; i++) {
    		if (mc.player.inventory.getStackInSlot(i).getItem() != ofType) continue;
    		slot = i;
		}
		
		return slot;
	}

	public int findItem(final int endSlot, final Item ofType) {
		int slot = -1;
		
		for (int i = 0; i < endSlot; i++) {
    		if (mc.player.inventory.getStackInSlot(i).getItem() != ofType) continue;
    		slot = i == 40 ? 45 : i < 9 ? 36 + i : i;
		}
		
		return slot;
	}
	
    public int findItem(int maxSlots, Item item, int startSlot) {
        for (int i = startSlot; i < maxSlots; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() == item) {
                return i == 40 ? 45 : i < 9 ? 36 + i : i;
            }
        }
        return -1;
    }
    
    public int shulkerCount() {
    	int count = 0;
        for (int i = 0; i < 45; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() instanceof BlockItem item && item.getBlock() instanceof ShulkerBoxBlock) {
                count += stack.getCount();
            }
        }
        return count;
    }
    
    public int count(Item item) {
    	int count = 0;
        for (int i = 0; i < 45; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }
    
    public int stackSize(Item item) {
    	int count = 0;
        for (int i = 0; i < 45; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() == item) {
                count += 1;
            }
        }
        return count;
    }
	
	public ItemStack find(int slot) {
		return mc.player.inventory.getStackInSlot(slot);
	}

	public void dropAllItems() {
		IntStream.range(0, mc.player.container.getInventory().size()).forEach(i -> {
			mc.playerController.windowClick(0, i, 0, ClickType.PICKUP, mc.player);
			mc.playerController.windowClick(0, -999, 0, ClickType.PICKUP, mc.player);
		});
	}

	public int findEatInHotbar() {
		return IntStream.range(0, 9)
				.filter(slot -> mc.player.inventory.getStackInSlot(slot).getUseAction() == UseAction.EAT).findFirst()
				.orElse(-1);
	}

	public int findEatInInventory() {
		OptionalInt slot = IntStream.range(9, 36) // Создаем поток индексов слотов от 9 до 35
				.filter(i -> { // Фильтруем слоты
					ItemStack stack = mc.player.inventory.getStackInSlot(i); // Получаем предмет в слоте
					return stack.getItem().getFood() != null; // Проверяем, является ли предмет едой
				}).findFirst(); // Находим первый подходящий слот, если таковой есть
		return slot.orElse(-1); // Возвращаем индекс слота или -1, если не найдено
	}

	public int findBlock() {
		return IntStream.range(0, 9) // Создаем поток индексов от 0 до 8
				.filter(i -> { // Фильтруем слоты
					ItemStack stack = mc.player.inventory.getStackInSlot(i); // Получаем предмет в слоте
					return stack.getItem() instanceof BlockItem; // Проверяем, является ли предмет блоком
				}).findFirst() // Находим первый подходящий слот, если таковой есть
				.orElse(-1); // Возвращаем индекс слота или -1, если не найдено
	}
	
	public int findBlockSolid() {
		return IntStream.range(0, 9) // Создаем поток индексов от 0 до 8
				.filter(i -> { // Фильтруем слоты
					ItemStack stack = mc.player.inventory.getStackInSlot(i); // Получаем предмет в слоте
					if (!(stack.getItem() instanceof BlockItem)) {
						return false;
					}
					
					Block block = ((BlockItem) stack.getItem()).getBlock();
					
					return block.getDefaultState().isSolid(); // Проверяем, является ли предмет блоком
				}).findFirst() // Находим первый подходящий слот, если таковой есть
				.orElse(-1); // Возвращаем индекс слота или -1, если не найдено
	}
	
	public boolean isElytraEquiped() {
		return mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() == Items.ELYTRA;
	}

	public Item getChest() {
		return mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem();
	}

    public int getDurability(ItemStack stack) {
        return (int) ((stack.getMaxDamage() - stack.getDamage()) / Math.max(0.1, stack.getMaxDamage()) * 100.0f);
    }
}
