package fun.rockstarity.api.render.ui.mainmenu.alt;

import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * @author ConeTin
 * @since 26 окт. 2024 г.
 */

@RequiredArgsConstructor
public enum Filter {
	FAVORITE("Избранные", "Аккаунты, помеченные как избранные", alt -> !alt.isFavorite()),
	BANNED("Забаненные", "Аккаунты, которые забанены на каком-либо сервере", alt -> !alt.isBanned()),
	RANDOM("Рандомные", "Аккаунты, созданные с рандомным названием", alt -> !alt.isRandom()),
	OTHER("Остальные", "Те аккаунты, которые не попадают под остальные категории", alt -> alt.isFavorite() || alt.isBanned() || alt.isRandom());
	
	@Getter private final Animation checkAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	@Getter private final String name;
	@Getter private final String desc;
	private final FilterProcessor function;
	@Getter @Setter private boolean enabled = true;
	@Getter private final TextBlock textBlock = new TextBlock();
	
    public boolean apply(Alt arg) {
        return enabled && !function.apply(arg);
    }
    
}
