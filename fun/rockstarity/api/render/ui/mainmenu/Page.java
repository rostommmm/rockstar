package fun.rockstarity.api.render.ui.mainmenu;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.ui.mainmenu.screens.AltScreen;
import fun.rockstarity.api.render.ui.mainmenu.screens.ExitScreen;
import fun.rockstarity.api.render.ui.mainmenu.screens.MainPageScreen;
import fun.rockstarity.api.render.ui.mainmenu.screens.MultiScreen;
import fun.rockstarity.api.render.ui.mainmenu.screens.SingleScreen;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;

/**
 * @author ConeTin
 * @since 20 окт. 2024 г.
 */

@Getter
@AllArgsConstructor
public enum Page implements IAccess {

    MAIN("home", new MainPageScreen()),
    SINGE("single", new SingleScreen(MAIN.getScreen())),
    MULTI("multi", new MultiScreen(MAIN.getScreen())),
    ALT("alt", new AltScreen()),
    SETTINGS("settings", new OptionsScreen(MAIN.getScreen(), mc.getGameSettings())),
    EXIT("exit", new ExitScreen());
    
    private final String icon;
    private final Screen screen;
    private final Animation showing = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
    private final Animation selection = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
    private final Animation selectionGlow = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
    
    // Находит Page по соответствующему Screen
    public static Page fromScreen(Screen screen) {
        for (Page page : values()) {
            if (page.getScreen() == screen) {
                return page;
            }
        }
        throw new IllegalArgumentException("No Page found for screen: " + screen);
    }
    
    // Возвращает предыдущий элемент (циклически)
    public static Page getPrevious(Screen screen) {
        return fromScreen(screen).previous();
    }
    
    // Возвращает следующий элемент (циклически)
    public static Page getNext(Screen screen) {
        return fromScreen(screen).next();
    }
    
    // Методы для навигации по элементам enum
    public Page previous() {
        Page[] pages = values();
        return pages[(ordinal() - 1 + pages.length) % pages.length];
    }
    
    public Page next() {
        Page[] pages = values();
        return pages[(ordinal() + 1) % pages.length];
    }
}