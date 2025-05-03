package fun.rockstarity.api.modules;

import javax.management.ConstructorParameters;

import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author ConeTin
 * @since 2 дек. 2023 г.
 */

@Getter
public enum Category {
	
	COMBAT("Combat", "Сражение"),
	MOVE("Move", "Движение"),
	RENDER("Render", "Отображение"),
	PLAYER("Player", "Игрок"),
	OTHER("Other", "Остальное"),
	SCRIPTS("Scripts", "Скрипты"),
	THEMES("Themes", "Темы");
	
	private final String name, displayName;
	@Setter
	private int index;
	private final Animation hover = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(200),
			open = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(200),
			moveAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(200);

	Category(String name, String displayName) {
		this.name = name;
		this.displayName = displayName;
	}
	
}
