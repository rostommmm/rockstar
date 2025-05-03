package fun.rockstarity.client.commands;

import java.util.ArrayList;
import java.util.List;

import fun.rockstarity.api.commands.CmdInfo;
import fun.rockstarity.api.commands.Command;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventPickupItem;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.client.commands.invsee.PlayerData;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

@NativeInclude
@CmdInfo(names={ "invsee", "see" }, desc="Позволяет посмотреть содержимое инвентаря игроков")
public class InvseeCommand extends Command {
	
	private final List<PlayerData> players = new ArrayList<>();
	private PlayerEntity ent;
	
	@Override
	public void execute(String[] args) {
		for (PlayerEntity player : mc.world.getPlayers()) {
			if (!args[0].equals(player.getName().getString())) continue;
			
			ent = player;
			break;
		}
	}
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventUpdate && ent != null) {
			InventoryScreen screen = new InventoryScreen(ent);
			
	        int containerSize = screen.getContainer().getInventory().size();

	        int inventoryStart = 0;

	        ItemStack goldenApple = new ItemStack(Items.GOLDEN_APPLE); 
	        for (int i = 44; i >= 9; i--) {
	        	screen.getContainer().putStackInSlot(i, new ItemStack(Items.AIR));
	        }
	        
			PlayerData data = get(ent);
			if (data != null) {
				int count = 0;
				for (int i = 44; i >= 9; i--) {
		        	screen.getContainer().putStackInSlot(i, (ItemStack) data.getInventory().values().toArray()[count]);
					
					count++;
					if (count >= data.getInventory().size()) break;
		        }
			}
	        
			mc.displayGuiScreen(screen);

			ent = null;
		}
		
		if (event instanceof EventUpdate) {
			for (PlayerEntity player : mc.world.getPlayers()) {
				if (contains(player)) {
					PlayerData data = get(player);
					
					data.getInventory().put(player.getHeldItemMainhand().getItem(), player.getHeldItemMainhand());
				} else {
					players.add(new PlayerData(player));
				}
			}
		}
		
		if (event instanceof EventPickupItem e && e.getLivingEntity() instanceof PlayerEntity player && player != mc.player) {
			PlayerData data = get(player);
			
			if (data != null) {
				if (data.getInventory().containsKey(e.getItemStack().getItem())) {
					int count = data.getInventory().get(e.getItemStack().getItem()).getCount() + e.getItemStack().getCount();
					e.getItemStack().setCount(count);
					data.getInventory().put(e.getItemStack().getItem(), e.getItemStack());
				} else {
					data.getInventory().put(e.getItemStack().getItem(), e.getItemStack());
				}
			}
		}
	}
	
	private PlayerData get(PlayerEntity player) {
		for (PlayerData data : players) {
			if (data.getPlayer() == player)
				return data;
		}
		
		return null;
	}
	
	private boolean contains(PlayerEntity player) {
		return get(player) != null;
	}
	
}
