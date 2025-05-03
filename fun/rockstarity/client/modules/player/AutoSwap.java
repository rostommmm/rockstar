package fun.rockstarity.client.modules.player;


import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.inputs.EventKey;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Binding;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Select.Element;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import fun.rockstarity.api.secure.Debugger;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.TranslationTextComponent;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Info(name = "AutoSwap", desc = "Автоматически свапает выбранный предмет", type = Category.PLAYER)
public class AutoSwap extends Module {
	
	Select mode = new Select(this, "Режим");
	Element gappleShield = new Element(mode, "Геплы/Щит");
	Element ballGapple = new Element(mode, "Шар/Геплы");
	Element sferaTal = new Element(mode, "Сфера/Талик");
	Element sferasfera = new Element(mode, "Сфера/Сфера");
	Element taltal = new Element(mode, "Талик/Талик");
	
	CheckBox autoSwap = new CheckBox(this, "Автоматический").desc("Сам свапает шары по ситуации");

	Binding gappleShieldBind = new Binding(this, "Кнопка геплы/щит").hide(() -> !gappleShield.get() || autoSwap.get());
	Binding ballGappleBind = new Binding(this, "Кнопка шар/геплы").hide(() -> !ballGapple.get() || autoSwap.get());
	Binding sferaTalBind = new Binding(this, "Кнопка сфера/талик").hide(() -> !sferaTal.get() || autoSwap.get());
	Binding sferasferaBind = new Binding(this, "Кнопка сфера/сфера").hide(() -> !sferasfera.get() || autoSwap.get());
	Binding taltalBind = new Binding(this, "Кнопка талик/талик").hide(() -> !taltal.get() || autoSwap.get());
	
	@NonFinal boolean swapHeal;
	TimerUtility safelyTimer = new TimerUtility();
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventKey key && mc.currentScreen == null && !autoSwap.get()) {
			handleKey(key);
		}
		
		if (event instanceof EventUpdate && autoSwap.get()) {
			if (mc.player.hurtTime > 0) {
				safelyTimer.reset();
			}
			
			boolean heal = !safelyTimer.passed(1000);

			if (heal != swapHeal && !(mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING && !mc.player.getHeldItemOffhand().isEnchanted())) {
				if (gappleShield.get() && findItem(Items.GOLDEN_APPLE) && findItem(Items.SHIELD)) {
					move(Player.findItem(46, Items.GOLDEN_APPLE), Player.findItem(46, Items.SHIELD));
				}
				
				if (ballGapple.get() && findItem(Items.PLAYER_HEAD) && findItem(Items.GOLDEN_APPLE)) {
					move(Player.findItem(46, Items.PLAYER_HEAD), Player.findItem(46, Items.GOLDEN_APPLE));
				}
				
				if (sferaTal.get() ) {
					swapSferaTal();
				}
				
				if (sferasfera.get()) {
					swapSferaSfera();
				}
				
				if (taltal.get()) {
					swapTalTal();
				}
			}
			
			swapHeal = heal;
		}
	}
	
	private void handleKey(EventKey e) {
		if (!e.isReleased()) {
			if (gappleShield.get() && gappleShieldBind.getBindByKey(e).isPresent() && findItem(Items.GOLDEN_APPLE) && findItem(Items.SHIELD)) {
				move(Player.findItem(46, Items.GOLDEN_APPLE), Player.findItem(46, Items.SHIELD));
			}
			
			if (ballGapple.get() && ballGappleBind.getBindByKey(e).isPresent() && findItem(Items.PLAYER_HEAD) && findItem(Items.GOLDEN_APPLE)) {
				move(Player.findItem(46, Items.PLAYER_HEAD), Player.findItem(46, Items.GOLDEN_APPLE));
			}
			
			if (sferaTal.get() && sferaTalBind.getBindByKey(e).isPresent() && !sferaTalBind.isHide()) {
				swapSferaTal();
			}
			
			if (sferasfera.get() && sferasferaBind.getBindByKey(e).isPresent() && !sferasferaBind.isHide()) {
				swapSferaSfera();
			}
			
			if (taltal.get() && taltalBind.getBindByKey(e).isPresent() && !taltalBind.isHide()) {
				swapTalTal();
			}
		}
	}
	
	private boolean swapSferaTal() {
		int findSfer = Player.findItemDefault(45, Items.PLAYER_HEAD);
		int findTal = findEnchantedTotem();
		
		findSfer = findSfer == 40 ? 45 : findSfer < 9 ? 36 + findSfer : findSfer;
		findTal = findTal == 40 ? 45 : findTal < 9 ? 36 + findTal : findTal;
		
		if (findSfer != 45 && findTal != 45) {
			findSfer = 45;
		}
		
		if (findSfer != -1 && findTal != -1) {
			move(findSfer, findTal);
			if (mc.player.getHeldItemOffhand().getItem() != Items.AIR)
				rock.getAlertHandler().alert("Свапнул " + mc.player.getHeldItemOffhand().getDisplayName().getString(), AlertType.INFO);
				//Player.overlay(mc.player.getHeldItemOffhand().getDisplayName());
			return true;
		}
		
		return false;
	}
	
	private boolean swapSferaSfera() {
		int findSfer1 = findHead(0);
		int findSfer2 = findHead(findSfer1 + 1);
		
		findSfer1 = findSfer1 == 40 ? 45 : findSfer1 < 9 ? 36 + findSfer1 : findSfer1;
		findSfer2 = findSfer2 == 40 ? 45 : findSfer2 < 9 ? 36 + findSfer2 : findSfer2;

		if (findSfer1 != 45 && findSfer2 != 45) {
			findSfer1 = 45;
		}
		
		if (findSfer1 != -1 && findSfer2 != -1) {
			move(findSfer1, findSfer2);
			if (mc.player.getHeldItemOffhand().getItem() != Items.AIR)
				Player.overlay(mc.player.getHeldItemOffhand().getDisplayName());
			return true;
		}
		
		return false;
	}
	
	private boolean swapTalTal() {
		int findTal1 = findEnchantedTotem(0);
		int findTal2 = findEnchantedTotem(findTal1 + 1);
		
		findTal1 = findTal1 == 40 ? 45 : findTal1 < 9 ? 36 + findTal1 : findTal1;
		findTal2 = findTal2 == 40 ? 45 : findTal2 < 9 ? 36 + findTal2 : findTal2;
		
		if (findTal1 != 45 && findTal2 != 45) {
			findTal1 = 45;
		}
		
		if (findTal1 != -1 && findTal2 != -1) {
			move(findTal1, findTal2);
			if (mc.player.getHeldItemOffhand().getItem() != Items.AIR)
				Player.overlay(mc.player.getHeldItemOffhand().getDisplayName());
			return true;
		}
		
		return false;
	}
	
	protected void move(int one, int two) {
		Player.moveItem(one, two, true);
	}
	
	protected boolean findItem(Item item) {
		return Player.findItem(46, item) != -1;
	}
	
    protected int findEnchantedTotem() {
    	int startSlot = 0;
    	
    	ItemStack stack1 = mc.player.inventory.getStackInSlot(40);
    	if (stack1.getItem() == Items.TOTEM_OF_UNDYING && stack1.isEnchanted() && startSlot == 0){
			return 45;
		}
    	
        for (int i = startSlot; i < 46; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING && stack.isEnchanted()) {
                return i;
            }
        }
        return -1;
    }
    
    protected int findEnchantedTotem(int startSlot) {
        for (int i = startSlot; i < 46; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING && stack.isEnchanted()) {
                return i;
            }
        }
        return -1;
    }
    
    protected int findHead(int startSlot) {
        for (int i = startSlot; i < 46; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() == Items.PLAYER_HEAD) {
                return i;
            }
        }
        return -1;
    }

	
	@Override
	public void onDisable() {
		
	}
	
	@Override
	public void onEnable() {
		
	}
}
