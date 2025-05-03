package fun.rockstarity.client.modules.move;

import java.util.ArrayList;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.events.list.render.world.EventRenderWorld;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.math.aura.Rotation;
import fun.rockstarity.api.helpers.player.Bypass;
import fun.rockstarity.api.helpers.player.Inventory;
import fun.rockstarity.api.helpers.player.Move;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import fun.rockstarity.api.secure.Debugger;
import io.netty.util.internal.ConcurrentSet;
import lombok.Getter;
import net.minecraft.block.Blocks;
import net.minecraft.block.WallBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TextFormatting;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author ConeTin
 * @since 8 дек. 2023 г.
 */
//sterpyat
@NativeInclude
@Info(name = "Flight", desc = "Симулятор птички", type = Category.MOVE)
public class Flight extends Module {

	private boolean elytras;
	private ItemStack old;
	private boolean jump;
	private Set<CPlayerPacket> packetFlyPackets = new ConcurrentSet();
	@Getter
	private final Mode mode = new Mode(this, "Режим");
	private final Mode.Element elytraY = new Mode.Element(mode, "Spooky Y");
	private final Mode.Element glide = new Mode.Element(mode, "Парение");
	private final Mode.Element trident = new Mode.Element(mode, "Трезубец");
	private final Mode.Element packet = new Mode.Element(mode, "Packet");
	//@Getter private final Mode.Element funtimeY = new Mode.Element(mode, "FunTimeY");

	private final Slider speed = new Slider(this, "Скорость").min(0.5f).max(5).inc(0.5f).set(1)
			.hide(() -> !this.mode.is(this.glide));
	
	//private final Slider speedFtY = new Slider(this, "Скорость поднятия").min(1).max(6).inc(1).set(1).hide(() -> !mode.is(funtimeY));

	private final ArrayList<IPacket> packets = new ArrayList<>();
	private final TimerUtility timer = new TimerUtility();
	private final TimerUtility placeTimer = new TimerUtility();
	int ticks;
	private int oldSlot;
	private final TimerUtility timers = new TimerUtility();
	
	@Override
	public void onAllEvent(Event event) {
		if (elytraY.get() && event instanceof EventReceivePacket e) {
			if (e.getPacket() instanceof SChatPacket packet) {
				String m = packet.getChatComponent().getString(); 
				if (m.contains("Вы не можете надевать элитры в PvP")) {
					e.cancel();
				}
			}
		}
	}

	@Override
	public void onEvent(Event event) {
		if (mode.is(elytraY) && event instanceof EventUpdate && mc.player.isElytraFlying()) {
		//	mc.player.setMotion(mc.player.getMotion().x, mc.player.getMotion().y + 0.05499999761581421D, mc.player.getMotion().z);
		}

		if (elytraY.get()) {
			if (event instanceof EventUpdate) {
				if (!Server.hasCT()) {
					rock.getAlertHandler().alert("Нужно находиться в PVP!", AlertType.ERROR);
					set(false);
					return;
				}
				
                this.ticks = 0;
                while (this.ticks < 9) {
                    if (Spider.mc.player.inventory.getStackInSlot(this.ticks).getItem() == Items.ELYTRA && !Spider.mc.player.isOnGround() && !mc.player.isInWater() && mc.gameSettings.keyBindJump.isKeyDown() && Spider.mc.player.fallDistance == 0.0f) {
                        Spider.mc.playerController.windowClick(0, 6, this.ticks, ClickType.SWAP, Spider.mc.player);
                        Spider.mc.player.connection.sendPacket(new CEntityActionPacket(Spider.mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
                        Spider.mc.player.getMotion().y = 0.366;
                        Spider.mc.playerController.windowClick(0, 6, this.ticks, ClickType.SWAP, Spider.mc.player);
                        this.oldSlot = this.ticks;
                    }
                    ++this.ticks;
                }
			}
			
			Player.look(event, mc.player.rotationYaw, 0, true);
		}
		
		/*
		if (this.mode.is(this.funtimeY)) {
			if (event instanceof EventMotion e) {
				Vector3d pos = mc.player.getPositionVec().add(0, 1.5f, 0);
				AxisAlignedBB hitbox = mc.player.getBoundingBox();
				
				float off = -0.1f;
				//mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.AIR
				boolean isAir = mc.world.getBlockState(new BlockPos(hitbox.minX-off, pos.y, hitbox.minZ-off)).getBlock() != Blocks.AIR
					|| mc.world.getBlockState(new BlockPos(hitbox.maxX+off, pos.y, hitbox.minZ-off)).getBlock() != Blocks.AIR
					|| mc.world.getBlockState(new BlockPos(hitbox.minX-off, pos.y, hitbox.maxZ+off)).getBlock() != Blocks.AIR
					|| mc.world.getBlockState(new BlockPos(hitbox.maxX+off, pos.y, hitbox.maxZ+off)).getBlock() != Blocks.AIR;
				
				if (isAir && !mc.player.isInWater()) {
					mc.getGameSettings().keyBindSneak.setPressed(false);
					if (mc.getGameSettings().keyBindJump.isKeyDown()) {
						mc.player.getMotion().y = 0.1f * speedFtY.get();
					}
					e.setGround(true);
				}
			}
			
			boolean doBypass = true;
		    
		    for (int i = 0; i < 3; i++) {
		    	if (Player.getBlock(0, -i, 0) != Blocks.AIR) {
		    		doBypass = false;
		    	}
		    }
			
		    if (event instanceof EventMotion e) {
		    	int slot = -1;

	            for (int i = 0; i < 9; i++) {
	                if (mc.player.inventory.getStackInSlot(i).getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof WallBlock) {
	                    slot = i;
	                    break;
	                }
	            }

		        if (slot == -1) return;
		        
		        mc.player.inventory.currentItem = slot;
		        //mc.setRightClickDelayTimer(0);
		        //mc.rightClickMouse();
		        
		        BlockRayTraceResult block = (BlockRayTraceResult)mc.objectMouseOver;
		        
		        // 624 74 1010
		        // (624.4078889597781, 73.37278359071955, 1011.25) - north - Mutable{x=624, y=73, z=1011} - false
		        String str = block.getHitVec() + " - " + block.getFace() + " - " + block.getPos() + " - " + block.isInside();
		        System.out.println(str);
		        Debugger.overlay(str);
		        
		        Vector3d pos = mc.player.getPositionVec();
		        BlockRayTraceResult newBlock = new BlockRayTraceResult(pos.add(0.4998030706926, 0, 1.548661486817), Direction.UP, new BlockPos(pos.add(0,0,1)), false);
		        
		        if (doBypass) {
		        	newBlock = new BlockRayTraceResult(pos.add(0.4078889597781, .37278359071955 - 1, 1.25), Direction.NORTH, new BlockPos(pos.add(0,-1,1)), false);
		        }
		        
		        newBlock = block;
		        
		        ActionResultType actionresulttype1 = mc.playerController.func_217292_a(mc.player, mc.world, Hand.MAIN_HAND, newBlock);

                if (actionresulttype1.isSuccessOrConsume())
                {
                    if (actionresulttype1.isSuccess())
                    {
                        mc.player.swingArm(Hand.MAIN_HAND);
                    }

                    return;
                }
		    }
		    
		    Vector2f rot = Rotation.get(mc.player.getPosition().getVec().add(0.5, doBypass ? -3f : 0.1f, 1.5));
		    Player.look(event, rot.x, rot.y, true);

		    if (event instanceof EventReceivePacket e) {
		    	
		    	if (e.getPacket() instanceof SPlayerPositionLookPacket) {
		    	//	rock.getAlertHandler().alert("Обнаружен флаг. Выключаю флай", AlertType.ERROR);
		    	//	set(false);
		    	//	onDisable();
		    	}
		    }
		}
		*/
		
		/*
		 * Обрабатываем событие Motion
		 */

		if (event instanceof EventMotion) {
			if (this.mode.is(this.glide)) {
				
				
				//mc.player.getMotion().y = mc.player.isOnGround() ? 0.42f
				//		: mc.getGameSettings().keyBindJump.isKeyDown() ? this.speed.get() * 0.4f
				//				: mc.getGameSettings().keyBindSneak.isKeyDown() ? this.speed.get() * -0.4f : -0.03f;
			//	Move.setSpeed(this.speed.get() * 0.4f);
				
				mc.player.setPosition(mc.player.getPosX(), mc.player.getPosY()+ 0.03f, mc.player.getPosZ());
				Bypass.send(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), mc.player.rotationYaw, mc.player.rotationPitch, false);
				
			} else if (this.mode.is(this.packet)) {
				final double speed = (mc.player.movementInput.jump && !Move.isMoving()) ? 0.062
						: (mc.player.movementInput.sneaking ? -0.82 : mc.player.movementInput.jump ? 1 : 0.0);
				final double[] strafing = Move.getSpeed(0.173f);
				for (int i = 1; i < 2; ++i) {
					mc.player.setVelocity(strafing[0] * i * 1.5, speed * i, strafing[1] * i * 1.5);
					this.sendPackets(mc.player.getMotion().x, mc.player.getMotion().y, mc.player.getMotion().z);
				}
			}
		}

		/*
		 * Обрабатываем событие Update
		 */
		if (event instanceof EventUpdate) {
			if (this.mode.is(this.trident)) {
				if (Player.findItem(41, Items.TRIDENT) == -1) {
					Chat.msg(TextFormatting.RED + "Для использования этого флая нужен трезубец!");
					this.toggle();
					return;
				}
				if (mc.player.isWet()
						&& (EnchantmentHelper.getRiptideModifier(mc.player.getHeldItemMainhand()) > 0
								|| EnchantmentHelper.getRiptideModifier(mc.player.getHeldItemOffhand()) > 0)) {
					mc.getGameSettings().keyBindUseItem.setPressed(mc.player.ticksExisted % 20 < 15);
				}
			}
		}
	}

	@Override
	public void onDisable() {
		mc.timer.reset();

		for (IPacket p : packets) {
			mc.player.connection.sendPacketSilent(p);
		}
		packets.clear();
		mc.world.removeEntityFromWorld(-13_37);
		timer.reset();
	}

	@Override
	public void onEnable() {
		this.timer.reset();
	}

	private void equip() {
		if (elytras) {
			int item = Inventory.getChestplate();
			Player.moveItem(item < 46 ? item : 6, 6, true);
		}
	}

	private void unequip() {
		if (!elytras) {
			old = Inventory.getItem(37);
			int item = Player.findItem(45, Items.ELYTRA);
			Player.moveItem(item < 46 ? item : 6, 6, true);
		}
	}

	private void sendPackets(final double x, final double y, final double z) {
		Vector3d position = mc.player.getPositionVec().add(x, y, z);
		Vector3d outOfBoundsPosition = position.add(new Vector3d(0.0, 1377.0, 0.0));
		packetSender(new CPlayerPacket.PositionPacket(position.x, position.y, position.z, true));
		packetSender(new CPlayerPacket.PositionPacket(outOfBoundsPosition.x, outOfBoundsPosition.y,
				outOfBoundsPosition.z, true));
	}

	private void packetSender(final CPlayerPacket packet) {
		packetFlyPackets.add(packet);
		mc.player.connection.sendPacket(packet);
	}
}
