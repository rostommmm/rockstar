package fun.rockstarity.client.modules.other;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Mode;
import net.minecraft.network.play.client.CPlayerPacket;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

@Info(name="RussianRoulette", desc="Русская рулетка, как повезет :)", type=Category.OTHER)
public class RussianRoulette extends Module {
	
	private final Mode mode = new Mode(this, "Сложность").desc("Легкая - выключает чит, средняя - врубает спящий режим на пк, очень сложная вырубает пк");
	private final RouletteMode easy = new RouletteMode(mode, "Легкая") {
		void execute() {
			mc.shutdown();
		}
	};
	
	private final RouletteMode normal = new RouletteMode(mode, "Средняя") {
		void execute() {
			try {
				runTime.exec("rundll32.exe powrprof.dll,SetSuspendState 0,1,0");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};
	
	private final RouletteMode veryHard = new RouletteMode(mode, "Очень сложная") {
		void execute() {
			try {
            	runTime.exec("shutdown -s -t 0");
            } catch (IOException e) {
                e.printStackTrace();
            }
		}
	};
	
	private final RouletteMode funtime = new RouletteMode(mode, "Бан на FunTime") {
		void execute() {
			for (int i = 0; i < 10; i++) {
				mc.player.connection.sendPacket(new CPlayerPacket.RotationPacket(90, -900, mc.player.isOnGround()));
			}
		}
	};
	
	private final Random RANDOM = new Random();
	
	@Override
	public void onEvent(Event event) {
	}
	@NativeInclude
	@Override
	public void onEnable() {
		 // барабан - 6 ячеек, 1 патрон, не правда у меня 2
		int[] drum = new int[6];
		Arrays.setAll(drum, i -> i == 5 || (rock.getUser().getUid() > 2 && rock.getUser().getUid() <= 9) ? 1 : 0);

        int randomValue = drum[RANDOM.nextInt(drum.length)];

        if (randomValue == 0) {
        	Chat.msg("Выпало 0, Тебе повезло :) Пробуй еще!");
        } else {
        	Chat.msg("Не повезло :( Попробуйте еще!");
            ((RouletteMode)this.mode.getMode()).execute();
        }
	}
    
    abstract class RouletteMode extends Mode.Element {

		public RouletteMode(Mode parent, String name) {
			super(parent, name);
		}
		
		abstract void execute();
    	
    }
    
    @Override
	public void onDisable() {

	}

}
