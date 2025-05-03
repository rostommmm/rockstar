package fun.rockstarity.client.modules.other;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.game.packet.EventSendPacket;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.helpers.render.AucUtilsWindow;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Select.Element;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CChatMessagePacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.util.text.ITextComponent;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Info(name = "AuctionUtils", desc = "Утилки для аукциона", type = Category.OTHER, module = {"Shulker"})
public class AuctionUtils extends Module {
	
    Select utils = new Select(this, "Утилиты").desc("Выбор утилит");
    Select.Element optimize = new Select.Element(utils, "Убирать по цене");
    Select.Element buying = new Select.Element(utils, "Подсвечивать цена").set(true);
	Select.Element shulkerPreview = new Select.Element(utils, "Шалкер превью");
	Element autoReSell = new Element(utils, "Авто переставление");
	
	CheckBox displayWindow = new CheckBox(this, "Отображать окно").hide(() -> !buying.get()).desc("Отображает окно настроек в меню аукциона");
	
	Select buy = new Select(this, "Выбор цен").desc("Выберите цены, которые будете подсвечивать").hide(() -> !this.buying.get());
	Select.Element green = new Select.Element(buy, "Минимальную").set(true);
	Select.Element yew = new Select.Element(buy, "Среднюю").set(true);
	Select.Element red = new Select.Element(buy, "Дорогую");
	
	CheckBox items = new CheckBox(this, "Фильтровать предметы").hide(() -> !buying.get());
	
	Select armorFilt = new Select(this, "Фильтры брони").hide(() -> !items.get());
	Element noShip = new Element(armorFilt, "Без шипов");
	Element prot = new Element(armorFilt, "Защита");
	Element durability = new Element(armorFilt, "Прочность");
	Element repair = new Element(armorFilt, "Починка");
	
	Select swordFilt = new Select(this, "Фильтры меча").hide(() -> !items.get());
	Element sharpness = new Element(swordFilt, "Острота");
	
	Select potionFilt = new Select(this, "Фильтры зелий").hide(() -> !items.get());
	Element strength = new Element(potionFilt, "Сила");
	Element speed = new Element(potionFilt, "Скорость");
	
	CheckBox noBroken = new CheckBox(this, "Без повреждений").hide(() -> !buying.get());
	
	@Setter @NonFinal int searchPrice = -1;
	AucUtilsWindow window = new AucUtilsWindow(this);
	TimerUtility timerUtility = new TimerUtility();
	@NonFinal private boolean awaitingReSell = false;
	@NonFinal private int retrievalSlot = 0;
	private final TimerUtility reSellDelayTimer = new TimerUtility();
	private final List<SellEntry> itemsToReSell = new ArrayList<>();
	
	@Override
	public void onEvent(Event event) {
		if (displayWindow.get()) {
		    if (event instanceof EventRender2D e) {
		    	window.getOpening().setForward(mc.currentScreen instanceof ContainerScreen screen && (screen.getTitle().getString().contains("Аукцион") || screen.getTitle().getString().contains("Поиск")));
		    	
		    	if (!window.getOpening().finished(false) && !window.getOpening().isForward()) {
		    	//	window.render(e.getMatrixStack(), 0, 0, 0);
		    	}
		    }
		}
		
		if (optimize.get()) {
			if (event instanceof EventSendPacket e && e.getPacket() instanceof CChatMessagePacket packet) {
			    String message = packet.getMessage();
			    if (message.startsWith("/ah search")) {
			        String[] args = message.split(" ");

			        if (args.length >= 4) {
			            String item = args[2];
			            int price;
			            try {
			                price = Integer.parseInt(args[3]);
			            } catch (NumberFormatException ex) {
			                return;
			            }

			            this.searchPrice = price;

			        }
			    }
			}
			
			if (event instanceof EventReceivePacket packet && packet.getPacket() instanceof CChatMessagePacket p) {
				String message = p.getMessage();
				if (message.contains("Поиск по цене может выполнять ")) {
					event.cancel();
				}
			}
		}
		
		if (autoReSell.get()) {
			if (event instanceof EventReceivePacket e && e.getPacket() instanceof SChatPacket packet) {
				String message = packet.getChatComponent().getString();
				if (message.contains("Вы можете переставлять предметы раз в минуту!")) {
					if (mc.currentScreen instanceof ContainerScreen screen && screen.getTitle().getString().contains("Хранилище")) {
						itemsToReSell.clear();
						awaitingReSell = false;
						for (int i = 0; i <= 14; i++) {
							ItemStack stack = screen.getContainer().getSlot(i).getStack();
							if (stack != null && !stack.isEmpty()) {
							    List<ITextComponent> tooltip = stack.getTooltip(mc.player, ITooltipFlag.TooltipFlags.NORMAL);

							    for (ITextComponent line : tooltip) {
							        if (line.getString().contains("$")) {
							            String[] parts = line.getString().split("\\$");
							            if (parts.length >= 2) {
							                String number = parts[2].replaceAll("[^0-9]", "");
							                try {
							                    int price = Integer.parseInt(number);
							                    itemsToReSell.add(new SellEntry(stack.copy(), price, stack.getCount()));
							                } catch (NumberFormatException ignored) {}
							            }
							            break;
							        }
							    }
							}
						}
						retrievalSlot = 0;
					}
				}
		    }
			
			if (event instanceof EventReceivePacket e && e.getPacket() instanceof SChatPacket packet) {
				String message = packet.getChatComponent().getString();
				
				if (message.contains("Вы не можете продать воздух!")) {
				    for (SellEntry entry : itemsToReSell) {
				        if (entry.markedForRemoval) {
				            entry.markedForRemoval = false;

				            int slot = findMatchingItem(entry);
				            if (slot != -1) {
				                mc.player.inventory.currentItem = slot;
				            }

				            break;
				        }
				    }
				    awaitingReSell = true;
				    reSellDelayTimer.reset();
				    return;
				}
			}
			
			if (event instanceof EventUpdate) {
			    if (!itemsToReSell.isEmpty() && mc.currentScreen instanceof ContainerScreen screen &&
			        screen.getTitle().getString().contains("Хранилище")) {

			        if (retrievalSlot <= 14) {
			            mc.playerController.windowClick(
			                screen.getContainer().windowId,
			                0,
			                0,
			                ClickType.PICKUP,
			                mc.player
			            );
			            retrievalSlot++;
			        } else {
			            mc.player.closeScreen();
			            awaitingReSell = true;
			        }
			    }

			    if (awaitingReSell && mc.currentScreen == null && !itemsToReSell.isEmpty()) {
			        if (!reSellDelayTimer.passed(500)) return;

			        Iterator<SellEntry> iterator = itemsToReSell.iterator();
			        while (iterator.hasNext()) {
			            SellEntry entry = iterator.next();
			            if (entry.isMarkedForRemoval()) {
			                iterator.remove();
			                continue;
			            }

			            ItemStack target = entry.getStack();
			            int price = entry.getPrice();
			            int countToSell = entry.getCount();

			            int slot = findMatchingItem(entry);
			            
			            if (slot != -1) {
			                ItemStack invStack = mc.player.inventory.getStackInSlot(slot);
			                if (invStack != null && !invStack.isEmpty() && ItemStack.areItemsEqual(target, invStack)) {
			                    mc.player.inventory.currentItem = slot;
			                    if (invStack.getCount() >= countToSell) {
			                        if (invStack.getCount() > countToSell) {
			                            ItemStack split = invStack.split(countToSell);
			                            mc.player.inventory.setInventorySlotContents(slot, invStack);
			                            mc.player.inventory.addItemStackToInventory(split);
			                        }

			                        if (!mc.player.getHeldItemMainhand().isEmpty()) {
			                            mc.player.connection.sendPacket(new CChatMessagePacket("/ah sell " + price));
			                            entry.setMarkedForRemoval(true);
			                            reSellDelayTimer.reset();
			                            break;
			                        }
			                    }
			                }
			            }
			        }
			    } else {
			        boolean allDone = true;
			        for (SellEntry entry : itemsToReSell) {
			            if (!entry.isMarkedForRemoval()) {
			                allDone = false;
			                break;
			            }
			        }
			        if (allDone) {
			            itemsToReSell.clear();
			            awaitingReSell = false;
			        }
			    }
			}
		}
	}
	
	private int findMatchingItem(SellEntry entry) {
	    for (int i = 0; i < mc.player.inventory.getSizeInventory(); i++) {
	        ItemStack stack = mc.player.inventory.getStackInSlot(i);
	        if (stack != null && !stack.isEmpty()) {
	            if (stack.getItem() == entry.stack.getItem()
	                && stack.getCount() >= entry.count
	                && stripColor(stack.getDisplayName().getString()).equalsIgnoreCase(stripColor(entry.stack.getDisplayName().getString()))) {
	                return i;
	            }
	        }
	    }
	    return -1;
	}
	
	private String stripColor(String s) {
	    return s.replaceAll("§[0-9a-fklmnor]", "");
	}
	
	@Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
	    timerUtility.reset();
	}
	
	@Getter
	class SellEntry {
	    private ItemStack stack;
	    private int price;
	    private int count;
	    @Setter private boolean markedForRemoval;
	    
	    public SellEntry(ItemStack stack, int price, int count) {
	        this.stack = stack;
	        this.price = price;
	        this.count = count;
	    }
	}
	
}
