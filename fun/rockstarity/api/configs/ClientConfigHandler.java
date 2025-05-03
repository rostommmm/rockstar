package fun.rockstarity.api.configs;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.binds.Bind;
import fun.rockstarity.api.binds.BindType;
import fun.rockstarity.api.friends.Friend;
import fun.rockstarity.api.helpers.game.Binds;
import fun.rockstarity.api.helpers.game.GameUtility;
import fun.rockstarity.api.helpers.secure.KeyGeneration;
import fun.rockstarity.api.helpers.secure.Web;
import fun.rockstarity.api.modules.settings.Setting;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.render.ui.draggables.Draggable;
import fun.rockstarity.api.render.ui.mainmenu.Page;
import fun.rockstarity.api.render.ui.mainmenu.alt.Alt;
import fun.rockstarity.api.render.ui.mainmenu.screens.AltScreen;
import fun.rockstarity.api.scripts.Script;
import fun.rockstarity.api.secure.Debugger;
import fun.rockstarity.client.commands.InventoryCommand;
import fun.rockstarity.client.commands.MacroCommand;
import fun.rockstarity.client.commands.ScoreCommand;
import fun.rockstarity.client.commands.ScoreCommand.Line;
import fun.rockstarity.client.commands.WayCommand;
import ru.kotopushka.antiautistleak.obfuscator.includes.annotations.compile.ReleaseCompileToNativeCalls;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;
import ru.kotopushka.j2c.sdk.annotations.VMProtect;
import ru.kotopushka.j2c.sdk.enums.VMProtectType;

/**
 * @author ConeTin
 * @since 1 апр. 2024 г.
 *
 * @author jbk
 */

@ReleaseCompileToNativeCalls
public class ClientConfigHandler implements IAccess {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public void load() {
		HashMap<String, String> requestData = new HashMap<>();
		requestData.put("id", KeyGeneration.encrypt(String.valueOf(rock.getUser().getId())));

		try {
			// не знаю как это и что это, но я крут
//			String response = Web.protectedPostRequest(requestData, "https://rockstar.moscow/api/v1/premium/client/client_config/load.php").trim();
			JsonObject info = GSON.fromJson("" /* При переписывании на локальные конфиги, тут надо будет ебануть получение контента из конфига */, JsonObject.class);

			if (info == null) return;

			// Загружаем макросы
			if (info.has("macroses")) {
				JsonObject macroses = info.getAsJsonObject("macroses");
				for (Map.Entry<String, JsonElement> entry : macroses.entrySet()) {
					MacroCommand.getMacroses().add(new MacroCommand.Macro(
							entry.getKey(),
							Binds.KEYS.get(entry.getKey().toLowerCase()),
							entry.getValue().getAsString()
					));
				}
			}

			// Загружаем изменения скорборда
			if (info.has("scoreboard")) {
				JsonObject scoreboard = info.getAsJsonObject("scoreboard");
				for (Map.Entry<String, JsonElement> entry : scoreboard.entrySet()) {
					ScoreCommand.getScores().add(new Line(entry.getKey(), entry.getValue().getAsString()));
				}
			}

			// Загружаем активные скрипты
			if (info.has("scripts")) {
				JsonObject scripts = info.getAsJsonObject("scripts");
				for (Script script : rock.getScriptHandler().getScripts()) {
					if (scripts.has(script.getName())) {
						script.setEnabled(scripts.get(script.getName()).getAsBoolean());
						if (script.isEnabled()) script.load();
					}
				}
			}

			// Загружаем положение драгграбельных объектов
			if (info.has("draggables")) {
				JsonObject draggables = info.getAsJsonObject("draggables");
				for (Draggable draggable : rock.getDraggableHandler().getDraggables()) {
					if (draggables.has(draggable.getName())) {
						JsonObject dragData = draggables.getAsJsonObject(draggable.getName());
						draggable.load(dragData);
					}
				}
			}

			// Загружаем никнейм
			if (info.has("username")) {
				GameUtility.changeName(info.get("username").getAsString());
			}
			
			// Загружаем никнейм
			if (info.has("via")) {
				rock.getVia().getViaScreen().setText(info.get("via").getAsString());
			}

			// Загружаем WayCommand
			if (info.has("waypoints")) {
				rock.getCommands().get(WayCommand.class).load(info.getAsJsonObject("waypoints"));
			}

			// Загружаем инвентарь
			if (info.has("inventory")) {
				rock.getCommands().get(InventoryCommand.class).load(info.getAsJsonObject("inventory"));
			}

			// Загружаем аккаунты (альты)
			if (info.has("alts")) {
				JsonObject alts = info.getAsJsonObject("alts");
				AltScreen altScreen = (AltScreen) Page.ALT.getScreen();
				for (Map.Entry<String, JsonElement> entry : alts.entrySet()) {
					altScreen.getAlts().add(Alt.load(entry.getValue().getAsJsonObject()));
				}
			}

			// Загружаем текущий конфиг
			if (info.has("config")) {
				rock.setCfgToLoad(info.get("config").getAsString());
			}

			// Загружаем скрытие информации
			if (info.has("hideinfo")) {
				rock.setHideInfo(info.get("hideinfo").getAsBoolean());
			}

			// Загружаем друзей
			if (info.has("friends")) {
				JsonObject friendsJson = info.getAsJsonObject("friends");
					for (Map.Entry<String, JsonElement> entry : friendsJson.entrySet()) {
						rock.getFriendsHandler().getFriends().add(new Friend(entry.getKey()));
					}
			}

			// Загружаем цели
			if (info.has("targets")) {
				String[] targetsArray = info.get("targets").getAsString().split(",");
				for (String target : targetsArray) {
					String trimmedTarget = target.trim();
					if (!trimmedTarget.isEmpty() && !rock.getTargetHandler().getTarget().contains(trimmedTarget)) {
						rock.getTargetHandler().getTarget().add(trimmedTarget);
					}
				}
			}
			
			// Загрузка утилит загрузки конфига
			if (info.has("configs")) {

				ConfigWrapper wrapper = rock.getConfigHandler().getWindow().getElement();
				ConfigsHandler configs = rock.getConfigHandler();
			    JsonObject jsonConfigs = info.getAsJsonObject("configs");
			    Gson gson = new Gson();
			    JSONObject config = (JSONObject) new JSONParser().parse(jsonConfigs.toString());

			    for (Setting set : wrapper.getSettings()) {
					set.getBinds().clear();
					try {
						configs.loadSetting(set, config);
						
						if (set instanceof Select select) {
							JSONObject selects = (JSONObject) ((JSONObject) config.get(set.getName())).get("selects");
							for (Select.Element elmt : select.getElements()) {
								elmt.getBinds().clear();
								JSONObject binds = (JSONObject) selects.get(elmt.getName());

								for (Object bind : binds.keySet()) {
									elmt.getBinds().add(new Bind(Integer.parseInt((bind + "").split(":")[0]), Integer.parseInt((bind + "").split(":")[1]), BindType.get(binds.get(bind) + "")));
								}
							}
						}
						
						JSONObject binds = (JSONObject) ((JSONObject) config.get(set.getName())).get("binds");

						for (Object bind : binds.keySet()) {
							set.getBinds().add(new Bind(Integer.parseInt((bind + "").split(":")[0]), Integer.parseInt((bind + "").split(":")[1]), BindType.get(binds.get(bind) + "")));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			Debugger.print(e);
		}
	}


	public void save() {
		if (rock.isPanic()) return;

		JsonObject info = new JsonObject();
		info.addProperty("username", mc.getSession().getUsername());
		info.addProperty("via", rock.getVia().getViaScreen().getText());
		info.addProperty("config", rock.getConfigHandler().getCurrent());
		info.add("waypoints", rock.getCommands().get(WayCommand.class).save());
		info.add("inventory", rock.getCommands().get(InventoryCommand.class).save());
		info.addProperty("hideinfo", rock.isHideInfo());
		info.addProperty("targets", String.join(",", rock.getTargetHandler().getTarget()));

		// Сохранение макросов
		JsonObject macroses = new JsonObject();
		for (MacroCommand.Macro macro : MacroCommand.getMacroses()) {
			macroses.addProperty(macro.getName(), macro.getMsg().trim());
		}
		info.add("macroses", macroses);

		// Сохраняем изменения скорборда
		JsonObject scoreboard = new JsonObject();
		for (Line line : ScoreCommand.getScores()) {
			scoreboard.addProperty(line.getOriginal(), line.getNewVal());
		}
		info.add("scoreboard", scoreboard);

		// Сохранение draggable-объектов
		JsonObject draggablesJson = new JsonObject();
		for (Draggable draggable : rock.getDraggableHandler().getDraggables()) {
			draggablesJson.add(draggable.getName(), draggable.save());
		}
		info.add("draggables", draggablesJson);

		// Сохранение активных скриптов
		JsonObject scripts = new JsonObject();
		for (Script script : rock.getScriptHandler().getEnabledScripts()) {
			scripts.addProperty(script.getName(), script.isEnabled());
		}
		info.add("scripts", scripts);

		// Сохранение аккаунтов
		JsonObject alts = new JsonObject();
		AltScreen altScreen = (AltScreen) Page.ALT.getScreen();
		for (int i = 0; i < altScreen.getAlts().size(); i++) {
			alts.add(String.valueOf(i), altScreen.getAlts().get(i).save());
		}
		info.add("alts", alts);

		// Сохранение друзей
		JsonObject friendsJson = new JsonObject();
		for (Friend friend : rock.getFriendsHandler().getFriends()) {
			friendsJson.add(friend.getName(), friend.save());
		}
		info.add("friends", friendsJson);
		
		// Сохранение утилит загрузки конфига
		ConfigWrapper wrapper = rock.getConfigHandler().getWindow().getElement();
		ConfigsHandler configs = rock.getConfigHandler();
		JSONObject config = new JSONObject();

		for (Setting set : wrapper.getSettings()) {
			JSONObject setting = new JSONObject();
			
			configs.saveSetting(set, setting);
			
			if (set instanceof Select select) {
				JSONObject selects = new JSONObject();
				for (Select.Element elmt : select.getElements()) {
					JSONObject binds = new JSONObject();
					for (Bind bind : elmt.getBinds()) {
						binds.put(bind.getKey() + ":" + bind.getScancode(), bind.getType().getName());
					}
					
					selects.put(elmt.getName(), binds);
				}
				setting.put("selects", selects);
			}
			
			JSONObject binds = new JSONObject();
			for (Bind bind : set.getBinds()) {
				binds.put(bind.getKey() + ":" + bind.getScancode(), bind.getType().getName());
			}
			
			setting.put("binds", binds);
			config.put(set.getName(), setting);
		}

		info.add("configs", new JsonParser().parse(config.toString()).getAsJsonObject());

		// JSON-сериализация
		String json = GSON.toJson(info);

//		HashMap<String, String> requestData = new HashMap<>();
//
//		requestData.put("id", KeyGeneration.encrypt(String.valueOf(rock.getUser().getId())));
//		requestData.put("text", KeyGeneration.encrypt(json));

//		Web.protectedPostRequest(requestData, "https://rockstar.moscow/api/v1/premium/client/client_config/save.php").trim();
	}
}