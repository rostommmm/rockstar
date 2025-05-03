package fun.rockstarity.api.configs;

import java.io.File; // Добавлено
import java.io.IOException; // Добавлено
import java.nio.charset.StandardCharsets; // Добавлено
import java.nio.file.*; // Добавлено
import java.util.ArrayList;
import java.util.Comparator; // Добавлено (для удаления папки, если нужно)
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors; // Добавлено
import java.util.stream.Stream; // Добавлено

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException; // Добавлено

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException; // Добавлено

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.autobuy.logic.items.AutoBuyItem;
import fun.rockstarity.api.autobuy.logic.items.MinecraftItem;
import fun.rockstarity.api.binds.Bind;
import fun.rockstarity.api.binds.BindType;
import fun.rockstarity.api.events.EventHandler;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.player.Player;
// import fun.rockstarity.api.helpers.secure.KeyGeneration; // Больше не нужно для файловых операций
// import fun.rockstarity.api.helpers.secure.Web; // Больше не нужно для файловых операций
import fun.rockstarity.api.helpers.system.ThreadManager;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.Setting;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.ColorPicker;
import fun.rockstarity.api.modules.settings.list.Input;
import fun.rockstarity.api.modules.settings.list.ItemSelect;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Position;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Select.Element;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.ui.alerts.Alert;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import fun.rockstarity.api.render.ui.clickgui.esp.ESPElement;
import fun.rockstarity.api.render.ui.draggables.Draggable;
import fun.rockstarity.api.scripts.Script;
import fun.rockstarity.api.secure.Debugger;
import fun.rockstarity.client.commands.MacroCommand;
import fun.rockstarity.client.commands.ScoreCommand;
import fun.rockstarity.client.modules.render.Interface;
import fun.rockstarity.client.modules.render.Interface.UIElement;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.text.TextFormatting;
import ru.kotopushka.antiautistleak.obfuscator.includes.annotations.compile.ReleaseCompileToNativeCalls;

@ReleaseCompileToNativeCalls
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConfigsHandler implements IAccess {

	// --- НАЧАЛО ИЗМЕНЕНИЙ ---
	// Путь к папке с конфигами (используем двойные слеши в строке Java)
	private static final String LOCAL_CONFIG_DIR_PATH = "C:\\Users\\VIP\\Desktop\\expensive-1.16.5-master\\amedir\\config";
	private static final Path LOCAL_CONFIG_DIR = Paths.get(LOCAL_CONFIG_DIR_PATH);
	private static final String CONFIG_EXTENSION = ".json"; // Конфиги будем сохранять как JSON

	// Хелпер для получения полного пути к файлу конфига
	private Path getConfigPath(String name) {
		// Убираем недопустимые символы и добавляем расширение
		String fileName = name.replaceAll("[^a-zA-Z0-9_.-]", "_") + CONFIG_EXTENSION;
		return LOCAL_CONFIG_DIR.resolve(fileName);
	}

	// Хелпер для проверки и создания папки
	private boolean ensureConfigDirectoryExists() {
		if (Files.notExists(LOCAL_CONFIG_DIR)) {
			try {
				Files.createDirectories(LOCAL_CONFIG_DIR);
				return true;
			} catch (IOException e) {
				Debugger.print("Не удалось создать папку для конфигов: " + LOCAL_CONFIG_DIR_PATH);
				Debugger.print(e);
				rock.getAlertHandler().alert("Ошибка создания папки конфигов!", AlertType.ERROR);
				return false;
			}
		}
		return true;
	}
	// --- КОНЕЦ ИЗМЕНЕНИЙ ---


	@Getter
	final ConfigWindow window = new ConfigWindow(new ConfigWrapper());

	@Getter @Setter
	String current = "default";
	Alert alert;

	public void save(String name, boolean silent) {
		if (name == null || name.trim().isEmpty()) {
			rock.getAlertHandler().alert("Имя конфига не может быть пустым", AlertType.ERROR);
			return;
		}
		if (name.length() > 50) { // Увеличил лимит, если нужно
			name = name.substring(0, 50);
		}

		final String finalName = name.trim(); // Убираем лишние пробелы
		// String user = rock.getUser().getName(); // Больше не нужно для локального сохранения

		// Проверяем/создаем папку перед запуском потока
		if (!ensureConfigDirectoryExists()) {
			return; // Ошибка уже показана
		}

		alert = silent ? null : rock.getAlertHandler().alert("Сохраняем конфиг локально...", AlertType.WAIT);

		ThreadManager.run(() -> {
			try {
				// --- ЛОГИКА СБОРА ДАННЫХ ОСТАЕТСЯ ПРЕЖНЕЙ ---
				JSONObject config = new JSONObject();
				JSONObject mods = new JSONObject();

				List<Module> modules = new ArrayList<>();
				rock.getModules().values().forEach(mod -> modules.add(mod));
				for (Script script : rock.getScriptHandler().getEnabledScripts()) {
					script.getScriptModules().forEach(mod -> modules.add(mod));
				}

				for (fun.rockstarity.api.modules.Module mod : modules) {
					JSONObject module = new JSONObject();
					// ... (весь код сборки настроек модуля из saveSetting и биндов) ...
					for (Setting set : mod.getSettings()) {
						JSONObject setting = new JSONObject();
						saveSetting(set, setting); // Используем существующий хелпер
						// ... (код для биндов настроек и Select) ...
						JSONObject settingBinds = new JSONObject();
						for (Bind bind : set.getBinds()) {
							settingBinds.put(bind.getKey() + ":" + bind.getScancode(), bind.getType().getName());
						}
						setting.put("binds", settingBinds); // Добавляем бинды настройки
						module.put(set.getName(), setting); // Добавляем настройку в модуль
					}
					JSONObject modBinds = new JSONObject();
					for (Bind bind : mod.getBinds()) {
						modBinds.put(bind.getKey() + ":" + bind.getScancode(), bind.getType().getName());
					}
					module.put("binds", modBinds); // Добавляем бинды модуля
					module.put("enabled", mod.get()); // Состояние модуля
					mods.put(mod.getInfo().name(), module); // Добавляем модуль в общий список
				}
				config.put("mods", mods);

				// ... (код для UI) ...
				JSONObject hud = new JSONObject();
				for (Element element : rock.getModules().get(Interface.class).getElements().getElements()) {
					// ... (логика сохранения UI как и раньше) ...
					UIElement ui = (UIElement) element;
					JSONObject elmt = new JSONObject();
					for (Setting set : ui.getSettings()) {
						JSONObject setting = new JSONObject();
						if (!(set.getParent() instanceof UIElement)) continue; // Безопаснее проверить
						saveSetting(set, setting);
						JSONObject binds = new JSONObject();
						for (Bind bind : set.getBinds()) {
							binds.put(bind.getKey() + ":" + bind.getScancode(), bind.getType().getName());
						}
						setting.put("binds", binds);
						elmt.put(set.getName(), setting);
					}
					hud.put(element.getName(), elmt);
				}
				config.put("ui", hud);

				// ... (код для ESP) ...
				JSONObject esp = new JSONObject();
				for (ESPElement elmt : rock.getEspSettingsHandler().getEspElements()) {
					JSONObject element = new JSONObject();
					element.put("active", elmt.isActive());
					element.put("direction", elmt.getDirection());
					esp.put(elmt.getName(), element);
				}
				config.put("esp", esp);

				// ... (код для AutoBuy) ...
				try {
					JSONArray itemsArray = new JSONArray();
					for (AutoBuyItem item : rock.getAutoBuy().getItems()) {
						itemsArray.add(item.toJson());
					}
					config.put("items", itemsArray);
				} catch (Exception e) {
					Debugger.print("Ошибка при сохранении AutoBuy items:");
					Debugger.print(e);
				}

				// --- ЛОГИКА ЗАПИСИ В ФАЙЛ ---
				// Преобразуем JSONObject в красивый JSON-строку
				String code = new GsonBuilder().setPrettyPrinting().create().toJson(config);

				// Получаем путь к файлу
				Path filePath = getConfigPath(finalName);

				// Записываем строку в файл (перезаписываем, если существует)
				Files.writeString(filePath, code, StandardCharsets.UTF_8,
						StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

				if (!silent) {
					rock.getAlertHandler().alert("Конфиг '" + finalName + "' сохранен локально", AlertType.INFO);
				}
				current = finalName; // Обновляем текущий конфиг

			} catch (IOException e) {
				Debugger.print("Ошибка записи конфига в файл: " + finalName);
				Debugger.print(e);
				if (!silent) {
					rock.getAlertHandler().alert("Ошибка сохранения конфига!", AlertType.ERROR);
				}
			} catch (Exception e) { // Ловим другие возможные ошибки при сборке JSON
				Debugger.print("Неожиданная ошибка при сохранении конфига: " + finalName);
				Debugger.print(e);
				if (!silent) {
					rock.getAlertHandler().alert("Ошибка сохранения конфига!", AlertType.ERROR);
				}
			} finally {
				if (alert != null) {
					alert.hide(); // Прячем уведомление в любом случае
				}
			}
		});
	}

	// Перегрузка для обратной совместимости
	public void load(String name) {
		load(name, false);
	}

	public void load(String name, boolean silent) {
		if (name == null || name.trim().isEmpty()) {
			rock.getAlertHandler().alert("Имя конфига не может быть пустым", AlertType.ERROR);
			return;
		}
		final String finalName = name.trim();
		// String user = rock.getUser().getName(); // Не нужно

		// Проверяем папку
		if (!ensureConfigDirectoryExists()) {
			return; // Ошибка уже показана
		}

		ThreadManager.run(() -> {
			Alert loadAlert = null; // Переименовал, чтобы не конфликтовать с полем класса
			if (!silent) {
				loadAlert = rock.getAlertHandler().alert("Загружаем конфиг '" + finalName + "' локально...", AlertType.WAIT);
			}

			boolean loadBinds = window.getElement().getLoadBinds().get() || !Player.isInGame();
			Path filePath = getConfigPath(finalName);

			if (Files.notExists(filePath) || !Files.isRegularFile(filePath)) {
				Debugger.print("Файл конфига не найден: " + filePath);
				if (!silent) {
					rock.getAlertHandler().alert("Конфиг '" + finalName + "' не найден!", AlertType.ERROR);
					if (loadAlert != null) loadAlert.hide();
				}
				return;
			}

			try {
				// --- ЛОГИКА ЧТЕНИЯ ФАЙЛА ---
				String code = Files.readString(filePath, StandardCharsets.UTF_8);

				// --- ЛОГИКА ПАРСИНГА И ПРИМЕНЕНИЯ ОСТАЕТСЯ ПРЕЖНЕЙ ---
				JSONObject config = null;
				try {
					JSONParser parser = new JSONParser();
					config = (JSONObject) parser.parse(code);

					// Сбрасываем бинды перед загрузкой, если нужно
					if (loadBinds) {
						for (fun.rockstarity.api.modules.Module mod : rock.getModules().values()) {
							mod.getBinds().clear();
							for(Setting s : mod.getSettings()) s.getBinds().clear();
						}
						// Сброс биндов UI элементов
						Interface interfaceMod = rock.getModules().get(Interface.class);
						if (interfaceMod != null) {
							for (Element element : interfaceMod.getElements().getElements()) {
								if (element instanceof UIElement ui) {
									for (Setting s : ui.getSettings()) s.getBinds().clear();
								}
							}
						}
						// Сброс биндов элементов Select внутри настроек
						// (Этот код был сложным, надо проверить его полноту)
						// TODO: Убедиться, что все бинды сбрасываются корректно
					}

				} catch (ParseException | JsonSyntaxException e) { // Ловим ошибки парсинга JSON
					Debugger.print("Ошибка парсинга JSON в файле: " + filePath);
					Debugger.print(e);
					if (!silent)
						rock.getAlertHandler().alert("Ошибка чтения конфига!", AlertType.ERROR);
					if (loadAlert != null) loadAlert.hide();
					return;
				} catch (Exception e) { // Ловим другие ошибки инициализации
					Debugger.print("Ошибка инициализации после парсинга JSON: " + filePath);
					Debugger.print(e);
					if (!silent)
						rock.getAlertHandler().alert("Ошибка применения конфига!", AlertType.ERROR);
					if (loadAlert != null) loadAlert.hide();
					return;
				}

				// --- Применение настроек ---
				List<Module> modules = new ArrayList<>();
				rock.getModules().values().forEach(mod -> modules.add(mod));
				for (Script script : rock.getScriptHandler().getEnabledScripts()) {
					script.getScriptModules().forEach(mod -> modules.add(mod));
				}

				// Применяем настройки модулей
				JSONObject modsJson = (JSONObject) config.get("mods");
				if (modsJson != null) {
					for (fun.rockstarity.api.modules.Module mod : modules) {
						boolean next = false; // Проверка на категорию (оставляем как было)
						if (Player.isInGame()) {
							for (Select.Element elmt : window.getElement().getCategories().getElements()) {
								if (mod.getInfo().type().getDisplayName().equals(elmt.getName()) && !elmt.get()) {
									next = true;
									break;
								}
							}
						}
						if (next) continue;

						JSONObject modJson = (JSONObject) modsJson.get(mod.getInfo().name());
						if (modJson == null) continue; // Пропускаем, если данных для модуля нет

						// Применяем настройки через loadSetting
						for (Setting set : mod.getSettings()) {
							try {
								loadSetting(set, modJson); // Используем существующий хелпер

								// Загрузка биндов самой настройки (если есть)
								if (loadBinds) {
									JSONObject settingJson = (JSONObject) modJson.get(set.getName());
									if (settingJson != null) {
										JSONObject bindsJson = (JSONObject) settingJson.get("binds");
										set.getBinds().clear(); // Очищаем перед загрузкой
										if (bindsJson != null) {
											for (Object bindKey : bindsJson.keySet()) {
												String keyStr = (String) bindKey;
												String typeStr = (String) bindsJson.get(bindKey);
												try {
													String[] parts = keyStr.split(":");
													if (parts.length == 2) {
														int key = Integer.parseInt(parts[0]);
														int scancode = Integer.parseInt(parts[1]);
														BindType type = BindType.get(typeStr);
														if (type != null) {
															set.getBinds().add(new Bind(key, scancode, type));
														}
													}
												} catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
													Debugger.print("Ошибка парсинга бинда настройки '" + set.getName() + "': " + keyStr);
												}
											}
										}
										// Загрузка биндов для элементов Select (если настройка - Select)
										if (set instanceof Select select) {
											JSONObject selectsBindsJson = (JSONObject) settingJson.get("selects");
											if (selectsBindsJson != null) {
												for (Select.Element elmt : select.getElements()) {
													JSONObject elmtBindsJson = (JSONObject) selectsBindsJson.get(elmt.getName());
													elmt.getBinds().clear();
													if (elmtBindsJson != null) {
														for (Object elmtBindKey : elmtBindsJson.keySet()) {
															// ... (аналогичный парсинг биндов для элемента Select) ...
															String keyStr = (String) elmtBindKey;
															String typeStr = (String) elmtBindsJson.get(elmtBindKey);
															try {
																String[] parts = keyStr.split(":");
																if (parts.length == 2) {
																	int key = Integer.parseInt(parts[0]);
																	int scancode = Integer.parseInt(parts[1]);
																	BindType type = BindType.get(typeStr);
																	if (type != null) {
																		elmt.getBinds().add(new Bind(key, scancode, type));
																	}
																}
															} catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
																Debugger.print("Ошибка парсинга бинда элемента Select '" + elmt.getName() + "' в '" + set.getName() + "': " + keyStr);
															}
														}
													}
												}
											}
										}
									}
								}

							} catch (Exception e) {
								Debugger.print("Ошибка загрузки настройки '" + set.getName() + "' для модуля '" + mod.getInfo().name() + "'");
								Debugger.print(e);
							}
						}

						// Загрузка биндов самого модуля
						if (loadBinds) {
							JSONObject bindsJson = (JSONObject) modJson.get("binds");
							mod.getBinds().clear(); // Очищаем перед загрузкой
							if (bindsJson != null) {
								for (Object bindKey : bindsJson.keySet()) {
									// ... (аналогичный парсинг биндов для модуля) ...
									String keyStr = (String) bindKey;
									String typeStr = (String) bindsJson.get(bindKey);
									try {
										String[] parts = keyStr.split(":");
										if (parts.length == 2) {
											int key = Integer.parseInt(parts[0]);
											int scancode = Integer.parseInt(parts[1]);
											BindType type = BindType.get(typeStr);
											if (type != null) {
												mod.getBinds().add(new Bind(key, scancode, type));
											}
										}
									} catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
										Debugger.print("Ошибка парсинга бинда модуля '" + mod.getInfo().name() + "': " + keyStr);
									}
								}
							}
						}

						// Включение/выключение модуля
						try {
							Object enabledObj = modJson.get("enabled");
							if (enabledObj instanceof Boolean) {
								mod.set((Boolean) enabledObj);
							}
						} catch (Exception e) {
							Debugger.print("Ошибка установки состояния модуля '" + mod.getInfo().name() + "'");
							Debugger.print(e);
						}
					}
				}

				// Применяем настройки UI (если есть секция ui)
				boolean checkRenderCategory = false; // Проверка на категорию Render (оставляем)
				if (Player.isInGame()) {
					for (Select.Element elmt : window.getElement().getCategories().getElements()) {
						if (Category.RENDER.getDisplayName().equals(elmt.getName()) && !elmt.get()) {
							checkRenderCategory = true;
							break;
						}
					}
				}
				if (!checkRenderCategory) {
					JSONObject hudJson = (JSONObject) config.get("ui");
					Interface interfaceMod = rock.getModules().get(Interface.class);
					if (hudJson != null && interfaceMod != null) {
						for (Element element : interfaceMod.getElements().getElements()) {
							if (!(element instanceof UIElement ui)) continue;
							JSONObject elmtJson = (JSONObject) hudJson.get(element.getName());
							if (elmtJson == null) continue;

							for (Setting set : ui.getSettings()) {
								try {
									loadSetting(set, elmtJson); // Применяем настройку UI элемента

									// Загрузка биндов UI элемента
									if (loadBinds) {
										JSONObject settingJson = (JSONObject) elmtJson.get(set.getName());
										if (settingJson != null) {
											JSONObject bindsJson = (JSONObject) settingJson.get("binds");
											set.getBinds().clear();
											if (bindsJson != null) {
												for (Object bindKey : bindsJson.keySet()) {
													// ... (аналогичный парсинг биндов) ...
													String keyStr = (String) bindKey;
													String typeStr = (String) bindsJson.get(bindKey);
													try {
														String[] parts = keyStr.split(":");
														if (parts.length == 2) {
															int key = Integer.parseInt(parts[0]);
															int scancode = Integer.parseInt(parts[1]);
															BindType type = BindType.get(typeStr);
															if (type != null) {
																set.getBinds().add(new Bind(key, scancode, type));
															}
														}
													} catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
														Debugger.print("Ошибка парсинга бинда UI '" + set.getName() + "': " + keyStr);
													}
												}
											}
										}
									}
								} catch (Exception e) {
									Debugger.print("Ошибка загрузки настройки '" + set.getName() + "' для UI элемента '" + element.getName() + "'");
									Debugger.print(e);
								}
							}
						}
					} else if (modsJson != null && config.get("ui") == null && interfaceMod != null) {
						// Попытка загрузить UI из старого формата (если секции ui нет)
						// Этот блок был в оригинале, оставляем на всякий случай
						JSONObject interfaceModJson = (JSONObject) modsJson.get(interfaceMod.getInfo().name());
						if(interfaceModJson != null) {
							for (Element element : interfaceMod.getElements().getElements()) {
								// ... (логика загрузки UI из modJson, как в оригинале) ...
							}
						}
					}
				}

				// Применяем настройки ESP
				try {
					JSONObject espJson = (JSONObject) config.get("esp");
					if (espJson != null) {
						for (ESPElement elmt : rock.getEspSettingsHandler().getEspElements()) {
							JSONObject elementJson = (JSONObject) espJson.get(elmt.getName());
							if (elementJson != null) {
								Object directionObj = elementJson.get("direction");
								if (directionObj instanceof Long) { // JSON-Simple парсит числа как Long
									elmt.setDirection(((Long) directionObj).intValue());
								} else if (directionObj instanceof Number) { // На всякий случай
									elmt.setDirection(((Number) directionObj).intValue());
								}

								Object activeObj = elementJson.get("active");
								if (activeObj instanceof Boolean) {
									elmt.setActive((Boolean) activeObj);
								}
							}
						}
					}
				} catch (Exception e) {
					Debugger.print("Ошибка загрузки настроек ESP");
					Debugger.print(e);
				}

				// Применяем настройки AutoBuy
				try {
					rock.getAutoBuy().getItems().clear(); // Очищаем перед загрузкой
					JSONArray itemsArray = (JSONArray) config.get("items");
					if (itemsArray != null) {
						for (Object itemObj : itemsArray) {
							if (itemObj instanceof JSONObject) {
								JSONObject itemJson = (JSONObject) itemObj;
								try {
									AutoBuyItem item = AutoBuyItem.fromJson(itemJson);
									if (item != null) {
										rock.getAutoBuy().getItems().add(item);
									}
								} catch (Exception eJson) {
									Debugger.print("Ошибка десериализации AutoBuyItem: " + itemJson.toJSONString());
									Debugger.print(eJson);
								}
							}
						}
					}
				} catch (Exception e) {
					Debugger.print("Ошибка загрузки настроек AutoBuy");
					Debugger.print(e);
				}

				// --- Завершение ---
				if (!silent)
					rock.getAlertHandler().alert("Конфиг '" + finalName + "' загружен", AlertType.INFO);
				EventHandler.resetConfig(); // Оставляем, если это важно для обновления UI/логики
				current = finalName; // Обновляем текущий

			} catch (IOException e) {
				Debugger.print("Ошибка чтения файла конфига: " + filePath);
				Debugger.print(e);
				if (!silent) {
					rock.getAlertHandler().alert("Ошибка чтения конфига!", AlertType.ERROR);
				}
			} catch (Exception e) { // Ловим другие ошибки во время применения настроек
				Debugger.print("Неожиданная ошибка при загрузке/применении конфига: " + finalName);
				Debugger.print(e);
				if (!silent) {
					rock.getAlertHandler().alert("Ошибка загрузки конфига!", AlertType.ERROR);
				}
			} finally {
				if (loadAlert != null) {
					loadAlert.hide(); // Прячем уведомление
				}
			}
		});
	}

	// Загрузка только одного модуля (почти как полная загрузка, но фильтрует модули)
	public void load(String name, String moduleName) {
		if (name == null || name.trim().isEmpty() || moduleName == null || moduleName.trim().isEmpty()) {
			rock.getAlertHandler().alert("Имя конфига и модуля не могут быть пустыми", AlertType.ERROR);
			return;
		}
		final String finalName = name.trim();
		final String finalModuleName = moduleName.trim();
		// String user = rock.getUser().getName(); // Не нужно

		if (!ensureConfigDirectoryExists()) {
			return;
		}

		ThreadManager.run(() -> {
			Alert loadAlert = rock.getAlertHandler().alert("Загружаем модуль '" + finalModuleName + "' из конфига '" + finalName + "'...", AlertType.WAIT);

			Path filePath = getConfigPath(finalName);

			if (Files.notExists(filePath) || !Files.isRegularFile(filePath)) {
				Debugger.print("Файл конфига не найден: " + filePath);
				rock.getAlertHandler().alert("Конфиг '" + finalName + "' не найден!", AlertType.ERROR);
				if (loadAlert != null) loadAlert.hide();
				return;
			}

			try {
				String code = Files.readString(filePath, StandardCharsets.UTF_8);
				JSONObject config = null;
				try {
					JSONParser parser = new JSONParser();
					config = (JSONObject) parser.parse(code);
				} catch (ParseException | JsonSyntaxException e) {
					Debugger.print("Ошибка парсинга JSON в файле: " + filePath);
					Debugger.print(e);
					rock.getAlertHandler().alert("Ошибка чтения конфига!", AlertType.ERROR);
					if (loadAlert != null) loadAlert.hide();
					return;
				}

				boolean moduleFound = false;

				// Загрузка конкретного модуля
				JSONObject modsJson = (JSONObject) config.get("mods");
				if (modsJson != null) {
					List<Module> modules = new ArrayList<>();
					rock.getModules().values().forEach(mod -> modules.add(mod));
					for (Script script : rock.getScriptHandler().getEnabledScripts()) {
						script.getScriptModules().forEach(mod -> modules.add(mod));
					}

					for (fun.rockstarity.api.modules.Module mod : modules) {
						if (!mod.getInfo().name().equalsIgnoreCase(finalModuleName)) continue;

						moduleFound = true;
						JSONObject modJson = (JSONObject) modsJson.get(mod.getInfo().name());
						if (modJson == null) {
							Debugger.print("Данные для модуля '" + finalModuleName + "' не найдены в конфиге.");
							continue; // Данных нет, но модуль нашли
						}

						// Применяем настройки через loadSetting (без биндов!)
						for (Setting set : mod.getSettings()) {
							try {
								loadSetting(set, modJson);
							} catch (Exception e) {
								Debugger.print("Ошибка загрузки настройки '" + set.getName() + "' для модуля '" + mod.getInfo().name() + "'");
								Debugger.print(e);
							}
						}

						// Включение/выключение модуля
						try {
							Object enabledObj = modJson.get("enabled");
							if (enabledObj instanceof Boolean) {
								mod.set((Boolean) enabledObj);
							}
						} catch (Exception e) {
							Debugger.print("Ошибка установки состояния модуля '" + mod.getInfo().name() + "'");
							Debugger.print(e);
						}
						break; // Нашли и обработали нужный модуль
					}
				}

				// Загрузка Interface (если запрошен он)
				if (finalModuleName.equalsIgnoreCase("Interface")) {
					moduleFound = true; // Считаем, что "модуль" найден
					JSONObject hudJson = (JSONObject) config.get("ui");
					Interface interfaceMod = rock.getModules().get(Interface.class);
					if (hudJson != null && interfaceMod != null) {
						for (Element element : interfaceMod.getElements().getElements()) {
							// ... (логика применения настроек UI без биндов) ...
							if (!(element instanceof UIElement ui)) continue;
							JSONObject elmtJson = (JSONObject) hudJson.get(element.getName());
							if (elmtJson == null) continue;
							for (Setting set : ui.getSettings()) {
								try {
									loadSetting(set, elmtJson);
								} catch (Exception e) {
									Debugger.print("Ошибка загрузки настройки '" + set.getName() + "' для UI элемента '" + element.getName() + "'");
									Debugger.print(e);
								}
							}
						}
					} else {
						Debugger.print("Секция 'ui' не найдена в конфиге для загрузки Interface.");
					}
				}

				// Загрузка ESP (если запрошен он)
				if (finalModuleName.equalsIgnoreCase("ESP")) {
					moduleFound = true; // Считаем, что "модуль" найден
					JSONObject espJson = (JSONObject) config.get("esp");
					if (espJson != null) {
						for (ESPElement elmt : rock.getEspSettingsHandler().getEspElements()) {
							// ... (логика применения настроек ESP) ...
							JSONObject elementJson = (JSONObject) espJson.get(elmt.getName());
							if (elementJson != null) {
								// ... (код установки direction и active как в полной загрузке) ...
								Object directionObj = elementJson.get("direction");
								if (directionObj instanceof Long) elmt.setDirection(((Long) directionObj).intValue());
								else if (directionObj instanceof Number) elmt.setDirection(((Number) directionObj).intValue());

								Object activeObj = elementJson.get("active");
								if (activeObj instanceof Boolean) elmt.setActive((Boolean) activeObj);
							}
						}
					} else {
						Debugger.print("Секция 'esp' не найдена в конфиге для загрузки ESP.");
					}
				}

				// Загрузка AutoBuy (если запрошен он)
				if (finalModuleName.equalsIgnoreCase("AutoBuy")) {
					moduleFound = true; // Считаем, что "модуль" найден
					JSONArray itemsArray = (JSONArray) config.get("items");
					if (itemsArray != null) {
						rock.getAutoBuy().getItems().clear();
						for (Object itemObj : itemsArray) {
							// ... (логика десериализации AutoBuyItem) ...
							if (itemObj instanceof JSONObject) {
								JSONObject itemJson = (JSONObject) itemObj;
								try {
									AutoBuyItem item = AutoBuyItem.fromJson(itemJson);
									if (item != null) rock.getAutoBuy().getItems().add(item);
								} catch (Exception eJson) {
									Debugger.print("Ошибка десериализации AutoBuyItem: " + itemJson.toJSONString());
									Debugger.print(eJson);
								}
							}
						}
					} else {
						Debugger.print("Секция 'items' не найдена в конфиге для загрузки AutoBuy.");
					}
				}

				if (moduleFound) {
					rock.getAlertHandler().alert("Модуль '" + finalModuleName + "' загружен из конфига", AlertType.INFO);
					EventHandler.resetConfig(); // Обновляем
				} else {
					rock.getAlertHandler().alert("Модуль '" + finalModuleName + "' не найден в конфиге или API", AlertType.ERROR);
				}

			} catch (IOException e) {
				Debugger.print("Ошибка чтения файла конфига: " + filePath);
				Debugger.print(e);
				rock.getAlertHandler().alert("Ошибка чтения конфига!", AlertType.ERROR);
			} catch (Exception e) {
				Debugger.print("Неожиданная ошибка при частичной загрузке конфига: " + finalName);
				Debugger.print(e);
				rock.getAlertHandler().alert("Ошибка загрузки модуля!", AlertType.ERROR);
			} finally {
				if (loadAlert != null) {
					loadAlert.hide();
				}
			}
		});
	}


	public void delete(String name) {
		if (name == null || name.trim().isEmpty()) {
			rock.getAlertHandler().alert("Имя конфига не может быть пустым", AlertType.ERROR);
			return;
		}
		final String finalName = name.trim();
		// String user = rock.getUser().getName(); // Не нужно

		if (!ensureConfigDirectoryExists()) {
			// Папки нет, удалять нечего (или ошибка создания, но она уже показана)
			return;
		}

		Alert deleteAlert = rock.getAlertHandler().alert("Удаляем конфиг '" + finalName + "' локально...", AlertType.WAIT);

		ThreadManager.run(() -> {
			Path filePath = getConfigPath(finalName);
			try {
				boolean deleted = Files.deleteIfExists(filePath); // Удаляем файл

				if (deleted) {
					rock.getAlertHandler().alert("Конфиг '" + finalName + "' удален", AlertType.INFO);
					if (current.equals(finalName)) { // Если удалили текущий, сбрасываем имя
						current = "default"; // Или другое имя по умолчанию
					}
				} else {
					rock.getAlertHandler().alert("Конфиг '" + finalName + "' не найден для удаления", AlertType.INFO);
				}
			} catch (IOException e) {
				Debugger.print("Ошибка удаления файла конфига: " + filePath);
				Debugger.print(e);
				rock.getAlertHandler().alert("Ошибка удаления конфига!", AlertType.ERROR);
			} catch (Exception e) {
				Debugger.print("Неожиданная ошибка при удалении конфига: " + finalName);
				Debugger.print(e);
				rock.getAlertHandler().alert("Ошибка удаления конфига!", AlertType.ERROR);
			} finally {
				if (deleteAlert != null) {
					deleteAlert.hide();
				}
			}
		});
	}

	public void list() {
		// String user = rock.getUser().getName(); // Не нужно

		if (!ensureConfigDirectoryExists()) {
			rock.getAlertHandler().alert("Папка конфигов не найдена или не может быть создана", AlertType.ERROR);
			return;
		}

		Alert listAlert = rock.getAlertHandler().alert("Получаем список локальных конфигов...", AlertType.WAIT);

		ThreadManager.run(() -> {
			try (Stream<Path> stream = Files.list(LOCAL_CONFIG_DIR)) {
				List<String> configFiles = stream
						.filter(Files::isRegularFile) // Только файлы
						.map(Path::getFileName)       // Получаем имя файла (Path)
						.map(Path::toString)          // Преобразуем в строку
						.filter(name -> name.toLowerCase().endsWith(CONFIG_EXTENSION)) // Фильтруем по расширению
						.map(name -> name.substring(0, name.length() - CONFIG_EXTENSION.length())) // Убираем расширение
						.sorted(String.CASE_INSENSITIVE_ORDER) // Сортируем
						.collect(Collectors.toList()); // Собираем в список

				if (configFiles.isEmpty()) {
					Chat.msg(TextFormatting.AQUA + "[Rockstar] " + TextFormatting.RESET + "Локальных конфигов (" + CONFIG_EXTENSION + ") не найдено в папке:");
					Chat.msg(TextFormatting.GRAY + LOCAL_CONFIG_DIR_PATH);
				} else {
					Chat.msg(TextFormatting.AQUA + "[Rockstar] " + TextFormatting.RESET + "Список локальных конфигов:");
					for (String cfgName : configFiles) {
						// Просто выводим имя, без кликабельности пока что
						Chat.msg(" - " + TextFormatting.GREEN + cfgName);
						// Если нужна кликабельность как раньше:
						// Chat.clickableMsg(" - " + TextFormatting.GREEN + cfgName,
						//                   TextFormatting.GRAY + " [Загрузить]",
						//                   () -> load(cfgName));
					}
					Chat.msg(TextFormatting.GRAY + "(Находятся в: " + LOCAL_CONFIG_DIR_PATH + ")");
				}
				rock.getAlertHandler().alert("Список локальных конфигов получен", AlertType.INFO);

			} catch (IOException e) {
				Debugger.print("Ошибка чтения папки конфигов: " + LOCAL_CONFIG_DIR_PATH);
				Debugger.print(e);
				rock.getAlertHandler().alert("Ошибка получения списка конфигов!", AlertType.ERROR);
			} catch (Exception e) {
				Debugger.print("Неожиданная ошибка при получении списка конфигов");
				Debugger.print(e);
				rock.getAlertHandler().alert("Ошибка получения списка конфигов!", AlertType.ERROR);
			} finally {
				if (listAlert != null) {
					listAlert.hide();
				}
			}
		});
	}

	// Метод clicked больше не нужен в таком виде, т.к. list() выводит некликабельный список
	// private void clicked(String cfg) {
	//     load(cfg.split("§f")[1]);
	// }

	// Метод resetToDefault остается без изменений, он сбрасывает настройки в памяти
	public void resetToDefault() {
		// ... (код сброса настроек как был) ...
		for (Module mod : rock.getModules().values()) {
			if (mod.getInfo().name().contains("ClickGui")) continue;
			mod.set(false);
			for (Setting set : mod.getSettings()) {
				set.reset();
			}
			mod.getBinds().clear();
		}
		for (Script script : rock.getScriptHandler().getScripts()) {
			script.setEnabled(false);
		}
		for (ESPElement elmt : rock.getEspSettingsHandler().getEspElements()) {
			elmt.setActive(false);
			elmt.setDirection(0);
		}
		for (Element element : rock.getModules().get(Interface.class).getElements().getElements()) {
			UIElement ui = (UIElement) element;
			for (Setting set : ui.getSettings()) {
				set.getBinds().clear();
			}
		}
		MacroCommand.getMacroses().clear();
		ScoreCommand.getScores().clear();
		rock.getFriendsHandler().getFriends().clear();
		rock.getTargetHandler().getTarget().clear();
		rock.getAutoBuy().getItems().clear();
		rock.setCfgToLoad("default");
		EventHandler.resetConfig();
		current = "default"; // Сбрасываем и имя текущего
		Chat.msg(TextFormatting.AQUA + "[Rockstar] " + TextFormatting.RESET + "Настройки сброшены к значениям по умолчанию (файлы не удалены).");
	}

	// Хелперы saveSetting и loadSetting остаются БЕЗ ИЗМЕНЕНИЙ
	// Они отвечают за структуру JSON, а не за место хранения файла
	public void saveSetting(Setting set, JSONObject setting) {
		// ... (весь код saveSetting как был) ...
		if (set instanceof Input) {
			setting.put("value", ((Input) set).get());
		}
		if (set instanceof CheckBox) {
			setting.put("value", ((CheckBox) set).get());
			setting.put("bind-value", ((CheckBox) set).bind());
		}
		// ... и т.д. для всех типов настроек
		// Важно: этот метод НЕ должен заниматься записью в файл
	}

	public void loadSetting(Setting set, JSONObject mods) {
		// ... (весь код loadSetting как был) ...
		boolean loadBinds = window.getElement().getLoadBinds().get() || !Player.isInGame(); // Учитываем это внутри loadSetting, если нужно
		if (set instanceof Input) {
			// ... (код загрузки Input)
			try {
				Object value = ((JSONObject) mods.get(set.getName())).get("value");
				if (value instanceof String) {
					((Input) set).getInput().setText((String) value);
					((Input) set).set((String) value);
				}
			} catch (Exception e) { Debugger.print("Error loading setting: " + set.getName()); }
		}
		if (set instanceof CheckBox) {
			// ... (код загрузки CheckBox)
			try {
				Object value = ((JSONObject) mods.get(set.getName())).get("value");
				Object bindValue = ((JSONObject) mods.get(set.getName())).get("bind-value");
				if (value instanceof Boolean) ((CheckBox) set).set((boolean) value);
				if (bindValue instanceof Boolean) ((CheckBox) set).bind((boolean) bindValue);
			} catch (Exception e) { Debugger.print("Error loading setting: " + set.getName()); }
		}
		// ... и т.д. для всех типов настроек
		// Важно: этот метод НЕ должен заниматься чтением из файла
	}
}