package fun.rockstarity.client.modules.render;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import fun.rockstarity.api.autobuy.logic.items.MinecraftItem;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.events.list.render.world.EventRenderWorld;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.ItemSelect;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.render.color.themes.Style;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

@Info(name = "XRay", desc = "Подсвечивает выбранные блоки", type = Category.RENDER)
public class XRay extends Module {
	
	@Getter private final ItemSelect itemSelect = new ItemSelect(this, "Блоки").onlyBlocks(true);
	
	private final Slider radius = new Slider(this, "Радиус").min(5).max(50).inc(1).set(15);
	
	@Getter private final Set<BlockPos> cachedBlocks = new HashSet<>();
	private final Queue<BlockPos> scanQueue = new LinkedList<>();
	private BlockPos lastCenter = BlockPos.ZERO;
	
	@Override
	public void onEvent(Event event) {
	    if (event instanceof EventUpdate) {
	        Iterator<BlockPos> it = cachedBlocks.iterator();
	        while (it.hasNext()) {
	            BlockPos pos = it.next();
	            BlockState state = mc.world.getBlockState(pos);
	            Item item = state.getBlock().asItem();
	            boolean keep = false;
	            for (MinecraftItem sel : itemSelect.getItems()) {
	                if (sel.getItem() == item) {
	                    keep = true;
	                    break;
	                }
	            }
	            if (!keep) {
	                it.remove();
	            }
	        }

	        int scansPerTick = 800;
	        for (int i = 0; i < scansPerTick && !scanQueue.isEmpty(); i++) {
	            BlockPos pos = scanQueue.poll();
	            if (cachedBlocks.contains(pos)) continue;

	            BlockState state = mc.world.getBlockState(pos);
	            Block block = state.getBlock();
	            Item item = block.asItem();

	            if (item == Items.AIR) continue;

	            for (MinecraftItem selected : itemSelect.getItems()) {
	                if (selected.getItem() == item) {
	                    cachedBlocks.add(pos);
	                    break;
	                }
	            }
	        }

	        if (mc.player.getPosition().distanceSq(lastCenter) > 16) {
	            lastCenter = mc.player.getPosition();
	            scanQueue.clear();
	            prepareScanQueue();
	        }
	    }

	    if (event instanceof EventRenderWorld) {
	        Vector3d renderPos = mc.getRenderManager().info.getProjectedView();

	        GL11.glPushMatrix();
	        GL11.glTranslated(-renderPos.x, -renderPos.y, -renderPos.z);

	        for (BlockPos pos : cachedBlocks) {
	            Render.blockEsp(pos, Style.getMain().getRGB());
	        }

	        GL11.glPopMatrix();
	    }
	}
	
	@Override
	public void onDisable() {
	}
	
	@Override
	public void onEnable() {
	    cachedBlocks.clear();
	    scanQueue.clear();
	    lastCenter = mc.player.getPosition();
	    prepareScanQueue();
	}
	
	private void prepareScanQueue() {
	    int r = (int) radius.get();
	    BlockPos center = lastCenter;

	    for (int x = -r; x <= r; x++) {
	        for (int y = -r; y <= r; y++) {
	            for (int z = -r; z <= r; z++) {
	                BlockPos pos = center.add(x, y, z);
	                scanQueue.add(pos);
	            }
	        }
	    }
	}
}
