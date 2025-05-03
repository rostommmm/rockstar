package fun.rockstarity.api.autobuy.logic.tasks;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

/**
 * @author ConeTin
 * @since 21 Р°РїСЂ. 2024 Рі.
 */


public class TaskManager extends ArrayList<CheckTask> {
	
	@Getter
	private final List<CheckTask> blackList = new ArrayList<>();

	public void createTask(String seller, String name, int price, Runnable onSuccess) {
		add(new CheckTask(seller, name, price, onSuccess, () -> {}));
	}
	
	public void createTask(String seller, String name, int price, Runnable onSuccess, Runnable onFake) {
		add(new CheckTask(seller, name, price, onSuccess, onFake));
	}
	
	public boolean haveTasks() {
		return !this.isEmpty();
	}
	
	public CheckTask getNearestTask() {
		return this.get(0);
	}
	
}
