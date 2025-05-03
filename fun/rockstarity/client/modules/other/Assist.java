package fun.rockstarity.client.modules.other;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventChatScreen;
import fun.rockstarity.api.events.list.game.EventPotionHit;
import fun.rockstarity.api.events.list.game.EventSetCooldown;
import fun.rockstarity.api.events.list.game.EventTotemBreak;
import fun.rockstarity.api.events.list.game.EventWorldChange;
import fun.rockstarity.api.events.list.game.inputs.EventInput;
import fun.rockstarity.api.events.list.game.inputs.EventKey;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.game.packet.EventSendPacket;
import fun.rockstarity.api.events.list.player.EventAttack;
import fun.rockstarity.api.events.list.player.EventFinishEat;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventMotionMove;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.events.list.render.EventRender3D;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.game.ItemUtility;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.RussianNumberParser;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.math.net.objecthunter.exp4j.Expression;
import fun.rockstarity.api.helpers.math.net.objecthunter.exp4j.ExpressionBuilder;
import fun.rockstarity.api.helpers.player.InvUtility;
import fun.rockstarity.api.helpers.player.Inventory;
import fun.rockstarity.api.helpers.player.Move;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Binding;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Input;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Select.Element;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.client.modules.combat.Aura;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ChorusFruitItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.LingeringPotionItem;
import net.minecraft.item.PotionItem;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.network.play.client.CChatMessagePacket;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket.Action;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.client.CResourcePackStatusPacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.network.play.server.SEntityTeleportPacket;
import net.minecraft.network.play.server.SOpenWindowPacket;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.EffectUtils;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * @author Malecharik
 * @since 14 Mar 2024 19:48:08
 */

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Info(name="Assist", desc="Помощник на серверах", type=Category.OTHER, module={"SRPSpoof", "ResourcePack", "FTHelper", "FunTime", "RWHelper", "HWHelper", "HolyWorld", "Bind", "AutoChorus", "AutoRCT"})
public class Assist extends Module {
	
	final Select util = new Select(this, "ReallyWorld").desc("Вспомогательные функции для ReallyWorld");
	
	final Element dragonFly = new Element(util, "Ускорять /fly");
	final Element closeMenu = new Element(util, "Закрывать меню");
	final Element autootkup = new Element(util, "Авто откуп");
	final Input msgotkup = new Input(autootkup, "Сообщение").set("Выкинь шар").desc("Введите сообщение, которое будет использоваться для отправки в чат");
	Mode otkupMode = new Mode(autootkup, "Режим");
	Mode.Element def = new Mode.Element(otkupMode, "По здоровью");
	Mode.Element totem = new Mode.Element(otkupMode, "После сноса тотема");
	final Slider hpotkup = new Slider(autootkup, "Здоровье").min(1).max(19).inc(0.5f).set(5).hide(() -> !otkupMode.is(def));
	
	final Element autoFix = new Element(util, "Авто фикс");
	final Element rpSpoof = new Element(util, "Спуф рп");
	final Slider speedXZ = new Slider(dragonFly, "Скорость по XZ").min(1).max(10).inc(0.5f).set(2).hide(() -> !dragonFly.get());
	final Slider speedY = new Slider(dragonFly, "Скорость по Y").min(1).max(10).inc(1f).set(2).hide(() -> !dragonFly.get());

	@Getter final Select utils = new Select(this, "FunTime").desc("Вспомогательные функции для FunTime. Калькулятор - Позволяет выставлять предметы командой по типу \"/ah sell 64*100\"");
	final Element bind = new Element(utils, "Исп. по бинду");
	final Element autopiona = new Element(utils, "Авто /piona");
	final Element visual = new Element(utils, "Отображение радиусов");
	final CheckBox visualRadius = new CheckBox(visual, "Подсвечивать если в радиусе");
	final Element players = new Element(utils, "Радиусы у других").hide(() -> !visual.get());
	final Element time = new Element(utils, "Конвертировать время").set(true);
	final Element autorct = new Element(utils, "Перезаход при чарке");
	final Element calucalator = new Element(utils, "Калькулятор (/ah sell)").set(true);
	final Element autochorus = new Element(utils, "Авто Хорус");
	final Element noplacesphere = new Element(utils, "Не ставить сферу");
	//final Element dodge = new Element(utils, "Авто уворот"); // TODO сделать нормальный
	final Element printEffects = new Element(utils, "Вывод полученных эффектов");
	final Element recudeTime = new Element(utils, "Уменьшать задержки");
	
	final Select hwUtils = new Select(this, "HolyWorld").desc("Вспомогательные функции для HolyWorld");
	final Element autoStop = new Element(hwUtils, "Авто стоп");
	final Element bindHw = new Element(hwUtils, "Исп. по бинду");
	final Element fastbreak = new Element(hwUtils, "Быстрое разрушение шалкеров");

	final Select selectHw = new Select(bindHw, "Бинд на...").hide(() -> !this.bindHw.get());
	final Element stan = new Element(selectHw, "Стан");
	final Element trapBoom = new Element(selectHw, "Взрывная трапка");
	
	final Binding stanBind = new Binding(bindHw, "Клавиша стана").hide(() -> !stan.get() || !bindHw.get());
	final Binding trapBoomBind = new Binding(trapBoom, "Клавиша взрывной трапки").hide(() -> !trapBoom.get() || !bindHw.get());
	
	final CheckBox floats = new CheckBox(calucalator, "Округлять числа").hide(() -> !this.calucalator.get());
	
	final Select select = new Select(bind, "Бинд на..").hide(() -> !this.bind.get()).desc("Выберите предметы, которые будут использоваться по бинду");
	final Element trap = new Element(select, "Трапка");
	final Element autoplast = new Element(select, "Пласт");
	final Element shalk = new Element(select, "Шалкер");
	final Element smerch = new Element(select, "Огненый смерч");
	final Element aura = new Element(select, "Божья аура");
	final Element crossbow = new Element(select, "Арбалет");
	final Element pilb = new Element(select, "Явная пыль");
	final Element flesh = new Element(select, "Моча Флеша");
	final Element med = new Element(select, "Зелье медика");
	final Element agent = new Element(select, "Зелье Агента");
	final Element win = new Element(select, "Зелье Победителя");
	final Element killer = new Element(select, "Зелье Киллера");
	final Element otr = new Element(select, "Зелье Открыжки");
	final Element serka = new Element(select, "Серная Кислота");
	final Element vsp = new Element(select, "Вспышка");
	final Element snow = new Element(select, "Снежок заморозки");
	final Element dezorent = new Element(select, "Дезориентация");
	final Element trident = new Element(select, "Трезубец");
	final Element horus = new Element(select, "Хорус");
	final Element charGapple = new Element(select, "Чарка");
	
	final Binding trapBind = new Binding(bind, "Клавиша трапки").hide(() -> !trap.get() || !bind.get()).desc("Кнопка, при нажатии которой, будет активироваться трапка");
	final Binding crossbowBind = new Binding(bind, "Клавиша арбалета").hide(() -> !crossbow.get() || !bind.get()).desc("Кнопка, при нажатии которой, будет выстреливать арбалет");
	final Binding autoplastBind = new Binding(bind, "Клавиша пласта").hide(() -> !autoplast.get() || !bind.get()).desc("Кнопка, при нажатии которой, будет активироваться пласт");
	final Binding smerchBind = new Binding(bind, "Клавиша смерча").hide(() -> !smerch.get() || !bind.get()).desc("Кнопка, при нажатии которой, будет активироваться смерч");
	final Binding auraBind = new Binding(bind, "Клавиша ауры").hide(() -> !aura.get() || !bind.get()).desc("Кнопка, при нажатии которой, будет активироваться аура");
	final Binding pilbBind = new Binding(bind, "Клавиша пыли").hide(() -> !pilb.get() || !bind.get()).desc("Кнопка, при нажатии которой, будет активироваться пыль");
	final Binding fleshBind = new Binding(bind, "Клавиша флеша").hide(() -> !flesh.get() || !bind.get()).desc("Кнопка, при нажатии которой, будет бросаться флеш");
	final Binding medBind = new Binding(bind, "Клавиша медика").hide(() -> !med.get() || !bind.get()).desc("Кнопка, при нажатии которой, будет бросаться медик");
	final Binding agentBind = new Binding(bind, "Клавиша агента").hide(() -> !agent.get() || !bind.get()).desc("Кнопка, при нажатии которой, будет бросаться агентка");
	final Binding winBind = new Binding(bind, "Клавиша победилки").hide(() -> !win.get() || !bind.get()).desc("Кнопка, при нажатии которой, будет бросаться победилка");
	final Binding killerBind = new Binding(bind, "Клавиша киллера").hide(() -> !killer.get() || !bind.get()).desc("Кнопка, при нажатии которой, будет бросаться киллерка");
	final Binding otrBind = new Binding(bind, "Клавиша отрыжки").hide(() -> !otr.get() || !bind.get()).desc("Кнопка, при нажатии которой, будет бросаться отрыжка");
	final Binding serkaBind = new Binding(bind, "Клавиша серки").hide(() -> !serka.get() || !bind.get()).desc("Кнопка, при нажатии которой, будет бросаться серка");
	final Binding vspBind = new Binding(bind, "Клавиша вспышки").hide(() -> !vsp.get() || !bind.get()).desc("Кнопка, при нажатии которой, будет бросаться вспышка");
	final Binding snowBind = new Binding(bind, "Клавиша снежка").hide(() -> !snow.get() || !bind.get()).desc("Кнопка, при нажатии которой, будет активироваться снежок");
	final Binding dezorentBind = new Binding(bind, "Клавиша дезорента").hide(() -> !dezorent.get() || !bind.get()).desc("Кнопка, при нажатии которой, будет активироваться дезориентация");
	final Binding tridentBind = new Binding(bind, "Клавиша трезубца").hide(() -> !trident.get() || !bind.get()).desc("Кнопка, при нажатии которой, будет бросаться трезубец");
	final Binding horusBind = new Binding(bind, "Клавиша хоруса").hide(() -> !horus.get() || !bind.get()).desc("Кнопка, при нажатии которой, будет использоваться хорус");
	final Binding charGappleBind = new Binding(bind, "Клавиша чарки").hide(() -> !charGapple.get() || !bind.get()).desc("Кнопка, при нажатии которой, будет использоваться чаркка");
	final Binding shalkBind = new Binding(bind, "Клавиша шалкера").hide(() -> !shalk.get() || !bind.get()).desc("Кнопка, при нажатии которой, будет открываться шалкер");
	
	final CheckBox brainPlast = new CheckBox(bind, "Умный пласт").hide(() -> !bind.get() || !autoplast.get()).desc("Если снизу трапка и нажат бинд на пласт то ставит пласт вниз");
	final CheckBox onlyHotbar = new CheckBox(bind, "Только из хотбара").hide(() -> !this.bind.get() || select.getToggled().isEmpty());
	
	final Slider count = new Slider(fastbreak, "Кол-во разрушений").min(2).max(500).inc(1).set(10).hide(() -> !fastbreak.get()).desc("Устанавливает кол-во разрушений шалкера за один раз");
	final CheckBox nearPlayer = new CheckBox(trap, "Только в радиусе").desc("Позволяет использовать только когда игрок попадает в радиус трапки").hide(() -> !trap.get());

	// Для трезуба аимбот $$$
	int prevTridentSlot;
	boolean usingTrident;
	
	// Для авто /fixall
	final TimerUtility fixTimer = new TimerUtility();

    // Для конвертации времени
	final Pattern TIME_PATTERN = Pattern.compile("До следующего ивента:?\\s+(\\w+)");
	//Для автооткупа
	private boolean messageOtkup = false;

	// Для авто хоруса
	int prevChorusSlot;
	boolean usingChorus;
	TimerUtility chorusTimer = new TimerUtility();
	TimerUtility chorusWaitTimer = new TimerUtility();
	
	// Для калькулятора
	String results = "";
	final Animation calcAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	
	boolean stop;
	final TimerUtility delay = new TimerUtility();
	int tridentEntityId = -1;
	int tridentThrowerId = -1;
	String potion;
	
	boolean use;
	boolean key;
	int tick = 0;
	
	@Override
	public void onEvent(Event event) {
		// ========================= HolyWorld =========================
		
		// > Быстрое разрушение шалкеров
		if (fastbreak.get()
				&& event instanceof EventSendPacket e
				&& e.getPacket() instanceof CPlayerDiggingPacket packet
				&& packet.getAction() == Action.STOP_DESTROY_BLOCK
				&& mc.world.getBlock(packet.getPosition()) instanceof ShulkerBoxBlock
				) {
			for (int i = 0; i < count.get()-1; i++) {
				//mc.player.connection.sendPacketSilent(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.START_DESTROY_BLOCK, packet.getPosition(), packet.getFacing()));
		        mc.player.connection.sendPacketSilent(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.STOP_DESTROY_BLOCK, packet.getPosition(), packet.getFacing()));
			}
		}
		
		// > Исп. по биндам
		if (event instanceof EventKey e && this.bindHw.get() && !(mc.currentScreen instanceof ChatScreen)) {
			this.handleKeyHw(e);
		}
		
		// > АвтоСтоп
		if (this.autoStop.get()) {
		    if (event instanceof EventReceivePacket e) {
		        if (e.getPacket() instanceof SChatPacket packet) {
		            String message = TextFormatting.getTextWithoutFormattingCodes(packet.getChatComponent().getString());
		            if (message.contains("Телепортация начнется")) {
		                this.stop = true;
		                delay.reset();
		            }
		        }
		        
		        if (e.getPacket() instanceof SPlayerPositionLookPacket) {
		        	stop = false;
		        }
		    }
			
			if (event instanceof EventInput e && this.stop && !delay.passed(5000L)) {
				e.setForward(0);
				e.setStrafe(0);
				e.setJump(false);
			}
			
			if (event instanceof EventWorldChange) {
				stop = false;
			}
		}
		// ========================= ReallyWorld =========================
		
		// > Спуф рп
		if (this.rpSpoof.get() && event instanceof EventUpdate && mc.player.ticksExisted < 10) {
			mc.player.connection.sendPacket(new CResourcePackStatusPacket(CResourcePackStatusPacket.Action.ACCEPTED));
	        mc.player.connection.sendPacket(new CResourcePackStatusPacket(CResourcePackStatusPacket.Action.SUCCESSFULLY_LOADED));
	        if (mc.currentScreen instanceof ConfirmScreen) mc.displayGuiScreen(null);
		}
		
		// > Драгон флай
		if (event instanceof EventMotionMove e) {
			if (this.dragonFly.get()) { // Включен ли сеттинг dragonFly
				if (mc.player.abilities.isFlying) { // Проверяем, летает ли игрок
					if (!mc.player.isSneaking() && mc.getGameSettings().keyBindJump.isKeyDown()) { // Проверяем, зажат ли прыжок и не зажат шифт у игрока
						mc.player.getMotion().y = this.speedY.get(); // Устанавливаем движение по Y
					} else if (mc.getGameSettings().keyBindSneak.isKeyDown()) { // Проверяем, зажат ли шифт у игрока 
						mc.player.getMotion().y = -this.speedY.get(); // Отрицательное значение Y для движения вниз
					}
					Move.setMoveMotion(e, this.speedXZ.get()); // Устанавливаем скорость движения по Горизонтали (X, Z)
				}
			}
		}
		
		// > Авто фикс
		// Проверяем, является ли событие моушеном
		if (event instanceof EventMotion) {
			// Проверяем, включен ли авто фикс & имеет ли игрок кд
			if (this.autoFix.get() && !Server.hasCT()) {
				// Получаем инвентарь игрока
				PlayerInventory inv = mc.player.inventory;
				// Перебираем предметы в инвентаре игрока
				for (int i = 0; i < inv.getSizeInventory(); i++) {
					ItemStack stack = inv.getStackInSlot(i); // Получаем предмет в текущем слоте
					if (!stack.isEmpty() && stack.getItem().isDamageable()) { // Проверяем, не пуст ли слот и является ли предмет поддающимся повреждениям
						int max = stack.getMaxDamage(), cur = max - stack.getDamage(); // Вычисляем текущую и максимальную прочность предмета
						double perc = (double) cur / (double) max; // Вычисляем процент оставшейся прочности
						// Проверяем, не меньше ли 50% прочность и пришло ли время для починки
						if (perc < 0.5f && mc.player.ticksExisted % 25 == 0) {
							mc.player.sendChatMessage("/fix all"); // Отправляем сообщение в чат для починки всех предметов
							fixTimer.reset();
							
							break; // Выходим из цикла после отправки сообщения, чтобы избежать множества сообщений за один тик
						}
					}
				}
			}
		}
		
		// > Авто закрытие меню
		// Проверяем, является ли событие отправкой пакета
		if (event instanceof EventReceivePacket e) {
			// Проверяем, включена ли опция закрытия меню
		    if (this.closeMenu.get()) {
		        if (e.getPacket() instanceof SOpenWindowPacket packet) { // Проверяем, является ли полученный пакет типа SOpenWindowPacket
		            String text = packet.getTitle().getUnformattedComponentText().trim(); // Получаем заголовок открытого окна
		            if (text.contains("Меню") || packet.getTitle().getString().contains("ꈁꀀꈂꌁꈂꀁ")) { // Проверяем, содержит ли заголовок "Меню"
		                mc.player.connection.sendPacket(new CCloseWindowPacket(packet.getWindowId()));
		                e.cancel();
		            }
		        }
		    }
			
			if (e.getPacket() instanceof SChatPacket packet) {
				String message = TextFormatting.getTextWithoutFormattingCodes(packet.getChatComponent().getString());
				
				if (message.contains("У Вас нет доступа к данной команде.") && !fixTimer.passed(500)) {
					this.autoFix.set(false);
				}
			}
		}

		if (this.autootkup.get() && event instanceof EventUpdate) {
		    Aura auraModule = rock.getModules().get(Aura.class);
		    LivingEntity target = auraModule.getPrevTarget();

		    if (target != null) {
		        if (otkupMode.is(def)) {
		            float health = target.getHealth();
		            if (health < hpotkup.get() && !messageOtkup) {
		                mc.player.sendChatMessage(target.getName().getString() + " " + msgotkup.get());
		                messageOtkup = true;
		            } else if (health >= hpotkup.get() && messageOtkup) {
		                messageOtkup = false;
		            }
		        }
		    }
		}
		
		if (this.autootkup.get() && event instanceof EventTotemBreak e) {
		    if (otkupMode.is(totem)) {
		        Entity entity = e.getEntity();
		        if (entity instanceof LivingEntity living) {
		            if (!messageOtkup) {
		                mc.player.sendChatMessage(living.getName().getString() + " " + msgotkup.get());
		                messageOtkup = true;
		            }
		        }
		    }
		}

		// ========================== FunTime ==========================
		
		// > Уменьшение кд
		if (recudeTime.get()) {
			if (event instanceof EventSetCooldown e) {
				if (e.getItem() == Items.ENCHANTED_GOLDEN_APPLE
						|| e.getItem() == Items.GOLDEN_APPLE
						|| e.getItem() == Items.POTION
						|| e.getItem() == Items.CHORUS_FRUIT) {
					e.setCooldown(e.getCooldown() - 32);
				}
			}
			
			if (event instanceof EventFinishEat e && ItemUtility.contains(mc.player.getHeldItemMainhand(), "Исцеление")) {
				mc.player.getCooldownTracker().setCooldown(Items.POTION, (int) (13.5f * 20));
			}

			if (event instanceof EventRender2D && (mc.player.getHeldItemMainhand().getItem() != Items.POTION || ItemUtility.contains(mc.player.getHeldItemMainhand(), "Исцеление"))) {
				if (mc.player.getCooldownTracker().hasCooldown(mc.player.getHeldItemMainhand().getItem())) {
					mc.getGameSettings().keyBindUseItem.setPressed(false);
				} else {
					mc.getGameSettings().keyBindUseItem.setPressed(GLFW.glfwGetMouseButton(mc.getMainWindow().getHandle(), mc.getGameSettings().keyBindUseItem.getDefault().getKeyCode()) == 1);
				}
			}
		}
		
		// > Принт эффектов
		if (printEffects.get()) {
			if (event instanceof EventPotionHit e) {
				if (e.getTarget().getName().getString().equals(mc.player.getName().getString())) return;
				if (!(e.getTarget() instanceof PlayerEntity)) return;
				
				Chat.msg(String.format("%s%s%s получил %s%s", TextFormatting.WHITE, e.getTarget().getName().getString(), TextFormatting.GRAY, TextFormatting.WHITE, e.getPotion().getName().getString()));
				for (EffectInstance effectinstance : e.getEffects()) {
                    Effect effect = effectinstance.getPotion();
					int i = (int)(e.getDuration() * (double)effectinstance.getDuration() + 0.5D);

                    if (i > 20)
                    {
    					EffectInstance eff = new EffectInstance(effect, i, effectinstance.getAmplifier(), effectinstance.isAmbient(), effectinstance.doesShowParticles());
    					Chat.msg(
    							String.format("%s%s %s %s(%s)", 
    									effect.getEffectType() == EffectType.HARMFUL ? TextFormatting.RED : TextFormatting.BLUE, I18n.format(eff.getEffectName()), 
    									I18n.format("enchantment.level." + (eff.getAmplifier() + 1)).replace("enchantment.level.", ""), 
    									TextFormatting.GRAY, EffectUtils.getPotionDurationString(eff, 1))
    					);
                    }
                };
			}
		}
		
		/*
		// > Авто Додж
		if (dodge.get()) {
			if (event instanceof EventReceivePacket e) {
				if (e.getPacket() instanceof SSpawnObjectPacket packet) {
					if (packet.getType() == EntityType.TRIDENT) {
						tridentEntityId = packet.getEntityID();
						tridentThrowerId = packet.getData();
					}
				}
			}
			
			if (event instanceof EventInput e && tridentEntityId != -1) {
				Entity entity = mc.world.getEntityByID(tridentEntityId);
				
				if (entity != null && (entity instanceof TridentEntity || entity instanceof ArrowEntity)) {
					if (tridentThrowerId == mc.player.getEntityId()) {
						tridentEntityId = -1;
						tridentThrowerId = -1;
						return;
					}
					
					dodge(e, entity);
				}
			}
			
			if (event instanceof EventUpdate && tridentEntityId != -1) {
				Entity entity = mc.world.getEntityByID(tridentEntityId);
				
				if (entity != null && entity instanceof TridentEntity ent && !mc.world.getAllEntities().contains(ent)) {
					tridentEntityId = -1;
					tridentThrowerId = -1;
				}
			}
		}
		*/
		
		if (nearPlayer.get()) {
		    if (event instanceof EventUpdate) {
		        double partialTicks = mc.currentScreen == null ? mc.getRenderPartialTicks() : 0;
		        double x = mc.player.lastTickPosX + (mc.player.getPosX() - mc.player.lastTickPosX) * partialTicks;
		        double y = mc.player.lastTickPosY + (mc.player.getPosY() - mc.player.lastTickPosY) * partialTicks;
		        double z = mc.player.lastTickPosZ + (mc.player.getPosZ() - mc.player.lastTickPosZ) * partialTicks;

		        AxisAlignedBB box1 = new AxisAlignedBB(x - 1, y, z - 1, x + 2, y + 2, z + 2);
		        AxisAlignedBB box2 = new AxisAlignedBB(x - 2, y, z - 2, x + 3, y + 3, z + 3);

		        use = mc.world.getPlayers().stream().filter(ent -> ent != mc.player).map(PlayerEntity::getBoundingBox).anyMatch(entityBox -> box1.intersects(entityBox) || box2.intersects(entityBox));
		    }
		}
		
		// > Авто Хорус
		if (this.autochorus.get() && rock.getModules().get(Aura.class).getPrevTarget() != null) {
    		if (event instanceof EventUpdate e) {
    			LivingEntity target = rock.getModules().get(Aura.class).getPrevTarget();
    			if (mc.player.getDistance(target) < 5) {
    				if (!(target.getActiveItemStack().getItem() instanceof ChorusFruitItem)) {
        				chorusTimer.reset();
        			}
        			
        			if (this.chorusTimer.passed(200) && target.getActiveItemStack().getItem() instanceof ChorusFruitItem) {
        				int slot = Inventory.findItemNoChanges(44, Items.CHORUS_FRUIT);
        				
            			boolean inHotbar = slot <= 8;
        				if (slot != -1 && !usingChorus) {
        					prevChorusSlot = mc.player.inventory.currentItem;
            				mc.getGameSettings().keyBindUseItem.setPressed(true);
                			
                			if (inHotbar) {
                				mc.player.inventory.currentItem = slot;
                			} else {
                				mc.playerController.pickItem(slot);
                			}
                			
            				usingChorus = true;
        				}
        				
        				chorusWaitTimer.reset();
        			}
    			}
    			
    			if (usingChorus && chorusWaitTimer.passed(1000)) {
        			boolean inHotbar = prevChorusSlot <= 8;
        			
					mc.getGameSettings().keyBindUseItem.setPressed(false);
					
        			if (inHotbar) {
        				mc.player.inventory.currentItem = prevChorusSlot;
        			} else {
        				mc.playerController.pickItem(prevChorusSlot);
        			}
                    usingChorus = false;
				}
    		}
    		
    		if (event instanceof EventReceivePacket e && rock.getModules().get(Aura.class).getPrevTarget() != null) {
    			LivingEntity target = rock.getModules().get(Aura.class).getPrevTarget();
    			
        		if (e.getPacket() instanceof SEntityTeleportPacket packet && packet.getEntityId() == target.getEntityId()) {
        			this.chorusWaitTimer.reset();
        		}
    		}
    		
    		if (event instanceof EventAttack e && this.usingChorus) {
        		e.cancel();
    		}
    		
    		if (event instanceof EventFinishEat e && e.getEntity() == mc.player && usingChorus) {
        		if (e.getItem() instanceof ChorusFruitItem) {
        			boolean inHotbar = prevChorusSlot <= 8;
        			
        			if (inHotbar) {
        				mc.player.inventory.currentItem = prevChorusSlot;
        			} else {
        				mc.playerController.pickItem(prevChorusSlot);
        			}
        			mc.getGameSettings().keyBindUseItem.setPressed(false);
        			usingChorus = false;
        		}
    		}
		}
		
		// > Калькулятор
		if (this.calucalator.get()) {
			if (event instanceof EventSendPacket e) {
			    if (e.getPacket() instanceof CChatMessagePacket packet) {
			        String msg = packet.getMessage();
			        
			        if (msg.startsWith("/ah sell ")) {
			            String result = calc(msg.replace("/ah sell ", "")); // Вычисляем пример (Если он имеется)
			            if (result.matches("\\d+(\\.\\d+)?")) {
			                packet.setMessage("/ah sell " + result);
			            }
			            this.results = "";
			            this.calcAnim.setForward(false);
			        }
			    }
			}
			if (event instanceof EventChatScreen e) {
			    String input = e.getField().getText().replace("/ah sell ", "");
			    this.calcAnim.setForward(!this.results.isEmpty());
			    if (!input.isEmpty() && input.matches(".*[+\\-*/%\\cos\\sin].*") && e.getField().getText().startsWith("/ah sell ")) {
			        results = calc(input);
			    } else {
			    	results = "";
			    }
			    if (!this.calcAnim.finished(false)) bold.get(16).draw(e.getMatrixStack(), "Итоговое число: " + results, 3 * this.calcAnim.get(), sr.getScaledHeight() - 27, rock.getThemes().getTextFirstColor().alpha(this.calcAnim.get()));
			}
		}
		
		if (event instanceof EventReceivePacket e) {
			if (e.getPacket() instanceof SChatPacket packet) {
				String m = packet.getChatComponent().getString(); // Создаем String с названием m которое является текстом из чата
				if (m.contains("Invalid move player packet received"))
					e.cancel();
			}
		}
		// > Авто пиона
		if (this.autopiona.get() && event instanceof EventReceivePacket e) {
			if (e.getPacket() instanceof SChatPacket packet) {
				String m = packet.getChatComponent().getString(); // Создаем String с названием m которое является текстом из чата
				
				// При первом заходе на анку прописываем пиону
				if (m.contains("10,000 было начислено вам") || m.contains("Повторите текст еще раз")) {
					mc.player.sendChatMessage("/piona");				
				}
			}
			
			if (e.getPacket() instanceof SOpenWindowPacket packet) {
				// Отменяем визуальное появление менюшки + зажимаем шифт
				if (packet.getTitle().getString().contains("Вам подарок")) {
					e.cancel();
					mc.getGameSettings().keyBindSneak.setPressed(true);
				}
			}
			
			if (e.getPacket() instanceof SPlaySoundEffectPacket packet) {
				// Убираем звуки
				if (packet.getSound().getName().toString().contains("glass.place") || packet.getSound().getName().toString().contains("note_block.xylophone")) {
					e.cancel();
				}
			}
			
			if (e.getPacket() instanceof SSetSlotPacket packet) {
				ItemStack stack = packet.getStack();

				// Если предмет тот, по которому надо кликнуть
				if (stack.getDisplayName().getString().contains("Ключ")) {
					mc.getGameSettings().keyBindSneak.setPressed(false); // Отжимаем шифт
					
					boolean click = true;

					List<ITextComponent> itemTooltip = stack.getTooltip(null, ITooltipFlag.TooltipFlags.NORMAL);
					for (ITextComponent str : itemTooltip) {
						if (str.getString().contains("Стоп")) { // Если пиона уже забрана, закрываем гуишку на серверсайде пакетом
							mc.player.connection.sendPacket(new CCloseWindowPacket(packet.getWindowId()));
							click = false;
							break;
						}
					}
					
					if (click) // Кликаем по пионе чтобы забрать ее
						mc.player.connection.sendPacket(new CClickWindowPacket(packet.getWindowId(),
								13, 0, ClickType.PICKUP,
								mc.player.openContainer.getSlot(13).getStack(),
								mc.player.openContainer.getNextTransactionID(mc.player.inventory)));
				}
			}
		}
		
		if (brainPlast.get()) {
			if (tick > 0) Player.look(event, mc.player.rotationYaw, 90, true); 
			if (event instanceof EventUpdate) {
		        if (tick > 0) tick--;
		        if (tick == 0 && key) {
		            if (this.onlyHotbar.get()) {
		                hotbar(Items.DRIED_KELP);
		            } else {
		            	InvUtility.use(Items.DRIED_KELP);
		            }
		            key = false;
		            tick = -1;
		        }
			}
		}
		
		// > Конвертация времени
		if (this.time.get() && event instanceof EventReceivePacket e && e.getPacket() instanceof SChatPacket packet) {
			String message = TextFormatting.getTextWithoutFormattingCodes(packet.getChatComponent().getString());
			Matcher matcher = this.TIME_PATTERN.matcher(message);
			if (matcher.find() && !matcher.group(1).isEmpty()) {
				boolean numeric = matcher.group(1).matches("[-+]?[0-9]*\\.?[0-9]+");
				if (!numeric)
					return;
				
		        int parse = Integer.parseInt(matcher.group(1));
		        String minutes = parse / 60 + " мин";
		        String seconds = parse % 60 + " сeк";

		    	String correctedMessage = message.replace("[1]§r До следующего ивента:§r ", "").replace(" сек§r", "");

		        String formattedTime = "§c" + minutes + " " + seconds;

				packet.setChatComponent(new TranslationTextComponent(message.replace(matcher.group(1), formattedTime).replace("[1]", "§c[1]").replace("До следующего", "§6До следующего").replace("ивента:§r ", "ивента:§r§c ").replace("сек", "")));
			}
			
			if (matcher.find() && message.contains("Статус: »") && !matcher.group(1).isEmpty()) {
				boolean numeric = matcher.group(1).matches("[-+]?[0-9]*\\.?[0-9]+");
				
				if (!numeric)
					return;
				
				int parsed = Integer.parseInt(message.split(" ")[message.split(" ").length-2]);
				
		        String minutes = parsed / 60 + " мин";
		        String seconds = parsed % 60 + " сeк";

		    	String correctedMessage = message.replace("[1]§r До следующего ивента:§r ", "").replace(" сек§r", "");

		        String formattedTime = ": §c" + minutes + " " + seconds;

			    packet.setChatComponent(new TranslationTextComponent(message.replace(" " + parsed, formattedTime).replace("||", "§c||").replace("Статус", "§fСтатус").replace("»", "§e»").replace("сек", "")));
			}
		}
		
		// > Авто ркт
		// Игрок закончил хавать
		if (event instanceof EventFinishEat e && e.getEntity() == mc.player) {
			if (autorct.get() && e.getItem() == Items.ENCHANTED_GOLDEN_APPLE && !Server.hasCT()) { // Предмет == Чарка и нет кт
				if (!rock.getModules().get(KTLeave.class).getBinds().isEmpty()) {
					rock.getModules().get(KTLeave.class).toggle();
				} else {
					rock.getCommands().execute("rct"); // Перезаходим на анку
				}
			}
		}
		
		if (visualRadius.get()) {
			if (event instanceof EventUpdate) {
				for (PlayerEntity entity : mc.world.getPlayers()) {
					if (entity == mc.player) continue;
		            ItemStack heldItem = mc.player.getHeldItemMainhand();
		            String itemName = heldItem.getDisplayName().getString().toLowerCase();
		            boolean glowNearby = false;

		            if (itemName.contains("дезориентация") && mc.player.getDistance(entity) <= 10) {
		                glowNearby = true;
		            }

		            if (itemName.contains("трапка") && mc.player.getDistance(entity) <= 4) {
		                glowNearby = true;
		            }
		            
		            entity.setGlowing(glowNearby);
				}
			}
		}
		
		// > Визуализация трапок и тд
		if (event instanceof EventRender3D e && this.visual.get()) {
		    MatrixStack ms = e.getMatrixStack();
		    
		    if (players.get()) {
		        for (PlayerEntity entity : mc.world.getPlayers()) {
		            renderEffects(ms, entity, e);
		        }
		    }
		    
		    renderEffects(ms, mc.player, e);
		}
		
		// > Вещи по биндам
		if (!(mc.currentScreen instanceof ChatScreen) && this.bind.get()) {
			if (event instanceof EventKey e) {
				handleKey(e);
			}
		}
		
		if (event instanceof EventUpdate && usingTrident) {
			mc.getGameSettings().keyBindUseItem.setPressed(true);
		}
	}
	
	public void handleKeyHw(EventKey e) {
		if (!e.isReleased()) {
			this.handleItemMovement(e, this.stan.get(), this.stanBind, Items.NETHER_STAR);
			this.handleItemMovement(e, this.trapBoom.get(), this.trapBoomBind, Items.PRISMARINE_SHARD);
		}
	}
	
	private void handleHoldItems(Item item, EventKey e) {
		if (item.isCooldowned()) return;
		
		if (!e.isReleased()) {
			int from = Inventory.findItemNoChanges(44, item);
			
			if (from == -1) return;
			
			boolean inHotbar = from <= 8;
			if (inHotbar) {
				prevTridentSlot = mc.player.inventory.currentItem;
				mc.player.inventory.currentItem = from;
				mc.getGameSettings().keyBindUseItem.setPressed(true);
			} else {
				prevTridentSlot = from;
				mc.playerController.pickItem(from);
			}
	    	
	    	this.usingTrident = true;
		} else {
	    	if (this.usingTrident) {
	    		mc.getGameSettings().keyBindUseItem.setPressed(false);
	    		mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));
	    		
    			boolean inHotbar = prevTridentSlot <= 8;
    			
    			if (inHotbar) {
    				mc.player.inventory.currentItem = prevTridentSlot;
    			} else {
    				mc.playerController.pickItem(prevTridentSlot);
    			}
	    		this.usingTrident = false;
	    	}
		}
	}
	
	private void renderEffects(MatrixStack ms, LivingEntity entity, EventRender3D e) {
	    ItemStack heldItem = entity.getHeldItemMainhand();
	    String itemName = heldItem.getDisplayName().getString().toLowerCase();
	    
	    double partialTicks = mc.currentScreen == null ? mc.getRenderPartialTicks() : 0;
	    double x = entity.lastTickPosX + (entity.getPosX() - entity.lastTickPosX) * partialTicks - mc.getRenderManager().info.getProjectedView().getX();
	    double y = entity.lastTickPosY + (entity.getPosY() - entity.lastTickPosY) * partialTicks - mc.getRenderManager().info.getProjectedView().getY();
	    double z = entity.lastTickPosZ + (entity.getPosZ() - entity.lastTickPosZ) * partialTicks - mc.getRenderManager().info.getProjectedView().getZ();

	    if (itemName.contains("дезориентация")) {
	        drawGlowEffect(ms, x, y, z, e);
	    }

	    if (itemName.contains("трапка")) {
	        drawBoxEffect(e);
	    }
	}
	
	private void drawGlowEffect(MatrixStack ms, double x, double y, double z, EventRender3D e) {
	    int len = 361;
	    double size = 1.5f;
	    int start = (int) mc.getRenderManager().info.getPitch();

	    for (int i = start; i <= start + len - 1; i++) {
	        double angle = Math.toRadians(i);
	        double s = 0.5f;
	        double posX = x + Math.cos(angle) * 20 * s;
	        double posY = y;
	        double posZ = z + Math.sin(angle) * 20 * s;

	        ms.push();
	        GlStateManager.depthMask(false);
	        ms.translate(posX, posY, posZ);
	        ms.rotate(mc.getRenderManager().info.getRotation());

	        Render.drawImage(ms, "masks/glow.png", -size / 2, -size / 2, -size / 2, size, size, Style.getPoint(i * 10));
	        GlStateManager.depthMask(true);
	        ms.pop();
	    }
	}
	
	private void drawBoxEffect(EventRender3D e) {
	    LivingEntity entity = mc.player;

	    double playerX = entity.lastTickPosX + (entity.getPosX() - entity.lastTickPosX) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getX();
	    double playerY = entity.lastTickPosY + (entity.getPosY() - entity.lastTickPosY) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getY();
	    double playerZ = entity.lastTickPosZ + (entity.getPosZ() - entity.lastTickPosZ) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getZ();

	    GL11.glPushMatrix();
	    GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

	    GL11.glRotated(e.getRenderInfo().getPitch(), 1.0, 0.0, 0.0);
	    GL11.glRotated(e.getRenderInfo().getYaw() + 180, 0.0, 1.0, 0.0);

	    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	    GL11.glEnable(GL11.GL_BLEND);
	    GL11.glLineWidth(2);
	    GL11.glDisable(GL11.GL_TEXTURE_2D);
	    GL11.glDisable(GL11.GL_DEPTH_TEST);

	    double offsetX = -0.5;
	    double offsetY = 0.0;
	    double offsetZ = -0.5;

	    Render.setColor(Style.getMain().getRGB());
	    Render.drawBoxing(new AxisAlignedBB(playerX + offsetX - 1, playerY + offsetY, playerZ + offsetZ - 1, playerX + offsetX + 2, playerY + offsetY, playerZ + offsetZ + 2));

	    Render.setColor(Style.getSecond().getRGB());
	    Render.drawBoxing(new AxisAlignedBB(playerX + offsetX - 2, playerY + offsetY, playerZ + offsetZ - 2, playerX + offsetX + 3, playerY + offsetY, playerZ + offsetZ + 3));

	    GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

	    GL11.glPopAttrib();
	    GL11.glPopMatrix();
	}


	
	private void handleKey(EventKey e) {
		if (mc.currentScreen != null) return;
		
	    if (!e.isReleased()) {
	    	if (use || !nearPlayer.get()) {
	    		handleItemMovement(e, this.trap.get(), this.trapBind, Items.NETHERITE_SCRAP);
	    	}
	        handleItemMovement(e, this.autoplast.get(), this.autoplastBind, Items.DRIED_KELP);
	        handleItemMovement(e, this.smerch.get(), this.smerchBind, Items.FIRE_CHARGE);
	        handleItemMovement(e, this.aura.get(), this.auraBind, Items.PHANTOM_MEMBRANE);
	        handleItemMovement(e, this.pilb.get(), this.pilbBind, Items.SUGAR);
	        handleItemMovement(e, this.dezorent.get(), this.dezorentBind, Items.ENDER_EYE);
	        handleItemMovement(e, this.snow.get(), this.snowBind, Items.SNOWBALL);
	        handleItemMovement(e, this.shalk.get(), this.shalkBind, Items.SHULKER_BOX);
	        handleItemMovement(e, this.crossbow.get(), this.crossbowBind, Items.CROSSBOW);
	        handleNamedPotionThrow(e, agent.get(), agentBind, "Зелье Агента");
	        handleNamedPotionThrow(e, med.get(), medBind, "Зелье медика");
	        handleNamedPotionThrow(e, win.get(), winBind, "Зелье Победителя");
	        handleNamedPotionThrow(e, killer.get(), killerBind, "Зелье Киллера");
	        handleNamedPotionThrow(e, otr.get(), otrBind, "Зелье Открыжки");
	        handleNamedPotionThrow(e, serka.get(), serkaBind, "Серная Кислота");
	        handleNamedPotionThrow(e, vsp.get(), vspBind, "Вспышка");
	        handleNamedPotionThrow(e, flesh.get(), fleshBind, "Моча Флеша");
	    }
	    
	    if (this.trident.get() && this.tridentBind.getBindByKey(e).isPresent()) {
	    	handleHoldItems(Items.TRIDENT, e);
	    }
	    
	    if (horus.get() && horusBind.getBindByKey(e).isPresent()) {
	    	handleHoldItems(Items.CHORUS_FRUIT, e);
	    }
	    
	    if (charGapple.get() && charGappleBind.getBindByKey(e).isPresent()) {
	    	handleHoldItems(Items.ENCHANTED_GOLDEN_APPLE, e);
	    }
	}
	
	private void handleItemMovement(EventKey e, boolean isFeatureEnabled, Binding bind, Item item) {
	    if (isFeatureEnabled && bind.getBindByKey(e).isPresent() && Player.findItemInInv(45, item) != -1) {
	        if (autoplastBind.getParent() == bind.getParent()) {
	            if (brainPlast.get() && isTrapNear()) {
	                key = true;
	                tick = 2;
	            } else {
	                if (this.onlyHotbar.get()) {
	                    hotbar(item);
	                } else {
	                    InvUtility.use(item);
	                }
	            }
	        } else {
	            if (this.onlyHotbar.get()) {
	                hotbar(item);
	            } else {
	                InvUtility.use(item);
	            }
	        }
	    }
	}
	
	private void handleNamedPotionThrow(EventKey e, boolean enabled, Binding bind, String expectedName) {
		if (!enabled || !bind.getBindByKey(e).isPresent()) return;
		
		for (int i = 0; i < 36; i++) {
			ItemStack stack = mc.player.inventory.getStackInSlot(i);
			if (isMatchingPotion(stack, expectedName)) {
	            if (this.onlyHotbar.get()) {
	                hotbar(stack.getItem());
	            } else {
	                InvUtility.use(stack.getItem());
	            }
			}
		}
	}
	
	private void hotbar(Item item) {
	    for (int i = 0; i < 9; i++) {
	        ItemStack itemStack = mc.player.inventory.getStackInSlot(i);
	        if (itemStack.getItem() == item) {
	            mc.player.connection.sendPacket(new CHeldItemChangePacket(i));
	            if (rock.getModules().get(Aura.class).getTarget() != null) {
	                mc.player.connection.sendPacket(new CPlayerPacket.RotationPacket(
	                mc.player.rotationYaw, mc.player.rotationPitch, true));
	            }
	            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
	            mc.player.swingArm(Hand.MAIN_HAND);
	            mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
	            break;
	        }
	    }
	}
	
	public String calc(String expression) {
        if (expression.matches(".*[а-яА-Я]+.*")) {
            expression = RussianNumberParser.convert(expression);
        }

        expression = expression.replaceAll("\\s+", "");

        if (expression.isEmpty()) return "";

        try {
            Expression expr = new ExpressionBuilder(expression).build();
            double result = expr.evaluate();
            if (this.floats.get()) {
                result = Math.round(result);
            }
            
            if (result == (long) result) {
                return String.valueOf((long) result);
            } else {
                return String.valueOf(result);
            }
        } catch (IllegalArgumentException e) {
            return expression;
        }
	}
	
	private void dodge(EventInput e, Entity entity) {
		//double dodgeDirection = (Math.random() > 0.5) ? 1 : -1;
		
		//mc.player.getMotion().x += dodgeDirection * 0.2;
		//mc.player.getMotion().z += dodgeDirection * 0.2;
		
		if (mc.player.getDistance(entity.getPositionVec()) < 7
				|| mc.player.getDistance(entity.getPositionVec().add(entity.getMotion().mul(2, 2, 2))) < 7
				|| mc.player.getDistance(entity.getPositionVec().add(entity.getMotion().mul(3, 3, 3))) < 7
				|| mc.player.getDistance(entity.getPositionVec().add(entity.getMotion().mul(4, 4, 4))) < 7
				|| mc.player.getDistance(entity.getPositionVec().add(entity.getMotion().mul(5, 5, 5))) < 7) {
			if (Player.getAngle(entity) < 45 || Player.getAngle(entity) > 135) {
				e.setStrafe(entity.getEntityId() % 2 == 0 ? 1 : -1);
			} else {
				e.setForward(1);
			}
		}
		
		//=if (mc.player.isOnGround()) mc.player.jump();
	}
	
	private boolean isMatchingPotion(ItemStack stack, String expectedName) {
		Item item = stack.getItem();
		if (item instanceof PotionItem || item instanceof SplashPotionItem || item instanceof LingeringPotionItem) {
			if (stack.hasDisplayName()) {
				return stack.getDisplayName().getString().contains(expectedName);
			}
		}
		return false;
	}
	
	public boolean isTrapNear() {
		BlockPos playerPos = mc.player.getPosition();
		
		for (int x = -3; x <= 1; x++) {
			for (int z = -3; z <= 1; z++) {
				BlockPos checkPos = playerPos.add(x, -1, z);
				if (mc.world.isAirBlock(checkPos) && mc.world.isAirBlock(checkPos.down()) && mc.world.isAirBlock(checkPos.down(2))) {
					int solidSides = 0;
					if (mc.world.getBlockState(checkPos.north()).isSolid()) solidSides++;
                    if (mc.world.getBlockState(checkPos.south()).isSolid()) solidSides++;
                    if (mc.world.getBlockState(checkPos.east()).isSolid()) solidSides++;
                    if (mc.world.getBlockState(checkPos.west()).isSolid()) solidSides++;
                    
                    if (solidSides >= 3) {
                    	return true;
                    }
				}
			}
		}
		
		return false;
	}
	
	@Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}
}
