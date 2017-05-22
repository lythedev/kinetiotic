import java.io.BufferedInputStream;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Sound {
	public static void playClip(String filename) throws Exception {
		InputStream file = null;
		try {
			// Open the file.
			// file = new DataInputStream(new FileInputStream(filename));
			file = Main.class.getResourceAsStream(filename);//new DataInputStream(Main.class.getResourceAsStream(filename));
		} catch (Exception ex) {
			System.err.format("File: %s -- Could not open for reading.", filename);
		}
		InputStream bufferedIn = new BufferedInputStream(file);
		AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);
		Clip clip = AudioSystem.getClip();
		clip.open(audioStream);
		clip.start();
	}
}