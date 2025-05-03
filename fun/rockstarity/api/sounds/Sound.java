package fun.rockstarity.api.sounds;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;

import consts.NativeConsts;
import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.render.Converter;
import fun.rockstarity.api.helpers.system.ThreadManager;
import fun.rockstarity.api.secure.Debugger;
import fun.rockstarity.api.secure.nativeapi.NativeHelper;
import fun.rockstarity.client.modules.other.Sounds;

/**
 * @author ConeTin
 * @since 29 мар. 2024 г.
 */

public class Sound implements IAccess {

	private final String name;
	private AudioInputStream stream;
	private Clip clip;
	private File file;
	private Thread creating;
	
	public Sound(String name) {
		this.name = name;

		try {
			this.clip = AudioSystem.getClip();
		} catch (LineUnavailableException e) {
			Debugger.print(e);
		}
		
		this.creating = ThreadManager.run(() -> {
			this.download();
			this.create();
		});
	}
	
	public void play(int count) {
		ThreadManager.run(() -> {
			try {
				this.creating.join();
			} catch (InterruptedException e) {
				Debugger.print(e);
			}
			
			clip.loop(count);
		});
	}
	
	public void play() {
		this.play(0);
	}
	
	public void stop() {
		try {
			this.creating.join();
		} catch (InterruptedException e) {
			Debugger.print(e);
		}
		
		clip.stop();
	}
	
	private void download() {
		File soundFile = new File(NativeConsts.PATH + "/" + name + ".wav");

		if (!soundFile.exists()) {
		    try {
		        soundFile.getParentFile().mkdirs(); // Create parent directories if they don't exist
		        String link = NativeHelper.getSoundResource(this.name);
				String resourcePath = (link.startsWith("/assets/") ? "" : "/assets/minecraft/rockstar/") + link;

		        try (InputStream inputStream = Sound.class.getResourceAsStream(resourcePath);
					 FileOutputStream outputStream = new FileOutputStream(soundFile)) {
		            byte[] buffer = new byte[4096];
		            int bytesRead;
		            int totalBytesRead = 0;
		            while ((bytesRead = inputStream.read(buffer)) != -1) {
		                outputStream.write(buffer, 0, bytesRead);
		                totalBytesRead += bytesRead;
		            }
		        }
		    } catch (IOException e) {
		    	Debugger.print(e);
		    }
		}
	}
	
	private void create() {
		try {
			File f = file = new File(NativeConsts.PATH + "/" + this.name + ".wav");

			AudioInputStream inputStream = stream = AudioSystem.getAudioInputStream(f.toURI().toURL());
			
			clip.open(inputStream);
			
			FloatControl vc = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
			float volume = rock.getModules().get(Sounds.class).getVolume().get();
			float newValue = (volume * (0 - -80)) + -80;
			vc.setValue(newValue);
		} catch (Exception e) {
			Debugger.print(e);
		}
	}
	
}
