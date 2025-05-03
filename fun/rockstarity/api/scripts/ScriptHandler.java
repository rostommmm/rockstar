package fun.rockstarity.api.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import fun.rockstarity.Rockstar;
import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.secure.KeyGeneration;
import fun.rockstarity.api.helpers.secure.Web;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.Setting;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import fun.rockstarity.api.render.ui.clickgui.GlyphType;
import fun.rockstarity.api.render.ui.clickgui.SettingRect;
import fun.rockstarity.api.scripts.wrappers.base.ScriptBase;
import fun.rockstarity.api.secure.Debugger;
import lombok.Getter;
import net.minecraft.util.text.TextFormatting;

public class ScriptHandler implements IAccess {
	private final File dir = new File(rock.getPath(), "scripts");
	@Getter
	private ArrayList<Script> scripts = new ArrayList<>();

	public final Comparator<Object> SORT_METHOD = Comparator.comparing(m -> {
		Module module = (Module) m;
		if (module.getInfo() == null) return "null";
		return module.getInfo().name();
	}).reversed();
	
	public ScriptHandler() {
		scripts.clear();

		if (!dir.exists()) {
			dir.mkdirs();
		}
		if (dir != null) {
			File[] files = dir
					.listFiles(f -> !f.isDirectory() && FilenameUtils.getExtension(f.getName()).equals("lua"));
			for (File f : files) {
				ArrayList<String> s3 = new ArrayList<>();
				
				try {
					FileReader fileReader = new FileReader(f);
					BufferedReader bufferedReader = new BufferedReader(fileReader);

					String line;
					while ((line = bufferedReader.readLine()) != null) {
						s3.add(line);
					}

					bufferedReader.close();
					fileReader.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				StringBuilder sb = new StringBuilder();
				for (String element : s3) {
					sb.append(element).append("\n");
				}

				create(ScriptBase.getName(), ScriptBase.getDesc(), ScriptBase.getDevs(),
						sb.toString(), f, false);
			}
		}
		
		HashMap<String, String> map = new HashMap<>();
		
		map.put("id", KeyGeneration.encrypt("" + rock.getUser().getId()));
		
//		String str = Web.protectedPostRequest(map, "https://rockstar.moscow/api/v1/premium/client/script/list.php").trim() + "";

		try {
//			if (str.contains("PHP_EOL")) {
//				String[] msgs = str.split("PHP_EOL");
//				for (String line : msgs) {
//					String[] data = line.split("☭");
//					create(data[0], ScriptBase.getDesc(), ScriptBase.getDevs(),
//								KeyGeneration.staticDecrypt(data[1]), null, true);
//				}
//			}
		} catch (Exception e) {
			Debugger.print(e);
		}
		
	}

	private void create(String name, String desc, String[] devs, String code, File f, boolean market) {
		Script script = new Script(name, desc, devs, code, f, market);
		try {
			script.load();
			script.setName(market ? name + TextFormatting.GRAY + " (" + ScriptBase.getName() + ")" : ScriptBase.getName());
			script.setDesc(ScriptBase.getDesc());
			script.setDevs(ScriptBase.getDevs());
		} catch (Exception e) {
			script.setName("Error");
			script.setDesc("Error");
			script.setDevs(new String[] {"Error"});
			e.printStackTrace();
		}
		scripts.add(script);
		script.getScriptModules().addAll(ScriptBase.getMod());
		ScriptBase.reset();
		script.unload();
	}
	
	public List<Script> getEnabledScripts() {
		return scripts.stream().filter(Script::isEnabled).collect(Collectors.toList());
	}

	public LuaValue invoke(Globals env, String function, LuaValue... args) {
		LuaValue func = env.get(LuaValue.valueOf(function));

		if (!func.isnil()) {
			Varargs arguments = LuaValue.varargsOf(args);
			LuaValue result = func.invoke(arguments).arg1();
			if (!result.isnil()) {
				return result;
			}
		}

		return null;
	}
	
	public void reload() {
		rock.getScriptSender().clear();
		
		for (Script script : rock.getScriptHandler().getEnabledScripts()) {
			script.unload();
			
			script.setEnabled(false);
		}
		rock.setScriptHandler(new ScriptHandler());
		
		rock.getClickGui().getWindow().getRenderer().getSettings().clear();
		
	}
	
	public void reloadModules() {
		if (rock.getClickGui() == null) return;
		
		
		rock.getClickGui().getWindow().getRenderer().getSettings().clear();
		
		for (Category cat : Category.values()) {
			int i = cat.getIndex();
			rock.getClickGui().getWindow().getRenderer().getGlyphes()[i] = new GlyphType();

			List<Module> modules = new ArrayList<>();
			
			rock.getModules().values().forEach(mod -> modules.add(mod));
			for (Script script : getEnabledScripts()) {
				script.getScriptModules().forEach(mod -> modules.add(mod));
			}
			
			modules.sort(SORT_METHOD);
			
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
}