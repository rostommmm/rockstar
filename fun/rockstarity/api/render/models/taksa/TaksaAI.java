package fun.rockstarity.api.render.models.taksa;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.player.Move;
import fun.rockstarity.api.helpers.system.ThreadManager;
import lombok.Setter;
import lombok.experimental.UtilityClass;

/**
 * @author ConeTin
 * @since 23 апр. 2025 г.
 */

@UtilityClass
public class TaksaAI implements IAccess {
	
	@Setter String lastMsg;
    String apiKey = "tHrnYE2zr5kcMQ4zzzo7qTslTD9vrjcS";
	
	public String gen(String args) {
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpPost postRequest = new HttpPost("https://api.mistral.ai/v1/agents/completions");
		    
		    // Устанавливаем заголовки с явным указанием кодировки
		    postRequest.setHeader("Content-Type", "application/json; charset=UTF-8");
		    postRequest.setHeader("Accept", "application/json; charset=UTF-8");
		    postRequest.setHeader("Authorization", "Bearer " + apiKey);
		    
		    // Создаем тело запроса с проверкой входных данных
		    JSONObject requestBody = new JSONObject();
		    requestBody.put("agent_id", "ag:5a868f4e:20250423:taksa-ai:840782a8");
		    
		    JSONArray messages = new JSONArray();
		    JSONObject message = new JSONObject();
		    
		    // Проверяем и экранируем входные данные
		    String userContent = (args != null) ? args : "";
		    message.put("role", "user");
		    message.put("content", userContent);
		    
		    messages.put(message);
		    requestBody.put("messages", messages);
		    
		    // Устанавливаем тело запроса с явным указанием кодировки
		    postRequest.setEntity(new StringEntity(requestBody.toString(), StandardCharsets.UTF_8));
            
            // Отправляем запрос и получаем ответ
            try (CloseableHttpResponse response = httpClient.execute(postRequest)) {
                InputStream content = response.getEntity().getContent();
                String result = new BufferedReader(new InputStreamReader(content)).lines().collect(Collectors.joining("\n"));
                
                String answer = new JSONObject(result)
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
                
                return answer;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		return "";
	}

	public String args() {
		List<String> args = new ArrayList<>();
		
		if (mc.player.isSneaking())
			args.add("Игрок на шифте");
		
		if (Move.isMoving())
			args.add("Игрок двигается");
		else
			args.add("Игрок стоит");
		
		if (mc.player.hurtTime > 0)
			args.add("Игрок получает урон");
		
		if (mc.player.isSwimming())
			args.add("Игрок плавает");
		
		if (mc.player.isElytraFlying())
			args.add("Игрок летит на элитрах");
		
		if (mc.player.isOnGround())
			args.add("Игрок на земле");
		
		if (mc.player.isHandActive())
			args.add("Игрок использует " + mc.player.getHeldItem(mc.player.getActiveHand()).getDisplayName().getString());
		
		if (lastMsg != null && !lastMsg.isEmpty())
			args.add("Последнее сообщение от хозяина: " + lastMsg);
		
		return String.join("\n", args);
	}
	
	public void update() {
		TaksaScheduler.getUpdateTimer().reset();

		ThreadManager.run(() -> {
			Chat.msg("Такса", gen(args()));
			lastMsg = "";
		});
	}
	
}
