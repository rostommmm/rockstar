package fun.rockstarity.client.modules.other;

import fun.rockstarity.api.autobuy.logic.items.AutoBuyItem;
import fun.rockstarity.api.autobuy.ui.AutoBuyScreen;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Clickable;
import fun.rockstarity.api.modules.settings.list.Input;
import fun.rockstarity.api.modules.settings.list.Slider;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

/**
 * @author ConeTin
 * @since 24 апр. 2024 г.
 */

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Info(name="AutoBuy", desc="Авто-покупка предметов", type=Category.OTHER)
public class AutoBuy extends Module {
	
	@NonFinal AutoBuyScreen screen;
	
	Clickable openMenu = new Clickable(this, "Меню AutoBuy")
			.set(() -> handleOpenMenu())
			.set("Открыть меню")
			.desc("Открытие меню можно назначить на клавишу нажав [ПКМ] по кнопке");
	
	Slider speed = new Slider(this, "Скорость обновления")
			.min(0).max(500).inc(50).set(500)
			.desc("Скорость обновления аукциона. Если установить скорость 500, то античит не будет замедлять работу AutoBuy");
	
	CheckBox swapAn = new CheckBox(this, "Переходить между /an")
			.desc("Переходит между двумя анархиями, если античит замедляет скорость обновления аукциона больше указанной. Есть риск бана за 4.5");
	
	Slider acSpeed = new Slider(this, "КД для перехода")
			.min(500).max(1500).inc(50).set(1000)
			.desc("При какой задержке обновления аукциона AutoBuy будет переходить на другую анархию")
			.hide(() -> !swapAn.get());
	
	Input an1 = new Input(this, "Первая анархия")
			.set("/an226")
			.desc("Укажите первую анархию для обхода задержки")
			.hide(() -> !swapAn.get());
	
	Input an2 = new Input(this, "Вторая анархия")
			.set("/an313")
			.desc("Укажите вторую анархию для обхода задержки")
			.hide(() -> !swapAn.get());
	
	CheckBox parser = new CheckBox(this, "Анализатор цен")
			.desc("'Парсит' цены с аукциона, чтобы покупать предметы по цене рыночной и продавать по рыночной цене")
			.onEnable(() -> {
				for (AutoBuyItem item : rock.getAutoBuy().getItems()) {
					item.setParsed(false);
				}
			});
	
	Slider parsePercent = new Slider(this, "Процент дешевизны").min(1).max(100).inc(1).set(44)
			.desc("Укажите, на сколько процентов дешевле рыночной цены AutoBuy будет покупать товары")
			.hide(() -> !parser.get());
	
	
	private void handleOpenMenu() {
		mc.displayGuiScreen(this.screen);
		
		this.screen.getRenderer().getOpening().setForward(true);
	}
	
	@Override
	public void onAllEvent(Event event) {
		if (event instanceof EventUpdate && this.screen == null)
			this.screen = new AutoBuyScreen();
	}
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventUpdate) {
		}
	}
	
	@Override
	public void onEnable() {
	}
	
	@Override
	public void onDisable() {

	}

}
