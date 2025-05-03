package fun.rockstarity.api.schedules;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.system.ThreadManager;
import fun.rockstarity.api.render.globals.emotions.instance.EmotionType;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import net.minecraft.entity.player.PlayerEntity;

/**
 * @author ConeTin
 * @since 12 янв. 2025 г.
 */


@UtilityClass
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScheduleServer implements IAccess {

	Socket socket;
	OutputStream output;
	InputStream input;
	PrintWriter writer;
	BufferedReader reader;

	public void init() {
		try {
			socket = new Socket("85.192.24.174", 14252);
			OutputStream output = socket.getOutputStream();
			InputStream input = socket.getInputStream();

			writer = new PrintWriter(output, true);
			reader = new BufferedReader(new InputStreamReader(input));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void finish() {
		try {
			if (output != null)
				output.close();
			if (input != null)
				input.close();
			if (writer != null)
				writer.close();
			if (reader != null)
				reader.close();
			if (socket != null)
				socket.close();
		} catch (IOException e) {
		}
	}

	public void updateSchedules() {
		if (writer != null)
			writer.println("/update");
	}

	
	public void send(Schedule schedule) {
		if (writer != null)
			writer.println(schedule.getAnarchy() + "/" + schedule.getSeconds());
	}

	public void update() {
		ThreadManager.run(() -> {
			if (reader == null) return;
			
			try {
				//Chat.debug("update");
				
					try {
						String message = null;
						while ((message = reader.readLine()) != null) {
							if (message.contains("/")) {
								//Chat.debug("contains");
								String[] splitted = message.split("/");
								
								synchronized (rock.getScheduleManager().getSchedules()) {
									rock.getScheduleManager().add(Integer.parseInt(splitted[0]), Integer.parseInt(splitted[1]));
								}
							}
						}
					} catch (Exception e) {
						//e.printStackTrace();
						init();
					}
				
			} catch (Exception e) {
			}
		});
	}

}
