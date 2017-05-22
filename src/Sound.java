import java.io.DataInputStream;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

// http://stackoverflow.com/questions/577724/trouble-playing-wav-in-java/577926#577926
public class Sound {
	public static void playClip(String filename) throws Exception {
		DataInputStream file = null;
		try {
			// Open the file.
			// file = new DataInputStream(new FileInputStream(filename));
			file = new DataInputStream(Main.class.getResourceAsStream(filename));
		} catch (Exception ex) {
			System.err.format("File: %s -- Could not open for reading.", filename);
		}
		Clip clip = AudioSystem.getClip();
		clip.open(AudioSystem.getAudioInputStream(file));
		clip.start();
	}
}
