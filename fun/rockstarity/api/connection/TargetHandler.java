package fun.rockstarity.api.connection;

import java.util.ArrayList;
import java.util.List;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import fun.rockstarity.api.secure.Debugger;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.TextFormatting;

public class TargetHandler implements IAccess {
	
	@Getter
	private final List<String> target = new ArrayList<>();
	
	public void add(String name) {
		if (name.length() > 2) {
			this.target.add(name);
			rock.getAlertHandler().alert("Таргет " + name + " добавлен", AlertType.INFO);
		} else {
			rock.getAlertHandler().alert("Ник таргета слишком короткий", AlertType.ERROR);
		}
	}
	
	public void remove(String name) {
		this.target.remove(name);
		rock.getAlertHandler().alert("Таргет " + name + " удален", AlertType.INFO);
	}
	
	public void clear() {
		this.target.clear();
		rock.getAlertHandler().alert("Список таргетов очищен", AlertType.INFO);
	}
	
	public void list() {
		if (this.target.isEmpty()) {
			Chat.msg("Список таргетов пуст!");
		} else {
			Chat.msg("Список таргетов:");
			try {
				for (String friend : this.target) {
					Chat.msg(TextFormatting.AQUA + "[Rockstar] " + TextFormatting.GRAY + "[" + (this.target.indexOf(friend)+1) + "] " + TextFormatting.WHITE + friend, "Удалить " + TextFormatting.GRAY + friend, () ->  {
						this.target.remove(friend);
						Chat.msg("таргет " + friend + " удален. Обновляю список");
						rock.getCommands().execute("target list");
					});
				}
			} catch (Exception e) {
				Debugger.print(e);
			}
		}
	}
	
	public boolean isTarget(String text) {
		return this.target.contains(text);
	}

	public boolean isTarget(Entity ent) {
		return this.isTarget(ent.getName().getString());
	}
}
