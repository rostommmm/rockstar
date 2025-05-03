package fun.rockstarity.api.render.globals.marks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.system.ThreadManager;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import net.minecraft.client.gui.overlay.PlayerTabOverlayGui;
import net.minecraft.util.math.vector.Vector3d;

/**
 * @author ConeTin
 * @since 12 янв. 2025 г.
 */


@UtilityClass
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MarkServer implements IAccess {

	Socket socket;
	OutputStream output;
	InputStream input;
	PrintWriter writer;
	BufferedReader reader;

	public void init() {
		try {
			socket = new Socket("85.192.24.174", 14253);
			OutputStream output = socket.getOutputStream();
			InputStream input = socket.getInputStream();

			writer = new PrintWriter(output, true);
			reader = new BufferedReader(new InputStreamReader(input));
		} catch (Exception e) {
		//	e.printStackTrace();
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

	
	public void send(Mark mark) {
		if (writer != null)
			writer.println(mc.player.getNameClear() + "/" + mark.getUser() + "/" + mark.getPosition().x + "/" + mark.getPosition().y + "/" + mark.getPosition().z);
	}

	public void update() {
		ThreadManager.run(() -> {
			if (reader == null) {
				init();
				return;
			}
			
			try {
				//Chat.debug("update");
				
					try {
						String message = null;
						while ((message = reader.readLine()) != null) {
							if (message.contains("/")) {
								//Chat.debug("contains");
								String[] splitted = message.split("/");
								
								synchronized (MarkManager.getMarks()) {
									for (String player : PlayerTabOverlayGui.getPlayersNames()) {
										if (player.contains(splitted[0])) {
											Mark mark = new Mark(splitted[0], new Vector3d(Double.parseDouble(splitted[2]), Double.parseDouble(splitted[3]), Double.parseDouble(splitted[4])));
								 			mark.getShowing().finish();
											MarkManager.getMarks().add(mark);
										}
									}
								}
							}
						}

					} catch (Exception e) {
						e.printStackTrace();
						init();
					}

			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

}
