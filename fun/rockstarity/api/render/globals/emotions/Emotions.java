package fun.rockstarity.api.render.globals.emotions;

import java.util.ArrayList;

import fun.rockstarity.Rockstar;
import fun.rockstarity.api.connection.globals.SyncServer;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.render.globals.emotions.instance.Emotion;
import fun.rockstarity.api.render.globals.emotions.instance.EmotionType;
import fun.rockstarity.api.render.globals.emotions.instance.list.*;
import fun.rockstarity.api.render.globals.emotions.shared.EmotionsWheel;
import lombok.Getter;
import net.minecraft.block.BreakableBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import org.luaj.vm2.ast.Stat;

/**
 * @author ConeTin
 * @since 6 авг. 2024 г.
 */

public class Emotions {
	
	@Getter
	private final ArrayList<Emotion> activeEmotions = new ArrayList<>();
	private final EmotionsWheel wheel;
	private final EmotionsPlayer emotionsPlayer;
	
	public Emotions() {
		this.emotionsPlayer = new EmotionsPlayer(this);
		this.wheel = new EmotionsWheel(this);
	}

	// TODO вызывать ивент ток когда надо
	public void onEvent(Event event) {
		emotionsPlayer.onEvent(event);
		
        wheel.onEvent(event);
    }
	
	public void playEmotion(LivingEntity player, EmotionType emotionType) {
		Emotion emotion = null;
		
		if (player instanceof ClientPlayerEntity) {
			SyncServer.send(emotionType);
		}

		switch (emotionType) {
		case DEB:
			emotion = new DebEmotion(player);
			break;
		case FLOSS:
			emotion = new FlossEmotion(player);
			break;
		case GET_GRIDDY:
			emotion = new GetGriddyEmotion(player);
			break;
		case MASTURBATE:
			emotion = new MasturbateEmotion(player);
			break;
		case HELLO:
			emotion = new HelloEmotion(player);
			break;
		case HAPPY:
			emotion = new HappyEmotion(player);
			break;
//		case CRY:
//			emotion = new CryEmotion(player);
//			break;
//		case CLAP:
//			emotion = new ClapEmotion(player);
//			break;
		/*
		case CONDITIONS:
			emotion = new ConditionsEmotion(player);
			break;
			*/
		//case ACROBATIC:
		//	emotion = new AcrobaticEmotion(player);
		//	break;
		default:
			break;
		}
		
		this.activeEmotions.add(emotion);
	}
}
