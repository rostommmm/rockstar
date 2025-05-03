package fun.rockstarity.client.modules.player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Select.Element;
import lombok.Getter;

/**
 * @author Malecharik
 * @since 15 Mar 2024 18:04:26
 */


@Getter
@Info(name="NoInteract", desc="Позволяет не нажимать ПКМ по печкам, дверям и т.д", type=Category.PLAYER)
public class NoInteract extends Module {
	
	private final CheckBox all = new CheckBox(this, "Все блоки");
	
	private final Select utils = new Select(this, "Убирать..").hide(() -> all.get()).desc("Убрать взаимодействие с объектами из списка");
	
	private final Element chest = new Element(utils, "Сундуки").set(true);
	private final Element doors = new Element(utils, "Двери").set(true);
	private final Element buttons = new Element(utils, "Кнопки");
	private final Element craftingTable = new Element(utils, "Верстак").set(true);
	private final Element trapDoor = new Element(utils, "Люки").set(true);
	private final Element lever = new Element(utils, "Рычаг");
	
    public Set<Integer> getBlocks() {
        HashSet<Integer> blocks = new HashSet<Integer>();
        this.addBlocksForInteractionType(blocks, 0, 147, 329, 270);
        this.addBlocksForInteractionType(blocks, 1, 173, 161, 485, 486, 487, 488, 489, 720, 721);
        this.addBlocksForInteractionType(blocks, 3, 151);
        this.addBlocksForInteractionType(blocks, 2, 183, 308, 309, 310, 311, 312, 313, 718, 719, 758);
        this.addBlocksForInteractionType(blocks, 4, 222, 223, 224, 225, 226, 227, 712, 713, 379);
        this.addBlocksForInteractionType(blocks, 5, 171);
        return blocks;
    }
    
    private void addBlocksForInteractionType(Set<Integer> blocks, int interactionType, Integer... blockIds) {
    	if (this.utils.getElements().stream().toList().get(interactionType).get()) 
    		blocks.addAll(Arrays.asList(blockIds));
    	
    }
    
    @Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}

	@Override
	public void onEvent(Event event) {
		
	}
	
}
