package fun.rockstarity.api.render.ui.mainmenu.alt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.ui.rect.Rect;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * @author ConeTin
 * @since 23 РѕРєС‚. 2024вЂЇРі.
 */

@Getter
@RequiredArgsConstructor
public class Alt {
	
	private final Animation selectedAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	private final Animation hoverAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	private final Animation favoriteAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	private final Animation deleteAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	
	@Setter private FixColor donateColor;
	@Setter private String donate;
	
	private final String username;
	private final boolean random;
	private final List<AltServer> servers = new ArrayList<>();
	@Setter private boolean favorite;
	@Setter private boolean banned;
	@Setter private String note = "";

	@Setter private Rect favoriteBtn, deleteBtn;
	
    public JsonObject save() {
        JsonObject json = new JsonObject();

        if (donateColor != null) {
            json.addProperty("donate-color", donateColor.getRGB());
        }
        if (donate != null) {
            json.addProperty("donate", donate);
        }

        json.addProperty("username", username);
        json.addProperty("banned", banned);
        json.addProperty("favorite", favorite);
        json.addProperty("random", random);
        json.addProperty("note", note);

        JsonObject serversJson = new JsonObject();
        for (AltServer server : servers) {
            serversJson.addProperty(server.getIp(), server.getBan());
        }
        json.add("servers", serversJson);

        return json;
    }

    public static Alt load(JsonObject json) {
        Alt alt = new Alt(json.get("username").getAsString().replace("\\", ""), json.has("random") && json.get("random").getAsBoolean());
        
        if (json.has("banned") && !json.get("banned").isJsonNull()) {
        	alt.setBanned(json.get("banned").getAsBoolean());
        }
        if (json.has("favorite") && !json.get("favorite").isJsonNull()) {
        	alt.setFavorite(json.get("favorite").getAsBoolean());
        }
        if (json.has("note") && !json.get("note").isJsonNull()) {
            alt.setNote(json.get("note").getAsString());
        }
        if (json.has("donate") && !json.get("donate").isJsonNull()) {
            alt.setDonate(json.get("donate").getAsString());
        }
        if (json.has("donate-color") && !json.get("donate-color").isJsonNull()) {
            alt.setDonateColor(new FixColor(json.get("donate-color").getAsInt()));
        }

        if (json.has("servers") && json.get("servers").isJsonObject()) {
            JsonObject serversJson = json.getAsJsonObject("servers");
            for (Map.Entry<String, JsonElement> entry : serversJson.entrySet()) {
                AltServer server = new AltServer(entry.getKey());
                server.setBan(entry.getValue().getAsString());
                alt.getServers().add(server);
            }
        }

        return alt;
    }
}
