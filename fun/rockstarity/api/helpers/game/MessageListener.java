package fun.rockstarity.api.helpers.game;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.system.TextUtility;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import fun.rockstarity.client.commands.WayCommand;
import lombok.experimental.UtilityClass;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

/**
 * @author ConeTin
 * @since 14 июл. 2024 г.
 */

@UtilityClass
public class MessageListener implements IAccess {
	
	private final Pattern COORDINATE_PATTERN = Pattern.compile("(-?\\d+)[\\s,]+(-?\\d+)(?:[\\s,]+(-?\\d+))?");

	public ITextComponent handle(ITextComponent comp) {
		if (rock.isPanic()) return comp;
		
	    if (comp instanceof StringTextComponent strComp) {
	        String message = strComp.getString();
	        Matcher matcher = COORDINATE_PATTERN.matcher(message);

	        TranslationTextComponent chatComponent = new TranslationTextComponent("");
	        
	        int lastEnd = 0;
	        if (matcher.find()) {
	        	String x = matcher.group(1);
	            String y = matcher.group(3) == null ? "60" : matcher.group(2);
	            String z = matcher.group(3) == null ? matcher.group(2) : matcher.group(3);
	            
	        	for (ITextComponent child1 : strComp.getSiblings()) {
	             	if (child1 instanceof StringTextComponent textComponent) {
	             		if (textComponent.text.contains(matcher.group())) {
	             			textComponent.setStyle(textComponent.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUNNABLE, () -> handleCoords(comp.getString(), x,y,z))).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Нажми, чтобы поставить метку"))));
	             		}
	                }
	             	
	             	for (ITextComponent child2 : child1.getSiblings()) {
	             		if (child2 instanceof StringTextComponent textComponent) {
	             			if (textComponent.text.contains(matcher.group())) {
		             			textComponent.setStyle(textComponent.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUNNABLE, () -> handleCoords(comp.getString(), x,y,z))).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Нажми, чтобы поставить метку"))));
		             		}
	                    }
	             		
	             		for (ITextComponent child3 : child1.getSiblings()) {
	                     	if (child3 instanceof StringTextComponent textComponent) { 
	                     		if (textComponent.text.contains(matcher.group())) {
	    	             			textComponent.setStyle(textComponent.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUNNABLE, () -> handleCoords(comp.getString(), x,y,z))).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Нажми, чтобы поставить метку"))));
	    	             		}
	                        }
	                    }
	                }
	            }
	        	
	        	return comp;
	        }
	    }

	    return comp;
	}

	
	private void handleCoords(String text, String strX, String strY, String strZ) {
		try {
			int x = Integer.parseInt(strX);
			int y = Integer.parseInt(strY);
			int z = Integer.parseInt(strZ);
			
			String name = "Метка";
			
			if (text.toLowerCase().contains("мистический алтарь")) {
				name = "Мистический Алтарь";
			} else if (text.toLowerCase().contains("мистический сундук")) {
				name = "Мистический Сундук";
			} else if (text.toLowerCase().contains("маяк убийца")) {
				name = "Маяк Убийца";
			} else if (text.toLowerCase().contains("вулкан")) {
				name = "Вулкан";
			} else if (text.toLowerCase().contains("алтарь")) {
				name = "Алтарь";
			}

			if (text.contains(" сек") && text.contains("через: ")) {
				Integer time = Integer.parseInt(text.split("через: ")[1].split(" сек")[0].trim());
				rock.getCommands().get(WayCommand.class).add(name, new Vector3d(x,y,z), time);
			} else if (text.contains(" сек") && text.contains("открытия: ")) {
				Integer time = Integer.parseInt(text.split("открытия: ")[1].split(" сек")[0].trim());
				rock.getCommands().get(WayCommand.class).add(name, new Vector3d(x,y,z), time);
			} else if (text.contains(" сек") && text.contains("извержения ")) {
				Integer time = Integer.parseInt(text.split("извержения ")[1].split(" сек")[0].trim());
				rock.getCommands().get(WayCommand.class).add(name, new Vector3d(x,y,z), time);
			} else {
				rock.getCommands().get(WayCommand.class).add(name, new Vector3d(x,y,z));
			}
			
			rock.getAlertHandler().alert("Метка добавлена", AlertType.INFO);
		} catch (Exception e) {
			Chat.debug(e);
		}
	}
	
}
