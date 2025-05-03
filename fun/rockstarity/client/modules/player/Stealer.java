package fun.rockstarity.client.modules.player;

import java.util.ArrayList;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventTick;
import fun.rockstarity.api.events.list.game.inputs.EventInput;
import fun.rockstarity.api.events.list.player.EventJump;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventMove;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.events.list.render.ui.EventBossBar;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.math.aura.Rotation;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Slider;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.EnderChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;

/**
 * @author ConeTin
 * @since 16 мар. 2024 г.
 */


@Info(name = "Stealer", desc = "Ворует ресурсы из контейнеров", type = Category.PLAYER)
public class Stealer extends Module {

	private final TimerUtility timer = new TimerUtility();

	private final Slider delay = new Slider(this, "Задержка").min(0).max(5).inc(0.1f).set(0.4f);
	private final CheckBox close = new CheckBox(this, "Закрывать").desc("Закрывает сундук, после его очистки");
	//private final CheckBox leave = new CheckBox(this, "Выходить с сервера").desc("Выходит из сервера, после очистки сундука");
	private final CheckBox off = new CheckBox(this, "Выключать").desc("Выключать модуль после использования");

	private final CheckBox open = new CheckBox(this, "Открывать мистики").desc("Мод для работы только с мистическими сундуками");
	
	private final CheckBox leave = new CheckBox(this, "Умный лив с серва").desc("Выходит с сервера, заходит с чётким расчётом тайминга и выходит после лутания");
	
	private final Mode steal = new Mode(this, "Режим лутания");

	private final Mode.Element up = new Mode.Element(steal, "Начиная сверху");
	private final Mode.Element down = new Mode.Element(steal, "Начиная снизу");
	private final Mode.Element center = new Mode.Element(steal, "С середины");
	private final Mode.Element random = new Mode.Element(steal, "Рандомно");
	
	private final ArrayList<EnderChestTileEntity> blackList = new ArrayList<>();

	private float yaw, pitch;
	private EnderChestTileEntity target;
	private int sec = 0;
	private TimerUtility cooldownTimer = new TimerUtility(), leaveTimer = new TimerUtility();
	private boolean processing, joined;

	@Override
	public void onEvent(Event event) {
		if (leave.get()) {
			if (event instanceof EventTick && this.processing) {
				if (this.cooldownTimer.passed(1000)) {
					Chat.debug(this.sec);
					this.sec -= 1;
					if (this.sec == 2) {
						rock.getCommands().execute("rct");
						this.processing = false;
						this.joined = true;
					}
					this.cooldownTimer.reset();
				}
			}
			
			if (event instanceof EventBossBar e && e.getText().contains("сек") && e.getText().contains(":") && !this.joined) {
				for (TileEntity entity : mc.world.loadedTileEntityList) {
					if (entity instanceof EnderChestTileEntity && MathHelper.sqrt(mc.player.getDistanceSq(entity.getPos())) < 2) {
						int sec = Integer.parseInt(e.getText().split(":")[1].replace("сек", "").trim());
						if (sec != this.sec) {
							mc.player.sendChatMessage("/hub");
							this.cooldownTimer.reset();
							this.processing = true;
						}
						this.sec = sec;
						break;
					}
				}
			}
		}
		
		if (event instanceof EventUpdate) {
			if (mc.currentScreen instanceof ChestScreen) {
				ChestScreen chest = (ChestScreen) mc.currentScreen;
				ChestContainer container = (ChestContainer) mc.player.openContainer;
				String chestName = chest.getTitle().getString();

				if (chestName.contains("Аукционы") || chestName.contains("Поиск"))
					return;

				int chestSize = chest.getContainer().getLowerChestInventory().getSizeInventory();
				int i;

				// Режим начиная сверху
				if (steal.is(up))
				for (i = 0; i < chestSize; i++) {
					if (delay.get() != 0 && !timer.passed((long) (delay.get() * 1000 + MathUtility.random(-100, 100))))
						break;
					if (chest.getContainer().getLowerChestInventory().getStackInSlot(i).isEmpty()) continue;
					mc.playerController.windowClick(chest.getContainer().windowId, i, 0, ClickType.QUICK_MOVE, mc.player);
					timer.reset();
				}

				// Режим начиная снизу
				if (steal.is(down))
				for (i = chestSize - 1; i >= 0; i--) {
					if (delay.get() != 0 && !timer.passed((long) (delay.get() * 1000 + MathUtility.random(-100, 100))))
						break;
					if (chest.getContainer().getLowerChestInventory().getStackInSlot(i).isEmpty()) continue;
					mc.playerController.windowClick(chest.getContainer().windowId, i, 0, ClickType.QUICK_MOVE, mc.player);
					timer.reset();
				}

				// Режим с середины
				if (steal.is(center)) {
					for (i = 0; i <= chestSize / 2; i++) {
				        if (delay.get() != 0 && !timer.passed((long) (delay.get() * 1000 + MathUtility.random(-100, 100))))
				            break;

				        int leftIndex = chestSize / 2 - i;
				        int rightIndex = chestSize / 2 + i;

				        ItemStack leftStack = chest.getContainer().getLowerChestInventory().getStackInSlot(leftIndex);
				        ItemStack rightStack = chest.getContainer().getLowerChestInventory().getStackInSlot(rightIndex);

				        if (leftStack.isEmpty() && rightStack.isEmpty()) continue;

				        if (!leftStack.isEmpty()) {
				            mc.playerController.windowClick(chest.getContainer().windowId, leftIndex, 0, ClickType.QUICK_MOVE, mc.player);
				            timer.reset();
				            continue;
				        } else if (!rightStack.isEmpty()) {
				            mc.playerController.windowClick(chest.getContainer().windowId, rightIndex, 0, ClickType.QUICK_MOVE, mc.player);
				            timer.reset();
				            continue;
				        }
				    }
					
				    for (i = 0; i <= chestSize / 2; i++) {
				        if (delay.get() != 0 && !timer.passed((long) (delay.get() * 1000 + MathUtility.random(-100, 100))))
				            break;

				        int leftIndex = chestSize / 2 - i;
				        int rightIndex = chestSize / 2 + i;

				        ItemStack leftStack = chest.getContainer().getLowerChestInventory().getStackInSlot(leftIndex);
				        ItemStack rightStack = chest.getContainer().getLowerChestInventory().getStackInSlot(rightIndex);

				        if (leftStack.isEmpty() && rightStack.isEmpty()) continue;

				        // Если раковин наутилуса нет, перемещаем обычные предметы
				        if (leftStack.isEmpty()) {
				            mc.playerController.windowClick(chest.getContainer().windowId, rightIndex, 0, ClickType.QUICK_MOVE, mc.player);
				        } else {
				            mc.playerController.windowClick(chest.getContainer().windowId, leftIndex, 0, ClickType.QUICK_MOVE, mc.player);
				        }
				        timer.reset();
				    }
				}


				// Режим рандомно
				if (steal.is(random)) {
					i = (int) MathUtility.random(0, chestSize);
					if (delay.get() != 0 && !timer.passed((long) (delay.get() * 1000 + MathUtility.random(-100, 100))))
						return;
					if (chest.getContainer().getLowerChestInventory().getStackInSlot(i).isEmpty()) return;
					mc.playerController.windowClick(chest.getContainer().windowId, i, 0, ClickType.QUICK_MOVE, mc.player);
					timer.reset();
				}

				if (isEmpty(chest)) {
					if (target != null && open.get()) {
						this.blackList.add(target);
					}
					if (off.get()) this.set(false);
					if (close.get()) mc.player.closeScreen();
					if (leave.get() && !Server.hasCT() && leaveTimer.passed(300L)) mc.player.sendChatMessage("/hub");
				} else {
					leaveTimer.reset();
				}
			} else {
				for (TileEntity entity : mc.world.loadedTileEntityList) {
					if (entity instanceof EnderChestTileEntity && MathHelper.sqrt(mc.player.getDistanceSq(entity.getPos())) < 4 && !blackList.contains(entity)) {
						target = (EnderChestTileEntity) entity;
					}
				}
			}
		}
		
		if (!(mc.currentScreen instanceof ChestScreen) && target != null && open.get()) {
        	if (event instanceof EventMotion e) {
        		BlockPos pos = target.getPos();
        		
        		Vector2f rot = Rotation.get(pos.getVec());
        		yaw = rot.x;
        		pitch = rot.y;
    			
    			e.setYaw(yaw);
    			e.setPitch(pitch);
    			
    			mc.player.rotationYawHead = yaw;
    			mc.player.rotationPitchHead = pitch;
    			mc.player.renderYawOffset = yaw;
            }
        	
        	if (event instanceof EventMove e) {
    			e.setYaw(yaw);
    			e.setPitch(pitch);
    		}
    		
        	if (event instanceof EventInput e) {
    			e.setYaw(yaw);
    		}
    		
    		if (event instanceof EventJump e) {
    			e.setYaw(yaw);
    		}
    		
    		if (event instanceof EventUpdate) {
    			if (MathHelper.sqrt(mc.player.getDistanceSq(target.getPos())) > 4 || blackList.contains(target)) this.target = null;
    			
    			BlockPos pos = target.getPos();
            	mc.playerController.func_217292_a(mc.player, mc.world, Hand.MAIN_HAND, new BlockRayTraceResult(pos.getVec(), net.minecraft.util.Direction.DOWN, pos, false));
    			mc.player.swingArm(Hand.MAIN_HAND);
    		}
        }
	}
	
	private boolean isEmpty(ChestScreen chest) {
        for (int i = 0; i < chest.getContainer().getLowerChestInventory().getSizeInventory(); ++i) {
            ItemStack stack = chest.getContainer().getLowerChestInventory().getStackInSlot(i);
            if (stack.isEmpty()) continue;
            return false;
        }
        return true;
    }
	
	@Override
	public void onEnable() {
		this.joined = false;
		this.blackList.clear();
	}
	
	@Override
	public void onDisable() {

	}

}
