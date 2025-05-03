package fun.rockstarity.api.render.ui.draggables;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.binds.Bindable;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.interfaces.Jsonable;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.animation.infinity.InfinityAnimation;
import fun.rockstarity.api.render.ui.rect.Rect;
import lombok.Getter;
import lombok.Setter;

/**
 * @author ConeTin
 * @since 19 мар. 2024 г.
 */

@Getter
public class Draggable extends Rect implements IAccess, Jsonable {
	
	@Setter private boolean canDrag = true;
	
	private final String name;
	@Setter private boolean dragging;
	@Setter private boolean selected;
	@Setter private float dragX, dragY;
	
	public Draggable(String name, float x, float y, float width, float height) {
		super(x,y,width,height);
		this.name = name;
		rock.getDraggableHandler().getDraggables().add(this);
	}
	
	public Draggable(String name, Rect rect) {
		this(name, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
	}

/**
     * Загружает данные из JSON.
     *
     * @param element JSON-элемент, содержащий данные
     */
    @Override
    public void load(JsonElement element) {
        JsonObject dragData = element.getAsJsonObject();
        this.setX(dragData.get("x").getAsFloat());
        this.setY(dragData.get("y").getAsFloat());
        this.setWidth(dragData.get("width").getAsFloat());
        this.setHeight(dragData.get("height").getAsFloat());
    }

    /**
     * Сохраняет объект в JSON.
     *
     * @return JsonElement, представляющий объект
     */
    @Override
    public JsonElement save() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("x", this.getX());
        jsonObject.addProperty("y", this.getY());
		// понятия не имею почему мы сохраняем это, но пусть будет))
        jsonObject.addProperty("width", this.getWidth());
        jsonObject.addProperty("height", this.getHeight());
        return jsonObject;
    }
}