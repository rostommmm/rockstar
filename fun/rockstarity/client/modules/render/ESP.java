package fun.rockstarity.client.modules.render;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.EventType;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.events.list.render.ui.EventRenderPreUI;
import fun.rockstarity.api.helpers.render.PositionTracker;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.render.ui.clickgui.esp.ESPElement;
import fun.rockstarity.api.render.ui.clickgui.esp.ESPEventable;
import fun.rockstarity.api.render.ui.clickgui.esp.elmts.ESPBoxes;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector4f;

/**
 * @author ConeTin
 * @since 15 мар. 2024 г.
 */


@Info(name="ESP", desc="Информация о игроках через стены", type=Category.RENDER)
public class ESP extends Module {
	
	private final HashMap<Entity, Vector4f> positions = new HashMap<>();
	
	@Override
	@EventType({EventRenderPreUI.class})
	public void onEvent(Event event) {
		if (event instanceof EventRenderPreUI e) {
			this.collect(e);
			this.render(e);
		}
		
		try {
			for (ESPElement elmt : rock.getEspSettingsHandler().getEspElements()) {
				if (elmt instanceof ESPEventable eventable && elmt.isActive())
					eventable.onEvent(event);
			}
		} catch (Exception e) {
		}
	}
	
	private void render(EventRenderPreUI e) {
		MatrixStack ms = e.getMatrixStack();
		NameTags nameTags = rock.getModules().get(NameTags.class);
		
		for (Map.Entry<Entity, Vector4f> entry : positions.entrySet()) {
            Vector4f position = entry.getValue();
            
            if (entry.getKey() instanceof LivingEntity entity && !entity.getDeathAnim().finished(false)) {
            	float x = position.x;
            	float y = position.y;
            	float width = position.z - position.x;
            	float height = position.w - position.y;
            	
            	float anim = entity.getDeathAnim().get();
            	
            	for (ESPElement elmt : rock.getEspSettingsHandler().getEspElements()) {
            		if (!entity.getUniqueID().equals(PlayerEntity.getOfflineUUID(entity.getName().getString()))) continue;
            		
            		if (!elmt.getActiveAnim().finished(false)) {
            			Vector2f tag = nameTags.getPlayerTags().get(entity);
            			if (elmt.getDirection() == 0 && tag != null && rock.getModules().get(NameTags.class).get() && !(elmt instanceof ESPBoxes)) {
            				elmt.drawOnEntity(ms, entity, tag.x - width/2, tag.y, width, height, anim);
            			} else {
            				elmt.drawOnEntity(ms, entity, x, y, width, height, anim);
            			}
            		}
            	}
            }
		}
	}
	
	private boolean isAir(ItemStack stack) {
		return stack.getItem() == Items.AIR;
	}
	
	private void collect(EventRenderPreUI e) {
		positions.clear();
		for (Entity entity : mc.world.getAllEntities()) {
            if (!PositionTracker.isInView(entity)) continue;
            if (!(entity instanceof PlayerEntity || entity instanceof ItemEntity)) continue;
            if (entity == mc.player && (mc.getGameSettings().getPointOfView() == PointOfView.FIRST_PERSON)) continue;
            
            double x = entity.lastTickPosX + (entity.getPosX() - entity.lastTickPosX) * (double) e.getPartialTicks();
            double y = entity.lastTickPosY + (entity.getPosY() - entity.lastTickPosY) * (double) e.getPartialTicks();
            double z = entity.lastTickPosZ + (entity.getPosZ() - entity.lastTickPosZ) * (double) e.getPartialTicks();

            Vector3d size = new Vector3d(entity.getBoundingBox().maxX - entity.getBoundingBox().minX, entity.getBoundingBox().maxY - entity.getBoundingBox().minY, entity.getBoundingBox().maxZ - entity.getBoundingBox().minZ);

            AxisAlignedBB aabb = new AxisAlignedBB(x - size.x / 2f, y, z - size.z / 2f, x + size.x / 2f, y + size.y, z + size.z / 2f);

            Vector4f position = null;

            for (int i = 0; i < 8; i++) {
                Vector2f vector = Render.projectf(i % 2 == 0 ? aabb.minX : aabb.maxX, (i / 2) % 2 == 0 ? aabb.minY : aabb.maxY, (i / 4) % 2 == 0 ? aabb.minZ : aabb.maxZ);

                if (position == null) {
                    position = new Vector4f(vector.x, vector.y, 1, 1.0f);
                } else {
                    position.x = Math.min(vector.x, position.x);
                    position.y = Math.min(vector.y, position.y);
                    position.z = Math.max(vector.x, position.z);
                    position.w = Math.max(vector.y, position.w);
                }
            }

            positions.put(entity, position);
        }
	}
	
	@Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}
	
}
