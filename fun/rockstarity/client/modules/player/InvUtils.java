package fun.rockstarity.client.modules.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.inputs.EventKey;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.game.packet.EventSendPacket;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.player.Move;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Select.Element;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.client.modules.combat.Aura;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.server.SCloseWindowPacket;
import net.minecraft.network.play.server.SHeldItemChangePacket;
import net.minecraft.world.Difficulty;

/**
 * @author Malecharik
 * @since 8 Mar 2024 23:00:26
 */


@Info(name="InvUtils", desc="Утилиты для инвентаря", type=Category.PLAYER, module = {"GuiMove", "InventoryMove", "InvMove", "GuiWalk", "XCarry", "ItemScroller"})
public class InvUtils extends Module {
	@Getter @Setter
	private boolean clicked;
	@Getter
	private final List<IPacket> queue = new ArrayList<>();
	@Getter
	private final List<IPacket> mainQueue = new ArrayList<>();
	@Getter
	private final TimerUtility timer = new TimerUtility();
	
	private final Select utils = new Select(this, "Выбор");
	
	@Getter private final Element buttonDrop = new Element(utils, "Кнопка выброса").set(true);
	@Getter private final Element itemScroll = new Element(utils, "Скрол предметов");
	private final Element guiMove = new Element(utils, "GuiMove");
	private final Element itemSwapFix = new Element(utils, "Фикс свапа предметов");
	@Getter private final CheckBox noClickIfMove = new CheckBox(guiMove, "Не двигать").desc("Не дает двигать предметы, если вы бежите с открытым инвентарем");
	@Getter private final Element glow = new Element(utils, "Подсвечивать дебафф");
	private final Element xCarry = new Element(utils, "XCarry");
	@Getter private final Element slotProtection = new Element(utils, "Защита слотов");
	private final Select procet = new Select(slotProtection, "Выбор");
	private final Element one = new Element(procet, "1");
	private final Element two = new Element(procet, "2");
	private final Element three = new Element(procet, "3");
	private final Element four = new Element(procet, "4");
	private final Element five = new Element(procet, "5");
	private final Element six = new Element(procet, "6");
	private final Element seven = new Element(procet, "7");
	private final Element eight = new Element(procet, "8");
	private final Element nine = new Element(procet, "9");
	@Getter private final CheckBox ctrq = new CheckBox(slotProtection, "Работать с контролом");
	
	private final CheckBox sneak = new CheckBox(this, "Присяд").hide(() -> !this.guiMove.get());
	@Getter
	private final CheckBox ft = new CheckBox(this, "Обход FunTime").desc("Мод для работы GuiMove на FunTime").hide(() -> !this.guiMove.get());

	private final CheckBox slow = new CheckBox(this, "Медленный выброс").hide(() -> !buttonDrop.get());

	private final CheckBox noslow = new CheckBox(this, "Без замедления").hide(() -> !ft.get());
	private final Slider delayFt = new Slider(this, "Задержка свапа").min(100).max(400).set(200).inc(5).desc("Задержка у инвентори-мува").hide(() -> !this.noslow.get() && !ft.get());
	@Getter
	private final Slider delay = new Slider(this, "Задержка").min(1).max(400).set(1).inc(10).desc("Задержка между перемещением предметов").hide(() -> !this.itemScroll.get());
	
	
	@Getter @Setter
	private boolean taskActive;
	private final TimerUtility dropTimer = new TimerUtility();
	
	@Getter @Setter
	private boolean stop;
	
	@Getter private boolean protect;
	
	@Override
	public void onAllEvent(Event event) {
		if (mc.currentScreen == rock.getClickGui() && !stop)
			this.handleGuiWalk(event);
	}
	
	@Override
	public void onEvent(Event event) {
		if (this.guiMove.get())
			this.handleGuiWalk(event);
		
		if (this.buttonDrop.get()) 
			this.handleActiveTask(event);
		
		if (this.xCarry.get() && event instanceof EventSendPacket e && e.getPacket() instanceof CCloseWindowPacket)
			event.cancel();
		
		if (itemSwapFix.get()) {
			if (mc.player == null || mc.player.ticksExisted % 40 != 0) return;
			
			if (event instanceof EventReceivePacket e) {
	            if (e.getPacket() instanceof SHeldItemChangePacket wrapper) {
	                final int serverSlot = wrapper.getHeldItemHotbarIndex();
	                if (serverSlot != mc.player.inventory.currentItem) {
	                    mc.player.connection.sendPacket(new CHeldItemChangePacket(Math.max(mc.player.inventory.currentItem - 1, 0)));
	                    mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
	                    e.cancel();
	                }
	            }
			}
		}
		
		
		if (slotProtection.get() && event instanceof EventUpdate) {
	        int currentSlot = mc.player.inventory.currentItem;

            protect = 
                    (currentSlot == 0 && one.get()) ||
                    (currentSlot == 1 && two.get()) ||
                    (currentSlot == 2 && three.get()) ||
                    (currentSlot == 3 && four.get()) ||
                    (currentSlot == 4 && five.get()) ||
                    (currentSlot == 5 && six.get()) ||
                    (currentSlot == 6 && seven.get()) ||
                    (currentSlot == 7 && eight.get()) ||
                    (currentSlot == 8 && nine.get());
		}
		
	}
	
	public void handleActiveTask(Event event) {
		if (!this.taskActive) return;
		
		if (event instanceof EventUpdate) {
			for (int i = 0; i < mc.player.container.getInventory().size(); i++) {
				if (i == mc.player.container.getInventory().size() - 1) this.taskActive = false;
				if (Player.find(i).isEmpty()) continue;
				
				mc.playerController.windowClick(0, i == 40 ? 45 : i < 9 ? 36 + i : (i >= 36 ? 8-(i-36) : i), 0, ClickType.PICKUP, mc.player);
			    mc.playerController.windowClick(0, -999, 0, ClickType.PICKUP, mc.player);
			    
			    this.dropTimer.reset();
			       
			    if (this.slow.get() || i % 9 == 0)
			    	break;
			}
		}
	}
	
	private void handleGuiWalk(Event event) {
		if (event instanceof EventMotion && !(mc.currentScreen instanceof ChatScreen)) {
			List<KeyBinding> keys = new ArrayList<>(Arrays.asList(
	                mc.getGameSettings().keyBindForward,
	                mc.getGameSettings().keyBindBack,
	                mc.getGameSettings().keyBindLeft,
	                mc.getGameSettings().keyBindRight,
	                mc.getGameSettings().keyBindJump
	            )); // Создаём список клавиш передвижения (вперёд, назад, влево, вправо, прыжок)
	        
			if (this.sneak.get()) keys.add(mc.getGameSettings().keyBindSneak); // Если включен сеттинг sneak, то добавляем клавишу шифта в список keys
			if (this.ft.get() && mc.currentScreen instanceof ChestScreen && mc.world.getDifficulty() != Difficulty.EASY) {
				return;
			}
			
			if (mc.player.fallDistance <= 0 && this.noslow.get() && !mc.player.isOnGround() && mc.currentScreen instanceof InventoryScreen) this.timer.reset();
			boolean canMove =
					this.timer.passed(delayFt.get())
							&& (rock.getClickGui().getWindow().getInput()
							== null || !rock.getClickGui().getWindow().getInput().isFocused())
					|| (noClickIfMove.get() && Move.getSpeed() < Move.getSpeed() / 3 + 0.05); // Проверяем, прошло ли несколько миллисекунд с момента последнего передвижения
			
			// Устанавливаем состояние нажатия каждой клавиши для передвижения в зависимости от прошедшего времени и текущего состояния клавиатуры.
			keys.forEach(keyBinding -> keyBinding.setPressed(!canMove && (mc.player.fallDistance > 0 || !noslow.get() || (mc.player.isOnGround() && !mainQueue.isEmpty())) || rock.getClickGui().getWindow().getInput() != null && rock.getClickGui().getWindow().getInput().isFocused() ? false : InputMappings.isKeyDown(mc.getMainWindow().getHandle(), keyBinding.getDefault().getKeyCode())));
			if (Server.isFS() && !canMove && mc.player.isInWater()) {
				Move.setSpeed(0);
				mc.player.getMotion().y = 0;
			}
			
			if (canMove && (!Server.isFS() || rock.getModules().get(Aura.class).getAttackTimer().passed(200) && (mc.player.fallDistance > 0 || !noslow.get() || mc.player.isOnGround()))) { // Если можно перемещаться.
				// Отправляем пакеты из основной очереди.
				//if (!mainQueue.isEmpty())
				//if (mc.currentScreen instanceof InventoryScreen)
				//mc.player.connection.sendPacket(new CCloseWindowPacket(mc.player.openContainer.windowId));
				rock.getModules().get(InvUtils.class).getMainQueue().stream().forEach(packet -> mc.player.connection.sendPacket(packet));
				this.mainQueue.clear();
		    }
		}

		if (event instanceof EventReceivePacket e && this.ft.get()) {

		    if (e.getPacket() instanceof SCloseWindowPacket) {
		    	// Отправляем пакет для открытия инвентаря.

		        event.cancel();// Отменяем событие.
		    }
		}
	}
	
	@Override
	public void onDisable() {
		this.mainQueue.clear();
	}

	@Override
	public void onEnable() {
		
	}
	
}
