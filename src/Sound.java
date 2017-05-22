import javax.sound.sampled.*;
import java.io.File;

// http://stackoverflow.com/questions/577724/trouble-playing-wav-in-java/577926#577926
public class Sound {
	public static void playClipLoop(File file) throws Exception {
		Clip clip = AudioSystem.getClip();
		clip.open(AudioSystem.getAudioInputStream(file));
		clip.start();
		clip.loop(Clip.LOOP_CONTINUOUSLY);
	}
	public static void playClip(File file) throws Exception {
		Clip clip = AudioSystem.getClip();
		clip.open(AudioSystem.getAudioInputStream(file));
		clip.start();
	}
}
