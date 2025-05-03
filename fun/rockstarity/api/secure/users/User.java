package fun.rockstarity.api.secure.users;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.render.Gifs;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.rect.Rect;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author ConeTin
 * @since 13 дек. 2023 г.
 */

@Getter @Setter
@AllArgsConstructor
public class User {

	private final int id;
	private final String name;
	private final int uid;
	private final String avatar;
	private final String role;
	private final long starts;
	private long playtime;
	@Getter private final TimerUtility played = new TimerUtility();
	private final long startTime;
	private final boolean gif;
	
	public void drawAvatar(MatrixStack ms, float x, float y, float size, float round, float alpha) {
		if (gif) {
			Gifs.avatar.drawRound(ms, x, y, size, size, round, alpha, 50);
		} else {
			Round.drawTextured(ms, avatar, new Rect(x, y, size, size), round, alpha);
		}
	}
}