package fun.rockstarity.client.modules.combat;

import java.util.HashMap;

import fun.rockstarity.api.ClientInfo;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.inputs.EventInput;
import fun.rockstarity.api.events.list.player.EventJump;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventMove;
import fun.rockstarity.api.events.list.player.EventTrace;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.helpers.math.aura.AuraUtility;
import fun.rockstarity.api.helpers.math.aura.Rotation;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Select.Element;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.client.modules.other.Globals;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;


/**
 * Я когда-нибудь уйду
 * И сурсы эти солью
 * Долбаёбы не поймут
 * Каково быть на виду
 * 
 * Я когда-нибудь уйду (Я, я, я, я-я)
 * И сурсы эти солью (Все сурсы солью)
 * Долбаёбы не поймут (Не понять, никогда)
 * Каково быть на виду (Это так, это так)
 * Я когда-нибудь уйду (Я, я, я, я-я)
 * И сурсы эти солью (Все сурсы солью)
 * Долбаёбы не поймут (Не понять, никогда)
 * Каково быть на виду (Это так, это так)
 * Я когда нибудь уйду...
 * 
 * Каждый класс, каждый метод - новое кино (Go)
 * Кто мне друг? Кто мне враг? Думаю, никто (Шоу)
 * Пастинг, кубы-игра - тупо шапито (Цирк)
 * Конетинка в этом цирке - самый главный клоун (Клоун)
 * Я мечтал, что буду популярен (Эй, ха, а-а)
 * Я всё еще мечтаю...
 * Я заработал только мешки под глазами
 * А чё мне дали эти кубы?
 * Нихуя не дали
 * "Ты же этого всю жизнь хотел
 * Ну а хули ты теперь тут ноешь?"
 * - Конечно, ною, я же пастерок
 * Я недоволен
 * 
 * Я когда-нибудь уйду (Я, я, я, я-я)
 * И сурсы эти солью (Все сурсы солью)
 * Долбаёбы не поймут (Не понять, никогда)
 * Каково быть на виду (Это так, это так)
 * Я когда-нибудь уйду (Я, я, я, я-я)
 * И сурсы эти солью (Все сурсы солью)
 * Долбаёбы не поймут (Не понять, никогда)
 * Каково быть на виду (Это так, это так)
 * Я когда нибудь уйду...
 */


/**
 * @author ConeTin
 * @since 29 июл. 2024 г.
 */

@FieldDefaults(level = AccessLevel.PRIVATE)
@Info(name="AimBot", desc="Наводится с лука, трезубцов, арбалетов и проч.", type=Category.COMBAT)
public class AimBot extends Module {
	
	final Select items = new Select(this, "Предметы").min(1);
	final ItemElement bow = new ItemElement(items, "Лук", Items.BOW, 0.15f);
    final ItemElement crossbow = new ItemElement(items, "Арбалет", Items.CROSSBOW, 0.16f);
    final ItemElement trident = new ItemElement(items, "Трезубец", Items.TRIDENT, 0.22f);
	
    final CheckBox prediction = new CheckBox(this, "Предсказывание позиции").desc("Предсказывает следующую позицию цели наперед, что позволяет попадать точнее").set(true);
    
	final Slider distance = new Slider(this, "Дистанция").min(0).max(100).inc(1).set(30).text(0, "Выкл.");
	
	final Slider fov = new Slider(this, "Поле зрения").min(1).max(180).set(90).inc(1).desc("Поле зрения на которой будет работать наводка");
	final CheckBox fovCircle = new CheckBox(this, "Круг FOV'a").hide(() -> this.fov.get() > 90).desc("Рендерит круг поле зрения");
	final CheckBox silent = new CheckBox(this, "Свободный аим").desc("Делает вашу камеру свободной");
	
	final Select targets = new Select(this, "Цели").min(1).desc("Сущности, на которых будет наводиться Aimbot");
	
	final Select.Element players = new Select.Element(targets, "Игроки").set(true);
	final Select.Element invisibles = new Select.Element(targets, "Невидимые").set(true).hide(() -> !this.players.get());
	final Select.Element naked = new Select.Element(targets, "Голые").set(true).hide(() -> !this.players.get());
	final Select.Element friends = new Select.Element(targets, "Друзья").set(true).hide(() -> !this.players.get());
	final Select.Element bots = new Select.Element(targets, "Боты").hide(() -> !this.players.get());
	final Select.Element mobs = new Select.Element(targets, "Мобы");
	final Select.Element rockUser = new Select.Element(targets, "Пользователи " + ClientInfo.NAME).hide(() -> !rock.getModules().get(Globals.class).get());
	
	float yaw, pitch;	
	@Getter LivingEntity target;
		
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventRender2D && fovCircle.get() && fov.get() <= 90 && (mc.player.getHeldItemMainhand().getItem() == Items.BOW && bow.get() || mc.player.getHeldItemMainhand().getItem() == Items.CROSSBOW && crossbow.get() || mc.player.getHeldItemMainhand().getItem() == Items.TRIDENT && trident.get())) {
			Render.drawCircle(sr.getScaledWidth() / 2, sr.getScaledHeight() / 2, 0, 360, fov.get() * 2, 1, false, Style.getMain().getRGB()); // Рисуем круг в центре экрана и с радиусом заданный через слайдер (fov.get())
		}
		
    	if ((event instanceof EventMotion || event instanceof EventTrace || event instanceof EventMove || event instanceof EventJump || event instanceof EventInput)) {
    		for (Element item : this.items.getElements()) {
    			if (check(item)) {
    				target = AuraUtility.calculateTarget(mc.player.getPositionVec(), this.distance.get(), this.players.get(), this.mobs.get(), this.invisibles.get(), this.naked.get(), this.bots.get(), this.friends.get(), rockUser.get(), true, false, false, false, false);
        			if (target == null || Player.getFOV(target) > fov.get()) break;
                	applyRotation(event, (ItemElement) item);
    			} else {
    				target = null;
    			}
            }
    	}
	}
	
	/**
	 * Метод для ротации для предмета
	 */
	private void applyRotation(Event event, ItemElement item) {
		if (event instanceof EventMotion e) {
			float modif = 10;
			
			Vector3d predicted = new Vector3d(
				this.target.getPosX() + (this.target.getPosX() - this.target.lastTickPosX) * modif,
				this.target.getPosY() + (this.target.getPosY() - this.target.lastTickPosY) * modif / 5F + this.target.getHeight()/2F,
				this.target.getPosZ() + (this.target.getPosZ() - this.target.lastTickPosZ) * modif
			);

			Vector2f rotation = Rotation.get(this.prediction.get() ? predicted : this.target.getPositionVec().add(0,this.target.getHeight()/2F,0));

			HashMap<ItemElement, Float[]> map = new HashMap<>();
			map.put(this.crossbow, new Float[] { 0.0f, 0.1f });
			map.put(this.bow, new Float[] { 0f, 0f });
			map.put(this.trident, new Float[] { 0.5f, 1f });
			
			Vector2f corrected = Rotation.correctRotation(
    			rotation.x, (float) (rotation.y - mc.player.getDistance(this.target) * item.gravity + mc.player.getMotion().y * mc.player.getDistance(this.target) * (mc.player.getMotion().y > 0 ? map.get(item)[0] : map.get(item)[1]))
			);
			
            yaw = corrected.x;
            pitch = corrected.y;
            
            e.setYaw(yaw);
            e.setPitch(pitch);
            
            if (!silent.get()) {
                mc.player.rotationYaw = yaw;
                mc.player.rotationPitch = pitch;
            }
		}
		
		if (silent.get()) { // Если Сайлент наводка включена
			Player.look(event, yaw, pitch, true);
		}
	}
	
	private boolean check(Element item) {
		return item.get() && mc.player.getHeldItemMainhand().getItem() == ((ItemElement) item).item && (((ItemElement) item).item == Items.CROSSBOW  ? ((CrossbowItem)mc.player.getHeldItemMainhand().getItem()).isCharged(mc.player.getHeldItemMainhand()) : mc.player.isHandActive());
	}

	class ItemElement extends Select.Element {
		
		private Item item;
		private float gravity;
		
        public ItemElement(Select select, String name, Item item, float gravity) {
            super(select, name);
            this.item = item;
            this.gravity = gravity;
            this.set(true);
        }
		
	}
	
	@NativeInclude
	@Override
	public void onDisable() {
		target = null;
	}

	@Override
	public void onEnable() {
		
	}
	
}