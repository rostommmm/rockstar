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
import fun.rockstarity.api.render.models.taksa.TaksaAI;
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
@CmdInfo(names={"taksa", "ai"}, desc="Позволяет общаться с таксой")
public class TaksaCommand extends Command {
	
	@Override
	public void execute(String[] args) {
	    if (args.length == 0) {
	        this.error();
	        return;
	    }

	    StringBuilder msg = new StringBuilder();
		for (int i = 0; i < args.length; ++i) {
			msg.append(args[i]).append(" ");
		}
		
		TaksaAI.setLastMsg(msg.toString());
		Chat.msg("Вы", msg.toString());
	}
	
}