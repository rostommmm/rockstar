package fun.rockstarity.api.scripts.wrappers.base;

import java.util.ArrayList;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.scripts.Script;
import lombok.Getter;


public class ScriptBase {
	@Getter
	private static String name, desc;
	@Getter
	private static String[] devs;
	@Getter
	private static ArrayList<Module> mod = new ArrayList<>();
	@Getter
	private Script script;
	
	public ScriptBase(Script script) {
		this.script = script;
	}
	
	public void name(String name) {
		this.name = name;
	}
	
	public void devs(String[] devs) {
		this.devs = devs;
	}
	
	public void devs(String devs) {
		this.devs = new String[] {devs};
	}
	
	public void dev(String devs) {
		this.devs = new String[] {devs};
	}
	
	public void desc(String desc) {
		this.desc = desc;
	}
	
	public static void reset() {
		name = null;
		desc = null;
		devs = null;
		mod.clear();
	}
}