package fun.rockstarity.client.commands;

import java.awt.Desktop;
import java.io.File;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

import fun.rockstarity.api.commands.CmdInfo;
import fun.rockstarity.api.commands.Command;
import fun.rockstarity.api.commands.CommandParameter;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.secure.Web;
import fun.rockstarity.api.helpers.system.FileUtility;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import fun.rockstarity.api.scripts.Script;
import fun.rockstarity.api.scripts.wrappers.Client;
import fun.rockstarity.api.scripts.wrappers.Events;
import fun.rockstarity.api.scripts.wrappers.Player;
import fun.rockstarity.api.scripts.wrappers.Render;
import fun.rockstarity.api.scripts.wrappers.base.FilesBase;
import fun.rockstarity.api.scripts.wrappers.base.GL11Base;
import fun.rockstarity.api.scripts.wrappers.base.MathBase;
import fun.rockstarity.api.scripts.wrappers.base.WorldBase;
import fun.rockstarity.api.scripts.wrappers.factory.AnimFactory;
import fun.rockstarity.api.scripts.wrappers.factory.BindFactory;
import fun.rockstarity.api.scripts.wrappers.factory.CheckBoxFactory;
import fun.rockstarity.api.scripts.wrappers.factory.ColorFactory;
import fun.rockstarity.api.scripts.wrappers.factory.DragFactory;
import fun.rockstarity.api.scripts.wrappers.factory.InputFactory;
import fun.rockstarity.api.scripts.wrappers.factory.ModeFactory;
import fun.rockstarity.api.scripts.wrappers.factory.ModuleFactory;
import fun.rockstarity.api.scripts.wrappers.factory.PickerFactory;
import fun.rockstarity.api.scripts.wrappers.factory.PositionFactory;
import fun.rockstarity.api.scripts.wrappers.factory.PotionFactory;
import fun.rockstarity.api.scripts.wrappers.factory.SelectFactory;
import fun.rockstarity.api.scripts.wrappers.factory.SliderFactory;
import fun.rockstarity.api.scripts.wrappers.factory.SoundFactory;
import fun.rockstarity.api.scripts.wrappers.factory.TimerFactory;
import fun.rockstarity.api.scripts.wrappers.factory.VectorFactory;
import fun.rockstarity.api.secure.Debugger;
import net.minecraft.item.Items;
import net.minecraft.util.text.TextFormatting;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;
@NativeInclude
@CmdInfo(names={"lua", "script", "scripts"}, desc="Позволяет управлять скриптами")
public class LuaCommand extends Command {

	Globals engine;
	
	CommandParameter list = new CommandParameter(this, "list");
	CommandParameter add = new CommandParameter(this, "add", "save", "create");
	CommandParameter load = new CommandParameter(this, "load", "enable", "use", "start", "new");
	CommandParameter unload = new CommandParameter(this, "unload", "disable", "stop");
	CommandParameter del = new CommandParameter(this, "del", "remove", "delete");
	CommandParameter reload = new CommandParameter(this, "reload");
	CommandParameter dir = new CommandParameter(this, "dir");
	
	@Override
	public void execute(String[] args) {
	    if (args.length == 0) {
	        this.error();
	        return;
	    }
	    String commandArg = args[0];

	    if (contains(commandArg, add)) {
	        File file = null;

	        if (args.length > 1) {
	        	FileUtility.writeFile(file = new File(rock.getPath() + "scripts/" + args[1] + ".lua"), "script:name(\"" + args[1] + "\") -- Имя скрипта\r\n"
						+ "script:devs({\"" + rock.getUser().getName() + "\"}) -- Разработчики\r\n"
						+ "script:desc(\"Example Script\") -- Описание");
	        } else {
	        	if (new File(rock.getPath() + "scripts/NewScript.lua").exists()) {
		        	for (int i = 1; i < 100; i++) {
		        		file = new File(rock.getPath() + "scripts/NewScript" + i + ".lua");
		        		if (!file.exists()) {
		        			FileUtility.writeFile(file, "script:name(\"New Script #" + i + "\") -- Имя скрипта\r\n"
									+ "script:devs({\"" + rock.getUser().getName() + "\"}) -- Разработчики\r\n"
									+ "script:desc(\"Example Script\") -- Описание");
		        			break;
		        		} else continue;
		        	}
		        } else {
		        	FileUtility.writeFile(file = new File(rock.getPath() + "scripts/NewScript.lua"), "script:name(\"New Script\") -- Имя скрипта\r\n"
							+ "script:devs({\"" + rock.getUser().getName() + "\"}) -- Разработчики\r\n"
							+ "script:desc(\"Example Script\") -- Описание");
		        }
	        }
	        
	        Desktop desktop = null;
	        if (Desktop.isDesktopSupported()) {
	            desktop = Desktop.getDesktop();
	        }
	        try {
	            desktop.open(file);
	        } catch (Exception ioe) {
	            ioe.printStackTrace();
	        }
	        
	        rock.getScriptHandler().reload();
	        rock.getAlertHandler().alert("Скрипт создан", AlertType.INFO);
	    } else if (contains(commandArg, load)) {
	    	for (Script script : rock.getScriptHandler().getScripts()) {
	    		if (script.getName().equals(args[1])) {
	    			script.setEnabled(true);
	    			script.load();
	    		}
	    	}
	        rock.getAlertHandler().alert("Скрипт загружен", AlertType.INFO);
	    } else if (contains(commandArg, unload)) {
	    	for (Script script : rock.getScriptHandler().getScripts()) {
	    		if (script.getName().equals(args[1])) {
	    			script.setEnabled(false);
	    			script.unload();
	    		}
	    	}
	        rock.getAlertHandler().alert("Скрипт выгружен", AlertType.INFO);
	    } else if (contains(commandArg, del)) {
	    	for (Script script : rock.getScriptHandler().getScripts()) {
	    		if (script.getName().equals(args[1])) {
	    			script.getFile().delete();
	    		}
	    	}
	        rock.getAlertHandler().alert("Скрипт удален", AlertType.INFO);
	    } else if (contains(commandArg, reload)) {
	        rock.getScriptHandler().reload();
	        rock.getAlertHandler().alert("Скрипты перезагружены", AlertType.INFO);
	    } else if (contains(commandArg, dir)) {
	    	try {
				Desktop desktop = Desktop.getDesktop();
				File configFolder = new File(rock.getPath() + "scripts/");
				if (!configFolder.exists()) configFolder.mkdirs();
				if (configFolder.isDirectory()) {
					desktop.open(configFolder);
					rock.getAlertHandler().alert("Папка с скриптами открыта!", AlertType.SUCCESS);
				} else {
					rock.getAlertHandler().alert("Папка с скриптами не найдена.", AlertType.ERROR);
				}
			} catch (Exception e) {
				Debugger.print(e);
				rock.getAlertHandler().alert("Произошла ошибка при открытии папки с скриптами.", AlertType.ERROR);
			}
	    } else if (contains(commandArg, list)) {
	        if (rock.getScriptHandler().getScripts().isEmpty()) {
	            msg("Список скриптов пуст!");
	        } else {
	        	msg(TextFormatting.GRAY + "Список локальных скриптов:");
	            for (Script script : rock.getScriptHandler().getScripts()) {
	            	if (!script.isMarket())
	                Chat.msg(TextFormatting.AQUA + "[Rockstar] " + TextFormatting.WHITE + script.getName() + " " + (script.isEnabled() ? TextFormatting.GREEN + "[вкл]" : TextFormatting.RED + "[выкл]" + TextFormatting.GRAY + " - " + script.getDesc()),
	                		(script.isEnabled() ? TextFormatting.RED + "Выключить" : TextFormatting.GREEN + "Включить") + TextFormatting.RESET + " " + script.getName(), () -> {
	                			if (script.isEnabled()) {
	                				script.setEnabled(false);
	                				script.unload();
	                			} else {
	                				script.setEnabled(true);
	                				script.load();
	                			}
	                            msg(script.getName() + " теперь " + (script.isEnabled() ? TextFormatting.GREEN + "включен" : TextFormatting.RED + "выключен") + TextFormatting.RESET + ", обновляю список");
	                            rock.getCommands().execute("lua list");
	                        });
	            }
	            
	            Chat.msg(TextFormatting.AQUA + "[Rockstar] " + TextFormatting.GRAY + "Список скриптов с " + TextFormatting.UNDERLINE + "маркета" + TextFormatting.GRAY + ":", "Перейти на маркет", () -> {
	            	Web.openWebpage("https://rockstar.moscow/");
	            });
	            for (Script script : rock.getScriptHandler().getScripts()) {
	            	if (script.isMarket())
	                Chat.msg(TextFormatting.AQUA + "[Rockstar] " + TextFormatting.WHITE + script.getName() + " " + (script.isEnabled() ? TextFormatting.GREEN + "[вкл]" : TextFormatting.RED + "[выкл]" + TextFormatting.GRAY + " - " + script.getDesc()),
	                		(script.isEnabled() ? TextFormatting.RED + "Выключить" : TextFormatting.GREEN + "Включить") + TextFormatting.RESET + " " + script.getName(), () -> {
	                			if (script.isEnabled()) {
	                				script.setEnabled(false);
	                				script.unload();
	                			} else {
	                				script.setEnabled(true);
	                				script.load();
	                			}
	                            msg(script.getName() + " теперь " + (script.isEnabled() ? TextFormatting.GREEN + "включен" : TextFormatting.RED + "выключен") + TextFormatting.RESET + ", обновляю список");
	                            rock.getCommands().execute("lua list");
	                        });
	            }
	        }
	    } else {
	    	StringBuilder msg = new StringBuilder();
			for (int i = 0; i < args.length; ++i) {
				msg.append(args[i]).append(" ");
			}
			
			engine = JsePlatform.standardGlobals();
			
			// Load an instance into globals
			LuaValue script = engine.load(msg.toString().replace(".new", ":create"));
			
//			try {
//				parse("module", ModuleBase.class);
//				parse("checkbox", CheckBoxBase.class);
//				parse("slider", SliderBase.class);
//				parse("colorpicker", PickerBase.class);
//				parse("mode", ModeBase.class);
//				parse("select", SelectBase.class);
//				//parse("info", InfoBase.class);
//				parse("input", InputBase.class);
//				parse("bind", BindBase.class);
//				
//				parse("drag", DragBase.class);
//				parse("animation", AnimBase.class);
//				parse("sound", SoundBase.class);
//				//parse("staff", StaffBase.class);
//				parse("potion", PotionBase.class);
//				
//				parse("items", Items.class);
//				parse("matrixstack", MatrixBase.class);
//				parse("color", ColorBase.class);
//				parse("screen", ScreenBase.class);
//				parse("entity", EntityBase.class);
//				parse("living_entity", LivingEntityBase.class);
//				parse("vector", VectorBase.class);
//				//parse("gl11", GL11.class);
//				
//				//parse("net.minecraft.network.play.client");
//				//parse("Script", ScriptBase.class);
//			} catch (Exception e) {
//				e.printStackTrace();
//				Chat.msg(e.getMessage());
//			}
//			
			engine.set("module", CoerceJavaToLua.coerce(new ModuleFactory()));
			engine.set("checkbox", CoerceJavaToLua.coerce(new CheckBoxFactory()));
			engine.set("slider", CoerceJavaToLua.coerce(new SliderFactory()));
			engine.set("colorpicker", CoerceJavaToLua.coerce(new PickerFactory()));
			engine.set("mode", CoerceJavaToLua.coerce(new ModeFactory()));
			engine.set("select", CoerceJavaToLua.coerce(new SelectFactory()));
			engine.set("position", CoerceJavaToLua.coerce(new PositionFactory()));
			engine.set("input", CoerceJavaToLua.coerce(new InputFactory()));
			engine.set("bind", CoerceJavaToLua.coerce(new BindFactory()));
			engine.set("drag", CoerceJavaToLua.coerce(new DragFactory()));
			engine.set("animation", CoerceJavaToLua.coerce(new AnimFactory()));
			engine.set("sound", CoerceJavaToLua.coerce(new SoundFactory()));
			engine.set("potion", CoerceJavaToLua.coerce(new PotionFactory()));
			engine.set("items", CoerceJavaToLua.coerce(new Items()));
			engine.set("color", CoerceJavaToLua.coerce(new ColorFactory()));
			engine.set("vector", CoerceJavaToLua.coerce(new VectorFactory()));
			engine.set("timer", CoerceJavaToLua.coerce(new TimerFactory()));

			engine.set("___conus228___", CoerceJavaToLua.coerce(this));
			
			engine.set("gl11", CoerceJavaToLua.coerce(new GL11Base()));
			engine.set("client", CoerceJavaToLua.coerce(new Client()));
			engine.set("player", CoerceJavaToLua.coerce(new Player()));
			engine.set("render", CoerceJavaToLua.coerce(new Render()));
			engine.set("world", CoerceJavaToLua.coerce(new WorldBase()));
			engine.set("math", CoerceJavaToLua.coerce(new MathBase()));
			engine.set("events", CoerceJavaToLua.coerce(new Events()));
			engine.set("files", CoerceJavaToLua.coerce(new FilesBase()));
			
			script.call();
	    }
	}
	
	private void parse(String name, Class<?> class1) throws Exception {
		engine.set(name, CoerceJavaToLua.coerce(class1));

		//run(String.format("local %s = luajava.bindClass(\"%s\", \"%s\");", name, name, class1.getCanonicalName()));
	}
	
}