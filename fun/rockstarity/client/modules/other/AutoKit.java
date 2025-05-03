package fun.rockstarity.client.modules.other;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.game.ItemUtility;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Slider;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.server.SChatPacket;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author Malecharik
 * @since 13 Mar 2024 12:53:49
 */

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Info(name="AutoKit", desc="Автоматически покупает сет на FunTime/SpookyTime", type=Category.OTHER)
public class AutoKit extends Module {
	
	Mode armor = new Mode(this, "Броня");
	Mode.Element armorNo = new Mode.Element(armor, "Нет");
	Mode.Element armorKrush = new Mode.Element(armor, "Крушитель");
	Mode.Element armorNezer = new Mode.Element(armor, "Незеритовая");
	CheckBox armorProtect5 = new CheckBox(armorNezer, "Только 5 прочн.");

	Mode additionalWeapon = new Mode(this, "Доп. Оружие");
	Mode.Element weaponNo = new Mode.Element(additionalWeapon, "Нет");
	Mode.Element weaponTrident = new Mode.Element(additionalWeapon, "Трезубец");
	Mode.Element weaponCrossbow = new Mode.Element(additionalWeapon, "Арбалет");
	Mode arrow = new Mode(weaponCrossbow, "Стрелы");
	Mode.Element proklyataya = new Mode.Element(arrow, "Проклятая стрела");
	Mode.Element paranoya = new Mode.Element(arrow, "Стрела паранойи");
	Mode.Element ledyanaya = new Mode.Element(arrow, "Ледяная стреда");
	Mode.Element dyavol = new Mode.Element(arrow, "Дьявольская стрела");
	Slider arrowCount = new Slider(weaponCrossbow, "Количество").min(1).max(64).inc(1).set(32);

	Mode effectsClean = new Mode(this, "Снятие эффектов");
	Mode.Element cleanNo = new Mode.Element(effectsClean, "Нет");
	Mode.Element cleanBozhka = new Mode.Element(effectsClean, "Божка");
	Mode.Element cleanMilk = new Mode.Element(effectsClean, "Молоко");
	Slider milkCount = new Slider(cleanMilk, "Количество").min(1).max(6).inc(1).set(2);
	Slider bozhkaCount = new Slider(cleanBozhka, "Количество").min(1).max(16).inc(1).set(2);

	Mode leftHand = new Mode(this, "Левая рука");
	Mode.Element leftNo = new Mode.Element(leftHand, "Нет");
	Mode.Element leftTal = new Mode.Element(leftHand, "Талисман");
	Mode.Element leftSphere = new Mode.Element(leftHand, "Сфера");
	Mode selectTal = new Mode(leftTal, "Выбор");
	Mode selectSphere = new Mode(leftSphere, "Выбор");

	Mode potion = new Mode(this, "Зелья");
	Mode.Element potionNo = new Mode.Element(potion, "Нет");
	Mode.Element potionBoth = new Mode.Element(potion, "Смешанные");
	Mode.Element potionSplit = new Mode.Element(potion, "Раздельные");
	Select potions = new Select(potionSplit, "Выбор");
	Select.Element silka = new Select.Element(potions, "Силка").set(true);
	Select.Element skorka = new Select.Element(potions, "Скорка").set(true);
	
	Select others = new Select(this, "Другое");
	Select.Element sword = new Select.Element(others, "Меч круша").set(true);
	Select.Element pearl = new Select.Element(others, "Перки").set(true);
	Select.Element chorus = new Select.Element(others, "Хорусы").set(true);
	Select.Element charka = new Select.Element(others, "Чарки").set(true);
	Select.Element food = new Select.Element(others, "Золотая морковь").set(true);
	Select.Element gapple = new Select.Element(others, "Геплы").set(true);
	Select.Element trap = new Select.Element(others, "Трапки").set(true);
	Select.Element plast = new Select.Element(others, "Пласты").set(true);
	Select.Element pilb = new Select.Element(others, "Явки").set(true);
	Select.Element heal = new Select.Element(others, "Исцел").set(true);
	Select.Element totem = new Select.Element(others, "Тотемы").set(true);
	Select.Element shulker = new Select.Element(others, "Шалкеры").set(true);
	Select.Element exp = new Select.Element(others, "Опыт").set(true);
	Select.Element elytra = new Select.Element(others, "Элитры").set(true);
	Select.Element firework = new Select.Element(others, "Фейерверки").set(true);

	// Перки
	Slider pearlCount = new Slider(pearl, "Количество").min(1).max(32).inc(1).set(16);
	// Чарки
	Slider charkaCount = new Slider(charka, "Количество").min(1).max(64).inc(1).set(7);
	// Хорусы
	Slider chorusCount = new Slider(chorus, "Количество").min(1).max(64).inc(1).set(16);
	// Геплы
	Slider gappleCount = new Slider(gapple, "Количество").min(1).max(64).inc(1).set(16);
	// Трапки
	Slider trapCount = new Slider(trap, "Количество").min(1).max(64).inc(1).set(6);
	// Пласты
	Slider plastCount = new Slider(plast, "Количество").min(1).max(64).inc(1).set(6);
	// Явки
	Slider pilbCount = new Slider(pilb, "Количество").min(1).max(64).inc(1).set(3);
	// Исцел
	Slider healCount = new Slider(heal, "Количество").min(1).max(64).inc(1).set(16);
	// Тотем
	Slider totemCount = new Slider(totem, "Количество").min(1).max(3).inc(1).set(2);
	// Шалкер
	Slider shulkerCount = new Slider(shulker, "Количество").min(1).max(3).inc(1).set(2);
	// Опыт
	Slider expCount = new Slider(exp, "Стаки").min(1).max(6).inc(1).set(3);
	// Элитры 
	Mode elytraMode = new Mode(elytra, "Выбор");
	Mode.Element elytraDef = new Mode.Element(elytraMode, "Обычные");
	Mode.Element elytraKrush = new Mode.Element(elytraMode, "Крушителя");
	// Элитры 
	Slider fireworkCount = new Slider(firework, "Стаки").min(1).max(4).inc(1).set(2);
	
	@NonFinal int stage;
	@NonFinal String stageItem = "zxc";

	TimerUtility aucTimer = new TimerUtility();
	TimerUtility buyTimer = new TimerUtility();
	
	public AutoKit() {
		new Mode.Element(selectTal, "Талисман Грани");
		new Mode.Element(selectTal, "Талисман Дедала");
		new Mode.Element(selectTal, "Талисман Тритона");
		new Mode.Element(selectTal, "Талисман Гармонии");
		new Mode.Element(selectTal, "Талисман Феникса");
		new Mode.Element(selectTal, "Талисман Ехидны");
		new Mode.Element(selectTal, "Талисман Крушителя");
		new Mode.Element(selectTal, "Талисман Карателя");
		
		new Mode.Element(selectSphere, "Сфера Андромеды");
		new Mode.Element(selectSphere, "Сфера Пандора");
		new Mode.Element(selectSphere, "Сфера Титана");
		new Mode.Element(selectSphere, "Сфера Аполлона");
		new Mode.Element(selectSphere, "Сфера Астрея");
		new Mode.Element(selectSphere, "Сфера Осириса");
		new Mode.Element(selectSphere, "Сфера Химеры");
	}
	
	@Override
	@NativeInclude
	public void onEvent(Event event) {
		if (event instanceof EventUpdate) {
			
			if (!(mc.player.openContainer instanceof ChestContainer) && aucTimer.passed(1000)) {
				stageItem = "";
				switch (stage) {
				case 0: {
					if (!armorNo.get()) stageItem = armorKrush.get() ? "шлем крушителя" : "незеритовый шлем";
					break;
				}
				case 1: {
					if (!armorNo.get()) stageItem = armorKrush.get() ? "нагрудник крушителя" : "незеритовый нагрудник";
					break;
				}
				case 2: {
					if (!armorNo.get()) stageItem = armorKrush.get() ? "поножи крушителя" : "незеритовые поножи";
					break;
				}
				case 3: {
					if (!armorNo.get()) stageItem = armorKrush.get() ? "ботинки крушителя" : "незеритовые ботинки";
					break;
				}
				case 4: {
					if (sword.get()) stageItem = "меч крушителя";
					break;
				}
				case 5: {
					if (pearl.get()) stageItem = "перка";
					break;
				}
				case 6: {
					if (chorus.get()) stageItem = "хорус";
					break;
				}
				case 7: {
					if (charka.get()) stageItem = "чарка";
					break;
				}
				case 8: {
					if (gapple.get()) stageItem = "золотое яблоко";
					break;
				}
				case 9: {
					if (trap.get()) stageItem = "трапка";
					break;
				}
				case 10: {
					if (plast.get()) stageItem = "пласт";
					break;
				}
				case 11: {
					if (pilb.get()) stageItem = "явная пыль";
					break;
				}
				case 12: {
					if (heal.get()) stageItem = "исцел";
					break;
				}
				case 13: {
					if (totem.get()) stageItem = "тотем";
					break;
				}
				case 14: {
					if (!weaponNo.get()) stageItem = (weaponTrident.get() ? "трезубец" : "арбалет") + " крушителя";
					break;
				}
				case 15: {
					if (weaponCrossbow.get()) stageItem = arrow.getMode().getName();
					break;
				}
				case 16: {
					if (shulker.get()) stageItem = "шалкер";
					break;
				}
				case 17: {
					if (exp.get()) stageItem = "бутылочка опыта";
					break;
				}
				case 18: {
					if (!cleanNo.get()) stageItem = cleanMilk.get() ? "молоко" : "божья аура";
					break;
				}
				case 19: {
					if (!leftNo.get()) stageItem = leftTal.get() ? selectTal.getMode().getName() : selectSphere.getMode().getName();
					break;
				}
				case 20: {
					if (!potionNo.get()) stageItem = "силка";
					break;
				}
				case 21: {
					if (potionSplit.get()) stageItem = "скорка";
					break;
				}
				case 22: {
					if (elytra.get()) stageItem = elytraDef.get() ? "элитры" : "элитры крушителя";
					break;
				}
				case 23: {
					if (firework.get()) stageItem = "фейерверк";
					break;
				}
				case 24: {
					if (food.get()) stageItem = "Золотая морковь";
					break;
				}
				case 25: {
					break;
				}
				
				default:
				}
				
				if (stageItem.isBlank()) {
					stage++;
					return;
				}
				
				search(stageItem);
				aucTimer.reset();
				buyTimer.reset();
			}
			
			if (mc.player.openContainer instanceof ChestContainer) {
				ChestScreen chest = (ChestScreen) mc.currentScreen;
				String chestName = chest.getTitle().getString();

				if (chestName.contains("Поиск") && chestName.contains(stageItem.replace("Пандоры", "Пандора"))) {
					ItemStack cheapestStack = null;
					int minPrice = Integer.MAX_VALUE;
					Slot slot = null;

					for (Slot s : mc.player.openContainer.inventorySlots) {
					    ItemStack stack = s.getStack();
					    
					    if (stack.isEmpty()) continue;
					    
					    int price = ItemUtility.getPrice(stack) / stack.getCount();
					    
					    if (price > 0 && price < minPrice && canBuy(stack)) {
					        minPrice = price;
					        cheapestStack = stack;
					        slot = s;
					    }
					}

					if (cheapestStack != null) {
					    String displayName = cheapestStack.getDisplayName().getString();
						mc.player.connection.sendPacket(new CClickWindowPacket(mc.player.openContainer.windowId, slot.slotNumber, 0, ClickType.QUICK_MOVE, mc.player.openContainer.getSlot(slot.slotNumber).getStack(), mc.player.openContainer.getNextTransactionID(mc.player.inventory)));
						Chat.msg("Покупаю " + displayName + " за " + minPrice + "..");
						buyTimer.reset();
			        	mc.player.closeScreen();
					}
				}
			}
		}
		
		if (event instanceof EventReceivePacket e && e.getPacket() instanceof SChatPacket packet) {
	        String message = packet.getChatComponent().getString();
	        
	        if (message.contains("Вы успешно купили")) {
	        	if (stage == 6 && chorus.get() && Player.count(Items.CHORUS_FRUIT) < chorusCount.get()-1
	        			|| stage == 5 && pearl.get() && Player.count(Items.ENDER_PEARL) < pearlCount.get()-1
	        			|| stage == 7 && charka.get() && Player.count(Items.ENCHANTED_GOLDEN_APPLE) < charkaCount.get()-1
	        			|| stage == 8 && gapple.get() && Player.count(Items.GOLDEN_APPLE) < gappleCount.get()-1
	        			|| stage == 9 && trap.get() && Player.count(Items.NETHERITE_SCRAP) < trapCount.get()-1
	        			|| stage == 10 && plast.get() && Player.count(Items.DRIED_KELP) < plastCount.get()-1
	        			|| stage == 11 && pilb.get() && Player.count(Items.SUGAR) < pilbCount.get()-1
	        			|| stage == 12 && heal.get() && Player.count(Items.POTION) < healCount.get()-1
	        			|| stage == 13 && totem.get() && Player.count(Items.TOTEM_OF_UNDYING) < totemCount.get()-1
	        			|| stage == 15 && weaponCrossbow.get() && Player.count(Items.ARROW) < arrowCount.get()-1
	        			|| stage == 16 && shulker.get() && Player.shulkerCount() < shulkerCount.get()-1
	        			|| stage == 17 && exp.get() && Player.stackSize(Items.EXPERIENCE_BOTTLE) < expCount.get()-1
	        			|| stage == 18 && (cleanBozhka.get() && Player.stackSize(Items.PHANTOM_MEMBRANE) < bozhkaCount.get()-1 || cleanMilk.get() && Player.stackSize(Items.MILK_BUCKET) < milkCount.get()-1)
	        			|| stage == 23 && firework.get() && Player.stackSize(Items.FIREWORK_ROCKET) < fireworkCount.get()-1) {
	        		return;
	        	}
	        	
	        	stage++;
	        	aucTimer.setStartTime(System.currentTimeMillis() - 1000);
	        }
		}
	}
	
	private boolean canBuy(ItemStack stack) {
		if (!aucTimer.passed(2000) || !buyTimer.passed(1000)) return false;
		
		
		if (stack.getItem() instanceof ArmorItem) {
			if (EnchantmentHelper.getEnchantmentLevel(Enchantments.THORNS, stack) > 0) return false; // шип
			if (EnchantmentHelper.getEnchantmentLevel(Enchantments.MENDING, stack) <= 0) return false; // починка
			if (EnchantmentHelper.getEnchantmentLevel(Enchantments.PROTECTION, stack) < 5) return false; // протекшен
			
			// прочность
			if (EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, stack) < 5 && armorProtect5.get()) return false;
			if (EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, stack) < 4) return false;
		}
		
		
		if (stack.getItem() instanceof PotionItem) {
			if (stage == 20 && !(potionBoth.get() ? ItemUtility.contains(stack, "Сила III") && ItemUtility.contains(stack, "Скорость III") : ItemUtility.contains(stack, "Сила III"))) {
				return false;
			}
			
			if (stage == 21 && potionSplit.get() && !ItemUtility.contains(stack, "Скорость III")) {
				return false;
			}
		}
		
		if (stack.getItem() instanceof ElytraItem item) {
			int max = stack.getMaxDamage(), cur = max - stack.getDamage();
			double perc = (double) cur / (double) max;
			if (perc < 0.7f) return false;
		}
		
    	if (stage == 6 && ItemUtility.getPrice(stack) < 100000) {
    		return false;
		}
    	/*
    	if (stage == 6 && chorus.get() && Player.count(Items.CHORUS_FRUIT) + stack.getCount() > chorusCount.get()
    			|| stage == 5 && pearl.get() && Player.count(Items.ENDER_PEARL) + stack.getCount() > pearlCount.get()
    			|| stage == 7 && charka.get() && Player.count(Items.ENCHANTED_GOLDEN_APPLE) + stack.getCount() > charkaCount.get()
    			|| stage == 8 && gapple.get() && Player.count(Items.GOLDEN_APPLE) + stack.getCount() > gappleCount.get()
    			|| stage == 9 && trap.get() && Player.count(Items.NETHERITE_SCRAP) + stack.getCount() > trapCount.get()
    			|| stage == 10 && plast.get() && Player.count(Items.DRIED_KELP) + stack.getCount() > plastCount.get()
    			|| stage == 11 && pilb.get() && Player.count(Items.SUGAR) + stack.getCount() > pilbCount.get()
    			|| stage == 12 && heal.get() && Player.count(Items.POTION) + stack.getCount() > healCount.get()
    			|| stage == 13 && totem.get() && Player.count(Items.TOTEM_OF_UNDYING) + stack.getCount() > totemCount.get()
    			|| stage == 15 && weaponCrossbow.get() && Player.count(Items.ARROW) + stack.getCount() > arrowCount.get()
    			|| stage == 16 && shulker.get() && Player.shulkerCount() + stack.getCount() > shulkerCount.get()
    			|| stage == 17 && exp.get() && Player.stackSize(Items.EXPERIENCE_BOTTLE) + stack.getCount() > expCount.get()
    			|| stage == 18 && (cleanBozhka.get() && Player.stackSize(Items.PHANTOM_MEMBRANE) + stack.getCount() > bozhkaCount.get() || cleanMilk.get() && Player.stackSize(Items.MILK_BUCKET) > milkCount.get())
    			|| stage == 23 && firework.get() && Player.stackSize(Items.FIREWORK_ROCKET) + stack.getCount() > fireworkCount.get()) {
    		return false;
    	}
    	*/
		return true;
	}
	
	private void search(String arg) {
		mc.player.sendChatMessage("/ah search " + arg);
	}
	
	@Override
	public void onEnable() {
		stage = 0;
		stageItem = "";
	}
	
    @Override
	public void onDisable() {
	}

}