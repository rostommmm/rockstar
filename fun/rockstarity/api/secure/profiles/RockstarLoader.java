package fun.rockstarity.api.secure.profiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import fun.rockstarity.api.render.color.themes.Themes;
import fun.rockstarity.api.render.color.themes.list.DarkTheme;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.mojang.blaze3d.platform.PlatformDescriptors;

//import ai.catboost.CatBoostError;
//import ai.catboost.CatBoostModel;

import fun.rockstarity.Rockstar;
import fun.rockstarity.api.autobuy.AutoBuy;
import fun.rockstarity.api.autobuy.bots.BotsHandler;
import fun.rockstarity.api.commands.Commands;
import fun.rockstarity.api.configs.ClientConfigHandler;
import fun.rockstarity.api.configs.ConfigsHandler;
import fun.rockstarity.api.connection.DiscordRPCHandler;
import fun.rockstarity.api.connection.TargetHandler;
import fun.rockstarity.api.connection.globals.GlobalsThread;
import fun.rockstarity.api.constuctor.ScriptConstructor;
import fun.rockstarity.api.friends.FriendsHandler;
import fun.rockstarity.api.helpers.game.TPSHandler;
import fun.rockstarity.api.helpers.game.proxy.ProxyServer;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.secure.KeyGeneration;
import fun.rockstarity.api.helpers.secure.Web;
import fun.rockstarity.api.modules.Modules;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.render.color.themes.Theme;
import fun.rockstarity.api.render.globals.emotions.Emotions;
import fun.rockstarity.api.render.optimize.culling.processor.CullingProcessor;
import fun.rockstarity.api.render.shaders.fog.depth.DepthShader;
import fun.rockstarity.api.render.ui.alerts.AlertHandler;
import fun.rockstarity.api.render.ui.clickgui.esp.ESPSettingsHandler;
import fun.rockstarity.api.render.ui.draggables.DraggableHandler;
import fun.rockstarity.api.render.ui.mainmenu.MenuManager;
import fun.rockstarity.api.render.ui.mainmenu.loading.LoadingScreen;
import fun.rockstarity.api.render.ui.mainmenu.loading.LoadingStage;
import fun.rockstarity.api.schedules.ScheduleManager;
import fun.rockstarity.api.script.ScriptSender;
import fun.rockstarity.api.scripts.ScriptHandler;
import fun.rockstarity.api.secure.Debugger;
import fun.rockstarity.api.secure.nativeapi.NativeHelper;
import fun.rockstarity.api.secure.users.User;
import fun.rockstarity.api.via.ViaMCP;
import fun.rockstarity.api.waveycapes.WaveyCapesBase;
import fun.rockstarity.client.commands.AdminCommand;
import fun.rockstarity.client.modules.combat.Aura;
import fun.rockstarity.client.modules.move.AutoUP;
import fun.rockstarity.client.modules.move.PeekAssist;
import fun.rockstarity.client.modules.other.AUTOFTDUPE;
import fun.rockstarity.client.modules.other.AutoKit;
import fun.rockstarity.client.modules.other.Avg;
import fun.rockstarity.client.modules.other.Recorder;
import fun.rockstarity.client.modules.other.Test;
import fun.rockstarity.client.modules.player.InvCleaner;
import fun.rockstarity.client.modules.render.Interface;
import ru.kotopushka.antiautistleak.obfuscator.includes.annotations.auth.ReleaseNativeAuth;
import ru.kotopushka.antiautistleak.obfuscator.includes.annotations.compile.ReleaseCompileToNativeCalls;
import ru.kotopushka.j2c.sdk.annotations.CompileUserIdentification;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;
import ru.kotopushka.j2c.sdk.annotations.VMProtect;
import ru.kotopushka.j2c.sdk.enums.VMProtectType;

/**
 * @author ConeTin
 * @since 13 июн. 2024 г.
 */

@ReleaseNativeAuth
@ReleaseCompileToNativeCalls
public class RockstarLoader extends Rockstar {
	
	/**
	 * Я написал этот класс в надежде что его не будет читать никто посторонний.
	 */
	@VMProtect(type=VMProtectType.ULTRA)
    @CompileUserIdentification
    @NativeInclude
	public void load() {
		LoadingStage.AUTH.activate();
		LoadingStage.stage("Вхожу в аккаунт..");
		String[] user = null;
		this.user = new User(
				1, 	// Айди
				"itskekoff", 					// Юзернейм
				1,  // Юид
				"https://i.imgur.com/ZyfPkQz.jpeg", 					// Ссылка на аватар
				"Admin", 					// Роль
				System.currentTimeMillis(), 	// Запуски
				System.currentTimeMillis(), 	// Наигранное время
				System.currentTimeMillis(), 	// Костыль для отсчета времени текущей сессии
				false	// Гифка ава или не гифка
		);
		LoadingScreen.AUTH = false;

        LoadingStage.stage("Получаю темы..");
		
		try {
			String json = NativeHelper.getThemes();
			JSONParser parser = new JSONParser();
			JSONArray array = (JSONArray) parser.parse(json);
			
			for (Object style : array.toArray()) {
				JSONObject styleJson = (JSONObject) style;
				
				String name = styleJson.get("name")+"";
				
				ArrayList<FixColor> colors = new ArrayList<>();
				
				for (Object obj : ((JSONArray)styleJson.get("colors")).toArray()) {
					String str = ""+obj;
					String[] splitted = str.split(",");
					int red = Integer.parseInt(splitted[0]);
					int green = Integer.parseInt(splitted[1]);
					int blue = Integer.parseInt(splitted[2]);
					colors.add(new FixColor(red, green, blue));
				}
				
				FixColor[] massive = new FixColor[colors.size()];
				
				Style style1 = new Style(name, colors.toArray(massive));
				
				if (Style.getCurrent() == null)
					Style.setCurrent(style1);
				
				Style.getStyles().add(style1);
			}
		} catch (Exception e) {
			Debugger.print(e);
		}
		
		LoadingStage.CLIENT.activate();
		
		LoadingStage.stage("Загружаю скрипты (1)..");
		scriptSender = new ScriptSender();
		scriptSender.clear();
		
		LoadingStage.stage("Загружаю эмоции..");
		emotions = new Emotions();
		
		LoadingStage.stage("Загружаю шейдеры (1)..");
		depthShader = new DepthShader();
		
		LoadingStage.stage("Загружаю драггейблы..");
		draggableHandler = new DraggableHandler();
		
		LoadingStage.stage("Загружаю менеджер меню..");
		menuManager = new MenuManager();
		
		LoadingStage.stage("Загружаю функции..");
		modules = new Modules();
		
		LoadingStage.stage("Загружаю команды..");
		commands = new Commands();
		
		LoadingStage.stage("Загружаю красивый плащ..");
		wavey = new WaveyCapesBase();
		
		LoadingStage.stage("Загружаю скрипты (2)..");
		scriptConstructor = new ScriptConstructor();
		
		LoadingStage.stage("Загружаю скрипты (3)..");
		scriptHandler = new ScriptHandler();
		
		LoadingStage.stage("Загружаю конфиги..");
		configHandler = new ConfigsHandler();
		
		LoadingStage.stage("Загружаю друзей..");
		friendsHandler = new FriendsHandler();
		
		LoadingStage.stage("Загружаю целей..");
		targetHandler = new TargetHandler();
		
		LoadingStage.stage("Загружаю менеджер уведомений..");
		alertHandler = new AlertHandler();
		
		LoadingStage.stage("Загружаю via..");
		via = new ViaMCP();
		
		LoadingStage.stage("Загружаю конфиг клиента..");
		clientConfigHandler = new ClientConfigHandler();
		
		LoadingStage.stage("Загружаю Discord RPC..");
		discordRPC = new DiscordRPCHandler();
		
		LoadingStage.stage("Загружаю Drag & Drop ESP..");
		espSettingsHandler = new ESPSettingsHandler();
		
		LoadingStage.stage("Загружаю обработчик TPS..");
		tpsHandler = new TPSHandler();
		
		LoadingStage.stage("Загружаю ботов..");
		botsHandler = new BotsHandler();
		
		LoadingStage.stage("Загружаю события FunTime..");
		scheduleManager = new ScheduleManager();
		
		//ScanEffectShader.INSTANCE.initialize();
		//ScanResultShader.INSTANCE.initialize();
        
//		for (Theme theme : rock.getThemes()) {
//			if (theme.getName().equals(user[6])) rock.getThemes().setCurrent(theme);
//		}
		rock.getThemes().setCurrent(new DarkTheme());
		
//		for (Style style : Style.values()) {
//			if (style.getName().equals(user[7])) Style.setCurrent(style);
//		}
//		Style.setCurrent(Style.values().get(0));
		
		if (rock.isDebugging()) {
			Aura aura = this.modules.get(Aura.class);
			modules.add(new Avg());
			modules.add(new Test());
			modules.add(new fun.rockstarity.client.modules.other.AutoBuy());
			modules.add(new AUTOFTDUPE());
			modules.add(new PeekAssist());
			modules.add(new AutoUP());
			commands.add(new AdminCommand());
		}
		
		if (this.user.getId() == 508
			|| this.user.getId() == 88
			|| this.user.getId() == 243
			|| this.user.getId() == 142
			|| this.user.getId() == 139
			|| this.user.getId() == 134
			|| this.user.getId() == 386
			|| this.user.getId() == 544
			|| this.user.getId() == 150
			|| this.user.getId() == 151
			|| this.user.getId() == 545
			|| this.user.getId() == 268
			|| rock.isDebugging()) {
			//modules.add(new Aura());
			modules.add(new Recorder());
		}
		
		autoBuy = new AutoBuy();

		this.clientConfigHandler.load();
		this.discordRPC.update();
		//this.autoBuy = new AutoBuy();
		
		String keygen1 = MathUtility.randomInt(0, Integer.MAX_VALUE) + "";
		
		HashMap<String, String> data1 = new HashMap<>();
		data1.put("key", KeyGeneration.encrypt(keygen1));
		data1.put("id", KeyGeneration.encrypt(rock.getUser().getId() + ""));
//		String req = Web.protectedPostRequest(data1, "https://rockstar.moscow/api/v1/premium/utility/auth/admin/stats/start.php").trim();
		//if (!KeyGeneration.decrypt(req).equals(keygen1)) {
			//System.exit(-1);
		//}
		
		if (rock.isDebugging()) {
			//rock.setNewYear(true);
			//rock.getModules().add(new Torus());
		}
		
		LoadingStage.stage("Запускаю Globals..");

		CullingProcessor.init();
		GlobalsThread.start();
		
		loaded = true;
	}

	@VMProtect(type=VMProtectType.ULTRA)
    @NativeInclude
	public void handlePotatoPC() {
        String glRenderer = PlatformDescriptors.getGlRenderer();
        String gpuModel = glRenderer.replaceAll(".*?(GTX|GeForce|RTX|Quadro|Radeon)\\s*(\\d{3,4}\\s?(?:Ti|M|Xi)?).*", "$1 $2");
        boolean normalGPU = !hasLowGPU(gpuModel);
        
        Interface ui = rock.getModules().get(Interface.class);
        
        ui.getBlur().set(normalGPU);
        ui.getOutline().set(normalGPU);
	}
	
	private boolean hasLowGPU(String gpuModel) {
		java.util.List<String> lowerGpuModels = Arrays.asList("GTX 750 Ti", "GTX 650 Ti", "GTX 550 Ti","GTX 550", "GTX 450 Ti","GTX 250", "GTX 240", "GTX 130");

		return lowerGpuModels.contains(gpuModel);
	}
}
