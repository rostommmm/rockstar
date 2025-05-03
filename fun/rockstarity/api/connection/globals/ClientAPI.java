package fun.rockstarity.api.connection.globals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

import lombok.experimental.UtilityClass;

/**
 * @author ConeTin
 * @since 13 РёСЋРЅ. 2024 Рі.
 */

@UtilityClass
public class ClientAPI {

    public final HashMap<String, GlobalsUser> USERS = new HashMap<>();

    public void update(String data) {
        if (data == null) return;
        USERS.clear();
        String[] userDatas = data.split("&");
        for (String userData : userDatas) {
            String[] parts = userData.split("/");
            if (parts.length < 2) continue;
            String name = parts[0];
            String client = parts[1];
            int model = parts.length >= 3 ? Integer.parseInt(parts[2]) : 0;
            String taksa = parts.length >= 4 ? parts[3] : "0";
            USERS.put(name, new GlobalsUser(name, client, model, taksa));
        }
    }

    public GlobalsUser getUser(String nick) {
        try {
            for (Entry<String, GlobalsUser> user : USERS.entrySet()) {
                if (nick.contains(user.getValue().getClient())) {
                    return user.getValue();
                }
            }
        } catch (Exception e) {
        }
        GlobalsUser user = USERS.get(nick);
        return user;
    }
    
    public String getClient(String nick) {
        GlobalsUser user = getUser(nick);
        if (user == null) return null;
        return user.getClient();
    }

	public boolean isRockstar(String nick) {
		GlobalsUser user = getUser(nick);
		if (user == null) return false;
		return user.getClient().equals("rockstar");
	}
    
	public int getModel(String nick) {
    	GlobalsUser user = getUser(nick);
        if (user == null) return 0;
        return user.getModel();
    }
  
	public String getTaksa(String nick) {
    	GlobalsUser user = getUser(nick);
        if (user == null) return "0";
        return user.getTaksa();
    }
    
}
