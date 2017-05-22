import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.jogamp.opengl.GL2;

public class TexImage {
	int w, h, tex;

	public TexImage(GL2 gl, String filename) {
		glTexImageTGAFile(gl, filename);
	}
	
	public void drawSprite(GL2 gl, int x, int y) {
		gl.glBindTexture(GL2.GL_TEXTURE_2D, this.tex);
		gl.glBegin(GL2.GL_QUADS);
		gl.glColor3ub((byte) -1, (byte) -1, (byte) -1);

		gl.glTexCoord2f(0, 1);
		gl.glVertex2i(x, y);
		gl.glTexCoord2f(1, 1);
		gl.glVertex2i(x + this.w, y);
		gl.glTexCoord2f(1, 0);
		gl.glVertex2i(x + this.w, y + this.h);
		gl.glTexCoord2f(0, 0);
		gl.glVertex2i(x, y + this.h);

		gl.glEnd();
	}
	
	private void glTexImageTGAFile(GL2 gl, String filename) {
		final int BPP = 4;

		DataInputStream file = null;
		try {
			// Open the file.
			// file = new DataInputStream(new FileInputStream(filename));
			file = new DataInputStream(Main.class.getResourceAsStream(filename));
		} catch (Exception ex) {
			System.err.format("File: %s -- Could not open for reading.", filename);
		}

		try {
			// Skip first two bytes of data we don't need.
			file.skipBytes(2);

			// Read in the image type. For our purposes the image type
			// should be either a 2 or a 3.
			int imageTypeCode = file.readByte();
			if (imageTypeCode != 2 && imageTypeCode != 3) {
				file.close();
				System.err.format("File: %s -- Unsupported TGA type: %d", filename, imageTypeCode);
			}

			// Skip 9 bytes of data we don't need.
			file.skipBytes(9);

			int imagew = Short.reverseBytes(file.readShort());
			int imageh = Short.reverseBytes(file.readShort());
			int bitCount = file.readByte();
			file.skipBytes(1);

			// Allocate space for the image data and read it in.
			byte[] bytes = new byte[imagew * imageh * BPP];

			// Read in data.
			if (bitCount == 32) {
				for (int it = 0; it < imagew * imageh; ++it) {
					bytes[it * BPP + 0] = file.readByte();
					bytes[it * BPP + 1] = file.readByte();
					bytes[it * BPP + 2] = file.readByte();
					bytes[it * BPP + 3] = file.readByte();
				}
			} else {
				for (int it = 0; it < imagew * imageh; ++it) {
					bytes[it * BPP + 0] = file.readByte();
					bytes[it * BPP + 1] = file.readByte();
					bytes[it * BPP + 2] = file.readByte();
					bytes[it * BPP + 3] = -1;
				}
			}

			file.close();

			// Load into OpenGL
			int[] texArray = new int[1];
			gl.glGenTextures(1, texArray, 0);
			int tex = texArray[0];
			gl.glBindTexture(GL2.GL_TEXTURE_2D, tex);
			gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA, imagew, imageh, 0, GL2.GL_BGRA, GL2.GL_UNSIGNED_BYTE,
					ByteBuffer.wrap(bytes));
			gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
			gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);

			this.w = imagew;
			this.h = imageh;
			this.tex = tex;
		} catch (IOException ex) {
			System.err.format("File: %s -- Unexpected end of file.", filename);
		}
	}
}
