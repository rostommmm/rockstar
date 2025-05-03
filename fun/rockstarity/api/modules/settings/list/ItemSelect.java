package fun.rockstarity.api.modules.settings.list;

import java.util.ArrayList;
import java.util.function.Supplier;

import fun.rockstarity.api.autobuy.logic.items.MinecraftItem;
import fun.rockstarity.api.binds.Bindable;
import fun.rockstarity.api.modules.settings.Setting;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.Item;

public class ItemSelect extends Setting {
	
	@Getter @Setter
	private ArrayList<Item> elements = new ArrayList<>();
	
	@Getter @Setter
	private ArrayList<MinecraftItem> items = new ArrayList<>();
	private Item current;
	@Getter
	private boolean onlyBlocks;
	
	public ItemSelect(Bindable parent, String name) {
		super(parent, name);
	}
	
	public ItemSelect set(Item elmt) {
		this.current = elmt;
		return this;
	}
	
	public ItemSelect set(String elmt) {
		for (Item elmta : elements) {
			if (elmta.getName().getString().equals(elmt)) {
				this.current = elmta;
			}
		}
		return this;
	}
	
	public Item getMode() {
		return current;
	}
	
	public boolean is(Item elmt) {
		return current == elmt;
	}
	
	public ItemSelect add(Item elmt) {
		elements.add(elmt);
		if (elements.size() == 1 && current == null) current = elmt;
		return this;
	}
	
	public ItemSelect onlyBlocks(boolean onlyBlocks) {
		this.onlyBlocks = onlyBlocks;
		return this;
	}
	
	public ItemSelect addCurrent(Item elmt) {
		elements.add(elmt);
		this.current = elmt;
		return this;
	}
	
	public ItemSelect hide(Supplier<Boolean> hide) {
		this.hide = hide;
		return this;
	}
	
	public ItemSelect desc(String desc) {
		this.desc = desc;
		return this;
	}
}
