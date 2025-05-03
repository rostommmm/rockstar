package fun.rockstarity.client.modules.move;

import java.util.ArrayList;
import java.util.Comparator;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventPickupItem;
import fun.rockstarity.api.events.list.game.EventTick;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.game.ItemUtility;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.math.aura.Rotation;
import fun.rockstarity.api.helpers.player.Inventory;
import fun.rockstarity.api.helpers.player.Move;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.secure.Debugger;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author ConeTin
 * @since 10 авг. 2024 г.
 */

@FieldDefaults(level = AccessLevel.PRIVATE)
@Info(name = "AutoLoot", desc = "Автоматически лутает вещи", type = Category.MOVE)
public class AutoLoot extends Module {
	
	ElytraTask task;
	Vector3d packetPos;

	@Override
	public void onEvent(Event event) {
		if (event instanceof EventPickupItem e) {
			if (e.getLivingEntity() != mc.player) return;
			
			if (task != null) {
				Chat.msg("Залутал " + e.getItemStack().getDisplayName().getString() + " за " + e.getItemEntity().ticksExisted + " тиков");
				mc.player.sendChatMessage("/god enable");
				task.loot = true;
			}
		}
		
		if (event instanceof EventUpdate) {
			boolean elytra = Player.find(38).getItem() == Items.ELYTRA;
			if (!elytra) {
				Chat.msg("Элитра не надета. Не будем лутать вещи.");
				this.set(false);
			} else {
				Entity target = findTarget();

				if (target != null && task == null) {
					task = new ElytraTask(target.getPositionVec(), mc.player.getPositionVec(), target);
					packetPos = mc.player.getPositionVec();
					mc.player.sendChatMessage("/god disable");
				}
				
				if (task != null) {
					Vector3d pos = mc.player.getPositionVec();
					
					if (task.loot) {
						while (true) {
							Vector3d newPos = new Vector3d(MathUtility.step((float)packetPos.x, (float) task.from.x, 10), MathUtility.step((float)packetPos.y, (float) task.from.y, 10), MathUtility.step((float)packetPos.z, (float) task.from.z, 10));
							mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(newPos.x, newPos.y, newPos.z, false));
							packetPos = newPos;
							
							if (packetPos.distanceTo(task.from) < 0.1f) {
								mc.player.setPosition(packetPos);
								break;
							}
						}
						
						if (mc.player.getPositionVec().distanceTo(task.from) < 1f) {
							task = null;
						}
					} else {
						while (true) {
							Vector3d newPos = new Vector3d(MathUtility.step((float)packetPos.x, (float) task.target.getPositionVec().x, 10), MathUtility.step((float)packetPos.y, (float) task.target.getPositionVec().y, 10), MathUtility.step((float)packetPos.z, (float) task.target.getPositionVec().z, 10));
							mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(newPos.x, newPos.y, newPos.z, false));
							packetPos = newPos;
							
							if (packetPos.distanceTo(task.target.getPositionVec()) < 0.1f) {
								mc.player.setPosition(packetPos);
								break;
							}
						}
					}
					
					if (!mc.world.getAllEntities().contains(task.target)) {
						task.loot = true;
						mc.player.sendChatMessage("/god enable");
					}
				}
			}
		}
	}
	
	@NativeInclude
	private Entity findTarget() {
		ArrayList<Entity> targets = new ArrayList<>();
		
		for (Entity entity : mc.world.getAllEntities()) {
			if (entity instanceof ItemEntity item && MathUtility.canSeen(item.getPositionVec()) && ItemUtility.isRare(item.getItem()) && item.ticksExisted > 37) {
				targets.add(item);
			}
		}
		
		targets.sort(Comparator.comparingDouble(mc.player::getDistance));
		return targets.size() == 0 ? null : targets.get(0);
	}
	
	@NativeInclude
	@Override
	public void onEnable() {
		boolean elytra = Player.find(38).getItem() == Items.ELYTRA;
		if (!elytra) {
			Chat.msg("Элитра не надета. Не будем лутать вещи.");
			set(false);
		}
		
		mc.player.sendChatMessage("/god enable");
		mc.player.sendChatMessage("/fly enable");
		packetPos = null;
	}
	
	@Override
	@NativeInclude
	public void onDisable() {
		task = null;
	}
	
	@RequiredArgsConstructor
	class ElytraTask {
		final Vector3d to;
		final Vector3d from;
		final Entity target;
		boolean loot;
	}
	
}
