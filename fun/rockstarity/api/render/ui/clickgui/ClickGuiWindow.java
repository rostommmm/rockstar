package fun.rockstarity.api.render.ui.clickgui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.configs.ConfigsHandler;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.math.Hover;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.helpers.secure.Web;
import fun.rockstarity.api.helpers.system.TextUtility;
import fun.rockstarity.api.helpers.system.ThreadManager;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.animation.infinity.InfinityAnimation;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.render.color.themes.Theme;
import fun.rockstarity.api.render.cursor.CursorType;
import fun.rockstarity.api.render.cursor.CursorUtility;
import fun.rockstarity.api.render.menufilter.MenuFilter;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import fun.rockstarity.api.render.ui.clickgui.esp.ESPElement;
import fun.rockstarity.api.render.ui.clickgui.esp.ESPSettings;
import fun.rockstarity.api.render.ui.clickgui.windows.BindListWindow;
import fun.rockstarity.api.render.ui.clickgui.windows.SettingsWindow;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.api.render.ui.widgets.InputWidget;
import fun.rockstarity.api.render.ui.widgets.Window;
import fun.rockstarity.api.scripts.Script;
import fun.rockstarity.api.sounds.Sound;
import fun.rockstarity.client.modules.render.ClickGui;
import fun.rockstarity.client.modules.render.ESP;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;

/**
 * @author ConeTin
 * @since 4 дек. 2023 г.
 */


@Getter
public class ClickGuiWindow extends Rect implements IAccess {
	
	private final ArrayList<Window> windows = new ArrayList<>();
	private Category current = Category.COMBAT, prevCurrent = Category.COMBAT;
	private int prevIndex;
	@Setter
	private Module opened, prevOpened, queue;
	private ClickGuiRenderer renderer;
	@Setter
	private boolean drag, tryDrag, canDrag = true, searching;
	private float dragX, dragY;
	@Setter
	private InputWidget input;
	private final InfinityAnimation scrollValue = new InfinityAnimation(), settingsScrollValue = new InfinityAnimation();
	@Setter
	private float scroll, settingsScroll;
	private ESPSettings espSettings;
	
	public ClickGuiWindow(float x, float y, float width, float height) {
		super(x, y, width, height);
		
		prevIndex = current.getIndex();
		ClickGuiRenderer.changeCurrent = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300).setSize(prevIndex - Category.COMBAT.getIndex());
		ClickGuiRenderer.changeCurrent.setForward(true);
		ClickGuiRenderer.changeCurrentTail = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(500).setSize(prevIndex - Category.COMBAT.getIndex());
		ClickGuiRenderer.changeCurrentTail.setForward(true);
		current = Category.COMBAT;
		renderer = new ClickGuiRenderer();
		this.espSettings = new ESPSettings();
	}
	
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		try {
			if (drag) {
				x = (float) MathHelper.clamp(mouseX + dragX, -140, sr.getScaledWidth() - this.getWidth() + 140);
	            y = (float) MathHelper.clamp(mouseY + dragY, -140, sr.getScaledHeight() - this.getHeight() + 140);
			}
			
			renderer.render(matrixStack, mouseX, mouseY, partialTicks);

			GL11.glPushMatrix();
		    GL11.glTranslated(0, 0, 999);
		    windows.stream().forEach(window -> window.render(matrixStack, mouseX, mouseY, partialTicks));
		    GL11.glPopMatrix();
			
			windows.removeIf(window -> window.getOpening().finished(false));
			this.scrollValue.animate(scroll, 150);
			this.settingsScrollValue.animate(settingsScroll, 150);
			if (this.scroll > 0)
				this.scroll = 0;
			if (this.settingsScroll > 0)
				this.settingsScroll = 0;
			
	       
		} catch (Exception e) {}
	}
	
	public boolean clicked(double mouseX, double mouseY, int button) {
		
		if (rock.isDebugging() && Hover.isHovered(sr.getScaledWidth() - 20, sr.getScaledHeight() - 20, 20, 20, mouseX, mouseY)) {
			renderer.setLoaded(!renderer.isLoaded());
		}
		
		
		if (!ClickGuiRenderer.isLoaded()) return false;
		if (this.input != null) this.input.mouseClicked(mouseX, mouseY, button);
		try {
			for (Window window : windows) {
				window.clicked(mouseX, mouseY, button);
				if (!window.canClick(mouseX, mouseY)) {
					for (Window window1 : windows) {
						if (window1 != window && window1.getOpening().finished() && (window1 instanceof BindListWindow && window1 instanceof BindListWindow || window1 instanceof SettingsWindow && window instanceof SettingsWindow)) {
							window1.getOpening().setForward(false);
						}
					}
					return true;
				}
			}
			
			// Обработка категорий
			float yOff = 0;
			for (Category type : Category.values()) {
				if (Hover.isHovered(x + 8, y + 49 + yOff, 21, 21, mouseX, mouseY) && type != this.current) {
					changeCategory(type);
				}
				
				yOff += 24;
			}
			
			if (Hover.isHovered(this.y(y + 30).height(height-30), mouseX, mouseY)) {
				// Обработка модулей ->
				float yOff1 = scrollValue.get();
				if (current == Category.SCRIPTS) {
					for (Script script : rock.getScriptHandler().getEnabledScripts()) {
						String name = script.getName();
						
						if (script.getScriptModules().isEmpty()) continue;

						yOff1 += 12;
						
						for (Module module : script.getScriptModules()) {
							if (Hover.isHovered(x + 37, y + 30.5f + yOff1, 118, 13, mouseX, mouseY)) {
								if (button == 0)
									module.toggle();
								else if (button == 1 && opened != module) {
									if (opened != null) {
										queue = module;
									} else {
										opened = module;
									}
								} else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
									windows.add(new BindListWindow(module, (float)mouseX, (float)mouseY, 60, 100));
								}
							}
							
							yOff1 += 13;
						}
					}
				} else {
					for (Entry<Character, List<Module>> entry : get(current).entrySet()) {
						String name = entry.getKey().toString();
						yOff1 += 12;
						for (Module module : entry.getValue()) {
							if (Hover.isHovered(x + 37, y + 30.5f + yOff1, 118, 13, mouseX, mouseY)) {
								if (button == 0)
									module.toggle();
								else if (button == 1 && opened != module) {
									if (opened != null) {
										queue = module;
									} else {
										opened = module;
									}
								} else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
									windows.add(new BindListWindow(module, (float)mouseX, (float)mouseY, 60, 100));
								}
							}
							
							yOff1 += 13;
						}
					}
				}
				
				// Обработка настроек ->
				if (Hover.isHovered(x + 156, (float) (y + 29 + 40), width - 156, (float) (height - 40 - (29)), mouseX, mouseY)) {
					for (SettingRect setting : get(opened)) {
						setting.clicked(mouseX, mouseY, button, false);
					}
				}
			}
			
			if (prevOpened instanceof ESP && !ClickGuiRenderer.openedAnim.finished(false)) {
				this.espSettings.clicked(mouseX, mouseY, button);;
			}
			
			// Обработка тем
			if (current == Category.THEMES) {
				float xOff = 0;
				for (Theme theme : rock.getThemes()) {
					
					float width = bold.get(18).getWidth(theme.getName()) + 65;
					if (Hover.isHovered(x + 45 + xOff, y + 37, width, 25, mouseX, mouseY)) {
						rock.getThemes().setCurrent(theme);
						ThreadManager.run(() -> {
							rock.saveTheme();
						});
					}
					
					xOff += width + 5;
				}
				
				xOff = 0;
				float yOff11 = 33;
				for (Style style : Style.values()) {
					float width = 105;
					if (Hover.isHovered(x + 45 + xOff, y + 37 + yOff11, this.width, 33, mouseX, mouseY)) {
						Style.setCurrent(style);
						ThreadManager.run(() -> {
							rock.saveTheme();
						});
					}
					
					xOff += width + 5;
					if (xOff + width + 5 > this.width) {
						yOff11 += 38;
						xOff = 0;
					}
				}
			}
			
			{ // Панелька сверху справа
				// Кнопка сохранения кфг
				if (!this.searching && Hover.isHovered(x + 166, y + 7 - (29 - 29 * ClickGuiRenderer.upSideAnim.get()), bold.get(16).getWidth("Сохранить конфиг") + 9, 15, mouseX, mouseY)) {
					ConfigsHandler handler = rock.getConfigHandler();
					handler.save(handler.getCurrent(), false);
				}
	
				// Кнопка открытия папки клиента
				if (Hover.isHovered(x + 449, y + 8 - (29 - 29 * ClickGuiRenderer.upSideAnim.get()), 13, 13, mouseX, mouseY)) {
					/*
					try {
						Desktop desktop = Desktop.getDesktop();
						File configFolder = new File(rockstarPath + "/configs/");
						if (!configFolder.exists()) configFolder.mkdirs();
						if (configFolder.isDirectory()) {
							desktop.open(configFolder);
							rock.getAlertHandler().alert("Папка с конфигами открыта!", AlertType.SUCCESS);
						} else {
							rock.getAlertHandler().alert("Папка с конфигами не найдена.", AlertType.ERROR);
						}
					} catch (Exception e) {
						Debugger.print(e);
						rock.getAlertHandler().alert("Произошла ошибка при открытии папки с конфигами.", AlertType.ERROR);
					}
					*/
				}
				
				// Кнопка поиска
				if (Hover.isHovered(x + 468, y + 9, 11, 11, mouseX, mouseY)) {
					this.searching = !this.searching;
					input.setText("");
					input.setFocused2(true);
				}
			}
			
			// Окошечки ->
			
			// Окошечко бинда
			if (opened != null) {
				if (Hover.isHovered(x + 171 + bold.get(24).getWidth("Настройки " + opened.getInfo().name()), y + 43.5f, 9, 9, mouseX, mouseY)) {
					windows.add(new BindListWindow(opened, (float)mouseX, (float)mouseY, 60, 100));
				}
			}
			
			// Кнопка закрытия
			if (Hover.isHovered(x + width - 21, y + 43, 9, 9, mouseX, mouseY)) {
				opened = null;
			}
			
			// Аватарка
			if (Hover.isHovered(x + 9 + 30 * ClickGuiRenderer.moveAnim.get() - 30-1,y + height - 30-1, 20+1*2, 20+1*2, mouseX, mouseY)) {
				Web.openWebpage("https://rockstar.moscow/profile.php");
				rock.getAlertHandler().alert("Профиль открыт!", AlertType.SUCCESS);
			}
		} catch (Exception e) {}
		
		return true;
	}
	
	public boolean released(double mouseX, double mouseY, int button) {
		try {
			windows.stream().forEach(window -> window.released(mouseX, mouseY, button));
			for (Window window : windows) {
				if (!window.canClick(mouseX, mouseY)) return true;
			}
			tryDrag = false;
			drag = false;
			for (SettingRect setting : get(opened)) {
				setting.clicked(mouseX, mouseY, button, true);
			}
			if (prevOpened instanceof ESP && !ClickGuiRenderer.openedAnim.finished(false)) {
				this.espSettings.released(mouseX, mouseY, button);
			}
		} catch (Exception e) {}
		
		return true;
	}

	public boolean dragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		try {
			for (Window window : windows) {
				window.dragged(mouseX, mouseY, button, dragX, dragY);
				if (!window.canClick(mouseX, mouseY)) return true;
			}
			
			boolean cancel = false;
			
			for (SettingRect setting : get(opened)) {
				if (setting.dragged(mouseX, mouseY, button, dragX, dragY)) cancel = true;
			}
			
			for (ESPElement elmt : rock.getEspSettingsHandler().getEspElements()) {
				if (elmt.isDragging()) cancel = true;
			}
			
			for (Window window : windows) {
				cancel = true;
			}
			
			if (cancel) return false;

			if (this.opened == rock.getModules().get(ESP.class) && (Hover.isHovered(espSettings.getElementsRect(), mouseX, mouseY) || Hover.isHovered(espSettings.getPreviewRect(), mouseX, mouseY))) return false;
			
			if (espSettings.isDragESP()) return false;
			
	        if (button == 0 && Hover.isHovered(this, mouseX, mouseY) && !drag && !tryDrag) {
	        	if (!canDrag) {
	        		tryDrag = true;
	        		return false;
	        	}
	            this.dragX = (float) (x - mouseX);
	            this.dragY = (float) (y - mouseY);
	            drag = true;
	        }
		} catch (Exception e) {}
		
		return true;
	}
	
	public boolean charTyped(char codePoint, int modifiers) {
	    if (this.searching && codePoint == '/' && this.input != null && this.input.getText().isEmpty()) {
	        return true;
	    }
		
		if (this.input != null) this.input.charTyped(codePoint, modifiers);
		
		for (SettingRect setting : get(opened)) {
			setting.charTyped(codePoint, modifiers);
		}
		for (Window window : windows) {
			window.charTyped(codePoint, modifiers);
		}
		return false;
	}
	
	public void tick() {
		if (this.input != null) this.input.tick();
		for (SettingRect setting : get(opened)) {
			setting.tick();
		}
	}

	public boolean pressed(int keyCode, int scanCode, int modifiers) {
	    drag = false;
	    
	    if (keyCode == GLFW.GLFW_KEY_ESCAPE && this.searching) {
	        this.searching = false;
	        if (this.input != null) {
	            this.input.setFocused2(false);
	        }
	        return true;
	    }
	    
	    if ((keyCode == GLFW.GLFW_KEY_SLASH || (keyCode == GLFW.GLFW_KEY_F && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0)) && !this.searching) {
	           this.searching = true;
	           if (this.input != null) {
	               this.input.setFocused2(true);
	           }
	           return true;
	       }
	    
	    if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
	        if (this.input != null && this.input.isFocused()) {
	            this.input.setFocused2(false);
	            return true;
	        }
	    }
	    
	    if (this.input != null) this.input.keyPressed(keyCode, scanCode, modifiers);
	    for (SettingRect setting : get(opened)) {
	        setting.pressed(keyCode, scanCode, modifiers);
	    }
	    windows.stream().forEach(window -> window.pressed(keyCode, scanCode, modifiers));
	    return true;
	}
	
	public boolean scrolled(double mouseX, double mouseY, double delta) {
		for (Window window : windows) {
			window.scrolled(mouseX, mouseY, delta);
			if (!window.canClick(mouseX, mouseY)) return true;
		}

		if (Hover.isHovered((float) (renderer.getLoadedAnimX() + 156 - 118 * ClickGuiRenderer.themesAnim.get()), (float) (y + 29 * ClickGuiRenderer.upSideAnim.get()), width - 156+118*ClickGuiRenderer.themesAnim.get(), (float) (height - (29 * ClickGuiRenderer.upSideAnim.get())), mouseX, mouseY)) {
			this.settingsScroll += (float) (delta * 15);
		} else
			this.scroll += (float) (delta * 15);
		return false;
	}
	
	public void close() {
		ClickGuiRenderer.opening.setSpeed(1000);
		windows.clear();
		ClickGuiRenderer.opening.setForward(false);
		ClickGuiRenderer.rotationESP.setForward(false);
		renderer.setClosePos(mc.gameRenderer.getActiveRenderInfo().getProjectedView());
		MenuFilter.show(false);
		if (rock.getModules().get(ClickGui.class).getSound().get()) {
			new Sound("closemenu").play();
		}
	}
	
	public void changeCategory(Category type) {
	    if (this.searching) {
	        this.searching = false;
	        if (this.input != null) {
	            this.input.setFocused2(false);
	        }
	    }
	    
		this.prevCurrent = current;
		prevIndex = current.getIndex();
		ClickGuiRenderer.changeCurrent = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300).setSize(prevIndex - type.getIndex());
		ClickGuiRenderer.changeCurrent.setForward(true);
		ClickGuiRenderer.changeCurrentTail = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(500).setSize(prevIndex - type.getIndex());
		ClickGuiRenderer.changeCurrentTail.setForward(true);
		ClickGuiRenderer.changeCategoryAnim.setForward(false);
		current = type;
		if (current == Category.THEMES) this.opened = null;
	}
	
	public GlyphType get(Category myType) {
	    if (searching && input != null && (input.isFocused() || !input.getText().isEmpty())) {
	        GlyphType sumGlyphType = new GlyphType(); // Создаем новый объект GlyphType для хранения суммы

	        for (Category cat : Category.values()) {
	            GlyphType glyphType = renderer.getGlyphes()[cat.getIndex()];
	            if (glyphType != null) {
	                for (Map.Entry<Character, List<fun.rockstarity.api.modules.Module>> entry : glyphType.entrySet()) {
	                    char key = entry.getKey();
	                    List<fun.rockstarity.api.modules.Module> modules = entry.getValue();
	                    if (modules != null) {
	                        Set<fun.rockstarity.api.modules.Module> uniqueModules = new HashSet<>();

	                        for (fun.rockstarity.api.modules.Module module : modules) {
	                            boolean added = false;

	                            if (module.getInfo().name().toLowerCase().contains(input.getText().toLowerCase())) {
	                                if (uniqueModules.add(module)) {
	                                    sumGlyphType.put(key, sumGlyphType.getOrDefault(key, new ArrayList<>()));
	                                    sumGlyphType.get(key).add(module);
	                                    added = true;
	                                }
	                            }

	                            String[] moduleNames = module.getInfo().module();
	                            if (moduleNames != null && !added) {
	                                for (String mod : moduleNames) {
	                                    if (mod != null && !mod.isEmpty()) {
	                                        if (mod.toLowerCase().contains(input.getText().toLowerCase())) {
	                                            if (uniqueModules.add(module)) {
	                                                sumGlyphType.put(key, sumGlyphType.getOrDefault(key, new ArrayList<>()));
	                                                sumGlyphType.get(key).add(module);
	                                            }
	                                            break; 
	                                        }
	                                        
	                                        int distance = TextUtility.levenshteinDistance(mod.toLowerCase(), input.getText().toLowerCase());
	                                        if (distance <= 2) { 
	                                            if (uniqueModules.add(module)) {
	                                                sumGlyphType.put(key, sumGlyphType.getOrDefault(key, new ArrayList<>()));
	                                                sumGlyphType.get(key).add(module);
	                                            }
	                                            break;
	                                        }
	                                    }
	                                }
	                            }
	                        }
	                    }
	                }
	            }
	        }

	        return sumGlyphType;
	    }

	    return renderer.getGlyphes()[myType.getIndex()];
	}


	
	public List<SettingRect> get(Module module) {
        try {
        	for (SettingRect rect : renderer.getSettings()) {
        		//if (rect.getParent().getParent() instanceof Module mod)
        		//	System.out.println(mod.getInfo().name());
        	}
        	return renderer.getSettings().stream()
                    .filter(setting -> setting.getParent().getParent() == module)
                    .collect(Collectors.toList());
        } catch (Exception e) {
        	return new ArrayList<SettingRect>();
        }
    }

	protected void screen() {
	    int screenWidth = sr.getScaledWidth();
	    int screenHeight = sr.getScaledHeight();

	    if (x < 0 || x + width > screenWidth) {
	        x = (screenWidth - width) / 2.0f;
	    }

	    if (y < 0 || y + height > screenHeight) {
	        y = (screenHeight - height) / 2.0f;
	    }
	}
}
