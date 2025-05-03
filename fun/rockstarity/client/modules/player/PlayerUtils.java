package fun.rockstarity.client.modules.player;

import java.util.Arrays;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventSafeWalk;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.player.EventAttack;
import fun.rockstarity.api.events.list.player.EventDeath;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.player.Move;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Input;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Select.Element;
import fun.rockstarity.api.modules.settings.list.Slider;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SDestroyEntitiesPacket;
import net.minecraft.network.play.server.SEntityTeleportPacket;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TextFormatting;

/**
 * @author Malecharik
 * @since 24 мая 2024 г. 18:08:18
 */

@FieldDefaults(level = AccessLevel.PRIVATE)
@Info(name = "PlayerUtils", desc = "Утилиты игрока", type = Category.PLAYER, module = {"FastLadder","Anti", "Auto", "FastBreak", "AutoFish", "AutoParkour", "AutoTool", "AntiAFK", "LeaveTracker", "AutoRespawn", "NoFriendDamage"})
public class PlayerUtils extends Module {
	final Select utils = new Select(this, "Настройки").desc("Выбор, что будет делать игрок");
	final Element autoFish = new Element(utils, "Авто рыбалка");
	final Element autoSwitcher = new Element(utils, "Авто выбор предмета");
	final CheckBox packet = new CheckBox(autoSwitcher, "Пакетно");
	final Element autoParkour = new Element(utils, "Авто-паркур");
	final Element fastLadder = new Element(utils, "Ускоряться на лестнице");
	final Element antiAfk = new Element(utils, "Анти-афк");
	final CheckBox antiAfkBox = new CheckBox(this, "Включать автоматически").hide(() -> !antiAfk.get());
	final Element leaveTracker = new Element(utils, "Отслеживать телепортацию");
	final Element noFrDamag = new Element(utils, "Отменять урон по друзьям").set(true);
	final Element renaissance = new Element(utils, "Авто возрождение").set(true);
	final CheckBox renaBox = new CheckBox(this, "Тп после смерти").hide(() -> !renaissance.get());
	final Input tpRespawn = new Input(this, "Куда телепортироваться?").set("/warp pvp").hide(() -> !renaissance.get());
	final Mode mode = new Mode(this, "Что делать?").hide(() -> !this.antiAfk.get());
	final Mode.Element jump = new Mode.Element(mode, "Прыгать");
	final Mode.Element chat = new Mode.Element(mode, "Писать в чат");
	final Mode.Element hand = new Mode.Element(mode, "Взмах");
	final CheckBox autoBlock = new CheckBox(this, "Брать блоки").hide(() -> !this.autoSwitcher.get());
	final Slider delay = new Slider(this, "Задержка").min(5).max(60).inc(5).set(50).hide(() -> !this.antiAfk.get());
	final Mode ladder = new Mode(fastLadder, "Режим");
	final Mode.Element grim = new Mode.Element(ladder, "Grim Latest");
	final Mode.Element intave = new Mode.Element(ladder, "Intave");
	final Element safeWalk = new Element(utils, "SafeWalk");
	final Mode safeWalkMode = new Mode(safeWalk, "Режим SafeWalk");
	final Mode.Element nonLegitMode = new Mode.Element(safeWalkMode, "Обычный");
	final Mode.Element legitMode = new Mode.Element(safeWalkMode, "Легит");
	int oldSlot = -1; // Запоминаем предыдущий слот
	boolean status; // Статус работы
	Vector3d latest = Vector3d.ZERO;
	private int currentBobberId = -1;
	private TimerUtility timerFish = new TimerUtility();
	private boolean fishCatch;
	
	private final TimerUtility timer = new TimerUtility();
	boolean antiafkactive;

	@Override
	public void onEvent(Event event) {
		if (safeWalk.get()) {
			if (event instanceof EventMotion) {
			    BlockPos pos = new BlockPos(mc.player.getPosX(), mc.player.getPosY() - 1.0D, mc.player.getPosZ());
			    
		        if (safeWalkMode.is(legitMode)) {
					mc.gameSettings.keyBindSneak.setPressed((mc.world.getBlockState(pos).getBlock() == Blocks.AIR && mc.player.isOnGround()));
		        }
			}
			
			if (event instanceof EventSafeWalk e) {
				BlockPos pos = new BlockPos(mc.player.getPosX(), mc.player.getPosY() - 1.0D, mc.player.getPosZ());
				e.setCancel((mc.world.getBlockState(pos).getBlock() == Blocks.AIR && mc.player.isOnGround()));
			}
		}
		
		if (event instanceof EventUpdate){
			if (antiAfkBox.get() && timer.passed(10000)){
				antiafkactive = true;
			}
			if (Move.isMoving()){
				antiafkactive = false;
				timer.reset();
			}
		}
		
		if (fastLadder.get()) {
			if (event instanceof EventMotion) {
				if (mc.player.isOnLadder() && mc.player != null) {
					mc.player.getMotion().y *= grim.get() ? 1.43f : 1.07f + MathUtility.randomNew(-0.2, 0.06);
				}
			}
		}
		
		if (event instanceof EventUpdate && this.antiAfk.get() && (!antiAfkBox.get() || antiafkactive) && mc.player.ticksExisted % this.delay.get() == 05) {
			if (this.mode.is(this.jump) && mc.player.isOnGround() && !mc.getGameSettings().keyBindJump.isPressed()) {
				mc.player.jump();
			} else if (this.mode.is(this.hand)) {
				mc.player.swingArm(Hand.MAIN_HAND);
			} else if (this.mode.is(this.chat)) {
				mc.player.sendChatMessage("/bal");
			}
		}
		
		if (this.leaveTracker.get() && event instanceof EventReceivePacket receive && receive.getPacket() instanceof SEntityTeleportPacket packet) {
			Vector3d position = new Vector3d((int) packet.getX(), (int) packet.getY(), (int) packet.getZ());
			if (mc.world.getEntityByID(packet.getEntityId()) != null && !latest.equals(position) && mc.player.getPositionVec().distanceTo(position) > 200 && mc.world.getEntityByID(packet.getEntityId()) instanceof PlayerEntity) {
				String playerName = mc.world.getEntityByID(packet.getEntityId()).getName().getString();
				int x = (int) packet.getX(), y = (int) packet.getY(), z = (int) packet.getZ();
				
				Chat.msg(TextFormatting.AQUA + "[Rockstar] " + TextFormatting.GRAY + "Игрок " + TextFormatting.WHITE + playerName + TextFormatting.GRAY + 
						" телепортировался на координаты x: " + TextFormatting.WHITE + x + TextFormatting.GRAY + 
						", y: " + TextFormatting.WHITE + y + TextFormatting.GRAY + 
						", z: " + TextFormatting.WHITE + z + TextFormatting.GRAY, "Поставить метку", () -> {
    						rock.getCommands().execute("way add tp " + x + " " + y + " " + z);
						});
			}
			
			latest = position;
			
		}

		if (this.autoParkour.get() && event instanceof EventUpdate) {
			if (mc.player.getPosY() > 0 && mc.world.getCollisionShapes(mc.player, mc.player.getBoundingBox().offset(0.0, -0.1f, 0.0)).toList().isEmpty() && mc.player.isOnGround()) {
				mc.player.jump();
			}
		}

		if (this.autoSwitcher.get() && event instanceof EventUpdate && mc.objectMouseOver != null) {
			if (mc.getGameSettings().keyBindAttack.isKeyDown() && !mc.player.isCreative()) {
				handleToolSwitching(); // Берем инструмент
			} else if (mc.getGameSettings().keyBindUseItem.isKeyDown() && this.autoBlock.get()) {
				handleBlockSwitching(); // Берем блок
			} else if (this.status && this.oldSlot != -1) {
				resetSlot(); // Возвращаем слот
			}
		}
		
		if (event instanceof EventAttack e) {
			event.setCancel(this.noFrDamag.get() && e.getTarget() instanceof PlayerEntity && rock.getFriendsHandler().isFriend(e.getTarget().getName().getString()));
		}

		if (this.renaissance.get() && event instanceof EventDeath) {
			mc.player.respawnPlayer(); // Возраждаем игрока
			if (renaBox.get() && !tpRespawn.get().isEmpty()) {
				mc.player.sendChatMessage(tpRespawn.get());
			}
			mc.displayGuiScreen(null); // Закрываем окно респавна
		}
		

		if (this.autoFish.get() && event instanceof EventReceivePacket e) {
			handleFishingEvent(e);
		}
		
	    if (fishCatch && event instanceof EventUpdate && timerFish.passed(500)) {
	        mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
	        mc.player.swingArm(Hand.MAIN_HAND);
	        fishCatch = false;
	    }
	}
	
	private boolean isPlayerHoldingFishingRod() {
	    return mc.player.getHeldItemMainhand().getItem() instanceof FishingRodItem || 
	           mc.player.getHeldItemOffhand().getItem() instanceof FishingRodItem;
	}
	
	private void handleFishingEvent(EventReceivePacket event) {
	    if (event.getPacket() instanceof SSpawnObjectPacket spawnPacket) {
	        if (spawnPacket.getType() == EntityType.FISHING_BOBBER && 
	            spawnPacket.getY() - mc.player.getPosY() < 5) {
	            currentBobberId = spawnPacket.getEntityID();
	            timerFish.reset();
	            fishCatch = false;
	        }
	    } 
	    else if (event.getPacket() instanceof SDestroyEntitiesPacket destroyPacket) {
	        if (Arrays.stream(destroyPacket.getEntityIDs()).anyMatch(id -> id == currentBobberId)) {
	            currentBobberId = -1;
	            
	            if (isPlayerHoldingFishingRod()) {
	                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
	                mc.player.swingArm(Hand.MAIN_HAND);
	            }
	        }
	    }
	    else if (event.getPacket() instanceof SPlaySoundEffectPacket soundPacket) {
	        if (soundPacket.getSound() == SoundEvents.ENTITY_FISHING_BOBBER_SPLASH && 
	            currentBobberId != -1 && 
	            isPlayerHoldingFishingRod() && 
	            timerFish.passed(1000)) {
	            
	            FishingBobberEntity bobber = (FishingBobberEntity) mc.world.getEntityByID(currentBobberId);
	            if (bobber != null && 
	                bobber.getDistanceSq(soundPacket.getX(), soundPacket.getY(), soundPacket.getZ()) < 2.0) {
	                fishCatch = true;
	                timerFish.reset();
	            }
	        }
	    }
	}
	
	private int getPlayerFish() {
	    for (Entity entity : mc.world.getAllEntities()) {
	        if (entity instanceof FishingBobberEntity ent) {
	            if (ent.getDistance(mc.player) < 10) {
	                return ent.getEntityId();
	            }
	        }
	    }
	    return -1;
	}
	
	private boolean isSound(EventReceivePacket event, int playerBobberId) {
	    if (event.getPacket() instanceof SPlaySoundEffectPacket soundPacket) {
	        FishingBobberEntity fish = (FishingBobberEntity) mc.world.getEntityByID(playerBobberId);
	        
	        if (fish != null) {
	            return fish.getDistanceSq(soundPacket.getX(), soundPacket.getY(), soundPacket.getZ()) < 1.0;
	        }
	    }
	    return false;
	}

	@Override
	public void onDisable() {
		mc.player.setSneaking(false);
		this.oldSlot = -1;
		this.status = false;
	}

	private void handleToolSwitching() {
		int index = Player.findBestToolSlotInHotBar(); // Ищем лучший инструмент в хотбаре
		if (index != -1) { // Если предмет найден
			if (!packet.get()) {
				if (this.oldSlot == -1) this.oldSlot = mc.player.inventory.currentItem; // Запоминаем слот
				mc.player.inventory.currentItem = index; // Заменяем на лучший инструмент
				this.status = true; // Устанавливаем статус исстины
			} else {
				status = true;
				if (oldSlot == -1) oldSlot = mc.player.inventory.currentItem;
				mc.player.connection.sendPacket(new CHeldItemChangePacket(index));
			}
		}
	}

	private void handleBlockSwitching() {
		int currentSlot = mc.player.inventory.currentItem; // Переменная слота
		if (mc.player.inventory.getStackInSlot(currentSlot).isEmpty()) { // Проверка на то что слот игрока пустой
			int index = Player.findBestBlockSlotInHotBar(); // Ищем лучший слот для блока
			if (index != -1) { // Если найден
				if (!packet.get()) {
					if (this.oldSlot == -1) this.oldSlot = mc.player.inventory.currentItem; // Запоминаем слот
					mc.player.inventory.currentItem = index; // Заменяем на лучший инструмент
					this.status = true; // Устанавливаем статус исстины
				} else {
					status = true;
					if (oldSlot == -1) oldSlot = mc.player.inventory.currentItem;
					mc.player.connection.sendPacket(new CHeldItemChangePacket(index));
				}
			}
		}
	}

	/*
	 * Метод для сброса значений
	 */
	private void resetSlot() {
		mc.player.inventory.currentItem = this.oldSlot;
		this.status = false;
		this.oldSlot = -1;
	}
	
	@Override
	public void onEnable() {
		
	}
}