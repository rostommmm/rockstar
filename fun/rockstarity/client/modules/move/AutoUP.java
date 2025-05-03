package fun.rockstarity.client.modules.move;

import java.util.ArrayList;
import java.util.Comparator;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.secure.Debugger;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author ConeTin
 * @since 10 авг. 2024 г.
 */

@FieldDefaults(level = AccessLevel.PRIVATE)
@Info(name = "AutoUP", desc = "Подхватывает людей удочкой на /fly", type = Category.MOVE)
public class AutoUP extends Module {
	
	Vector3d packetPos;
	LivingEntity target;
	TimerUtility timer = new TimerUtility();
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventUpdate e) {
			boolean elytra = Player.find(38).getItem() == Items.ELYTRA;
			if (!elytra) {
				Chat.msg("Элитра не надета. Не будем лутать вещи.");
				this.set(false);
			} else {
				if (target == null || !mc.world.getPlayers().contains(target)) {
					target = findTarget();
				}
				
				boolean containsBobber = false;


				for (Entity entity : mc.world.getAllEntities()) {
					if (entity instanceof FishingBobberEntity bob) {
						containsBobber = true;
					}
				}
				
				if (target != null && mc.player.getHeldItemMainhand().getItem() instanceof FishingRodItem) {
					//e.setPitch(90);
					
					Vector3d pos = containsBobber ? target.getPositionVec().add(0, 25, 0) : target.getPositionVec().add(0, 1, 0);

					Debugger.overlay(target.getDisplayName().getString() + " - "+ containsBobber);
					
					if (timer.passed(300) && mc.player.getDistance(pos) < 1.1f) {
						mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
				        mc.player.swingArm(Hand.MAIN_HAND);
						timer.reset();
					}
					mc.player.setPosition(MathUtility.step((float)mc.player.getPositionVec().x, (float) pos.x, 9), MathUtility.step((float)mc.player.getPositionVec().y, (float) pos.y, 9), MathUtility.step((float)mc.player.getPositionVec().z, (float) pos.z, 9));
				}
			}
		}
	}
	
	@NativeInclude
	private LivingEntity findTarget() {
		ArrayList<LivingEntity> targets = new ArrayList<>();
		
		for (Entity entity : mc.world.getAllEntities()) {
			if (entity instanceof LivingEntity e && !(e instanceof ClientPlayerEntity) && MathUtility.canSeen(entity.getPositionVec()))
				targets.add(e);
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
		
		mc.player.sendChatMessage("/fly enable");
		packetPos = null;
	}
	
	@Override
	@NativeInclude
	public void onDisable() {
		target = null;
	}
	
}
