package fun.rockstarity.api.connection.globals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.system.ThreadManager;
import fun.rockstarity.api.render.globals.emotions.instance.EmotionType;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import net.minecraft.entity.player.PlayerEntity;

/**
 * @author ConeTin
 * @since 13 июн. 2024 г.
 */

@UtilityClass
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SyncServer implements IAccess {

	Socket socket;
	OutputStream output;
	InputStream input;
	PrintWriter writer;
	BufferedReader reader;

	public void init() {
		try {
			socket = new Socket("85.192.24.174", 14251);
			OutputStream output = socket.getOutputStream();
			InputStream input = socket.getInputStream();

			writer = new PrintWriter(output, true);
			reader = new BufferedReader(new InputStreamReader(input));

		} catch (Exception e) {
			//e.printStackTrace();
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

	public void send(EmotionType type) {
		if (writer != null)
			writer.println(mc.getSession().getUsername() + "/" + type.getName());
	}


	public void update() {
		if (mc.world == null) {
			return;
		}
		
		ThreadManager.run(() -> {
			if (reader == null) {
				init();
				return;
			}
			
			try {
				//Chat.debug("update");
				
					try {
						String message = null;
						if ((message = reader.readLine()) != null) {
							if (message.contains("/")) {
								//Chat.debug("contains");
								String[] splitted = message.split("/");
								if (mc.world == null) {
									return;
								}
								for (PlayerEntity player : mc.world.getPlayers()) {
			    					if (player.getName().getString().equals(splitted[0]) && player != mc.player) {
			    						rock.getEmotions().playEmotion(player, EmotionType.getFromName(splitted[1]));
			    						break;
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
