package fun.rockstarity.api.render.ui.clickgui.esp;

import java.util.ArrayList;
import java.util.Arrays;

import fun.rockstarity.api.render.ui.clickgui.esp.elmts.ESPArmor;
import fun.rockstarity.api.render.ui.clickgui.esp.elmts.ESPBoxes;
import fun.rockstarity.api.render.ui.clickgui.esp.elmts.ESPCooldowns;
import fun.rockstarity.api.render.ui.clickgui.esp.elmts.ESPHealth;
import fun.rockstarity.api.render.ui.clickgui.esp.elmts.ESPLeftItem;
import fun.rockstarity.api.render.ui.clickgui.esp.elmts.ESPPing;
import fun.rockstarity.api.render.ui.clickgui.esp.elmts.ESPRightItem;
import fun.rockstarity.api.render.ui.clickgui.esp.elmts.ESPShifting;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * @author ConeTin
 * @since 6 апр. 2024 г.
 */

@Getter
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class ESPSettingsHandler {
	
	ArrayList<ESPElement> espElements = new ArrayList<>();
	
	ESPBoxes boxes;
	//ESPTextElement test, test2, test3, test4, test5, test6, test7, test8;
	ESPPing ping;
	ESPRightItem rightItem;
	ESPLeftItem leftItem;
	ESPHealth health;
	ESPShifting shift;
	ESPArmor armor;
	ESPCooldowns cooldowns;
//	ESPPotions potions;

	public ESPSettingsHandler() {
		Arrays.stream(new ESPElement[] {
				boxes = new ESPBoxes(),
				//test = new ESPTextElement("Тест"),
						/*
				test = new ESPTextElement("Тестинг"),
				test2 = new ESPTextElement("Тестиров"),
				test3 = new ESPTextElement("Тест1"),
				test4 = new ESPTextElement("Тест2"),
				test5 = new ESPTextElement("Тест4"),
				test6 = new ESPTextElement("Тестаоф1"),
				test7 = new ESPTextElement("Тест6ыфы"),
				test8 = new ESPTextElement("Брроняяяяя")
				*/
//				health = new ESPHealth(),
				health = new ESPHealth(),
				armor = new ESPArmor(),
				rightItem = new ESPRightItem(),
				leftItem = new ESPLeftItem(),
				ping = new ESPPing(),
				shift = new ESPShifting(),
				cooldowns = new ESPCooldowns(),
//				potions = new ESPPotions(),
		}).forEach(espElements::add);
	}
	
}
