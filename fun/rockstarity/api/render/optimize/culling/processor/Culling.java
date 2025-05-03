package fun.rockstarity.api.render.optimize.culling.processor;

import lombok.Getter;
import lombok.Setter;

/**
 * @author ConeTin
 * @since 20 янв. 2025 г.
 */

public class Culling {
	private long lasttime = 0;
	@Getter
    private boolean culled;
	@Getter @Setter
    private boolean prevCulled;
	@Getter @Setter
    private boolean outOfCamera;

    public void setTimeout() {
        lasttime = System.currentTimeMillis() + 1000;
    }

    public boolean isForcedVisible() {
        return lasttime > System.currentTimeMillis();
    }

    public void setCulled(boolean value) {
    	if (culled)
        	prevCulled = true;
    	
        this.culled = value;
        if (!value) {
            setTimeout();
        }
        
        if (!value)
        	prevCulled = false;
    }
}
