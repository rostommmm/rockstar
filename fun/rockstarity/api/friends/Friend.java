package fun.rockstarity.api.friends;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fun.rockstarity.api.interfaces.Jsonable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Представляет собой друга, который используется в {@link FriendsHandler}
 * @author jbk
 * @since 31.01.2025
 */
@Setter
public class Friend implements Jsonable {

    public Friend(String name, String hiddenName) {
        this.name = name;
        this.hiddenName = hiddenName;
    }

    public Friend(String name) {
        this.name = name;
        this.hiddenName = "";
    }

    @Getter 
    private String name;

    /**
     * Имя, которое используется если модуль NameProtect включен
     */
    private String hiddenName;

    @Override
    public void load(JsonElement element) {
        // ничего не делаем (наверное?)
    }

    @Override
    public JsonElement save() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("originalName", this.name);
        jsonObject.addProperty("hiddenName", this.hiddenName);
        return jsonObject;
    }
    
    public boolean haveHiddenName() {
    	return !hiddenName.isEmpty();
    }

    public String getHiddenName(String alternative) {
        return hiddenName.isEmpty() ? alternative : hiddenName;
    }

    @Override
    public String toString() {
        return name;
    }
}
