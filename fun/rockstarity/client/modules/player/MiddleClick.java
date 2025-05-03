package fun.rockstarity.client.modules.player;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventTick;
import fun.rockstarity.api.events.list.game.inputs.EventKey;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.math.InventoryUtility;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.player.Bypass;
import fun.rockstarity.api.helpers.player.InvUtility;
import fun.rockstarity.api.helpers.player.Inventory;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Binding;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Select.Element;
import fun.rockstarity.client.modules.combat.Aura;
import lombok.Getter;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TextFormatting;

/**
 * @author Malecharik
 * @since 28 мар. 2024 г. 12:19:13
 */


@Getter
@Info(name="MiddleClick", desc="Позволяет кидать перл или добавлять в друзья", type=Category.PLAYER, module = {"ClickAction"})
public class MiddleClick extends Module {

	private final Select utils = new Select(this, "Выбор");

	private final Element clickFriend = new Element(utils, "Добавление друга");
	private final Element clickPearl = new Element(utils, "Перл").set(true);

	private final Mode clickMode = new Mode(clickPearl, "Режим");
	private final Mode.Element packet = new Mode.Element(clickMode, "Пакетный");
	private final Mode.Element legit = new Mode.Element(clickMode, "Легитный");

	private final Binding friendBind = new Binding(this, "Кнопка друзей").hide(() -> !clickFriend.get());
	private final Binding pearlBind = new Binding(this, "Кнопка перла").hide(() -> !clickPearl.get());

	TimerUtility timer = new TimerUtility();
	final InventoryUtility.Hand handUtil = new InventoryUtility.Hand();
	long delay;
	final TimerUtility waitMe = new TimerUtility();
	final TimerUtility timer1 = new TimerUtility();
	final TimerUtility timer2 = new TimerUtility();
	public ActionType actionType = ActionType.START;
	Runnable runnableAction;
	private int originalSlot = -1;

	public MiddleClick(){
		super(999);
	}

	@Override
	public void onEvent(Event event) {
		if (!(mc.currentScreen instanceof ChatScreen)) if (event instanceof EventKey e) this.handleKey(e);

		if (!Bypass.via()) if (clickPearl.isEnabled())
			if (!timer.passed(100)) Player.look(event, mc.player.rotationYaw, mc.player.rotationPitch, rock.isDebugging());

		if (event instanceof EventTick) if (runnableAction != null) runnableAction.run();
		if (event instanceof EventReceivePacket e) this.handUtil.onEvent(e);

		if (event instanceof EventUpdate) {
			this.handUtil.setOriginalSlot(mc.player.inventory.currentItem);
			this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 250L);
		}
	}

	private void handleKey(EventKey e) {
		int key = e.getKey();
		if (mc.currentScreen == null && !e.isReleased()) {
			if (this.clickFriend.get() && friendBind.getBindByKey(e).isPresent()) {
				if (mc.pointedEntity instanceof PlayerEntity) {
					String entity = mc.pointedEntity.getName().getString();

					if (rock.getFriendsHandler().isFriend(entity)) {
						rock.getFriendsHandler().remove(entity);
						Chat.msg(TextFormatting.RED + "Друг удален: " + TextFormatting.RED + entity);
					} else {
						rock.getFriendsHandler().add(entity);
						Chat.msg(TextFormatting.GREEN + "Друг добавлен: " + TextFormatting.GREEN + entity);
					}
				} else if (mc.pointedEntity instanceof SlimeEntity) Chat.msg("Удивительно! но мама смирнова издает такие же звуки! За посхалочку промокод на скидку (10%) - mamaSmirnova");
			}

			if (clickPearl.isEnabled() && pearlBind.getBindByKey(e).isPresent()) {
				if (clickMode.is(packet)) {
					InvUtility.use(Items.ENDER_PEARL, true);
					timer.reset();
				} else {
					if (runnableAction == null) {
						actionType = ActionType.START;
						runnableAction = () -> sender();
						timer1.reset();
						timer2.reset();
					}
				}
			}
		}
	}

	private void sender() {
		int slot = InventoryUtility.getInstance().getSlotInInventoryOrHotbar(Items.ENDER_PEARL, true);
		Hand hand = mc.player.getHeldItemOffhand().getItem() instanceof EnderPearlItem ? Hand.OFF_HAND : Hand.MAIN_HAND;
		if (slot != -1) interact(slot, hand); else runnableAction = null;
	}

	private void swingAndSendPacket(Hand hand) {
		mc.gameSettings.keyBindUseItem.setPressed(true);
		mc.player.swingArm(hand);
	}

	private void interact(Integer slot, Hand hand) {
        if (actionType == ActionType.START) {
			originalSlot = mc.player.inventory.currentItem;
            switchSlot(slot, hand);
            actionType = ActionType.WAIT;
        } else if (actionType == ActionType.WAIT && timer1.passed(100L)) {
            actionType = ActionType.USE_ITEM;
        } else if (actionType == ActionType.USE_ITEM) {
            sendRotatePacket();
            swingAndSendPacket(hand);
            actionType = ActionType.SWAP_BACK;
        } else if (actionType == ActionType.SWAP_BACK && timer2.passed(150L)) {
			mc.gameSettings.keyBindUseItem.setPressed(false);
        	switchSlot(originalSlot, hand);
            runnableAction = null;
            originalSlot = -1;
        }
    }

	private void switchSlot(int slot, Hand hand) {
		if (slot != mc.player.inventory.currentItem && hand != Hand.OFF_HAND) {
			mc.player.inventory.currentItem = slot;
		}
	}

	private void sendRotatePacket() {
		if (rock.getModules().get(Aura.class).get() && rock.getModules().get(Aura.class).getTarget() != null) {
			mc.player.connection.sendPacket(new CPlayerPacket.RotationPacket(mc.player.rotationYaw, mc.player.rotationPitch, mc.player.isOnGround()));
		}
	}

	public enum ActionType {
		START, WAIT, USE_ITEM, SWAP_BACK
	}

	@Override
	public void onDisable() {}
	@Override
	public void onEnable() {}
}
