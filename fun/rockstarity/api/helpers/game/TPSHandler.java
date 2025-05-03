package fun.rockstarity.api.helpers.game;

import java.util.Arrays;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import net.minecraft.network.play.server.SUpdateTimePacket;
import net.minecraft.util.math.MathHelper;

/**
 * @author ConeTin
 * @since 9 Р°РїСЂ. 2024 Рі.
 */

public class TPSHandler {
	
	private static float[] tickRates = new float[20];
    private int nextIndex = 0;
    private long timeLastTimeUpdate;
    
    public TPSHandler() {
        this.nextIndex = 0;
        this.timeLastTimeUpdate = -1L;
        Arrays.fill(tickRates, 0.0F);
    }
    
	public float getTPS() {
        float numTicks = 0.0F, sumTickRates = 0.0F;
        for (float tickRate : tickRates) {
            if (tickRate > 0.0F) {
                sumTickRates += tickRate;
                numTicks++;
            }
        }
        return MathHelper.clamp(sumTickRates / numTicks, 0.0F, 20.0F);
    }
	
	private void onTimeUpdate() {
	    if (this.timeLastTimeUpdate != -1L) {
	        float timeElapsed = (float)(System.nanoTime() - this.timeLastTimeUpdate) / 1_000_000_000.0F;
	        tickRates[this.nextIndex % tickRates.length] = MathHelper.clamp(20.0F / timeElapsed, 0.0F, 20.0F);
	        this.nextIndex++;
	    }
	    this.timeLastTimeUpdate = System.nanoTime();
	}

	public void onEvent(Event e) {
		if (e instanceof EventReceivePacket) {
			EventReceivePacket event = (EventReceivePacket) e;
			if (event.getPacket() instanceof SUpdateTimePacket)
	            onTimeUpdate();
		}
	}
	
}	
