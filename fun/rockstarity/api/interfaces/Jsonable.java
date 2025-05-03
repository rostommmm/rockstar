package fun.rockstarity.api.interfaces;

import com.google.gson.JsonElement;

/**
 * Интерфейс для объектов, которые могут быть сохранены и загружены из JSON
 * @author jbk
 * @since 31.01.2025
 */
public interface Jsonable {

    /**
     * Загружает данные из JSON.
     *
     * @param element JSON-элемент, содержащий данные
     */
    void load(JsonElement element);

    /**
     * Сохраняет объект в JSON.
     *
     * @return JsonElement, представляющий объект
     */
    JsonElement save();
}
