package fun.rockstarity.api.scripts.wrappers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.system.TextUtility;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.Setting;
import fun.rockstarity.api.modules.settings.list.Binding;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.ColorPicker;
import fun.rockstarity.api.modules.settings.list.Input;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Position;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import fun.rockstarity.api.render.ui.draggables.Draggable;
import fun.rockstarity.api.scripts.Script;
import fun.rockstarity.api.scripts.helpers.Information;
import fun.rockstarity.api.scripts.wrappers.base.ColorBase;
import fun.rockstarity.api.scripts.wrappers.base.DragBase;
import fun.rockstarity.api.scripts.wrappers.base.LivingEntityBase;
import fun.rockstarity.api.scripts.wrappers.base.ModuleBase;
import fun.rockstarity.api.scripts.wrappers.base.PotionBase;
import fun.rockstarity.api.scripts.wrappers.settings.BindBase;
import fun.rockstarity.api.scripts.wrappers.settings.CheckBoxBase;
import fun.rockstarity.api.scripts.wrappers.settings.InputBase;
import fun.rockstarity.api.scripts.wrappers.settings.ModeBase;
import fun.rockstarity.api.scripts.wrappers.settings.PickerBase;
import fun.rockstarity.api.scripts.wrappers.settings.PositionBase;
import fun.rockstarity.api.scripts.wrappers.settings.SelectBase;
import fun.rockstarity.api.scripts.wrappers.settings.SettingBase;
import fun.rockstarity.api.scripts.wrappers.settings.SliderBase;
import fun.rockstarity.client.modules.combat.AimAssist;
import fun.rockstarity.client.modules.combat.AimBot;
import fun.rockstarity.client.modules.combat.Aura;
import net.minecraft.client.MainWindow;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

public class Client implements IAccess {
	public void print(String text) {
		mc.player.addChatMessage(text);
	}
	
	public void print(Object msg, String hover, LuaValue luaFunction) {
	    Runnable action = () -> {
	        try {
	            luaFunction.call();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    };
	    
	    StringTextComponent message = new StringTextComponent(I18n.format(msg.toString()));
	    message.setStyle(net.minecraft.util.text.Style.EMPTY
	        .setClickEvent(new ClickEvent(ClickEvent.Action.RUNNABLE, action))
	        .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(hover))));
	    mc.ingameGUI.getChatGUI().printChatMessage(message);
	}
	
	public int fps() {
		return mc.debugFPS;
	}
	
	public long ping() {
		return Server.getIP().contains("local")  ? 0 : Server.ping();
	}
	
	public String server() {
		return Server.getIP();
	}
	
	public boolean focused() {
		return mc.isGameFocused();
	}
	
	public float tps() {
		return rock.getTpsHandler().getTPS();
	}
	
	public String hwid() {
		 return Information.getHWID();
		//return Launcher.HWID;
	}
	
	public void notification(String text) {
		rock.getAlertHandler().alert(text, AlertType.INFO);
	}
	
	public void notification(String text, String type) {
		rock.getAlertHandler().alert(text, AlertType.get(type));
	}
	
	public String name() {
		return rock.getUser().getName();
	}
	
	public String avatar() {
		return rock.getUser().getAvatar();
	}
	
	public long id() {
		return rock.getUser().getId();
	}
	
	public long uid() {
		return rock.getUser().getUid();
	}
	
	public String role() {
		return rock.getUser().getRole();
	}
	
	public long starts() {
		return rock.getUser().getStarts();
	}
	
	public float screen_width() {
		MainWindow sr = mc.getMainWindow();
		return sr.getScaledWidth();
	}
	
	public boolean clickgui() {
		return mc.currentScreen == rock.getClickGui();
	}
	
	public float screen_height() {
		MainWindow sr = mc.getMainWindow();
		return sr.getScaledHeight();
	}
	
	public LivingEntityBase aura_target() {
		if (rock.getModules().get(Aura.class).getTarget() != null)
			return new LivingEntityBase(rock.getModules().get(Aura.class).getTarget());
		return null;
	}
	
	public LivingEntityBase target() {
		LivingEntity target = Stream.of(
        		rock.getModules().get(Aura.class).getTarget(),
        		rock.getModules().get(AimAssist.class).getTarget(), 
        		rock.getModules().get(AimBot.class).getTarget()
        ).filter(Objects::nonNull).findFirst().orElse(null);
		
		if (target != null) {
			new LivingEntityBase(target);
		}

		return null;
	}
	
	public void display(LuaTable screen) {
		mc.displayGuiScreen(new Screen(new TranslationTextComponent("PENIS")) {
			@Override
			public void init() {
				LuaValue func = screen.get("init");
			    if (!func.isnil() && func.isfunction()) {
			    	try {
			    		func.call();
			    	} catch (Exception e) {}
			    }
				super.init();
			}
			
			@Override
			public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
				LuaValue func = screen.get("render");
			    if (!func.isnil() && func.isfunction()) {
			    	try {
			    		func.call(LuaValue.valueOf(mouseX), LuaValue.valueOf(mouseY));
			    	} catch (Exception e) {
			    		e.printStackTrace();			    	
			    	}
			    }
				super.render(matrixStack, mouseX, mouseY, partialTicks);
			}
			
			@Override
			public boolean mouseClicked(double mouseX, double mouseY, int button) {
				LuaValue func = screen.get("mouseClicked");
			    if (!func.isnil() && func.isfunction()) {
			    	try {
			    		func.call(LuaValue.valueOf(mouseX), LuaValue.valueOf(mouseY), LuaValue.valueOf(button));
			    	} catch (Exception e) {}
			    }
				return super.mouseClicked(mouseX, mouseY, button);
			}
			
			@Override
			public boolean mouseReleased(double mouseX, double mouseY, int button) {
				LuaValue func = screen.get("mouseReleased");
			    if (!func.isnil() && func.isfunction()) {
			    	try {
			    		func.call(LuaValue.valueOf(mouseX), LuaValue.valueOf(mouseY), LuaValue.valueOf(button));
			    	} catch (Exception e) {}
			    }
				return super.mouseReleased(mouseX, mouseY, button);
			}
			
			@Override
			public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
				LuaValue func = screen.get("keyPressed");
			    if (!func.isnil() && func.isfunction()) {
			    	try {
			    		func.call(LuaValue.valueOf(keyCode));
			    	} catch (Exception e) {}
			    }
				return super.keyPressed(keyCode, scanCode, modifiers);
			}
			
			@Override
			public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
				LuaValue func = screen.get("keyReleased");
			    if (!func.isnil() && func.isfunction()) {
			    	try {
			    		func.call(LuaValue.valueOf(keyCode));
			    	} catch (Exception e) {}
			    }
				return super.keyReleased(keyCode, scanCode, modifiers);
			}
			
			@Override
			public void tick() {
				LuaValue func = screen.get("tick");
			    if (!func.isnil() && func.isfunction()) {
			    	try {
			    		func.call();
			    	} catch (Exception e) {}
			    }
				super.tick();
			}
			
			@Override
			public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
				LuaValue func = screen.get("mouseScrolled");
			    if (!func.isnil() && func.isfunction()) {
			    	try {
			    		func.call(LuaValue.valueOf(mouseX), LuaValue.valueOf(mouseY), LuaValue.valueOf(delta));
			    	} catch (Exception e) {}
			    }
				return super.mouseScrolled(mouseX, mouseY, delta);
			}
			
			@Override
			public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
				LuaValue func = screen.get("mouseDragged");
			    if (!func.isnil() && func.isfunction()) {
			    	try {
			    		func.call(LuaValue.valueOf(dragX), LuaValue.valueOf(dragY), LuaValue.valueOf(button));
			    	} catch (Exception e) {}
			    }
				return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
			}
		});
	}
	
	public void exit() {
		System.exit(0);
	}
	
	/*
	public StaffBase[] staffs() {
		ArrayList<StaffBase> staffs = new ArrayList<>();
		
		for (String str : Interface.get().getStafflist().getAllStaff()) {
			staffs.add(new StaffBase(str, true));
		}
		
		StaffBase[] array = new StaffBase[staffs.size()];
		array = staffs.toArray(array);
		return array;
	}
	*/

	public ModuleBase[] modules() {
		ArrayList<ModuleBase> modules = new ArrayList<>();
		
		for (fun.rockstarity.api.modules.Module mod : rock.getModules().values()) {
			modules.add(new ModuleBase(mod));
		}
		
		ModuleBase[] array = new ModuleBase[modules.size()];
		array = modules.toArray(array);
		return array;
	}
	
	public PotionBase[] potions() {
		ArrayList<PotionBase> potions = new ArrayList<>();
		Collection<EffectInstance> activeEffects = mc.player.getActivePotionEffects();
		
		if (!activeEffects.isEmpty()) {
			for (EffectInstance effectInstance : activeEffects) {
				Effect effect = effectInstance.getPotion();
				String power;
				switch (effectInstance.getAmplifier()) {
				case 0:
			        power = "I";
			        break;
				case 1:
			        power = "II";
			        break;
			    case 2:
			        power = "III";
			        break;
			    case 3:
			        power = "IV";
			        break;
			    case 4:
			        power = "V";
			        break;
			    default:
			        power = "";
			        break;
				}
				if (effect != null) {
					String s = I18n.format(effect.getName()) + " " + power;
			        String s1 = EffectUtils.getPotionDurationString(effectInstance, 1.0F);
			        
			        potions.add(new PotionBase(effect, s, s1));
				}
			}
		}
		
		PotionBase[] array = new PotionBase[potions.size()];
		array = potions.toArray(array);
		return array;
	}
	
	public ColorBase[] client_colors() {
		return new ColorBase[] { new ColorBase(Style.getMain()), new ColorBase(Style.getSecond()) };
	}
	
	public ColorBase client_color(int i) {
		return new ColorBase(Style.getPoint(i));
	}
	
	public ColorBase client_color() {
		return new ColorBase(Style.getMain());
	}
	
	public DragBase[] drags() {
		ArrayList<DragBase> drags = new ArrayList<>();
		for (Draggable d : rock.getDraggableHandler().getDraggables()) {
			drags.add(new DragBase(d));
		}
		DragBase[] array = new DragBase[drags.size()];
		array = drags.toArray(array);
		return array;
	}
	
	public float menu_x() {
		return rock.getClickGui().getWindow().getX();
	}
	
	public float menu_y() {
		return rock.getClickGui().getWindow().getY();
	}
	
	public float menu_width() {
		return rock.getClickGui().getWindow().getWidth();
	}
	
	public float menu_height() {
		return rock.getClickGui().getWindow().getHeight();
	}

	public void load(String name) {
		for (Script script : rock.getScriptHandler().getScripts())
			if (script.getName().equalsIgnoreCase(name)) {
				script.load();
				script.setEnabled(true);
			}
	}

	public void unload(String name) {
		for (Script script : rock.getScriptHandler().getScripts())
			if (script.getName().equalsIgnoreCase(name)) {
				script.unload();
				script.setEnabled(false);
			}
	}
	
	public void unload() {
		Script.getCurrent().unload();
		Script.getCurrent().setEnabled(false);
	}
	
	public void copy(String text) {
		TextUtility.copyText(text);
	}
	
	public ColorBase theme_main() {
		return new ColorBase(rock.getThemes().getFirstColor());
	}
	
	public ColorBase theme_second() {
		return new ColorBase(rock.getThemes().getSecondColor());
	}
	
	public ColorBase theme_text() {
		return new ColorBase(rock.getThemes().getTextFirstColor());
	}
	
	public ColorBase theme_second_text() {
		return new ColorBase(rock.getThemes().getTextSecondColor());
	}
	
	public ModuleBase get(String name) {
		Module mod = null;
		
		for (fun.rockstarity.api.modules.Module m : rock.getModules().values())
			if (m.getInfo().name().equalsIgnoreCase(name)) mod = m;
		
		
		return new ModuleBase(mod);
	}
	
	public SettingBase get(String name, String setting) {
		for (fun.rockstarity.api.modules.Module m : rock.getModules().values())
			if (m.getInfo().name().equalsIgnoreCase(name))
				for (Setting s : m.getSettings())
					if (s.getName().equals(setting)) {
						if (s instanceof CheckBox) {
							return (new CheckBoxBase((CheckBox)s));
						} else if (s instanceof Binding) {
							return (new BindBase((Binding)s));
						} else if (s instanceof Input) {
							return (new InputBase((Input)s));
						} else if (s instanceof Mode) {
							return (new ModeBase((Mode)s));
						} else if (s instanceof ColorPicker) {
							return (new PickerBase((ColorPicker)s));
						} else if (s instanceof Select) {
							return (new SelectBase((Select)s));
						} else if (s instanceof Position) {
							return (new PositionBase((Position)s));
						} else if (s instanceof Slider) {
							return (new SliderBase((Slider)s));
						} else {
							return (new SettingBase(s, false));
						}
					}
		
		return null;
	}
	
	public CheckBoxBase get_checkbox(String name, String setting) {
		CheckBox set = null;
		
		for (fun.rockstarity.api.modules.Module m : rock.getModules().values())
			if (m.getInfo().name().equalsIgnoreCase(name)) 
				for (Setting s : m.getSettings())
					if (s instanceof CheckBox)
						if (s.getName().equals(setting)) set = (CheckBox) s;
		
		return new CheckBoxBase(set);
	}
	
	public SliderBase get_slider(String name, String setting) {
		Slider set = null;
		
		for (fun.rockstarity.api.modules.Module m : rock.getModules().values())
			if (m.getInfo().name().equalsIgnoreCase(name)) 
				for (Setting s : m.getSettings())
					if (s instanceof Slider)
						if (s.getName().equals(setting)) set = (Slider) s;
		
		return new SliderBase(set);
	}
	
	public ModeBase get_mode(String name, String setting) {
		Mode set = null;
		
		for (fun.rockstarity.api.modules.Module m : rock.getModules().values())
			if (m.getInfo().name().equalsIgnoreCase(name)) 
				for (Setting s : m.getSettings())
					if (s instanceof Mode)
						if (s.getName().equals(setting)) set = (Mode) s;
		
		ModeBase mode = new ModeBase(set);
		mode.getPrev().clear();
		mode.getPrev().addAll(set.getElements());
		return mode;
	}
	
	public SelectBase get_select(String name, String setting) {
		Select set = null;
		
		for (fun.rockstarity.api.modules.Module m : rock.getModules().values())
			if (m.getInfo().name().equalsIgnoreCase(name)) 
				for (Setting s : m.getSettings())
					if (s instanceof Select)
						if (s.getName().equals(setting)) set = (Select) s;
		
		return new SelectBase(set);
	}
	
	public PickerBase get_colorpicker(String name, String setting) {
		ColorPicker set = null;
		
		for (fun.rockstarity.api.modules.Module m : rock.getModules().values())
			if (m.getInfo().name().equalsIgnoreCase(name)) 
				for (Setting s : m.getSettings())
					if (s instanceof ColorPicker)
						if (s.getName().equals(setting)) set = (ColorPicker) s;
		
		return new PickerBase(set);
	}
	
	public PositionBase get_position(String name, String setting) {
		Position set = null;
		
		for (fun.rockstarity.api.modules.Module m : rock.getModules().values())
			if (m.getInfo().name().equalsIgnoreCase(name)) 
				for (Setting s : m.getSettings())
					if (s instanceof ColorPicker)
						if (s.getName().equals(setting)) set = (Position) s;
		
		return new PositionBase(set);
	}
	
	public InputBase get_input(String name, String setting) {
		Input set = null;
		
		for (fun.rockstarity.api.modules.Module m : rock.getModules().values())
			if (m.getInfo().name().equalsIgnoreCase(name)) 
				for (Setting s : m.getSettings())
					if (s instanceof Input)
						if (s.getName().equals(setting)) set = (Input) s;
		
		return new InputBase(set);
	}
	
	public BindBase get_bind(String name, String setting) {
		Binding set = null;
		
		for (fun.rockstarity.api.modules.Module m : rock.getModules().values())
			if (m.getInfo().name().equalsIgnoreCase(name)) 
				for (Setting s : m.getSettings())
					if (s instanceof Binding)
						if (s.getName().equals(setting)) set = (Binding) s;
		
		return new BindBase(set);
	}
}