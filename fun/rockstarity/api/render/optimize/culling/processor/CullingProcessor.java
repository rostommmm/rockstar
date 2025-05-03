package fun.rockstarity.api.render.optimize.culling.processor;

import java.rmi.registry.Registry;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.render.optimize.culling.util.OcclusionCullingInstance;
import lombok.experimental.UtilityClass;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;

/**
 * @author ConeTin
 * @since 20 СЏРЅРІ. 2025вЂЇРі.
 */

@UtilityClass
public class CullingProcessor {

    public static final Logger LOGGER = LogManager.getLogger("EntityCulling");
	protected Thread cullThread;
	public CullTask cullTask;
	public OcclusionCullingInstance culling;
	private Set<Function<TileEntity, Boolean>> dynamicBlockEntityWhitelist = new HashSet<>();
    private Set<Function<Entity, Boolean>> dynamicEntityWhitelist = new HashSet<>();
    public Set<TileEntityType<?>> blockEntityWhitelist = new HashSet<>();
    public Set<EntityType<?>> entityWhistelist = new HashSet<>();
    public Set<EntityType<?>> tickCullWhistelist = new HashSet<>();
    private boolean lateInit;
    
	public void init() {
		culling = new OcclusionCullingInstance(CullingConfig.tracingDistance, new Provider());
        cullTask = new CullTask(culling, blockEntityWhitelist, entityWhistelist);
        
		cullThread = new Thread(cullTask, "CullThread");
		cullThread.setUncaughtExceptionHandler((thread, ex) -> {
			LOGGER.error("The CullingThread has crashed! Please report the following stacktrace!", ex);
		});
	}
	
	public void onEvent(Event event) {
		if (event instanceof EventUpdate) {
			if (!lateInit) {
				cullThread.start();
				lateInit = true;
			}
			
			cullTask.requestCull = true;
		}
	}

}
