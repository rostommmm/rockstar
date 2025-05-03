package fun.rockstarity.client.modules.combat;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.math.aura.AuraUtility;
import fun.rockstarity.api.helpers.math.aura.BotUtility;
import fun.rockstarity.api.helpers.math.aura.IdealHitUtility;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Slider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.UseAction;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CEntityActionPacket.Action;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;

@Info(name = "TriggerBot", desc = "При наводке на энтити, автоматически бьет ее", type = Category.COMBAT, module= {"TapeMouse", "AutoClicker"})
public class TriggerBot extends Module {

	private final Mode mode = new Mode(this,"Режим");
	private final Mode.Element defaults = new Mode.Element(mode, "Обычный");
	private final Mode.Element hides = new Mode.Element(mode, "Скрытный");
	
	private final CheckBox crit = new CheckBox(this, "Только криты").set(true)
			.desc("TriggerBot будет бить только тогда, когда может нанести критический удар").hide(() -> !this.mode.is(defaults));
	private final CheckBox breaks = new CheckBox(this, "Ломать щит").desc("Ломает щит противнику").hide(() -> !this.mode.is(defaults));

	private final Select targets = new Select(this, "Цели").desc("Сущности, которых будет бить TriggerBot").hide(() -> !this.mode.is(defaults));

	private final Select.Element players = new Select.Element(targets, "Игроки").set(true);
	private final Select.Element invisibles = new Select.Element(targets, "Невидимые").set(true)
			.hide(() -> !this.players.get());
	private final Select.Element bots = new Select.Element(targets, "Боты").hide(() -> !this.players.get());
	private final Select.Element mobs = new Select.Element(targets, "Мобы");
	private final Select.Element friend = new Select.Element(targets, "Друзья");

	private final Slider delay = new Slider(this, "Задержка").min(100).max(5000).inc(100).set(500).hide(() -> !this.mode.is(this.hides));
	
	private final TimerUtility timer = new TimerUtility();
	
	@Override
	public void onEvent(Event event) {
	    if (event instanceof EventMotion && mode.is(defaults)) {

	        RayTraceResult traceResult = mc.objectMouseOver;

	        if (traceResult == null || traceResult.getType() != RayTraceResult.Type.ENTITY
	                || mc.getGameSettings().keyBindUseItem.isKeyDown()) {
	            return;
	        }

	        Entity entity = ((EntityRayTraceResult) traceResult).getEntity();

	        if (!isValid(entity)) return;

	        if (crit.get() && mc.player.fallDistance < 0.1 || !this.timer.passed(470))
	            return;

	        if (mc.player.getCooledAttackStrength(0.5F) >= (crit.get() ? 0.95F : 1f)) {

	            if (mc.player.isSprinting() || mc.player.serverSprintState && Server.is("spooky")) {
	                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, Action.STOP_SPRINTING));
	                mc.player.serverSprintState = false;
	            }

	            boolean blocking = mc.player.isHandActive() && mc.player.getActiveItemStack().getItem().getUseAction(mc.player.getActiveItemStack()) == UseAction.BLOCK;
	            if (blocking) {
	                mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));
	            }

	            mc.playerController.attackEntity(mc.player, entity);
	            mc.player.swingArm(Hand.MAIN_HAND);
	            this.timer.reset();

	            if (blocking) {
	                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(mc.player.getActiveHand()));
	            }

	            if (breaks.get()) {
	                AuraUtility.tryBreakShield();
	            }
	        }
	    }

	    if (this.mode.is(this.hides) && event instanceof EventUpdate && this.timer.passed((long) this.delay.get() + 300)) {
	    	mc.setLeftClickCounter(0);
	        mc.clickMouse();
	        this.timer.reset();
	    }
	}

	private boolean isValid(Entity entity) {
	    if (entity == null || entity == mc.player) return false;

	    if (entity instanceof PlayerEntity && players.get()) {
	        if (((LivingEntity) entity).getHealth() <= 0) return false;
	    } else if ((entity instanceof MobEntity || entity instanceof AnimalEntity) && mobs.get()) {
	        if (((LivingEntity) entity).getHealth() <= 0) return false;
	    } else {
	        return false;
	    }

	    if (entity.isInvisible() && !invisibles.get()) return false;

	    if (rock.getFriendsHandler().isFriend(entity) && !friend.get()) return false;

	    if (Server.isRW() && !bots.get() && BotUtility.isRWBot((LivingEntity) entity)) return false;

	    return true;
	}

	
	@Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}
	
}