package fun.rockstarity.api.helpers.math;

import java.util.HashMap;
import java.util.Map;

import com.mojang.datafixers.util.Pair;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.game.Server;
import lombok.experimental.UtilityClass;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Score;
import net.minecraft.util.text.ITextComponent;

/**
 * @author ConeTin
 * @since 6 окт. 2024 г.
 */

@UtilityClass
public class EntityHealthHelper implements IAccess {
	
	public float getHealth(LivingEntity entity) {
		if (Server.isServerForHPFix()) {
			return entity.getRealHealth();
		}
		
		return entity.getHealth();
	}
	
	public float getMaxHealth(LivingEntity entity) {
		return Server.isServerForHPFix() ? 20 : entity.getMaxHealth();
	}
	
	public float getGoldenHealth(LivingEntity entity) {
		return Server.isFT() ? 0 : entity.getAbsorptionAmount();
	}
	
	public void handle(Pair<Score, ITextComponent> pair) {
        if (!Server.isRW()) return;
		
        // TODO доделать
        /*
        for (PlayerEntity entity : mc.world.getPlayers()) {
        	String name = entity.getName().getString();
            String text = pair.getSecond().getString();

            if (text.contains(name)) {
            	int health = Integer.parseInt(text);
            	
            	if (health <= 20) {
            		entity.setRealHealth(health);
            	}
            }
        }
        */
	}
	
}
