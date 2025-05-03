package fun.rockstarity.client.modules.combat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventPlace;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Select.Element;
import fun.rockstarity.api.modules.settings.list.Slider;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author Malecharik
 * @since 6 мая 2024 г. 19:27:12
 */


@Info(name="AutoExplosion", desc="Автоматически ставит и взрывает якоря/кристаллы", type=Category.COMBAT)
public class AutoExplosion extends Module {
	private final Select utils = new Select(this, "Выбор");
	
	private final Element crystals = new Element(utils, "Кристалл").set(true);
	private final Element anchor = new Element(utils, "Якорь");
	
	private final CheckBox self = new CheckBox(this, "Не взрывать себя").set(true);
	private final Slider delay = new Slider(this, "Задержка").max(100f).min(5f).inc(5f).set(50f).hide(() -> !crystals.get()).desc("Задержка с которой будет ставиться кристал на обсидиан");
	
	private final List<EventPlace> blocks = new ArrayList<>();
	private final TimerUtility timer = new TimerUtility();
    private float[] rot;
	
	@Override
	public void onEvent(Event event) {
        if (event instanceof EventPlace place) {
            handleBlockPlaceEvent(place);
            return;
        }
		
		if (event instanceof EventMotion e) {
			handleMotionEvent(e);
		}
		
		if (rot != null) Player.look(event, rot[0], rot[1], false);
	}
	@NativeInclude
    private void handleBlockPlaceEvent(EventPlace place) {
        if (isBlockValid(place.getBlock(), place)) {
            blocks.add(place);
        }
    }
	
    private boolean isBlockValid(Block block, EventPlace place) {
    	if (block == Blocks.OBSIDIAN && this.self.get() && mc.player.getPosY() > place.getPos().getY()) return false;
        return block == Blocks.OBSIDIAN || block == Blocks.BEDROCK || (anchor.isEnabled() && block == Blocks.RESPAWN_ANCHOR);
    }
    
    private void handleMotionEvent(EventMotion e) {
        if (!blocks.isEmpty()) {
            EventPlace block = blocks.get(0);
            rot = MathUtility.getRotVec(mc.player.getPositionVec(), new Vector3d(block.getPos().getX(), block.getPos().getY(), block.getPos().getZ()));
            
            int slot = Player.findItem(9, block.getBlock() == Blocks.RESPAWN_ANCHOR ? Items.GLOWSTONE : Items.END_CRYSTAL);
            if (slot!= -1 && timer.passed(delay.get() * 9)) {
                placeAndBreakBlock(block, slot);
                rot = null;
                timer.reset();
            }
        }
        
        attackNearbyCrystals(e);
    }
    
    private void placeAndBreakBlock(EventPlace block, int slot) {
        if (mc.world.getBlockState(block.getPos()).getBlock()!= Blocks.AIR) {
            mc.player.connection.sendPacket(new CHeldItemChangePacket(slot - 36));
            Direction facing = Direction.UP;
            mc.playerController.func_217292_a(mc.player, mc.world, Hand.MAIN_HAND, new BlockRayTraceResult(block.getPos().getVec().add(0.5, 0.5, 0.5), facing, block.getPos(), false));
            mc.player.swingArm(Hand.MAIN_HAND);
            mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
        }
        
        if (mc.world.getBlockState(block.getPos()).getBlock()!= Blocks.RESPAWN_ANCHOR) {
            blocks.clear();
        }
    }
    
    private void attackNearbyCrystals(EventMotion e) {
        for (Entity ent : mc.world.getAllEntities()) {
            if (ent instanceof EnderCrystalEntity && mc.player.getDistance(ent) < 5) {
                if (mc.player.getCooledAttackStrength(0) > 0.5) {
                    mc.playerController.attackEntity(mc.player, ent);
                    mc.player.swingArm(Hand.MAIN_HAND);
                    rot = null;
                }
            }
        }
    }
    
    @Override
    public void onDisable() {
    	blocks.clear();
    	rot = null;
    }
    
    @Override
    public void onEnable() {
    	timer.reset();
    }
}