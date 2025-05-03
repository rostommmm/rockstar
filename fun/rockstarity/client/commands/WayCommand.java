package fun.rockstarity.client.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fun.rockstarity.api.commands.CmdInfo;
import fun.rockstarity.api.commands.Command;
import fun.rockstarity.api.commands.CommandParameter;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.render.EventRender3D;
import fun.rockstarity.api.events.list.render.ui.EventRenderPreUI;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import fun.rockstarity.api.render.ui.rect.Rect;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;
@NativeInclude
@CmdInfo(names = {"way", "gps"}, desc = "Позволяет управлять метками")
public class WayCommand extends Command {
	
	public static Map<String, Vector3d> points = new HashMap<>();
	public static Map<String, Timing> timers = new HashMap<>();
	private final List<Tag> tags = new ArrayList<>();
	
	CommandParameter add = new CommandParameter(this, "add", "save", "create");
	CommandParameter del = new CommandParameter(this, "del", "remove", "delete");
	CommandParameter list = new CommandParameter(this, "list");
	CommandParameter clear = new CommandParameter(this, "clear", "off");
	
	@Override
	public void execute(String[] args) {
	    if (args.length == 0) {
	        Chat.msg(".way <name> <x> <z> (или .way add <name> <x> <z>)");
	        this.error();
	        return;
	    }

	    String arg = args[0];

	    if (args.length == 2 || args.length == 4 || args.length == 5) {
	        arg = "add";
	    }

	    if (contains(arg, add)) {
	        if (args[1].length() > 20) {
	            rock.getAlertHandler().alert("Название не может превышать 20 букв", AlertType.ERROR);
	            return;
	        }

	        if (args.length == 2) {
	            this.add(args[1], mc.player.getPositionVec());
	        } else if (args.length == 4 || args.length == 5) {
	            double x = Double.parseDouble(args[2].replaceAll("[,.]", ""));
	            double y = args.length == 5 ? Double.parseDouble(args[3].replaceAll("[,.]", "")) : 60;
	            double z = Double.parseDouble(args[args.length - 1].replaceAll("[,.]", ""));
	            int hell = mc.world.getDimensionType() == DimensionType.NETHER_TYPE ? 8 : 1;
	            this.add(args[1], new Vector3d(x / hell, y, z / hell));
	        } else {
	            this.error();
	            return;
	        }

	        rock.getAlertHandler().alert("Метка сохранена: " + args[1], AlertType.SUCCESS);
	    } else if (contains(arg, del)) {
	        if (points.containsKey(args[1])) {
	            this.remove(args[1]);
	            rock.getAlertHandler().alert("Метка удалена: " + args[1], AlertType.ERROR);
	        } else {
	            rock.getAlertHandler().alert("Метка с именем " + args[1] + " не найдена", AlertType.ERROR);
	        }
	    } else if (contains(arg, list)) {
	        if (points.isEmpty()) {
	            msg("Список меток пуст");
	        } else {
	            msg("Список меток: ");
	            try {
	                for (Entry<String, Vector3d> way : points.entrySet()) {
	                    Chat.msg(TextFormatting.AQUA + "[Rockstar] " + TextFormatting.WHITE + way.getKey() + " - " + TextFormatting.GRAY + "(" + (int)way.getValue().x + ", " + (int)way.getValue().y + ", " + (int)way.getValue().z + ")", "Удалить метку " + TextFormatting.GRAY + way.getKey(), () ->  {
	                        this.remove(way.getKey());
	                        msg("Метка удалена. Обновляю список");
	                        rock.getCommands().execute("way list");
	                    });
	                }
	            } catch (Exception e) {
	            }
	        }
	    } else if (contains(arg, clear)) {
	        points.clear();
	        rock.getAlertHandler().alert("Все метки очищены!", AlertType.INFO);
	    }
	}
	
	@Override
	public void onEvent(Event event) {
		
		if (event instanceof EventRenderPreUI e) {
			this.render(e);
		}
		
		if (event instanceof EventRender3D) {
			this.updateTags();
		}
	}
	
	private void render(EventRenderPreUI event) {
		String toRemove = null;
		
		for (Tag tag : tags) {
			Render.scale(tag.x, tag.y, 0.8f);
			
			float size = 30;
			Type type = Type.DEFAULT;
			
			if (tag.getEntity().getKey().toLowerCase().contains("мистический") || tag.getEntity().getKey().toLowerCase().contains("алтарь")) {
				type = Type.MYST;
			} else if (tag.getEntity().getKey().toLowerCase().contains("маяк")) {
				type = Type.BEACON;
			} else if (tag.getEntity().getKey().toLowerCase().contains("вулкан")) {
				type = Type.VULCAN;
			}
			
			Render.image(String.format("icons/gps/%s.png", type), tag.x - size / 2, tag.y - size / 2, size, size, FixColor.WHITE);
			
			if (type == Type.DEFAULT)
				Round.draw(event.getMatrixStack(), new Rect(tag.x - 11f / 2, tag.y - 15 / 2, 11, 11), 6, Style.getMain());
			
			Timing timer = timers.get(tag.getEntity().getKey());
			String name = tag.getEntity().getKey() + (timer != null ? " - " + (int) (timer.sec - timer.timer.getElapsed() / 1000) + " сек" : "");
			bold.get(17).draw(event.getMatrixStack(), name, tag.x - bold.get(17).getWidth(name) / 2, tag.y + 13,rock.getThemes().getDarkTheme().getTextFirstColor());
			bold.get(15).draw(event.getMatrixStack(), (int) mc.player.getPositionVec().distanceTo(tag.getEntity().getValue()) + "m", tag.x - bold.get(17).getWidth((int) mc.player.getPositionVec().distanceTo(tag.getEntity().getValue()) + "m") / 2, tag.y + 22, rock.getThemes().getDarkTheme().getTextSecondColor());
			
			Render.end();
			
			if (timer != null && (int) (timer.sec - timer.timer.getElapsed() / 1000) < 0) {
				toRemove = tag.getEntity().getKey();
			}
		}
		
		if (toRemove != null) {
			points.remove(toRemove);
		}
	}
	
	private void updateTags() {
        tags.clear();
        for (Entry<String, Vector3d> way : points.entrySet()) {
            double x = way.getValue().x, y = way.getValue().y, z = way.getValue().z;
            Vector2f pos = Render.projectf(x, y, z);
            if (pos == null) continue;
            //Chat.debug(PositionTracker.isInView(way.getValue().subtract(mc.getRenderManager().info.getProjectedView())));
            //if (PositionTracker.isInView(way.getValue())) {
            	tags.add(new Tag(way, (float) pos.x, (float) pos.y));
            //}
        }
    }
	
	public void add(String name, Vector3d pos) {
    	points.put(name, pos);
    }
	
	public void add(String name, Vector3d pos, long time) {
    	points.put(name, pos);
    	timers.put(name, new Timing(time));
    }

    public void remove(String name) {
    	points.remove(name);
    }
    
    public JsonObject save() {
        JsonObject json = new JsonObject();
        for (Map.Entry<String, Vector3d> entry : points.entrySet()) {
            JsonObject position = new JsonObject();
            position.addProperty("x", entry.getValue().x);
            position.addProperty("y", entry.getValue().y);
            position.addProperty("z", entry.getValue().z);
            json.add(entry.getKey(), position);
        }
        return json;
    }

    public void load(JsonObject json) {
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            JsonObject position = entry.getValue().getAsJsonObject();
            double x = position.get("x").getAsDouble();
            double y = position.get("y").getAsDouble();
            double z = position.get("z").getAsDouble();
            this.add(entry.getKey(), new Vector3d(x, y, z));
        }
    }
	
	@AllArgsConstructor
	public static class Tag {
		@Getter
		private Entry<String, Vector3d> entity;
		public float x, y;
	}
	
	@AllArgsConstructor
	public static class Timing {
		private final TimerUtility timer = new TimerUtility();
		public float sec;
	}
	
	
	@AllArgsConstructor
	public static enum Type {
		DEFAULT("Метка", "point"),
		BEACON("Маяк убийца", "beacon"),
		METEOR("Метеоритный дождь", "meteor"),
		MYST("Мистический сундук", "myst"),
		VULCAN("Вулкан", "vulcan");
		
		String name, iconName;
		
		@Override
		public String toString() {
			return iconName;
		}
	}
}
