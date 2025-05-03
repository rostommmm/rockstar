package fun.rockstarity.client.modules.other;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.mojang.datafixers.util.Pair;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventTick;
import fun.rockstarity.api.events.list.game.packet.EventSendPacket;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.player.Bypass;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.PremiumModule;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Slider;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author ConeTin
 * @since 17 авг. 2024 г.
 */

@Info(name = "AUTOFTDUPE", desc = "СОСАЛ ДА СОСАЛ", type = Category.OTHER)
public class AUTOFTDUPE extends PremiumModule {

	private final CheckBox dropTrident = new CheckBox(this, "Выбрасывать трезубец");
	final Slider delay = new Slider(this, "Задержка").min(0).max(10).inc(0.1f).set(5);

    private int delayCounter = 0;
    private int bestSlot = -1;
    private boolean cancel;
    
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    ScheduledExecutorService scheduler2 = Executors.newScheduledThreadPool(1);

    
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventSendPacket e) {
			if (e.getPacket() instanceof CPlayerPacket
					 || e.getPacket() instanceof CCloseWindowPacket)
		        return;

		    if (!(e.getPacket() instanceof CClickWindowPacket)
		        && !(e.getPacket() instanceof CPlayerDiggingPacket))
		    {
		        return;
		    }
		    
		    if (!cancel) return;

		    // Для дебага (необязательно)
		    ITextComponent packetStr = new StringTextComponent(e.getPacket().toString())
		        .mergeStyle(TextFormatting.WHITE);
		    
		    System.out.println(packetStr.getString());
		    
		    event.cancel();
		}
		
	}
	
	private void findBestTrident() {
        bestSlot = -1;
        int bestDurability = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() == Items.TRIDENT) {
                int durability = stack.getMaxDamage() - stack.getDamage();
                if (durability > bestDurability) {
                    bestDurability = durability;
                    bestSlot = i;
                }
            }
        }

        if (bestSlot == -1) {
            Chat.debug("No trident found in hotbar!");
            toggle();
        }
    }

	@NativeInclude
    private void dupe()
    {
        int delayInt = (int) (delay.get())*100;

        int lowestHotbarSlot = 0;
        int lowestHotbarDamage = 1000;
        for (int i = 0; i < 9; i++)
        {
            if (mc.player.inventory.getStackInSlot((i)).getItem() == Items.TRIDENT)
            {
                Integer currentHotbarDamage = mc.player.inventory.getStackInSlot((i)).getDamage();
                if(lowestHotbarDamage > currentHotbarDamage) { lowestHotbarSlot = i; lowestHotbarDamage = currentHotbarDamage;}

            }
        }

        mc.playerController.processRightClick(mc.player, mc.world, Hand.MAIN_HAND);
        cancel = true;

        int finalLowestHotbarSlot = lowestHotbarSlot;
        scheduler.schedule(() -> {
        	cancel = false;
        	
        	if (!get()) return;
        	
            mc.playerController.windowClick(0, 45, 0, ClickType.SWAP, mc.player);

            mc.player.connection.sendPacketSilent(new CPlayerDiggingPacket(
            		CPlayerDiggingPacket.Action.RELEASE_USE_ITEM,
                    BlockPos.ZERO,
                    Direction.DOWN
                ));

            if(dropTrident.get()) mc.playerController.windowClick(0, 45, 0, ClickType.THROW, mc.player);

            cancel = true;
            
            scheduler.schedule(this::dupe, delayInt, TimeUnit.MILLISECONDS);            
        }, delayInt, TimeUnit.MILLISECONDS);
    }

	@NativeInclude
	@Override
	public void onEnable() {
		if (mc.player == null)
            return;
		for (int i = 0; i < 9; i++)
        {
            if (mc.player.inventory.getStackInSlot((i)).getItem() == Items.TRIDENT)
            {
                Integer currentHotbarDamage = mc.player.inventory.getStackInSlot((i)).getDamage();

            }
        }

        mc.player.connection.sendPacketSilent(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));

        Int2ObjectMap<ItemStack> modifiedStacks = new Int2ObjectOpenHashMap<>();

        modifiedStacks.put(3,  mc.player.inventory.getStackInSlot(mc.player.inventory.currentItem));
        modifiedStacks.put(36,  mc.player.inventory.getStackInSlot(mc.player.inventory.currentItem));

        
        dupe();
	}
	
	@Override
	public void onDisable() {
	}

}
