package fun.rockstarity.client.modules.render;


import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.Setting;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.menufilter.MenuFilter;
import fun.rockstarity.api.render.ui.clickgui.ClickGuiRenderer;
import fun.rockstarity.api.render.ui.clickgui.ClickGuiScreen;
import fun.rockstarity.api.render.ui.clickgui.GlyphType;
import fun.rockstarity.api.render.ui.clickgui.SettingRect;
import fun.rockstarity.api.scripts.Script;
import fun.rockstarity.api.sounds.Sound;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * @author ConeTin
 * @since 4 дек. 2023 г.
 */

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Info(name="ClickGui", desc="Меню чита", type=Category.RENDER)
public class ClickGui extends Module {
	

	Mode mode = new Mode(this, "Анимация закрытия");

	Mode.Element classic = new Mode.Element(mode, "Классическая");
	Mode.Element filter = new Mode.Element(mode, "Фильтр");
	Mode.Element tech = new Mode.Element(mode, "Трёхмерная").set();
	
	CheckBox shadow = new CheckBox(this, "Затемнение").set(true).hide(() -> !classic.get());
	CheckBox sound = new CheckBox(this, "Звук").set(true).hide(() -> !filter.get());
	Slider zoom = new Slider(this, "Приближение").min(0).max(100).inc(5f).set(50).text(0, "Нет")
			.hide(() -> !filter.get());
	
	Animation darkness = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(200);
	
	@Override
	public void onAllEvent(Event event) {
		if (event instanceof EventUpdate && rock.getClickGui() == null) {
			rock.setClickGui(new ClickGuiScreen());
			rock.getClickGui().getWindow().getEspSettings().renderPage(new MatrixStack(), 0, 0, 0);
		}
		
		if (event instanceof EventUpdate) {
			if (rock.getClickGui() == null) {
				rock.setClickGui(new ClickGuiScreen());
				rock.getClickGui().getWindow().getEspSettings().renderPage(new MatrixStack(), 0, 0, 0);
			}
			
			if (!(mc.currentScreen instanceof ClickGuiScreen) && MenuFilter.active()) {
				rock.getClickGui().getWindow().close();
			}
		}
		
		super.onAllEvent(event);
	}
	
	@Override
	public void onEvent(Event event) {
	}
	
	@Override
	public void onEnable() {
		if (filter.get()) {
			MenuFilter.show(true);
			if (sound.get()) {
                new Sound("openmenu").play();
            }
		}
		
		mc.displayGuiScreen(rock.getClickGui());
		
		ClickGuiRenderer.opening.setForward(true);
		ClickGuiRenderer.rotationESP.setForward(true);
		
		if (rock.getClickGui().getWindow().getRenderer().getSettings().isEmpty()) {
			for (Category cat : Category.values()) {
				int i = cat.getIndex();
				rock.getClickGui().getWindow().getRenderer().getGlyphes()[i] = new GlyphType();

				List<Module> modules = new ArrayList<>();
				
				rock.getModules().values().forEach(mod -> modules.add(mod));
				for (Script script : rock.getScriptHandler().getEnabledScripts()) {
					script.getScriptModules().forEach(mod -> modules.add(mod));
				}
				
				modules.sort(rock.getScriptHandler().SORT_METHOD);
				
				for (Module module : modules) {
					if (module.getInfo().type() == cat) {
						if (!rock.getClickGui().getWindow().getRenderer().getGlyphes()[i].containsKey(module.getInfo().name().charAt(0)))
							rock.getClickGui().getWindow().getRenderer().getGlyphes()[i].put(module.getInfo().name().charAt(0), new ArrayList<>());
						
						rock.getClickGui().getWindow().getRenderer().getGlyphes()[i].get(module.getInfo().name().charAt(0)).add(module);
						
						for (Setting setting : module.getSettings()) {
							rock.getClickGui().getWindow().getRenderer().getSettings().add(new SettingRect(setting));
						}
					}
				}
			}
		}
		
		this.toggle(false, true);
	}
	
	@Override
	public void onDisable() {

	}
	
}
