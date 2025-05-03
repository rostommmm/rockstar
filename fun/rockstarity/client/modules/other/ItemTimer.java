package fun.rockstarity.client.modules.other;

import org.lwjgl.glfw.GLFW;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventTick;
import fun.rockstarity.api.events.list.player.EventFinishEat;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Select.Element;
import fun.rockstarity.api.modules.settings.list.Slider;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author ConeTin
 * @since 6 дек. 2023 г.
 */


@Info(name="ItemTimer", desc="Визуализация задержки на предметы", type=Category.OTHER)
public class ItemTimer extends Module {

	private final Select select = new Select(this, "Предметы").desc("Предметы на которые можно установить визуальную задержку");
	
	private final Element gapple = new Element(select, "Золотое яблоко").set(true);
	private final Element horus = new Element(select, "Хорус").set(true);
	private final Element pearl = new Element(select, "Эндер перл").set(true);
	private final Element charka = new Element(select, "Чарка");
	private final Element dezorent = new Element(select, "Дезориентация");
	private final Element trapka = new Element(select, "Трапка");
	
	private final Slider gappleCool = new Slider(this, "Кд золотого яблока").min(1).max(200).inc(1f).set(100).hide(() -> !gapple.get());
	private final Slider horusCool = new Slider(this, "Кд хоруса").min(1).max(200).inc(1f).set(100).hide(() -> !horus.get());
	private final Slider charkaCool = new Slider(this, "Кд чарки").min(1).max(200).inc(1f).set(200).hide(() -> !charka.get());
	private final Slider pearlCool = new Slider(this, "Кд эндер перла").min(1).max(300).inc(1f).set(300).hide(() -> !pearl.get());
	private final Slider trapkaCool = new Slider(this, "Кд трапки").min(1).max(20000).inc(1f).set(20000).hide(() -> !trapka.get());
	private final Slider dezorentCool = new Slider(this, "Кд дезориентации").min(1).max(20000).inc(1f).set(20000).hide(() -> !dezorent.get());
	
    private final CheckBox onlyPvp = new CheckBox(this, "Только в PVP").desc("Накладывает задержку только в PVP режиме");
	
	@Override
	@NativeInclude
	public void onEvent(Event event) {
		Item[] items = { Items.GOLDEN_APPLE, Items.CHORUS_FRUIT, Items.ENCHANTED_GOLDEN_APPLE, Items.ENDER_PEARL, Items.ENDER_EYE, Items.NETHERITE_SCRAP };
		if (event instanceof EventFinishEat e && e.getEntity() == mc.player && (Server.hasCT() || !this.onlyPvp.get())) {
			double[] cooldowns = {gappleCool.get(), horusCool.get(), charkaCool.get(), pearlCool.get(), dezorentCool.get(), trapkaCool.get()};
			
			for (int i = 0; i < items.length; i++) {
				if (e.getItem() != items[i]) continue;
				mc.player.getCooldownTracker().setCooldown(items[i], (int) cooldowns[i]);
				break;
			}
		}

		if (event instanceof EventRender2D) {
			
			for (Item item : items) { // Создаем цикл для предметов из массива items
				if (item.isCooldowned() && mc.player.getActiveItemStack().getItem() == item) { // Проверяем задержку на предмете и активный предмет
	                mc.getGameSettings().keyBindUseItem.setPressed(false); // Отменяем использование предмета
	            }
				
				if ((mc.player.getHeldItemMainhand().getItem() == item || mc.player.getHeldItemOffhand().getItem() == item) && !item.isCooldowned() && GLFW.glfwGetMouseButton(mc.getMainWindow().getHandle(), mc.getGameSettings().keyBindUseItem.getDefault().getKeyCode()) == 1 && !(mc.currentScreen instanceof ContainerScreen)) { // Если клавиша зажата и ниче не открыто
	                mc.getGameSettings().keyBindUseItem.setPressed(GLFW.glfwGetMouseButton(mc.getMainWindow().getHandle(), mc.getGameSettings().keyBindUseItem.getDefault().getKeyCode()) == 1); // Начинаем юзать по новой
				}
			}
		}
		
		if (event instanceof EventTick) {
			Item[] itemsDon = {Items.ENDER_EYE, Items.NETHERITE_SCRAP};
			
			for (Item item : itemsDon) { // Создаем цикл для предметов из массива items
				if (mc.player.getHeldItemMainhand().getItem() == item || mc.player.getHeldItemOffhand().getItem() == item) { // Проверяем предметы которые держит юзер в руках
					if (mc.getGameSettings().keyBindUseItem.isPressed() && !mc.player.getCooldownTracker().hasCooldown(item) && (Server.hasCT() || !this.onlyPvp.get())) {
						mc.player.getCooldownTracker().setCooldown(item, 400); // Устанавливаем задержку на предмет
					}
				}
			}
		}
	}
	
	@Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}
	
}
