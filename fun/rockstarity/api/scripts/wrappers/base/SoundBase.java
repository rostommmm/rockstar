package fun.rockstarity.api.scripts.wrappers.base;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;

import fun.rockstarity.Rockstar;
import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.scripts.helpers.LuaSound;
import fun.rockstarity.client.modules.other.Sounds;
import net.minecraft.util.ResourceLocation;

public class SoundBase implements IAccess {
	private LuaSound child;
	static Map<String, LuaSound> soundMap = new HashMap<>();
	
	public SoundBase(String url) {
		child = new LuaSound(url);
	}
	
	public void create() {
		child.setThread(new Thread(() -> {
			try {
				if (child.getUrl().contains("http://") || child.getUrl().contains("https://")) {
					URL url = new URL(child.getUrl());
					
					URLConnection connection = url.openConnection();
					connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11"); connection.connect(); InputStream audioSrc = connection.getInputStream();
					
					BufferedInputStream bufferedIn = new BufferedInputStream(audioSrc);
					
					AudioInputStream inputStream = AudioSystem.getAudioInputStream(bufferedIn);
					
					child.getClip().open(inputStream);
					inputStream.close();
				} else {
					File f = new File(child.getUrl().contains(":/") ? child.getUrl() : rock.getPath() + "scripts/" + child.getUrl());
					child.setFile(f);

					AudioInputStream inputStream = AudioSystem.getAudioInputStream(f.toURI().toURL());
					child.setStream(inputStream);
					
					child.getClip().open(inputStream);
					inputStream.close();
				}
				//soundMap.put(child.getUrl(), child);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}));
		
		child.getThread().start();
	}
	
	public void start() {
		String url = child.getUrl();
		child = new LuaSound(url);
		create();
		try {
			child.getThread().join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		child.getClip().start();
	}
	
	public void start(int count) {
		String url = child.getUrl();
		child = new LuaSound(url);
		create();
		try {
			child.getThread().join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			FloatControl vc = (FloatControl) child.getClip().getControl(FloatControl.Type.MASTER_GAIN);
			float newValue = (child.VOLUME * (0 - -80)) + -80;
			vc.setValue(newValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
		child.getClip().loop(count);
	}
	
	public void stop() {
		try {
			child.getThread().join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		child.getClip().stop();
		child.setPlaying(false);;
	}
	
	public float volume() {
		return child.VOLUME;
	}
	
	public void set_volume(float volume) {
		child.VOLUME = volume;
		
		try {
			FloatControl vc = (FloatControl) child.getClip().getControl(FloatControl.Type.MASTER_GAIN);
			float newValue = (volume * (0 - -80)) + -80;
			vc.setValue(newValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}