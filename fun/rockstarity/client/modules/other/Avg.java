package fun.rockstarity.client.modules.other;

import java.util.ArrayList;
import java.util.List;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import net.minecraft.client.Minecraft;

@Info(name="Avg", desc="Средний FPS", type=Category.OTHER)
public class Avg extends Module {
	
	private final CheckBox auto = new CheckBox(this, "Авто выключение").set(true);

    private final TimerUtility timer = new TimerUtility();
    private final TimerUtility timerOff = new TimerUtility();
    private final List<Integer> fpsValues = new ArrayList<>();
    private int minFPS = Integer.MAX_VALUE;
    private int maxFPS = Integer.MIN_VALUE;

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            int currentFPS = Minecraft.debugFPS;
            fpsValues.add(currentFPS);

            if (currentFPS < minFPS) {
                minFPS = currentFPS;
            }
            if (currentFPS > maxFPS) {
                maxFPS = currentFPS;
            }

            if (timer.passed(1200)) {
                int avgFPS = calculateAverageFPS();
               // Chat.msg("FPS: Средний: " + avgFPS + " | Минимальный: " + minFPS + " | Максимальный: " + maxFPS);
                timer.reset();
            }
            
            if (auto.get()) {
            	if (timerOff.passed(5_000)) {
            		onDisable();
            		set(false);
            	}
            }
        }
    }

    private int calculateAverageFPS() {
        if (fpsValues.isEmpty()) {
            return 0;
        }
        int sum = 0;
        for (int fps : fpsValues) {
            sum += fps;
        }
        return sum / fpsValues.size();
    }

    @Override
    public void onEnable() {
    	timerOff.reset();
        fpsValues.clear();
        minFPS = Integer.MAX_VALUE;
        maxFPS = Integer.MIN_VALUE;
    }

    @Override
    public void onDisable() {
        if (!fpsValues.isEmpty()) {
            int avgFPS = calculateAverageFPS();
            Chat.msg("Финальная статистика FPS: Средний: " + avgFPS + " | Минимальный: " + minFPS + " | Максимальный: " + maxFPS);
        }
    }
}
