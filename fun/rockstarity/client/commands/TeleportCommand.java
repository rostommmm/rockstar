package fun.rockstarity.client.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fun.rockstarity.api.commands.CmdInfo;
import fun.rockstarity.api.commands.Command;
import fun.rockstarity.api.commands.CommandParameter;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.events.list.render.EventRender3D;
import fun.rockstarity.api.events.list.render.ui.EventRenderPreUI;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import fun.rockstarity.api.render.ui.rect.Rect;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

@NativeInclude
@CmdInfo(names = {"tp", "teleport"}, desc = "Телепортирует на указанные координаты")
public class TeleportCommand extends Command {
	
	CommandParameter cancel = new CommandParameter(this, "cancel");
	Vector3d task, packetPos;
	
	@Override
	public void execute(String[] args) {
	    if (args.length == 0) {
	        Chat.msg(".tp <x> <y> <z>");
	        this.error();
	        return;
	    }

	    String arg = args[0];

	    if (contains(arg, cancel)) {
	    	Chat.msg("Задача отменена");
	    	task = null;
	    } else {
	    	double x = Double.parseDouble(args[0].replaceAll("[,.]", ""));
            double y = args.length == 3 ? Double.parseDouble(args[1].replaceAll("[,.]", "")) : mc.player.getPosY();
            double z = Double.parseDouble(args[args.length - 1].replaceAll("[,.]", ""));
            
            packetPos = mc.player.getPositionVec();
            task = new Vector3d(x, y, z);
	    }
	}
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventUpdate && task != null) {
			mc.player.setPosition(MathUtility.step((float)mc.player.getPositionVec().x, (float) task.x, 6), MathUtility.step((float)mc.player.getPositionVec().y, (float) task.y, 1), MathUtility.step((float)mc.player.getPositionVec().z, (float) task.z, 6));
			
			if (mc.player.getDistance(task) < 0.1f) {
				task = null;
			}
			
		}
	}
	
}
