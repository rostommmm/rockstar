/**
 * 
 */
package fun.rockstarity.api.helpers.game;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import fun.rockstarity.api.IAccess;
import lombok.experimental.UtilityClass;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;

/**
 * @author Malecharik
 * @since 25 мая 2024 г. 15:02:48
 */

@UtilityClass
public class ItemUtility implements IAccess {
	private final Pattern funTimePricePattern = Pattern.compile("\\$(\\d+(?:\\s\\d{3})*(?:\\.\\d{2})?)");
	
	// TODO я как бабушка см%@&#*$%%@ ношу костыли  
	public ListNBT[] UNSHIP = new ListNBT[4];
	public ListNBT[] SHIP = new ListNBT[4];
	
    public int getPrice(ItemStack stack) {
    	try {
        	List<ITextComponent> itemTooltip = stack.getTooltip(null, ITooltipFlag.TooltipFlags.NORMAL);
        	String sell = Server.is("spooky") ? "Цена" : Server.isHW() ? "Цена" : "Ценa";
    		for (ITextComponent str : itemTooltip) {
    			//System.out.println(str.getString());
    			if (str.getString().contains(sell)) {
    				if (Server.isFT()) {
    					return Integer.parseInt(str.getString().replace("$ Ценa $", "").replace(" ", "").replace(",", ""));
    				} else if (Server.isHW()) {
    					return Integer.parseInt(str.getString().replace("▍", "").replace("¤", "").replace("Цена", "").replace(" ", "").replace(",", "").replace(":", "").replace("шага", ""));
    				} else if (Server.isFS()) {
    					return Integer.parseInt(str.getString().replace("$", "").replace("Цена", "").replace(":", "").replace("$ Цена $", "").replace(" ", "").replace(",", ""));
    				} else {
    					return Integer.parseInt(str.getString().replace("Цена", "").replace("$", "").replace(": ", "").replace(" ", "").replace(",", "").trim());
    				}
    			}
    		}
    	} catch (NumberFormatException number) {
    		Chat.debug(number);
    		return -1;
    	}
    	return -1;
    }
    

    public boolean contains(ItemStack stack, String str1) {
    	try {
        	List<ITextComponent> itemTooltip = stack.getTooltip(null, ITooltipFlag.TooltipFlags.NORMAL);
    		for (ITextComponent str : itemTooltip) {
    			if (str.getString().contains(str1)) {
    				return true;
    			}
    		}
    	} catch (NumberFormatException number) {
    		return false;
    	}
    	return false;
    }
    
    
    public String getSeller(ItemStack stack) {
    	try {
        	List<ITextComponent> itemTooltip = stack.getTooltip(null, ITooltipFlag.TooltipFlags.NORMAL);

        	for (ITextComponent str : itemTooltip) {
				String name = str.getString();
				
				if (name.contains("⚕ Прoдaвeц: ")) {
					String seller = name.replace("⚕ Прoдaвeц: ", "");
        			if (!seller.equals(mc.player.getName().getString())) {
        				return seller;
        			}
        		}
			}
    	} catch (NumberFormatException number) {
    		Chat.debug(number);
    		return "";
    	}
    	return "";
    }
    
    public List<ItemStack> getItemsInShulker(ItemStack s) {

        CompoundNBT compoundnbt = s.getChildTag("BlockEntityTag");

        if (compoundnbt != null) {
            if (compoundnbt.contains("Items", 9)) {
                NonNullList<ItemStack> nonnulllist = NonNullList.withSize(27, ItemStack.EMPTY);
                ItemStackHelper.loadAllItems(compoundnbt, nonnulllist);
                return nonnulllist.stream()
                		.filter(item -> item.getItem() != Items.AIR)
                		//.filter(item -> item.getTag() != null && item.getTag().contains("don-item"))
                		.collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }
    
    public boolean isRare(ItemStack stack) {
    	//if (true) return true;
    	
    	return stack.getDisplayName().getString().contains("★")
    			|| stack.getDisplayName().getString().toLowerCase().contains("крушителя")
    			|| stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE
    			|| stack.getItem() == Items.SHULKER_BOX
    			|| stack.getItem() == Items.NETHERITE_INGOT;
    }
    
}
