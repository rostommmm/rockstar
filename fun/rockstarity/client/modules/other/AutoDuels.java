package fun.rockstarity.client.modules.other;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventWorldChange;
import fun.rockstarity.api.events.list.game.inputs.EventInput;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.player.EventFinishEat;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Input;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.secure.Debugger;
import fun.rockstarity.client.modules.combat.Aura;
import net.minecraft.client.gui.overlay.PlayerTabOverlayGui;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.network.play.server.SCloseWindowPacket;
import net.minecraft.network.play.server.SOpenWindowPacket;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;

/**
 * @author ConeTin
 * @since 6 дек. 2023 г.
 */

@Info(name = "AutoDuels", desc = "Автоматически кидает дуэли", type = Category.OTHER)
public class AutoDuels extends Module {

	private final List<String> sent = Lists.newArrayList();
	private final TimerUtility counter = new TimerUtility();

	private final Mode mode1 = new Mode(this, "Режим");
	private final Mode.Element reallyworld = new Mode.Element(mode1, "ReallyWorld");
	private final Mode.Element spooky = new Mode.Element(mode1, "Spooky Auto");
	
	private final Slider kit = new Slider(this, "Кит").min(1).max(12).inc(1).set(10).hide(() -> reallyworld.get());

	private final Select mode = new Select(this, "Киты").min(1).hide(() -> spooky.get());

	private final Select.Element shield = new Select.Element(mode, "Щиты");
	private final Select.Element shipi = new Select.Element(mode, "Шипы 3");
	private final Select.Element bow = new Select.Element(mode, "Лук");
	private final Select.Element totem = new Select.Element(mode, "Тотем");
	private final Select.Element noDebaff = new Select.Element(mode, "НоуДебаф");
	private final Select.Element balls = new Select.Element(mode, "Шары");
	private final Select.Element classik = new Select.Element(mode, "Классик");
	private final Select.Element cheats = new Select.Element(mode, "Читерский рай");
	private final Select.Element nezer = new Select.Element(mode, "Незер");

	private final CheckBox stavkaAll = new CheckBox(this, "Ставить все").hide(() -> spooky.get());

	private final Mode sort = new Mode(this, "Предпочитать...").hide(() -> spooky.get());
	private final Mode.Element soft = new Mode.Element(sort, "Софтеров");
	private final Mode.Element noSoft = new Mode.Element(sort, "Ансофтеров");
	private final Mode.Element random = new Mode.Element(sort, "Рандом");

	private final Input stavk = new Input(this, "Ставка").set("0").set(true).hide(() -> stavkaAll.get() || spooky.get());

	private String lastTarget = "";
	private int balance;
	
	private boolean eating;
	private int wins, loses;
	private final TimerUtility charka = new TimerUtility();
	private final TimerUtility healka = new TimerUtility();

	
	@Override
	public void onAllEvent(Event event) {
		if (reallyworld.get()) {
			if (event instanceof EventReceivePacket e && e.getPacket() instanceof SChatPacket packet && Server.isRW()) {
				String m = packet.getChatComponent().getString();

				// Обновляем баланс игрока
				if (m.contains("выиграли") || m.contains("Баланс")) {
					try {
						balance = Integer.parseInt(m.split("\\$")[1].replace("!", ""));
					} catch (Exception e1) {
						Debugger.print(e1);
					}
				}
			}
		}
	}

	@Override
	public void onEvent(Event event) {
		if (reallyworld.get()) {
			if (event instanceof EventUpdate) {

				List<String> list = PlayerTabOverlayGui.getPlayersNames();

				if (this.sort.is(this.random))
					Collections.shuffle(list);
				else if (this.sort.is(this.soft))
					Collections.reverse(list);
				
				for (String player : list) {
					if (counter.passed(900) && !this.sent.contains(player) && !player.equals(mc.player.getNameClear()) && !player.contains("§")) {
			            lastTarget = player;
			            mc.player.sendChatMessage(String.format("/duel %s %s", player, stavkaAll.get() ? balance + "" : stavk.get()));
			            this.sent.add(player);
			            counter.reset();
			        }
				}
			}
			if (event instanceof EventReceivePacket e) {
				if (e.getPacket() instanceof SChatPacket packet) {
					String m = packet.getChatComponent().getString();
					if ((m.contains("принял") && !m.contains("не принял")) || m.contains("команды")) {
						sent.clear(); // Очищаем массив с игроками
						this.set(false); // Выключаем модуль
					}

					if (m.contains("Баланс") || m.contains("отключил запросы")) {
						e.cancel();
					}
				}

				String stavka = stavkaAll.get() ? balance + "" : this.stavk.get();

				if (e.getPacket() instanceof SCloseWindowPacket packet) {
					e.cancel();
				}

				if (e.getPacket() instanceof SOpenWindowPacket packet) {
					if (packet.getTitle().getString().contains("Выбор набора")) {
						mc.player.connection.sendPacket(new CClickWindowPacket(
								packet.getWindowId(), this.mode.getElements().indexOf(this.mode.getRandomEnabledElement()), 0,
								ClickType.PICKUP, mc.player.openContainer
										.getSlot(this.mode.getElements().indexOf(this.mode.getRandomEnabledElement())).getStack(),
								mc.player.openContainer.getNextTransactionID(mc.player.inventory)));
						e.cancel();
					} else if (packet.getTitle().getString().contains("Настройка поединка")) {
						if (stavka.isEmpty() || stavka.equals("0"))
							mc.player.connection.sendPacket(new CClickWindowPacket(packet.getWindowId(), 0, 0,
									ClickType.PICKUP, mc.player.openContainer.getSlot(0).getStack(),
									mc.player.openContainer.getNextTransactionID(mc.player.inventory)));

						e.cancel();
					}
				}

				if (e.getPacket() instanceof SSetSlotPacket packet && !stavka.equals("0")) {
					ItemStack stack = packet.getStack();

					if (stack.getDisplayName().getString().contains("Информация")) {
						List<ITextComponent> itemTooltip = stack.getTooltip(null, ITooltipFlag.TooltipFlags.NORMAL);
						for (ITextComponent str : itemTooltip) {

							if (str.getString().contains("Ставка:")) {
								if (str.getString().contains("Ставка: 0")) {
									mc.player.sendChatMessage("/duel " + this.lastTarget + " " + stavka + " " + stavka);
								} else {
									mc.player.connection.sendPacket(new CClickWindowPacket(packet.getWindowId(), 0, 0,
											ClickType.PICKUP, mc.player.openContainer.getSlot(0).getStack(),
											mc.player.openContainer.getNextTransactionID(mc.player.inventory)));
								}
							}
						}
					}
				}
			}
		} else {
			if (event instanceof EventFinishEat e && e.getEntity() == mc.player) {
				mc.player.inventory.currentItem = 0;
				mc.playerController.onStoppedUsingItem(mc.player);
				mc.gameSettings.keyBindUseItem.setPressed(false);
				if (e.getItem() == Items.ENCHANTED_GOLDEN_APPLE)
					charka.reset();
				
				if (e.getItem() == Items.POTION)
					healka.reset();
				eating = false;
			}
			
			if (event instanceof EventWorldChange) {
				mc.player.setAbsorptionAmount(0);
				eating = false;
			}
			
			if (event instanceof EventUpdate) {
				if (mc.player.getHeldItemMainhand().getDisplayName().getString().contains("Вход в очередь")) {
		            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
				}
				
				if (mc.player.inventory.currentItem == 1) {
					mc.player.inventory.currentItem = 0;
				}
				
				if (!mc.player.getCooldownTracker().hasCooldown(Items.ENCHANTED_GOLDEN_APPLE) && Player.findItem(Items.ENCHANTED_GOLDEN_APPLE) != -1 && mc.player.getAbsorptionAmount() <= 0) {
					mc.player.inventory.currentItem = Player.findItem(Items.ENCHANTED_GOLDEN_APPLE);
					if (mc.currentScreen == null) {
						mc.gameSettings.keyBindUseItem.setPressed(true);
					} else {
						mc.playerController.processRightClick(mc.player, mc.world, Hand.MAIN_HAND);
					}
										eating = true;
					return;
				}
				
				if (Player.findItem(Items.POTION) != -1 && mc.player.getHealth() + mc.player.getAbsorptionAmount() < 7 && healka.passed(13000)) {
					mc.player.inventory.currentItem = 7;
					if (mc.currentScreen == null) {
						mc.gameSettings.keyBindUseItem.setPressed(true);
					} else {
						mc.playerController.processRightClick(mc.player, mc.world, Hand.MAIN_HAND);
					}
					eating = true;
					return;
				}
				
				if (!mc.player.getCooldownTracker().hasCooldown(Items.GOLDEN_APPLE) && Player.findItem(Items.GOLDEN_APPLE) != -1 && mc.player.getHealth() + mc.player.getAbsorptionAmount() < 7) {
					mc.player.inventory.currentItem = Player.findItem(Items.GOLDEN_APPLE);
					if (mc.currentScreen == null) {
						mc.gameSettings.keyBindUseItem.setPressed(true);
					} else {
						mc.playerController.processRightClick(mc.player, mc.world, Hand.MAIN_HAND);
					}
										eating = true;
					return;
				}
				
				if (!mc.player.isPotionActive(Effects.STRENGTH) && Player.findItem(Items.POTION) != -1) {
					mc.player.inventory.currentItem = 8;
					if (mc.currentScreen == null) {
						mc.gameSettings.keyBindUseItem.setPressed(true);
					} else {
						mc.playerController.processRightClick(mc.player, mc.world, Hand.MAIN_HAND);
					}
					eating = true;
					return;
				}
			}
			
			if (event instanceof EventInput e && rock.getModules().get(Aura.class).getTarget() != null) {
				e.setForward(1);
				e.setJump(true);
			}

			if (event instanceof EventReceivePacket e) {
				if (e.getPacket() instanceof SOpenWindowPacket packet) {
					mc.player.connection.sendPacket(new CClickWindowPacket(packet.getWindowId(),
							(int) (kit.get() - 1), 0, ClickType.PICKUP,
							mc.player.openContainer.getSlot((int) (kit.get() - 1)).getStack(),
							mc.player.openContainer.getNextTransactionID(mc.player.inventory)));
				}
			}
			
			if (event instanceof EventReceivePacket e && e.getPacket() instanceof SChatPacket packet) {
		        String message = packet.getChatComponent().getString().toLowerCase();

		        if (message.contains("победитель") && message.contains(mc.player.getNameClear().toLowerCase())) {
		        	wins++;
		        }
		        
		        if (message.contains("проигрывший") && message.contains(mc.player.getNameClear().toLowerCase())) {
		        	loses++;
		        }
		    }
			
			if (event instanceof EventRender2D e) {
				bold.get(16).draw(e.getMatrixStack(), "Всего игр: " + (wins + loses), sr.getScaledWidth() / 2F - bold.get(16).getWidth("Всего игр: " + (wins + loses)) / 2F, sr.getScaledHeight() / 2F + 10, FixColor.WHITE);
				bold.get(16).draw(e.getMatrixStack(), "Побед: " + wins, sr.getScaledWidth() / 2F - bold.get(16).getWidth("Побед: " + wins) / 2F, sr.getScaledHeight() / 2F + 20, FixColor.WHITE);
				bold.get(16).draw(e.getMatrixStack(), "Лузов: " + loses, sr.getScaledWidth() / 2F - bold.get(16).getWidth("Лузов: " + loses) / 2F, sr.getScaledHeight() / 2F + 30, FixColor.WHITE);
			}
		}
	}

	@Override
	public void onEnable() {
		wins = 0;
		loses = 0;
		
		if (spooky.get()) {
			rock.getModules().get(Baritone.class).set(true);
			mc.player.sendChatMessage("#follow players");
			mc.player.sendChatMessage("#allowBreak false");
		} else {
			mc.player.sendChatMessage("/bal"); // Пишем /bal для обновления баланса
			counter.reset();
		}
	}
	
	@Override
	public void onDisable() {
		if (spooky.get()) {
			mc.player.sendChatMessage("#stop");
			mc.player.sendChatMessage("#allowBreak true");
		}
	}

}
