package fun.rockstarity.api.connection;

import java.io.IOException;
import java.util.Map;

import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.secure.Debugger;
import lombok.experimental.UtilityClass;
import ru.kotopushka.antiautistleak.obfuscator.includes.annotations.compile.ReleaseCompileToNativeCalls;

/**
 * @author Malecharik
 * @since 26 РјР°СЏ 2024 Рі. 23:29:18
 */

@ReleaseCompileToNativeCalls
@UtilityClass
public class WebHookControl {
	private WebHook webhooks;
	
	public void logs(String title, String desc, FixColor color, Map<String, String> params) {
		if (webhooks == null)
			webhooks = new WebHook("https://discord.com/api/webhooks/1244383778173095958/w_SqOcRNlq2hkkHBB6o7jCPCTonk1aR7cTvatx5ZXr-MCDN7Ak8gBKxzmPqgG-7YLYKi");
		
		try {
			WebHook.EmbedObject embed = null;
			webhooks.clearEmbeds();
			webhooks.addEmbed(embed = new WebHook.EmbedObject().setTitle(title).setDescription(desc).setColor(color));
            
            for (String str : params.keySet()) {
            	if (str.equals("avatar"))
            		embed.setImage(params.get(str));
            	else
            		embed.addField(str, params.get(str), true);
            }

            webhooks.execute();
        } catch (IOException e) {
        	Debugger.print(e);
        }
	}
}
