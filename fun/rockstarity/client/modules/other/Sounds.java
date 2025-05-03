package fun.rockstarity.client.modules.other;


import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventKill;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.player.EventAttack;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.sounds.Sound;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.util.SoundEvents;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author ConeTin
 * @since 29 мар. 2024 г.
 */

@FieldDefaults(level = AccessLevel.PRIVATE)
@Info(name="Sounds", desc="Звуки при разных действиях", type=Category.OTHER)
public class Sounds extends Module {
	
	final Select sounds = new Select(this, "Звуки");
	@Getter final Select.Element module  = new Select.Element(sounds, "Переключение модуля").set(true);
	final Select.Element hit  = new Select.Element(sounds, "Удар");
	
	final Select voice = new Select(this, "Озвучка");
	final Select.Element kill = new Select.Element(voice, "Убийство").set(true);
	@Getter final Select.Element shieldbreak = new Select.Element(voice, "Ломание щита").set(true);
	@Getter final Select.Element tooltips = new Select.Element(voice, "Подсказки").set(true);
	
	final Mode modeKill = new Mode(this, "Звук убийства").hide(() -> !kill.get());
	final Mode.Element nastya = new Mode.Element(modeKill, "Настенька");
	final Mode.Element tanks = new Mode.Element(modeKill, "Мужские");
	
	final Mode mode = new Mode(this, "Переключение модуля").hide(() -> !module.get());

	final Mode.Element simple = new Mode.Element(mode, "Простое");
	final Mode.Element click = new Mode.Element(mode, "Щелчок");
	
	@Getter
	final Slider volume = new Slider(this, "Громкость").min(0).max(1).inc(0.05f).set(1f);
	
	int killCount;
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventKill e && this.kill.get() && !(mc.currentScreen instanceof DeathScreen) && e.getTarget() != mc.player && e.getTarget() instanceof PlayerEntity) {
			playKillSound();
		}
		
		if (hit.get()) {
			if (event instanceof EventAttack e && mc.player.fallDistance > 0) {
				new Sound("weave").play();
			}
			
	        if (event instanceof EventReceivePacket e && e.getPacket() instanceof SPlaySoundEffectPacket play && (play.getSound() == SoundEvents.ENTITY_PLAYER_ATTACK_CRIT || play.getSound() == SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE && play.getSound() == SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK)) {
	        	e.cancel();
	        }
		}
	}
	
	private void playKillSound() {
	    String sound = getKillSound();
	    new Sound(sound).play();
	    if (killCount == 2) {
	        killCount = 0;
	    }
	    killCount++;
	}
	
	private boolean isNastya() {
	    return modeKill.is(nastya);
	}
	
	private String getKillSound() {
	    if (killCount == 2) {
	        return isNastya() ? "nastya/double_kill" : "kill";
	    } else {
	        int random = MathUtility.randomInt(1, 6);
	        return isNastya() ? (random == 5 ? "nastya/penit" : "nastya/kill" + random) : "kill";
	    }
	}
	
	public String getModifier() {
		if (this.mode.is(simple)) 
			return rock.getThemes().getCurrent() == rock.getThemes().getDarkTheme() ? "1" : "";
		if (this.mode.is(click)) 
			return "2";
		return "";
	}
	@NativeInclude
	@Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}
	
}
