package fun.rockstarity.api.connection.globals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import fun.rockstarity.api.ClientInfo;
import fun.rockstarity.api.IAccess;
import fun.rockstarity.client.modules.other.Globals;
import fun.rockstarity.client.modules.render.Cosmetics;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;

/**
 * @author ConeTin
 * @since 13 июн. 2024 г.
 */

@UtilityClass
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServerAPI implements IAccess {

	Socket socket;
	OutputStream output;
	InputStream input;
    PrintWriter writer;
    BufferedReader reader;
    
	public void init() {
		try {
			socket = new Socket("85.192.24.174", 25565);
	        OutputStream output = socket.getOutputStream();
	        InputStream input = socket.getInputStream();
	        
			writer = new PrintWriter(output, true);
			reader = new BufferedReader(new InputStreamReader(input));
			
			
			writer.println(mc.getSession().getUsername()+"/"+ClientInfo.NAME.toString().toLowerCase()+"/"+rock.getModules().get(Cosmetics.class).getLocal()+"/"+(rock.getModules().get(Cosmetics.class).getPet().get() ? "taksa_default" : 0));

		} catch (Exception ex) {
			ex.printStackTrace();
        }
	}
	
	public void finish() {
        try {
            if (output != null) output.close();
            if (input != null) input.close();
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
        }
    }
	
	public void updateName() {
		updateName(mc.getSession().getUsername());
	}

	public void updateName(String name) {
		if (writer != null)
		writer.println(name+"/"+ClientInfo.NAME.toString().toLowerCase()+"/"+rock.getModules().get(Cosmetics.class).getLocal()+"/"+(rock.getModules().get(Cosmetics.class).getPet().get() ? "taksa_default" : 0));
	}
	
	public String getClients() {
		if (writer == null || reader == null) {
			init();
			return null;
		}
		
		try {
			writer.println("/getClients");
			return reader.readLine();
		} catch (Exception e) {
			ServerAPI.init();
			e.printStackTrace();
			return null;
		}
	}
	
}
