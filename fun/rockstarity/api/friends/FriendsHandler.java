package fun.rockstarity.api.friends;

import java.util.ArrayList;
import java.util.Map.Entry;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import fun.rockstarity.api.secure.Debugger;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.TextFormatting;

/**
 * @author ConeTin
 * @since 25 мар. 2024 г.
 */

@Getter
public class FriendsHandler implements IAccess {

	private final ArrayList<Friend> friends = new ArrayList<>();

	/**
	 * Добавляет друга в список друзей
	 *
	 * @param friendName имя друга, которого нужно добавить
	 * @param hiddenName имя друга которое будет отображаться при включенном NameProtect
	 *                   может быть пустым, если это так - будет отображаться оригинальное имя
	 */
	public void add(String friendName, String hiddenName) {
		if (friendName.length() <= 2) {
			rock.getAlertHandler().alert("Имя друга слишком короткое", AlertType.ERROR);
			return;
		}

		if (this.get(friendName) != null) {
			rock.getAlertHandler().alert("Друг с именем" + friendName + " уже существует", AlertType.ERROR);
			return;
		}

		this.friends.add(new Friend(friendName, hiddenName));
		rock.getAlertHandler().alert("Друг " + friendName + " добавлен", AlertType.INFO);

	}

	public void add(String name) {
		add(name, "");
	}

	public void remove(String name) {
		Friend friendToRemove = friends.stream()
				.filter(friend -> friend.getName().equalsIgnoreCase(name))
				.findFirst()
				.orElse(null);

		if (friendToRemove != null) {
			rock.getAlertHandler().alert("Друг " + name + " удален", AlertType.INFO);
			friends.remove(friendToRemove);
		} else {
			rock.getAlertHandler().alert("Друг с именем " + name + " не найден", AlertType.ERROR);
		}
	}

	public void clear() {
		this.friends.clear();
		rock.getAlertHandler().alert("Список друзей очищен", AlertType.INFO);
	}

	/**
	 * Получает друга в виде объекта {@link Friend}
	 * @param name имя игрока, которого нужно получить в качестве друга
	 * @return объект класса {@link Friend} или {@code null}
	 */
	public Friend get(String name) {
		return friends.stream()
				.filter(friend -> friend.getName().equalsIgnoreCase(name))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Получает друга в виде объекта {@link Friend}
	 * @param entity сущность, которую нужно получить в качестве друга
	 * @return объект класса {@link Friend} или {@code null}
	 */
	public Friend get(Entity entity) {
        return get(entity.getName().getString());
    }

	public void list() {
		if (this.friends.isEmpty()) {
			Chat.msg("Список друзей пуст!");
		} else {
			Chat.msg("Список друзей:");
			try {
				for (Friend friend : this.friends) {
					final String friendName = friend.getName();
					Chat.msg(TextFormatting.AQUA + "[Rockstar] " + TextFormatting.GRAY + "[" + (this.friends.indexOf(friend) + 1) + "] " + TextFormatting.WHITE + friendName,
							"Удалить " + TextFormatting.GRAY + friend, () -> {
								this.friends.remove(friend);
								Chat.msg("Друг " + friendName + " удален. Обновляю список");
								rock.getCommands().execute("friend list");
							});
				}
			} catch (Exception e) {
				Debugger.print(e);
			}
		}
	}

	public boolean isFriend(String friendName) {
		return this.friends
				.stream()
				.anyMatch(friend -> friend.getName().equalsIgnoreCase(friendName));
	}

	public boolean isFriend(Entity ent) {
		if (ent == null) return false;
		
		return this.isFriend(ent.getName().getString());
	}
}