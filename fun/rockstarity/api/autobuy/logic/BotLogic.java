package fun.rockstarity.api.autobuy.logic;

import java.util.List;

import fun.rockstarity.api.autobuy.AutoBuy;
import fun.rockstarity.api.autobuy.bots.Bot;
import fun.rockstarity.api.autobuy.bots.player.BotController;
import fun.rockstarity.api.autobuy.bots.player.BotPlayer;
import fun.rockstarity.api.autobuy.bots.world.BotWorld;
import fun.rockstarity.api.autobuy.logic.interfaces.ILogicHandler;
import fun.rockstarity.api.autobuy.logic.tasks.CheckTask;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.game.ItemUtility;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.math.TimerUtility;
import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.Difficulty;

/**
 * @author ConeTin
 * @since 20 апр. 2024 г.
 */


public class BotLogic implements ILogicHandler {

	final TimerUtility openAuctionTimer = new TimerUtility();
	private CheckTask active;
	private final AutoBuy autoBuy;

	public BotLogic(AutoBuy autoBuy) {
		this.autoBuy = autoBuy;
	}

	@Override
	public void onEvent(Event event) {
		if (event instanceof EventUpdate) {
			try {
				int i1 = 0;
				for (Bot bot : rock.getBotsHandler().getBots()) {
					BotPlayer player = bot.getPlayer();
					BotController controller = bot.getController();
					BotWorld world = bot.getWorld();

					//Chat.msg(player.getNameClear() + " - " + Math.abs(player.getPosX()));
					/*
					if (player.getUpdateTimer().passed(5000) && world.getDifficulty() == Difficulty.EASY) {
						Chat.debug(player.getNameClear() + ": Захожу на 313 анархию..");
						player.sendChatMessage("/an313");
						player.getUpdateTimer().reset();
					}
					*/
					
					if (player.getAntiAfkTimer().passed(10000)) {
						Chat.bot(player.getNameClear(), TextFormatting.GRAY + "Смотрю баланс..");
						player.sendChatMessage("/bal");
						player.sendChatMessage("/an" + Server.FT_ANARCHY);
						player.getAntiAfkTimer().reset();
					}
					
					if (Math.abs(player.getPosX()) < 1)
						continue;
					
					handleOpenAuction(player);


					//if (player.isOnGround())
					//	player.getMotion().y = 0.42f;

					if (player.openContainer instanceof ChestContainer) {
						ChestScreen chest = (ChestScreen) player.currentScreen;
						ChestContainer container = (ChestContainer) chest.getContainer();
						String chestName = chest.getTitle().getString();

						if (player.openContainer == null)
							return;
						
						if (chestName.contains("Аукционы (")) {
							for (Slot s : player.openContainer.inventorySlots) {
								if (s.slotNumber == 0) {
								//	Chat.debug(s.getStack().getDisplayName().getString());
								}
								ItemStack stack = s.getStack();
								List<ITextComponent> itemTooltip = stack.getTooltip(null, ITooltipFlag.TooltipFlags.NORMAL);
								String displayName = stack.getDisplayName().getString().toLowerCase();
								
								String seller = ItemUtility.getSeller(stack);
								
                    			if (active != null && active.check(stack)) {
                    				active.getOnSuccess().run();
                    				autoBuy.getTaskManager().remove(active);
                    				active = null;
                    				player.closeScreen();
                    			}
                    			
                    			if (s.slotNumber == 49 && !player.openContainer.getSlot(s.slotNumber).getStack().isEmpty()) {
                    				if (active != null) {
            							Chat.bot(player.getNameClear(), TextFormatting.RED + "Предмет " + active.getName() + " за " + active.getPrice() + " по всей видимости фейк. Удаляю задачу");
            							if (active.getOnFake() != null)
            								active.getOnFake().run();
            							autoBuy.getTaskManager().getBlackList().add(active);
            							autoBuy.getTaskManager().remove(active);
                        				active = null;
                        				player.closeScreen();
                						openAuctionTimer.reset();
            						}
                    			}
							}
						}
					}
					
					if (openAuctionTimer.passed(100)) {
						if (active != null) {
							Chat.bot(player.getNameClear(), TextFormatting.GOLD + "Предмет " + active.getName() + " за " + active.getPrice() + " по всей видимости фейк. Удаляю задачу");
							if (active.getOnFake() != null)
								active.getOnFake().run();
							autoBuy.getTaskManager().getBlackList().add(active);
							autoBuy.getTaskManager().remove(active);
            				active = null;
            				player.closeScreen();
    						openAuctionTimer.reset();
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void handleOpenAuction(BotPlayer player) {
		if (!autoBuy.getTaskManager().haveTasks() ||/* mc.currentScreen instanceof ChestScreen ||*/ !this.openAuctionTimer.passed(1000) || active != null) return;
		
		CheckTask task = autoBuy.getTaskManager().getNearestTask();
		
		Chat.bot(player.getNameClear(), TextFormatting.YELLOW + "Беру задачу на " + task.getName() + " за " + task.getPrice());
		
		player.sendChatMessage("/ah " + task.getSeller());
		
		active = task;
		
		openAuctionTimer.reset();
	}
}
