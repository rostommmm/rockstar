package fun.rockstarity.client.modules.other;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.EventType;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.game.packet.EventSendPacket;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Input;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Select.Element;
import fun.rockstarity.api.modules.settings.list.Slider;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.NewChatGui;
import net.minecraft.network.play.client.CChatMessagePacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;
import net.minecraft.util.math.MathHelper;

/**
 * @author Malecharik
 * @since 3 мая 2024 г. 20:41:45
 */


@Info(name = "ChatUtils", desc = "Утилиты для чата", type = Category.OTHER, module = {"Hider"})
public class ChatUtils extends Module {

	private final Select delete = new Select(this, "Убрать...").desc("Убирает какой-то элемент из чата");
	private final Element delAd = new Element(delete, "Реклама");
	private final Element spam = new Element(delete, "Спам");
	@Getter
	private final Element fon = new Element(delete, "Фон");

	private final Select utils = new Select(this, "Утилиты").desc("Утилиты для использования чата");
	private final Element autoMe = new Element(utils, "Авто \"Мне\"");
	private final Element casino = new Element(utils, "Казино");
	private final Element ahme = new Element(utils, "/ah me");
	private final Element kk = new Element(utils, "Сокращать тысячи");
	private final Element russia = new Element(utils, "Русификация");
	private final Element full = new Element(utils, "/pay full");
	private final Element sosal = new Element(utils, "Байтить на \"сосал\"");
	@Getter
	private final Element chatHistory = new Element(utils, "Сохранять историю");

	private final Select highlight = new Select(this, "Подсвечивать...").desc("Элементы которые будут подсвечиваться в чате");
	@Getter
	private final Element friendsMessages = new Element(highlight, "Сообщения друзей");
	@Getter
	private final Element spec = new Element(highlight, "!Спек");
	Select settings = new Select(spec, "Настройки").desc("Выберите дополнительные настройки");
	@Getter Select.Element nearPlayer = new Select.Element(settings, "Игрок рядом");
	@Getter
	private final Element selfMention = new Element(highlight, "Упоминание себя");
	@Getter
	final Element portalGod = new Element(utils, "Игнорировать портал");
	
	private final Pattern LINK_PATTERN = Pattern.compile(".*(\\.su|\\.ru|\\.com|переходите|\\.space|\\.net|\\.club|\\.org|\\.xyz|link|\\.fun|\\.play).*",Pattern.CASE_INSENSITIVE);
	private final Pattern KEYWORD_PATTERN = Pattern.compile(".*(бан|мут|кик|в жопу|в попу|пизды|в рот|в мут|сосать дать|докс|сват|ратку|стилер|вирус|ротик|Рот|Ротик).*", Pattern.CASE_INSENSITIVE);
	private final String[] word = {"!Скинь мне монеты, и я удвою их", "!Скинь мне монеты, чтобы участвовать в лотерее! Один счастливчик получит главный приз - супер редкий предмет или крупную сумму монет!"};
	private final String[] sosalBait = {"незерку", "донат"};
	private final TimerUtility timerBait = new TimerUtility();
	private final TimerUtility timer = new TimerUtility();
	private final TimerUtility timerSosal = new TimerUtility();
	private final Pattern COINS_PATTERN = Pattern.compile("(\\w+) получено от игрока (\\w+)");
	private final Random random = new Random();
	final Pattern TIME_PATTERN = Pattern.compile("До следующего ивента: (\\w+)");
    private String lastMessage;
    private int amount;
    private int line;
    @Getter @Setter
    private boolean cancel;
    private boolean active;
    private String targetPlayer = null;
    
    private boolean startsWith(CChatMessagePacket packet, String startsOne, String startsTwo) {
    	String message = packet.getMessage();
        return message.startsWith(startsOne) || message.startsWith(startsTwo);
    }
    
	@Override
	@EventType({EventUpdate.class, EventSendPacket.class, EventReceivePacket.class})
	public void onEvent(Event event) {
		int balance = 0;
		
		
		if (event instanceof EventUpdate) {
			if (casino.get() && timer.passed(10_000)) {
				mc.player.sendChatMessage(word[random.nextInt(word.length)]);
				timer.reset();
			}
			
			if (sosal.get()) {
				if (timerBait.passed(15_000)) {
					mc.player.sendChatMessage("Кто хочет " + sosalBait[random.nextInt(word.length)] + " ?");
					if (timerSosal.passed(1050)) {
						mc.player.sendChatMessage("Кто сосал ?");
						timerSosal.reset();
					}
					timerBait.reset();
				}
			}
		}
		
		
		if (event instanceof EventSendPacket e && e.getPacket() instanceof CChatMessagePacket packet) {
			String message = packet.getMessage();
			
			if (this.ahme.get() && this.startsWith(packet, "/ah me", "/ah im")) {
				mc.player.sendChatMessage("/ah " + mc.player.getNameClear());
				e.cancel();
			}
			
			if (russia.get()) {
				if (message.startsWith("/рги")) {
					mc.player.sendChatMessage("/hub");
				} else if (message.startsWith("/кез")) {
					mc.player.sendChatMessage("/rtp");
				}
			}
			
			if (full.get() && message.startsWith("/pay")) {
				String[] parts = message.split(" ");
				
				if (parts.length >= 3 && parts[2].equalsIgnoreCase("full")) {
					String targetPlayer = parts[1];
					mc.player.sendChatMessage("/money");
					e.cancel();
					this.targetPlayer = targetPlayer;
				}
			}
			
	        if (kk.get() && message.startsWith("/ah sell")) {
	        	String[] parts = message.split(" ");
	        	
	        	if (parts.length > 2) {
	        		String amountStr = parts[2];
	        		int amount = parse(amountStr);
	        		
	        		if (amount > 0) {
	        			packet.setMessage("/ah sell " + amount);
	        		}
	        	}
	        }
		}
		
		
		if (event instanceof EventReceivePacket e && e.getPacket() instanceof SChatPacket packet) {
			String message = packet.getChatComponent().getString();
			if (message.contains("❤ Игрок") || message.contains("❤ Игрок")) {
			//	packet.setChatComponent(new TranslationTextComponent(message));
			}
		}
		
		if (event instanceof EventReceivePacket e && e.getPacket() instanceof SChatPacket packet) {
				String message = packet.getChatComponent().getString().toLowerCase();
				String cleanMessage = message.replaceAll("§[0-9a-fk-or]", "");
				
				if (message.contains("[$] ваш баланс: ")) {
					String balanceStr = cleanMessage.replaceAll("[^0-9.,]", "");
					
					if (balanceStr.contains(".")) {
						balanceStr = balanceStr.split("\\.")[0];
					}
					balanceStr = balanceStr.replace(",", "");					
					
					int bal = Integer.parseInt(balanceStr);
					
					if (bal > 0 && this.targetPlayer != null) {
						mc.player.sendChatMessage("/pay " + this.targetPlayer + " " + bal);
						this.targetPlayer = null;
					}
				}
				
				if (this.spam.get() && packet.getType() == ChatType.CHAT) {
					ITextComponent messages = packet.getChatComponent();
					String rawMessage = messages.getString();
					NewChatGui chatGui = mc.ingameGUI.getChatGUI();
					Matcher matcher = this.TIME_PATTERN.matcher(message);
					
					//if (matcher.find()) Chat.msg(true);
					
					if (lastMessage != null && lastMessage.equals(rawMessage)) {
						amount++;
						chatGui.deleteChatLine(line);
						messages.getSiblings().add(new StringTextComponent(TextFormatting.GRAY + " [x" + amount + "]"));
					} else {
						amount = 1;
					}
					++line;
					lastMessage = rawMessage;
					chatGui.printChatMessageWithOptionalDeletion(messages, line);
		            if (line > 256) line = 0;
		            event.cancel();
				}
				
				if (this.delAd.get() && LINK_PATTERN.matcher(message).matches()) {
					e.cancel();
				}
				
				if (this.autoMe.get() && !KEYWORD_PATTERN.matcher(message).matches() && message.contains("кому")) {
					mc.player.sendChatMessage("!мне");
				}
				
				if (this.casino.get()) {
					Matcher matcher = COINS_PATTERN.matcher(message.replace(",", ""));
					if (matcher.find()) {
						int coins = Integer.parseInt(matcher.group(1));
						String playerName = matcher.group(2);
						if (coins < 1000) {
							mc.player.sendChatMessage("/msg " + playerName + " Слишком маленькая сумма, минимум ставки 1000$");
						} else {
							mc.player.sendChatMessage("/msg " + playerName + " Спасибо за участие! В случае выигрыша мы вам сообщим");
						}
					}
				}
			}
		
	}
	
	private int parse(String message) {
	    double multiplier = 1.0;
	    message = message.toLowerCase().replaceAll("[^0-9кК.,]", "");

	    if (message.endsWith("ккк")) {
	        multiplier = 1000000000;
	        message = message.substring(0, message.length() - 3);
	    } else if (message.endsWith("кк")) {
	        multiplier = 1000000;
	        message = message.substring(0, message.length() - 2);
	    } else if (message.endsWith("к")) {
	        multiplier = 1000;
	        message = message.substring(0, message.length() - 1);
	    }

	    message = message.replace(',', '.');

	    try {
	        double value = Double.parseDouble(message);
	        return (int) (value * multiplier);
	    } catch (NumberFormatException e) {
	        return 0;
	    }
	}
	@NativeInclude
	@Override
	public void onEnable() {
		timer.reset();
	}
	
	@Override
	public void onDisable() {

	}

}
