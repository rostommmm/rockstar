package fun.rockstarity.api.autobuy.fake;

import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ChestScreen;

/**
 * @author ConeTin
 * @since 21 Р°РїСЂ. 2024 Рі.
 */


@UtilityClass
public class ScreenTransformer {

	public Screen transform(Screen screen) {
		if (screen instanceof ChestScreen chest) {
			/*
			ChestContainer container = (ChestContainer) chest.getContainer();
			
			// РџРѕР»СѓС‡Р°РµРј РєРѕР»РёС‡РµСЃС‚РІРѕ СЃР»РѕС‚РѕРІ РІ РєРѕРЅС‚РµР№РЅРµСЂРµ
	        int containerSize = chest.getContainer().getLowerChestInventory().getSizeInventory();

	        // РћРїСЂРµРґРµР»СЏРµРј РЅР°С‡Р°Р»Рѕ РёРЅРІРµРЅС‚Р°СЂРЅС‹С… СЃР»РѕС‚РѕРІ (РѕР±С‹С‡РЅРѕ РїРѕСЃР»Рµ СЃР»РѕС‚РѕРІ СЃСѓРЅРґСѓРєР°)
	        int inventoryStart = containerSize - 27; // РџСЂРµРґРїРѕР»Р°РіР°РµРј, С‡С‚Рѕ 27 СЃР»РѕС‚РѕРІ - РёРЅРІРµРЅС‚Р°СЂСЊ

	        // Р—Р°РјРµРЅСЏРµРј СЃРѕРґРµСЂР¶РёРјРѕРµ РёРЅРІРµРЅС‚Р°СЂРЅС‹С… СЃР»РѕС‚РѕРІ РЅР° Р·РѕР»РѕС‚РѕРµ СЏР±Р»РѕРєРѕ
	        ItemStack goldenApple = new ItemStack(Items.GOLDEN_APPLE); // РЎРѕР·РґР°РµРј Р·РѕР»РѕС‚РѕРµ СЏР±Р»РѕРєРѕ
	        for (int i = inventoryStart; i < containerSize; i++) {
	            // РЈСЃС‚Р°РЅР°РІР»РёРІР°РµРј Р·РѕР»РѕС‚РѕРµ СЏР±Р»РѕРєРѕ РІ РєР°Р¶РґС‹Р№ РёРЅРІРµРЅС‚Р°СЂРЅС‹Р№ СЃР»РѕС‚
	        	chest.getContainer().getLowerChestInventory().setInventorySlotContents(i, goldenApple.copy());
	        }
	        */
		}
		
		// TODO Р”РѕРґРµР»Р°С‚СЊ СЃРєСЂС‹С‚РёРµ СЃР»РѕС‚РѕРІ РІ ContainerScreen
		Minecraft.getInstance().displayGuiScreen(screen);
		
		return screen;
	}
	
}
