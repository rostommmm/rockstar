package fun.rockstarity.api.scripts.helpers;

import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.client.modules.other.Sounds;
import lombok.Getter;
import lombok.Setter;

@Getter
public class LuaSound implements IAccess {
	private String url;
	public static float VOLUME = 0.6f;
	private Clip clip;
	@Setter
	private AudioInputStream stream;
	@Setter
	private Thread thread;
	@Setter
	private boolean playing;
	@Setter
	private File file;
	private boolean runFirstly = true;

	public LuaSound(String url) {
		this.url = url;
		try {
			Clip clip = AudioSystem.getClip();
			this.clip = clip;
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}
	
	public void startUntilTurns() {
		create();
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		clip.loop(99);
		playing = true;
	}

	public void start() {
		create();
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		clip.start();
	}

	public void create() {
		thread = new Thread(() -> {
			try {
				File f = file = new File(rock.getPath() + "sounds/" + url + ".wav");

				AudioInputStream inputStream = stream = AudioSystem.getAudioInputStream(f.toURI().toURL());
				
				clip.open(inputStream);
				
				FloatControl vc = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
				float newValue = VOLUME;
				vc.setValue(newValue);
				
				clip.start();
				//inputStream.close();
			} catch (Exception e) {
				//if (this.runFirstly) {
				//	this.runFirstly = false;
				//}
				e.printStackTrace();
			}
		});
		
		thread.start();
	}

	public void stop() {
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		clip.stop();
		//clip.close();
		playing = false;
	}
}