package fun.rockstarity.api.schedules;

import java.util.Iterator;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventWorldChange;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.system.ThreadManager;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.world.Difficulty;

/**
 * @author ConeTin
 * @since 12 янв. 2025 г.
 */

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScheduleRecorder implements IAccess {
	
	final TimerUtility sinceJoin = new TimerUtility();
	final TimerUtility sinceRemove = new TimerUtility();
	final TimerUtility sinceCommand = new TimerUtility();

	boolean sended, synced, check;
	int anarchy;

	public void onEvent(Event event) {
		if (event instanceof EventUpdate) {
			if (!synced) {
				ThreadManager.run(() -> {
					ScheduleServer.init();
					ScheduleServer.updateSchedules();
					ScheduleServer.update();
				});
				synced = true;
			}
			
			// Проверка на анке ли челик
			if (!onAnarchy()) {
				anarchy = -1;
				return;
			}
			
			// Обновляем анархию и время захода
			if (anarchy != Server.FT_ANARCHY) {
				anarchy = Server.FT_ANARCHY;
				sinceJoin.reset();
				sended = false;
			}
			
			synchronized(rock.getScheduleManager().getSchedules()) {
				Iterator i = rock.getScheduleManager().getSchedules().iterator();
				while (i.hasNext()) {
					Schedule schedule = (Schedule) i.next();
					if (schedule.getSeconds() <= 2) {
						sinceRemove.reset();
						check = true;
					}
				}
			}
			
			// Если прошла минута с захода и еще не отправляли сообщение
			if (sinceJoin.passed(60_000) && !sended) {
				mc.player.sendChatMessage("/event delay");
				sinceCommand.reset();
				sended = true;
			}
			
			// После исчезновения ивента через 10 секунд опять чекаем для того чтобы добавить серв обратно
			if (sinceJoin.passed(60_000) && sinceRemove.passed(10_000) && check) {
				mc.player.sendChatMessage("/event delay");
				sinceCommand.reset();
				check = false;
			}
		}
		
		if (event instanceof EventReceivePacket e
				&& e.getPacket() instanceof SChatPacket packet
				&& onAnarchy()) {
	        String message = packet.getChatComponent().getString().toLowerCase();
	        
	        try {
	        	if (message.contains("идёт страшный бой")) {
	        		return;
	        	}
	        	
	        	if (message.contains("[1] до следующего ивента: ")) {
		        	String time = message.replace("[1] до следующего ивента: ", "").replace(" сек", "").replace(".", "");
		        	parse(time);
		        }
		        
		        if (message.contains("|| статус:")) {
		        	String time = message.replaceAll("[^\\d.]", "").replace(" сек", "").replace(".", "");
		        	parse(time);
		        }
	        } catch (Exception e2) {
	        	System.out.println(message);
	        	e2.printStackTrace();
	        }
	        
	        if (!sinceCommand.passed(150)) {
	        	e.cancel();
	        }
		}
	}
	
	private void parse(String time) {
		try {
			boolean numeric = time.matches("[-+]?[0-9]*\\.?[0-9]+");
			
			if (time.isEmpty() || !numeric)
				return;
			
			ScheduleServer.send(rock.getScheduleManager().add(anarchy, Integer.parseInt(time)));
		} catch (Exception e2) {
	    	System.out.println(time);
	    	e2.printStackTrace();
	    }
	}
	
	private boolean onAnarchy() {
		return mc.world.getDifficulty() != Difficulty.EASY && Server.isFT();
	}
	
}
