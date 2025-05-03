package fun.rockstarity.client.modules.combat;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.EventType;
import fun.rockstarity.api.events.list.game.EventPickupItem;
import fun.rockstarity.api.events.list.game.EventTotemBreak;
import fun.rockstarity.api.events.list.game.EventWorldChange;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.player.Inventory;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Select.Element;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.color.FixColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.item.minecart.TNTMinecartEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author ConeTin
 * @since 6 дек. 2023 г.
 */


@Info(name="AutoTotem", desc="Автоматически ставит тотем в левую руку", type=Category.COMBAT)
public class AutoTotem extends Module {

	private final Mode mode = new Mode(this, "Брать первую очередь");
	private final Mode.Element noSort = new Mode.Element(mode, "Без сортировки");
	private final Mode.Element sortNon = new Mode.Element(mode, "Не зачарованные");
	private final Mode.Element sortOn = new Mode.Element(mode, "Зачарованные");
	
    private final Slider health = new Slider(this, "Здоровье").min(1).max(20).inc(0.5f).set(4).desc("Здоровье при котором будет браться тотем");
    private final Slider healthwithelytra = new Slider(this, "Здоровье c элитрой").min(1).max(20).inc(0.5f).set(4).desc("Здоровье с элитрой при котором будет браться тотем");
    private final CheckBox swapBack = new CheckBox(this, "Возвращать обратно").set(true).desc("Возвращает предмет обратно");
    private final CheckBox display = new CheckBox(this, "Отображение количества").set(true).desc("Отображает количество тотемов");
    
    private final CheckBox eat = new CheckBox(this, "Не менять если ешь");

    private final Select utils = new Select(this, "Брать при...");

    private final Element crys = new Element(utils, "Кристалле").set(true);
    private final Element minecart = new Element(utils, "Вагонетке с динамитом").set(true);
    private final Element fall = new Element(utils, "Падении");

    private final Slider crysDist = new Slider(this, "Дистанция кристалла").min(2).max(20).inc(1).set(7).hide(() -> !this.crys.get());
    private final Slider minecartDist = new Slider(this, "Дистанция вагонетки").min(2).max(20).inc(1).set(7).hide(() -> !this.minecart.get());
    private final Slider fallDist = new Slider(this, "Дистанция до земли").min(4).max(52).inc(2).set(6).hide(() -> !this.fall.get());

    private boolean needSwap;
    private int swapItem = -1;
    private Item last;
    protected final Animation showing = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
    
    private final TimerUtility lastUse = new TimerUtility();
    
    private final Slider cooldown = new Slider(this, "Задержка на взятие").min(0).max(5000).inc(250).set(1000).desc("Задержка в миллисекундах на взятие нового тотема после сноса старого");

    @NativeInclude
    public AutoTotem() {
    	super(0);
    }

    @Override
    @EventType({EventRender2D.class, EventUpdate.class, EventTotemBreak.class})
    public void onEvent(Event event) {
        if (event instanceof EventRender2D e) {
            renderTotem(e);
        }
        
        if (event instanceof EventPickupItem) {
        	lastUse.reset();
        }
        
        if (event instanceof EventUpdate) {
            float health = mc.player.getHealth() + mc.player.getAbsorptionAmount(); // Сумма здоровья и золотых сердечек
            ItemStack leftStack = mc.player.getHeldItemOffhand(); // Получаем предмет в левой руке
            Item left = leftStack.getItem();
            ItemStack rightStack = mc.player.getHeldItemMainhand(); // Получаем предмет в правой руке
            Item right = rightStack.getItem();
            
            int preferredTotem = -1; 
            int fallbackTotem = -1; 
            
            if (this.mode.is(this.sortNon)) {
                preferredTotem = this.findTotemInInventoryNon();
                fallbackTotem = this.findTotemInInventoryOn();
            } 
            else if (this.mode.is(this.sortOn)) {
                preferredTotem = this.findTotemInInventoryOn();
                fallbackTotem = this.findTotemInInventoryNon();
            } 
            else {
                preferredTotem = this.findAnyTotemInInventory();
            }

            float required = Player.isElytraEquiped() ? healthwithelytra.get() : this.health.get();
            boolean shouldActivate = (!eat.get() || !mc.player.isHandActive()) && 
                                   (health <= required || this.checkCrystal() || this.checkMinecart() || this.checkFall()) &&
                                   lastUse.passed(cooldown.get());

            if (shouldActivate) {
                int totemToUse = -1;
                
                if (this.mode.is(this.sortNon)) {
                    if (preferredTotem != -1) {
                        totemToUse = preferredTotem;
                    } else if (fallbackTotem != -1 && left != Items.TOTEM_OF_UNDYING) {
                        totemToUse = fallbackTotem;
                    }
                } 
                else if (this.mode.is(this.sortOn)) {
                    if (preferredTotem != -1) {
                        totemToUse = preferredTotem;
                    } else if (fallbackTotem != -1 && left != Items.TOTEM_OF_UNDYING) {
                        totemToUse = fallbackTotem;
                    }
                } 
                else {
                    if (preferredTotem != -1) {
                        totemToUse = preferredTotem;
                    }
                }

                if (totemToUse != -1) {
                    this.moveItem(totemToUse);
                }
            } 
            else {
                if ((this.swapItem >= 0 || last != null) && this.needSwap && this.swapBack.get()) {
                    swap();
                }
            }
        }

        if (event instanceof EventTotemBreak e && e.getEntity() == mc.player) { // Проверяем, что тотем снесли именно тебе
            this.needSwap = true; // При сносе тотема сообщаем о том, что его необходимо свапнуть обратно
            lastUse.reset();
        }
        
        if (event instanceof EventWorldChange) {
            needSwap = false;
        }
    }

    private void swap() {
    	Item item = mc.player.getItemStackFromSlot(EquipmentSlotType.OFFHAND).getItem();

    	//if (item == Items.TOTEM_OF_UNDYING || item == Items.AIR)
    	//	return;

    	if (last != null) {
    		Inventory.moveItem(Inventory.findItem(44, last), 45, true);
    	    swapItem = -1;
            needSwap = false;
    		return;
    	}
        Inventory.moveItem(this.swapItem, 45, true); // Перемещаем вещь которая была до этого в левую руку
        swapItem = -1;
        needSwap = false;
    }

    private void renderTotem(EventRender2D e) {
    	showing.setForward(mc.getGameSettings().hideGUI || mc.getGameSettings().showDebugInfo || !display.get());
        int heigt = sr.getScaledHeight();
        int width = sr.getScaledWidth();
        float x = width / 2 + 103;
        float y = heigt - 24.5f - 14 * mc.ingameGUI.getOpening().get() + 17 * showing.get();
        
        if (!showing.finished()) {
            if (foundTotemCount() > 0) {
                bold.get(12).draw(e.getMatrixStack(), String.valueOf(foundTotemCount()),
                        x - bold.get(12).getWidth(String.valueOf(foundTotemCount())) / 2 + 8.1f, y + 15.3f, foundTotemCount() > 2 ? FixColor.WHITE : FixColor.RED);

                
                Render.drawStackOld(Items.TOTEM_OF_UNDYING.getDefaultInstance(), (int) x - 8, (int) y + 7, 1);
            }
        }
    }

    private boolean checkFall() {
        if (!this.fall.get()) return false;
        if (mc.player.isElytraFlying()) return false;

        return mc.player.fallDistance >= this.fallDist.get();
    }

    private boolean checkCrystal() {
        if (!this.crys.get()) return false;

        for (Entity entity : mc.world.getAllEntities()) {
            if (!(entity instanceof EnderCrystalEntity) || mc.player.getDistance(entity) > this.crysDist.get()) continue;
            return true;
        }
        return false;
    }

    private boolean checkMinecart() {
        if (!this.minecart.get()) return false;

        for (Entity entity : mc.world.getAllEntities()) {
            if (!(entity instanceof TNTMinecartEntity) || mc.player.getDistance(entity) > this.minecartDist.get()) continue;
            return true;
        }
        return false;
    }

    private int foundTotemCount() {
        return (int) mc.player.inventory.mainInventory
                .stream()
                .filter(stack -> stack.getItem() == Items.TOTEM_OF_UNDYING)
                .count();
    }
    
    private int findAnyTotemInInventory() {
        for (int i = 0; i < mc.player.inventory.mainInventory.size(); ++i) {
            ItemStack stack = mc.player.inventory.mainInventory.get(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                return i;
            }
        }
        return -1;
    }

    private int findTotemInInventoryNon() {
        for (int i = 0; i < mc.player.inventory.mainInventory.size(); ++i) {
            ItemStack stack = mc.player.inventory.mainInventory.get(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING && !stack.isEnchanted()) {
                return i; 
            }
        }
        return -1;
    }

    private int findTotemInInventoryOn() {
        for (int i = 0; i < mc.player.inventory.mainInventory.size(); ++i) {
            ItemStack stack = mc.player.inventory.mainInventory.get(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING && stack.isEnchanted()) {
                return i;
            }
        }
        return -1;
    }
    
    private void moveItem(int j) {
    	Item item = mc.player.getItemStackFromSlot(EquipmentSlotType.OFFHAND).getItem();
    	if (item != Items.AIR && item != Items.TOTEM_OF_UNDYING)
    		last = item;
        swapItem = j < 9 ? j + 36 : j;
        Inventory.moveItem(45, j < 9? j + 36 : j, true);
        this.needSwap = true;
    }
    
    @Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}
}