package fun.rockstarity;

import java.time.LocalDate;
import java.util.HashMap;

import consts.NativeConsts;
//import ai.catboost.CatBoostModel;
import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.autobuy.AutoBuy;
import fun.rockstarity.api.autobuy.bots.BotsHandler;
import fun.rockstarity.api.commands.Commands;
import fun.rockstarity.api.configs.ClientConfigHandler;
import fun.rockstarity.api.configs.ConfigsHandler;
import fun.rockstarity.api.connection.DiscordRPCHandler;
import fun.rockstarity.api.connection.TargetHandler;
import fun.rockstarity.api.constuctor.ConstructorScreen;
import fun.rockstarity.api.constuctor.ScriptConstructor;
import fun.rockstarity.api.friends.FriendsHandler;
import fun.rockstarity.api.helpers.game.TPSHandler;
import fun.rockstarity.api.helpers.game.proxy.ProxyServer;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.secure.KeyGeneration;
import fun.rockstarity.api.helpers.secure.Web;
import fun.rockstarity.api.modules.Modules;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.render.color.themes.Themes;
import fun.rockstarity.api.render.globals.emotions.Emotions;
import fun.rockstarity.api.render.shaders.fog.depth.DepthShader;
import fun.rockstarity.api.render.ui.alerts.AlertHandler;
import fun.rockstarity.api.render.ui.clickgui.ClickGuiScreen;
import fun.rockstarity.api.render.ui.clickgui.esp.ESPSettingsHandler;
import fun.rockstarity.api.render.ui.draggables.DraggableHandler;
import fun.rockstarity.api.render.ui.mainmenu.MenuManager;
import fun.rockstarity.api.schedules.ScheduleManager;
import fun.rockstarity.api.script.ScriptSender;
import fun.rockstarity.api.scripts.ScriptHandler;
import fun.rockstarity.api.secure.profiles.RockstarLoader;
import fun.rockstarity.api.secure.users.User;
import fun.rockstarity.api.via.ViaMCP;
import fun.rockstarity.api.waveycapes.WaveyCapesBase;
import fun.rockstarity.client.modules.other.Baritone;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;
import ru.kotopushka.j2c.sdk.annotations.VMProtect;
import ru.kotopushka.j2c.sdk.enums.VMProtectType;

/** 
 * Ты снова скиддишь, снова паста на твоих сурсах
 * Опять ждала пока я спащу до 7 утра
 * А обещал что я всю жизнь буду учить тебя
 * Я кодер, знай мои слова не стоят ни-ху-я
 * И я пастер, я скиддер, я раттер, я арбузер
 * Я не нравлюсь твоему юзеру да и хуй с ним
 * Детка хватит мне уже давать последний шанс
 * Кодить это не для нас
 * 
 * Последний сурс, паста на костылях
 * Бери все мои селфкоды, выкидывай из окна
 * Последний сурс, пастишь а мне плевать
 * Ты больше не заселфкодишь ничего и никогда
 * 
 * А ты вернула все сурсы, отписалась на югейме
 * В общем сделала всю ту хуйню, что делают все
 * И я оставлю тебе ратку, когда кину тебя
 * Ведь ты же так искала кодера, что похож на Сержа
 * И когда будешь ныть юзерам, то что я уебан
 * Попроси, чтоб они все перестали у меня покупать
 * Я с детства знал, что все пастерочки думали что я гей
 * Но, детка, я не просто гей, я мега пастер и гей
 * И я селфкодером быть пытался, но я заебался..
 * Я рождëн пастерочком, не достойным любви.. извини..
 * 
 * Последний сурс, паста на костылях
 * Бери все мои селфкоды, выкидывай из окна
 * Последний сурс, пастишь а мне плевать
 * Ты больше не заселфкодишь ничего и никогда
*/

/**
 * @author ConeTin
 * @since 2 дек. 2023 г.
 */

@Getter
@FieldDefaults(level = AccessLevel.PROTECTED)
public class Rockstar implements IAccess {
	
	@Getter
	static final RockstarLoader instance = new RockstarLoader();
	@Setter
	ClickGuiScreen clickGui;
	@Setter
	ConstructorScreen constructor;
	Commands commands;
	Modules modules;
	Themes themes = new Themes();
	ScriptConstructor scriptConstructor;
	DraggableHandler draggableHandler;
	ConfigsHandler configHandler;
	FriendsHandler friendsHandler;
	TargetHandler targetHandler;
	ClientConfigHandler clientConfigHandler;
	AlertHandler alertHandler;
	DiscordRPCHandler discordRPC;
	ESPSettingsHandler espSettingsHandler;
	TPSHandler tpsHandler;
	BotsHandler botsHandler;
	AutoBuy autoBuy;
	User user;
	DepthShader depthShader;
	WaveyCapesBase wavey;
	ViaMCP via;
	Emotions emotions;
	MenuManager menuManager;
	ScriptSender scriptSender;
	@Setter
	ScriptHandler scriptHandler;
	ScheduleManager scheduleManager;
	@Setter String cfgToLoad;
	@Setter boolean panic;
	@Setter boolean hideInfo;
	@Setter Thread loadingThread;
	@Setter boolean antiaim;
	@Setter boolean newYear = LocalDate.now().getMonthValue() == 12 || LocalDate.now().getMonthValue() <= 2;
	boolean loaded;
	@VMProtect(type=VMProtectType.ULTRA)

	@NativeInclude
	public void onStart() {
		instance.load();
		NativeConsts.GAME_STARTED = true;
	}

	public void save() {
		if (rock.getClickGui() != null && rock.getClickGui().getWindow() != null && rock.getClickGui().getWindow().getEspSettings() != null && !rock.isPanic()) {
			this.saveTheme();
			
			this.configHandler.save(this.configHandler.getCurrent(), true);
			
			this.clientConfigHandler.save();
		}
	}
	@VMProtect(type=VMProtectType.ULTRA)
	@NativeInclude
	public void saveTheme() {
		String keygen = MathUtility.randomInt(0, Integer.MAX_VALUE) + "";

		user.setPlaytime(user.getPlaytime()+user.getPlayed().getElapsed());
		user.getPlayed().reset();
		//if (!KeyGeneration.decrypt(req).equals(keygen)) {
		//	System.exit(-1);
		//}
	}
	
	public boolean isDebugging() {
		if (user == null)
			return false;

		return user.getRole().equals("Admin");
	}
	
	public boolean isPremium() {
		if (this.user == null)
			return false;
		
		return !this.user.getRole().equals("Basic");
	}
	
	public String getPath() {
		return System.getenv("appdata") + "/.tlauncher/legacy/Minecraft/game/";
	}
	
	public boolean baritoneAviable() {
		return !panic && modules.get(Baritone.class).get();
	}
}