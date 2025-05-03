package fun.rockstarity.api.helpers.game;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.secure.Debugger;
import lombok.experimental.UtilityClass;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

@UtilityClass
public class Chat implements IAccess {
	
	public void msg(Object msg) {
		msg(msg, true);
	}
	
	public void msg(Object msg, boolean prefix) {
		String str = msg + "";
		try {
			if (str.contains("PHP_EOL")) {
				String[] msgs = str.split("PHP_EOL");
				int i = 0;
				for (String message : msgs) {
					mc.player.addChatMessage((i == 0 || prefix ? TextFormatting.AQUA + "[Rockstar] " : "") + TextFormatting.RESET + message);
				}
				return;
			}
			mc.player.addChatMessage(TextFormatting.AQUA + "[Rockstar] " + TextFormatting.RESET + msg);
		} catch (Exception e) {}
	}
	
	public void msg(Object msg, String hover, Runnable action) {
		StringTextComponent stringtextcomponent = new StringTextComponent(I18n.format(msg.toString()));
		stringtextcomponent.setStyle(Style.EMPTY.setClickEvent(new ClickEvent(ClickEvent.Action.RUNNABLE, action)).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(hover))));
		mc.ingameGUI.getChatGUI().printChatMessage(stringtextcomponent);
	}
	
	public void msg(String prefix, Object msg) {
		mc.player.addChatMessage(String.format("%s[%s]%s %s", TextFormatting.AQUA, prefix, TextFormatting.RESET, msg));
	}
	
	public void debug(Object msg) {
		if (!rock.isDebugging()) return;
		
		String str = msg + "";
		try {
			if (str.contains("PHP_EOL")) {
				String[] msgs = str.split("PHP_EOL");
				int i = 0;
				for (String message : msgs) {
					mc.player.addChatMessage(message);
				}
				return;
			}
			mc.player.addChatMessage(TextFormatting.AQUA + "[Debug] " + TextFormatting.RESET + msg);
		} catch (Exception e) {}
	}
	
	public void bot(String bot, Object msg) {
		if (!rock.isDebugging()) return;
		
		String str = msg + "";
		try {
			
			mc.player.addChatMessage(TextFormatting.AQUA + "[" + bot + "] " + TextFormatting.RESET + msg);
			//Debugger.overlay(bot + ": " + msg.toString());
		} catch (Exception e) {}
	}
	
}
