package fun.rockstarity.api.script;

import java.util.HashMap;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.secure.KeyGeneration;
import fun.rockstarity.api.helpers.secure.Web;
import fun.rockstarity.api.helpers.system.ThreadManager;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import fun.rockstarity.api.scripts.Script;
import fun.rockstarity.api.scripts.wrappers.base.ScriptBase;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import ru.kotopushka.antiautistleak.obfuscator.includes.annotations.compile.ReleaseCompileToNativeCalls;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

@ReleaseCompileToNativeCalls
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScriptSender implements IAccess {
	
	@Getter @Setter
	String current = "default";
	
	public void save(Script script) {
		String name = ScriptBase.getName();
		
		if (name == null) return;
		
		if (name.length() > 20) {
			name = name.substring(0, 19);
		}
		
		final String finalName = name;
		String user = rock.getUser().getName();
		
		ThreadManager.run(() -> {
//			HashMap<String, String> map = new HashMap<>();
//
//			map.put("id", KeyGeneration.encrypt("" + rock.getUser().getId()));
//			map.put("cfgname", KeyGeneration.encrypt(finalName));
//			map.put("code", KeyGeneration.encrypt(script.getContent()));
			//if (rock.isDebugging())
			//	System.out.println(code);
			
//			Web.protectedPostRequest(map, "https://rockstar.moscow/api/v1/premium/client/script/save.php");
			
			current = finalName;
		});
	}
	
	/*
	public void load(String name) {
		String user = rock.getUser().getName();
		
		ThreadManager.run(() -> {
			try {
				HashMap<String, String> data = new HashMap<>();
				data.put("id", KeyGeneration.encrypt("" + rock.getUser().getId()));
				data.put("cfgname", KeyGeneration.encrypt(name));

				String req = Web.protectedPostRequest(data, "https://rockstar.moscow/api/v1/premium/client/config/load.php").trim();
				String code = KeyGeneration.decrypt(req);

				JSONObject config = null;
				try {
					JSONParser parser = new JSONParser();
					config = (JSONObject) parser.parse(code);
					
					for (fun.rockstarity.api.modules.Module mod : rock.getModules().values()) {
						if (loadBinds)
						mod.getBinds().clear();
					}
				} catch (Exception e) {
					Debugger.print(e);
					if (!silent)
						alert.hide();
					rock.getAlertHandler().alert("Ошибка при загрузке", AlertType.ERROR);
					return;
				}
				
				List<Module> modules = new ArrayList<>();
				
				rock.getModules().values().forEach(mod -> modules.add(mod));
				for (Script script : rock.getScriptHandler().getEnabledScripts()) {
					script.getScriptModules().forEach(mod -> modules.add(mod));
				}
				
				for (fun.rockstarity.api.modules.Module mod : modules) {
					boolean next = false;
					
					if (Player.isInGame())
					for (Select.Element elmt : window.getElement().getCategories().getElements()) {
						if (mod.getInfo().type().getDisplayName().equals(elmt.getName()) && !elmt.get()) {
							next = true;
						}
					}

					if (next) continue;
					
					JSONObject mods = (JSONObject) ((JSONObject) config.get("mods")).get(mod.getInfo().name());

					for (Setting set : mod.getSettings()) {
						if (loadBinds)
						set.getBinds().clear();
						try {
							loadSetting(set, mods);
							
							if (set instanceof Select select) {
								JSONObject selects = (JSONObject) ((JSONObject) mods.get(set.getName())).get("selects");
								for (Select.Element elmt : select.getElements()) {
									if (loadBinds)
									elmt.getBinds().clear();
									JSONObject binds = (JSONObject) selects.get(elmt.getName());

									if (loadBinds)
									for (Object bind : binds.keySet()) {
										elmt.getBinds().add(new Bind(Integer.parseInt((bind + "").split(":")[0]), Integer.parseInt((bind + "").split(":")[1]), BindType.get(binds.get(bind) + "")));
									}
								}
							}
							
							JSONObject binds = (JSONObject) ((JSONObject) mods.get(set.getName())).get("binds");

							if (loadBinds)
							for (Object bind : binds.keySet()) {
								set.getBinds().add(new Bind(Integer.parseInt((bind + "").split(":")[0]), Integer.parseInt((bind + "").split(":")[1]), BindType.get(binds.get(bind) + "")));
							}
						} catch (Exception e) {
						}
					}
					
					if (mod instanceof Interface interfaceMod && !config.containsKey("ui")) {
						for (Element element : interfaceMod.getElements().getElements()) {
							UIElement ui = (UIElement) element;
							
							for (Setting set : ui.getSettings()) {
								if (loadBinds)
								set.getBinds().clear();
								try {
									if (!(set.getParent() instanceof UIElement parent)) return;
									
									loadSetting(set, mods);
									
									JSONObject binds = (JSONObject) ((JSONObject) mods.get(set.getName())).get("binds");

									if (loadBinds)
									for (Object bind : binds.keySet()) {
										set.getBinds().add(new Bind(Integer.parseInt((bind + "").split(":")[0]), Integer.parseInt((bind + "").split(":")[1]), BindType.get(binds.get(bind) + "")));
									}
								} catch (Exception e) {
								}
							}
						}
					}

					try {
						JSONObject binds = (JSONObject) mods.get("binds");

						if (loadBinds)
						for (Object bind : binds.keySet()) {
							mod.getBinds().add(new Bind(Integer.parseInt((bind + "").split(":")[0]), Integer.parseInt((bind + "").split(":")[1]), BindType.get(binds.get(bind) + "")));
						}
					
						mod.set((boolean) mods.get("enabled"));
					} catch (Exception e) {
						Debugger.print(e);
					}
				}
				boolean next = false;
				if (Player.isInGame())
					for (Select.Element elmt : window.getElement().getCategories().getElements()) {
						if (Category.RENDER.getDisplayName().equals(elmt.getName()) && !elmt.get()) {
							next = true;
						}
					}
				
				if (!next)
				try {
					JSONObject hud = (JSONObject) config.get("ui");
					
					for (Element element : rock.getModules().get(Interface.class).getElements().getElements()) {
						UIElement ui = (UIElement) element;
						
						JSONObject elmt = (JSONObject) hud.get(element.getName());
						
						for (Setting set : ui.getSettings()) {
							if (loadBinds)
							set.getBinds().clear();
							try {
								if (!(set.getParent() instanceof UIElement parent)) return;
								
								loadSetting(set, elmt);
								
								JSONObject binds = (JSONObject) ((JSONObject) elmt.get(set.getName())).get("binds");

								if (loadBinds)
								for (Object bind : binds.keySet()) {
									set.getBinds().add(new Bind(Integer.parseInt((bind + "").split(":")[0]), Integer.parseInt((bind + "").split(":")[1]), BindType.get(binds.get(bind) + "")));
								}
							} catch (Exception e) {
							}
						}
					}
				} catch (Exception e) {
					Debugger.print(e);
				}
				
				try {
					JSONObject esp = (JSONObject) config.get("esp");
					
					for (ESPElement elmt : rock.getEspSettingsHandler().getEspElements()) {
						JSONObject element = (JSONObject) esp.get(elmt.getName());
						
						elmt.setDirection((int) (long) element.get("direction"));
						elmt.setActive((boolean) element.get("active"));
					}
				} catch (Exception e) {
					Debugger.print(e);
				}
				
				try {
	               	rock.getAutoBuy().getItems().clear();

					JSONArray itemsArray = (JSONArray) config.get("items");
			        if (itemsArray != null) {
			            for (Object itemObj : itemsArray) {
			                JSONObject itemJson = (JSONObject) itemObj;
			                AutoBuyItem item = AutoBuyItem.fromJson(itemJson);
			               	rock.getAutoBuy().getItems().add(item);
			            }
			        }
				} catch (Exception e) {
					Debugger.print(e);
				}
		        
				if (!silent)
					rock.getAlertHandler().alert("Конфиг загружен", AlertType.INFO);
				EventHandler.resetConfig();
				current = name;

			} catch (Exception e) {
				Debugger.print(e);
				rock.getAlertHandler().alert("Ошибка при загрузке", AlertType.ERROR);
			}
			if (!silent)
				alert.hide();
		});
	}
	*/
	
	/*
	public void delete(String name) {
		String user = rock.getUser().getName();
		
		ThreadManager.run(() -> {
			HashMap<String, String> map = new HashMap<>();
			
			map.put("id", KeyGeneration.encrypt("" + rock.getUser().getId()));
			map.put("cfgname", KeyGeneration.encrypt(name));
			
			rock.getAlertHandler().alert(KeyGeneration.decrypt(Web.protectedPostRequest(map, "https://rockstar.moscow/api/v1/premium/client/script/delete.php").trim()), AlertType.INFO);
		});
	}
	*/
	
	@NativeInclude
	public void clear() {
//		String user = rock.getUser().getName();
		
//		HashMap<String, String> map = new HashMap<>();
		
//		map.put("id", KeyGeneration.encrypt("" + rock.getUser().getId()));
		
//		Web.protectedPostRequest(map, "https://rockstar.moscow/api/v1/premium/client/script/clear.php").trim();
	}
	
	/*
	public void list() {
		String user = rock.getUser().getName();
		
		Alert alert = rock.getAlertHandler().alert("Получаем список конфигов", AlertType.WAIT);
		
		ThreadManager.run(() -> {
			HashMap<String, String> map = new HashMap<>();
			
			map.put("id", KeyGeneration.encrypt("" + rock.getUser().getId()));
			
			String str = Web.protectedPostRequest(map, "https://rockstar.moscow/api/v1/premium/client/config/list.php").trim() + "";
			try {
				if (str.contains("PHP_EOL")) {
					String[] msgs = str.split("PHP_EOL");
					int i = 0;
					for (String cfg : msgs) {
						if (cfg.contains("[")) {
							Chat.msg(TextFormatting.AQUA + "[Rockstar] " + TextFormatting.RESET + cfg, String.format("%sЗагрузить конфиг %s%s", TextFormatting.WHITE, TextFormatting.UNDERLINE, cfg.split("§f")[1]), () -> {
								clicked(cfg);
							});
						} else {
							Chat.msg(cfg);
						}
					}
				} else {
					mc.player.addChatMessage(TextFormatting.AQUA + "[Rockstar] " + TextFormatting.RESET + str);
				}
				rock.getAlertHandler().alert("Список получен", AlertType.INFO);
			} catch (Exception e) {
				rock.getAlertHandler().alert("Ошибка при получении списка", AlertType.ERROR);
				Debugger.print(e);
			}
			alert.hide();
		});
	}
	*/
}
	